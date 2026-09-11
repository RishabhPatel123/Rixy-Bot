# Rixy

A Grok-style, chat-first Android assistant powered by Google Gemini. Bring your own API key — your conversations stay on your phone and are sent only to Gemini.

Rixy streams replies in real time, generates images, understands photos you attach, answers with live web sources, speaks and listens, and can plan any goal into actionable tasks.

## ✨ Features

- **Chat-first UI** — dark, minimal, monochrome design with a navigation drawer for chat history; message bubbles, streaming responses with a live cursor, liquid-motion animations, and a stop button.
- **Streaming Gemini chat** — real-time token streaming (`streamGenerateContent` SSE) with multi-turn conversation history, not canned replies.
- **Image generation** — flip to Image mode, describe a picture, and Rixy generates it (Gemini image model) with a save-to-gallery button. Attach a photo to use it as a reference.
- **Photo analysis** — attach an image from your library and ask about it; the picture rides along in the multimodal request.
- **Web-grounded answers** — toggle Web mode and replies cite their Google Search sources as tappable chips.
- **Voice** — tap the mic to speak your message (Android speech recognition), tap the speaker on any reply to hear it read aloud (TTS).
- **Plan mode** — describe a goal and get 3–6 prioritized sub-task cards; save them to the Tasks screen with one tap.
- **Import conversations** — paste or pick a ChatGPT/Grok/Gemini transcript and continue it in Rixy.
- **Markdown rendering** — bold/italic, inline code, bullet and numbered lists, headings, and fenced code blocks with one-tap copy.
- **First-run key onboarding** — welcome screen with a live "test key" button; skippable with an in-chat banner reminder.
- **Private by design** — the API key is stored in [EncryptedSharedPreferences](https://developer.android.com/privacy-and-security/cryptography) (Android Keystore), backups are disabled, and there is no analytics, telemetry, or third-party network call. The only network peer is `generativelanguage.googleapis.com`.

**Permissions:** Internet (Gemini API) and microphone (voice input, requested at first use, optional). Photo picking uses the privacy-friendly system photo picker — no photo library permission.

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
