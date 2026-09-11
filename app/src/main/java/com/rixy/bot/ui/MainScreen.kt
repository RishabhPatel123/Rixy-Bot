package com.rixy.bot.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rixy.bot.R
import com.rixy.bot.data.prefs.SecretsStore
import com.rixy.bot.ui.chat.ChatScreen
import com.rixy.bot.ui.components.PrimaryButton
import com.rixy.bot.ui.onboarding.OnboardingScreen
import com.rixy.bot.ui.settings.SettingsScreen
import com.rixy.bot.ui.tasks.TasksScreen
import com.rixy.bot.ui.theme.Motion
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.TextTertiary
import com.rixy.bot.ui.viewmodel.ChatViewModel
import com.rixy.bot.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

object Routes {
    const val ONBOARDING = "onboarding"
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val TASKS = "tasks"
}

@Composable
fun RixyApp(secrets: SecretsStore) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val chatViewModel: ChatViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val chats by chatViewModel.chats.collectAsStateWithLifecycle()
    val selectedChatId by chatViewModel.selectedChatId.collectAsStateWithLifecycle()

    val startDestination = if (secrets.onboardingDone) Routes.CHAT else Routes.ONBOARDING

    var renameTarget by remember { mutableStateOf<Long?>(null) }
    var deleteTarget by remember { mutableStateOf<Long?>(null) }
    var showImport by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    val importFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            runCatching {
                importText = context.contentResolver.openInputStream(it)?.use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                }.orEmpty()
            }
        }
    }

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == Routes.CHAT,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
            ) {
                Column(Modifier.padding(horizontal = Spacing.lg)) {
                    Spacer(Modifier.height(Spacing.xxl))
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    PrimaryButton(
                        text = stringResource(R.string.chat_new),
                        onClick = {
                            chatViewModel.newChat()
                            closeDrawer()
                            navController.navigate(Routes.CHAT) {
                                popUpTo(Routes.CHAT)
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    Text(
                        stringResource(R.string.drawer_chats_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                    Spacer(Modifier.height(Spacing.sm))

                    if (chats.isEmpty()) {
                        Text(
                            stringResource(R.string.drawer_no_chats),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                            modifier = Modifier.height(340.dp),
                        ) {
                            items(chats, key = { it.id }) { chat ->
                                DrawerChatRow(
                                    title = chat.title,
                                    selected = chat.id == selectedChatId && currentRoute == Routes.CHAT,
                                    onClick = {
                                        chatViewModel.selectChat(chat.id)
                                        closeDrawer()
                                        navController.navigate(Routes.CHAT) {
                                            popUpTo(Routes.CHAT)
                                            launchSingleTop = true
                                        }
                                    },
                                    onRename = { renameTarget = chat.id },
                                    onDelete = { deleteTarget = chat.id },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))
                    DrawerLink(
                        icon = {
                            Icon(
                                Icons.Filled.UploadFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        label = stringResource(R.string.import_conversation),
                        onClick = {
                            closeDrawer()
                            showImport = true
                        },
                    )
                    DrawerLink(
                        icon = { Icon(Icons.Filled.Checklist, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        label = stringResource(R.string.drawer_tasks),
                        onClick = {
                            closeDrawer()
                            navController.navigate(Routes.TASKS)
                        },
                    )
                    DrawerLink(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        label = stringResource(R.string.drawer_settings),
                        onClick = {
                            closeDrawer()
                            navController.navigate(Routes.SETTINGS)
                        },
                    )
                    Spacer(Modifier.height(Spacing.xxl))
                }
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                fadeIn(tween(Motion.FADE_MS)) +
                    scaleIn(initialScale = 0.98f, animationSpec = tween(Motion.FADE_MS))
            },
            exitTransition = { fadeOut(tween(Motion.FADE_MS / 2)) },
            popEnterTransition = {
                fadeIn(tween(Motion.FADE_MS)) +
                    slideInVertically(
                        initialOffsetY = { it / 24 },
                        animationSpec = tween(Motion.FADE_MS),
                    )
            },
            popExitTransition = { fadeOut(tween(Motion.FADE_MS / 2)) },
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.CHAT) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    viewModel = settingsViewModel,
                )
            }
            composable(Routes.CHAT) {
                val title = chats.find { it.id == selectedChatId }?.title
                    ?: stringResource(R.string.app_name)
                ChatScreen(
                    chatTitle = title,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNewChat = { chatViewModel.newChat() },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    viewModel = chatViewModel,
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = settingsViewModel,
                )
            }
            composable(Routes.TASKS) {
                TasksScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = koinViewModel(),
                )
            }
        }
    }

    if (showImport) {
        val parsed = remember(importText) {
            com.rixy.bot.util.ConversationImportParser.parse(importText)
        }
        AlertDialog(
            onDismissRequest = { showImport = false },
            title = { Text(stringResource(R.string.import_conversation)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.import_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    androidx.compose.material3.OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        minLines = 6,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (parsed.isNotEmpty()) {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            stringResource(
                                R.string.import_preview,
                                parsed.count { it.isFromUser },
                                parsed.count { !it.isFromUser },
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    TextButton(onClick = {
                        importFilePicker.launch(arrayOf("text/plain", "text/markdown", "application/octet-stream"))
                    }) {
                        Text(stringResource(R.string.import_pick_file))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = parsed.isNotEmpty(),
                    onClick = {
                        chatViewModel.importConversation(parsed)
                        showImport = false
                        importText = ""
                        navController.navigate(Routes.CHAT) {
                            popUpTo(Routes.CHAT)
                            launchSingleTop = true
                        }
                    },
                ) { Text(stringResource(R.string.import_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showImport = false; importText = "" }) {
                    Text(stringResource(R.string.chat_dismiss))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    renameTarget?.let { chatId ->
        val chat = chats.find { it.id == chatId }
        var titleInput by remember(chatId) { mutableStateOf(chat?.title.orEmpty()) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.chat_rename)) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    chatViewModel.renameChat(chatId, titleInput)
                    renameTarget = null
                }) { Text(stringResource(R.string.chat_rename)) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text(stringResource(R.string.chat_dismiss))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    deleteTarget?.let { chatId ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.chat_delete_title)) },
            text = { Text(stringResource(R.string.chat_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    chatViewModel.deleteChat(chatId)
                    deleteTarget = null
                }) { Text(stringResource(R.string.chat_delete), color = com.rixy.bot.ui.theme.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.chat_dismiss))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun DrawerChatRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs, horizontal = Spacing.xs),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            IconButton(onClick = onRename, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.chat_rename), tint = TextTertiary, modifier = Modifier.size(15.dp))
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.chat_delete), tint = TextTertiary, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun DrawerLink(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
    ) {
        icon()
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
