package com.kidsgames.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.CelebrationOverlay
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.ClaySurface
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.Mascot
import com.kidsgames.app.ui.StarCounter
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import com.kidsgames.app.ui.theme.Radius
import com.kidsgames.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Gentle endless pulse that points the child at what to tap next. */
@Composable
internal fun rememberPulse(active: Boolean): Float {
    if (!active) return 1f
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale",
    )
    return scale
}

/** A row of earned stars, or soft dots where a level is still to play. */
@Composable
internal fun StarRow(filled: Int, slots: Int, starSize: Dp, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
        repeat(slots) { index ->
            if (index < filled) {
                ArtImage(Art.STAR, Modifier.size(starSize))
            } else {
                Box(
                    Modifier
                        .size(starSize)
                        .padding(starSize / 4)
                        .clip(CircleShape)
                        .background(ClayColors.SurfaceStrong)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Level picker
// ---------------------------------------------------------------------------------------------

/** The three levels of one world, laid out as stepping stones. The next one to play pulses. */
@Composable
fun LevelPicker(world: World, onHome: () -> Unit, onPlay: (level: Int) -> Unit) {
    val progress = LocalProgress.current
    val feedback = rememberGameFeedback()
    val next = progress.nextLevel(world.game)

    LaunchedEffect(Unit) { feedback.ask("¡${world.label}! Elige un nivel") }

    GameScaffold(
        onHome = onHome,
        stars = progress.totalStars,
        mood = feedback.mood,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = Space.md),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(world.color.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                ArtImage(world.art, Modifier.size(72.dp))
            }
        }
        // Stones zigzag down the screen like a little path.
        val bias = listOf(-0.55f, 0.55f, -0.1f)
        (1..LEVEL_COUNT).forEach { level ->
            val open = progress.isLevelOpen(world.game, level)
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = BiasAlignment(bias[(level - 1) % bias.size], 0f),
            ) {
                LevelStone(
                    level = level,
                    rating = progress.rating(world.game, level),
                    open = open,
                    isNext = open && level == next,
                    world = world,
                    onClick = {
                        if (open) {
                            feedback.tap()
                            onPlay(level)
                        } else {
                            feedback.ask("Ese todavía no. Juega primero el nivel ${level - 1}")
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun LevelStone(
    level: Int,
    rating: Int,
    open: Boolean,
    isNext: Boolean,
    world: World,
    onClick: () -> Unit,
) {
    val scale = rememberPulse(isNext)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        ClaySurface(
            color = if (open) world.color else ClayColors.SurfaceStrong,
            // The lip takes 10dp at the bottom, so the body stays a circle.
            modifier = Modifier.size(width = 112.dp, height = 122.dp),
            onClick = onClick,
            cornerRadius = Radius.pill,
            depth = 10.dp,
        ) {
            if (open) {
                Text("$level", style = KidsType.number, color = ClayColors.Ink)
            } else {
                ArtImage(Art.LOCK, Modifier.size(48.dp), tint = ClayColors.Muted)
            }
        }
        Spacer(Modifier.height(Space.xxs))
        // Stars appear only once earned: an unplayed level shows soft dots, never "zero".
        StarRow(filled = rating, slots = if (rating > 0) rating else 3, starSize = 26.dp)
    }
}

// ---------------------------------------------------------------------------------------------
// Playing a level
// ---------------------------------------------------------------------------------------------

/**
 * Plays [level] of [world]: runs its game until every round is played, saving the stars
 * of each round as it goes, then shows the level-complete celebration.
 */
@Composable
fun LevelPlay(
    world: World,
    level: Int,
    onHome: () -> Unit,
    onReplay: () -> Unit,
    onNext: (() -> Unit)?,
) {
    val store = LocalProgressStore.current
    val progress = LocalProgress.current
    // Decided before the first round: the sticker is new only if this level was never finished.
    val newSticker = remember(world, level) { progress.rating(world.game, level) == 0 }
    val run = remember(world, level) {
        LevelRun(
            rounds = world.game.roundsPerLevel,
            onRound = store::addStars,
            onFinished = { rating -> store.finishLevel(world.game, level, rating) },
        )
    }

    if (run.finished) {
        LevelComplete(
            rating = run.rating,
            stickerWorld = if (newSticker) world else null,
            onHome = onHome,
            onReplay = onReplay,
            onNext = onNext,
        )
        return
    }
    when (world.game) {
        GameId.COLORS -> ChoiceGame(COLOR_ITEMS, level, run, onHome)
        GameId.ANIMALS -> ChoiceGame(ANIMAL_ITEMS, level, run, onHome)
        GameId.FRUITS -> ChoiceGame(FRUIT_ITEMS, level, run, onHome)
        GameId.SHAPES -> ChoiceGame(SHAPE_ITEMS, level, run, onHome)
        GameId.COUNTING -> CountingGame(level, run, onHome)
        GameId.MEMORY -> MemoryGame(level, run, onHome)
        GameId.LETTERS -> ChoiceGame(
            items = if (level < 3) VOWEL_ITEMS else LETTER_ITEMS,
            level = level,
            run = run,
            onHome = onHome,
            promptOf = { "Toca la letra " + it.spoken.removePrefix("la ") },
        )
        GameId.NUMBERS -> when (level) {
            1 -> ChoiceGame(NUMBER_ITEMS_EASY, level, run, onHome)
            2 -> ChoiceGame(NUMBER_ITEMS_ALL, level, run, onHome)
            else -> OrderGame(run, onHome)
        }
        GameId.SOUNDS -> ChoiceGame(
            items = SOUND_ITEMS,
            level = level,
            run = run,
            onHome = onHome,
            promptOf = { (it.sound ?: "") + " ¿Quién hace así?" },
        )
        GameId.TRACE -> TraceGame(level, run, onHome)
    }
}

private fun levelPraise(rating: Int, newSticker: Boolean): String {
    val base = when (rating) {
        3 -> "¡Nivel superado! ¡Tres estrellas! ¡Eres genial!"
        2 -> "¡Nivel superado! ¡Dos estrellas! ¡Muy bien!"
        else -> "¡Nivel superado! ¡Una estrella! ¡Bien hecho!"
    }
    return if (newSticker) "$base ¡Y ganaste una pegatina!" else base
}

/**
 * Celebration after the last round. It shows only the stars earned (1 to 3), each popping
 * in, so there is never an empty slot that reads as a loss.
 */
@Composable
private fun LevelComplete(
    rating: Int,
    /** The world whose sticker was just won, or null when this level was finished before. */
    stickerWorld: World?,
    onHome: () -> Unit,
    onReplay: () -> Unit,
    onNext: (() -> Unit)?,
) {
    val feedback = rememberGameFeedback()
    val pops = remember { List(rating) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        feedback.win(levelPraise(rating, stickerWorld != null))
        pops.forEachIndexed { index, pop ->
            launch {
                delay(400L + 350L * index)
                pop.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(Space.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                StarCounter(LocalProgress.current.totalStars)
            }
            Spacer(Modifier.weight(1f))
            Mascot(mood = feedback.mood, modifier = Modifier.size(if (stickerWorld != null) 160.dp else 200.dp))
            Spacer(Modifier.height(Space.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                pops.forEach { pop ->
                    ArtImage(
                        Art.STAR,
                        Modifier
                            .size(88.dp)
                            .graphicsLayer {
                                scaleX = pop.value
                                scaleY = pop.value
                            },
                    )
                }
            }
            if (stickerWorld != null) {
                Spacer(Modifier.height(Space.md))
                // Arrives after the last star has popped.
                StickerReveal(stickerWorld, delayMillis = 400L + 350L * rating + 500L)
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClayButton(
                    onClick = { feedback.tap(); onHome() },
                    modifier = Modifier.size(width = 88.dp, height = 94.dp),
                    color = ClayColors.Teal,
                ) {
                    ArtImage(Art.HOME, Modifier.size(42.dp), tint = ClayColors.Canvas)
                }
                ClayButton(
                    onClick = { feedback.tap(); onReplay() },
                    modifier = Modifier.size(width = 88.dp, height = 94.dp),
                    color = ClayColors.Ochre,
                ) {
                    ArtImage(Art.REPLAY, Modifier.size(46.dp))
                }
                if (onNext != null) {
                    val scale = rememberPulse(true)
                    ClayButton(
                        onClick = { feedback.tap(); onNext() },
                        modifier = Modifier
                            .weight(1f)
                            .height(94.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                        color = ClayColors.Pink,
                    ) {
                        ArtImage(Art.NEXT, Modifier.size(52.dp), tint = ClayColors.Canvas)
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(Space.sm))
        }
        CelebrationOverlay(starBursts = feedback.starBursts, confettiShots = feedback.confettiShots)
    }
}
