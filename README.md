# VoidCore — Autonomous Offline-First Android AI Assistant

VoidCore is a self-contained, privacy-preserving Android AI assistant powered by a native embedded neural engine, dynamic particle visual renderer, 4-tier deterministic router, deep device automation suite, real-time notification intelligence, call screening proxy, and interactive 6:00 AM Morning Briefing engine.

VoidCore operates **100% offline-first** with zero external dependencies (no Termux, no local Python servers, no mandatory cloud backends).

---

## Key Features

- **Embedded Local Neural Intelligence**: Supports on-device LLM runtimes (Lite 1.5B, Balanced 3B, Pro 7B) running locally in app-private storage with real-time token streaming and strict JSON intent parsing.
- **4-Tier Deterministic-First Routing**:
  - **Tier 1 (Deterministic)**: Instant (0ms, 0 tokens) execution for device controls (flashlight, volume, brightness, timers, alarms, media).
  - **Tier 2 (Ambiguous Action Phrases)**: Routes to embedded local model to synthesize structured JSON intents with parameter verification.
  - **Tier 3 (Local Conversation)**: Fluid conversational dialogues handled directly on-device by local model runtime.
  - **Tier 4 (Complex Web Queries)**: Isolated cloud fallback (Gemini API) strictly isolated from device execution tools.
- **Interactive Void Core Renderer**: Dynamic OpenGL/Canvas dual-mode particle visualizer reacting to assistant states (IDLE, LISTENING, THINKING, SPEAKING, EXECUTING, ALERT).
- **Accessibility Automation Engine**: Non-intrusive UI automation supporting smart node finding, UI tapping, typing, scrolling, and screen content extraction.
- **Notification Intelligence Hub**: Priority classification, automated summary generation, and interactive quick replies.
- **Call Screening & AI Proxy**: Autonomous call screening assistant identifying itself clearly to callers, recording transcriptions, and scheduling callback reminders without impersonation.
- **6:00 AM Morning Briefing Call**: Daily exact-alarm automation presenting a full-screen, lock-screen interactive voice briefing covering weather, schedules, tasks, notifications, and device vitals.

---

## Technical Specifications

- **Minimum Android Version**: Android 8.0 (API Level 26, `Oreo`)
- **Target Android Version**: Android 16 (API Level 36)
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM + Clean Architecture + Unidirectional Data Flow (UDF)
- **Persistence**: Room Database (SQLite) + Encrypted Preferences

---

## Required Permissions & Services

VoidCore declares least-privilege permissions configured with user consent flows:

| Permission / Service | Purpose |
| :--- | :--- |
| `POST_NOTIFICATIONS` | Delivering priority alerts and background task statuses. |
| `RECORD_AUDIO` | Voice recognition, wake word detection, and audio laboratory. |
| `CAMERA` | Vision OCR, real-time object detection, and visual QA. |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Reliable 6:00 AM Morning Briefing triggering. |
| `SYSTEM_ALERT_WINDOW` | Full-screen wake overlay for incoming morning briefing calls. |
| `READ_PHONE_STATE` / `MANAGE_OWN_CALLS` | Call state awareness and screening proxy management. |
| `VoidAccessibilityService` (`BIND_ACCESSIBILITY_SERVICE`) | Device UI tree inspection, UI clicks, text typing, and screen scrolling. |
| `VoidNotificationListenerService` (`BIND_NOTIFICATION_LISTENER_SERVICE`) | Intercepting, prioritizing, and summarizing device notifications. |

---

## Build Instructions

### Prerequisites
- **Java Development Kit**: JDK 17 (Eclipse Temurin or Zulu recommended)
- **Android SDK**: Platforms 26 through 36, Build-Tools 36.0.0+
- **Gradle**: Managed via included Gradle Wrapper (`gradlew`)

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd VoidCore
   ```

2. **Initialize Environment File**:
   ```bash
   cp .env.example .env
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Run Android Lint**:
   ```bash
   ./gradlew lintDebug
   ```

5. **Assemble Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The compiled APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## Local AI Model Setup & Lifecycle

