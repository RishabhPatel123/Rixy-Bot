package com.rixy.bot.ui.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rixy.bot.R
import com.rixy.bot.data.model.ChatMessageEntity
import com.rixy.bot.ui.components.EnterAnimation
import com.rixy.bot.ui.components.ErrorBanner
import com.rixy.bot.ui.components.ErrorBubble
import com.rixy.bot.ui.components.RixyBubble
import com.rixy.bot.ui.components.TypingIndicator
import com.rixy.bot.ui.components.UserBubble
import com.rixy.bot.ui.theme.InputBarShape
import com.rixy.bot.ui.theme.Motion
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.Surface
import com.rixy.bot.ui.theme.TextTertiary
import com.rixy.bot.ui.theme.Warning
import com.rixy.bot.ui.viewmodel.ChatUiEvent
import com.rixy.bot.ui.viewmodel.ChatViewModel
import com.rixy.bot.ui.viewmodel.SendMode
import kotlinx.coroutines.launch
import org.json.JSONArray

@Composable
fun ChatScreen(
    chatTitle: String,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ChatViewModel,
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()
    val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
    val isPlanning by viewModel.isPlanning.collectAsStateWithLifecycle()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsStateWithLifecycle()
    val isAgentWorking by viewModel.isAgentWorking.collectAsStateWithLifecycle()
    val agentStatus by viewModel.agentStatus.collectAsStateWithLifecycle()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsStateWithLifecycle()
    val attachedImagePath by viewModel.attachedImagePath.collectAsStateWithLifecycle()
    val hasKey = viewModel.hasApiKey
    val snackbarHostState = remember { SnackbarHostState() }
    var inputText by remember { mutableStateOf("") }

    // ---- Text-to-speech ----
    var speakingMessageId by remember { mutableStateOf<Long?>(null) }
    val ttsReady = remember { mutableStateOf(false) }
    val tts = remember {
        runCatching {
            var engine: TextToSpeech? = null
            engine = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    engine?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            utteranceId?.toLongOrNull()?.let { speakingMessageId = null }
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            utteranceId?.toLongOrNull()?.let { speakingMessageId = null }
                        }
                    })
                    ttsReady.value = true
                }
            }
            engine
        }.getOrNull()
    }
    DisposableEffect(Unit) { onDispose { tts?.shutdown() } }
    val speak: (ChatMessageEntity) -> Unit = { message ->
        val engine = tts
        if (engine != null) {
            if (speakingMessageId == message.id) {
                engine.stop()
                speakingMessageId = null
            } else {
                speakingMessageId = message.id
                engine.speak(
                    message.text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    message.id.toString(),
                )
            }
        }
    }

    // ---- Speech-to-text ----
    var showListening by remember { mutableStateOf(false) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) showListening = true }
    val agentPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onPermissionResult(granted) }
    val onMicTap = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) showListening = true else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // ---- Image attachment ----
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.attachImage(viewModel.imageStore.copyFromUri(it)) }
    }

    val gallerySavedMessage = stringResource(R.string.chat_image_saved)
    val gallerySaveFailedMessage = stringResource(R.string.chat_image_save_failed)
    val importedMessage = stringResource(R.string.import_success)
    val planSavedMessage = stringResource(R.string.chat_plan_saved)
    val chatCopiedMessage = stringResource(R.string.chat_copy_all_done)
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val copyScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatUiEvent.Error -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Settings",
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) onOpenSettings()
                }
                is ChatUiEvent.PlanFailed ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                ChatUiEvent.PlanSaved ->
                    snackbarHostState.showSnackbar(planSavedMessage, duration = SnackbarDuration.Short)
                is ChatUiEvent.ImageSavedToGallery ->
                    snackbarHostState.showSnackbar(
                        if (event.success) gallerySavedMessage else gallerySaveFailedMessage,
                        duration = SnackbarDuration.Short,
                    )
                is ChatUiEvent.Imported ->
                    snackbarHostState.showSnackbar(
                        importedMessage.format(event.count),
                        duration = SnackbarDuration.Short,
                    )
                is ChatUiEvent.PermissionNeeded ->
                    agentPermissionLauncher.launch(event.permission)
            }
        }
    }

    val busy = isStreaming || isPlanning || isGeneratingImage || isAgentWorking
    val listIsEmpty = messages.isEmpty() && streamingText == null && !isPlanning &&
        !isGeneratingImage && !isAgentWorking

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        ChatTopBar(
            title = chatTitle,
            onOpenDrawer = onOpenDrawer,
            onNewChat = onNewChat,
            onCopyChat = {
                val transcript = messages
                    .filterNot { it.isError }
                    .joinToString("\n\n") {
                        (if (it.isFromUser) "You: " else "Rixy: ") + it.text
                    }
                if (transcript.isNotBlank()) {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(transcript))
                    copyScope.launch {
                        snackbarHostState.showSnackbar(
                            chatCopiedMessage,
                            duration = SnackbarDuration.Short,
                        )
                    }
                }
            },
            messagesEmpty = messages.isEmpty(),
        )
        SnackbarHost(snackbarHostState)

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = listIsEmpty,
                transitionSpec = {
                    (fadeIn(tween(Motion.FADE_MS)) + scaleIn(initialScale = 0.985f, animationSpec = tween(Motion.FADE_MS)))
                        .togetherWith(fadeOut(tween(Motion.FADE_MS / 2)))
                },
                label = "chat-content",
            ) { isEmpty ->
                if (isEmpty) {
                    ChatEmptyState(
                        hasKey = hasKey,
                        onOpenSettings = onOpenSettings,
                        onSuggestion = { inputText = it },
                    )
                } else {
                    MessageList(
                        messages = messages,
                        streamingText = streamingText,
                        isPlanning = isPlanning,
                        isGeneratingImage = isGeneratingImage,
                        isAgentWorking = isAgentWorking,
                        agentStatus = agentStatus,
                        imageStore = viewModel.imageStore,
                        speakingMessageId = speakingMessageId,
                        onSpeak = speak,
                        onPlanSave = viewModel::savePlanToTasks,
                        onSaveImage = viewModel::saveImageToGallery,
                        onRetry = viewModel::retryMessage,
                    )
                }
            }
        }

        ChatInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            attachmentPath = attachedImagePath,
            onAttach = {
                pickImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveAttachment = { viewModel.attachImage(null) },
            onMic = onMicTap,
            isBusy = busy,
            onSend = { value, mode, webGrounded ->
                viewModel.sendMessage(value, mode, webGrounded)
                inputText = ""
            },
            onStop = viewModel::stopStreaming,
        )
    }

    if (showListening) {
        ListeningSheet(
            onDismiss = { showListening = false },
            onConfirm = { transcript ->
                showListening = false
                if (transcript.isNotBlank()) inputText = transcript
            },
        )
    }

    pendingConfirmation?.let { confirmation ->
        var alwaysAllow by remember(confirmation) { mutableStateOf(false) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmation.onResult(false, false) },
            title = { Text(stringResource(R.string.agent_confirm_title)) },
            text = {
                Column {
                    Text(
                        confirmation.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = alwaysAllow,
                            onCheckedChange = { alwaysAllow = it },
                        )
                        Text(
                            stringResource(R.string.agent_confirm_always),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { confirmation.onResult(true, alwaysAllow) }) {
                    Text(stringResource(R.string.agent_confirm_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmation.onResult(false, false) }) {
                    Text(stringResource(R.string.agent_confirm_deny))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun ChatTopBar(
    title: String,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onCopyChat: () -> Unit,
    messagesEmpty: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xs, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenDrawer) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = stringResource(R.string.drawer_chats_title),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (!messagesEmpty) {
            IconButton(onClick = onCopyChat) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = stringResource(R.string.chat_copy_all),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        IconButton(onClick = onNewChat) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.chat_new),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessageEntity>,
    streamingText: String?,
    isPlanning: Boolean,
    isGeneratingImage: Boolean,
    isAgentWorking: Boolean,
    agentStatus: String?,
    imageStore: com.rixy.bot.data.prefs.ImageStore,
    speakingMessageId: Long?,
    onSpeak: (ChatMessageEntity) -> Unit,
    onPlanSave: (String) -> Unit,
    onSaveImage: (String) -> Unit,
    onRetry: (ChatMessageEntity) -> Unit,
) {
    val listState = rememberLazyListState()
    val initialTopId = remember { messages.maxOfOrNull { it.id } ?: Long.MIN_VALUE }
    LaunchedEffect(messages.size, streamingText, isPlanning, isGeneratingImage, isAgentWorking) {
        if (messages.isNotEmpty() || streamingText != null || isPlanning || isGeneratingImage || isAgentWorking) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        items(messages, key = { it.id }) { message ->
            val isNew = message.id > initialTopId
            Box(modifier = Modifier.animateItem()) {
                when {
                    message.isError -> ErrorBubble(
                        text = message.text,
                        onRetry = { onRetry(message) },
                        animate = isNew,
                    )
                    message.isFromUser -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        UserBubble(
                            text = message.text,
                            imagePath = message.imagePath,
                            imageStore = imageStore,
                            animate = isNew,
                        )
                    }
                    message.planJson != null -> PlanCard(message.planJson, onPlanSave, animate = isNew)
                    else -> RixyBubble(
                        text = message.text,
                        imagePath = message.imagePath,
                        sourcesJson = message.sourcesJson,
                        imageStore = imageStore,
                        animate = isNew,
                        speaking = speakingMessageId == message.id,
                        onSpeak = { onSpeak(message) },
                        onSaveImage = message.imagePath?.let { path -> { onSaveImage(path) } },
                    )
                }
            }
        }
        if (streamingText != null) {
            item(key = "streaming") {
                Box(modifier = Modifier.animateItem()) {
                    RixyBubble(streamingText, streaming = true)
                }
            }
        }
        if (isPlanning && streamingText == null) {
            item(key = "planning") {
                Text(
                    stringResource(R.string.chat_plan_generating),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
            }
        }
        if (isGeneratingImage) {
            item(key = "generating-image") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    TypingIndicator()
                    Text(
                        stringResource(R.string.chat_image_generating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                    )
                }
            }
        }
        if (isAgentWorking) {
            item(key = "agent-working") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    TypingIndicator()
                    Text(
                        agentStatus ?: stringResource(R.string.agent_working),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(planJson: String, onPlanSave: (String) -> Unit, animate: Boolean = true) {
    val items = remember(planJson) {
        runCatching {
            val arr = JSONArray(planJson)
            buildList {
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { obj ->
                        add(
                            Triple(
                                obj.optString("title"),
                                obj.optString("description"),
                                obj.optString("priority", "MEDIUM"),
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        items.indices.forEach { index ->
            val (title, description, priority) = items[index]
            val stagger = if (animate) index * Motion.STAGGER_MS else 0L
            val content: @Composable () -> Unit = {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(Spacing.lg)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f),
                            )
                            PriorityChip(priority)
                        }
                        if (description.isNotEmpty()) {
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            if (animate) EnterAnimation(staggerMs = stagger) { content() } else content()
        }
        OutlinedButton(
            onClick = { onPlanSave(planJson) },
            shape = MaterialTheme.shapes.small,
        ) {
            Text(stringResource(R.string.chat_plan_save_tasks))
        }
    }
}

@Composable
private fun PriorityChip(priority: String) {
    val (labelRes, color) = when (priority.uppercase()) {
        "HIGH" -> R.string.task_priority_high to Warning
        "LOW" -> R.string.task_priority_low to TextTertiary
        else -> R.string.task_priority_medium to TextTertiary
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.14f),
    ) {
        Text(
            stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
        )
    }
}

@Composable
private fun ChatEmptyState(
    hasKey: Boolean,
    onOpenSettings: () -> Unit,
    onSuggestion: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EnterAnimation {
            Icon(
                Icons.Filled.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(Spacing.md))
        EnterAnimation(staggerMs = Motion.STAGGER_MS) {
            Text(
                stringResource(R.string.chat_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        if (!hasKey) {
            EnterAnimation(staggerMs = Motion.STAGGER_MS * 2) {
                ErrorBanner(
                    message = stringResource(R.string.chat_no_key_message),
                    action = stringResource(R.string.chat_no_key_action),
                    onAction = onOpenSettings,
                    modifier = Modifier.widthIn(max = 420.dp),
                )
            }
            Spacer(Modifier.height(Spacing.huge))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.widthIn(max = 420.dp),
        ) {
            listOf(
                R.string.chat_suggestion_plan,
                R.string.chat_suggestion_explain,
                R.string.chat_suggestion_draft,
                R.string.chat_suggestion_code,
            ).forEachIndexed { index, res ->
                val label = stringResource(res)
                EnterAnimation(offsetDp = 12, staggerMs = Motion.STAGGER_MS * (2 + index)) {
                    OutlinedButton(
                        onClick = { onSuggestion(label) },
                        shape = InputBarShape,
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = Spacing.xs),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    attachmentPath: String?,
    onAttach: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onMic: () -> Unit,
    isBusy: Boolean,
    onSend: (String, SendMode, Boolean) -> Unit,
    onStop: () -> Unit,
) {
    var mode by remember { mutableStateOf(SendMode.CHAT) }
    var webGrounded by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
        AnimatedVisibility(
            visible = !isBusy,
            enter = expandVertically(tween(Motion.FADE_MS)) + fadeIn(tween(Motion.FADE_MS)),
            exit = shrinkVertically(tween(Motion.FADE_MS / 2)) + fadeOut(tween(Motion.FADE_MS / 2)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                ModeChip(
                    label = stringResource(R.string.chat_plan_mode),
                    selected = mode == SendMode.PLAN,
                    onClick = { mode = if (mode == SendMode.PLAN) SendMode.CHAT else SendMode.PLAN },
                )
                ModeChip(
                    label = stringResource(R.string.chat_image_mode),
                    selected = mode == SendMode.IMAGE,
                    onClick = { mode = if (mode == SendMode.IMAGE) SendMode.CHAT else SendMode.IMAGE },
                )
                ModeChip(
                    label = stringResource(R.string.chat_agent_mode),
                    selected = mode == SendMode.AGENT,
                    onClick = { mode = if (mode == SendMode.AGENT) SendMode.CHAT else SendMode.AGENT },
                )
                Spacer(Modifier.weight(1f))
                ModeChip(
                    label = stringResource(R.string.chat_web_mode),
                    selected = webGrounded,
                    onClick = { webGrounded = !webGrounded },
                )
            }
        }

        AnimatedVisibility(
            visible = attachmentPath != null,
            enter = expandVertically(tween(Motion.FADE_MS)) + fadeIn(tween(Motion.FADE_MS)),
            exit = shrinkVertically(tween(Motion.FADE_MS / 2)) + fadeOut(tween(Motion.FADE_MS / 2)),
        ) {
            if (attachmentPath != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.padding(vertical = Spacing.xs),
                ) {
                    AttachmentThumbnail(attachmentPath)
                    Text(
                        stringResource(R.string.chat_attachment_attached),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onRemoveAttachment, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.chat_attachment_remove),
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            IconButton(
                onClick = onAttach,
                enabled = !isBusy,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.chat_attach_image),
                    tint = TextTertiary,
                )
            }
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        stringResource(
                            when (mode) {
                                SendMode.PLAN -> R.string.chat_plan_hint
                                SendMode.IMAGE -> R.string.chat_image_hint
                                SendMode.AGENT -> R.string.chat_agent_hint
                                SendMode.CHAT -> R.string.chat_input_hint
                            }
                        ),
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier.weight(1f),
                shape = InputBarShape,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
            IconButton(
                onClick = onMic,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = stringResource(R.string.chat_speak_to_text),
                    tint = TextTertiary,
                )
            }
            Spacer(Modifier.size(Spacing.xs))
            SendStopButton(
                isStreaming = isBusy,
                enabled = text.isNotBlank() || attachmentPath != null,
                onSend = { onSend(text.trim(), mode, webGrounded) },
                onStop = onStop,
            )
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        shape = InputBarShape,
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun AttachmentThumbnail(path: String) {
    val context = LocalContext.current
    val bitmap = remember(path) {
        com.rixy.bot.data.prefs.ImageStore(context).decodeBounded(path, 256)
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small),
        )
    } else {
        Icon(
            Icons.Filled.Image,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun SendStopButton(
    isStreaming: Boolean,
    enabled: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = Motion.Liquid,
        label = "send-press",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isStreaming || enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        animationSpec = tween(Motion.FADE_MS),
        label = "send-color",
    )
    Surface(
        shape = InputBarShape,
        color = containerColor,
        modifier = Modifier
            .size(56.dp)
            .scale(scale),
    ) {
        IconButton(
            onClick = { if (isStreaming) onStop() else if (enabled) onSend() },
            enabled = isStreaming || enabled,
            interactionSource = interaction,
        ) {
            Icon(
                if (isStreaming) Icons.Filled.StopCircle else Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(
                    if (isStreaming) R.string.chat_stop else R.string.chat_send
                ),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListeningSheet(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val context = LocalContext.current
    var transcript by remember { mutableStateOf("") }
    val recognizer = remember {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return@remember null
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) { /* sheet stays; user can retry or cancel */ }
                override fun onResults(results: android.os.Bundle?) {
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()?.let { transcript = it }
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()?.let { if (it.isNotBlank()) transcript = it }
                }
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
        }
    }
    DisposableEffect(recognizer) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer?.startListening(intent)
        onDispose {
            recognizer?.stopListening()
            recognizer?.destroy()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xxl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(Spacing.lg)
                        .size(28.dp),
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                if (transcript.isBlank()) stringResource(R.string.chat_listening)
                else transcript,
                style = MaterialTheme.typography.bodyLarge,
                color = if (transcript.isBlank()) TextTertiary
                else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.chat_dismiss))
                }
                Surface(
                    onClick = { onConfirm(transcript) },
                    enabled = transcript.isNotBlank(),
                    shape = InputBarShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        stringResource(R.string.chat_use_transcript),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.md),
                    )
                }
            }
            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
