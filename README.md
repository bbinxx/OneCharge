# OneCharge

OneCharge is a Pixel-quality, highly optimized Android application that tracks your true Screen-On Time (SOT) across an entire battery cycle. Unlike standard battery trackers that reset daily or on every minor charge, OneCharge accurately measures the lifespan of your battery from 100% to 0%.

## Features
- **True Cycle Tracking:** Tracks your device from full charge until depletion.
- **Battery-conscious Updates:** Event-driven tracking with one Android-scheduled widget refresh every 15 minutes while widgets are installed; refreshes may be deferred by Android.
- **Real-time Dashboard:** Live-ticking SOT calculation when viewing the dashboard.
- **Glance App Widget:** Displays current battery and SOT snapshots without opening the app. Usage access is needed to recover screen history after the process exits.
- **Material 3 Design:** A sleek, dynamic-color compatible UI built with Jetpack Compose.
- **Detailed History:** Logs all your completed battery cycles with analytics like Battery Drain Per Hour.

## Architecture & Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture + MVVM
- **Dependency Injection:** Dagger Hilt
- **Database:** Room (SQLite)
- **Background Work:** Foreground Services & BroadcastReceivers
- **Widgets:** Jetpack Glance

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- Android SDK 34+.

### Building Locally
The easiest way to build and run the app is using the included `run.sh` script, which handles KSP cache issues, compiles the debug APK, and streams logcat directly to your terminal:
```bash
./run.sh
```

Alternatively, use standard Gradle:
```bash
./gradlew assembleDebug
```

## License
MIT License

## Contributing

Agents must read [AGENTS.md](AGENTS.md). Setup, verification, troubleshooting, and the required `dev` → task branch → PR workflow are documented in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).
