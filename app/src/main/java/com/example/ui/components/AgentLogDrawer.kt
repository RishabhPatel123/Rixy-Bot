package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.ui.theme.AgentStateActive
import com.example.ui.theme.AgentStateError
import com.example.ui.theme.AgentStateWorking
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ImmersiveActiveMint
import com.example.ui.theme.ImmersiveAlertBg
import com.example.ui.theme.ImmersiveAlertBorder
import com.example.ui.theme.ImmersiveAlertCoral
import com.example.ui.theme.ImmersiveAlertOnCoral
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AgentLogEntry(
    val timestamp: Long,
    val level: String, // "ERROR", "WARN", "INFO", "CRITICAL"
    val stage: String,
    val message: String,
    val errorTrace: String? = null,
    val targetUrlOrFile: String? = null,
    val exitCode: Int? = null
)

/**
 * Dedicated Drawer / BottomSheet for inspecting live execution logs, error traces,
 * stack dumps, and troubleshooting remediation actions for any AI agent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentLogDrawer(
    bot: BotEntity,
    tasks: List<TaskEntity>,
    onDismiss: () -> Unit,
    onRetryTask: ((TaskEntity) -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "ERROR", "WARN", "INFO"
    var expandedTraceIndex by remember { mutableStateOf<Int?>(null) }

    // Generate comprehensive execution log entries from bot context and tasks
    val logEntries = remember(bot, tasks) {
        generateAgentLogs(bot, tasks)
    }

    val filteredLogs = remember(logEntries, selectedFilter) {
        if (selectedFilter == "ALL") logEntries
        else logEntries.filter { it.level.equals(selectedFilter, ignoreCase = true) }
    }

    val errorCount = remember(logEntries) {
        logEntries.count { it.level == "ERROR" || it.level == "CRITICAL" }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ImmersiveSurfaceVariant,
        dragHandle = null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_log_drawer_${bot.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(top = 12.dp)
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ImmersiveBorder)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Drawer Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotAvatar(
                        botName = bot.name,
                        role = bot.role,
                        size = 38,
                        colorHex = bot.colorHex,
                        isWorking = bot.status == "WORKING",
                        activityState = bot.activityState
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${bot.name} Error & Diagnostic Logs",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary
                            )
                        }
                        Text(
                            text = "${bot.role.title} • VM: ${bot.currentVmHost}",
                            fontSize = 11.sp,
                            color = ImmersiveTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Logs",
                        tint = ImmersiveTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Error Status Alert Strip
            if (errorCount > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ImmersiveAlertBg,
                    border = BorderStroke(1.dp, ImmersiveAlertBorder.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(AgentStateError.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = "Errors",
                                    tint = AgentStateError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$errorCount Issue${if (errorCount > 1) "s" else ""} Captured During Execution",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveAlertCoral
                                )
                                Text(
                                    text = "Inspect stack traces below to identify root cause",
                                    fontSize = 10.sp,
                                    color = ImmersiveTextSecondary
                                )
                            }
                        }

                        // Copy full log button
                        OutlinedButton(
                            onClick = {
                                val fullLogText = formatFullLogText(bot, logEntries)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Agent Log Trace", fullLogText))
                                Toast.makeText(context, "Full error trace copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, ImmersiveAlertCoral.copy(alpha = 0.5f)),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = ImmersiveAlertCoral,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Trace", fontSize = 11.sp, color = ImmersiveAlertCoral)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Severity Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    LogFilterBadge(
                        label = "All (${logEntries.size})",
                        isSelected = selectedFilter == "ALL",
                        color = ImmersivePrimary,
                        onClick = { selectedFilter = "ALL" }
                    )
                }
                item {
                    LogFilterBadge(
                        label = "Errors ($errorCount)",
                        isSelected = selectedFilter == "ERROR",
                        color = AgentStateError,
                        onClick = { selectedFilter = "ERROR" }
                    )
                }
                item {
                    val warnCount = logEntries.count { it.level == "WARN" }
                    LogFilterBadge(
                        label = "Warnings ($warnCount)",
                        isSelected = selectedFilter == "WARN",
                        color = AgentStateWorking,
                        onClick = { selectedFilter = "WARN" }
                    )
                }
                item {
                    val infoCount = logEntries.count { it.level == "INFO" }
                    LogFilterBadge(
                        label = "Info ($infoCount)",
                        isSelected = selectedFilter == "INFO",
                        color = CyanPrimary,
                        onClick = { selectedFilter = "INFO" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Log entries list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredLogs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Clean Logs",
                                    tint = ImmersiveActiveMint,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No logs matching '$selectedFilter'",
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveTextPrimary
                                )
                                Text(
                                    text = "Agent runtime execution healthy with no active faults",
                                    fontSize = 11.sp,
                                    color = ImmersiveTextMuted
                                )
                            }
                        }
                    }
                } else {
                    items(filteredLogs.size) { index ->
                        val entry = filteredLogs[index]
                        val isExpanded = expandedTraceIndex == index

                        AgentLogItemCard(
                            entry = entry,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedTraceIndex = if (isExpanded) null else index
                            },
                            onCopyTrace = { trace ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Error Trace", trace))
                                Toast.makeText(context, "Trace copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // Troubleshooting Tips section at the end of the log drawer
                item {
                    TroubleshootingRecommendations(bot = bot)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LogFilterBadge(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF2B2930))
            .border(
                1.dp,
                if (isSelected) color else ImmersiveBorder.copy(alpha = 0.4f),
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else ImmersiveTextSecondary
        )
    }
}

@Composable
private fun AgentLogItemCard(
    entry: AgentLogEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCopyTrace: (String) -> Unit
) {
    val levelColor = when (entry.level) {
        "ERROR", "CRITICAL" -> AgentStateError
        "WARN" -> AgentStateWorking
        else -> CyanPrimary
    }

    val timeFormatted = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        sdf.format(Date(entry.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(
            1.dp,
            if (entry.level == "ERROR" || entry.level == "CRITICAL") AgentStateError.copy(alpha = 0.4f)
            else ImmersiveBorder.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Timestamp + Stage + Level Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(levelColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.level,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.stage,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ImmersiveTextPrimary
                    )
                }

                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ImmersiveTextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Message
            Text(
                text = entry.message,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = ImmersiveTextPrimary,
                lineHeight = 16.sp
            )

            // Target URL or File if present
            if (!entry.targetUrlOrFile.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TARGET: ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextMuted
                    )
                    Text(
                        text = entry.targetUrlOrFile,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ImmersivePrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Expandable Error Stack Trace section
            if (!entry.errorTrace.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleExpand() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Stack Trace",
                            tint = AgentStateError,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpanded) "Hide Full Stack Trace" else "View Full Stack Trace",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgentStateError
                        )
                    }

                    if (isExpanded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onCopyTrace(entry.errorTrace) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Trace",
                                tint = ImmersiveTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy",
                                fontSize = 10.sp,
                                color = ImmersiveTextSecondary
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0E13))
                                .border(1.dp, ImmersiveBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = entry.errorTrace,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFFB4AB),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TroubleshootingRecommendations(bot: BotEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B1920),
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Troubleshooting",
                    tint = AgentStateWorking,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TROUBLESHOOTING & REMEDIATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgentStateWorking,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1. Verify Cloud VM Network: Ensure ${bot.currentVmHost} has unrestricted outbound egress to required vendor endpoints.",
                fontSize = 11.sp,
                color = ImmersiveTextSecondary,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "2. Session Cookies / 2FA: If task was halted by anti-bot verification, submit the 2FA SMS code or authorization token via the Approvals tab.",
                fontSize = 11.sp,
                color = ImmersiveTextSecondary,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "3. Auto-Restart Agent: Resetting will clear the Chromium session cache and restart task execution from the last valid checkpoint.",
                fontSize = 11.sp,
                color = ImmersiveTextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

/**
 * Builds realistic, structured diagnostic execution and error logs for the specified agent.
 */
