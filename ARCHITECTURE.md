# VoidCore System Architecture Specification

This document provides a technical overview of VoidCore's core architecture, internal state machines, security policies, and execution pipelines.

---

## 1. High-Level Architecture Overview

VoidCore is architected as an **offline-first, zero-cloud-mandatory Android cognitive system**. It couples an embedded on-device neural runtime with deterministic hardware and UI automation pipelines, strictly governed by a 3-tier Security Gate and immutable audit ledger.

```
+-----------------------------------------------------------------------------------+
|                               VOIDCORE USER INTERFACE                             |
|    +-----------------------------+       +-----------------------------------+    |
|    | VoidParticle Visualizer     | <---> | Assistant State Machine (UDF)     |    |
|    | (OpenGLES 3.0 / Canvas)     |       | (IDLE, LISTENING, THINKING, etc.) |    |
|    +-----------------------------+       +-----------------------------------+    |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                            4-TIER TASK ROUTER                                     |
|                                                                                   |
|  [Tier 1: Deterministic]   --> Instant Hardware/OS Execution (0ms, 0 tokens)      |
|  [Tier 2: Ambiguous]       --> Embedded Local Model Strict JSON Intent Synthesis  |
|  [Tier 3: Conversation]    --> Embedded Local Model Streaming Token Generation    |
|  [Tier 4: Complex Web]     --> Cloud Fallback (Gemini API) [Tool-Execution Isolated]
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                        SECURITY GATE & RISK EVALUATION                            |
|                                                                                   |
|  - LOW RISK: Auto-execute immediately with background ledger logging             |
|  - MEDIUM RISK: Passive confirmation / non-destructive UI toast                   |
|  - HIGH RISK / DANGEROUS: Mandatory Explicit Biometric/PIN Modal Confirmation      |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                            CORE EXECUTION SUBSYSTEMS                              |
|                                                                                   |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  | Android Tool       |  | Accessibility Engine |  | Notification Intelligence |  |
|  | Executor           |  | (UI Tap, Type, OCR)  |  | (Triage & Quick Action)   |  |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  | Call Screening     |  | 6:00 AM Morning     |  | Memory Vault              |  |
|  | Intelligence Proxy |  | Briefing Engine      |  | (Room + Vector Embeddings)|  |
|  +--------------------+  +----------------------+  +---------------------------+  |
+-----------------------------------------------------------------------------------+
```

---

## 2. Core Subsystems

### 2.1. Assistant State Machine
The core visualizer and voice interaction pipeline operate as a strict Unidirectional Data Flow (UDF) state machine:
- `IDLE`: Calm breathing particle motion, low power consumption, standing by for hotword/tap.
- `LISTENING`: Inward particle gravity vortex, capturing user voice audio with live decibel amplitude feedback.
- `THINKING`: High-speed orbital accretion ring, computing local neural tokens or parsing deterministic commands.
- `SPEAKING`: Resonant harmonic waveform emitting audio pulses synchronized to TTS audio output.
- `EXECUTING`: Radiant pulse indicating successful dispatch to the Android Tool Executor or Accessibility Engine.
- `ALERT / BLOCKED`: Crimson shockwave expansion signifying a High-Risk security challenge requiring biometric/user consent.

### 2.2. 4-Tier Task Router & Routing Pipeline
1. **Tier 1 (Deterministic Parser)**: High-speed regex and keyword parsing (`DeterministicCommandParser.kt`) matching standard device commands (flashlight, volume, brightness, timer, alarm, media). Executes in 0ms with zero model load.
2. **Tier 2 (Ambiguous Action Phrases)**: Ambiguous requests route to the embedded on-device model (`LocalModelRuntime.kt`) constrained by grammar rules to synthesize strict JSON intents.
3. **Tier 3 (Local Conversation)**: General user conversational turns stream tokens locally on-device.
4. **Tier 4 (Complex Web Queries)**: Optional cloud fallback via Gemini API, strictly sandboxed with zero tool-execution privileges.

