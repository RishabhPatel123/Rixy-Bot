package com.example.domain.manager

import com.example.data.model.BotEntity

data class BotRecommendationResult(
    val recommendedBot: BotEntity,
    val matchScorePercent: Int,
    val managerialRationale: String,
    val fallbackBot: BotEntity?,
    val capabilityHighlights: List<String>,
    val riskFactor: String = "Low",
    val estimatedMinutes: Int = 15
)

data class ManagerInitiativePhase(
    val phaseNumber: Int,
    val phaseName: String,
    val assignedBotId: String,
    val assignedBotName: String,
    val assignedBotRoleTitle: String,
    val taskTitle: String,
    val taskDescription: String,
    val expectedArtifact: String,
    val priority: String = "Normal",
    val requiresHumanApprovalGate: Boolean = false,
    val approvalReason: String? = null,
    val dependencies: String = "None"
)

data class ComplexInitiativePlan(
    val initiativeTitle: String,
    val executiveSummary: String,
    val complexityRating: String,
    val estimatedDurationMinutes: Int,
    val phases: List<ManagerInitiativePhase>,
    val contingencyPolicy: String,
    val targetSwarmCluster: String = "vm-cluster-director-orion"
)

data class EnterprisePresetInitiative(
    val title: String,
    val subtitle: String,
    val goalPrompt: String,
    val category: String,
    val estimatedPhases: Int = 4
)
