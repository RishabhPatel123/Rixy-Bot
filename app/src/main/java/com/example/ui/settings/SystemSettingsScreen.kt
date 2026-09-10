package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.McpServerEntity
import com.example.data.model.SystemSettingsEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoworkerViewModel

@Composable
fun SystemSettingsScreen(
    viewModel: CoworkerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.systemSettings.collectAsState()
    val vmTestState by viewModel.vmTestState.collectAsState()
    val aiTestState by viewModel.aiTestState.collectAsState()
    val mcpServers by viewModel.mcpServers.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Cloud VM", "API Credentials", "Live Mode", "MCP Integrations")

    // Local form state initialized from persistent settings
    var vmProvider by remember { mutableStateOf(settings.vmProvider) }
    var primaryVmHost by remember { mutableStateOf(settings.primaryVmHost) }
    var sshPort by remember { mutableStateOf(settings.sshPort.toString()) }
    var clusterRegion by remember { mutableStateOf(settings.clusterRegion) }
    var vmAuthToken by remember { mutableStateOf(settings.vmAuthToken) }
    var browserSandboxEndpoint by remember { mutableStateOf(settings.browserSandboxEndpoint) }
    var vmCpuCores by remember { mutableIntStateOf(settings.vmCpuCores) }
    var vmRamGb by remember { mutableIntStateOf(settings.vmRamGb) }

    var geminiApiKey by remember { mutableStateOf(settings.geminiApiKeyOverride) }
    var geminiModel by remember { mutableStateOf(settings.geminiModel) }
    var customAiEndpoint by remember { mutableStateOf(settings.customAiEndpoint) }
    var customAiApiKey by remember { mutableStateOf(settings.customAiApiKey) }
    var githubToken by remember { mutableStateOf(settings.githubToken) }
    var stripeApiKey by remember { mutableStateOf(settings.stripeApiKey) }
    var outreachApiKey by remember { mutableStateOf(settings.outreachApiKey) }
    var apmMonitoringToken by remember { mutableStateOf(settings.apmMonitoringToken) }

    var isLiveExecutionEnabled by remember { mutableStateOf(settings.isLiveExecutionEnabled) }
    var useLiveTelemetry by remember { mutableStateOf(settings.useLiveCloudVmTelemetry) }
    var spendLimitUsd by remember { mutableFloatStateOf(settings.maxHourlySpendLimitUsd.toFloat()) }

    var saveNotification by remember { mutableStateOf<String?>(null) }

    // Keep draft state in sync when persistent settings update externally
    LaunchedEffect(settings) {
        vmProvider = settings.vmProvider
        primaryVmHost = settings.primaryVmHost
        sshPort = settings.sshPort.toString()
        clusterRegion = settings.clusterRegion
        vmAuthToken = settings.vmAuthToken
        browserSandboxEndpoint = settings.browserSandboxEndpoint
        vmCpuCores = settings.vmCpuCores
        vmRamGb = settings.vmRamGb
        geminiApiKey = settings.geminiApiKeyOverride
        geminiModel = settings.geminiModel
        customAiEndpoint = settings.customAiEndpoint
        customAiApiKey = settings.customAiApiKey
        githubToken = settings.githubToken
        stripeApiKey = settings.stripeApiKey
        outreachApiKey = settings.outreachApiKey
        apmMonitoringToken = settings.apmMonitoringToken
        isLiveExecutionEnabled = settings.isLiveExecutionEnabled
        useLiveTelemetry = settings.useLiveCloudVmTelemetry
        spendLimitUsd = settings.maxHourlySpendLimitUsd.toFloat()
    }

    val onSaveCurrentSettings = {
        val parsedPort = sshPort.toIntOrNull() ?: 22
        val updated = settings.copy(
            vmProvider = vmProvider,
            primaryVmHost = primaryVmHost.trim(),
            sshPort = parsedPort,
            clusterRegion = clusterRegion.trim(),
            vmAuthToken = vmAuthToken.trim(),
            browserSandboxEndpoint = browserSandboxEndpoint.trim(),
            vmCpuCores = vmCpuCores,
            vmRamGb = vmRamGb,
            geminiApiKeyOverride = geminiApiKey.trim(),
            geminiModel = geminiModel.trim(),
            customAiEndpoint = customAiEndpoint.trim(),
            customAiApiKey = customAiApiKey.trim(),
            githubToken = githubToken.trim(),
            stripeApiKey = stripeApiKey.trim(),
            outreachApiKey = outreachApiKey.trim(),
            apmMonitoringToken = apmMonitoringToken.trim(),
            isLiveExecutionEnabled = isLiveExecutionEnabled,
            useLiveCloudVmTelemetry = useLiveTelemetry,
            maxHourlySpendLimitUsd = spendLimitUsd.toDouble(),
            updatedAt = System.currentTimeMillis()
        )
        viewModel.updateSystemSettings(updated)
        saveNotification = "Settings applied successfully"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ImmersiveSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
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

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ENTERPRISE CONFIGURATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersivePrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (settings.isLiveExecutionEnabled) Color(0xFF10B981).copy(alpha = 0.2f)
                                else Color(0xFFF59E0B).copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (settings.isLiveExecutionEnabled) "LIVE ACTIVE" else "SIMULATION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.isLiveExecutionEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
                Text(
                    text = "Cloud VM & API Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            }

            IconButton(
                onClick = { onSaveCurrentSettings() },
                modifier = Modifier.testTag("save_settings_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save Settings",
                    tint = ImmersivePrimary
                )
            }
        }

        // Notification Banner
        AnimatedVisibility(visible = saveNotification != null) {
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = saveNotification ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "DISMISS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.clickable { saveNotification = null }
                    )
                }
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = ImmersiveSurface,
            contentColor = ImmersivePrimary,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == index) ImmersivePrimary else ImmersiveTextSecondary
                        )
                    }
                )
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: CLOUD VM & INFRASTRUCTURE
                    item {
                        CloudVmSection(
                            provider = vmProvider,
                            onProviderChange = { vmProvider = it },
                            host = primaryVmHost,
                            onHostChange = { primaryVmHost = it },
                            port = sshPort,
                            onPortChange = { sshPort = it },
                            region = clusterRegion,
                            onRegionChange = { clusterRegion = it },
                            token = vmAuthToken,
                            onTokenChange = { vmAuthToken = it },
                            sandbox = browserSandboxEndpoint,
                            onSandboxChange = { browserSandboxEndpoint = it },
                            cores = vmCpuCores,
                            onCoresChange = { vmCpuCores = it },
                            ramGb = vmRamGb,
                            onRamChange = { vmRamGb = it },
                            testState = vmTestState,
                            onTestPing = {
                                viewModel.testCloudVmConnection(
                                    host = primaryVmHost,
                                    port = sshPort.toIntOrNull() ?: 22,
                                    token = vmAuthToken
                                )
                            }
                        )
                    }
                }
                1 -> {
                    // TAB 1: API CREDENTIALS & AI ENGINE
                    item {
                        ApiCredentialsSection(
                            geminiKey = geminiApiKey,
                            onGeminiKeyChange = { geminiApiKey = it },
                            model = geminiModel,
                            onModelChange = { geminiModel = it },
                            customEndpoint = customAiEndpoint,
                            onCustomEndpointChange = { customAiEndpoint = it },
                            customKey = customAiApiKey,
                            onCustomKeyChange = { customAiApiKey = it },
                            githubToken = githubToken,
                            onGithubTokenChange = { githubToken = it },
                            stripeKey = stripeApiKey,
                            onStripeKeyChange = { stripeApiKey = it },
                            outreachKey = outreachApiKey,
                            onOutreachKeyChange = { outreachApiKey = it },
                            apmToken = apmMonitoringToken,
                            onApmTokenChange = { apmMonitoringToken = it },
                            testState = aiTestState,
                            onTestAi = {
                                viewModel.testGeminiAiCredentials(
                                    apiKey = geminiApiKey,
                                    model = geminiModel
                                )
                            }
                        )
                    }
                }
                2 -> {
                    // TAB 2: LIVE MODE VS MOCK DATA
                    item {
                        LiveModeSection(
                            isLiveEnabled = isLiveExecutionEnabled,
                            onLiveToggle = {
                                isLiveExecutionEnabled = it
                                viewModel.toggleLiveExecutionMode(it)
                            },
                            useLiveTelemetry = useLiveTelemetry,
                            onTelemetryToggle = { useLiveTelemetry = it },
                            spendLimit = spendLimitUsd,
                            onSpendLimitChange = { spendLimitUsd = it },
                            currentVmStatus = settings.lastVmStatus,
                            currentAiStatus = settings.lastAiStatus,
                            vmPingMs = settings.lastVmPingMs
                        )
                    }
                }
                3 -> {
                    // TAB 3: MCP INTEGRATIONS & SERVERS
                    item {
                        McpIntegrationsSection(
                            servers = mcpServers,
                            onToggle = { viewModel.toggleMcpServerStatus(it) }
                        )
                    }
                }
            }

            // Bottom Action Bar
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetSettingsToDefaults()
                            saveNotification = "Settings reset to defaults"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_settings_button"),
                        border = BorderStroke(1.dp, ImmersiveBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextSecondary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Defaults", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onSaveCurrentSettings() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_settings_bottom_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersivePrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply & Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 1: CLOUD VM & INFRASTRUCTURE
// -------------------------------------------------------------
@Composable
fun CloudVmSection(
    provider: String,
    onProviderChange: (String) -> Unit,
    host: String,
    onHostChange: (String) -> Unit,
    port: String,
    onPortChange: (String) -> Unit,
    region: String,
    onRegionChange: (String) -> Unit,
    token: String,
    onTokenChange: (String) -> Unit,
    sandbox: String,
    onSandboxChange: (String) -> Unit,
    cores: Int,
    onCoresChange: (Int) -> Unit,
    ramGb: Int,
    onRamChange: (Int) -> Unit,
    testState: com.example.ui.viewmodel.ConnectionTestUiState,
    onTestPing: () -> Unit
) {
    var isTokenVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Info Banner
        SettingsCard(
            title = "Cloud VM Cluster Telemetry",
            subtitle = "Direct your AI coworkers to execute tasks on real remote Linux VMs, headless browser sandboxes, or private VPC nodes.",
            icon = Icons.Default.Computer
        ) {
            // Provider Choice Chips
            Text(
                text = "CLOUD HOSTING PROVIDER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ImmersivePrimary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val providers = listOf(
                "GCP Compute Engine",
                "AWS EC2",
                "Microsoft Azure",
                "DigitalOcean",
                "Private Bare-Metal / VPC"
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                providers.forEach { p ->
                    val isSelected = provider.equals(p, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ImmersivePrimary.copy(alpha = 0.15f) else ImmersiveBackground)
                            .clickable { onProviderChange(p) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ImmersivePrimary else ImmersiveBorder)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = p,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ImmersiveTextPrimary else ImmersiveTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // VM Host / IP
            OutlinedTextField(
                value = host,
                onValueChange = onHostChange,
                label = { Text("Primary VM Host IP / Hostname") },
                placeholder = { Text("e.g. 10.142.0.4 or orchestrator.mycloud.internal") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vm_host_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Port & Region
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = port,
                    onValueChange = onPortChange,
                    label = { Text("SSH / Port") },
                    placeholder = { Text("22") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vm_port_input"),
                    colors = outlinedTextFieldColors(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = region,
                    onValueChange = onRegionChange,
                    label = { Text("Region / Zone") },
                    placeholder = { Text("us-central1-a") },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("vm_region_input"),
                    colors = outlinedTextFieldColors(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Auth Token / SSH Key
            OutlinedTextField(
                value = token,
                onValueChange = onTokenChange,
                label = { Text("VM Bearer Token / SSH Key Fingerprint") },
                placeholder = { Text("Optional authorization secret") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vm_token_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true,
                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                        Icon(
                            imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle visibility",
                            tint = ImmersiveTextSecondary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Headless Browser Sandbox Endpoint
            OutlinedTextField(
                value = sandbox,
                onValueChange = onSandboxChange,
                label = { Text("Browser Sandbox Node Endpoint (Playwright / Puppeteer)") },
                placeholder = { Text("ws://10.142.0.12:3000 or http://sandbox.local") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vm_sandbox_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Hardware Allocation Limits
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("VM Hardware Allocation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                    Text("Assigned to swarm workloads", fontSize = 11.sp, color = ImmersiveTextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ImmersiveSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${cores} vCPUs", fontSize = 11.sp, color = ImmersivePrimary, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ImmersiveSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${ramGb} GB RAM", fontSize = 11.sp, color = ImmersiveSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test Ping Button & Result
            Button(
                onClick = onTestPing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_vm_ping_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImmersiveSurfaceVariant,
                    contentColor = ImmersivePrimary
                ),
                border = BorderStroke(1.dp, ImmersivePrimary.copy(alpha = 0.5f))
            ) {
                if (testState.isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ImmersivePrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pinging VM Host & Checking Ports...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ping & Verify Cloud VM Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Test Result Box
            AnimatedVisibility(visible = testState.message.isNotEmpty()) {
                val isSuccess = testState.isSuccess == true
                Surface(
                    color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isSuccess) "VM Status: ONLINE (${testState.latencyMs}ms)" else "VM Reachability Warning",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Text(
                                text = testState.message,
                                fontSize = 11.sp,
                                color = ImmersiveTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 2: API CREDENTIALS & AI ENGINE
// -------------------------------------------------------------
@Composable
fun ApiCredentialsSection(
    geminiKey: String,
    onGeminiKeyChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    customEndpoint: String,
    onCustomEndpointChange: (String) -> Unit,
    customKey: String,
    onCustomKeyChange: (String) -> Unit,
    githubToken: String,
    onGithubTokenChange: (String) -> Unit,
    stripeKey: String,
    onStripeKeyChange: (String) -> Unit,
    outreachKey: String,
    onOutreachKeyChange: (String) -> Unit,
    apmToken: String,
    onApmTokenChange: (String) -> Unit,
    testState: com.example.ui.viewmodel.ConnectionTestUiState,
    onTestAi: () -> Unit
) {
    var isGeminiKeyVisible by remember { mutableStateOf(false) }
    var isGithubKeyVisible by remember { mutableStateOf(false) }
    var isStripeKeyVisible by remember { mutableStateOf(false) }
    var isOutreachKeyVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // AI Engine Configuration Card
        SettingsCard(
            title = "Google Gemini AI Engine",
            subtitle = "Provides live neural reasoning for your AI Coworkers, replacing simulated response templates with real-time generative intelligence.",
            icon = Icons.Default.Key
        ) {
            // Environment Key Status
            val buildConfigHasKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ImmersiveSurfaceVariant)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (buildConfigHasKey || geminiKey.isNotBlank()) Color(0xFF10B981) else Color(0xFFF59E0B))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (geminiKey.isNotBlank()) "Using UI Custom Gemini Key Override"
                        else if (buildConfigHasKey) "Using Environment / Secrets Panel Key"
                        else "No API Key Detected (Running in Offline Mode)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                    Text(
                        text = "You can enter your API key below to configure or override it directly.",
                        fontSize = 10.sp,
                        color = ImmersiveTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gemini API Key Input
            OutlinedTextField(
                value = geminiKey,
                onValueChange = onGeminiKeyChange,
                label = { Text("Gemini API Key") },
                placeholder = { Text(if (buildConfigHasKey) "•••••••••••••••• (Inherited from Secrets)" else "AIzaSy...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_api_key_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true,
                visualTransformation = if (isGeminiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isGeminiKeyVisible = !isGeminiKeyVisible }) {
                        Icon(
                            imageVector = if (isGeminiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle visibility",
                            tint = ImmersiveTextSecondary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Model Selection
            Text(
                text = "ACTIVE GEMINI MODEL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ImmersivePrimary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val models = listOf(
                "gemini-3.5-flash" to "Fast, balanced multimodal reasoning (Recommended)",
                "gemini-3.1-pro-preview" to "Advanced reasoning for complex coding and math",
                "gemini-3.1-flash-lite-preview" to "Ultra-low latency for quick routine tasks"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                models.forEach { (m, desc) ->
                    val isSelected = model == m
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ImmersivePrimary.copy(alpha = 0.15f) else ImmersiveBackground)
                            .clickable { onModelChange(m) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ImmersivePrimary else ImmersiveBorder)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = m,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ImmersiveTextPrimary else ImmersiveTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = desc,
                                fontSize = 10.sp,
                                color = ImmersiveTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Test AI Credentials Button
            Button(
                onClick = onTestAi,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_ai_credentials_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImmersiveSurfaceVariant,
                    contentColor = ImmersiveSecondary
                ),
                border = BorderStroke(1.dp, ImmersiveSecondary.copy(alpha = 0.5f))
            ) {
                if (testState.isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ImmersiveSecondary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Testing Neural Endpoint Connection...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Gemini API Credentials", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Test Result Box
            AnimatedVisibility(visible = testState.message.isNotEmpty()) {
                val isSuccess = testState.isSuccess == true
                Surface(
                    color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isSuccess) "AI Engine Verified (${testState.latencyMs}ms)" else "AI Authentication Failed",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Text(
                                text = testState.message,
                                fontSize = 11.sp,
                                color = ImmersiveTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Specialist Bot API Credentials Card
        SettingsCard(
            title = "Specialist Bot Tool Credentials",
            subtitle = "Provide tokens for specialized tasks so bots execute live operations instead of simulated actions.",
            icon = Icons.Default.Security
        ) {
            // Turing Developer Bot - GitHub PAT
            OutlinedTextField(
                value = githubToken,
                onValueChange = onGithubTokenChange,
                label = { Text("GitHub PAT (Turing Developer Bot)") },
                placeholder = { Text("ghp_••••••••••••••••") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_token_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true,
                visualTransformation = if (isGithubKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isGithubKeyVisible = !isGithubKeyVisible }) {
                        Icon(
                            imageVector = if (isGithubKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = ImmersiveTextSecondary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ledger FinOps Bot - Stripe Key
            OutlinedTextField(
                value = stripeKey,
                onValueChange = onStripeKeyChange,
                label = { Text("Stripe Secret Key (Ledger FinOps Bot)") },
                placeholder = { Text("sk_live_••••••••••••••••") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stripe_key_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true,
                visualTransformation = if (isStripeKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isStripeKeyVisible = !isStripeKeyVisible }) {
                        Icon(
                            imageVector = if (isStripeKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = ImmersiveTextSecondary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Atlas Sales Bot - Outreach / CRM Key
            OutlinedTextField(
                value = outreachKey,
                onValueChange = onOutreachKeyChange,
                label = { Text("Outreach / SendGrid Key (Atlas Sales Bot)") },
                placeholder = { Text("SG.••••••••••••••••") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("outreach_key_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true,
                visualTransformation = if (isOutreachKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isOutreachKeyVisible = !isOutreachKeyVisible }) {
                        Icon(
                            imageVector = if (isOutreachKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = ImmersiveTextSecondary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Nova DevOps Bot - Datadog / APM Token
            OutlinedTextField(
                value = apmToken,
                onValueChange = onApmTokenChange,
                label = { Text("Datadog / APM Key (Nova DevOps Bot)") },
                placeholder = { Text("ddp_••••••••••••••••") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apm_token_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true
            )
        }

        // Custom / Self-Hosted LLM Card
        SettingsCard(
            title = "Self-Hosted / Local LLM (Optional)",
            subtitle = "Connect Ollama, vLLM, or private OpenAI-compatible endpoints on your Cloud VM.",
            icon = Icons.Default.Storage
        ) {
            OutlinedTextField(
                value = customEndpoint,
                onValueChange = onCustomEndpointChange,
                label = { Text("Local LLM Base URL") },
                placeholder = { Text("http://10.142.0.4:11434/v1") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_ai_endpoint_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customKey,
                onValueChange = onCustomKeyChange,
                label = { Text("Custom Auth Header / API Key") },
                placeholder = { Text("Bearer token if required") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_ai_key_input"),
                colors = outlinedTextFieldColors(),
                singleLine = true
            )
        }
    }
}

// -------------------------------------------------------------
// SECTION 3: LIVE EXECUTION MODE (REPLACE MOCK DATA)
// -------------------------------------------------------------
@Composable
fun LiveModeSection(
    isLiveEnabled: Boolean,
    onLiveToggle: (Boolean) -> Unit,
    useLiveTelemetry: Boolean,
    onTelemetryToggle: (Boolean) -> Unit,
    spendLimit: Float,
    onSpendLimitChange: (Float) -> Unit,
    currentVmStatus: String,
    currentAiStatus: String,
    vmPingMs: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Master Live Mode Card
        Surface(
            color = if (isLiveEnabled) ImmersivePrimary.copy(alpha = 0.08f) else ImmersiveSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, if (isLiveEnabled) ImmersivePrimary else ImmersiveBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Execution Mode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "Replace mock data with real cloud VM calls & Gemini responses",
                            fontSize = 12.sp,
                            color = ImmersiveTextSecondary
                        )
                    }

                    Switch(
                        checked = isLiveEnabled,
                        onCheckedChange = onLiveToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ImmersivePrimary,
                            checkedTrackColor = ImmersivePrimary.copy(alpha = 0.3f),
                            uncheckedThumbColor = ImmersiveTextSecondary,
                            uncheckedTrackColor = ImmersiveBorder
                        ),
                        modifier = Modifier.testTag("live_execution_master_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Explanation Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLiveEnabled) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLiveEnabled) Icons.Default.Speed else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isLiveEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isLiveEnabled)
                                "ACTIVE: Coworkers communicate using real Gemini API neural reasoning, query your configured Cloud VM, and execute authentic workflows."
                            else
                                "SIMULATION: Running offline safe mode using local sample flows. No external API credits or cloud compute consumed.",
                            fontSize = 12.sp,
                            color = if (isLiveEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Live Telemetry Stream Switch
        SettingsCard(
            title = "Live Cloud Telemetry & Metrics",
            subtitle = "Stream actual CPU and memory statistics from remote VM host instead of synthetic loads.",
            icon = Icons.Default.Memory
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Continuous VM Health Polling", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                    Text("Polls VM every 10s via SSH/HTTP socket", fontSize = 11.sp, color = ImmersiveTextSecondary)
                }
                Switch(
                    checked = useLiveTelemetry,
                    onCheckedChange = onTelemetryToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ImmersivePrimary,
                        checkedTrackColor = ImmersivePrimary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("telemetry_stream_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Autonomous Spend Cap Slider
            Text(
                text = "MAXIMUM HOURLY CLOUD SPEND CAP: $${spendLimit.toInt()} USD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ImmersivePrimary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = spendLimit,
                onValueChange = onSpendLimitChange,
                valueRange = 10f..500f,
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = ImmersivePrimary,
                    activeTrackColor = ImmersivePrimary,
                    inactiveTrackColor = ImmersiveBorder
                ),
                modifier = Modifier.testTag("spend_limit_slider")
            )
            Text(
                text = "If bot operations approach this threshold, financial actions are automatically halted until human authorization is granted.",
                fontSize = 11.sp,
                color = ImmersiveTextSecondary
            )
        }

        // Live Status Diagnostics Card
        SettingsCard(
            title = "Infrastructure Connection Status",
            subtitle = "Live operational indicators for external cluster connections.",
            icon = Icons.Default.Speed
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusPill(
                    title = "Cloud VM",
                    status = currentVmStatus,
                    detail = if (vmPingMs > 0) "${vmPingMs}ms" else "Ready",
                    isSuccess = currentVmStatus == "CONNECTED",
                    modifier = Modifier.weight(1f)
                )

                StatusPill(
                    title = "Gemini Core",
                    status = currentAiStatus,
                    detail = "API Ready",
                    isSuccess = currentAiStatus == "VERIFIED" || currentAiStatus == "READY",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 4: MCP INTEGRATIONS & SERVERS
// -------------------------------------------------------------
@Composable
fun McpIntegrationsSection(
    servers: List<McpServerEntity>,
    onToggle: (McpServerEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Model Context Protocol (MCP) Connectors",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ImmersiveTextPrimary
        )
        Text(
            text = "Enable or disable corporate databases, source code repositories, and messaging backends.",
            fontSize = 12.sp,
            color = ImmersiveTextSecondary
        )

        servers.forEach { server ->
            val icon = when {
                server.serverName.contains("GitHub", ignoreCase = true) -> Icons.Default.Code
                server.serverName.contains("Slack", ignoreCase = true) -> Icons.Default.Message
                server.serverName.contains("AWS", ignoreCase = true) -> Icons.Default.Cloud
                server.serverName.contains("Postgres", ignoreCase = true) -> Icons.Default.Storage
                else -> Icons.Default.Build
            }
            IntegrationCard(
                server = server,
                icon = icon,
                onToggle = { onToggle(server) }
            )
        }
    }
}

// -------------------------------------------------------------
// REUSABLE HELPER COMPONENTS
// -------------------------------------------------------------
@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        color = ImmersiveSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ImmersiveBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ImmersiveSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                    Text(text = subtitle, fontSize = 11.sp, color = ImmersiveTextSecondary, lineHeight = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun StatusPill(
    title: String,
    status: String,
    detail: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ImmersiveBackground,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSuccess) Color(0xFF10B981).copy(alpha = 0.5f) else ImmersiveBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = ImmersiveTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
            )
            Text(text = detail, fontSize = 10.sp, color = ImmersiveTextSecondary)
        }
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ImmersivePrimary,
    unfocusedBorderColor = ImmersiveBorder,
    focusedLabelColor = ImmersivePrimary,
    unfocusedLabelColor = ImmersiveTextSecondary,
    cursorColor = ImmersivePrimary,
    focusedTextColor = ImmersiveTextPrimary,
    unfocusedTextColor = ImmersiveTextPrimary,
    focusedContainerColor = ImmersiveBackground,
    unfocusedContainerColor = ImmersiveBackground
)
