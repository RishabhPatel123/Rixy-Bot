package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.approvals.ApprovalQueueScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.routines.RoutinesMcpScreen
import com.example.ui.settings.SystemSettingsScreen
import com.example.ui.swarms.SwarmWorkspaceScreen
import com.example.ui.viewmodel.CoworkerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel: CoworkerViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Approvals") },
                    label = { Text("Approvals") },
                    selected = currentRoute == "approvals",
                    onClick = { navController.navigate("approvals") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Swarms") },
                    label = { Text("Swarms") },
                    selected = currentRoute == "swarms",
                    onClick = { navController.navigate("swarms") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Build, contentDescription = "Routines") },
                    label = { Text("Routines") },
                    selected = currentRoute == "routines",
                    onClick = { navController.navigate("routines") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToApprovals = { navController.navigate("approvals") },
                    onNavigateToSwarms = { swarmId -> 
                        viewModel.selectSwarm(swarmId)
                        navController.navigate("swarms") 
                    },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("approvals") {
                ApprovalQueueScreen(viewModel = viewModel)
            }
            composable("swarms") {
                SwarmWorkspaceScreen(viewModel = viewModel)
            }
            composable("routines") {
                RoutinesMcpScreen(viewModel = viewModel)
            }
            composable("settings") {
                SystemSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
