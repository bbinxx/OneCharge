# Development guide

## Setup and architecture

Use the checked-in Gradle wrapper. Daemon criteria request JDK 21; app bytecode targets Java 17. Install the Android SDK matching `app/build.gradle.kts` (currently API 36.1), accept SDK licenses, and set the machine-local `sdk.dir` in ignored `local.properties`. Dependency versions live in `gradle/libs.versions.toml`. The app requires Android 15 (API 35) or newer.

`domain` contains models, repository interfaces, and use cases. `data` implements Room, DataStore, usage access, tracking, receivers, and the foreground service. `di` binds dependencies. `ui` contains Compose screens, ViewModels, navigation, themes, and the Glance widget. `OneChargeApp` registers battery/screen receivers; `BatteryCycleTracker` updates cycles; the widget reads the same repository.

## Branch lifecycle

Start with a clean or explicitly understood working tree:

```bash
git status --short
git fetch origin
git switch dev
git merge --ff-only origin/dev
git switch -c fix/short-description
```

Use a separate branch for each independent fix or feature. Documentation-only work uses `docs/<topic>`. `dev` is the only permanent branch; delete temporary task branches after verified integration. Never develop directly on `dev`. If local changes exist, preserve them and avoid mixing them into a task commit. When remote `dev` is first bootstrapped, an authorized maintainer publishes it with `git push -u origin dev` and sets repository PR defaults/protection separately. Remove legacy local branches only after confirming they contain no unique commits; deleting GitHub branches requires explicit scope approval.

Before opening a PR, fetch and merge `origin/dev` into the task branch, resolve conflicts, rerun relevant checks, and review `git diff --check` and the final diff. PRs target `dev` and describe the problem, resulting behavior, tests, and remaining limitations. After an approved merge, fetch and fast-forward local `dev`. Keep `dev` updated after every integration; open work stays on its own branch. Release validated commits on `dev` using an authorized version tag and the existing release workflow. Do not force push shared history or publish releases without authorization.

## Implement and verify

1. Reproduce the issue and capture evidence before changing code when a device is available. Inspect existing user edits and isolate the affected path.
2. Make a focused change that preserves data and follows existing boundaries. Check startup, permission denial, lifecycle transitions, and background behavior for affected features.
3. Add meaningful regression coverage for logic changes. Replace neither real verification nor bug reproduction with template example tests.
4. Run the relevant checks:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
git diff --check
```

For release/R8 changes, also assemble the release variant and smoke-test that APK. Release currently uses debug signing; production signing must be configured before distribution. Never commit keystores, passwords, local SDK paths, APKs, build output, or private device logs.

## App and widget troubleshooting

```bash
adb devices
adb logcat -b crash -d
adb shell am start -W -n com.bbinxx.onecharge/.MainActivity
adb logcat -d -s AndroidRuntime OneChargeReceiver
```

Install the locally built APK on a test device using `adb install -r app/build/outputs/apk/debug/app-debug.apk` when installation is authorized. Preserve history; do not uninstall or clear app data as a routine fix. Capture the stack trace and build version. Use the matching R8 mapping for obfuscated release failures. Do not claim the crash is resolved without matching evidence.

Manual regression checklist:

- Launch with usage access granted and denied; return from Settings and reopen after normal process death.
- Create/restart a cycle, lock/unlock, connect/disconnect power, and compare dashboard, notification, history, and widget SOT/battery readings.
- Enable/disable persistent notifications, including notification permission denied. Reboot and confirm expected restoration.
- Add and resize the widget at each supported size, test empty history, start/clear a cycle while the widget is present, and tap to open the app.
- Reopen after background process termination. Periodic widget updates are best effort and may be delayed by Android; the configured interval is 30 minutes.
- Distinguish process death, launcher removal from recents, and force-stop: force-stop suppresses background work until the user launches the app again.

If no device or SDK/dependency access is available, record the limitation and the exact pending checks. Never report checks as passed when they were not run.

### Regression check for packaged Room databases

Both build variants use R8. Room 2.6.1's bundled keep rule retains database classes but does not explicitly retain the no-argument constructors. AGP 9's strict keep-rule behavior can remove those constructors, causing `WorkDatabase_Impl.<init>` to fail inside the startup provider before the activity opens. The explicit constructor rule in `app/proguard-rules.pro` covers both the application and WorkManager databases.

After changing shrinking rules, build both variants and inspect the actual APKs (put Android SDK `cmdline-tools/latest/bin` on PATH, or supply `--apkanalyzer /path/to/apkanalyzer`):

```bash
python3 scripts/check_apk_database_constructors.py app/build/outputs/apk/debug/app-debug.apk
python3 scripts/check_apk_database_constructors.py app/build/outputs/apk/release/app-release.apk
```

The check must pass for both databases. Install each variant in turn, launch it, wait, then verify the app process is still running, the activity is resumed, and that process has no fatal logcat entries. `am start -W` returning `Status: ok` alone is insufficient: Android can accept the launch before the process crashes. Leave the debug variant installed after testing unless requested otherwise. Keep app data during reinstalls.
