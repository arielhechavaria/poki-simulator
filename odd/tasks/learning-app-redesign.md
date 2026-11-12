# learning-app-redesign

Objective: turn the app into a learning-only, highly visual and gamified app for a 4-year-old.
Branch: main (the user builds only through CI on push to main); one task per session, one or more commits per task.
Test runner: none locally (no JDK/SDK); proof is a green GitHub Actions build plus the user's device test.

## Specs

- S1: "ELIMINAR la sección "Divertirse" y todo lo asociado: WebGameScreen.kt, assets/games/, el paso de Kart Royale en el CI, la dependencia androidx.webkit, los créditos del README y el cambio de orientación/barras del sistema. El inicio pasa a mostrar directamente los juegos educativos."
- S2: "Centrarse solo en aprendizaje, para un niño de 4 años: botones grandes, sin texto obligatorio, voz en español y sin pantallas de "perdiste"."
- S3: "Mucho más visual y atractivo: tema infantil (colores vivos, tipografía redondeada, fondos con degradados), personajes o mascota que guíen y celebren, animaciones (rebotes, confeti, estrellas), sonidos, ilustraciones propias con Canvas o vectores en vez de emojis planos, e ícono de la app propio."
- S4: "Muchas más funcionalidades: nuevos juegos (letras y vocales, números, pintar y dibujar, rompecabezas, formas para encajar, sonidos de animales, rutinas, lo opuesto/tamaños), niveles progresivos, recompensas (pegatinas, estrellas), progreso guardado en el dispositivo, y una zona para padres con control de tiempo."
- S5: "Cuidar la licencia de lo que se incluya: solo contenido propio o con licencia permisiva verificada, con créditos en el README."
- S6: "cambiar la autoría de los commits al correo noreply de GitHub (el correo real es visible en el repo público), añadir una licencia MIT, y que la voz TTS en español funcione sin internet."
- S7: "veo poco temas de gamificacion. vamos a construir una app de alto nivel profesional paso a paso con gran compromsio en lo visual y lo estetico. vamos a usar este aspecto visual" followed by the Clay style reference.

### Visual direction (from the Clay reference in S7)

- Colors: canvas `#fffaf0`, surface soft `#faf5e8`, surface card `#f5f0e0`, surface strong `#ebe6d6`,
  ink `#0a0a0a`, body `#3a3a3a`, muted `#6a6a6a`, hairline `#e5e5e5`, dark surface `#0a1a1a`;
  brand pink `#ff4d8b`, teal `#1a3a3a`, lavender `#b8a4ed`, peach `#ffb084`, ochre `#e8b94a`,
  mint `#a4d4c5`, coral `#ff6b5a`; success `#22c55e`, warning `#f59e0b`, error `#ef4444`.
  No colors outside this set.
- Shape: radius 6/8/12/16/24 and pill; spacing 4/8/12/16/24/32/48; card padding 24-32.
- Look: cream canvas, one saturated color per card, ink-colored primary buttons, claymation feel.

Assumptions (not user requirements): the reference gives palette and shape language only, never
Clay's name, logo or artwork. Its paid display font is replaced by a rounded OFL font. Its web type
sizes are scaled up for a 4-year-old (touch targets of at least 64dp). The clay look is drawn in
Canvas with gradients, highlights and soft shadows, not real 3D renders.

## Tasks

Suggested model in brackets. Briefs are under "Task briefs".

- [x] T1 (S1, S6 license) inline — remove the fun section, add MIT license. Commit 9aa977a, CI green.
- [x] T2 (S6 authorship) inline — noreply email set for new commits; history left as is by user decision (L4).
- [x] T3 (S3, S7) inline [opus] — design system foundation and restyled home. Commit 2535bfe, CI green.
- [x] T4 (S3, S7) inline [sonnet] — mascot, celebrations and sounds. Commit 09e000b, CI green.
- [x] T5 (S3, S5) inline [sonnet] — own illustrations for the 6 games and app icon. Commit e896471, CI green.
- [x] T6 (S4, S7) inline [opus] — saved progress, stars, progressive levels, adventure-map home. Commit 5f2aa61, CI green.
- [x] T7 (S4, S7) inline [sonnet] — sticker album, mascot accessories, soft daily streak. Commit 2ec1c20, CI green.
- [x] T8 (S4) inline [sonnet] — new games A: vowels and letters, numbers, animal sounds. Commit 30b7221, CI green.
- [x] T9 (S4) inline [sonnet] — new games B: paint and draw, puzzles, shape sorting. Commit 9df5350, CI green.
- [x] T10 (S4) inline [sonnet] — new games C: routines, opposites and sizes. Commit 55a76fa, CI green.
- [ ] T11 (S4, S6 voice) inline [sonnet] — parents area: gate, time limit, Spanish voice check.
- [ ] T12 (S5) inline [sonnet] — README, credits, version bump and release v1.0.0.

