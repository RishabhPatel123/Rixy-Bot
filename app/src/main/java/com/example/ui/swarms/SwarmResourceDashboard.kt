package com.example.ui.swarms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.data.model.SwarmEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.BotAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwarmResourceDashboard(
    swarm: SwarmEntity,
    bots: List<BotEntity>,
    tasks: List<TaskEntity>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("swarm_agent_resource_pull_to_refresh"),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = rememberPullToRefreshState(),
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = ImmersiveSurface,
                color = ImmersivePrimary
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("swarm_agent_list_lazy_column"),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cloud Sandbox Analytics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ImmersiveTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Live telemetry for VM ${swarm.sharedVmId} • Pull to update",
                            fontSize = 12.sp,
                            color = ImmersiveTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("refresh_swarm_telemetry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Swarm Telemetry",
                            tint = if (isRefreshing) ImmersivePrimary else ImmersiveTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Shared Volume Usage Card
            item {
                SharedVolumeCard(swarm = swarm)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agent Resource Allocation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ImmersiveTextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "${bots.size} Swarm Agents",
                        fontSize = 12.sp,
                        color = CyanPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            items(bots) { bot ->
                val botTasks = tasks.filter { it.primaryBotId == bot.id && it.status.name != "COMPLETED" }
                AgentResourceCard(bot = bot, activeTasks = botTasks)
            }
        }
    }
}

@Composable
fun SharedVolumeCard(swarm: SwarmEntity) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ImmersiveSurface,
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Storage",
                    tint = ImmersiveActiveMint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shared Context Volume",
                    fontWeight = FontWeight.SemiBold,
                    color = ImmersiveTextPrimary,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mock volume usage
            val usageRatio = 0.35f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Storage Used", fontSize = 12.sp, color = ImmersiveTextSecondary)
                Text("3.5 GB / 10 GB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ImmersivePrimary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { usageRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ImmersiveActiveMint,
                trackColor = Color(0xFF1D1B20)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All bots in this swarm can read/write to this volume.",
                fontSize = 11.sp,
                color = ImmersiveTextMuted
            )
        }
    }
}

@Composable
fun AgentResourceCard(bot: BotEntity, activeTasks: List<TaskEntity>) {
    // Generate some mock fluctuating usage values for demo
    var cpuUsage by remember { mutableStateOf(if (bot.status == "WORKING") Random.nextInt(40, 85) else Random.nextInt(1, 5)) }
    var ramUsage by remember { mutableStateOf(if (bot.status == "WORKING") Random.nextInt(500, 1500) else Random.nextInt(100, 300)) }

    LaunchedEffect(bot.status) {
        while (true) {
            delay(2000)
            if (bot.status == "WORKING") {
                cpuUsage = (cpuUsage + Random.nextInt(-10, 11)).coerceIn(20, 95)
                ramUsage = (ramUsage + Random.nextInt(-50, 51)).coerceIn(800, 2048)
            } else {
                cpuUsage = (cpuUsage + Random.nextInt(-2, 3)).coerceIn(1, 10)
                ramUsage = (ramUsage + Random.nextInt(-10, 11)).coerceIn(100, 400)
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141218),
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotAvatar(
                        botName = bot.name,
                        role = bot.role,
                        size = 32,
                        colorHex = bot.colorHex,
                        showStatusDot = true,
                        isWorking = bot.status == "WORKING",
                        activityState = bot.activityState
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = bot.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = bot.role.title,
                            fontSize = 10.sp,
                            color = ImmersivePrimary
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (bot.status == "WORKING") ImmersiveActiveMint.copy(alpha = 0.1f) else Color.Transparent,
                    border = BorderStroke(1.dp, if (bot.status == "WORKING") ImmersiveActiveMint.copy(alpha = 0.3f) else ImmersiveBorder),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = bot.activityState.label,
                        fontSize = 9.sp,
                        color = if (bot.status == "WORKING") ImmersiveActiveMint else ImmersiveTextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resource Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // CPU
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = "CPU", tint = ImmersiveTextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CPU", fontSize = 11.sp, color = ImmersiveTextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$cpuUsage%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (cpuUsage > 80) ImmersiveAlertCoral else ImmersivePrimary
                    )
                }
                
                Divider(modifier = Modifier.height(30.dp).width(1.dp), color = ImmersiveBorder)
                
                // RAM
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Memory, contentDescription = "RAM", tint = ImmersiveTextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RAM", fontSize = 11.sp, color = ImmersiveTextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ramUsage}MB",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersivePrimary
                    )
                }
            }

            // Active Tasks
            if (activeTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = ImmersiveBorder.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Active Tasks (${activeTasks.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                activeTasks.forEach { task ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ImmersiveSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.title,
                                fontSize = 12.sp,
                                color = ImmersiveTextPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${task.progressPercent}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImmersivePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
