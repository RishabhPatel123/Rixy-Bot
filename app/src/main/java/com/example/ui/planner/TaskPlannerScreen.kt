package com.example.ui.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotEntity
import com.example.ui.components.BotAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SubTaskPlan(
    val title: String,
    val description: String,
    val botId: String,
    var priority: String = "Normal",
    var status: String = "Pending"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPlannerDialog(geminiApiService: com.example.network.GeminiApiService,
    bots: List<BotEntity>,
    onDismiss: () -> Unit,
    onDispatchPlan: (goal: String, subTasks: List<SubTaskPlan>) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Orchestrate, 1: Direct Handoff

    // Tab 0 State
    var goalText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedPlan by remember { mutableStateOf<List<SubTaskPlan>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Tab 1 State
    var selectedTargetBot by remember { mutableStateOf<BotEntity?>(bots.firstOrNull()) }
    var expandedBotDropdown by remember { mutableStateOf(false) }
    val availableArtifacts = remember { listOf("competitor_pricing.csv", "auth_session_token.json", "user_bug_report.md", "app_logs.txt") }
    val selectedArtifacts = remember { mutableStateListOf<String>() }
    var isDispatchingRpc by remember { mutableStateOf(false) }
    var rpcSuccess by remember { mutableStateOf(false) }
    var handoffNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        containerColor = ImmersiveSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task Planner",
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ImmersiveTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 550.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = ImmersivePrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ImmersivePrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Auto Orchestrate") },
                        unselectedContentColor = ImmersiveTextMuted
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Direct Handoff") },
                        unselectedContentColor = ImmersiveTextMuted
                    )
                }

                if (selectedTab == 0) {
                    // TAB 0: ORCHESTRATE
                    Text(
                        text = "Define a high-level goal. The orchestrator will break it down into specialized sub-tasks and route them.",
                        fontSize = 12.sp,
                        color = ImmersiveTextSecondary
                    )

                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        label = { Text("High-Level Goal") },
                        placeholder = { Text("e.g. 'Audit competitor pricing'") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("planner_goal_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImmersivePrimary,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedLabelColor = ImmersivePrimary,
                            cursorColor = ImmersivePrimary,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    if (generatedPlan == null && !isGenerating) {
                        Button(
                            onClick = {
                                isGenerating = true
                                coroutineScope.launch {
                                    val aiPlan = geminiApiService.generateTaskPlan(goalText)
                                    if (aiPlan.isNotEmpty()) {
                                        val mappedPlan = aiPlan.map { json ->
                                            SubTaskPlan(
                                                title = json.optString("title", "Unknown Task"),
                                                description = json.optString("description", ""),
                                                botId = bots.random().id, // Assign to a random available bot for now
                                                priority = json.optString("priority", "Normal")
                                            )
                                        }
                                        generatedPlan = mappedPlan
                                    } else {
                                        generatedPlan = mockGeneratePlan(goalText, bots)
                                    }
                                    isGenerating = false
                                }
                            },
                            enabled = goalText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ImmersivePrimaryContainer,
                                contentColor = ImmersivePrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("generate_plan_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Breakdown Goal", fontWeight = FontWeight.Bold)
                        }
                    } else if (isGenerating) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = ImmersivePrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Orchestrator reasoning...", color = ImmersiveTextMuted, fontSize = 12.sp)
                        }
                    } else if (generatedPlan != null) {
                        Text(
                            text = "Orchestrated Sub-tasks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ImmersiveTextPrimary
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(generatedPlan!!.size) { index ->
                                val subTask = generatedPlan!![index]
                                val bot = bots.find { it.id == subTask.botId }
                                SubTaskCard(
                                    subTask = subTask,
                                    bot = bot,
                                    onPriorityChange = { newPriority ->
                                        val mutablePlan = generatedPlan!!.toMutableList()
                                        mutablePlan[index] = subTask.copy(priority = newPriority)
                                        generatedPlan = mutablePlan
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // TAB 1: DIRECT HANDOFF
                    if (rpcSuccess) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = ImmersiveActiveMint,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Handoff Successful via RPC",
                                color = ImmersiveActiveMint,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Artifacts and context dispatched to ${selectedTargetBot?.name}.",
                                color = ImmersiveTextSecondary,
                                fontSize = 12.sp
                            )
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary)
                            ) {
                                Text("Close")
                            }
                        }
                    } else {
                        Text(
                            text = "Select state contexts or artifacts to dispatch directly to a swarm agent via RPC.",
                            fontSize = 12.sp,
                            color = ImmersiveTextSecondary
                        )

                        // Target Agent Selection
                        ExposedDropdownMenuBox(
                            expanded = expandedBotDropdown,
                            onExpandedChange = { expandedBotDropdown = !expandedBotDropdown }
                        ) {
                            OutlinedTextField(
                                value = selectedTargetBot?.name ?: "Select Agent",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Target Agent") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBotDropdown) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveBorder,
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBotDropdown,
                                onDismissRequest = { expandedBotDropdown = false },
                                modifier = Modifier.background(ImmersiveSurfaceVariant)
                            ) {
                                bots.forEach { bot ->
                                    DropdownMenuItem(
                                        text = { Text("${bot.name} (${bot.role.title})") },
                                        onClick = {
                                            selectedTargetBot = bot
                                            expandedBotDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Context Artifacts
                        Text(
                            text = "Select Artifacts to Attach:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = ImmersiveTextPrimary
                        )

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableArtifacts.forEach { artifact ->
                                val isSelected = selectedArtifacts.contains(artifact)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedArtifacts.remove(artifact)
                                        else selectedArtifacts.add(artifact)
                                    },
                                    label = { Text(artifact, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ImmersivePrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = ImmersivePrimary,
                                        labelColor = ImmersiveTextMuted
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = handoffNote,
                            onValueChange = { handoffNote = it },
                            label = { Text("RPC Instruction Note") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveBorder,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (isDispatchingRpc) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = ImmersivePrimary,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Transmitting payload via RPC...", color = ImmersiveTextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0 && generatedPlan != null) {
                Button(
                    onClick = {
                        onDispatchPlan(goalText, generatedPlan!!)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersivePrimary,
                        contentColor = ImmersiveOnPrimary
                    ),
                    modifier = Modifier.testTag("dispatch_swarm_btn")
                ) {
                    Text("Dispatch Swarm", fontWeight = FontWeight.Bold)
                }
            } else if (selectedTab == 1 && !rpcSuccess && !isDispatchingRpc) {
                Button(
                    onClick = {
                        isDispatchingRpc = true
                        coroutineScope.launch {
                            delay(1200) // Simulate network RPC
                            isDispatchingRpc = false
                            rpcSuccess = true
                        }
                    },
                    enabled = selectedTargetBot != null && (selectedArtifacts.isNotEmpty() || handoffNote.isNotBlank()),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersiveActiveMint,
                        contentColor = Color(0xFF003816)
                    ),
                    modifier = Modifier.testTag("dispatch_rpc_btn")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dispatch via RPC", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTaskCard(subTask: SubTaskPlan, bot: BotEntity?, onPriorityChange: (String) -> Unit) {
    var expandedPriorityDropdown by remember { mutableStateOf(false) }
    val priorities = listOf("Normal", "High", "Urgent")
    val priorityColor = when (subTask.priority) {
        "Urgent" -> ImmersiveAlertCoral
        "High" -> Color(0xFFFFB300)
        else -> ImmersivePrimary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF141218),
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subTask.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ImmersiveTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subTask.description,
                        fontSize = 11.sp,
                        color = ImmersiveTextSecondary,
                        lineHeight = 14.sp
                    )
                }
                
                ExposedDropdownMenuBox(
                    expanded = expandedPriorityDropdown,
                    onExpandedChange = { expandedPriorityDropdown = !expandedPriorityDropdown }
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = priorityColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.5f)),
                        modifier = Modifier.menuAnchor().clickable { expandedPriorityDropdown = true }
                    ) {
                        Text(
                            text = subTask.priority,
                            color = priorityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = expandedPriorityDropdown,
                        onDismissRequest = { expandedPriorityDropdown = false },
                        modifier = Modifier.background(ImmersiveSurfaceVariant)
                    ) {
                        priorities.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 12.sp) },
                                onClick = {
                                    onPriorityChange(option)
                                    expandedPriorityDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
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
                            size = 20,
                            colorHex = bot.colorHex,
                            showStatusDot = false
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Assigned to: ${bot.name} (${bot.role.title})",
                            fontSize = 10.sp,
                            color = ImmersivePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text("Unassigned", color = ImmersiveAlertCoral, fontSize = 10.sp)
                    }
                }
                
                // Status Indicator Pill
                val statusColor = when (subTask.status) {
                    "Pending" -> ImmersiveTextMuted
                    "Ready" -> ImmersiveActiveMint
                    else -> ImmersivePrimary
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = subTask.status,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Simple deterministic mock generator for demonstration
fun mockGeneratePlan(goal: String, bots: List<BotEntity>): List<SubTaskPlan> {
    val lowerGoal = goal.lowercase()
    val plan = mutableListOf<SubTaskPlan>()
    
    // Pick appropriate bots based on roles
    val intelligenceBot = bots.find { it.role.name == "MARKET_INTELLIGENCE" } ?: bots.first()
    val terminalBot = bots.find { it.role.name == "TERMINAL_OPERATOR" } ?: bots.first()
    val salesBot = bots.find { it.role.name == "SALES_OUTBOUND" } ?: bots.first()
    
    if (lowerGoal.contains("audit") || lowerGoal.contains("pricing") || lowerGoal.contains("competitor")) {
        plan.add(SubTaskPlan("Scrape Competitor Pricing", "Navigate to 3 competitor websites, parse pricing tables, and save raw CSV data to shared volume.", intelligenceBot.id, "High"))
        plan.add(SubTaskPlan("Process & Filter Data", "Run pandas script in terminal to normalize pricing models and generate comparison matrix.", terminalBot.id))
        plan.add(SubTaskPlan("Draft Strategy Memo", "Synthesize findings into an executive summary and draft an email for the product team.", salesBot.id))
    } else {
        // Generic 3-step plan
        plan.add(SubTaskPlan("Initial Research & Data Gathering", "Use headless browser to fetch required context and save raw artifacts.", intelligenceBot.id, "High"))
        plan.add(SubTaskPlan("Analysis & Code Execution", "Process the gathered artifacts via python shell scripts.", terminalBot.id))
        plan.add(SubTaskPlan("Final Review & Reporting", "Format the processed data and draft final communications.", salesBot.id))
    }
    
    return plan
}
