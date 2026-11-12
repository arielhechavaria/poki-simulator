# Kids Games

Offline educational mini-games for a 4-year-old, built with Kotlin and Jetpack Compose.
No ads, no network permission, no tracking.

## Games

Colors, animals, fruits, shapes (voice-guided "find it" game), counting, and memory pairs.

## Build

The APK is built by GitHub Actions (`.github/workflows/build.yml`). Open the latest
workflow run and download the `kids-games-debug-apk` artifact, then install it on the phone
(allow "install unknown apps").

Voice prompts use the phone's text-to-speech engine; install the Spanish voice data
for offline use (Settings > System > Languages > Text-to-speech).
