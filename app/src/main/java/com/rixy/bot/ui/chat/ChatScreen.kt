package com.rixy.bot.ui.chat

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rixy.bot.R
import com.rixy.bot.data.model.ChatMessageEntity
import com.rixy.bot.ui.components.EnterAnimation
import com.rixy.bot.ui.components.ErrorBanner
import com.rixy.bot.ui.components.RixyBubble
import com.rixy.bot.ui.components.UserBubble
import com.rixy.bot.ui.theme.InputBarShape
import com.rixy.bot.ui.theme.Motion
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.Surface
import com.rixy.bot.ui.theme.TextTertiary
import com.rixy.bot.ui.theme.Warning
import com.rixy.bot.ui.viewmodel.ChatUiEvent
import com.rixy.bot.ui.viewmodel.ChatViewModel
import org.json.JSONArray

@Composable
fun ChatScreen(
    chatTitle: String,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ChatViewModel,
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()
    val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
    val isPlanning by viewModel.isPlanning.collectAsStateWithLifecycle()
    val hasKey = viewModel.hasApiKey
    val snackbarHostState = remember { SnackbarHostState() }
    var inputText by remember { mutableStateOf("") }
    val planSavedMessage = stringResource(R.string.chat_plan_saved)

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
                    snackbarHostState.showSnackbar(
                        planSavedMessage,
                        duration = SnackbarDuration.Short,
                    )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        ChatTopBar(title = chatTitle, onOpenDrawer = onOpenDrawer, onNewChat = onNewChat)
        SnackbarHost(snackbarHostState)

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = messages.isEmpty() && streamingText == null && !isPlanning,
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
                        onPlanSave = viewModel::savePlanToTasks,
                    )
                }
            }
        }

        ChatInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            isStreaming = isStreaming || isPlanning,
            onSend = { value, planMode ->
                if (planMode) viewModel.requestPlan(value) else viewModel.sendMessage(value)
                inputText = ""
            },
            onStop = viewModel::stopStreaming,
        )
    }
}

@Composable
private fun ChatTopBar(title: String, onOpenDrawer: () -> Unit, onNewChat: () -> Unit) {
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
    onPlanSave: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    // Only liquid-enter messages that arrive after this screen first renders;
    // history loaded later (or scrolled back to) appears instantly.
    val initialTopId = remember { messages.maxOfOrNull { it.id } ?: Long.MIN_VALUE }
    LaunchedEffect(messages.size, streamingText, isPlanning) {
        if (messages.isNotEmpty() || streamingText != null || isPlanning) {
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
                if (message.isFromUser) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        UserBubble(message.text, animate = isNew)
                    }
                } else if (message.planJson != null) {
                    PlanCard(message.planJson, onPlanSave, animate = isNew)
                } else {
                    RixyBubble(message.text, animate = isNew)
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
    val card: @Composable (Int) -> Unit = { index ->
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        items.indices.forEach { index -> card(index) }
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
    isStreaming: Boolean,
    onSend: (String, Boolean) -> Unit,
    onStop: () -> Unit,
) {
    var planMode by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
        AnimatedVisibility(
            visible = !isStreaming,
            enter = expandVertically(tween(Motion.FADE_MS)) + fadeIn(tween(Motion.FADE_MS)),
            exit = shrinkVertically(tween(Motion.FADE_MS / 2)) + fadeOut(tween(Motion.FADE_MS / 2)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(
                    stringResource(R.string.chat_plan_mode),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
                Switch(
                    checked = planMode,
                    onCheckedChange = { planMode = it },
                    enabled = !isStreaming,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        stringResource(R.string.chat_input_hint),
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
            Spacer(Modifier.size(Spacing.sm))
            SendStopButton(
                isStreaming = isStreaming,
                enabled = text.isNotBlank(),
                onSend = { onSend(text.trim(), planMode) },
                onStop = onStop,
            )
        }
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
