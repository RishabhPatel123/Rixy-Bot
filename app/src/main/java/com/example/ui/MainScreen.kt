package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.approvals.ApprovalQueueScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.routines.RoutinesMcpScreen
import com.example.ui.skills.TeachTaskScreen
import com.example.ui.swarms.SwarmWorkspaceScreen
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ImmersiveAlertCoral
import com.example.ui.theme.ImmersiveAlertOnCoral
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveContainer
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.viewmodel.CoworkerViewModel

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Command", Icons.Default.Dashboard, "tab_dashboard"),
    SWARMS("Swarms", Icons.Default.Group, "tab_swarms"),
    APPROVALS("Approvals", Icons.Default.Security, "tab_approvals"),
    SKILLS("Skills", Icons.Default.Psychology, "tab_skills"),
    ROUTINES("Routines", Icons.Default.Extension, "tab_routines")
}

@Composable
fun MainScreen(
    viewModel: CoworkerViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            val topBorderColor = ImmersiveBorder.copy(alpha = 0.25f)
            NavigationBar(
                containerColor = ImmersiveSurfaceVariant,
                contentColor = ImmersiveTextPrimary,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .drawBehind {
                        drawLine(
                            color = topBorderColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .testTag("bottom_nav_bar")
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            if (tab == NavigationTab.APPROVALS && pendingApprovals.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = ImmersiveAlertCoral,
                                            contentColor = ImmersiveAlertOnCoral
                                        ) {
                                            Text(
                                                text = "${pendingApprovals.size}",
                                                color = ImmersiveAlertOnCoral,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ImmersivePrimary,
                            selectedTextColor = ImmersiveTextPrimary,
                            indicatorColor = ImmersiveContainer,
                            unselectedIconColor = ImmersiveTextMuted,
                            unselectedTextColor = ImmersiveTextMuted
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { paddingValues ->
        when (currentTab) {
            NavigationTab.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToApprovals = { currentTab = NavigationTab.APPROVALS },
                onNavigateToSwarms = { swarmId ->
                    viewModel.selectSwarm(swarmId)
                    currentTab = NavigationTab.SWARMS
                },
                modifier = Modifier.padding(paddingValues)
            )
            NavigationTab.SWARMS -> SwarmWorkspaceScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
            NavigationTab.APPROVALS -> ApprovalQueueScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
            NavigationTab.SKILLS -> TeachTaskScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
            NavigationTab.ROUTINES -> RoutinesMcpScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
