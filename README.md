# Rixy

A Grok-style, chat-first Android assistant powered by Google Gemini. Bring your own API key — your conversations stay on your phone and are sent only to Gemini.

Rixy streams replies in real time, renders markdown (bold, lists, fenced code blocks with copy buttons), and includes a **Plan mode** that decomposes any goal into prioritized, actionable tasks you can save to a built-in task list.

## ✨ Features

- **Chat-first UI** — dark, minimal, monochrome design with a navigation drawer for chat history; message bubbles, streaming responses with a live cursor, and a stop button.
- **Streaming Gemini chat** — real-time token streaming (`streamGenerateContent` SSE) with multi-turn conversation history, not canned replies.
- **Markdown rendering** — bold/italic, inline code, bullet and numbered lists, headings, and fenced code blocks with one-tap copy.
- **Plan mode** — toggle it on, describe a goal, and Rixy returns 3–6 prioritized sub-tasks as cards; save them to the Tasks screen with one tap.
- **Tasks screen** — planned tasks with priority chips, done/dismiss/reopen states, and delete.
- **First-run key onboarding** — welcome screen with a live "test key" button and a link to get a free key; skippable with an in-chat banner reminder.
- **Chat management** — create, rename, delete chats; delete-all with confirmation.
- **Private by design** — the API key is stored in [EncryptedSharedPreferences](https://developer.android.com/privacy-and-security/cryptography) (Android Keystore), backups are disabled, and there is no analytics, telemetry, or third-party network call. The only network peer is `generativelanguage.googleapis.com`.
- **Model picker** — switch between Gemini models (2.5 Flash/Pro, 2.0 Flash, and more) at runtime.

## 🛠 Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3), Navigation Compose, custom dark theme & typography scale |
| Architecture | Single-activity, ViewModel-per-feature, Koin DI |
| Persistence | Room (KSP), schema exported, indices on message/task foreign keys |
| Secrets | EncryptedSharedPreferences (Android Keystore), `allowBackup=false` |
| Networking | OkHttp — SSE streaming, `x-goog-api-key` header, typed error taxonomy |
| Testing | JUnit, Robolectric, coroutines-test |

No Firebase, no WorkManager, no ads SDKs — the dependency list is deliberately tiny.

## 🚀 Getting started

### Prerequisites

- Android Studio (latest stable recommended)
- JDK 17
- A device or emulator running API 24+

### Build & run

```bash
git clone https://github.com/RishabhPatel123/Rixy-Bot.git
cd Rixy-Bot
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and press **Run**.

### API key

On first launch, Rixy asks for your Gemini API key ([get one free here](https://aistudio.google.com/apikey)). The key can also be provided at build time via a `.env` file in the repo root:

```properties
GEMINI_API_KEY=your_key_here
```

(copy `.env.example` to `.env`). A key entered in the app always wins over the build-time one. You can change the key and model any time in **Settings**.

### Project structure

```
app/src/main/java/com/rixy/bot/
├── RixyApplication.kt        # Koin setup
├── MainActivity.kt           # Single activity, edge-to-edge
├── data/
│   ├── db/                   # Room database + DAOs (chats, messages, tasks)
│   ├── model/                # Room entities
│   ├── prefs/                # SecretsStore (encrypted key/model storage)
│   └── repo/                 # ChatRepository (transactional writes)
├── di/                       # Koin modules
├── network/                  # GeminiApiService (SSE streaming), parser, typed errors
└── ui/
    ├── theme/                # Monochrome palette, typography, shapes, spacing grid
    ├── components/           # Buttons, fields, banners, markdown renderer, chat bubbles
    ├── chat/                 # Chat screen: message list, plan cards, input bar
    ├── onboarding/           # First-run key setup
    ├── tasks/                # Saved planned tasks
    ├── settings/             # Key, model, data management, about
    └── viewmodel/            # ChatViewModel, SettingsViewModel
```

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```

Covers the Gemini SSE/JSON parsing, plan extraction (including fenced JSON and priority normalization), and chat-title derivation.

## 📄 License

All rights reserved by the repository owner.
