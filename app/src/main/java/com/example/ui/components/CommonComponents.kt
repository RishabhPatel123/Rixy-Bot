package com.example.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotActivityState
import com.example.data.model.BotEntity
import com.example.data.model.BotRole
import com.example.data.model.TaskStatus
import com.example.ui.theme.AgentStateActive
import com.example.ui.theme.AgentStateError
import com.example.ui.theme.AgentStateIdle
import com.example.ui.theme.AgentStatePaused
import com.example.ui.theme.AgentStateWorking
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ImmersiveActiveMint
import com.example.ui.theme.ImmersiveAlertCoral
import com.example.ui.theme.ImmersiveAlertOnCoral
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
import com.example.ui.theme.StatusAmberAnalysis
import com.example.ui.theme.StatusCoralInput
import com.example.ui.theme.StatusCyanResearch
import com.example.ui.theme.StatusMintCompleted
import com.example.ui.theme.StatusMutedIdle
import com.example.ui.theme.StatusPurpleDraft

@Composable
fun BotAvatar(
    botName: String,
    role: BotRole,
    modifier: Modifier = Modifier,
    size: Int = 42,
    colorHex: Long = 0xFF00E5FF,
    showStatusDot: Boolean = true,
    isWorking: Boolean = true,
    activityState: BotActivityState? = null
) {
    val accentColor = Color(colorHex)
    val dotColor = when (activityState) {
        BotActivityState.RESEARCHING -> StatusCyanResearch
        BotActivityState.DRAFTING -> StatusPurpleDraft
        BotActivityState.ANALYZING -> StatusAmberAnalysis
        BotActivityState.WAITING_FOR_INPUT -> StatusCoralInput
        BotActivityState.COMPLETED -> StatusMintCompleted
        BotActivityState.IDLE -> StatusMutedIdle
        null -> if (isWorking) EmeraldAccent else Color.Gray
    }

    val isPulsing = activityState in listOf(
        BotActivityState.RESEARCHING,
        BotActivityState.DRAFTING,
        BotActivityState.ANALYZING,
        BotActivityState.WAITING_FOR_INPUT
    ) || (activityState == null && isWorking)

    val pulseTransition = rememberInfiniteTransition(label = "bot_avatar_pulse")
    val pulseAlpha by if (isPulsing) {
        pulseTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "avatar_pulse_alpha"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0.12f)
                        )
                    )
                )
                .border(1.5.dp, accentColor.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val initial = botName.firstOrNull()?.toString() ?: "B"
            Text(
                text = initial,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.42).sp
            )
        }

        if (showStatusDot) {
            Box(
                modifier = Modifier
                    .size((size * 0.34).coerceAtLeast(10.0).dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = if (isPulsing) pulseAlpha else 1f))
                    .border(1.5.dp, DarkSurface, CircleShape)
            )
        }
    }
}

