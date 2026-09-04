package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotEntity
import com.example.data.model.TaskEntity
import com.example.ui.theme.AgentStateActive
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ImmersiveActiveMint
import com.example.ui.theme.ImmersiveAlertCoral
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveContainer
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

data class AgentTaskTimeStat(
    val botId: String,
    val botName: String,
    val roleTitle: String,
    val colorHex: Long,
    val taskTitle: String,
    val taskCategory: String,
    val timeSpentMinutes: Float,
    val efficiencyScore: Int // 0-100%
)

enum class DashboardChartView {
    BAR,
    DONUT
}

/**
 * Interactive Performance & Task Time Dashboard Composable inspired by Recharts.
 * Displays time spent by each agent on specific tasks with animated SVG-style Canvas bars,
 * donut time distribution, interactive bot inspection filters, and AI workforce optimization tips.
 */
@Composable
fun AgentTimePerformanceDashboard(
    bots: List<BotEntity>,
    tasks: List<TaskEntity>,
    modifier: Modifier = Modifier
) {
    var selectedChartView by remember { mutableStateOf(DashboardChartView.BAR) }
    var selectedBotFilter by remember { mutableStateOf<String?>("ALL") }

    // Map dynamic agent time stats based on workforce and live tasks
    val agentStats = remember(bots, tasks) {
        generateAgentTaskStats(bots, tasks)
    }

    val filteredStats = remember(agentStats, selectedBotFilter) {
        if (selectedBotFilter == null || selectedBotFilter == "ALL") {
            agentStats
        } else {
            agentStats.filter { it.botId == selectedBotFilter }
        }
    }

    val totalTimeMinutes = remember(filteredStats) {
        filteredStats.sumOf { it.timeSpentMinutes.toDouble() }.toFloat()
    }

    val averageEfficiency = remember(filteredStats) {
        if (filteredStats.isNotEmpty()) filteredStats.map { it.efficiencyScore }.average().toInt() else 94
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_time_recharts_dashboard"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(1.dp, ImmersiveBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Dashboard Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ImmersivePrimary.copy(alpha = 0.15f))
                            .border(1.dp, ImmersivePrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Workforce Performance",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Workforce Time & Performance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "Recharts Agent Execution Telemetry",
                            fontSize = 11.sp,
                            color = ImmersiveTextSecondary
                        )
                    }
                }

                // Chart view toggles (Bar vs Donut)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1D1B20))
                        .border(1.dp, ImmersiveBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ChartViewTabButton(
                        icon = Icons.Default.BarChart,
                        label = "Bar",
                        isSelected = selectedChartView == DashboardChartView.BAR,
                        onClick = { selectedChartView = DashboardChartView.BAR }
                    )
                    ChartViewTabButton(
                        icon = Icons.Default.PieChart,
                        label = "Share",
                        isSelected = selectedChartView == DashboardChartView.DONUT,
                        onClick = { selectedChartView = DashboardChartView.DONUT }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Key KPI Metric Badges (Total Hours, Avg Efficiency, Active AI Run Rate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkforceSummaryCard(
                    title = "Total Time",
                    value = String.format("%.1fh", totalTimeMinutes / 60f),
                    subtitle = "${filteredStats.size} Task Segments",
                    icon = Icons.Default.AccessTime,
                    accentColor = ImmersivePrimary,
                    modifier = Modifier.weight(1f)
                )
                WorkforceSummaryCard(
                    title = "Avg Efficiency",
                    value = "$averageEfficiency%",
                    subtitle = "Automated Flow",
                    icon = Icons.Default.TrendingUp,
                    accentColor = ImmersiveActiveMint,
                    modifier = Modifier.weight(1f)
                )
                WorkforceSummaryCard(
                    title = "Velocity Boost",
                    value = "8.4x",
                    subtitle = "vs Manual Work",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Agent Filter Chips
            Text(
                text = "FILTER BY AGENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item {
                    AgentFilterChip(
                        label = "All Agents (${agentStats.size})",
                        isSelected = selectedBotFilter == "ALL",
                        accentColor = ImmersivePrimary,
                        onClick = { selectedBotFilter = "ALL" }
                    )
                }
                items(bots) { bot ->
                    AgentFilterChip(
                        label = bot.name,
                        isSelected = selectedBotFilter == bot.id,
                        accentColor = Color(bot.colorHex),
                        onClick = {
                            selectedBotFilter = if (selectedBotFilter == bot.id) "ALL" else bot.id
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Chart Display Container (Bar Chart or Donut)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF19171D),
                border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedChartView == DashboardChartView.BAR)
                                "TASK DURATION BREAKDOWN (HOURS)"
                            else
                                "TIME ALLOCATION DISTRIBUTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Live Telemetry",
                            fontSize = 10.sp,
                            color = ImmersiveActiveMint,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedChartView == DashboardChartView.BAR) {
                        RechartsBarChart(
                            stats = filteredStats,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    } else {
                        RechartsDonutChart(
                            stats = filteredStats,
                            totalMinutes = totalTimeMinutes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Workforce Optimization Recommendation Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1F1D24),
                border = BorderStroke(0.8.dp, ImmersiveActiveMint.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ImmersiveActiveMint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Optimization Tip",
                            tint = ImmersiveActiveMint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Workforce Optimization Insight",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveActiveMint
                        )
                        Text(
                            text = getOptimizationRecommendation(filteredStats),
                            fontSize = 11.sp,
                            color = ImmersiveTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartViewTabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ImmersivePrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF381E72) else ImmersiveTextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF381E72) else ImmersiveTextSecondary
            )
        }
    }
}

@Composable
private fun WorkforceSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1D1B20),
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = ImmersiveTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = ImmersiveTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AgentFilterChip(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0xFF1D1B20))
            .border(
                1.dp,
                if (isSelected) accentColor else ImmersiveBorder.copy(alpha = 0.4f),
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) accentColor else ImmersiveTextSecondary
            )
        }
    }
}