## Task briefs

Every task ends the same way: push, green CI run, tick the task, one Log line, hand over the next prompt.

- T3: load the `frontend-design` skill first. Add a `ui/theme` package with color, shape, spacing and
  type tokens from "Visual direction". Bundle one rounded OFL font in `res/font` (verify the license
  at its source, keep its license file, credit it in the README). Build the shared pieces:
  clay card and clay button with a press-bounce, gradient canvas background, new `GameScaffold`
  (home button, star counter). Restyle the home grid with them. Emojis may stay for now.
- T4: draw a mascot in Canvas with idle, talking and cheering states; it greets on home and reacts
  to right and wrong answers (wrong = gentle encouragement). Add confetti and star-burst effects and
  short sound effects synthesized in code (no audio files). Wire them into the three game engines.
- T5: replace the emojis in colors, animals, fruits, shapes, counting and memory with Canvas or
  vector illustrations in the clay look. Add an adaptive launcher icon and review the app name.
- T6: persist progress on the device (DataStore). Each round gives 1 to 3 stars, never zero.
  Each game raises difficulty by level (2, then 3, then 4 choices; larger counts; more pairs).
  Replace the home grid with an adventure-map path of worlds that unlock with stars.
- T7: sticker album (one sticker per completed level, with an opening animation), mascot accessories
  unlocked with stars, and a daily gift for playing with no penalty for missed days.
- T8: vowels and letters (hear and pick, trace the letter), numbers 1 to 10 (recognize and order),
  animal sounds (hear and pick; sounds synthesized or recorded by the user, else TTS onomatopoeia).
- T9: free painting with brushes and stamps plus coloring outlines, 4 to 9 piece puzzles from the
  app's own illustrations, and drag-to-fit shape sorting.
- T10: daily routines (order 3 to 4 steps: wake up, brush teeth...), opposites and sizes
  (big/small, up/down, many/few).
- T11: parents area behind a gate a 4-year-old cannot pass (hold plus a simple sum), daily time limit
  with a friendly goodbye from the mascot, progress summary and reset, and a check that the Spanish
  voice is installed with a shortcut to the system install screen.
- T12: final README with screenshots list and credits, `versionCode`/`versionName` bump, release.

## Log

- L1 (2026-11-12): original request, quoted in S1-S6 verbatim; it also set the working rules now in `CLAUDE.md`.
- L2 (2026-11-12): user correction, quoted in S7. Gamification added (T6, T7); Clay reference adopted.
- L3 (2026-11-12): T1 done in 9aa977a, CI green. Local git email switched to the GitHub noreply address.
- L4 (2026-11-12): user decision on history: "déjalos como están y sigue con la base visual. para evitar
  pasarnos del contexto podemos definir un plan de trabajo y los prompts por cada tarea. Asi hacemos cada
  tarea en una section. clear y seguimos para la otra." T2 closed; T3 split into T3-T5; one task per session.
- L5 (2026-11-12): T3 done in 2535bfe, CI run 43236925717 green; device test pending. Font is Sniglet
  (OFL 1.1, static Regular and ExtraBold from google/fonts). Tokens in `ui/theme`, `ClaySurface`/`ClayCard`/
  `ClayButton`/`ClayBackground` in `ui/Clay.kt`, `GameScaffold` and `StarCounter` in `ui/GameScaffold.kt`.
  The three games already use the clay cards; emojis stay until T5.
- L6 (2026-11-12): T4 done in 09e000b, CI run 43238635012 green; device test pending (look, sound
  volume and timing were never seen or heard, only compiled). `Mascot` and `MascotMood` in `ui/Mascot.kt`,
  `CelebrationOverlay` and `starPath` in `ui/Effects.kt`, `Sfx`/`LocalSfx` (static `AudioTrack` tones) in
  `Sfx.kt`, and `GameFeedback` in `Feedback.kt`, which the three engines call (`ask`, `right`, `wrong`,
  `miss`, `win`, `tap`). New games should reuse `rememberGameFeedback()` and `GameScaffold`.
