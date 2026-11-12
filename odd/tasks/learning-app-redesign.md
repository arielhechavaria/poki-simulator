# learning-app-redesign

Objective: turn the app into a learning-only, highly visual and gamified app for a 4-year-old.
Branch: main (the user builds only through CI on push to main). Delivery: single-pr not applicable, direct pushes per phase.
Test runner: none locally (no JDK/SDK); proof is the GitHub Actions build plus the user's device test.

## Specs

- S1: "ELIMINAR la sección "Divertirse" y todo lo asociado: WebGameScreen.kt, assets/games/, el paso de Kart Royale en el CI, la dependencia androidx.webkit, los créditos del README y el cambio de orientación/barras del sistema. El inicio pasa a mostrar directamente los juegos educativos."
- S2: "Centrarse solo en aprendizaje, para un niño de 4 años: botones grandes, sin texto obligatorio, voz en español y sin pantallas de "perdiste"."
- S3: "Mucho más visual y atractivo: tema infantil (colores vivos, tipografía redondeada, fondos con degradados), personajes o mascota que guíen y celebren, animaciones (rebotes, confeti, estrellas), sonidos, ilustraciones propias con Canvas o vectores en vez de emojis planos, e ícono de la app propio."
- S4: "Muchas más funcionalidades: nuevos juegos (letras y vocales, números, pintar y dibujar, rompecabezas, formas para encajar, sonidos de animales, rutinas, lo opuesto/tamaños), niveles progresivos, recompensas (pegatinas, estrellas), progreso guardado en el dispositivo, y una zona para padres con control de tiempo."
- S5: "Cuidar la licencia de lo que se incluya: solo contenido propio o con licencia permisiva verificada, con créditos en el README."
- S6: "cambiar la autoría de los commits al correo noreply de GitHub (el correo real es visible en el repo público), añadir una licencia MIT, y que la voz TTS en español funcione sin internet."
- S7: "veo poco temas de gamificacion. vamos a construir una app de alto nivel profesional paso a paso con gran compromsio en lo visual y lo estetico. vamos a usar este aspecto visual" (Clay style reference: cream canvas `#fffaf0`, ink `#0a0a0a`, saturated cards pink `#ff4d8b`, teal `#1a3a3a`, lavender `#b8a4ed`, peach `#ffb084`, ochre `#e8b94a`, mint `#a4d4c5`, coral `#ff6b5a`; radius up to 24; claymation look.)

Assumptions (not user requirements): the Clay reference is used as a palette and shape language only,
never its name, logo or artwork; its proprietary display font is replaced by an OFL rounded font;
its web type sizes are scaled up for a 4-year-old.

## Tasks

- [x] T1 (S1, S6 license) inline — remove the fun section, add MIT license. Commit: see Log.
- [ ] T2 (S6 authorship) inline — noreply for new commits is set; history rewrite pending the user's decision.
- [ ] T3 (S3, S7) inline — design system: tokens, type, clay surfaces, mascot, motion, sounds, app icon; migrate the 6 games.
- [ ] T4 (S4, S7) inline — gamification core: saved progress, stars, levels, stickers, daily streak, mascot growth.
- [ ] T5 (S4) inline — new games, batch A: vowels/letters, numbers, animal sounds.
- [ ] T6 (S4) inline — new games, batch B: paint, puzzles, shape sorting.
- [ ] T7 (S4) inline — new games, batch C: routines, opposites/sizes.
- [ ] T8 (S4, S6 voice) inline — parents area: gate, time limit, Spanish voice check.

## Log

- L1 (2026-11-12): original request, quoted in S1-S6 above verbatim; it also set the working rules:
  build only through GitHub Actions, conventional commits without Co-Authored-By, UI in Spanish, code in English.
- L2 (2026-11-12): user correction, quoted in S7. Gamification added as its own task (T4) and the Clay
  reference adopted as the visual direction.
- L3 (2026-11-12): T1 done. Local git email switched to the GitHub noreply address for new commits.
