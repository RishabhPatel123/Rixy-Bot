package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotActivityState
import com.example.data.model.BotEntity
import com.example.data.model.BotRole
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.ui.components.AgentLogDrawer
import com.example.ui.components.AgentStatusIndicator
import com.example.ui.components.AgentTimePerformanceDashboard
import com.example.ui.components.BotActivityBadge
import com.example.ui.components.BotAvatar
import com.example.ui.components.StatusBadge
import com.example.ui.components.VMClusterCard
import com.example.ui.components.VisualBrowserWindow
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ImmersiveActiveMint
import com.example.ui.theme.ImmersiveAlertBg
import com.example.ui.theme.ImmersiveAlertBorder
import com.example.ui.theme.ImmersiveAlertCoral
import com.example.ui.theme.ImmersiveAlertOnCoral
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveContainer
import com.example.ui.theme.ImmersiveOnPrimary
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.StatusAmberAnalysis
import com.example.ui.theme.StatusCoralInput
import com.example.ui.theme.StatusCyanResearch
import com.example.ui.theme.StatusMintCompleted
import com.example.ui.theme.StatusMutedIdle
import com.example.ui.theme.StatusPurpleDraft
import com.example.ui.viewmodel.CoworkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CoworkerViewModel,
    onNavigateToApprovals: () -> Unit,
    onNavigateToSwarms: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val bots by viewModel.bots.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()

    var showDispatchDialog by remember { mutableStateOf(false) }
    var selectedBotForDetail by remember { mutableStateOf<BotEntity?>(null) }
    var selectedBotForLogs by remember { mutableStateOf<BotEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDispatchDialog = true },
                containerColor = ImmersivePrimary,
                contentColor = ImmersiveOnPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("dispatch_task_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Assign Task")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Assign Task", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
        ) {
            // Header: Command Center with Active Badge and Profile Icon (Matching Immersive UI HTML)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ImmersivePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "Command Hub",
                                tint = ImmersiveOnPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Command Center",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.3).sp,
                                color = ImmersiveTextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ImmersiveActiveMint)
                                )
                                Text(
                                    text = "${bots.count { it.status == "WORKING" }} AGENTS ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ImmersiveActiveMint,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }

                    // Account Circle Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ImmersiveSurface)
                            .border(1.dp, ImmersiveBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account",
                            tint = ImmersiveTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Persistent Cloud Computers status card
            item {
                VMClusterCard(
                    activeVmCount = 6,
                    runningBotsCount = bots.count { it.status == "WORKING" }
                )
            }

            // Approval Required Banner (Matching Immersive UI HTML banner)
            if (pendingApprovals.isNotEmpty()) {
                item {
                    val firstPending = pendingApprovals.firstOrNull()
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = ImmersiveAlertBg,
                        border = BorderStroke(1.dp, ImmersiveAlertBorder.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToApprovals() }
                            .testTag("pending_approvals_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ImmersiveAlertCoral),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = ImmersiveAlertOnCoral,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Approval Required",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ImmersiveAlertCoral
                                    )
                                    Text(
                                        text = "${pendingApprovals.size} Pending • ${firstPending?.title ?: "Review and authorize actions"}",
                                        fontSize = 12.sp,
                                        color = ImmersiveAlertCoral.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ImmersiveAlertCoral
                            ) {
                                Text(
                                    text = "Review",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveAlertOnCoral,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section: Your AI Workforce (Roles)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Workforce by Role",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${bots.size} Specialists",
                        fontSize = 12.sp,
                        color = CyanPrimary
                    )
                }
            }

            // Horizontal row of AI Coworkers
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(bots) { bot ->
                        BotMiniCard(
                            bot = bot,
                            onClick = { selectedBotForDetail = bot },
                            onViewLogs = { selectedBotForLogs = bot }
                        )
                    }
                }
            }

            // Mini Dashboard using Recharts-styled visual telemetry to display time spent by agents on tasks
            item {
                AgentTimePerformanceDashboard(
                    bots = bots,
                    tasks = tasks
                )
            }

            // Active Tasks with Live Visual Computer Use
            item {
                Text(
                    text = "Live Asynchronous Tasks",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (tasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "No tasks",
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No active tasks in cloud VM",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Assign a task to let your bots work 24/7",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(tasks) { task ->
                    val primaryBot = bots.find { it.id == task.primaryBotId }
                    TaskCommandCard(
                        task = task,
                        bot = primaryBot,
                        onReviewApproval = { onNavigateToApprovals() },
                        onViewAgentLogs = {
                            primaryBot?.let { selectedBotForLogs = it }
                        }
                    )
                }
            }
        }
    }

    // Dialog for Assigning Tasks via Text
    if (showDispatchDialog) {
        DispatchTaskDialog(
            bots = bots,
            onDismiss = { showDispatchDialog = false },
            onDispatch = { title, desc, botId, approvalRequired ->
                viewModel.dispatchTask(title, desc, botId, null, approvalRequired)
                showDispatchDialog = false
            }
        )
    }

    // Bot Details Sheet/Dialog
    selectedBotForDetail?.let { bot ->
        BotDetailDialog(
            bot = bot,
            onDismiss = { selectedBotForDetail = null },
            onViewLogs = {
                selectedBotForLogs = bot
                selectedBotForDetail = null
            }
        )
    }

    // Dedicated Agent Log Drawer / Screen
    selectedBotForLogs?.let { bot ->
        AgentLogDrawer(
            bot = bot,
            tasks = tasks.filter { it.primaryBotId == bot.id },
            onDismiss = { selectedBotForLogs = null }
        )
    }
}

