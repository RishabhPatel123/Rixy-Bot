package com.example.ui.swarms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotActivityState
import com.example.data.model.BotEntity
import com.example.data.model.BotRole
import com.example.data.model.SwarmEntity
import com.example.data.model.SwarmMessageEntity
import com.example.ui.components.BotActivityBadge
import com.example.ui.components.BotAvatar
import com.example.ui.components.FileAttachmentChip
import com.example.ui.components.TerminalSnippetView
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ImmersiveActiveMint
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveContainer
import com.example.ui.theme.ImmersiveOnPrimary
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.viewmodel.CoworkerViewModel

@Composable
fun SwarmWorkspaceScreen(
    viewModel: CoworkerViewModel,
    modifier: Modifier = Modifier
) {
    val swarms by viewModel.swarms.collectAsState()
    val bots by viewModel.bots.collectAsState()
    val selectedSwarmId by viewModel.selectedSwarmId.collectAsState()
    val messages by viewModel.currentSwarmMessages.collectAsState()

    val currentSwarm = swarms.find { it.id == selectedSwarmId } ?: swarms.firstOrNull()

    var showCreateSwarmDialog by remember { mutableStateOf(false) }
    var userMessageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ImmersiveSurface)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ImmersivePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Swarms",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MULTI-BOT SWARMS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersivePrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Shared Cloud Workspaces",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                    }
                }

                Button(
                    onClick = { showCreateSwarmDialog = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersiveContainer,
                        contentColor = ImmersivePrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("create_swarm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Swarm",
                        tint = ImmersivePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Swarm",
                        color = ImmersivePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Swarms Tab Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(swarms) { s ->
                    val isSelected = s.id == currentSwarm?.id
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) ImmersivePrimary else ImmersiveSurfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ImmersivePrimary else ImmersiveBorder.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable { viewModel.selectSwarm(s.id) }
                            .testTag("swarm_tab_${s.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = s.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ImmersiveOnPrimary else ImmersiveTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Shared Workspace Telemetry Bar
        if (currentSwarm != null) {
            val assignedBotIds = currentSwarm.botIds.split(",").map { it.trim() }
            val swarmBots = bots.filter { assignedBotIds.contains(it.id) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                color = ImmersiveContainer,
                border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = "Shared Storage",
                                tint = ImmersiveActiveMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHARED VM: ${currentSwarm.sharedVmId}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ImmersiveActiveMint,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bot icons participating in swarm with live status
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            swarmBots.forEach { b ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF141218))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    BotAvatar(
                                        botName = b.name,
                                        role = b.role,
                                        size = 20,
                                        colorHex = b.colorHex,
                                        showStatusDot = true,
                                        isWorking = b.status == "WORKING",
                                        activityState = b.activityState
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = b.activityState.label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ImmersiveTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Goal: ${currentSwarm.activeGoal}",
                        fontSize = 11.sp,
                        color = ImmersiveTextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // Chat Stream: Collaborative Swarm History
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                val senderBot = bots.find { it.id == msg.senderId }
                SwarmMessageBubble(message = msg, senderBot = senderBot)
            }
        }

        // Input field for Commander instructions
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ImmersiveSurface,
            border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userMessageText,
                    onValueChange = { userMessageText = it },
                    placeholder = { Text("Issue command to swarm...", color = ImmersiveTextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("swarm_message_input"),
                    shape = RoundedCornerShape(50),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userMessageText.isNotBlank() && currentSwarm != null) {
                            viewModel.sendSwarmMessage(currentSwarm.id, userMessageText)
                            userMessageText = ""
                        }
                    },
                    enabled = userMessageText.isNotBlank() && currentSwarm != null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (userMessageText.isNotBlank()) ImmersivePrimary else ImmersiveContainer)
                        .testTag("send_swarm_message_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (userMessageText.isNotBlank()) ImmersiveOnPrimary else ImmersiveTextMuted
                    )
                }
            }
        }
    }

    // Dialog for Creating a new Multi-Bot Swarm (2 to 6 bots)
    if (showCreateSwarmDialog) {
        CreateSwarmDialog(
            allBots = bots,
            onDismiss = { showCreateSwarmDialog = false },
            onCreate = { name, desc, selectedBots, goal ->
                viewModel.createSwarm(name, desc, selectedBots, goal)
                showCreateSwarmDialog = false
            }
        )
    }
}

