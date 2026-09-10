package com.rixy.bot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rixy.bot.data.model.ChatMessageEntity
import com.rixy.bot.data.model.PlannedTaskEntity
import com.rixy.bot.data.prefs.SecretsStore
import com.rixy.bot.data.repo.ChatRepository
import com.rixy.bot.network.ChatTurn
import com.rixy.bot.network.GeminiApiService
import com.rixy.bot.network.GeminiException
import com.rixy.bot.network.PlanItem
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

sealed interface ChatUiEvent {
    data class Error(val message: String, val canRetry: Boolean) : ChatUiEvent
    data object PlanSaved : ChatUiEvent
    data class PlanFailed(val message: String) : ChatUiEvent
}

class ChatViewModel(
    private val repository: ChatRepository,
    private val gemini: GeminiApiService,
    private val secrets: SecretsStore,
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

    fun sendMessage(rawText: String) {
        val prompt = rawText.trim()
        if (prompt.isEmpty() || _isStreaming.value || _isPlanning.value) return

        viewModelScope.launch {
            val chatId = _selectedChatId.value
                ?: repository.createChat(deriveTitle(prompt), System.currentTimeMillis())
                    .also { _selectedChatId.value = it }

            runCatching { repository.addMessage(chatId, isFromUser = true, text = prompt) }

            val apiKey = secrets.resolveApiKey()
            val model = secrets.geminiModel
            if (apiKey == null) {
                _events.emit(ChatUiEvent.Error(NO_KEY_MESSAGE, canRetry = true))
                return@launch
            }

            val history = runCatching {
                repository.getHistory(chatId).takeLast(HISTORY_LIMIT).map {
                    ChatTurn(isFromUser = it.isFromUser, text = it.text)
                }
            }.getOrDefault(emptyList())

            _isStreaming.value = true
            _streamingText.value = ""
            streamingChatId = chatId
            val buffer = StringBuilder()

            streamJob = launch {
                try {
                    gemini.streamChat(history, apiKey, model).collect { delta ->
                        buffer.append(delta)
                        _streamingText.value = buffer.toString()
                    }
                    val finalText = buffer.toString()
                    if (finalText.isNotBlank()) {
                        repository.addMessage(chatId, isFromUser = false, text = finalText)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val partial = buffer.toString()
                    if (partial.isNotBlank()) {
                        runCatching {
                            repository.addMessage(chatId, isFromUser = false, text = "$partial\n\n*${STOPPED_NOTE}*")
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

    /** Plan mode: decompose a goal into tasks, rendered as a plan card in the chat. */
    fun requestPlan(rawGoal: String) {
        val goal = rawGoal.trim()
        if (goal.isEmpty() || _isStreaming.value || _isPlanning.value) return

        viewModelScope.launch {
            val chatId = _selectedChatId.value
                ?: repository.createChat(deriveTitle(goal), System.currentTimeMillis())
                    .also { _selectedChatId.value = it }

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
