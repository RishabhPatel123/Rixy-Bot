# Rixy-Bot — AI Coworkers

A mobile command center for persistent multi-agent **AI coworker swarms** executing complex computer-use workflows on shared cloud VMs — right from your Android device.

AI Coworkers simulates a virtual workforce of specialist AI bots — led by a Company Manager named **Orion** — that research, draft, sell, audit, and operate inside cloud-VM workspaces. Dispatch tasks or multi-phase initiatives, watch live execution progress, chat with your bots, and stay in control through a human-in-the-loop approval queue before bots send emails, spend money, or pass 2FA checks.

> **Note:** Task execution is currently **simulated on-device**. With a Gemini API key configured, real AI powers task planning and swarm chat responses; everything runs in a fully functional offline-simulation mode without any keys.

## ✨ Features

- **Command Center dashboard** — live agent status, VM cluster health, task progress tracking, per-bot execution logs, and a cognitive-loop status tracker.
- **Task dispatch & AI planning** — assign tasks directly to a bot, or let Gemini auto-decompose a high-level goal into prioritized sub-tasks and route them ("Auto Orchestrate").
- **Company Manager hub** — complex-initiative decomposition into phased roadmaps, rule-based "which bot should do this?" recommendations with match scores and risk ratings, plus an org chart with supervisory directives.
- **Multi-bot swarms** — spin up teams of 2–6 bots sharing a cloud VM with a chat-style workspace, shared storage/CPU/RAM resource dashboard, and a terminal view.
- **Human-in-the-loop approvals** — bots pause before risky actions (sending email, spending money, bypassing 2FA, code deploys, CRM bulk updates) and wait for your sign-off, with a full audit history.
- **Routines** — schedule recurring bot jobs with cron-like schedules and enable toggles.
- **MCP integrations** — connect enterprise tools via Model Context Protocol server bridges (GitHub, Slack, AWS, and more).
- **Skill recording** — teach a bot a workflow by performing it once; an accessibility service records your clicks/typing into a replayable, parameterized automation trace.
- **Live-mode guardrails** — cloud VM connection testing, VM health polling, autonomous outbound toggles, and an hourly USD spend cap.

### Meet the workforce

| Bot | Role |
|-----|------|
| **Orion** | Company Manager & Chief Orchestrator |
| **Atlas** | Sales Outbound |
| **Lyra** | Talent Scout |
| **Ledger** | Invoice & Expense Manager |
| **Spectre** | Bug Reproduction |
| **Nova** | Market Intelligence |
| **Turing** | Dev & Terminal Operator |

## 🛠 Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3), Navigation Compose |
| Architecture | Single-activity, ViewModel + Repository, Koin for DI |
| Persistence | Room (KSP) |
| Background work | WorkManager (`CoroutineWorker`, periodic sync) |
| Networking | OkHttp (+ logging interceptor), `org.json` |
| AI | Google Gemini API (`generateContent`, REST) with configurable model |
| Automation | Android `AccessibilityService` for skill trace recording |
| Testing | JUnit, Robolectric, Roborazzi (screenshot tests), Compose UI tests |
| Build | Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10 — minSdk 24, target SDK 36 |

## 🚀 Getting started

### Prerequisites

- Android Studio (Ladybug or newer recommended)
- JDK 17
- An Android device or emulator running API 24+

### Build & run

```bash
git clone https://github.com/RishabhPatel123/Rixy-Bot.git
cd Rixy-Bot
```

1. Create a `local.properties` pointing at your Android SDK (Android Studio does this automatically):
   ```properties
   sdk.dir=C\:\\path\\to\\Android\\Sdk
   ```
2. *(Optional)* Provide a Gemini API key — copy `.env.example` to `.env` and set:
   ```properties
   GEMINI_API_KEY=your_real_key_here
   ```
3. Build and install:
   ```bash
   ./gradlew :app:assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

Or simply open the project in Android Studio and press **Run**.

> **No keys required to try it:** without a Gemini key or Cloud VM configuration the app runs in offline simulation mode — task planning falls back to a canned local plan and bots reply with a notice that they're simulating. A debug keystore is auto-generated (or create `debug.keystore` in the repo root: storepass `android`, alias `androiddebugkey`).

### In-app configuration

The **Settings** tab lets you configure at runtime (no rebuild needed):

- **Cloud VM** — host/IP, SSH port, region, bearer token, browser-sandbox endpoint (Playwright/Puppeteer), vCPU/RAM allocation, and a "Ping & Verify" connection test.
- **API credentials** — Gemini API key + model override (with a live "Test" button), GitHub PAT, Stripe key, outreach key, APM key, and a local LLM base URL.
- **Live mode** — toggle real execution, VM health polling, autonomous outbound actions, and set an hourly spend cap.

## 📁 Project structure

```
app/src/main/java/com/example/
├── CoworkerApplication.kt    # Koin application setup
├── MainActivity.kt           # Single activity, nav host, background sync
├── data/
│   ├── dao/                  # Room DAOs
│   ├── database/             # Room database
│   ├── model/                # Room entities (Bot, Swarm, Task, Approval, Skill, ...)
│   └── repository/           # CoworkerRepository (+ initial seeding)
├── di/                       # Koin modules
├── domain/                   # Orchestration engine (bot routing, initiative plans)
├── network/                  # GeminiApiService, network monitor
├── service/                  # AccessibilityService skill recorder
├── ui/
│   ├── dashboard/            # Command Center
│   ├── approvals/            # Human-in-the-loop sign-off queue
│   ├── swarms/               # Swarm workspace + resource dashboard
│   ├── routines/             # Routines & MCP servers
│   ├── manager/              # Company Manager hub
│   ├── planner/              # Task planner (Auto Orchestrate / Direct Handoff)
│   ├── skills/               # Skill library (recorded automation traces)
│   └── settings/             # System & integration settings
└── worker/                   # WorkManager background sync
```

## 🗺 Roadmap ideas

- Wire up the Skill Library screen (recording engine is implemented).
- Real Cloud VM execution via the configured SSH/browser-sandbox endpoints.
- Firebase AI SDK migration (dependencies are already present).

## 📄 License

All rights reserved by the repository owner.