- L7 (2026-11-12): T5 done in e896471, CI run 43239719955 green; device test pending (illustrations and icon were never seen, only compiled). `Art` enum + `ArtImage`/`ClayBall` in `ui/Art.kt` (100x100 grid, all drawn in code, palette plus natural fruit/animal tones); new art goes there. Includes interface icons (HOME, SPEAKER, REPLAY, tinted). Adaptive icon: `drawable/ic_launcher_{background,foreground}.xml`, `mipmap-anydpi-v26` adaptive, `mipmap-anydpi` layer-list for API 24-25. App name changed from "Mis Juegos" to "Aprendo Jugando". Emojis are gone from app code.
- L8 (2026-11-12): T6 done in 5f2aa61, CI run 43241672229 green; device test pending (map, levels and saving were never seen, only compiled). `ProgressStore`/`Progress`/`LocalProgress` in `Progress.kt` (DataStore Preferences 1.1.1, keys `stars_total` and `best_<GameId>_<level>`: never rename `GameId` entries). `World`/`WORLDS` (stars to open: 0, 3, 6, 10, 15, 20), `LevelRun`, `roundsPerLevel` (5, memory 2) in `Worlds.kt`. `LevelPicker`, `LevelPlay`, `LevelComplete` (shows only earned stars), `StarRow`, `rememberPulse` in `LevelScreens.kt`; `AdventureMap` in `AdventureMap.kt`. Games take `(level, run, onHome)` and call `run.finishRound(stars)`; `GameScaffold` gained `roundsDone`/`roundsTotal`. New icons `Art.LOCK`, `Art.NEXT`. T7 stickers can derive "completed level" from `Progress.rating(game, level) > 0`.
- Engram mirror pending: the memory save was blocked in auto mode, so this file is the source of truth.
- L9 (2026-11-12): T7 done in 2ec1c20, CI run 43243755724 green; device test pending (album, reveal animation, accessories and gift were never seen, only compiled). `Collection.kt`: `StickerBadge`, `StickerReveal` (shown in `LevelComplete` only the first time a level is finished), `StickerAlbum` (18 stickers = one per game level, plus wardrobe), `DailyGift`. Stickers are derived from `Progress.rating > 0`, nothing new stored. `Accessory` enum + `LocalMascotAccessory` in `ui/Mascot.kt` (BOW 10, GLASSES 25, HAT 45, CROWN 70 stars; saved by name, never rename). New keys `gift_day`, `gift_count`, `accessory`; `currentDay()`, `giftReward()` (2 stars, every 7th gift 5), `ProgressStore.claimGift/equip`. Missed days never subtract: the week row only fills. New art `Art.GIFT`, `Art.ALBUM`.
- L10 (2026-11-12): T8 done in 30b7221, CI run 43244557050 green; device test pending (nothing was seen or heard, only compiled; TTS onomatopoeia and the trace coverage feel are the main unknowns). Four new worlds after Memoria, stars to open 25/30/36/42: `GameId.LETTERS` (levels 1-2 vowels, 3 more letters), `NUMBERS` (1-5, 1-10, then `OrderGame`), `SOUNDS` (animal says its sound via TTS, child picks it), `TRACE` (`TraceGame`: finger paints inside a bold system-font glyph, 80% coverage wins, always 3 stars). `ChoiceGame` gained `promptOf` and `Choice` gained `text`/`sound`. New art `BLOCKS`, `ABACUS`, `NOTE`, `CRAYON`. `GameId` entries are stored keys: never rename.
- L11 (2026-11-12): T9 done in 9df5350, CI run 43245995558 green; device test pending (nothing was seen or touched, only compiled; drag feel, snap radius and puzzle crop are the main unknowns). Three new worlds after Trazar, stars to open 48/54/60: `GameId.PAINT` (`PaintGame`: level 1 finger painting with 5 colours and thick/thin brush, 2 stamps, 3 colouring three own pictures by tapping parts; always 3 stars), `PUZZLE` (`PuzzleGame`: 4, 6, 9 pieces cut from own animal art, faint hint on the board), `SORT` (`SortGame`: 3, 4, 6 shapes dragged into outlines). `DragFit.kt` is the shared drag-to-fit board (`fitLayout`, `DragFit`) for T10 if needed. `roundsPerLevel`: PAINT/PUZZLE 2, SORT 3. New art `BRUSH`, `PUZZLE`, `SORTER`. `GameId` entries are stored keys: never rename. The album now has 13 worlds x 3 stickers.
- L12 (2026-11-12): T10 done in 55a76fa, CI run 43246775910 green; device test pending (nothing was seen or heard, only compiled; new art drawings, card layouts and the wording of the voice prompts are the main unknowns). Two new worlds after Encajar, stars to open 66/72: `GameId.ROUTINES` (`RoutineGame`: tap 3 steps at level 1, 4 steps later, of a morning or night routine in order; mistakes only nudge) and `OPPOSITES` (`OppositesGame`: two cards, big/small at level 1, plus many/few at level 2, plus up/down at level 3). New art `CLOCK`, `SCALE`, `SUN`, `TOOTHBRUSH`, `BOWL`, `SHIRT`, `BED`, `MOON`. `GameId` entries are stored keys: never rename. The album now has 15 worlds x 3 stickers.
