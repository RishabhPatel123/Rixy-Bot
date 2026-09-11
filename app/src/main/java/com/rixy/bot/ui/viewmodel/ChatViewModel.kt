package com.rixy.bot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rixy.bot.data.model.ChatMessageEntity
import com.rixy.bot.data.model.PlannedTaskEntity
import com.rixy.bot.data.prefs.ImageStore
import com.rixy.bot.data.prefs.SecretsStore
import com.rixy.bot.data.repo.ChatRepository
import com.rixy.bot.network.ChatTurn
import com.rixy.bot.network.GeminiApiService
import com.rixy.bot.network.GeminiException
import com.rixy.bot.network.PlanItem
import com.rixy.bot.network.Source
import com.rixy.bot.network.StreamEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ChatUiEvent {
    data class Error(val message: String, val canRetry: Boolean) : ChatUiEvent
    data object PlanSaved : ChatUiEvent
    data class PlanFailed(val message: String) : ChatUiEvent
    data class ImageSavedToGallery(val success: Boolean) : ChatUiEvent
    data class Imported(val count: Int) : ChatUiEvent
    data class PermissionNeeded(val permission: String) : ChatUiEvent
}

/** Awaiting the user's verdict on an irreversible agent action. */
data class PendingConfirmation(
    val title: String,
    val summary: String,
    val onResult: (allowed: Boolean, alwaysAllow: Boolean) -> Unit,
)

/** What the input bar should do with the next message. */
enum class SendMode { CHAT, PLAN, IMAGE, AGENT }