private fun generateAgentLogs(
    bot: BotEntity,
    tasks: List<TaskEntity>
): List<AgentLogEntry> {
    val now = System.currentTimeMillis()
    val logs = mutableListOf<AgentLogEntry>()

    // Specific tailored error traces depending on bot role and state
    when (bot.id) {
        "bot_atlas" -> {
            logs.add(
                AgentLogEntry(
                    timestamp = now - 180000,
                    level = "INFO",
                    stage = "VM_INIT",
                    message = "Allocated cloud sandbox on ${bot.currentVmHost} with 4 vCPUs and 8GB RAM.",
                    targetUrlOrFile = "https://app.hubspot.com"
                )
            )
            logs.add(
                AgentLogEntry(
                    timestamp = now - 120000,
                    level = "WARN",
                    stage = "DOM_SELECTOR",
                    message = "HubSpot email composer updated DOM schema. Fallback CSS selector '.email-draft-subject-input' required 2 retries.",
                    targetUrlOrFile = "https://app.hubspot.com/email/drafts/3982"
                )
            )
            logs.add(
                AgentLogEntry(
                    timestamp = now - 60000,
                    level = "ERROR",
                    stage = "GATE_BOUNDARY",
                    message = "Outbound email batch halted by Human-in-the-Loop policy. 33 draft messages parked awaiting commander review.",
                    errorTrace = """com.coworker.agent.security.PolicyViolationException: OutboundEmailThresholdExceeded
    at com.coworker.agent.engine.PolicyEnforcer.assertApproval(PolicyEnforcer.kt:94)
    at com.coworker.agent.sales.SalesOutboundRunner.execute(SalesOutboundRunner.kt:142)
    at com.coworker.agent.engine.AgentRuntime.dispatch(AgentRuntime.kt:61)
    Caused by: SecurityPolicy[ACTION=SEND_EMAIL, BATCH_SIZE=33, MAX_AUTONOMOUS=0]
    -> Action requires human cryptographic signature on mobile device.""",
                    targetUrlOrFile = "https://app.hubspot.com/email/drafts/3982",
                    exitCode = 403
                )
            )
        }
        "bot_lyra" -> {
            logs.add(
                AgentLogEntry(
                    timestamp = now - 240000,
                    level = "INFO",
                    stage = "MCP_CONNECT",
                    message = "Connected to Greenhouse internal ATS via mcp://greenhouse.internal.",
                    targetUrlOrFile = "mcp://greenhouse.internal"
                )
            )
            logs.add(
                AgentLogEntry(
                    timestamp = now - 140000,
                    level = "ERROR",
                    stage = "CHALLENGE_2FA",
                    message = "LinkedIn Recruiter session challenged by device verification. Headless Chrome session intercepted by CAPTCHA gate.",
                    errorTrace = """com.coworker.agent.browser.ChallengeDetectedException: Cloud Browser Encountered SMS Challenge
    at com.coworker.agent.browser.ChromiumDriver.waitForNavigation(ChromiumDriver.kt:218)
    at com.coworker.agent.talent.LinkedInScout.navigateTalentPool(LinkedInScout.kt:88)
    at com.coworker.agent.engine.AgentRuntime.dispatch(AgentRuntime.kt:61)
    Caused by: ChallengeInterceptor: Intercepted Challenge Type 'SMS_2FA' at https://www.linkedin.com/checkpoint/challenge
    -> Waiting for user OTP code on Android Command Center.""",
                    targetUrlOrFile = "https://www.linkedin.com/checkpoint/challenge",
                    exitCode = 429
                )
            )
        }
        "bot_ledger" -> {
            logs.add(
                AgentLogEntry(
                    timestamp = now - 300000,
                    level = "INFO",
                    stage = "PDF_DOWNLOAD",
                    message = "Downloaded AWS August consolidated invoice 'aws_invoice_august_2026.pdf'.",
                    targetUrlOrFile = "https://billing.vendorportal.com/invoices/991"
                )
            )
            logs.add(
                AgentLogEntry(
                    timestamp = now - 90000,
                    level = "ERROR",
                    stage = "FINANCE_LIMIT",
                    message = "Settlement amount $142.50 exceeds default automated expenditure threshold ($50.00). Transaction execution paused.",
                    errorTrace = """com.coworker.finance.SpendThresholdExceededException: Transaction Amount Requires Human Sign-off
    at com.coworker.finance.ExpenseValidator.verifyBudget(ExpenseValidator.kt:53)
    at com.coworker.agent.finance.InvoiceManager.settleBill(InvoiceManager.kt:119)
    Caused by: SpendingLimitExceeded: Amount $142.50 > Autonomous Limit $50.00
    -> Generated ApprovalRequest #2 for commander authorization.""",
                    targetUrlOrFile = "https://billing.vendorportal.com/invoices/991",
                    exitCode = 402
                )
            )
        }
        "bot_spectre" -> {
            logs.add(
                AgentLogEntry(
                    timestamp = now - 200000,
                    level = "INFO",
                    stage = "BROWSER_START",
                    message = "Launched Chromium with DevTools protocol on viewport 1920x1080 @ 30fps.",
                    targetUrlOrFile = "https://staging.acmeshop.internal/checkout"
                )
            )
            logs.add(
                AgentLogEntry(
                    timestamp = now - 80000,
                    level = "WARN",
                    stage = "RACE_CONDITION",
                    message = "Detected UnhandledPromiseRejection: HTTP 409 Conflict during simulated promo code double-click.",
                    errorTrace = """UnhandledPromiseRejection: HTTP 409 Conflict (Race Condition)
    at checkout.bundle.js:842:19
    at async applyPromoCode (promo.ts:114)
    Caused by: MutationObserver fired before previous atomic transaction resolved.
    Network trace: POST /api/checkout/apply-coupon returned status 409.""",
                    targetUrlOrFile = "https://staging.acmeshop.internal/checkout",
                    exitCode = 409
                )
            )
        }
        else -> {
            logs.add(
                AgentLogEntry(
                    timestamp = now - 150000,
                    level = "INFO",
                    stage = "SYS_STATUS",
                    message = "Bot ${bot.name} operational on node ${bot.currentVmHost}. CPU ${bot.cpuUsage}, Memory ${bot.memoryUsage}.",
                    targetUrlOrFile = "cloud-vm-container"
                )
            )
            logs.add(
                AgentLogEntry(
                    timestamp = now - 75000,
                    level = "INFO",
                    stage = "STREAMING",
                    message = "Browser session streaming at 30 FPS. Telemetry synced with mobile client.",
                    targetUrlOrFile = "https://cloud.workspace.internal"
                )
            )
        }
    }

    return logs
}

private fun formatFullLogText(bot: BotEntity, logs: List<AgentLogEntry>): String {
    val sb = StringBuilder()
    sb.append("=== AI AGENT DIAGNOSTIC LOG DUMP ===\n")
    sb.append("Agent Name: ${bot.name} (${bot.role.title})\n")
    sb.append("VM Node: ${bot.currentVmHost}\n")
    sb.append("Current Status: ${bot.status} | Activity: ${bot.activityState.label}\n")
    sb.append("Total Log Entries: ${logs.size}\n\n")

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    logs.forEach { entry ->
        sb.append("[${sdf.format(Date(entry.timestamp))}] [${entry.level}] [${entry.stage}] ${entry.message}\n")
        if (!entry.targetUrlOrFile.isNullOrBlank()) {
            sb.append("  Target: ${entry.targetUrlOrFile}\n")
        }
        if (!entry.errorTrace.isNullOrBlank()) {
            sb.append("  --- STACK TRACE ---\n")
            sb.append(entry.errorTrace)
            sb.append("\n  -------------------\n")
        }
        sb.append("\n")
    }

    return sb.toString()
}
