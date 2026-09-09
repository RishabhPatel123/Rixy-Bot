package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.McpServerEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoworkerViewModel

@Composable
fun IntegrationsSettingsScreen(
    viewModel: CoworkerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mcpServers by viewModel.mcpServers.collectAsState()

    // Filter to specific servers requested: GitHub, Slack, AWS
    val integrationServers = mcpServers.filter {
        it.serverName.contains("GitHub", ignoreCase = true) ||
        it.serverName.contains("Slack", ignoreCase = true) ||
        it.serverName.contains("AWS", ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ImmersiveSurface)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onBack() }
                    .background(ImmersiveSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ImmersivePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "SETTINGS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersivePrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Integrations & Databases",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Enterprise Tooling & Cloud Platforms",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manage agent access to your corporate services via Model Context Protocol (MCP) integrations.",
                    fontSize = 12.sp,
                    color = ImmersiveTextSecondary,
                    lineHeight = 16.sp
                )
            }

            items(integrationServers) { server ->
                val icon = when {
                    server.serverName.contains("GitHub", ignoreCase = true) -> Icons.Default.Code
                    server.serverName.contains("Slack", ignoreCase = true) -> Icons.Default.Message
                    server.serverName.contains("AWS", ignoreCase = true) -> Icons.Default.Cloud
                    else -> Icons.Default.Cloud
                }
                IntegrationCard(
                    server = server,
                    icon = icon,
                    onToggle = { viewModel.toggleMcpServerStatus(server) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Other Connected Databases (MCP)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            }

            val otherServers = mcpServers.filterNot { integrationServers.contains(it) }
            items(otherServers) { server ->
                IntegrationCard(
                    server = server,
                    icon = Icons.Default.Cloud,
                    onToggle = { viewModel.toggleMcpServerStatus(server) }
                )
            }
        }
    }
}

@Composable
fun IntegrationCard(
    server: McpServerEntity,
    icon: ImageVector,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = server.status == "CONNECTED" || server.status == "SYNCING"
    
    val statusColor = when (server.status) {
        "CONNECTED" -> ImmersiveActiveMint
        "SYNCING" -> Color(0xFFFFB300)
        else -> Color.Gray
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("integration_card_${server.id}"),
        shape = RoundedCornerShape(20.dp),
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isConnected) ImmersivePrimaryContainer else DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (server.status == "SYNCING") {
                            CircularProgressIndicator(
                                color = ImmersivePrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = server.serverName,
                                tint = if (isConnected) ImmersivePrimary else ImmersiveTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = server.serverName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isConnected) ImmersiveTextPrimary else ImmersiveTextMuted
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = server.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                
                Switch(
                    checked = isConnected,
                    onCheckedChange = { onToggle() },
                    enabled = server.status != "SYNCING",
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ImmersivePrimary,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("toggle_integration_${server.id}")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = server.description,
                fontSize = 12.sp,
                color = ImmersiveTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF141218),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Protocol URL",
                        fontSize = 10.sp,
                        color = ImmersiveTextMuted
                    )
                    Text(
                        text = server.protocolUrl,
                        fontSize = 10.sp,
                        color = ImmersivePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