@Composable
fun BotMiniCard(
    bot: BotEntity,
    onClick: () -> Unit,
    onViewLogs: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(170.dp)
            .clickable { onClick() }
            .testTag("bot_card_${bot.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(
            1.dp,
            if (bot.activityState == BotActivityState.WAITING_FOR_INPUT) ImmersiveAlertCoral.copy(alpha = 0.6f)
            else ImmersiveBorder.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotAvatar(
                    botName = bot.name,
                    role = bot.role,
                    size = 36,
                    colorHex = bot.colorHex,
                    isWorking = bot.status == "WORKING",
                    activityState = bot.activityState
                )
                BotActivityBadge(activityState = bot.activityState)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = bot.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextPrimary
            )

            Text(
                text = bot.role.title,
                fontSize = 11.sp,
                color = ImmersivePrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Real-time live action description text
            Text(
                text = bot.currentActionText,
                fontSize = 10.sp,
                color = ImmersiveTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bot.completedTasksCount} jobs completed",
                    fontSize = 10.sp,
                    color = ImmersiveTextMuted
                )

                if (onViewLogs != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ImmersiveBorder.copy(alpha = 0.4f))
                            .clickable { onViewLogs() }
                            .padding(4.dp)
                            .testTag("bot_mini_log_btn_${bot.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "View Logs",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCommandCard(
    task: TaskEntity,
    bot: BotEntity?,
    onReviewApproval: () -> Unit,
    onViewAgentLogs: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(
            1.dp,
            if (task.status == TaskStatus.AWAITING_APPROVAL) ImmersiveAlertCoral.copy(alpha = 0.7f)
            else ImmersiveBorder.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Task Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (bot != null) {
                        BotAvatar(
                            botName = bot.name,
                            role = bot.role,
                            size = 34,
                            colorHex = bot.colorHex,
                            showStatusDot = true,
                            isWorking = bot.status == "WORKING",
                            activityState = bot.activityState
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = bot?.name ?: "Autonomous Bot",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersivePrimary
                        )
                        Text(
                            text = bot?.role?.title ?: "Coworker",
                            fontSize = 10.sp,
                            color = ImmersiveTextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (bot != null) {
                        BotActivityBadge(activityState = bot.activityState)
                    }
                    StatusBadge(status = task.status.name)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = ImmersiveTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cloud Execution Progress",
                    fontSize = 11.sp,
                    color = ImmersiveTextMuted
                )
                Text(
                    text = "${task.progressPercent}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersivePrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { task.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (task.status == TaskStatus.AWAITING_APPROVAL) ImmersiveAlertCoral else ImmersivePrimary,
                trackColor = Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Live Computer-Use Visual Screen Window
            VisualBrowserWindow(
                url = task.browserUrl,
                stepDescription = task.currentStepText
            )

            // Actions & Log inspection row
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onViewAgentLogs != null) {
                    TextButton(
                        onClick = onViewAgentLogs,
                        modifier = Modifier.testTag("task_inspect_logs_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Agent Logs",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Agent Logs & Trace",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ImmersivePrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = "Node: ${task.vmNode}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ImmersiveTextMuted
                )
            }

            // If task is parked waiting for human sign-off
            if (task.status == TaskStatus.AWAITING_APPROVAL) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onReviewApproval,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_approval_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersiveAlertCoral,
                        contentColor = ImmersiveAlertOnCoral
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Review",
                        tint = ImmersiveAlertOnCoral,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Review & Sign-off on Phone",
                        color = ImmersiveAlertOnCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DispatchTaskDialog(
    bots: List<BotEntity>,
    onDismiss: () -> Unit,
    onDispatch: (title: String, desc: String, botId: String, requiresApproval: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedBotId by remember { mutableStateOf(bots.firstOrNull()?.id ?: "bot_atlas") }
    var requiresApproval by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Assign Task to Cloud Coworker",
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Your task runs 24/7 in persistent cloud computers with real browser and terminal access.",
                    fontSize = 12.sp,
                    color = ImmersiveTextSecondary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Objective") },
                    placeholder = { Text("e.g. Settle August AWS billing statement") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions & Parameters") },
                    placeholder = { Text("e.g. Download invoice PDF, verify total under $200, code to infra expense") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input"),
                    maxLines = 3
                )

                Text(
                    text = "Assign to Specialist Bot:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ImmersiveTextPrimary
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bots) { b ->
                        val isSelected = b.id == selectedBotId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ImmersivePrimary.copy(alpha = 0.2f) else Color(0xFF1D1B20),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ImmersivePrimary else ImmersiveBorder
                            ),
                            modifier = Modifier.clickable { selectedBotId = b.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BotAvatar(
                                    botName = b.name,
                                    role = b.role,
                                    size = 22,
                                    colorHex = b.colorHex,
                                    showStatusDot = false
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = b.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ImmersivePrimary else ImmersiveTextPrimary
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = requiresApproval,
                        onCheckedChange = { requiresApproval = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Park & ping phone for sign-off before critical actions (spending money, emails, 2FA)",
                        fontSize = 11.sp,
                        color = ImmersiveTextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onDispatch(title, description, selectedBotId, requiresApproval)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImmersivePrimary,
                    contentColor = ImmersiveOnPrimary
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("submit_dispatch_button")
            ) {
                Text("Dispatch to Cloud", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ImmersiveTextMuted)
            }
        }
    )
}

@Composable
fun BotDetailDialog(
    bot: BotEntity,
    onDismiss: () -> Unit,
    onViewLogs: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotAvatar(
                        botName = bot.name,
                        role = bot.role,
                        size = 42,
                        colorHex = bot.colorHex,
                        activityState = bot.activityState,
                        isWorking = bot.status == "WORKING"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = bot.name, fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                        Text(
                            text = bot.role.title,
                            fontSize = 12.sp,
                            color = ImmersivePrimary
                        )
                    }
                }
                BotActivityBadge(activityState = bot.activityState)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = bot.role.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ImmersiveTextSecondary
                )

                // Real-time Action Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1D1B20),
                    border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE ACTIVITY STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                            AgentStatusIndicator(
                                status = bot.status,
                                showLabel = true,
                                isPulsing = true
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bot.currentActionText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ImmersiveTextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1D1B20),
                    border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PERSISTENT CLOUD ENVIRONMENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersivePrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Host Node: ${bot.currentVmHost}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "RAM Usage: ${bot.memoryUsage} | CPU: ${bot.cpuUsage}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "Visual Chrome Session: ${if (bot.browserSessionActive) "Active & Streaming" else "Standby"}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "Lifetime Completed Tasks: ${bot.completedTasksCount}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersiveTextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ImmersivePrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onViewLogs,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, ImmersivePrimary.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersivePrimary),
                modifier = Modifier.testTag("view_agent_logs_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Logs",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Agent Logs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
