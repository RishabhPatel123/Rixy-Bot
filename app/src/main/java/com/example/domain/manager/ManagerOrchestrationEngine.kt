package com.example.domain.manager

import com.example.data.model.BotEntity
import com.example.data.model.BotRole

object ManagerOrchestrationEngine {

    val enterprisePresets = listOf(
        EnterprisePresetInitiative(
            title = "Competitor Intelligence & Go-To-Market",
            subtitle = "Nova + Turing + Atlas + Orion Sign-off",
            goalPrompt = "Scrape competitor pricing models, run normalization script in terminal, prepare sales comparison matrix, and draft executive memo for sign-off.",
            category = "Strategy & Outbound",
            estimatedPhases = 4
        ),
        EnterprisePresetInitiative(
            title = "Quarterly Cloud Invoice & FinOps Audit",
            subtitle = "Turing + Ledger + Atlas + Human Sign-off",
            goalPrompt = "Inspect cloud VM Docker usage, download monthly AWS/Stripe PDF invoices, calculate discrepancies, and queue approval for expense adjustments.",
            category = "Finance & FinOps",
            estimatedPhases = 4
        ),
        EnterprisePresetInitiative(
            title = "Production Incident Repro & Hotfix Validation",
            subtitle = "Spectre + Turing + Nova + Dev Sign-off",
            goalPrompt = "Recreate browser DOM exception reported by customers, collect pod container crash dumps, inspect git diffs, and package reproducible report.",
            category = "Engineering & QA",
            estimatedPhases = 4
        ),
        EnterprisePresetInitiative(
            title = "VP Engineering Global Recruitment Drive",
            subtitle = "Nova + Lyra + Atlas + Executive Approval",
            goalPrompt = "Map top engineering leads across tech firms, verify GitHub/LinkedIn profiles, filter against internal ATS, and generate personalized outreach drafts.",
            category = "Talent & Growth",
            estimatedPhases = 4
        )
    )

