# NOVA — Native Offline Android Voice Assistant

NOVA is a production-quality, native Android voice assistant inspired by Apple Siri, featuring a fluid animated interface, audio-reactive visual effects, and a private, 100% offline rule-based command engine.

NOVA operates completely on-device without cloud AI services, external LLMs, or remote tracking servers.

---

## Architecture Overview

NOVA is built according to **Clean Architecture** and **MVVM** principles using native Kotlin and modern Android Jetpack libraries.

```text
com.nova.assistant
│
├── data
│   ├── database        # Room Database (conversations table, DAO)
│   ├── datastore       # DataStore Preferences (settings, sensitivity, pitch/speed)
│   ├── repository      # ConversationRepository
│   └── models          # AssistantState, DeviceStatus, LocationData
│
├── domain
│   ├── intent          # NovaIntent definitions
│   ├── entity          # Structured entity extraction (percentages, units, numbers, contacts)
│   ├── matcher         # Deterministic regex and keyword pattern matcher
│   ├── parser          # Compound command splitting & wake-word stripper
│   ├── executor        # Hardware execution and response builder
│   └── context         # Multi-turn conversation context tracking
│
├── speech
│   ├── wakeword        # Lightweight local keyword spotter for "Hey Nova"
│   ├── recognition     # Vosk offline speech recognition engine
│   ├── tts             # Android TextToSpeech offline output wrapper
│   └── audio           # AudioCaptureManager & AudioVisualizerProcessor (RMS amplitude)
│
├── service             # NovaListeningService (Foreground microphone service with persistent notification)
│
├── device              # Hardware controllers (Flashlight, Volume, Brightness, Battery, Storage, GPS, Connectivity)
│
├── apps                # Launcher, Contacts provider query, Phone dialer, SMS & WhatsApp deep-links
│
└── ui
    ├── animations      # NovaOrbCanvas (Fluid harmonic waves, glow bloom, floating particle field)
    ├── components      # Translucent GlassmorphicCard, custom indicators
    ├── home            # Voice assistant centerpiece screen
    ├── dashboard       # Live device hardware & system metrics dashboard
    ├── history         # Room local conversation history
    ├── settings        # Voice preferences, sensitivity, mute & reset controls
    ├── voice           # Pitch & speed customization with test playback
    ├── permissions     # Zero-cloud privacy dashboard & permission manager
    ├── modelsetup      # Offline Vosk speech model guide & loader
    └── theme           # Cinematic dark theme, typography, Siri-inspired glow palette
```

---

## Key Features

1. **Wake Word Detection ("Hey Nova")**:
   - Hands-free background listening via a dedicated Android Foreground Service (`NovaListeningService`).
   - Compliant with Android 10+ background microphone requirements with a persistent notification.
   - Low CPU and battery consumption when idle.

2. **Siri-Inspired Fluid Animated Orb**:
   - Implemented using Jetpack Compose `Canvas`, `Animatable`, and `InfiniteTransition`.
   - Real audio-reactive dynamics modulating the orb's radius and harmonic wave deformations based on microphone amplitude and TTS speech.
   - Distinct, state-driven visual behaviors for all 9 states: `IDLE`, `WAKE_WORD_LISTENING`, `WAKE_WORD_DETECTED`, `COMMAND_LISTENING`, `PROCESSING`, `EXECUTING`, `SPEAKING`, `ERROR`, `PAUSED`.
   - Ambient cosmic particle field and battery-saving mode option.

3. **Rule-Based Command Engine (Zero Cloud / Zero LLM)**:
   - Keyword spotting, regex matching, entity extraction, and command templates.
   - Compound command execution in a single sentence:
     * *"Turn on flashlight and increase volume"*
     * *"Turn off the torch, open WhatsApp"*
   - Context-aware follow-up handling:
     * User: *"What is my battery percentage?"* -> NOVA: *"Your battery is at 78 percent."*
     * User: *"What about my storage?"* -> NOVA: *"You have 24 GB of available storage."*

4. **Real Hardware Device Controls**:
   - **Flashlight**: CameraManager torch toggling with hardware verification.
   - **Volume**: AudioManager adjustments (increase, decrease, mute, set percentage).
   - **Brightness**: System brightness adjustment with permission flow.
   - **Battery**: Real-time percentage, charging state, and charger type detection.
   - **Storage**: StatFs internal disk capacity and remaining GB calculation.
   - **Wi-Fi & Bluetooth**: Connection status checks and direct settings intents.
   - **Network**: Internet reachability detection.

5. **GPS & Location Support**:
   - FusedLocationProviderClient coordinates retrieval (Latitude, Longitude, Accuracy, Altitude).
   - One-tap intent to open coordinates directly in Google Maps.

6. **Application Launching & Communication**:
   - Smart launcher for installed apps (WhatsApp, YouTube, Chrome, Gmail, Camera, Gallery, Calculator, etc.).
   - Contact resolution via Android Contacts Provider.
   - Action dialer, prefilled SMS messages, and WhatsApp deep-links.

7. **Local Utilities**:
   - Local arithmetic calculations (*"calculate 250 multiplied by 5"*, *"what is 20 percent of 500"*).
   - Unit conversions (length, weight, temperature).
   - Clock intents for alarms and countdown timers.

---

## Offline Speech Model Setup (Vosk)

NOVA uses the Vosk offline speech recognition engine:

1. Download the lightweight English model package:
   - [vosk-model-small-en-us-0.15](https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip) (~40 MB)
2. Extract the archive.
3. Push or copy the contents into the device directory:
   `/Android/data/com.nova.assistant/files/model/`
4. In NOVA, navigate to **Settings > Offline Speech Model Setup** and tap **Verify & Load Model**.
5. Once loaded, NOVA performs 100% offline speech recognition, even in Airplane Mode.

---

## Automated CI Build with GitHub Actions

The repository includes a ready-to-run GitHub Actions workflow located at `.github/workflows/build.yml`.

### Workflow Capabilities:
- Runs automatically on every push or pull request to `main` or `master`.
- Can be triggered manually via GitHub **Run workflow** (`workflow_dispatch`).
- Sets up JDK 17 with Gradle caching.
- Executes all unit tests (`testDebugUnitTest`).
- Builds the debug APK (`assembleDebug`).
- Uploads the generated APK as a downloadable artifact named `nova-assistant-debug-apk`.

---

## Building Locally

### Prerequisites
- JDK 17
- Android SDK (API 34, Build Tools 34.0.0)

### Commands
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```
The resulting APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
