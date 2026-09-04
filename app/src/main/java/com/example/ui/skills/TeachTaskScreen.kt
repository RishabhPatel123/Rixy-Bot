package com.example.ui.skills

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.SkillEntity
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
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.viewmodel.CoworkerViewModel

@Composable
fun TeachTaskScreen(
    viewModel: CoworkerViewModel,
    modifier: Modifier = Modifier
) {
    val skills by viewModel.skills.collectAsState()

    var isRecordingActive by remember { mutableStateOf(false) }
    var recordingToolName by remember { mutableStateOf("") }
    var recordingSkillTitle by remember { mutableStateOf("") }
    var recordingDescription by remember { mutableStateOf("") }
    val capturedSteps = remember { mutableStateListOf<String>() }
    var showStartRecordDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Teach a Task",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TEACH A TASK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersivePrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Visual Screen Recording & Skills",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                    }
                }

                if (!isRecordingActive) {
                    Button(
                        onClick = { showStartRecordDialog = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersiveAlertCoral,
                            contentColor = ImmersiveAlertOnCoral
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("record_skill_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Record",
                            tint = ImmersiveAlertOnCoral,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Teach Task", color = ImmersiveAlertOnCoral, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "If a bot doesn't know how to navigate a custom vendor portal or CRM, execute the workflow once while the bot watches. It learns the visual sequence as a repeatable Skill.",
                fontSize = 12.sp,
                color = ImmersiveTextSecondary,
                lineHeight = 16.sp
            )
        }

        // Active Screen Recording Session Banner (When Teaching Mode is active)
        AnimatedVisibility(visible = isRecordingActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveAlertBg),
                border = BorderStroke(1.dp, ImmersiveAlertCoral.copy(alpha = 0.7f))
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
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ImmersiveAlertCoral)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SCREEN RECORDING IN PROGRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveAlertCoral,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = "Target: $recordingToolName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ImmersiveAlertCoral
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The AI Coworker is observing your clicks, keystrokes, and DOM transitions to synthesize an automated macro.",
                        fontSize = 12.sp,
                        color = Color(0xFFFFDAD6)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Captured visual sequence list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141218))
                            .border(1.dp, ImmersiveBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CAPTURED ACTIONS (${capturedSteps.size})",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ImmersivePrimary
                        )
                        capturedSteps.forEachIndexed { idx, step ->
                            Text(
                                text = "${idx + 1}. $step",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val actionNumber = capturedSteps.size + 1
                                val sampleActions = listOf(
                                    "Click [data-testid='export-csv'] at (1040, 280)",
                                    "Type 'status:active' into filter search bar",
                                    "Wait for DOM element '.download-ready' to render",
                                    "Save blob to persistent cloud storage /invoices"
                                )
                                capturedSteps.add(sampleActions[(actionNumber - 1) % sampleActions.size])
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ImmersiveContainer,
                                contentColor = ImmersivePrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Mouse, contentDescription = "Simulate Action", modifier = Modifier.size(16.dp), tint = ImmersivePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Click", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val stepsText = capturedSteps.joinToString("\n")
                                viewModel.recordNewSkill(
                                    recordingSkillTitle,
                                    recordingToolName,
                                    recordingDescription,
                                    stepsText
                                )
                                isRecordingActive = false
                                capturedSteps.clear()
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ImmersiveActiveMint,
                                contentColor = Color(0xFF003816)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Finish", modifier = Modifier.size(16.dp), tint = Color(0xFF003816))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Skill", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Saved Skills List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Repeatable Learned Skills",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                    Text(
                        text = "${skills.size} Visual Macros",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ImmersivePrimary
                    )
                }
            }

            items(skills) { skill ->
                SkillCard(skill = skill)
            }
        }
    }

    // Dialog to start recording a new workflow
    if (showStartRecordDialog) {
        AlertDialog(
            onDismissRequest = { showStartRecordDialog = false },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Teach Bot via Screen Recording",
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Specify the tool or portal you will be navigating. The bot will watch your session, learn the visual coordinates, and compile it into a reusable macro.",
                        fontSize = 12.sp,
                        color = ImmersiveTextSecondary
                    )

                    OutlinedTextField(
                        value = recordingSkillTitle,
                        onValueChange = { recordingSkillTitle = it },
                        label = { Text("Skill Name") },
                        placeholder = { Text("e.g. Stripe Tax Exemption Downloader") },
                        modifier = Modifier.fillMaxWidth().testTag("skill_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = recordingToolName,
                        onValueChange = { recordingToolName = it },
                        label = { Text("Target Application / URL") },
                        placeholder = { Text("e.g. billing.vendorportal.com") },
                        modifier = Modifier.fillMaxWidth().testTag("skill_target_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = recordingDescription,
                        onValueChange = { recordingDescription = it },
                        label = { Text("Expected Output Description") },
                        placeholder = { Text("e.g. Extracts quarterly statement and downloads invoice PDF") },
                        modifier = Modifier.fillMaxWidth().testTag("skill_desc_input"),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRecordingActive = true
                        capturedSteps.clear()
                        capturedSteps.add("1. Launch browser to $recordingToolName")
                        capturedSteps.add("2. Focus primary workspace iframe")
                        showStartRecordDialog = false
                    },
                    enabled = recordingSkillTitle.isNotBlank() && recordingToolName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersiveAlertCoral,
                        contentColor = ImmersiveAlertOnCoral
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("start_recording_confirm")
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Record",
                        tint = ImmersiveAlertOnCoral,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Recording", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartRecordDialog = false }) {
                    Text("Cancel", color = ImmersiveTextMuted)
                }
            }
        )
    }
}

@Composable
fun SkillCard(
    skill: SkillEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("skill_card_${skill.id}"),
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
                Text(
                    text = skill.targetAppOrTool,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersivePrimary
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF2B2930)
                ) {
                    Text(
                        text = "Executed ${skill.timesExecuted} times",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = ImmersiveTextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = skill.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = ImmersiveTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Recorded visual sequence box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF141218),
                border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "LEARNED VISUAL SEQUENCE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = skill.recordedStepsJson,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFCAC4D0),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