@Composable
fun BotActivityBadge(
    activityState: BotActivityState,
    modifier: Modifier = Modifier
) {
    val (bg, fg, icon) = when (activityState) {
        BotActivityState.RESEARCHING -> Triple(
            StatusCyanResearch.copy(alpha = 0.16f),
            StatusCyanResearch,
            Icons.Default.FindInPage
        )
        BotActivityState.DRAFTING -> Triple(
            StatusPurpleDraft.copy(alpha = 0.16f),
            StatusPurpleDraft,
            Icons.Default.EditNote
        )
        BotActivityState.ANALYZING -> Triple(
            StatusAmberAnalysis.copy(alpha = 0.16f),
            StatusAmberAnalysis,
            Icons.Default.Insights
        )
        BotActivityState.WAITING_FOR_INPUT -> Triple(
            StatusCoralInput.copy(alpha = 0.20f),
            StatusCoralInput,
            Icons.Default.HourglassTop
        )
        BotActivityState.COMPLETED -> Triple(
            StatusMintCompleted.copy(alpha = 0.16f),
            StatusMintCompleted,
            Icons.Default.CheckCircle
        )
        BotActivityState.IDLE -> Triple(
            StatusMutedIdle.copy(alpha = 0.12f),
            StatusMutedIdle,
            Icons.Default.SmartToy
        )
    }

    val isPulsing = activityState in listOf(
        BotActivityState.RESEARCHING,
        BotActivityState.DRAFTING,
        BotActivityState.ANALYZING,
        BotActivityState.WAITING_FOR_INPUT
    )

    val pulseTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseAlpha by if (isPulsing) {
        pulseTransition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "badge_pulse_alpha"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(0.8.dp, fg.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg.copy(alpha = pulseAlpha))
        )
        Spacer(modifier = Modifier.width(5.dp))
        Icon(
            imageVector = icon,
            contentDescription = activityState.label,
            tint = fg,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = activityState.label,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bg, fg, icon) = when (status.uppercase()) {
        "ACTIVE", "ONLINE" -> Triple(
            AgentStateActive.copy(alpha = 0.16f),
            AgentStateActive,
            Icons.Default.CheckCircle
        )
        "WORKING", "RUNNING" -> Triple(
            AgentStateWorking.copy(alpha = 0.18f),
            AgentStateWorking,
            Icons.Default.Bolt
        )
        "ERROR", "FAILED" -> Triple(
            AgentStateError.copy(alpha = 0.18f),
            AgentStateError,
            Icons.Default.Warning
        )
        "AWAITING_APPROVAL", "PARKED", "PAUSED", "WAITING" -> Triple(
            AgentStatePaused.copy(alpha = 0.18f),
            AgentStatePaused,
            Icons.Default.HourglassTop
        )
        "COMPLETED" -> Triple(
            AgentStateActive.copy(alpha = 0.16f),
            AgentStateActive,
            Icons.Default.CheckCircle
        )
        "IDLE", "STANDBY" -> Triple(
            AgentStateIdle.copy(alpha = 0.14f),
            AgentStateIdle,
            Icons.Default.SmartToy
        )
        else -> Triple(
            AgentStateIdle.copy(alpha = 0.14f),
            AgentStateIdle,
            Icons.Default.SmartToy
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.8.dp, fg.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status,
            tint = fg,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.replace("_", " "),
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Dynamic Agent Status Indicator UI component.
 * Explicitly transitions color based on agent state:
 * - Green (AgentStateActive) for Active / Online
 * - Amber (AgentStateWorking) for Working / Processing
 * - Red (AgentStateError) for Error / Failure
 * - Blue (AgentStatePaused) for Paused / Awaiting Input
 * - Gray (AgentStateIdle) for Idle / Standby
 *
 * Supports optional pulse animation and label toggle.
 */
@Composable
fun AgentStatusIndicator(
    status: String,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    isPulsing: Boolean = true
) {
    val upper = status.uppercase()
    val (statusColor, statusIcon, labelText) = when {
        upper in listOf("ACTIVE", "ONLINE", "READY") -> Triple(
            AgentStateActive,
            Icons.Default.CheckCircle,
            "Active"
        )
        upper in listOf("WORKING", "RUNNING", "BUSY", "PROCESSING", "RESEARCHING", "DRAFTING", "ANALYZING") -> Triple(
            AgentStateWorking,
            Icons.Default.Bolt,
            "Working"
        )
        upper in listOf("ERROR", "FAILED", "ALERT") -> Triple(
            AgentStateError,
            Icons.Default.Warning,
            "Error"
        )
        upper in listOf("PAUSED", "PARKED", "WAITING", "AWAITING_APPROVAL", "WAITING_FOR_INPUT") -> Triple(
            AgentStatePaused,
            Icons.Default.HourglassTop,
            "Paused"
        )
        else -> Triple(
            AgentStateIdle,
            Icons.Default.SmartToy,
            if (upper.isNotBlank()) status.replace("_", " ") else "Idle"
        )
    }

    val shouldPulse = isPulsing && upper in listOf("WORKING", "RUNNING", "BUSY", "PROCESSING", "RESEARCHING", "DRAFTING", "ANALYZING")
    val pulseTransition = rememberInfiniteTransition(label = "agent_status_pulse")
    val pulseAlpha by if (shouldPulse) {
        pulseTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(statusColor.copy(alpha = 0.16f))
            .border(0.8.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = pulseAlpha))
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = statusIcon,
                contentDescription = labelText,
                tint = statusColor,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = labelText,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
fun VMClusterCard(
    activeVmCount: Int,
    runningBotsCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vm_cluster_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, ImmersiveBorder)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2B2930), Color(0xFF1D1B20))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Header: Current Swarm Tag + Cloud Instance Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "CURRENT SWARM",
                            style = MaterialTheme.typography.labelSmall,
                            color = ImmersivePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sales Outbound",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF381E72)
                    ) {
                        Text(
                            text = "Cloud Instance: X-09",
                            color = ImmersivePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Visual Navigation Progress Bar (Matching Immersive UI HTML)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1D1B20).copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF4A4458)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Browser Navigating",
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Navigating: Apollo.io",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCAC4D0),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "85%",
                                    fontSize = 12.sp,
                                    color = ImmersivePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ImmersiveBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(ImmersivePrimary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "'Scraping target profiles and cross-referencing with internal CRM filters...'",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF938F99),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Telemetry Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricPill(
                        icon = Icons.Default.Computer,
                        label = "Cloud VMs",
                        value = "$activeVmCount Active",
                        modifier = Modifier.weight(1f)
                    )
                    MetricPill(
                        icon = Icons.Default.SmartToy,
                        label = "Workforce",
                        value = "$runningBotsCount Online",
                        modifier = Modifier.weight(1f)
                    )
                    MetricPill(
                        icon = Icons.Default.Terminal,
                        label = "Sandbox",
                        value = "Isolated",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricPill(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1D1B20),
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = ImmersivePrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color(0xFFCAC4D0)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE6E1E5),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VisualBrowserWindow(
    url: String,
    stepDescription: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(1.dp, ImmersiveBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Browser Address Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1D1B20))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Window dots
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                Spacer(modifier = Modifier.width(5.dp))
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Spacer(modifier = Modifier.width(5.dp))
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ImmersiveActiveMint))
                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "URL",
                    tint = Color(0xFF938F99),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = url,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFCAC4D0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated Visual Computer Use Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141218))
                    .border(1.dp, ImmersiveBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "COMPUTER-USE VISUAL AGENT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersivePrimary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "FPS: 30 | HEADLESS",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF938F99)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stepDescription,
                        fontSize = 12.sp,
                        color = Color(0xFFE6E1E5),
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Visual simulated mouse cursor
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ImmersivePrimary.copy(alpha = 0.2f))
                        .border(1.dp, ImmersivePrimary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Cursor Click",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "CLICK (x:840, y:390)",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersivePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileAttachmentChip(
    fileName: String,
    fileType: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1D1B20),
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = fileName,
                tint = when (fileType?.lowercase()) {
                    "pdf" -> Color(0xFFF87171)
                    "csv" -> ImmersiveActiveMint
                    "webm" -> ImmersivePrimary
                    else -> ImmersivePrimary
                },
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = fileName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE6E1E5)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SHARED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF938F99)
            )
        }
    }
}

@Composable
fun TerminalSnippetView(
    commandOutput: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141218)),
        border = BorderStroke(1.dp, ImmersiveBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = ImmersiveActiveMint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CLOUD VM BASH SESSION",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ImmersiveActiveMint,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = commandOutput,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE6E1E5),
                lineHeight = 15.sp
            )
        }
    }
}

