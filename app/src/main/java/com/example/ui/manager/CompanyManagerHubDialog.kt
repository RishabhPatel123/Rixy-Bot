package com.example.ui.manager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BotActivityState
import com.example.data.model.BotEntity
import com.example.data.model.BotRole
import com.example.domain.manager.ComplexInitiativePlan
import com.example.domain.manager.ManagerInitiativePhase
import com.example.domain.manager.ManagerOrchestrationEngine
import com.example.ui.components.BotAvatar

private val DirectorViolet = Color(0xFF9333EA)
private val DirectorDeepPurple = Color(0xFF1E1035)
private val DirectorCardBg = Color(0xFF160E28)
private val EmeraldSuccess = Color(0xFF10B981)
private val AmberWarning = Color(0xFFF59E0B)
private val CoralAlert = Color(0xFFFF5252)
private val CyanAccent = Color(0xFF00E5FF)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompanyManagerHubDialog(
    bots: List<BotEntity>,
    onDismiss: () -> Unit,
    onDispatchInitiative: (ComplexInitiativePlan) -> Unit,
    onDirectTaskDispatch: (String, String, String) -> Unit,
    onRebalanceWorkforce: () -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val orion = bots.find { it.isManager || it.id == "bot_orion" } ?: bots.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .testTag("company_manager_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DirectorDeepPurple),
            border = BorderStroke(1.5.dp, DirectorViolet.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Executive Director Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    DirectorViolet.copy(alpha = 0.35f),
                                    DirectorDeepPurple,
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(DirectorViolet.copy(alpha = 0.25f))
                                    .border(1.5.dp, DirectorViolet, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👑", fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = orion?.name ?: "Orion",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = DirectorViolet
                                    ) {
                                        Text(
                                            text = "COMPANY MANAGER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Chief Orchestrator • Controls all specialist bots & complex initiatives",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_manager_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Manager",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 2. Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF140D24),
                    contentColor = DirectorViolet,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = DirectorViolet,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Complex Task Decomposer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = TextMuted
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Which Bot to Use?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = TextMuted
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Org Chart & Directives", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = TextMuted
                    )
                }

                // 3. Tab Contents
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    when (selectedTab) {
                        0 -> ComplexTaskDecomposerTab(
                            bots = bots,
                            onDispatchInitiative = { plan ->
                                onDispatchInitiative(plan)
                                onDismiss()
                            }
                        )
                        1 -> WhichBotToUseTab(
                            bots = bots,
                            onDirectTaskDispatch = { title, desc, botId ->
                                onDirectTaskDispatch(title, desc, botId)
                                onDismiss()
                            }
                        )
                        2 -> OrgChartAndDirectivesTab(
                            bots = bots,
                            orion = orion,
                            onRebalanceWorkforce = onRebalanceWorkforce
                        )
                    }
                }
            }
        }
    }
}