    /**
     * Determines which bot should be used for any given task with managerial justification.
     */
    fun recommendBotForTask(taskDescription: String, bots: List<BotEntity>): BotRecommendationResult {
        val query = taskDescription.lowercase()
        val specialists = bots.filter { !it.isManager }

        val atlas = specialists.find { it.role == BotRole.SALES_OUTBOUND } ?: bots.first()
        val lyra = specialists.find { it.role == BotRole.TALENT_SCOUT } ?: bots.first()
        val ledger = specialists.find { it.role == BotRole.INVOICE_EXPENSE } ?: bots.first()
        val spectre = specialists.find { it.role == BotRole.BUG_REPRO } ?: bots.first()
        val nova = specialists.find { it.role == BotRole.MARKET_INTELLIGENCE } ?: bots.first()
        val turing = specialists.find { it.role == BotRole.TERMINAL_OPERATOR } ?: bots.first()

        return when {
            // Finance, invoices, accounting, expenses, billing, QuickBooks, tax
            listOf("invoice", "receipt", "expense", "bill", "quickbooks", "accounting", "tax", "cost", "reimbursement", "voucher", "dollar", "payment").any { query.contains(it) } -> {
                BotRecommendationResult(
                    recommendedBot = ledger,
                    matchScorePercent = 98,
                    managerialRationale = "Ledger is assigned: His automated portal auth and receipt reconciliation routines prevent manual financial data-entry leaks. Workload capacity is optimal.",
                    fallbackBot = turing,
                    capabilityHighlights = listOf("Automated Portal Login", "PDF Table Extraction", "Expense Coding", "Discrepancy Flags"),
                    riskFactor = "Medium (Requires Human Sign-off for funds > $500)",
                    estimatedMinutes = 15
                )
            }

            // Recruitment, hiring, candidates, LinkedIn, talent, interview, ATS
            listOf("candidate", "recruit", "hire", "hiring", "talent", "linkedin", "resume", "cv", "job", "applicant", "ats", "headhunt").any { query.contains(it) } -> {
                BotRecommendationResult(
                    recommendedBot = lyra,
                    matchScorePercent = 96,
                    managerialRationale = "Lyra is assigned: Equipped with high-precision talent matching and deduplication against internal ATS databases to prevent candidate duplicate contacts.",
                    fallbackBot = atlas,
                    capabilityHighlights = listOf("Profile Scraping", "ATS Deduplication", "Scorecards", "Personalized Icebreakers"),
                    riskFactor = "Low",
                    estimatedMinutes = 20
                )
            }

            // Bugs, crashes, repro, DOM, browser glitches, visual tests, QA
            listOf("bug", "repro", "crash", "error", "glitch", "broken", "qa", "dom", "exception", "defect", "fail", "button not working", "console").any { query.contains(it) } -> {
                BotRecommendationResult(
                    recommendedBot = spectre,
                    matchScorePercent = 97,
                    managerialRationale = "Spectre is assigned: Autonomous headless Chrome session with DOM event tracking will reliably isolate the failure state and generate step-by-step repros.",
                    fallbackBot = turing,
                    capabilityHighlights = listOf("Headless Browser Session", "DOM Event Trapping", "Console Log Dumps", "Visual Delta Diffing"),
                    riskFactor = "Low",
                    estimatedMinutes = 12
                )
            }

            // Market intelligence, pricing, competitor, scrape, benchmark, filings
            listOf("scrape", "competitor", "pricing", "market", "benchmark", "intelligence", "filing", "trend", "sec", "scrape tables", "catalog").any { query.contains(it) } -> {
                BotRecommendationResult(
                    recommendedBot = nova,
                    matchScorePercent = 95,
                    managerialRationale = "Nova is assigned: Specialized in persistent high-volume web intelligence, anti-bot bypass protocols, and structured CSV/JSON data distillation.",
                    fallbackBot = atlas,
                    capabilityHighlights = listOf("Dynamic DOM Scraping", "Competitor Matrix", "Anti-Bot Evasion", "Automated Benchmark Export"),
                    riskFactor = "Low",
                    estimatedMinutes = 25
                )
            }

            // Terminal, bash, docker, git, deploy, scripts, logs, server, vm
            listOf("terminal", "bash", "shell", "docker", "pod", "git", "deploy", "script", "linux", "container", "database", "sql", "migration").any { query.contains(it) } -> {
                BotRecommendationResult(
                    recommendedBot = turing,
                    matchScorePercent = 98,
                    managerialRationale = "Turing is assigned: Cloud VM bash operator capable of running automated test containers, git branching, and system-level telemetry validation.",
                    fallbackBot = spectre,
                    capabilityHighlights = listOf("Isolated Cloud Bash", "Docker Daemon Control", "Git Tree Inspection", "System Health Telemetry"),
                    riskFactor = "Medium (Protected branches require code deploy approval)",
                    estimatedMinutes = 10
                )
            }

            // Sales, outreach, leads, clients, email, prospects, CRM, pipeline
            listOf("sales", "lead", "outreach", "prospect", "email", "crm", "hubspot", "apollo", "customer", "cold", "pitch").any { query.contains(it) } -> {
                BotRecommendationResult(
                    recommendedBot = atlas,
                    matchScorePercent = 94,
                    managerialRationale = "Atlas is assigned: Outbound sales specialist engineered to verify domain deliverability, enrich executive targets, and queue customized outreach drafts.",
                    fallbackBot = nova,
                    capabilityHighlights = listOf("Executive Email Verification", "HubSpot CRM Sync", "Context-Aware Copy", "Domain Health Guardrails"),
                    riskFactor = "Medium (Outbound sends intercepted by Human-in-the-Loop)",
                    estimatedMinutes = 18
                )
            }

            // Default fallback
            else -> {
                BotRecommendationResult(
                    recommendedBot = atlas,
                    matchScorePercent = 88,
                    managerialRationale = "Atlas is recommended by default for customer-facing communication and task execution, with Turing providing terminal support.",
                    fallbackBot = turing,
                    capabilityHighlights = listOf("Goal Orchestration", "Context Extraction", "Execution Handoff"),
                    riskFactor = "Low",
                    estimatedMinutes = 15
                )
            }
        }
    }

