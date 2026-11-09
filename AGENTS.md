# Agent instructions

Read [the development guide](docs/DEVELOPMENT.md) before changing this repository.

## Required workflow

- `dev` is the only permanent branch. Feature and fix branches are temporary and must be deleted after their changes are integrated and verified.
- Every feature, fix, documentation change, or maintenance task gets its own branch from the latest `dev`: `feature/<topic>`, `fix/<topic>`, `docs/<topic>`, or `chore/<topic>`.
- Inspect status and existing diffs first. Preserve user changes; never stage unrelated work, reset, clean, or stash it without authorization.
- Fetch before starting when network access is available. Update `dev` with `git merge --ff-only origin/dev`; do not force push shared branches.
- Keep task branches current with `dev` before review. Integrate validated changes through a PR targeting `dev`, then update local `dev`. Do not merge failing work just to keep `dev` updated.
- If `origin/dev` does not exist, report that bootstrap is needed; do not silently use a different base. Local `dev` was initialized from the existing `develop` baseline.
- Do not commit, publish, push, release, delete remote branches, or change remote defaults unless the user authorizes it. Explain any verification or access blockers.

## Implementation rules

- Follow Kotlin, Compose, MVVM, Hilt, and existing repository/use-case boundaries.
- Keep database and disk I/O out of composables and the main thread. Collect observable data without `runBlocking`.
- Use lifecycle-owned coroutines where possible. Short asynchronous receiver work must use `goAsync()` with `finish()` in `finally`; long work belongs in WorkManager.
- Preserve coroutine cancellation. Handle recoverable background failures explicitly; do not hide errors behind empty catches.
- Keep stored SOT and live elapsed SOT separate. Avoid double-counting after sync, process restart, charging, or cycle reset.
- Widgets must handle empty history, process recreation, data changes, all supported sizes, and tapping to open the app.
- Respect Android receiver, foreground service, notification, and usage-access restrictions for minSdk 35 / targetSdk 36. Never promise operation after force-stop.
- Preserve user history. Database changes need explicit migrations and upgrade checks; never enable destructive fallback as a crash fix.
- Add focused regression tests for changed behavior when practical. Run relevant checks and report exact results; compilation is not device validation.
- End with changed behavior, checks performed, unresolved issues, and branch state.