/**
 * TAB 0: How a long or complex task will be handled (Decomposer & Roadmap)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComplexTaskDecomposerTab(
    bots: List<BotEntity>,
    onDispatchInitiative: (ComplexInitiativePlan) -> Unit
) {
    var customGoalInput by remember { mutableStateOf("") }
    var activeGoal by remember {
        mutableStateOf(ManagerOrchestrationEngine.enterprisePresets.first().goalPrompt)
    }

    val currentPlan = remember(activeGoal, bots) {
        ManagerOrchestrationEngine.decomposeComplexTask(activeGoal, bots)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Presets Title
        item {
            Text(
                text = "Enterprise Initiative Blueprints (Select or Type Custom Task)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
        }

        // Preset Chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ManagerOrchestrationEngine.enterprisePresets.forEach { preset ->
                    val isSelected = activeGoal == preset.goalPrompt
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) DirectorViolet else DirectorCardBg,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) DirectorViolet else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.clickable {
                            activeGoal = preset.goalPrompt
                            customGoalInput = preset.goalPrompt
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Custom Goal Text Input
        item {
            OutlinedTextField(
                value = customGoalInput,
                onValueChange = {
                    customGoalInput = it
                    if (it.isNotBlank()) activeGoal = it
                },
                placeholder = {
                    Text(
                        "Enter any complex company initiative or project...",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complex_goal_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DirectorViolet,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = DirectorCardBg,
                    unfocusedContainerColor = DirectorCardBg,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    if (customGoalInput.isNotBlank()) {
                        IconButton(onClick = {
                            activeGoal = customGoalInput
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Decompose", tint = DirectorViolet)
                        }
                    }
                },
                maxLines = 3
            )
        }

        // Manager's Executive Strategy Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DirectorCardBg),
                border = BorderStroke(1.dp, DirectorViolet.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = DirectorViolet, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Manager Orion's Strategic Architecture",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Est. ${currentPlan.estimatedDurationMinutes}m Runtime",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentPlan.executiveSummary,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contingency & Safeguard banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentPlan.contingencyPolicy,
                            fontSize = 10.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Section Title: Phased Execution Roadmap
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phased Execution Roadmap (${currentPlan.phases.size} Milestones)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Dependencies & Gates Linked",
                    fontSize = 11.sp,
                    color = CyanAccent
                )
            }
        }

        // Each Phase Item
        items(currentPlan.phases) { phase ->
            PhaseMilestoneCard(phase = phase, bots = bots)
        }

        // Primary Action: Authorize & Dispatch Initiative
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { onDispatchInitiative(currentPlan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("dispatch_initiative_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DirectorViolet)
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Authorize & Dispatch Phased Initiative",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PhaseMilestoneCard(
    phase: ManagerInitiativePhase,
    bots: List<BotEntity>
) {
    val assignedBot = bots.find { it.id == phase.assignedBotId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF130B22)),
        border = BorderStroke(
            1.dp,
            if (phase.requiresHumanApprovalGate) CoralAlert.copy(alpha = 0.7f)
            else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Phase Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = DirectorViolet.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, DirectorViolet.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Stage ${phase.phaseNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8B4FE),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = phase.phaseName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (phase.requiresHumanApprovalGate) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CoralAlert.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, CoralAlert)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = CoralAlert, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "HUMAN GATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralAlert
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Specialist Bot Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B1130))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotAvatar(
                        botName = assignedBot?.name ?: phase.assignedBotName,
                        role = assignedBot?.role ?: BotRole.SALES_OUTBOUND,
                        size = 32,
                        colorHex = assignedBot?.colorHex ?: 0xFF00E5FF,
                        showStatusDot = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = assignedBot?.name ?: phase.assignedBotName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = phase.assignedBotRoleTitle,
                            fontSize = 10.sp,
                            color = CyanAccent
                        )
                    }
                }

                // Output Artifact Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Text(
                        text = "📦 ${phase.expectedArtifact}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task title and description
            Text(
                text = phase.taskTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = phase.taskDescription,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )

            // Dependency Tag
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Prerequisite: ",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = phase.dependencies,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA78BFA)
                )
            }
        }
    }
}

/**
 * TAB 1: Which bot should use for task (Advisor & Matchmaker)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhichBotToUseTab(
    bots: List<BotEntity>,
    onDirectTaskDispatch: (String, String, String) -> Unit
) {
    var taskQueryInput by remember { mutableStateOf("") }
    val sampleQueries = listOf(
        "Download monthly Stripe invoices and reconcile with QuickBooks",
        "Source Senior Full-Stack Kotlin engineers on LinkedIn",
        "Reproduce DOM button click failure on checkout page",
        "Scrape competitor SaaS pricing tables into CSV",
        "Inspect container pod logs and run git integration tests",
        "Draft executive sales outreach for enterprise CIOs"
    )

    val currentRecommendation = remember(taskQueryInput, bots) {
        ManagerOrchestrationEngine.recommendBotForTask(
            taskQueryInput.ifBlank { sampleQueries.first() },
            bots
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = "Ask Manager Orion: Which Bot Should Handle This Task?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Manager Orion analyzes task requirements against each specialist's VM capabilities & workloads.",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        // Query input
        item {
            OutlinedTextField(
                value = taskQueryInput,
                onValueChange = { taskQueryInput = it },
                placeholder = {
                    Text(
                        "Describe any task (e.g. download receipts, scrape pricing, fix bug)...",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bot_matcher_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DirectorViolet,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = DirectorCardBg,
                    unfocusedContainerColor = DirectorCardBg,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 2
            )
        }

        // Quick suggestions
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sampleQueries.take(4).forEach { sample ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DirectorCardBg,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.clickable { taskQueryInput = sample }
                    ) {
                        Text(
                            text = sample,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Manager Recommendation Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DirectorCardBg),
                border = BorderStroke(1.5.dp, DirectorViolet)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Top recommendation tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = DirectorViolet
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ORION'S RECOMMENDED SPECIALIST",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = "${currentRecommendation.matchScorePercent}% Match",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bot Details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BotAvatar(
                            botName = currentRecommendation.recommendedBot.name,
                            role = currentRecommendation.recommendedBot.role,
                            size = 50,
                            colorHex = currentRecommendation.recommendedBot.colorHex,
                            showStatusDot = true,
                            isWorking = currentRecommendation.recommendedBot.status == "WORKING",
                            activityState = currentRecommendation.recommendedBot.activityState
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = currentRecommendation.recommendedBot.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentRecommendation.recommendedBot.role.title,
                                fontSize = 12.sp,
                                color = CyanAccent
                            )
                            Text(
                                text = "Current Load: CPU ${currentRecommendation.recommendedBot.cpuUsage} • ${currentRecommendation.recommendedBot.memoryUsage}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Manager Rationale
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1C1233),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = DirectorViolet, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Manager Orion's Justification:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentRecommendation.managerialRationale,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Capabilities Chips
                    Text(
                        text = "Verified Bot Competencies:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentRecommendation.capabilityHighlights.forEach { cap ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = "✓ $cap",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Fallback Bot Tag
                    if (currentRecommendation.fallbackBot != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Backup Specialist: ${currentRecommendation.fallbackBot.name} (${currentRecommendation.fallbackBot.role.title})",
                            fontSize = 10.sp,
                            color = AmberWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Direct Assign Button
                    Button(
                        onClick = {
                            val taskTitle = taskQueryInput.ifBlank { "Assigned Task" }
                            onDirectTaskDispatch(
                                taskTitle,
                                "Direct dispatch assigned by Manager Orion: $taskTitle",
                                currentRecommendation.recommendedBot.id
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("direct_assign_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DirectorViolet)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Assign Task Directly to ${currentRecommendation.recommendedBot.name}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * TAB 2: Company Org Chart & Manager Directives
 */
