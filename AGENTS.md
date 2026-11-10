# Agent instructions — OneCharge

Read [the development guide](docs/DEVELOPMENT.md) before changing this repository. Treat repository code and configuration as the source of truth; verify assumptions before making changes.

## Required Git workflow

- `dev` is the only permanent development branch. Feature and fix branches are temporary and should be deleted only after their changes are integrated, verified, and deletion is authorized.
- Every feature, fix, documentation change, or maintenance task gets its own branch from the latest verified `dev`: `feature/<topic>`, `fix/<topic>`, `docs/<topic>`, or `chore/<topic>`. Use short descriptive lowercase names.
- Before any change, inspect `git status`, the current branch, existing diffs, and relevant worktrees. Preserve all user changes; never stage unrelated files or reset, clean, or stash changes without explicit authorization.
- Fetch before starting when network access is available. If `origin/dev` exists, update local `dev` using a fast-forward-only operation (`git merge --ff-only origin/dev`) when safe. If the checkout is dirty, diverged, or otherwise unsafe to update, stop and explain the blocker; do not overwrite or silently work around it.
- If `origin/dev` does not exist, report that repository bootstrap is needed; do not silently choose another base. Local `dev` was initialized from the existing `develop` baseline.
- Create the task branch before implementation. For substantial or risky work, prefer an isolated worktree when the installed `using-git-worktrees` skill is available. Do not move or overwrite existing worktrees or user changes. Small tasks may use a normal task branch.
- Keep task branches current with `dev` before review, but do not rewrite published history or rebase shared branches without authorization. Resolve conflicts deliberately and preserve all changes.
- Integrate validated changes through a PR targeting `dev`, then update local `dev` safely. Do not merge failing work just to keep `dev` updated.
- Never commit, publish, push, merge, release, delete local or remote branches/worktrees, force-push, or change remote defaults without user authorization. An instruction to implement a feature does not itself authorize these Git operations.
- Never delete a branch or worktree with uncommitted or unmerged work. After integration and verification, propose cleanup and obtain authorization before performing it.
- Prefer small, focused commits using Conventional Commits (`feat:`, `fix:`, `refactor:`, `perf:`, `test:`, `docs:`, `chore:`). Review staged diffs; exclude secrets, local settings, generated outputs, and unrelated files.
- If network, credentials, repository state, or permissions block the workflow, report the exact blocker and request a decision rather than changing the branch policy.

## Implementation rules

- Follow Kotlin, Jetpack Compose, MVVM, Hilt, and existing repository/use-case boundaries. Do not introduce a new architecture or dependency without a concrete need.
- Keep database and disk I/O out of composables and the main thread. Collect observable data without `runBlocking`; use lifecycle-aware collection and structured concurrency.
- Use lifecycle-owned coroutines where possible. Short asynchronous receiver work must use `goAsync()` with `finish()` in `finally`; long work belongs in WorkManager where Android policy permits.
- Preserve coroutine cancellation. Handle recoverable background failures explicitly; do not hide errors behind empty catches or broad exception handling.
- Keep stored screen-on-time (SOT) and live elapsed SOT separate. Avoid double-counting after sync, process restart, charging, or cycle reset. Maintain correct time and state boundaries.
- Widgets must handle empty history, process recreation, data changes, all supported sizes, and tapping to open the app.
- Respect Android receiver, foreground service, notification, and usage-access restrictions for minSdk 35 / targetSdk 36. Never promise operation after force-stop. Check actual platform behavior rather than assuming background execution is guaranteed.
- Preserve user history. Database schema changes require explicit migrations and upgrade-path checks; never enable destructive fallback as a crash fix.
- Write idiomatic, maintainable Kotlin. Prefer clear APIs, small cohesive components, explicit error handling, and the simplest solution that meets requirements. Avoid unnecessary abstractions, dependencies, allocations, and Compose recompositions.
- Follow native Android accessibility and Material design guidance consistent with the existing UI. Preserve existing functionality unless changes are requested.
- Add focused regression tests for changed behavior when practical. For SOT, charging, sync, persistence, and widgets, prioritize edge cases and process/lifecycle transitions.

## Skill selection and application

- Before each task, inspect the available skills and select only those relevant to the task. Read the selected `SKILL.md` files and follow their instructions. Do not assume a skill is installed; verify availability. Prefer official Android guidance when it applies.
- Core Android development: `android-dev`; UI: `compose`, `mobile-android-design`, `android-ux`; concurrency: `kotlin-coroutines`, `kotlin-flows`; data: `android-data-layer`.
- Debugging: `systematic-debugging`, `android-debugging`; testing: `android-testing`, `test-driven-development`; planning: `writing-plans`; review: `requesting-code-review`; completion: `verification-before-completion`.
- Git workflow: use `using-git-worktrees` for appropriate isolated work, `finishing-a-development-branch` for integration planning, and `receiving-code-review` when addressing review feedback, if installed. These skills do not override this repository's branch policy or authorization requirements.
- Use technology-specific skills (`datastore`, `coil-compose`, `android-retrofit`, `koin`, `paging`, `modularization`, `android-gradle-logic`, `gradle-build-performance`, and others) only when the codebase and task warrant them.
- If skills conflict, follow repository safety rules and actual project requirements; explain any material conflict. Briefly report which skills were applied and why.

## Verification and completion

- Inspect relevant code paths and tests before editing. For complex changes, propose a concise implementation plan with risks and verification steps.
- Run applicable Gradle compilation, unit tests, lint, and other relevant checks. Where available and appropriate, run emulator/device validation for behavior that depends on Android lifecycle, permissions, widgets, or background execution.
- Review the final diff for unintended changes, sensitive data, and formatting issues. Investigate failures; do not hide them or claim checks passed when they were not run.
- Distinguish compilation, automated test success, and real-device validation. Do not claim performance improvements without measurement.
- End with a concise report of changed behavior, changed files, selected skills, exact checks and results, unresolved issues, and current branch/worktree/commit/PR state. State clearly which Git actions still require authorization.
