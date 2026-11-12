# Kids Games

Offline game portal for a 4-year-old, built with Kotlin and Jetpack Compose.
No ads, no tracking, and the app declares no INTERNET permission.

## Sections

- **Aprender** (native Compose): colors, animals, fruits, shapes, counting, memory pairs.
- **Divertirse** (bundled HTML5 games in a sandboxed WebView, served from `assets/games/<id>/`):
  Carros (original), Pac-Man (MIT, with touch controls added).

## Adding an HTML5 game

1. Put the game in `app/src/main/assets/games/<id>/index.html` (all files local, no CDNs).
2. Add a `WebGame(...)` entry to `WEB_GAMES` in `WebGameScreen.kt`.
3. Keep the game's LICENSE file next to it and list it under Credits.

## Build

GitHub Actions builds the APK (`.github/workflows/build.yml`); download the
`kids-games-debug-apk` artifact or the latest release and install it (allow "install unknown apps").
Voice prompts use the phone's text-to-speech engine; install the Spanish voice data for offline use.

Karts (Kart Royale) is not stored in this repo: CI clones a pinned upstream commit, builds it
with Vite and copies it into `assets/games/kart-royale/`. A local Android Studio build
without that step shows an empty page for that game.

## Credits

- Kart Royale: https://github.com/ryancampbell/kart-royale (MIT, license copied into its assets folder at build time).
- Pac-Man: https://github.com/mumuy/pacman (MIT, see `assets/games/pacman/LICENSE.txt`).
  Touch wrapper page written for this project; the font PressStart2P is licensed under the SIL OFL.