@Composable
private fun OrgChartAndDirectivesTab(
    bots: List<BotEntity>,
    orion: BotEntity?,
    onRebalanceWorkforce: () -> Unit
) {
    var rebalanceConfirmed by remember { mutableStateOf(false) }
    val specialists = bots.filter { !it.isManager }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Company Workforce Hierarchy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Manager Orion supervises 6 persistent specialists across cloud VMs",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = {
                        onRebalanceWorkforce()
                        rebalanceConfirmed = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DirectorViolet.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, DirectorViolet),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = DirectorViolet, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (rebalanceConfirmed) "Balanced ✓" else "Rebalance Load",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Apex Card: Orion (Manager)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DirectorCardBg),
                border = BorderStroke(1.5.dp, DirectorViolet)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotAvatar(
                        botName = orion?.name ?: "Orion",
                        role = BotRole.COMPANY_MANAGER,
                        size = 46,
                        colorHex = 0xFF9333EA,
                        showStatusDot = true,
                        isWorking = true,
                        activityState = BotActivityState.ANALYZING
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = orion?.name ?: "Orion",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = DirectorViolet
                            ) {
                                Text(
                                    text = "APEX DIRECTOR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Orchestrating 6 specialist bots • Supervised Autonomy Mode",
                            fontSize = 11.sp,
                            color = CyanAccent
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = orion?.currentActionText ?: "Active company supervision",
                            fontSize = 10.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Tree Branch Indicator
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(DirectorViolet.copy(alpha = 0.7f))
                    )
                    Text(
                        text = "DIRECT REPORTS (SPECIALIST BOTS)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA)
                    )
                }
            }
        }

        // Specialist Cards
        items(specialists) { bot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF120B20)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BotAvatar(
                            botName = bot.name,
                            role = bot.role,
                            size = 36,
                            colorHex = bot.colorHex,
                            showStatusDot = true,
                            isWorking = bot.status == "WORKING",
                            activityState = bot.activityState
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = bot.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = bot.role.title,
                                fontSize = 10.sp,
                                color = CyanAccent
                            )
                            Text(
                                text = bot.currentActionText,
                                fontSize = 9.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // CPU / Memory Stats
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = "CPU ${bot.cpuUsage}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bot.memoryUsage,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Supervisory Directives Policy
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160E28)),
                border = BorderStroke(1.dp, DirectorViolet.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = DirectorViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manager Governance & Checkpoint Rules",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val policies = listOf(
                        "🔒 Financial Gate: Any outbound transaction or journal adjustment > $500 requires human officer sign-off.",
                        "📧 Outreach Gate: Bulk outbound campaigns to prospective clients are queued for human review.",
                        "💻 Terminal Gate: Code commits and cloud container deployments require engineering sign-off.",
                        "⚡ Auto-Recovery: If a specialist bot stalls or hits rate limits, Manager Orion re-routes to secondary fallback."
                    )

                    policies.forEach { policy ->
                        Text(
                            text = policy,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
