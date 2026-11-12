# Kids Games — working rules

Android app (Kotlin + Jetpack Compose) with offline learning games for a 4-year-old.

- No JDK or Android SDK on this machine. Build ONLY through GitHub Actions
  (`.github/workflows/build.yml`): push to `main`, wait with `gh run watch`, download the APK
  with `gh run download`. Work directly on `main`.
- Never run third-party downloaded code locally; anything third-party is built in CI.
  If `gh` fails on quota, point `TMPDIR` at the session scratchpad.
- Conventional commits, no Co-Authored-By. Commits use the GitHub noreply email already set
  in this repo's git config; do not rewrite existing history.
- UI text in Spanish, code and docs in English.
- Only original content or assets with a verified permissive license, credited in the README.
- Audience rules: big touch targets, no required reading, Spanish voice, never a "you lost" screen.
- The plan, specs and per-task briefs live in `odd/tasks/learning-app-redesign.md`.
  One task per session: do the task, push, wait for a green CI run, tick the task and append
  one Log line, then give the user the prompt for the next task.
- Short answers; ask at most one question at a time.