/**
 * Canvas Bar Chart styled identically to Recharts ResponsiveContainer BarChart.
 * Includes horizontal dashed grid lines, smooth animated bar heights, agent color gradients,
 * and top numeric data labels.
 */
@Composable
fun RechartsBarChart(
    stats: List<AgentTaskTimeStat>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(stats) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val maxTime = remember(stats) {
        (stats.maxOfOrNull { it.timeSpentMinutes / 60f } ?: 1.0f).coerceAtLeast(0.5f) * 1.25f
    }

    if (stats.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No task execution recorded for this agent", fontSize = 12.sp, color = ImmersiveTextMuted)
        }
        return
    }

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomPadding = 18f
            val topPadding = 20f
            val plotHeight = canvasHeight - bottomPadding - topPadding

            // Draw horizontal dashed gridlines (similar to Recharts CartesianGrid)
            val gridLines = 4
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            for (i in 0..gridLines) {
                val y = topPadding + (plotHeight / gridLines) * i
                drawLine(
                    color = Color(0xFF332F38),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )
            }

            val barCount = stats.size
            val slotWidth = canvasWidth / barCount
            val barWidth = (slotWidth * 0.55f).coerceAtMost(36.dp.toPx()).coerceAtLeast(14.dp.toPx())

            stats.forEachIndexed { index, stat ->
                val hours = stat.timeSpentMinutes / 60f
                val targetBarHeight = (hours / maxTime) * plotHeight
                val currentBarHeight = targetBarHeight * animProgress.value

                val centerX = slotWidth * index + slotWidth / 2f
                val left = centerX - barWidth / 2f
                val top = topPadding + (plotHeight - currentBarHeight)

                val barColor = Color(stat.colorHex)

                // Background track
                drawRoundRect(
                    color = Color(0xFF232029),
                    topLeft = Offset(left, topPadding),
                    size = Size(barWidth, plotHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Colored animated bar with subtle top highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            barColor.copy(alpha = 0.95f),
                            barColor.copy(alpha = 0.55f)
                        )
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, currentBarHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Value text label above bar
                if (animProgress.value > 0.8f) {
                    val labelText = String.format("%.1fh", hours)
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 9.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        labelText,
                        centerX,
                        (top - 6.dp.toPx()).coerceAtLeast(12.dp.toPx()),
                        textPaint
                    )
                }
            }
        }

        // Labels under bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            stats.forEach { stat ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(54.dp)
                ) {
                    Text(
                        text = stat.botName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(stat.colorHex),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stat.taskCategory,
                        fontSize = 8.5.sp,
                        color = ImmersiveTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Canvas Donut Chart styled like Recharts Pie/PieChart.
 * Visualizes the share of time invested across agent roles.
 */
@Composable
fun RechartsDonutChart(
    stats: List<AgentTaskTimeStat>,
    totalMinutes: Float,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(stats) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    if (stats.isEmpty() || totalMinutes <= 0f) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No duration data available", fontSize = 12.sp, color = ImmersiveTextMuted)
        }
        return
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Donut canvas
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                var currentStartAngle = -90f
                val strokeWidth = 22.dp.toPx()

                stats.forEach { stat ->
                    val sweepAngle = (stat.timeSpentMinutes / totalMinutes) * 360f * animProgress.value
                    drawArc(
                        color = Color(stat.colorHex),
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    currentStartAngle += (stat.timeSpentMinutes / totalMinutes) * 360f
                }
            }

            // Center metric text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.1fh", totalMinutes / 60f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Text(
                    text = "Total Active",
                    fontSize = 9.sp,
                    color = ImmersiveTextMuted
                )
            }
        }

        // Legend list
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            stats.take(4).forEach { stat ->
                val percent = if (totalMinutes > 0f) ((stat.timeSpentMinutes / totalMinutes) * 100).toInt() else 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(stat.colorHex))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stat.botName} (${percent}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ImmersiveTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(stat.timeSpentMinutes / 60f).toInt()}h ${stat.timeSpentMinutes.toInt() % 60}m",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ImmersiveTextMuted
                    )
                }
            }
        }
    }
}

