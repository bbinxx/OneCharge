# Widget refresh and SOT accounting implementation plan

Goal: Refresh installed widgets without opening the activity, with bounded Android-scheduled work and accurate screen interval boundaries.

Scope: The user's request to improve widget reliability and minimize battery use. Keep SDK/dependency versions, preserve history, use existing WorkManager/Glance APIs, and leave the implementation uncommitted for review.

1. Add failing unit tests for screen intervals: an ongoing interval must remain separate from stored SOT; cycles begun mid-screen-session cannot inherit earlier screen time; repeated screen-on events and clock rollback must not inflate SOT.
2. Implement pure interval accounting and share live-time projection across dashboard, service, and widget. Read only screen-state usage events for widget snapshots and sync; perform Android queries on IO.
3. Add one unique 15-minute periodic widget refresh job. Schedule for installed widgets on update/application start, cancel when the last widget is removed, and use persisted Glance refresh state so an existing composition receives the update. Do not add alarms, networking, or foreground service starts. Use the same refresh path for existing data-change triggers.
4. Make widget snapshots independent of tracker process state and query current battery capacity. Avoid polling outside the visible dashboard or optional notification service. Pause notification ticking when the screen is off.
5. Run unit tests, compilation, lint, debug APK constructor check, and connected-device startup/widget/job validation. Inspect job counts and foreground service state; do not claim zero battery drain without a power measurement.

Review focus: no widgets installed; multiple widgets; process recreation; permission denied; cycle reset while screen stays on; screen off and device restart; cancellation; scheduled refresh during an active Glance session.
