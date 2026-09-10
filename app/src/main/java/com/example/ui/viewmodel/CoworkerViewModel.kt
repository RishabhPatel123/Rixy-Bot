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



class CoworkerViewModel(
    private val repository: com.example.data.repository.CoworkerRepository,
    val geminiApiService: com.example.network.GeminiApiService
) : androidx.lifecycle.ViewModel() {


    init {
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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow(System.currentTimeMillis())
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()

    fun refreshSwarmStatus(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            try {
                repository.refreshSwarmAndBots()
                delay(750) // Realistic telemetry synchronization delay
                _lastRefreshTime.value = System.currentTimeMillis()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
                onComplete?.invoke()
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentSwarmMessages: StateFlow<List<SwarmMessageEntity>> = _selectedSwarmId
        .flatMapLatest { id ->
            if (id > 0) repository.getMessagesForSwarm(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSwarm(swarmId: Long) {
        _selectedSwarmId.value = swarmId
    }

    fun dispatchTaskPlan(goal: String, subTasks: List<com.example.ui.planner.SubTaskPlan>) {
        viewModelScope.launch(Dispatchers.IO) {
            // Optional: Create a temporary Swarm or just dispatch multiple tasks.
            // Let's create a swarm for this goal
            val botIds = subTasks.map { it.botId }.distinct()
            val swarmId = repository.insertSwarm(
                SwarmEntity(
                    name = "Goal Orchestration",
                    description = goal,
                    botIds = botIds.joinToString(","),
                    sharedVmId = "vm-swarm-${System.currentTimeMillis() % 10000}",
                    activeGoal = goal
                )
            )

            // Dispatch each sub-task
            subTasks.forEachIndexed { index, subTask ->
                val title = subTask.title
                val description = subTask.description
                val botId = subTask.botId
                val priority = subTask.priority
                
                // Add a small delay between dispatches so they start sequentially
                delay((index * 400).toLong())
                
                dispatchTask(
                    title = title,
                    description = description,
                    botId = botId,
                    swarmId = swarmId,
                    requiresApproval = index == subTasks.size - 1, // Make the last task require approval to show intercept
                    priority = priority
                )
            }
        }
    }

    fun rebalanceWorkforce() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rebalanceBotWorkloads()
        }
    }

    fun delegateManagerPlan(plan: com.example.domain.manager.ComplexInitiativePlan, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val swarmId = repository.delegateManagerInitiative(plan)
            _selectedSwarmId.value = swarmId
            onComplete?.invoke(swarmId)
        }
    }

    fun updateBotDirectives(botId: String, directives: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bot = repository.getBotById(botId) ?: return@launch
            repository.updateBot(bot.copy(supervisoryDirectives = directives))
        }
    }

    fun dispatchTask(
        title: String,
        description: String,
        botId: String,
        swarmId: Long? = null,
        requiresApproval: Boolean = false,
        priority: String = "Normal"
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
                    isHumanApprovalNeeded = requiresApproval,
                    priority = priority
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
            val currentSwarm = swarms.value.find { it.id == swarmId }
            val botIdList = currentSwarm?.botIds?.split(",")?.map { it.trim() } ?: listOf("bot_atlas")
            val respondingBotId = botIdList.randomOrNull() ?: "bot_atlas"
            val bot = bots.value.find { it.id == respondingBotId }

            val responseRole = bot?.role?.title ?: "AI Coworker"
            val responseName = bot?.name ?: "Coworker"

            val replyText = geminiApiService.generateChatResponse(
                userMessage = messageText,
                botName = responseName,
                botRole = responseRole
            )

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
                    senderName = responseName,
                    senderRole = responseRole,
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
        recordedSteps: String,
        inputParametersSchema: String = "[]"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSkill(
                SkillEntity(
                    title = title,
                    targetAppOrTool = targetAppOrTool,
                    description = description,
                    recordedStepsJson = recordedSteps,
                    inputParametersSchema = inputParametersSchema,
                    timesExecuted = 1
                )
            )
        }
    }

    fun deleteSkill(skill: SkillEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSkill(skill)
        }
    }

    fun updateSkill(skill: SkillEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSkill(skill)
        }
    }

    fun toggleRoutine(routine: RoutineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleRoutine(routine)
        }
    }

    fun toggleMcpServerStatus(server: McpServerEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (server.status == "CONNECTED") {
                repository.updateMcpServer(server.copy(status = "DISCONNECTED"))
            } else if (server.status == "DISCONNECTED") {
                // Show syncing state first
                repository.updateMcpServer(server.copy(status = "SYNCING"))
                delay(1500) // Simulate sync
                repository.updateMcpServer(server.copy(status = "CONNECTED"))
            }
        }
    }

    fun handoffTask(taskId: Long, targetBotId: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = tasks.value.find { it.id == taskId }
            if (task != null) {
                // Handoff to new bot
                repository.updateTask(
                    task.copy(
                        primaryBotId = targetBotId,
                        currentStepText = "Handed off. Note: $note",
                        progressPercent = 10,
                        status = TaskStatus.RUNNING
                    )
                )
                // Set the target bot's status to WORKING
                val targetBot = repository.getBotById(targetBotId)
                targetBot?.let {
                    repository.updateBot(
                        it.copy(
                            status = "WORKING",
                            activityState = BotActivityState.RESEARCHING,
                            currentActionText = "Received handoff: $note"
                        )
                    )
                }
            }
        }
    }
}
