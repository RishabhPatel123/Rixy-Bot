package com.example.ui.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.NetworkMonitorService
import com.example.ui.theme.*

@Composable
fun NetworkMonitorOverlay(
    modifier: Modifier = Modifier
) {
    val service = remember { NetworkMonitorService.getInstance() }
    val isConnecting by service.isConnecting.collectAsState()
    val logs by service.logs.collectAsState()
    
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { expanded = !expanded },
                color = if (isConnecting) ImmersiveActiveMint.copy(alpha = 0.2f) else DarkSurfaceVariant,
                border = BorderStroke(1.dp, if (isConnecting) ImmersiveActiveMint.copy(alpha = 0.5f) else ImmersiveBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isConnecting) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Network Status",
                        tint = if (isConnecting) ImmersiveActiveMint else ImmersiveTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isConnecting) "NETWORK ACTIVE" else "NETWORK IDLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnecting) ImmersiveActiveMint else ImmersiveTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            // Expanded Logs View
            AnimatedVisibility(visible = expanded) {
                Surface(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF141218),
                    border = BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    if (logs.isEmpty()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                "No network activity yet.",
                                color = ImmersiveTextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(logs) { log ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "[${log.formattedTime}]",
                                        color = ImmersivePrimary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = log.message,
                                        color = Color(0xFFCAC4D0),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
