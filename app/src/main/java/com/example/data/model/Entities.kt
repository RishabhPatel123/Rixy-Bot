package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BotRole(val title: String, val description: String) {
    SALES_OUTBOUND(
        "Sales Outbound",
        "Researches targets, updates CRM, and queues up customized outreach drafts for sign-off."
    ),
    TALENT_SCOUT(
        "Talent Scout",
        "Sources candidates on LinkedIn, cross-references internal ATS to avoid duplicates, drafts messages."
    ),
    INVOICE_EXPENSE(
        "Invoice & Expense Manager",
        "Logs into vendor portals, downloads monthly PDF invoices, codes expenses, chases missing receipts."
    ),
    BUG_REPRO(
        "Bug Reproduction",
        "Reads error reports, physically recreates the issue in a browser, and hands engineering a clean write-up."
    ),
    MARKET_INTELLIGENCE(
        "Market Intelligence",
        "Scrapes competitor pricing, monitors regulatory filings, and summarizes industry shifts."
    ),
    TERMINAL_OPERATOR(
        "Dev & Terminal Operator",
        "Runs builds, manages Docker pods, checks system telemetry, and inspects git repos via cloud bash."
    )
}

enum class TaskStatus {
    RUNNING,
    AWAITING_APPROVAL,
    COMPLETED,
    FAILED,
    SCHEDULED
}

enum class BotActivityState(val label: String) {
    IDLE("Idle"),
    RESEARCHING("Researching"),
    DRAFTING("Drafting"),
    ANALYZING("Analyzing"),
    WAITING_FOR_INPUT("Waiting for Input"),
    COMPLETED("Completed")
}

enum class ApprovalType {
    SEND_EMAIL,
    SPEND_MONEY,
    BYPASS_2FA,
    CODE_DEPLOY,
    CRM_BULK_UPDATE
}

@Entity(tableName = "bots")
data class BotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: BotRole,
    val status: String = "IDLE", // IDLE, WORKING, PARKED
    val activityState: BotActivityState = BotActivityState.IDLE,
    val currentActionText: String = "Standing by in persistent VM",
    val currentVmHost: String = "cloud-vm-us-east-1a",
    val cpuUsage: String = "12%",
    val memoryUsage: String = "1.8 GB",
    val browserSessionActive: Boolean = true,
    val completedTasksCount: Int = 0,
    val colorHex: Long = 0xFF00E5FF
)

@Entity(tableName = "swarms")
data class SwarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val botIds: String, // Comma-separated bot IDs (2 to 6 bots)
    val sharedVmId: String = "vm-swarm-cluster-09",
    val activeGoal: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isRunning: Boolean = true
)

@Entity(tableName = "swarm_messages")
data class SwarmMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val swarmId: Long,
    val senderId: String, // "USER" or bot id
    val senderName: String,
    val senderRole: String, // e.g. "User", "Sales Outbound", "Talent Scout"
    val messageText: String,
    val sharedFileName: String? = null,
    val sharedFileType: String? = null, // "csv", "pdf", "webm", "json"
    val browserActionSnapshot: String? = null, // e.g. "Navigated to https://linkedin.com/recruiter"
    val terminalCommandOutput: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val swarmId: Long? = null,
    val primaryBotId: String,
    val title: String,
    val description: String,
    val status: TaskStatus = TaskStatus.RUNNING,
    val currentStepText: String,
    val progressPercent: Int = 0, // 0 to 100
    val executionLog: String = "", // Delimited logs
    val vmNode: String = "cloud-vm-east-4",
    val browserUrl: String = "https://app.vendorportal.com",
    val isHumanApprovalNeeded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "approvals")
data class ApprovalRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val botId: String,
    val botName: String,
    val botRole: BotRole,
    val type: ApprovalType,
    val title: String,
    val details: String,
    val previewPayload: String, // e.g. draft email content, invoice receipt details, 2FA prompt
    val riskLevel: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
    val requiresInput: Boolean = false, // e.g. entering a 2FA OTP code
    val isApproved: Boolean? = null, // null = pending, true = approved, false = rejected
    val rejectionReason: String? = null,
    val userResponseInput: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAppOrTool: String,
    val description: String,
    val recordedStepsJson: String, // Step by step recorded visual actions
    val timesExecuted: Int = 0,
    val lastRecordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val assignedBotId: String,
    val scheduleCron: String, // e.g. "Every Monday at 9:00 AM"
    val routineAction: String,
    val isEnabled: Boolean = true,
    val lastRunTime: String = "Yesterday, 9:00 AM",
    val nextRunTime: String = "Monday, 9:00 AM"
)

@Entity(tableName = "mcp_servers")
data class McpServerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverName: String,
    val protocolUrl: String, // "mcp://hubspot.internal", "mcp://postgres.prod"
    val status: String = "CONNECTED", // CONNECTED, SYNCING, DISCONNECTED
    val toolsCount: Int = 8,
    val description: String
)
