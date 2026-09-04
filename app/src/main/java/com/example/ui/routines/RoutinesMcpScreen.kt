package com.example.ui.routines

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.McpServerEntity
import com.example.data.model.RoutineEntity
import com.example.ui.components.BotAvatar
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ImmersiveActiveMint
import com.example.ui.theme.ImmersiveAlertCoral
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
fun RoutinesMcpScreen(
    viewModel: CoworkerViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val mcpServers by viewModel.mcpServers.collectAsState()
    val bots by viewModel.bots.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ImmersiveSurface)
                .padding(18.dp)
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
                        imageVector = Icons.Default.Extension,
                        contentDescription = "Routines & MCP",
                        tint = ImmersivePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AUTOMATION & PLUGINS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersivePrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Scheduled Routines & MCP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Program bots on recurring timers and securely bridge enterprise databases via Model Context Protocol (MCP) servers.",
                fontSize = 12.sp,
                color = ImmersiveTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1D1B20),
                contentColor = ImmersivePrimary,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Routines (${routines.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) ImmersivePrimary else ImmersiveTextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "MCP Servers (${mcpServers.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) ImmersivePrimary else ImmersiveTextMuted
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedTab == 0) {
                item {
                    Text(
                        text = "Automated Timers & Triggers",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                }

                items(routines) { routine ->
                    val bot = bots.find { it.id == routine.assignedBotId }
                    RoutineCard(
                        routine = routine,
                        assignedBotName = bot?.name ?: "Specialist Bot",
                        onToggle = { viewModel.toggleRoutine(routine) }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Enterprise Model Context Protocol Servers",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                }

                items(mcpServers) { server ->
                    McpServerCard(
                        server = server,
                        onToggle = { viewModel.toggleMcpServerStatus(server) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineCard(
    routine: RoutineEntity,
    assignedBotName: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ImmersivePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Timer",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = routine.scheduleCron,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersivePrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ImmersivePrimary,
                        checkedTrackColor = ImmersivePrimaryContainer,
                        uncheckedThumbColor = ImmersiveTextMuted,
                        uncheckedTrackColor = Color(0xFF2B2930)
                    ),
                    modifier = Modifier.testTag("routine_toggle_${routine.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = routine.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = routine.routineAction,
                style = MaterialTheme.typography.bodySmall,
                color = ImmersiveTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF141218),
                border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assigned: $assignedBotName",
                        fontSize = 11.sp,
                        color = ImmersiveTextMuted
                    )
                    Text(
                        text = "Next: ${routine.nextRunTime}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ImmersiveActiveMint
                    )
                }
            }
        }
    }
}

@Composable
fun McpServerCard(
    server: McpServerEntity,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = server.status == "CONNECTED"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mcp_card_${server.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(
            1.dp,
            if (isConnected) ImmersiveActiveMint.copy(alpha = 0.4f) else ImmersiveBorder.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) ImmersiveActiveMint.copy(alpha = 0.15f) else Color(0xFF2B2930)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataArray,
                            contentDescription = "MCP Protocol",
                            tint = if (isConnected) ImmersiveActiveMint else ImmersiveTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = server.serverName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = server.protocolUrl,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersivePrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isConnected) ImmersiveActiveMint.copy(alpha = 0.15f) else Color(0xFF2B2930),
                    border = BorderStroke(
                        1.dp,
                        if (isConnected) ImmersiveActiveMint.copy(alpha = 0.4f) else ImmersiveBorder.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.clickable { onToggle() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) ImmersiveActiveMint else ImmersiveTextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = server.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) ImmersiveActiveMint else ImmersiveTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = server.description,
                style = MaterialTheme.typography.bodySmall,
                color = ImmersiveTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Tools",
                    tint = ImmersiveTextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${server.toolsCount} MCP Tool Endpoints Registered",
                    fontSize = 11.sp,
                    color = ImmersiveTextMuted
                )
            }
        }
    }
}
