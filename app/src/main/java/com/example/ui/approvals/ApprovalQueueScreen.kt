package com.example.ui.approvals

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApprovalRequestEntity
import com.example.data.model.ApprovalType
import com.example.data.model.BotActivityState
import com.example.data.model.BotEntity
import com.example.ui.components.BotActivityBadge
import com.example.ui.components.BotAvatar
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CrimsonAlert
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
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveContainer
import com.example.ui.theme.ImmersiveOnPrimary
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.viewmodel.CoworkerViewModel

@Composable
fun ApprovalQueueScreen(
    viewModel: CoworkerViewModel,
    modifier: Modifier = Modifier
) {
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()
    val allApprovals by viewModel.allApprovals.collectAsState()
    val bots by viewModel.bots.collectAsState()
    val resolvedApprovals = allApprovals.filter { it.isApproved != null }

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedApprovalForAction by remember { mutableStateOf<ApprovalRequestEntity?>(null) }
    var showRejectDialog by remember { mutableStateOf(false) }

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
                        .background(ImmersiveAlertCoral),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Human in the Loop",
                        tint = ImmersiveAlertOnCoral,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "HUMAN-IN-THE-LOOP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveAlertCoral,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Safety Sign-off Queue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Bots work autonomously 24/7 in cloud VMs, but halt automatically before sending emails, spending money, or passing 2FA checkpoints.",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Pending (${pendingApprovals.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) ImmersivePrimary else ImmersiveTextMuted
                            )
                            if (pendingApprovals.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ImmersiveAlertCoral)
                                )
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Audit History (${resolvedApprovals.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) ImmersivePrimary else ImmersiveTextMuted
                        )
                    }
                )
            }
        }

        // List
        val listToShow = if (selectedTab == 0) pendingApprovals else resolvedApprovals

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (listToShow.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Safe",
                                tint = ImmersiveActiveMint,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedTab == 0) "All Clear - No Parked Tasks" else "No audit entries",
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedTab == 0) "Bots are operating safely within defined boundaries." else "Resolved actions will be logged here.",
                                fontSize = 12.sp,
                                color = ImmersiveTextMuted
                            )
                        }
                    }
                }
            } else {
                items(listToShow) { approval ->
                    val bot = bots.find { it.id == approval.botId }
                    ApprovalCard(
                        approval = approval,
                        bot = bot,
                        onApprove = {
                            if (approval.requiresInput) {
                                selectedApprovalForAction = approval
                            } else {
                                viewModel.resolveApproval(approval.id, true)
                            }
                        },
                        onReject = {
                            selectedApprovalForAction = approval
                            showRejectDialog = true
                        }
                    )
                }
            }
        }
    }

    // Input Dialog (e.g. for 2FA SMS Code injection into cloud browser)
    if (selectedApprovalForAction != null && !showRejectDialog) {
        val target = selectedApprovalForAction!!
        var inputCode by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedApprovalForAction = null },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Bypass 2FA on Cloud Browser",
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "The persistent cloud VM is parked at the 2FA login screen. Enter the 6-digit SMS verification code to allow ${target.botName} to proceed.",
                        fontSize = 12.sp,
                        color = ImmersiveTextSecondary
                    )

                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it },
                        label = { Text("2FA Verification Code") },
                        placeholder = { Text("e.g. 592810") },
                        modifier = Modifier.fillMaxWidth().testTag("two_factor_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resolveApproval(target.id, true, userInput = inputCode)
                        selectedApprovalForAction = null
                    },
                    enabled = inputCode.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersivePrimary,
                        contentColor = ImmersiveOnPrimary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("submit_two_factor_button")
                ) {
                    Text("Inject into Cloud VM", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedApprovalForAction = null }) {
                    Text("Cancel", color = ImmersiveTextMuted)
                }
            }
        )
    }

    // Rejection Dialog with feedback reason
    if (showRejectDialog && selectedApprovalForAction != null) {
        val target = selectedApprovalForAction!!
        var reasonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showRejectDialog = false
                selectedApprovalForAction = null
            },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Halt Task & Reject Action",
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Rejecting this step will instruct ${target.botName} to abort the action and stop execution in the cloud VM.",
                        fontSize = 12.sp,
                        color = ImmersiveTextSecondary
                    )

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason for Rejection (sent to bot)") },
                        placeholder = { Text("e.g. Too expensive / change email wording") },
                        modifier = Modifier.fillMaxWidth().testTag("rejection_reason_input"),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resolveApproval(target.id, false, rejectionReason = reasonText)
                        showRejectDialog = false
                        selectedApprovalForAction = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersiveAlertCoral,
                        contentColor = ImmersiveAlertOnCoral
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("submit_rejection_button")
                ) {
                    Text("Confirm Rejection", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    selectedApprovalForAction = null
                }) {
                    Text("Back", color = ImmersiveTextMuted)
                }
            }
        )
    }
}

@Composable
fun ApprovalCard(
    approval: ApprovalRequestEntity,
    bot: BotEntity? = null,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPending = approval.isApproved == null
    val typeIcon = when (approval.type) {
        ApprovalType.SEND_EMAIL -> Icons.Default.Email
        ApprovalType.SPEND_MONEY -> Icons.Default.AttachMoney
        ApprovalType.BYPASS_2FA -> Icons.Default.Key
        else -> Icons.Default.Warning
    }

    val riskColor = when (approval.riskLevel) {
        "CRITICAL" -> ImmersiveAlertCoral
        "HIGH" -> Color(0xFFFFB4A9)
        "MEDIUM" -> Color(0xFFF59E0B)
        else -> ImmersivePrimary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("approval_card_${approval.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(
            1.dp,
            if (isPending) riskColor.copy(alpha = 0.5f) else ImmersiveBorder.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
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
                            size = 36,
                            colorHex = bot.colorHex,
                            showStatusDot = true,
                            isWorking = bot.status == "WORKING",
                            activityState = bot.activityState
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(riskColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = approval.type.name,
                                tint = riskColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${approval.botName} (${approval.botRole.title})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersivePrimary
                        )
                        Text(
                            text = approval.type.name.replace("_", " "),
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
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = riskColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, riskColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${approval.riskLevel} RISK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = approval.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = approval.details,
                style = MaterialTheme.typography.bodySmall,
                color = ImmersiveTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Payload / Preview snapshot card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF141218),
                border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ACTION PAYLOAD & PREVIEW",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = approval.previewPayload,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFCAC4D0),
                        lineHeight = 15.sp
                    )
                }
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_approval_${approval.id}"),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, ImmersiveAlertCoral.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveAlertCoral)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject Step", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("approve_approval_${approval.id}"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (approval.requiresInput) ImmersivePrimary else ImmersiveActiveMint,
                            contentColor = if (approval.requiresInput) ImmersiveOnPrimary else Color(0xFF003816)
                        )
                    ) {
                        Icon(
                            imageVector = if (approval.requiresInput) Icons.Default.Key else Icons.Default.Check,
                            contentDescription = "Approve",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (approval.requiresInput) "Enter 2FA Code" else "Approve",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val approved = approval.isApproved == true
                    Icon(
                        imageVector = if (approved) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = "Status",
                        tint = if (approved) ImmersiveActiveMint else ImmersiveAlertCoral,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (approved) "Approved by Human Lead" else "Rejected: ${approval.rejectionReason ?: "Halted"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (approved) ImmersiveActiveMint else ImmersiveAlertCoral
                    )
                }
            }
        }
    }
}