### 2.3. Embedded Local AI Model Runtime
- **Architecture**: Decoupled `ILocalModelRuntime` interface supporting on-device neural network execution.
- **Model Profiles**:
  - `Lite (1.5B)`: Fast, energy-efficient on-device parameter model.
  - `Balanced (3B)`: Balanced conversational agent with high reasoning capabilities.
  - `Pro (7B)`: High-fidelity reasoning and complex multi-parameter JSON synthesis.
- **Storage & Memory Guardrails**: Storage checks via `StatFs`, SHA-256 binary validation, and real-time device RAM telemetry.

### 2.4. Security Gate & Risk Ledger
- Every structured intent is intercepted by `SecurityGate.kt` before hardware invocation.
- Risk Classification:
  - `LOW`: Flashlight, volume, media playback, timer setting, read-only search.
  - `MEDIUM`: Clipboard write, app launching, screen navigation.
  - `HIGH`: Phone calls, SMS sending, setting modifications, device shutdowns, file deletions.
- All actions generate an immutable cryptographically timestamped audit entry in the local Room database (`SecurityLedgerDao`).

### 2.5. Accessibility Service Engine (`VoidAccessibilityService`)
- Provides system-level interaction capabilities on Android:
  - **Node Traversal**: Recursively scans visible window hierarchies for accessibility nodes.
  - **Smart Target Matching**: Locates buttons, text fields, and icons by text, content description, or view ID.
  - **Simulated Gestures**: Dispatches `AccessibilityNodeInfo.ACTION_CLICK`, text input via `ACTION_SET_TEXT`, and scrolling actions.

### 2.6. Notification Intelligence Subsystem (`VoidNotificationListenerService`)
- Intercepts incoming status bar notifications.
- Performs priority classification (Urgent, Communication, Informational, Low).
- Generates conversational notification summaries and executes direct inline replies.

### 2.7. Call Intelligence & AI Screening Proxy
- `CallIntelligenceManager.kt` monitors incoming telephony calls.
- In screening mode, it synthesizes an automated assistant audio response:
  - Discloses assistant identity: *"Hi, I'm VoidCore, Arif's assistant..."*
  - Relays user-configured messages without user impersonation.
  - Automatically schedules follow-up callback reminders (30-minute default) and logs audit records.

### 2.8. 6:00 AM Morning Briefing Call Engine
- Scheduled via `AlarmManager.RTC_WAKEUP` using exact alarm APIs.
- Broadcast receiver awakens the system into a high-priority, full-screen lock-screen overlay.
- `BriefingDataBuilder.kt` compiles an interactive spoken audio report:
  1. Date/Time & personalized greeting
  2. Weather forecast and weather alerts
  3. First calendar events & schedule conflicts
  4. Pending tasks & reminders due today
  5. Important overnight messages & missed calls
  6. Battery level & hardware health
  7. Focus summary ("Top 3 things needing attention")
- Interactive voice control allows skipping, snoozing, rescheduling, or stopping the briefing on command.

### 2.9. Vision Engine
- CameraX live preview integration with on-device OCR and visual analysis.
- Enables visual question answering, document scanning, and scene inspection.

### 2.10. Automation & Cron Pipeline
- Background automation scheduler powered by Room database entities and Android `WorkManager` / `AlarmManager`.
- Supports recurring cron expressions, trigger conditions, and chained automated tasks.

### 2.11. Memory Vault (Local Knowledge Store)
- Persistent semantic and episodic memory stored in local Room database tables.
- Supports tag indexing, importance weighting, and local semantic recall.

### 2.12. Void Core Particle Renderer
- Dual-mode renderer featuring an OpenGLES 3.0 shader backend and fallback Jetpack Compose Canvas particle system.
- Real-time physics simulation with dynamic attraction, orbital velocity, noise perturbations, and audio-reactive harmonics.

### 2.13. Voice & Audio Laboratory
- Android `TextToSpeech` and `AudioRecord` integration.
- Configurable voice pitch, speech rate, custom audio identities, and real-time audio visualization waveforms.
