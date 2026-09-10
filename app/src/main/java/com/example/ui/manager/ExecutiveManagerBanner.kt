package com.example.ui.manager

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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.BotActivityState
import com.example.data.model.BotEntity
import com.example.data.model.BotRole
import com.example.ui.components.BotAvatar

private val DirectorViolet = Color(0xFF9333EA)
private val DirectorDeepPurple = Color(0xFF1E1035)
private val EmeraldSuccess = Color(0xFF10B981)
private val CyanAccent = Color(0xFF00E5FF)
private val TextMuted = Color(0xFF94A3B8)

@Composable
fun ExecutiveManagerBanner(
    managerBot: BotEntity?,
    onOpenManagerHub: (initialTab: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "director_pulse")
    val borderAlpha by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("executive_manager_banner")
            .clickable { onOpenManagerHub(0) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DirectorDeepPurple),
        border = BorderStroke(1.5.dp, DirectorViolet.copy(alpha = borderAlpha))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DirectorViolet.copy(alpha = 0.25f),
                            DirectorDeepPurple,
                            Color(0xFF0D0819)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            // Top Row: Title & Manager Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DirectorViolet.copy(alpha = 0.3f))
                            .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = managerBot?.name ?: "Orion",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = DirectorViolet
                            ) {
                                Text(
                                    text = "COMPANY DIRECTOR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Manager of all bots • Controls routing & complex initiatives",
                            fontSize = 11.sp,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = EmeraldSuccess.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Supervising 6 Bots",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Director Status & Directive
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF130B24),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = DirectorViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Manager Directive & Status:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = managerBot?.currentActionText
                                ?: "Supervising 6 specialist bots • Decomposing complex initiatives & load balancing",
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Quick Manager Actions: Complex Task, Which Bot to Use, Org Hierarchy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onOpenManagerHub(0) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("manager_handle_complex_task_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DirectorViolet),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Complex Tasks",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { onOpenManagerHub(1) },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                        .testTag("manager_which_bot_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DirectorViolet.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(15.dp), tint = DirectorViolet)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Which Bot?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { onOpenManagerHub(2) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("manager_org_chart_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFA78BFA))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Org Chart",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