    /**
     * Decomposes a long or complex company task into a multi-phase corporate execution plan.
     */
    fun decomposeComplexTask(complexGoal: String, bots: List<BotEntity>): ComplexInitiativePlan {
        val query = complexGoal.lowercase()
        val specialists = bots.filter { !it.isManager }

        val atlas = specialists.find { it.role == BotRole.SALES_OUTBOUND } ?: bots.first()
        val lyra = specialists.find { it.role == BotRole.TALENT_SCOUT } ?: bots.first()
        val ledger = specialists.find { it.role == BotRole.INVOICE_EXPENSE } ?: bots.first()
        val spectre = specialists.find { it.role == BotRole.BUG_REPRO } ?: bots.first()
        val nova = specialists.find { it.role == BotRole.MARKET_INTELLIGENCE } ?: bots.first()
        val turing = specialists.find { it.role == BotRole.TERMINAL_OPERATOR } ?: bots.first()

        val phases = mutableListOf<ManagerInitiativePhase>()

        val title: String
        val summary: String
        val duration: Int

        when {
            query.contains("invoice") || query.contains("cloud") || query.contains("finops") || query.contains("expense") -> {
                title = "Quarterly Cloud Infrastructure & FinOps Audit"
                summary = "Manager Orion orchestrates an automated spend review: Turing inspects compute allocations, Ledger reconciles multi-vendor PDF invoices, Atlas synthesizes savings, with final human approval gate."
                duration = 45

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 1,
                        phaseName = "Phase 1: Cloud Telemetry & VM Usage Audit",
                        assignedBotId = turing.id,
                        assignedBotName = turing.name,
                        assignedBotRoleTitle = turing.role.title,
                        taskTitle = "Inspect AWS/GCP Container Pod Telemetry",
                        taskDescription = "Execute cloud terminal scripts to query running container resource usage, identify idle VM instances, and dump raw metrics to /shared_volume/cloud_usage.json.",
                        expectedArtifact = "cloud_usage.json",
                        priority = "High",
                        dependencies = "None (Initiator)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 2,
                        phaseName = "Phase 2: Vendor Invoicing & Receipt Reconciliation",
                        assignedBotId = ledger.id,
                        assignedBotName = ledger.name,
                        assignedBotRoleTitle = ledger.role.title,
                        taskTitle = "Download Vendor Statements & Code Discrepancies",
                        taskDescription = "Authenticate to AWS Billing and Stripe portals, extract monthly PDF invoices, cross-reference billing lines against Turing's usage metrics, and flag unexpected surcharges.",
                        expectedArtifact = "reconciliation_matrix.csv",
                        priority = "High",
                        dependencies = "Phase 1 (cloud_usage.json)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 3,
                        phaseName = "Phase 3: Executive Savings Memo & Vendor Disputes",
                        assignedBotId = atlas.id,
                        assignedBotName = atlas.name,
                        assignedBotRoleTitle = atlas.role.title,
                        taskTitle = "Draft Vendor Refund Claim & Executive Summary",
                        taskDescription = "Synthesize cost discrepancies into an executive presentation and draft credit-claim communications for AWS/vendor account reps.",
                        expectedArtifact = "finops_executive_memo.md",
                        priority = "Normal",
                        dependencies = "Phase 2 (reconciliation_matrix.csv)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 4,
                        phaseName = "Phase 4: Executive Gate & Human Authorization",
                        assignedBotId = ledger.id,
                        assignedBotName = ledger.name,
                        assignedBotRoleTitle = ledger.role.title,
                        taskTitle = "Authorize Vendor Dispute & Ledger Adjustments",
                        taskDescription = "Manager Orion pauses automated dispatch to require verified human sign-off on QuickBooks ledger journal adjustments and outbound dispute emails.",
                        expectedArtifact = "signed_audit_record.json",
                        priority = "Urgent",
                        requiresHumanApprovalGate = true,
                        approvalReason = "Financial journal entry and vendor refund claims require human officer sign-off.",
                        dependencies = "Phase 3 (finops_executive_memo.md)"
                    )
                )
            }