class ChatViewModel(
    private val repository: ChatRepository,
    private val gemini: GeminiApiService,
    private val secrets: SecretsStore,
    val imageStore: ImageStore,
    private val toolRegistry: com.rixy.bot.agent.ToolRegistry,
    private val appContext: android.content.Context,
) : ViewModel() {

    val chats = repository.chats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedChatId = MutableStateFlow<Long?>(null)
    val selectedChatId = _selectedChatId.asStateFlow()

    val messages: StateFlow<List<ChatMessageEntity>> = _selectedChatId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _streamingText = MutableStateFlow<String?>(null)
    val streamingText = _streamingText.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming = _isStreaming.asStateFlow()

    private val _isPlanning = MutableStateFlow(false)
    val isPlanning = _isPlanning.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage = _isGeneratingImage.asStateFlow()

    private val _isAgentWorking = MutableStateFlow(false)
    val isAgentWorking = _isAgentWorking.asStateFlow()

    private val _agentStatus = MutableStateFlow<String?>(null)
    val agentStatus = _agentStatus.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<PendingConfirmation?>(null)
    val pendingConfirmation = _pendingConfirmation.asStateFlow()

    private var permissionDeferred: kotlinx.coroutines.CompletableDeferred<Boolean>? = null

    private val _attachedImagePath = MutableStateFlow<String?>(null)
    val attachedImagePath = _attachedImagePath.asStateFlow()

    private val _events = MutableSharedFlow<ChatUiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    val hasApiKey get() = secrets.resolveApiKey() != null

    private var streamJob: Job? = null
    private var streamingChatId: Long? = null

    fun selectChat(id: Long?) {
        if (_isStreaming.value) stopStreaming()
        _selectedChatId.value = id
    }

    fun newChat() {
        if (_isStreaming.value) stopStreaming()
        _selectedChatId.value = null
    }

    fun deleteChat(id: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteChat(id) }
            if (_selectedChatId.value == id) _selectedChatId.value = null
        }
    }

    fun renameChat(id: Long, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { runCatching { repository.renameChat(id, trimmed) } }
    }

    fun attachImage(path: String?) {
        _attachedImagePath.value = path
    }

    /** Entry point from the input bar. */
    fun sendMessage(rawText: String, mode: SendMode, webGrounded: Boolean) {
        when (mode) {
            SendMode.PLAN -> requestPlan(rawText)
            SendMode.IMAGE -> requestImage(rawText)
            SendMode.AGENT -> runAgent(rawText)
            SendMode.CHAT -> sendChat(rawText, webGrounded)
        }
    }

    /** Agent mode: let Gemini call device tools to fulfill the request. */
    private fun runAgent(rawRequest: String) {
        val request = rawRequest.trim()
        if (request.isEmpty() || busy()) return

        viewModelScope.launch {
            val apiKey = secrets.resolveApiKey()
            val model = secrets.geminiModel
            if (apiKey == null) {
                _events.emit(ChatUiEvent.Error(NO_KEY_MESSAGE, canRetry = true))
                return@launch
            }

            val chatId = ensureChat(request)
            runCatching { repository.addMessage(chatId, isFromUser = true, text = request) }
            val history = runCatching {
                repository.getHistory(chatId)
                    .dropLast(1) // the request itself is appended by the executor
                    .takeLast(20)
                    .map { it.isFromUser to it.text }
            }.getOrDefault(emptyList())

            val transport = object : com.rixy.bot.agent.AgentTransport {
                override suspend fun turn(
                    contents: org.json.JSONArray,
                    tools: org.json.JSONArray,
                ): String = gemini.agentTurn(contents, tools, apiKey, model)
            }
            val executor = com.rixy.bot.agent.AgentExecutor(
                appContext = appContext,
                registry = toolRegistry,
                transport = transport,
                permissionGate = { tool ->
                    val permission = tool.requiredPermission
                    val alreadyGranted = permission != null &&
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            appContext, permission
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    when {
                        permission == null || alreadyGranted -> true
                        else -> {
                            val deferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
                            permissionDeferred = deferred
                            _events.emit(ChatUiEvent.PermissionNeeded(permission))
                            val granted = deferred.await()
                            permissionDeferred = null
                            granted
                        }
                    }
                },
                confirmationGate = { tool, args ->
                    val skipPrompt = secrets.agentFullAuto || tool.name in secrets.agentAllowedTools
                    if (skipPrompt) {
                        true
                    } else {
                        val deferred = kotlinx.coroutines.CompletableDeferred<Pair<Boolean, Boolean>>()
                        _pendingConfirmation.value = PendingConfirmation(
                            title = tool.name,
                            summary = tool.summarize(args),
                            onResult = { allowed, alwaysAllow -> deferred.complete(allowed to alwaysAllow) },
                        )
                        val (allowed, alwaysAllow) = deferred.await()
                        _pendingConfirmation.value = null
                        if (allowed && alwaysAllow) {
                            secrets.agentAllowedTools = secrets.agentAllowedTools + tool.name
                        }
                        allowed
                    }
                },
                onAction = { status -> _agentStatus.value = status },
            )

            _isAgentWorking.value = true
            try {
                when (val outcome = executor.run(history, request)) {
                    is com.rixy.bot.agent.AgentExecutor.Outcome.Reply -> {
                        val text = if (outcome.actions.isEmpty()) outcome.text
                        else outcome.actions.joinToString("\n") + "\n\n" + outcome.text
                        repository.addMessage(chatId, isFromUser = false, text = text)
                    }
                    is com.rixy.bot.agent.AgentExecutor.Outcome.Failed ->
                        _events.emit(ChatUiEvent.Error(outcome.message, canRetry = true))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(ChatUiEvent.Error(userMessage(e), canRetry = true))
            } finally {
                _isAgentWorking.value = false
                _agentStatus.value = null
                _pendingConfirmation.value = null
            }
        }
    }

    /** Called by the UI after a runtime permission requested by the agent resolves. */
    fun onPermissionResult(granted: Boolean) {
        permissionDeferred?.complete(granted)
    }

    private fun sendChat(rawText: String, webGrounded: Boolean) {
        val prompt = rawText.trim()
        val attachment = _attachedImagePath.value
        if ((prompt.isEmpty() && attachment == null) || busy()) return
        _attachedImagePath.value = null

        viewModelScope.launch {
            val chatId = ensureChat(prompt.ifEmpty { "Image" })
            runCatching {
                repository.addMessage(chatId, isFromUser = true, text = prompt, imagePath = attachment)
            }

            val apiKey = secrets.resolveApiKey()
            val model = secrets.geminiModel
            if (apiKey == null) {
                _events.emit(ChatUiEvent.Error(NO_KEY_MESSAGE, canRetry = true))
                return@launch
            }

            val history = buildHistory(chatId, attachment)
            _isStreaming.value = true
            _streamingText.value = ""
            streamingChatId = chatId
            val buffer = StringBuilder()
            val sources = mutableListOf<Source>()

            streamJob = launch {
                try {
                    gemini.streamChat(history, apiKey, model, webGrounded).collect { event ->
                        when (event) {
                            is StreamEvent.Delta -> {
                                buffer.append(event.text)
                                _streamingText.value = buffer.toString()
                            }
                            is StreamEvent.Sources -> sources += event.sources
                        }
                    }
                    val finalText = buffer.toString()
                    if (finalText.isNotBlank() || attachment != null) {
                        repository.addMessage(
                            chatId,
                            isFromUser = false,
                            text = finalText,
                            sourcesJson = sourcesToJson(sources),
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val partial = buffer.toString()
                    if (partial.isNotBlank()) {
                        runCatching {
                            repository.addMessage(
                                chatId,
                                isFromUser = false,
                                text = "$partial\n\n*${STOPPED_NOTE}*",
                                sourcesJson = sourcesToJson(sources),
                            )
                        }
                    } else {
                        _events.emit(ChatUiEvent.Error(userMessage(e), canRetry = true))
                    }
                } finally {
                    _isStreaming.value = false
                    _streamingText.value = null
                    streamingChatId = null
                }
            }
        }
    }

    /** Image mode: generate an image for the prompt (an attachment acts as a style reference). */
    private fun requestImage(rawText: String) {
        val prompt = rawText.trim()
        val attachment = _attachedImagePath.value
        if (prompt.isEmpty() && attachment == null) return
        if (busy()) return
        _attachedImagePath.value = null

        viewModelScope.launch {
            val chatId = ensureChat(prompt.ifEmpty { "Image" })
            runCatching {
                repository.addMessage(chatId, isFromUser = true, text = prompt, imagePath = attachment)
            }

            val apiKey = secrets.resolveApiKey() ?: run {
                _events.emit(ChatUiEvent.Error(NO_KEY_MESSAGE, canRetry = false))
                return@launch
            }

            _isGeneratingImage.value = true
            try {
                val image = gemini.generateImage(prompt, apiKey)
                val savedPath = imageStore.saveBase64(image.base64, image.mimeType)
                repository.addMessage(
                    chatId,
                    isFromUser = false,
                    text = image.caption.ifEmpty { "Generated image" },
                    imagePath = savedPath,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(ChatUiEvent.Error(userMessage(e), canRetry = false))
            } finally {
                _isGeneratingImage.value = false
            }
        }
    }

    /** Plan mode: decompose a goal into tasks, rendered as a plan card in the chat. */
    fun requestPlan(rawGoal: String) {
        val goal = rawGoal.trim()
        if (goal.isEmpty() || busy()) return

        viewModelScope.launch {
            val chatId = ensureChat(goal)
            runCatching { repository.addMessage(chatId, isFromUser = true, text = PLAN_PREFIX + goal) }

            val apiKey = secrets.resolveApiKey()
            val model = secrets.geminiModel
            if (apiKey == null) {
                _events.emit(ChatUiEvent.Error(NO_KEY_MESSAGE, canRetry = false))
                return@launch
            }

            _isPlanning.value = true
            try {
                val plan = gemini.generatePlan(goal, apiKey, model)
                val jsonArray = JSONArray().apply {
                    plan.forEach { item ->
                        put(
                            JSONObject()
                                .put("title", item.title)
                                .put("description", item.description)
                                .put("priority", item.priority)
                        )
                    }
                }
                repository.addMessage(
                    chatId,
                    isFromUser = false,
                    text = "Here's a plan for **${goal.take(80)}**:",
                    planJson = jsonArray.toString(),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(ChatUiEvent.PlanFailed(userMessage(e)))
            } finally {
                _isPlanning.value = false
            }
        }
    }

    fun savePlanToTasks(planJson: String) {
        viewModelScope.launch {
            try {
                val chatId = _selectedChatId.value ?: return@launch
                val arr = JSONArray(planJson)
                val now = System.currentTimeMillis()
                val tasks = buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        add(
                            PlannedTaskEntity(
                                chatId = chatId,
                                title = obj.optString("title"),
                                description = obj.optString("description"),
                                priority = obj.optString("priority", "MEDIUM"),
                                status = PlannedTaskEntity.STATUS_OPEN,
                                createdAt = now,
                            )
                        )
                    }
                }
                repository.saveTasks(tasks)
                _events.emit(ChatUiEvent.PlanSaved)
            } catch (e: Exception) {
                _events.emit(ChatUiEvent.PlanFailed(userMessage(e)))
            }
        }
    }

    fun saveImageToGallery(path: String) {
        viewModelScope.launch {
            val ok = runCatching {
                imageStore.saveToGallery(path, "rixy_${System.currentTimeMillis()}")
            }.getOrDefault(false)
            _events.emit(ChatUiEvent.ImageSavedToGallery(ok))
        }
    }

    /** Imports a parsed conversation as a new chat and selects it. */
    fun importConversation(parsed: List<com.rixy.bot.util.ConversationImportParser.ParsedMessage>) {
        if (parsed.isEmpty()) return
        viewModelScope.launch {
            val title = "Imported · " + SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                .format(Date())
            runCatching {
                val chatId = repository.createChat(title, System.currentTimeMillis())
                repository.importConversation(chatId, parsed.map { it.isFromUser to it.text })
                _selectedChatId.value = chatId
                _events.emit(ChatUiEvent.Imported(parsed.size))
            }
        }
    }

    /** Stops generation and keeps whatever streamed so far. */
    fun stopStreaming() {
        val chatId = streamingChatId
        val partial = _streamingText.value
        streamJob?.cancel()
        if (chatId != null && !partial.isNullOrBlank()) {
            viewModelScope.launch {
                runCatching {
                    repository.addMessage(chatId, isFromUser = false, text = "$partial\n\n*${STOPPED_NOTE}*")
                }
            }
        }
    }

    private fun busy() =
        _isStreaming.value || _isPlanning.value || _isGeneratingImage.value || _isAgentWorking.value

    private suspend fun ensureChat(firstPrompt: String): Long =
        _selectedChatId.value
            ?: repository.createChat(deriveTitle(firstPrompt), System.currentTimeMillis())
                .also { _selectedChatId.value = it }

    /**
     * Builds API history: the newest message keeps its actual image bytes; older
     * image messages degrade to a "[image attached]" text placeholder to keep
     * request size predictable.
     */
    private suspend fun buildHistory(chatId: Long, currentAttachment: String?): List<ChatTurn> {
        val history = runCatching { repository.getHistory(chatId) }.getOrDefault(emptyList())
        return history.takeLast(HISTORY_LIMIT).mapIndexed { index, msg ->
            val isLast = index == history.takeLast(HISTORY_LIMIT).lastIndex
            val imagePath = msg.imagePath
            val carriesImage = isLast && msg.isFromUser && imagePath != null && imagePath == currentAttachment
            ChatTurn(
                isFromUser = msg.isFromUser,
                text = when {
                    imagePath != null && !carriesImage && msg.text.isEmpty() -> "[image attached]"
                    imagePath != null && !carriesImage -> msg.text + "\n[image attached]"
                    else -> msg.text
                },
                imageBase64 = if (carriesImage) imageStore.toBase64(imagePath!!) else null,
                imageMimeType = if (carriesImage) imageStore.mimeTypeFor(imagePath!!) else null,
            )
        }
    }

    private fun sourcesToJson(sources: List<Source>): String? =
        if (sources.isEmpty()) null
        else JSONArray().apply {
            sources.forEach { put(JSONObject().put("title", it.title).put("uri", it.uri)) }
        }.toString()

    private fun userMessage(e: Exception): String = when (e) {
        is GeminiException.Auth -> "Your API key was rejected. Check it in Settings."
        is GeminiException.ModelNotFound -> "Model not found — pick another in Settings."
        is GeminiException.RateLimited -> "Rate limited. Wait a moment and try again."
        is GeminiException.Network -> "Network error. Check your connection."
        else -> "Something went wrong. Try again."
    }

    companion object {
        private const val HISTORY_LIMIT = 30
        private const val STOPPED_NOTE = "Response stopped"
        private const val PLAN_PREFIX = "Plan this goal: "
        const val NO_KEY_MESSAGE =
            "No API key configured. Add your Gemini key in Settings to start chatting."

        /** Derives a chat title from the first message. */
        fun deriveTitle(text: String): String {
            val clean = text.replace('\n', ' ').trim()
            return if (clean.length <= 48) clean.ifEmpty { "New chat" }
            else clean.take(45).trimEnd() + "…"
        }
    }
}