@Composable
fun SwarmMessageBubble(
    message: SwarmMessageEntity,
    senderBot: BotEntity? = null,
    modifier: Modifier = Modifier
) {
    val isUser = message.senderId == "USER"
    val isSystem = message.senderId == "SYSTEM"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            ) {
                if (senderBot != null) {
                    BotAvatar(
                        botName = senderBot.name,
                        role = senderBot.role,
                        size = 18,
                        colorHex = senderBot.colorHex,
                        showStatusDot = false,
                        activityState = senderBot.activityState
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = message.senderName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystem) ImmersiveActiveMint else ImmersivePrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${message.senderRole}",
                    fontSize = 10.sp,
                    color = ImmersiveTextMuted
                )
                if (senderBot != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    BotActivityBadge(activityState = senderBot.activityState)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isUser -> ImmersivePrimary
                    isSystem -> ImmersiveContainer
                    else -> ImmersiveSurface
                }
            ),
            border = BorderStroke(
                1.dp,
                when {
                    isUser -> ImmersivePrimary
                    isSystem -> ImmersiveBorder.copy(alpha = 0.6f)
                    else -> ImmersiveBorder.copy(alpha = 0.5f)
                }
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.95f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.messageText,
                    fontSize = 13.sp,
                    color = if (isUser) ImmersiveOnPrimary else ImmersiveTextPrimary,
                    lineHeight = 18.sp
                )

                // Shared File Attached
                if (message.sharedFileName != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FileAttachmentChip(
                        fileName = message.sharedFileName,
                        fileType = message.sharedFileType
                    )
                }

                // Browser Action Snapshot
                if (message.browserActionSnapshot != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF141218),
                        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Browser Action",
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.browserActionSnapshot,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ImmersivePrimary
                            )
                        }
                    }
                }

                // Terminal Command Output
                if (message.terminalCommandOutput != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TerminalSnippetView(commandOutput = message.terminalCommandOutput)
                }
            }
        }
    }
}

@Composable
fun CreateSwarmDialog(
    allBots: List<BotEntity>,
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, selectedBots: List<String>, goal: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    val selectedBotIds = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Assemble Multi-Bot Swarm",
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select 2 to 6 specialized bots to share a persistent cloud computer and pass files/browser sessions without manual copy-paste.",
                    fontSize = 12.sp,
                    color = ImmersiveTextSecondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Swarm Name") },
                    placeholder = { Text("e.g. Talent & Outreach Swarm") },
                    modifier = Modifier.fillMaxWidth().testTag("swarm_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Active Swarm Goal") },
                    placeholder = { Text("e.g. Source 30 leads & verify ATS") },
                    modifier = Modifier.fillMaxWidth().testTag("swarm_goal_input")
                )

                Text(
                    text = "Select Bots (${selectedBotIds.size} selected - Choose 2 to 6):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )

                allBots.forEach { bot ->
                    val isChecked = selectedBotIds.contains(bot.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) {
                                    selectedBotIds.remove(bot.id)
                                } else if (selectedBotIds.size < 6) {
                                    selectedBotIds.add(bot.id)
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked && selectedBotIds.size < 6) {
                                    selectedBotIds.add(bot.id)
                                } else {
                                    selectedBotIds.remove(bot.id)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BotAvatar(
                            botName = bot.name,
                            role = bot.role,
                            size = 26,
                            colorHex = bot.colorHex,
                            showStatusDot = false
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = bot.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = ImmersiveTextPrimary
                            )
                            Text(
                                text = bot.role.title,
                                fontSize = 10.sp,
                                color = ImmersivePrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val canCreate = name.isNotBlank() && selectedBotIds.size in 2..6
            Button(
                onClick = {
                    onCreate(
                        name,
                        "Custom multi-bot swarm executing in shared VM.",
                        selectedBotIds.toList(),
                        goal
                    )
                },
                enabled = canCreate,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImmersivePrimary,
                    contentColor = ImmersiveOnPrimary
                ),
                modifier = Modifier.testTag("submit_create_swarm_button")
            ) {
                Text("Deploy Swarm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ImmersiveTextMuted)
            }
        }
    )
}
