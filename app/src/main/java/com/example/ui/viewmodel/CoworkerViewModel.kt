package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ApprovalRequestEntity
import com.example.data.model.ApprovalType
import com.example.data.model.BotActivityState
import com.example.data.model.BotEntity
import com.example.data.model.BotRole
import com.example.data.model.McpServerEntity
import com.example.data.model.RoutineEntity
import com.example.data.model.SkillEntity
import com.example.data.model.SwarmEntity
import com.example.data.model.SwarmMessageEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.data.repository.CoworkerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CoworkerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CoworkerRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CoworkerRepository(database.coworkerDao())
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    val bots: StateFlow<List<BotEntity>> = repository.allBots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val swarms: StateFlow<List<SwarmEntity>> = repository.allSwarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val runningTasks: StateFlow<List<TaskEntity>> = repository.runningTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApprovals: StateFlow<List<ApprovalRequestEntity>> = repository.pendingApprovals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApprovals: StateFlow<List<ApprovalRequestEntity>> = repository.allApprovals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skills: StateFlow<List<SkillEntity>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mcpServers: StateFlow<List<McpServerEntity>> = repository.allMcpServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSwarmId = MutableStateFlow<Long>(1L)
    val selectedSwarmId: StateFlow<Long> = _selectedSwarmId.asStateFlow()

    val currentSwarmMessages: StateFlow<List<SwarmMessageEntity>> = _selectedSwarmId
        .flatMapLatest { id ->
            if (id > 0) repository.getMessagesForSwarm(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSwarm(swarmId: Long) {
        _selectedSwarmId.value = swarmId
    }

    fun dispatchTask(
        title: String,
        description: String,
        botId: String,
        swarmId: Long? = null,
        requiresApproval: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val assignedBot = repository.getBotById(botId)
            if (assignedBot != null) {
                repository.updateBot(
                    assignedBot.copy(
                        status = "WORKING",
                        activityState = BotActivityState.RESEARCHING,
                        currentActionText = "Starting task: $title — Gathering context & opening VM browser",
                        browserSessionActive = true
                    )
                )
            }

            val taskId = repository.insertTask(
                TaskEntity(
                    swarmId = swarmId,
                    primaryBotId = botId,
                    title = title,
                    description = description,
                    status = TaskStatus.RUNNING,
                    currentStepText = "Provisioning persistent VM container. Opening Chromium browser session...",
                    progressPercent = 10,
                    vmNode = "cloud-vm-cluster-${(10..99).random()}",
                    browserUrl = "https://cloud.workspace.internal/app",
                    isHumanApprovalNeeded = requiresApproval
                )
            )

            // Simulate Real-Time Asynchronous Multi-Stage Bot State Progression
            launch {
                simulateTaskProgression(taskId, title, botId, requiresApproval)
            }
        }
    }

    private suspend fun simulateTaskProgression(
        taskId: Long,
        title: String,
        botId: String,
        requiresApproval: Boolean
    ) {
        // Stage 1: RESEARCHING (Gathering data / crawling / querying)
        delay(1800)
        var currentBot = repository.getBotById(botId)
        currentBot?.let {
            repository.updateBot(
                it.copy(
                    status = "WORKING",
                    activityState = BotActivityState.RESEARCHING,
                    currentActionText = "Researching target interfaces & verifying DOM selectors in browser"
                )
            )
        }
        repository.updateTask(
            TaskEntity(
                id = taskId,
                primaryBotId = botId,
                title = title,
                description = "Autonomous research and data collection in progress.",
                status = TaskStatus.RUNNING,
                currentStepText = "Inspecting target page, reading form schema, extracting input fields...",
                progressPercent = 25,
                vmNode = currentBot?.currentVmHost ?: "cloud-vm-node-8",
                browserUrl = "https://app.vendorportal.com/data",
                isHumanApprovalNeeded = requiresApproval
            )
        )

        // Stage 2: DRAFTING / EXECUTING
        delay(2400)
        currentBot = repository.getBotById(botId)
        currentBot?.let {
            repository.updateBot(
                it.copy(
                    status = "WORKING",
                    activityState = BotActivityState.DRAFTING,
                    currentActionText = "Drafting payload: typing inputs, compiling templates, filling forms"
                )
            )
        }
        repository.updateTask(
            TaskEntity(
                id = taskId,
                primaryBotId = botId,
                title = title,
                description = "Drafting and preparing execution parameters.",
                status = TaskStatus.RUNNING,
                currentStepText = "Drafting action payload: populating fields, staging transaction draft...",
                progressPercent = 55,
                vmNode = currentBot?.currentVmHost ?: "cloud-vm-node-8",
                browserUrl = "https://app.vendorportal.com/draft",
                isHumanApprovalNeeded = requiresApproval
            )
        )

        // Stage 3: ANALYZING
        delay(2200)
        currentBot = repository.getBotById(botId)
        currentBot?.let {
            repository.updateBot(
                it.copy(
                    status = "WORKING",
                    activityState = BotActivityState.ANALYZING,
                    currentActionText = "Analyzing safety policies, verifying totals against constraints"
                )
            )
        }
        repository.updateTask(
            TaskEntity(
                id = taskId,
                primaryBotId = botId,
                title = title,
                description = "Analyzing results and verifying policy compliance.",
                status = TaskStatus.RUNNING,
                currentStepText = "Running sanity checks and cross-referencing audit rules...",
                progressPercent = 75,
                vmNode = currentBot?.currentVmHost ?: "cloud-vm-node-8",
                browserUrl = "https://app.vendorportal.com/verify",
                isHumanApprovalNeeded = requiresApproval
            )
        )

        delay(2000)
        if (requiresApproval) {
            // Stage 4A: WAITING FOR INPUT (Human approval requested)
            currentBot = repository.getBotById(botId)
            currentBot?.let {
                repository.updateBot(
                    it.copy(
                        status = "PARKED",
                        activityState = BotActivityState.WAITING_FOR_INPUT,
                        currentActionText = "Waiting for human sign-off on Android device"
                    )
                )
            }
            repository.updateTask(
                TaskEntity(
                    id = taskId,
                    primaryBotId = botId,
                    title = title,
                    description = "Autonomous execution parked at safety boundary.",
                    status = TaskStatus.AWAITING_APPROVAL,
                    currentStepText = "Task Parked: Waiting for user approval on Android command center.",
                    progressPercent = 85,
                    vmNode = currentBot?.currentVmHost ?: "cloud-vm-node-8",
                    browserUrl = "https://app.vendorportal.com/data/confirm",
                    isHumanApprovalNeeded = true
                )
            )
        } else {
            // Stage 4B: COMPLETED
            currentBot = repository.getBotById(botId)
            currentBot?.let {
                repository.updateBot(
                    it.copy(
                        status = "IDLE",
                        activityState = BotActivityState.COMPLETED,
                        currentActionText = "Task completed successfully. Standing by in cloud VM.",
                        completedTasksCount = it.completedTasksCount + 1
                    )
                )
            }
            repository.updateTask(
                TaskEntity(
                    id = taskId,
                    primaryBotId = botId,
                    title = title,
                    description = "Job finished successfully 24/7 in persistent cloud computer.",
                    status = TaskStatus.COMPLETED,
                    currentStepText = "Completed: Artifacts saved to cloud file system and synced to phone.",
                    progressPercent = 100,
                    vmNode = currentBot?.currentVmHost ?: "cloud-vm-node-8",
                    browserUrl = "https://app.vendorportal.com/done",
                    isHumanApprovalNeeded = false
                )
            )
        }
    }

    fun sendSwarmMessage(swarmId: Long, messageText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Post user instruction
            repository.insertMessage(
                SwarmMessageEntity(
                    swarmId = swarmId,
                    senderId = "USER",
                    senderName = "Commander (You)",
                    senderRole = "Human Lead",
                    messageText = messageText
                )
            )

            // Multi-bot swarm auto-reply simulation
            delay(1200)
            val currentSwarm = swarms.value.find { it.id == swarmId }
            val botIdList = currentSwarm?.botIds?.split(",")?.map { it.trim() } ?: listOf("bot_atlas")
            val respondingBotId = botIdList.randomOrNull() ?: "bot_atlas"
            val bot = bots.value.find { it.id == respondingBotId }

            val responseRole = bot?.role ?: BotRole.SALES_OUTBOUND
            val replyText = when (responseRole) {
                BotRole.SALES_OUTBOUND -> "Parsed your instruction. Sourcing target list and queuing drafts in HubSpot."
                BotRole.TALENT_SCOUT -> "Checked ATS deduplication table. 18 qualified candidates matched your criteria."
                BotRole.INVOICE_EXPENSE -> "Navigating vendor billing portal. Downloading transaction receipts now."
                BotRole.BUG_REPRO -> "Spinning up clean browser instance to recreate test sequence. Recording DOM telemetry."
                BotRole.MARKET_INTELLIGENCE -> "Scanning pricing pages and SEC filings. Summarizing delta in shared workspace."
                BotRole.TERMINAL_OPERATOR -> "Executing command in persistent cloud terminal: `docker-compose ps && git status`"
            }

            val mockFile = when ((1..4).random()) {
                1 -> "pipeline_export_live.csv" to "csv"
                2 -> "vendor_statement_q3.pdf" to "pdf"
                3 -> "repro_screen_stream.webm" to "webm"
                else -> null
            }

            repository.insertMessage(
                SwarmMessageEntity(
                    swarmId = swarmId,
                    senderId = respondingBotId,
                    senderName = bot?.name ?: "Coworker",
                    senderRole = bot?.role?.title ?: "AI Coworker",
                    messageText = replyText,
                    sharedFileName = mockFile?.first,
                    sharedFileType = mockFile?.second,
                    browserActionSnapshot = "Headless Chromium session [ID: sess_9901x] active on ${bot?.currentVmHost ?: "cloud-vm"}"
                )
            )
        }
    }

    fun createSwarm(
        name: String,
        description: String,
        selectedBotIds: List<String>,
        activeGoal: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newSwarmId = repository.insertSwarm(
                SwarmEntity(
                    name = name,
                    description = description,
                    botIds = selectedBotIds.joinToString(","),
                    sharedVmId = "vm-swarm-${System.currentTimeMillis() % 10000}",
                    activeGoal = activeGoal
                )
            )

            // Welcome message in swarm
            repository.insertMessage(
                SwarmMessageEntity(
                    swarmId = newSwarmId,
                    senderId = "SYSTEM",
                    senderName = "Swarm Orchestrator",
                    senderRole = "Cloud Supervisor",
                    messageText = "Swarm initialized with ${selectedBotIds.size} specialized bots sharing cloud VM and unified filesystem."
                )
            )
            _selectedSwarmId.value = newSwarmId
        }
    }

    fun resolveApproval(
        approvalId: Long,
        isApproved: Boolean,
        userInput: String? = null,
        rejectionReason: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetApproval = allApprovals.value.find { it.id == approvalId }
            repository.resolveApproval(approvalId, isApproved, userInput, rejectionReason)

            if (targetApproval != null) {
                val bot = repository.getBotById(targetApproval.botId)
                if (bot != null) {
                    if (isApproved) {
                        repository.updateBot(
                            bot.copy(
                                status = "WORKING",
                                activityState = BotActivityState.ANALYZING,
                                currentActionText = "Authorized by commander. Executing final transaction..."
                            )
                        )
                        delay(2500)
                        repository.updateBot(
                            bot.copy(
                                status = "IDLE",
                                activityState = BotActivityState.COMPLETED,
                                currentActionText = "Completed approved transaction successfully.",
                                completedTasksCount = bot.completedTasksCount + 1
                            )
                        )
                    } else {
                        repository.updateBot(
                            bot.copy(
                                status = "IDLE",
                                activityState = BotActivityState.IDLE,
                                currentActionText = "Action rejected by human reviewer. Standing by."
                            )
                        )
                    }
                }
            }
        }
    }

    fun recordNewSkill(
        title: String,
        targetAppOrTool: String,
        description: String,
        recordedSteps: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSkill(
                SkillEntity(
                    title = title,
                    targetAppOrTool = targetAppOrTool,
                    description = description,
                    recordedStepsJson = recordedSteps,
                    timesExecuted = 1
                )
            )
        }
    }

    fun toggleRoutine(routine: RoutineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleRoutine(routine)
        }
    }

    fun toggleMcpServerStatus(server: McpServerEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val newStatus = if (server.status == "CONNECTED") "DISCONNECTED" else "CONNECTED"
            repository.updateMcpServer(server.copy(status = newStatus))
        }
    }
}
