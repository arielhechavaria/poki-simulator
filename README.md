# Kids Games

Offline learning games for a 4-year-old, built with Kotlin and Jetpack Compose.
No ads, no tracking, and the app declares no INTERNET permission.

## Games

Colors, animals, fruits, shapes, counting and memory pairs. All of them are native
Compose screens with Spanish voice prompts and no "you lost" screens.

A mascot drawn in Canvas greets on the home screen and reacts to every answer: it cheers
with a star burst or confetti when the child gets it right and gently encourages another
try otherwise. The sound effects are synthesized in code, so the app ships no audio files.

## Build

GitHub Actions builds the APK (`.github/workflows/build.yml`); download the
`kids-games-debug-apk` artifact or the latest release and install it (allow "install unknown apps").

Voice prompts use the phone's text-to-speech engine; install the Spanish voice data for offline use.

## Credits

All code and content in this repository is original, except:

- **Sniglet** typeface (`app/src/main/res/font/`), by Haley Fiege, Brenda Gallo and Pablo
  Impallari, under the SIL Open Font License 1.1 (`licenses/Sniglet-OFL.txt`). Source:
  <https://github.com/google/fonts/tree/main/ofl/sniglet>.

Third-party assets added later must carry a verified permissive license and be listed here.

## License

MIT, see [LICENSE](LICENSE).