            query.contains("bug") || query.contains("incident") || query.contains("crash") || query.contains("outage") -> {
                title = "Critical Production Incident Diagnostic & Patch Verification"
                summary = "Manager Orion deploys Spectre for visual DOM reproduction, Turing for container crash logs, Nova for regression checking, and requests dev sign-off."
                duration = 30

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 1,
                        phaseName = "Phase 1: Automated Browser Reproduction",
                        assignedBotId = spectre.id,
                        assignedBotName = spectre.name,
                        assignedBotRoleTitle = spectre.role.title,
                        taskTitle = "Headless Browser Reproduction & DOM Event Trapping",
                        taskDescription = "Launch persistent VM Chrome instance, replay user event sequence, trap unhandled JavaScript exceptions, and capture screenshot artifact.",
                        expectedArtifact = "incident_repro_snapshot.png",
                        priority = "Urgent",
                        dependencies = "None (Initiator)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 2,
                        phaseName = "Phase 2: Pod Diagnostics & Stack Trace Triangulation",
                        assignedBotId = turing.id,
                        assignedBotName = turing.name,
                        assignedBotRoleTitle = turing.role.title,
                        taskTitle = "Inspect Kubernetes Pod Logs & Git Blame",
                        taskDescription = "Execute terminal commands across production pods, correlate browser crash timestamp with backend stack traces, and inspect latest commit diffs.",
                        expectedArtifact = "pod_crash_diagnostics.log",
                        priority = "Urgent",
                        dependencies = "Phase 1 (incident_repro_snapshot.png)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 3,
                        phaseName = "Phase 3: Hotfix Script Synthesis & Regression Verification",
                        assignedBotId = turing.id,
                        assignedBotName = turing.name,
                        assignedBotRoleTitle = turing.role.title,
                        taskTitle = "Draft Targeted Patch & Run Unit Test Suite",
                        taskDescription = "Apply sandbox code patch, execute test suite in Docker container, and verify that bug reproduction step in Phase 1 no longer triggers.",
                        expectedArtifact = "patch_diff.patch",
                        priority = "High",
                        dependencies = "Phase 2 (pod_crash_diagnostics.log)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 4,
                        phaseName = "Phase 4: Engineering Deployment Gate",
                        assignedBotId = turing.id,
                        assignedBotName = turing.name,
                        assignedBotRoleTitle = turing.role.title,
                        taskTitle = "Human Sign-off for Production Code Push",
                        taskDescription = "Manager Orion halts deployment. Engineering manager must verify the patch diff before it is pushed to release branch.",
                        expectedArtifact = "deploy_authorization.json",
                        priority = "Urgent",
                        requiresHumanApprovalGate = true,
                        approvalReason = "Code deployment to production requires engineering manager verification.",
                        dependencies = "Phase 3 (patch_diff.patch)"
                    )
                )
            }

            query.contains("recruit") || query.contains("talent") || query.contains("hire") || query.contains("candidate") -> {
                title = "Global Engineering Talent Pipeline & Personalized Outreach"
                summary = "Manager Orion coordinates market talent scouting: Nova maps competitor engineering rosters, Lyra enriches and deduplicates candidates, Atlas drafts outreach, and human reviews message drafts."
                duration = 40

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 1,
                        phaseName = "Phase 1: Competitor Roster Mapping",
                        assignedBotId = nova.id,
                        assignedBotName = nova.name,
                        assignedBotRoleTitle = nova.role.title,
                        taskTitle = "Map Engineering Orgs & Tech Stack Signals",
                        taskDescription = "Scrape public engineering blogs, conference speaker rosters, and GitHub organization contributors to identify prime talent targets.",
                        expectedArtifact = "target_engineers_pool.json",
                        priority = "High",
                        dependencies = "None (Initiator)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 2,
                        phaseName = "Phase 2: ATS Deduplication & Candidate Scoring",
                        assignedBotId = lyra.id,
                        assignedBotName = lyra.name,
                        assignedBotRoleTitle = lyra.role.title,
                        taskTitle = "Cross-Reference Greenhouse/Lever ATS & Score Fit",
                        taskDescription = "Filter scraped profiles against existing database to eliminate duplicate contacts, extract verified skills, and compute fit scores (0-100).",
                        expectedArtifact = "vetted_candidates.csv",
                        priority = "High",
                        dependencies = "Phase 1 (target_engineers_pool.json)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 3,
                        phaseName = "Phase 3: Hyper-Personalized Outreach Generation",
                        assignedBotId = atlas.id,
                        assignedBotName = atlas.name,
                        assignedBotRoleTitle = atlas.role.title,
                        taskTitle = "Draft Tailored Technical Value Propositions",
                        taskDescription = "Compose personalized introductory messages referencing each engineer's recent open-source repositories and relevant system architecture challenges.",
                        expectedArtifact = "outreach_campaign_queue.json",
                        priority = "Normal",
                        dependencies = "Phase 2 (vetted_candidates.csv)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 4,
                        phaseName = "Phase 4: Recruiter Sign-off Gate",
                        assignedBotId = atlas.id,
                        assignedBotName = atlas.name,
                        assignedBotRoleTitle = atlas.role.title,
                        taskTitle = "Review Candidate Outreach Drafts Before Sending",
                        taskDescription = "Manager Orion holds the campaign queue. Human recruiter must approve the personalized drafts before messages are dispatched.",
                        expectedArtifact = "authorized_campaign.json",
                        priority = "Urgent",
                        requiresHumanApprovalGate = true,
                        approvalReason = "External communications with prospective executive candidates require recruiter review.",
                        dependencies = "Phase 3 (outreach_campaign_queue.json)"
                    )
                )
            }

            // Default / Competitor / Market Strategy
            else -> {
                title = "Competitor Intelligence & Go-To-Market Assault"
                summary = "Manager Orion directs a 4-phase cross-specialist campaign: Nova extracts competitor pricing tables, Turing normalizes schemas in terminal, Atlas drafts sales rebuttals, and human signs off on final assets."
                duration = 35

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 1,
                        phaseName = "Phase 1: Dynamic Web Scraping & Pricing Extraction",
                        assignedBotId = nova.id,
                        assignedBotName = nova.name,
                        assignedBotRoleTitle = nova.role.title,
                        taskTitle = "Scrape 3 Competitor Pricing Tiers & Feature Paywalls",
                        taskDescription = "Navigate to competitor domains in persistent cloud VM, parse dynamic pricing calculators, and save structured raw metrics to /shared_volume/competitor_pricing.csv.",
                        expectedArtifact = "competitor_pricing.csv",
                        priority = "High",
                        dependencies = "None (Initiator)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 2,
                        phaseName = "Phase 2: Terminal Data Normalization & Matrix Generation",
                        assignedBotId = turing.id,
                        assignedBotName = turing.name,
                        assignedBotRoleTitle = turing.role.title,
                        taskTitle = "Run Python Pandas Cleaning Script in Cloud Bash",
                        taskDescription = "Execute normalization scripts inside the shared VM sandbox, filter currency variations, calculate feature parity ratios, and generate comparison matrix.",
                        expectedArtifact = "pricing_comparison_matrix.json",
                        priority = "High",
                        dependencies = "Phase 1 (competitor_pricing.csv)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 3,
                        phaseName = "Phase 3: Sales Collateral & Executive Battlecard Synthesis",
                        assignedBotId = atlas.id,
                        assignedBotName = atlas.name,
                        assignedBotRoleTitle = atlas.role.title,
                        taskTitle = "Draft Sales Battlecards & Objection Handling Guide",
                        taskDescription = "Synthesize feature-price gaps into actionable sales talking points, objection handles, and update CRM product pricing cheat-sheet.",
                        expectedArtifact = "sales_battlecard_v1.md",
                        priority = "Normal",
                        dependencies = "Phase 2 (pricing_comparison_matrix.json)"
                    )
                )

                phases.add(
                    ManagerInitiativePhase(
                        phaseNumber = 4,
                        phaseName = "Phase 4: Executive Gate & Human Approval",
                        assignedBotId = atlas.id,
                        assignedBotName = atlas.name,
                        assignedBotRoleTitle = atlas.role.title,
                        taskTitle = "Review & Approve Strategic Battlecards Before Team Broadcast",
                        taskDescription = "Manager Orion holds the battlecards. Product and sales leadership must sign off before the materials are pushed into the CRM company wiki.",
                        expectedArtifact = "signed_battlecard_release.json",
                        priority = "Urgent",
                        requiresHumanApprovalGate = true,
                        approvalReason = "External pricing guides and sales collateral require product leadership sign-off.",
                        dependencies = "Phase 3 (sales_battlecard_v1.md)"
                    )
                )
            }
        }

        return ComplexInitiativePlan(
            initiativeTitle = title,
            executiveSummary = summary,
            complexityRating = "High Complexity • 4 Phased Milestones • Cross-Bot Swarm",
            estimatedDurationMinutes = duration,
            phases = phases,
            contingencyPolicy = "If any specialist bot encounters an unhandled exception or rate limit, Manager Orion halts downstream dependent phases, alerts human supervisor, and automatically attempts secondary fallback routing."
        )
    }
}