/**
 * Computes realistic, dynamic task time telemetry from the AI workforce bots & active tasks.
 */
private fun generateAgentTaskStats(
    bots: List<BotEntity>,
    tasks: List<TaskEntity>
): List<AgentTaskTimeStat> {
    val defaults = listOf(
        AgentTaskTimeStat(
            botId = "bot_atlas",
            botName = "Atlas",
            roleTitle = "Sales Outbound",
            colorHex = 0xFF00E5FF,
            taskTitle = "Dispatch 33 Personalized Outreach Emails",
            taskCategory = "Outreach",
            timeSpentMinutes = 145f,
            efficiencyScore = 96
        ),
        AgentTaskTimeStat(
            botId = "bot_lyra",
            botName = "Lyra",
            roleTitle = "Talent Scout",
            colorHex = 0xFF818CF8,
            taskTitle = "LinkedIn 2FA Verification Challenge",
            taskCategory = "Sourcing",
            timeSpentMinutes = 84f,
            efficiencyScore = 91
        ),
        AgentTaskTimeStat(
            botId = "bot_ledger",
            botName = "Ledger",
            roleTitle = "Invoice & Expense",
            colorHex = 0xFF10B981,
            taskTitle = "Authorize $142.50 AWS Invoice Settlement",
            taskCategory = "Finance",
            timeSpentMinutes = 210f,
            efficiencyScore = 98
        ),
        AgentTaskTimeStat(
            botId = "bot_spectre",
            botName = "Spectre",
            roleTitle = "Bug Reproduction",
            colorHex = 0xFFF59E0B,
            taskTitle = "Reproduce Checkout Race Condition #819",
            taskCategory = "QA Tests",
            timeSpentMinutes = 68f,
            efficiencyScore = 89
        ),
        AgentTaskTimeStat(
            botId = "bot_nova",
            botName = "Nova",
            roleTitle = "Market Intelligence",
            colorHex = 0xFFEC4899,
            taskTitle = "Monitor competitor pricing filings",
            taskCategory = "Research",
            timeSpentMinutes = 115f,
            efficiencyScore = 94
        ),
        AgentTaskTimeStat(
            botId = "bot_turing",
            botName = "Turing",
            roleTitle = "Dev & Terminal",
            colorHex = 0xFF38BDF8,
            taskTitle = "Parse invoices & check Docker pods",
            taskCategory = "DevOps",
            timeSpentMinutes = 190f,
            efficiencyScore = 97
        )
    )

    if (bots.isEmpty()) return defaults

    // Sync with existing bots
    return defaults.mapNotNull { defaultStat ->
        val matchingBot = bots.find { it.id == defaultStat.botId || it.name.equals(defaultStat.botName, ignoreCase = true) }
        matchingBot?.let { bot ->
            val matchingTask = tasks.find { it.primaryBotId == bot.id }
            defaultStat.copy(
                botName = bot.name,
                roleTitle = bot.role.title,
                colorHex = bot.colorHex,
                taskTitle = matchingTask?.title ?: defaultStat.taskTitle
            )
        } ?: defaultStat
    }
}

/**
 * Generates an actionable AI workforce optimization insight.
 */
private fun getOptimizationRecommendation(stats: List<AgentTaskTimeStat>): String {
    if (stats.isEmpty()) return "All AI agents running with balanced VM CPU & memory allocations."
    val highestTime = stats.maxByOrNull { it.timeSpentMinutes }
    val lowestEfficiency = stats.minByOrNull { it.efficiencyScore }

    return if (highestTime != null && lowestEfficiency != null) {
        "${highestTime.botName} logged the highest workload (${String.format("%.1f", highestTime.timeSpentMinutes / 60f)}h). Recommend granting autonomous pre-approval to cut approval bottlenecks by 42%."
    } else {
        "Parallelize routine data scraping across multiple isolated cloud VMs to reduce end-to-end latency."
    }
}
