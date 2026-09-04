package com.example.data.repository

import com.example.data.dao.CoworkerDao
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class CoworkerRepository(private val dao: CoworkerDao) {

    val allBots: Flow<List<BotEntity>> = dao.getAllBots()
    val allSwarms: Flow<List<SwarmEntity>> = dao.getAllSwarms()
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val runningTasks: Flow<List<TaskEntity>> = dao.getRunningTasks()
    val pendingApprovals: Flow<List<ApprovalRequestEntity>> = dao.getPendingApprovals()
    val allApprovals: Flow<List<ApprovalRequestEntity>> = dao.getAllApprovals()
    val allSkills: Flow<List<SkillEntity>> = dao.getAllSkills()
    val allRoutines: Flow<List<RoutineEntity>> = dao.getAllRoutines()
    val allMcpServers: Flow<List<McpServerEntity>> = dao.getAllMcpServers()

    fun getMessagesForSwarm(swarmId: Long): Flow<List<SwarmMessageEntity>> =
        dao.getMessagesForSwarm(swarmId)

    fun getSwarmById(swarmId: Long): Flow<SwarmEntity?> = dao.getSwarmById(swarmId)

    suspend fun getBotById(id: String): BotEntity? = withContext(Dispatchers.IO) {
        dao.getBotById(id)
    }

    suspend fun updateBot(bot: BotEntity) = withContext(Dispatchers.IO) {
        dao.updateBot(bot)
    }

    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        dao.updateTask(task)
    }

    suspend fun insertSwarm(swarm: SwarmEntity): Long = withContext(Dispatchers.IO) {
        dao.insertSwarm(swarm)
    }

    suspend fun insertMessage(message: SwarmMessageEntity): Long = withContext(Dispatchers.IO) {
        dao.insertMessage(message)
    }

    suspend fun resolveApproval(
        approvalId: Long,
        isApproved: Boolean,
        userInput: String? = null,
        rejectionReason: String? = null
    ) = withContext(Dispatchers.IO) {
        val approvals = dao.getAllApprovals().firstOrNull() ?: emptyList()
        val target = approvals.find { it.id == approvalId }
        if (target != null) {
            val updated = target.copy(
                isApproved = isApproved,
                userResponseInput = userInput,
                rejectionReason = rejectionReason
            )
            dao.updateApproval(updated)

            // Also resume or update corresponding task
            val task = dao.getTaskById(target.taskId)
            if (task != null) {
                if (isApproved) {
                    dao.updateTask(
                        task.copy(
                            status = TaskStatus.RUNNING,
                            currentStepText = "Human approval granted. Resuming execution in cloud VM...",
                            progressPercent = (task.progressPercent + 20).coerceAtMost(100)
                        )
                    )
                } else {
                    dao.updateTask(
                        task.copy(
                            status = TaskStatus.FAILED,
                            currentStepText = "Execution halted by human reviewer: ${rejectionReason ?: "Rejected"}"
                        )
                    )
                }
            }
        }
    }

    suspend fun insertSkill(skill: SkillEntity): Long = withContext(Dispatchers.IO) {
        dao.insertSkill(skill)
    }

    suspend fun toggleRoutine(routine: RoutineEntity) = withContext(Dispatchers.IO) {
        dao.updateRoutine(routine.copy(isEnabled = !routine.isEnabled))
    }

    suspend fun updateMcpServer(server: McpServerEntity) = withContext(Dispatchers.IO) {
        dao.updateMcpServer(server)
    }

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingBots = dao.getAllBots().firstOrNull()
        if (!existingBots.isNullOrEmpty()) return@withContext

        // 1. Seed 6 specialized AI Coworker Bots
        val bots = listOf(
            BotEntity(
                id = "bot_atlas",
                name = "Atlas",
                role = BotRole.SALES_OUTBOUND,
                status = "WORKING",
                activityState = BotActivityState.RESEARCHING,
                currentActionText = "Researching target accounts & verifying executive emails on Apollo",
                currentVmHost = "cloud-vm-us-east-1a",
                cpuUsage = "28%",
                memoryUsage = "3.1 GB",
                browserSessionActive = true,
                completedTasksCount = 142,
                colorHex = 0xFF00E5FF
            ),
            BotEntity(
                id = "bot_lyra",
                name = "Lyra",
                role = BotRole.TALENT_SCOUT,
                status = "PARKED",
                activityState = BotActivityState.WAITING_FOR_INPUT,
                currentActionText = "Waiting for human sign-off on 18 sourced candidates",
                currentVmHost = "cloud-vm-us-east-1b",
                cpuUsage = "6%",
                memoryUsage = "2.4 GB",
                browserSessionActive = true,
                completedTasksCount = 89,
                colorHex = 0xFF818CF8
            ),
            BotEntity(
                id = "bot_ledger",
                name = "Ledger",
                role = BotRole.INVOICE_EXPENSE,
                status = "WORKING",
                activityState = BotActivityState.DRAFTING,
                currentActionText = "Drafting QuickBooks expense voucher for AWS infrastructure invoice",
                currentVmHost = "cloud-vm-eu-west-2",
                cpuUsage = "19%",
                memoryUsage = "2.8 GB",
                browserSessionActive = true,
                completedTasksCount = 210,
                colorHex = 0xFF10B981
            ),
            BotEntity(
                id = "bot_spectre",
                name = "Spectre",
                role = BotRole.BUG_REPRO,
                status = "WORKING",
                activityState = BotActivityState.ANALYZING,
                currentActionText = "Analyzing browser console errors & reproducing visual DOM glitch",
                currentVmHost = "cloud-vm-us-central-3",
                cpuUsage = "45%",
                memoryUsage = "4.2 GB",
                browserSessionActive = true,
                completedTasksCount = 67,
                colorHex = 0xFFF59E0B
            ),
            BotEntity(
                id = "bot_nova",
                name = "Nova",
                role = BotRole.MARKET_INTELLIGENCE,
                status = "IDLE",
                activityState = BotActivityState.COMPLETED,
                currentActionText = "Finished competitive pricing benchmark; waiting for next routine",
                currentVmHost = "cloud-vm-us-east-1a",
                cpuUsage = "3%",
                memoryUsage = "1.5 GB",
                browserSessionActive = false,
                completedTasksCount = 312,
                colorHex = 0xFFEC4899
            ),
            BotEntity(
                id = "bot_turing",
                name = "Turing",
                role = BotRole.TERMINAL_OPERATOR,
                status = "WORKING",
                activityState = BotActivityState.RESEARCHING,
                currentActionText = "Inspecting git diffs and running integration test container",
                currentVmHost = "cloud-vm-us-west-4",
                cpuUsage = "33%",
                memoryUsage = "3.8 GB",
                browserSessionActive = true,
                completedTasksCount = 195,
                colorHex = 0xFF06B6D4
            )
        )
        dao.insertBots(bots)

        // 2. Seed Swarms (2 to 6 bots collaborating in single cloud workspace)
        val swarm1Id = dao.insertSwarm(
            SwarmEntity(
                name = "Growth & Outbound Pipeline Swarm",
                description = "Atlas (Sales) & Lyra (Talent) passing enriched leads, updating CRM, and queuing verified outreach drafts.",
                botIds = "bot_atlas,bot_lyra,bot_nova",
                sharedVmId = "vm-swarm-cluster-01",
                activeGoal = "Sourcing 50 VP of Engineering candidates & updating HubSpot CRM pipeline."
            )
        )

        val swarm2Id = dao.insertSwarm(
            SwarmEntity(
                name = "Finance & Invoice Audit Swarm",
                description = "Ledger (Invoices) & Turing (Terminal) downloading monthly vendor PDFs, coding expenses, and verifying AWS/Stripe receipts.",
                botIds = "bot_ledger,bot_turing",
                sharedVmId = "vm-swarm-cluster-02",
                activeGoal = "Parsing Q3 AWS and Datadog invoices, cross-referencing credit card transactions."
            )
        )

        val swarm3Id = dao.insertSwarm(
            SwarmEntity(
                name = "QA & Live Bug Repro Swarm",
                description = "Spectre (Repro) recreating user error reports visually in headless Chrome, while Turing inspects Docker pod logs.",
                botIds = "bot_spectre,bot_turing",
                sharedVmId = "vm-swarm-cluster-03",
                activeGoal = "Reproducing checkout race condition on staging environment."
            )
        )

        // 3. Seed Swarm Messages (showing inter-bot communication, shared files, browser visual steps)
        dao.insertMessage(
            SwarmMessageEntity(
                swarmId = swarm1Id,
                senderId = "USER",
                senderName = "Commander (You)",
                senderRole = "Human Lead",
                messageText = "Assigning new initiative: Find Series B SaaS startups hiring Head of AI, cross-reference ATS to prevent duplicate outreach, and draft intro emails."
            )
        )
        dao.insertMessage(
            SwarmMessageEntity(
                swarmId = swarm1Id,
                senderId = "bot_nova",
                senderName = "Nova",
                senderRole = "Market Intelligence",
                messageText = "Scraped Crunchbase & LinkedIn for 45 newly funded AI teams. Uploaded raw targets to shared workspace.",
                sharedFileName = "targets_series_b_august.csv",
                sharedFileType = "csv",
                browserActionSnapshot = "Scraped 45 companies via Chromium headless session [Viewport: 1920x1080]"
            )
        )
        dao.insertMessage(
            SwarmMessageEntity(
                swarmId = swarm1Id,
                senderId = "bot_lyra",
                senderName = "Lyra",
                senderRole = "Talent Scout",
                messageText = "Cross-referenced targets against internal Greenhouse ATS via MCP connector. Filtered out 12 existing contacts. 33 clean prospects remain.",
                sharedFileName = "ats_deduped_candidates.json",
                sharedFileType = "json"
            )
        )
        dao.insertMessage(
            SwarmMessageEntity(
                swarmId = swarm1Id,
                senderId = "bot_atlas",
                senderName = "Atlas",
                senderRole = "Sales Outbound",
                messageText = "Drafted 33 customized intro emails in human voice. Task parked for Human-in-the-Loop review before dispatching.",
                browserActionSnapshot = "Browser parked on HubSpot Drafts tab. Awaiting sign-off."
            )
        )

        // Swarm 2 Messages (Finance)
        dao.insertMessage(
            SwarmMessageEntity(
                swarmId = swarm2Id,
                senderId = "bot_ledger",
                senderName = "Ledger",
                senderRole = "Invoice & Expense Manager",
                messageText = "Logged into AWS Billing Portal via Visual Computer-Use session. Downloaded August Consolidated Invoice.",
                sharedFileName = "aws_invoice_august_2026.pdf",
                sharedFileType = "pdf",
                browserActionSnapshot = "Clicked 'Download PDF' at coordinate (1140, 380) on console.aws.amazon.com"
            )
        )
        dao.insertMessage(
            SwarmMessageEntity(
                swarmId = swarm2Id,
                senderId = "bot_turing",
                senderName = "Turing",
                senderRole = "Dev & Terminal Operator",
                messageText = "Extracted line items via terminal script `parse_invoice.py`. Coded $142.50 to Infrastructure -> S3 egress.",
                terminalCommandOutput = "$ python3 parse_invoice.py --file aws_invoice_august_2026.pdf\nExtracted: Total $142.50 USD | Tax: $0.00 | GL Code: 6010-Infra"
            )
        )

        // 4. Seed Tasks
        val task1Id = dao.insertTask(
            TaskEntity(
                swarmId = swarm1Id,
                primaryBotId = "bot_atlas",
                title = "Dispatch 33 Personalized Outreach Emails",
                description = "Outbound campaign to targeted VP of Engineering leads. Requires human sign-off before transmission.",
                status = TaskStatus.AWAITING_APPROVAL,
                currentStepText = "Parked at approval gate: Human review needed to send emails.",
                progressPercent = 85,
                vmNode = "cloud-vm-us-east-1a",
                browserUrl = "https://app.hubspot.com/email/drafts/3982",
                isHumanApprovalNeeded = true
            )
        )

        val task2Id = dao.insertTask(
            TaskEntity(
                swarmId = swarm2Id,
                primaryBotId = "bot_ledger",
                title = "Authorize $142.50 AWS Invoice Settlement",
                description = "Vendor portal requested authorization to settle overdue invoice statement via corporate card.",
                status = TaskStatus.AWAITING_APPROVAL,
                currentStepText = "Parked at spending boundary: $142.50 exceeds auto-spend threshold of $50.",
                progressPercent = 70,
                vmNode = "cloud-vm-eu-west-2",
                browserUrl = "https://billing.vendorportal.com/invoices/991",
                isHumanApprovalNeeded = true
            )
        )

        val task3Id = dao.insertTask(
            TaskEntity(
                swarmId = swarm1Id,
                primaryBotId = "bot_lyra",
                title = "LinkedIn 2FA Verification Challenge",
                description = "Portal prompted for 6-digit SMS verification code to proceed with recruiter search.",
                status = TaskStatus.AWAITING_APPROVAL,
                currentStepText = "Parked at 2FA prompt on cloud browser: Awaiting SMS token from phone.",
                progressPercent = 40,
                vmNode = "cloud-vm-us-east-1b",
                browserUrl = "https://www.linkedin.com/checkpoint/challenge",
                isHumanApprovalNeeded = true
            )
        )

        dao.insertTask(
            TaskEntity(
                swarmId = swarm3Id,
                primaryBotId = "bot_spectre",
                title = "Reproduce Checkout Race Condition #819",
                description = "Replaying user click stream on staging environment: Cart -> Promo Code -> Rapid Double Click.",
                status = TaskStatus.RUNNING,
                currentStepText = "Simulating click sequence at coordinate (680, 520). Monitoring DevTools network tab...",
                progressPercent = 55,
                vmNode = "cloud-vm-us-central-3",
                browserUrl = "https://staging.acmeshop.internal/checkout",
                isHumanApprovalNeeded = false
            )
        )

        // 5. Seed Human-in-the-Loop Approvals
        dao.insertApproval(
            ApprovalRequestEntity(
                taskId = task1Id,
                botId = "bot_atlas",
                botName = "Atlas",
                botRole = BotRole.SALES_OUTBOUND,
                type = ApprovalType.SEND_EMAIL,
                title = "Send 33 Personalized Outreach Emails",
                details = "Atlas completed candidate research and drafted 33 tailored introduction notes for Series B VP of Engineering leads.",
                previewPayload = "Subject: Quick question regarding your infrastructure at {{company_name}}\n\nHi {{first_name}},\nI saw you're expanding your platform engineering group after the Series B. We've built an autonomous AI coworker swarm that handles routine dev ops and bug reproduction...\n\n[33 recipients queued in HubSpot]",
                riskLevel = "MEDIUM",
                requiresInput = false
            )
        )

        dao.insertApproval(
            ApprovalRequestEntity(
                taskId = task2Id,
                botId = "bot_ledger",
                botName = "Ledger",
                botRole = BotRole.INVOICE_EXPENSE,
                type = ApprovalType.SPEND_MONEY,
                title = "Pay AWS Cloud Invoice: $142.50",
                details = "Monthly compute and S3 bucket fees for cloud VM clusters. Expense exceeds $50 autonomous spending boundary.",
                previewPayload = "Vendor: Amazon Web Services Inc.\nAmount: $142.50 USD\nPayment Method: Corporate Visa ending in •••• 4019\nInvoice Number: INV-984210-US\nGL Account: 6010 Infrastructure",
                riskLevel = "HIGH",
                requiresInput = false
            )
        )

        dao.insertApproval(
            ApprovalRequestEntity(
                taskId = task3Id,
                botId = "bot_lyra",
                botName = "Lyra",
                botRole = BotRole.TALENT_SCOUT,
                type = ApprovalType.BYPASS_2FA,
                title = "Enter 2FA Code for LinkedIn Recruiter",
                details = "Persistent cloud browser encountered 2FA security checkpoint. Enter the 6-digit code received on your phone to continue.",
                previewPayload = "Challenge: SMS 2FA verification\nPhone: +1 (•••) •••-4912\nSession VM: cloud-vm-us-east-1b (Headless Chrome v128)",
                riskLevel = "CRITICAL",
                requiresInput = true
            )
        )

        // 6. Seed Repeatable "Skills" (Taught via Screen Recording)
        dao.insertSkill(
            SkillEntity(
                title = "Stripe Monthly Tax Invoice Downloader",
                targetAppOrTool = "Stripe Dashboard (Web)",
                description = "Navigates to Payments -> Invoices, filters by status 'Paid', downloads unified PDF, and extracts totals.",
                recordedStepsJson = "1. Click [data-nav='invoices']\n2. Set filter status:Paid\n3. Click export button at (1040, 210)\n4. Wait for download event\n5. Move to /workspace/invoices",
                timesExecuted = 48
            )
        )

        dao.insertSkill(
            SkillEntity(
                title = "Greenhouse Candidate ATS De-duplication",
                targetAppOrTool = "Greenhouse ATS Portal",
                description = "Logs in, executes candidate search by email and LinkedIn URL, flags duplicates, and updates stage.",
                recordedStepsJson = "1. Navigate to candidates/search\n2. Type query in input field\n3. Parse search results table\n4. Compare email against ATS index\n5. Return JSON verification",
                timesExecuted = 112
            )
        )

        dao.insertSkill(
            SkillEntity(
                title = "Sentry Error to Linear Ticket Generator",
                targetAppOrTool = "Sentry.io & Linear",
                description = "Extracts stack trace, breadcrumbs, and browser user agent from Sentry, opens Linear, and files structured ticket.",
                recordedStepsJson = "1. Focus Sentry issue details\n2. Copy stack trace and tags\n3. Open linear.app/new-issue\n4. Paste markdown template\n5. Assign label 'bug-repro'",
                timesExecuted = 35
            )
        )

        // 7. Seed Scheduled Routines
        dao.insertRoutines(
            listOf(
                RoutineEntity(
                    title = "Weekly Competitor Pricing & Feature Scrape",
                    assignedBotId = "bot_nova",
                    scheduleCron = "Every Monday at 9:00 AM",
                    routineAction = "Scrapes 6 competitor pricing tables, calculates delta, and posts summary in Slack #growth.",
                    isEnabled = true,
                    lastRunTime = "Last Monday, 9:00 AM",
                    nextRunTime = "Next Monday, 9:00 AM"
                ),
                RoutineEntity(
                    title = "Nightly ATS Sync & Candidate Deduplication",
                    assignedBotId = "bot_lyra",
                    scheduleCron = "Every Day at 2:00 AM",
                    routineAction = "Queries LinkedIn Recruiter pipeline and updates internal candidate stages.",
                    isEnabled = true,
                    lastRunTime = "Today, 2:00 AM",
                    nextRunTime = "Tomorrow, 2:00 AM"
                ),
                RoutineEntity(
                    title = "Friday Vendor Portal Invoice Reconciliation",
                    assignedBotId = "bot_ledger",
                    scheduleCron = "Every Friday at 4:30 PM",
                    routineAction = "Logs into AWS, Datadog, and Google Cloud, harvests PDF receipts, and matches credit card statements.",
                    isEnabled = true,
                    lastRunTime = "Last Friday, 4:30 PM",
                    nextRunTime = "This Friday, 4:30 PM"
                )
            )
        )

        // 8. Seed MCP Servers & Plugins
        dao.insertMcpServers(
            listOf(
                McpServerEntity(
                    serverName = "HubSpot CRM MCP Server",
                    protocolUrl = "mcp://hubspot.internal:8080",
                    status = "CONNECTED",
                    toolsCount = 12,
                    description = "Provides contacts, deals, company lookup, and draft email endpoints."
                ),
                McpServerEntity(
                    serverName = "PostgreSQL Warehouse MCP",
                    protocolUrl = "mcp://postgres-prod.data.internal",
                    status = "CONNECTED",
                    toolsCount = 18,
                    description = "Read-only access to customer analytics, subscriptions, and usage logs."
                ),
                McpServerEntity(
                    serverName = "GitHub Enterprise Terminal MCP",
                    protocolUrl = "mcp://github.internal",
                    status = "CONNECTED",
                    toolsCount = 15,
                    description = "Branch creation, issue tracking, CI run status, and git commit visualizer."
                ),
                McpServerEntity(
                    serverName = "Stripe Financial MCP Plugin",
                    protocolUrl = "mcp://stripe.plugin.internal",
                    status = "CONNECTED",
                    toolsCount = 9,
                    description = "Invoicing, refund approvals, subscription management, and webhook inspection."
                )
            )
        )
    }
}