VoidCore does **not** bundle multi-gigabyte neural network binaries inside the Git repository. Models are managed dynamically on-device:

### Supported Model Profiles
1. **Lite (1.5B)**: ~420 MB RAM footprint, 1.1 GB storage. Optimized for low-end devices and battery preservation.
2. **Balanced (3B)**: ~780 MB RAM footprint, 2.1 GB storage. Ideal balance of conversational depth and speed.
3. **Pro (7B)**: ~1,450 MB RAM footprint, 3.6 GB storage. High reasoning fidelity for complex on-device parsing.

### In-App Download & Management
- Navigate to **Offline Performance / AI Models** screen in the application.
- Select your target model profile and tap **Download**.
- Real-time download progress, download speed, and estimated time remaining (ETA) are rendered live.
- Downloads are validated via **SHA-256 Checksum** and verified against device storage limits (`StatFs`) before extraction into app-private storage (`context.filesDir/models/`).
- Users can load, unload, pause, resume, or delete model binaries at any time.

---

## Optional Cloud Provider Setup (Gemini API)

While VoidCore functions 100% offline, users can optionally enable Gemini API for general web intelligence queries:

1. Obtain a Gemini API Key from Google AI Studio.
2. In the AI Studio build interface, configure `GEMINI_API_KEY` in the **Secrets** panel, or set `GEMINI_API_KEY=your_key_here` in your local `.env` file.
3. In the VoidCore **AI Providers** settings screen, toggle Cloud Fallback ON or OFF.
4. *Security Guarantee*: Cloud fallback is strictly restricted from executing device actions or tools. All action phrases remain strictly governed by local security gates.

---

## System Services Setup

### 1. Accessibility Service Setup
1. Open Android **Settings** > **Accessibility** (or **Installed Apps** / **Accessibility**).
2. Locate and tap **VoidCore Assistant Service**.
3. Toggle the switch to **ON** and grant requested permissions.
4. Return to VoidCore; the UI Automation engine is now active.

### 2. Notification Listener Setup
1. Open Android **Settings** > **Apps & Notifications** > **Special App Access** > **Notification Access**.
2. Locate **VoidCore Notification Hub** and toggle **Allow**.
3. VoidCore will now classify incoming notifications and present priority summaries.

---

## Supported Android Actions & Tool Capabilities

- **Device Controls**: Flashlight/Torch toggle, media volume, screen brightness adjustment.
- **Time & Alarms**: Set countdown timers, create alarms, schedule calendar reminders.
- **Media & Music**: Play, pause, skip, and resume media sessions.
- **App Management**: Launch installed applications by name or package identifier.
- **Search & Files**: Query device documents, local files, and launch web queries.
- **Clipboard Management**: Inspect clipboard content and copy designated text safely.
- **UI Automation (Accessibility)**: Find on-screen UI elements, perform taps, input text fields, and scroll lists.
- **Call Management**: Active call screening, automated caller assistance, transcript generation, and callback scheduling.
- **Morning Briefing**: Spoken briefing with weather, calendar, reminders, overnight message summaries, and device health.

---

## Known Platform Limitations

- **Headless Environments**: Audio microphone recording and hardware camera capture require physical Android hardware or emulators with virtual sensor routing.
- **Call Screening Role**: Automatic call redirection requires Android Telecom Role support (`RoleManager.ROLE_CALL_SCREENING`); unsupported devices automatically fall back to assisted manual relay.
- **Exact Alarms (Android 12+)**: Requires granting the `Alarms & Reminders` permission in system settings for second-precise morning wakeups.

---

## Obtaining the APK from GitHub Actions

Every push or pull request to `main` triggers an automated CI workflow:

1. Navigate to the **Actions** tab on your GitHub repository.
2. Select the latest workflow run named **Android CI Build & Test**.
3. Scroll down to the **Artifacts** section at the bottom of the summary page.
4. Download the **`voidcore-debug`** artifact.
5. Extract the downloaded ZIP file to obtain `app-debug.apk`.
6. Transfer and install `app-debug.apk` onto your Android device.
