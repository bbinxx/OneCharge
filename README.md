# OneCharge

OneCharge is a Pixel-quality, highly optimized Android application that tracks your true Screen-On Time (SOT) across an entire battery cycle. Unlike standard battery trackers that reset daily or on every minor charge, OneCharge accurately measures the lifespan of your battery from 100% to 0%.

## Features
- **True Cycle Tracking:** Tracks your device from full charge until depletion.
- **Zero Battery Drain:** Uses mathematically perfect background tracking without holding wake locks or polling the OS.
- **Real-time Dashboard:** Live-ticking SOT calculation when viewing the dashboard.
- **Glance App Widget:** A perfectly synced home screen widget to monitor your SOT at a glance.
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
