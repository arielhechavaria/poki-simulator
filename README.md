# Kids Games

Offline learning games for a 4-year-old, built with Kotlin and Jetpack Compose.
No ads, no tracking, and the app declares no INTERNET permission.

## Games

Fifteen worlds on an adventure map, each with three levels that get harder. All of them are
native Compose screens with Spanish voice prompts, big touch targets, no required reading and
no "you lost" screens.

| Group | Worlds |
| --- | --- |
| Naming | Colors, fruits, animals, shapes |
| Numbers and letters | Counting, numbers 1 to 10, vowels and letters |
| Listening | Animal sounds |
| Thinking | Memory pairs, routines in order, opposites (big/small, many/few, up/down) |
| Making | Letter tracing, painting and coloring, puzzles, shape sorting |

A mascot drawn in Canvas greets on the home screen and reacts to every answer: it cheers
with a star burst or confetti when the child gets it right and gently encourages another
try otherwise. The sound effects are synthesized in code, so the app ships no audio files.

Every round pays 1 to 3 stars, never zero, and stars open new worlds. Each finished level
earns a sticker for the album, stars unlock mascot accessories, and a daily gift never
penalizes missed days. Progress is saved on the device with Jetpack DataStore.

## Screenshots

To add under `docs/screenshots/` after a device test: adventure map, a choice game, the
sticker album, painting, a puzzle, and the level-complete screen.

## Build

GitHub Actions builds the APK (`.github/workflows/build.yml`); download the
`kids-games-debug-apk` artifact or the latest release and install it (allow "install unknown apps").

Voice prompts use the phone's text-to-speech engine; install the Spanish voice data for offline use.

## Credits

All code, illustrations (drawn in Canvas), the mascot, the app icon and the sounds are original,
except:

- **Sniglet** typeface (`app/src/main/res/font/`), by Haley Fiege, Brenda Gallo and Pablo
  Impallari, under the SIL Open Font License 1.1 (`licenses/Sniglet-OFL.txt`). Source:
  <https://github.com/google/fonts/tree/main/ofl/sniglet>.

Third-party assets added later must carry a verified permissive license and be listed here.

## License

MIT, see [LICENSE](LICENSE).
