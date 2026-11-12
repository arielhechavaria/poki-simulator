package com.kidsgames.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Accessory
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.ClaySurface
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.Mascot
import com.kidsgames.app.ui.MascotMood
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.Radius
import com.kidsgames.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * One sticker: the world's picture on a clay disc. A sticker that is not won yet is a
 * soft, empty spot waiting for it, never a lock.
 */
@Composable
internal fun StickerBadge(
    world: World,
    earned: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    ClaySurface(
        color = if (earned) world.color else ClayColors.SurfaceStrong,
        modifier = modifier.size(width = size, height = size + 8.dp),
        onClick = onClick,
        cornerRadius = Radius.pill,
        depth = 8.dp,
    ) {
        Box(
            Modifier
                .size(size * 0.82f)
                .clip(CircleShape)
                .background(ClayColors.Canvas.copy(alpha = if (earned) 0.85f else 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            ArtImage(
                world.art,
                Modifier
                    .size(size * 0.6f)
                    .graphicsLayer { alpha = if (earned) 1f else 0.18f },
            )
        }
    }
}

/**
 * Pops a freshly won sticker in: it spins and bounces into place after [delayMillis].
 */
@Composable
internal fun StickerReveal(world: World, delayMillis: Long, modifier: Modifier = Modifier) {
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        pop.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow),
        )
    }
    StickerBadge(
        world = world,
        earned = true,
        size = 104.dp,
        modifier = modifier.graphicsLayer {
            scaleX = pop.value
            scaleY = pop.value
            rotationZ = (1f - pop.value) * -200f
        },
    )
}

// ---------------------------------------------------------------------------------------------
// Album and wardrobe
// ---------------------------------------------------------------------------------------------

/** The child's collection: every sticker won so far, and the mascot's wardrobe. */
@Composable
fun StickerAlbum(onHome: () -> Unit) {
    val progress = LocalProgress.current
    val store = LocalProgressStore.current
    val feedback = rememberGameFeedback()
    val worn = progress.accessory

    LaunchedEffect(Unit) {
        feedback.ask(
            if (progress.stickersEarned() == 0) "Aquí pondremos tus pegatinas"
            else "¡Mira todas tus pegatinas!"
        )
    }

    GameScaffold(
        onHome = onHome,
        stars = progress.totalStars,
        mood = feedback.mood,
        starBursts = feedback.starBursts,
        confettiShots = feedback.confettiShots,
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = Space.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            WORLDS.forEach { world ->
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    (1..LEVEL_COUNT).forEach { level ->
                        val earned = progress.rating(world.game, level) > 0
                        StickerBadge(
                            world = world,
                            earned = earned,
                            size = 92.dp,
                            onClick = {
                                if (earned) {
                                    feedback.tap()
                                    feedback.ask("${world.label}, nivel $level")
                                } else {
                                    feedback.ask("Juega ${world.label} para ganar esta pegatina")
                                }
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.sm))
            Wardrobe(
                progress = progress,
                worn = worn,
                onPick = { accessory ->
                    when {
                        !progress.isUnlocked(accessory) -> {
                            val missing = accessory.starsToUnlock - progress.totalStars
                            feedback.ask(
                                if (missing == 1) "¡Te falta una estrella!"
                                else "¡Te faltan $missing estrellas!"
                            )
                        }
                        accessory == worn -> {
                            feedback.tap()
                            store.equip(null)
                        }
                        else -> {
                            store.equip(accessory)
                            feedback.win("¡Ahora llevo ${accessory.label}!")
                        }
                    }
                },
            )
            Spacer(Modifier.height(Space.md))
        }
    }
}

/** The mascot in each accessory; the ones not unlocked yet are dimmed and show their star price. */
@Composable
private fun Wardrobe(progress: Progress, worn: Accessory?, onPick: (Accessory) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.md),
    ) {
        Accessory.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                pair.forEach { accessory ->
                    val unlocked = progress.isUnlocked(accessory)
                    ClayCard(
                        color = when {
                            !unlocked -> ClayColors.SurfaceStrong
                            accessory == worn -> ClayColors.Pink
                            else -> ClayColors.Lavender.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.size(width = 150.dp, height = 168.dp),
                        onClick = { onPick(accessory) },
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center) {
                                Mascot(
                                    mood = MascotMood.IDLE,
                                    modifier = Modifier
                                        .size(112.dp)
                                        .graphicsLayer { alpha = if (unlocked) 1f else 0.3f },
                                    accessory = accessory,
                                )
                                if (!unlocked) {
                                    ArtImage(Art.LOCK, Modifier.size(40.dp), tint = ClayColors.Muted)
                                }
                            }
                            if (!unlocked) StarsToOpen(accessory.starsToUnlock)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Daily gift
// ---------------------------------------------------------------------------------------------

/**
 * A gift to open once a day. Missing days costs nothing: the week of little stars below the
 * box only ever fills up, and the seventh spot holds a bigger gift.
 */
@Composable
fun DailyGift(onHome: () -> Unit) {
    val progress = LocalProgress.current
    val store = LocalProgressStore.current
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    val today = remember { currentDay() }
    // Fixed when the screen opens, so the numbers stay put after the save lands.
    val before = remember { progress.giftCount }
    val ready = remember { progress.giftReady(today) }
    val reward = remember { giftReward(before + 1) }
    var opened by remember { mutableStateOf(false) }
    val burst = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        feedback.ask(if (ready) "¡Tengo un regalo para ti! Tócalo" else "¡Vuelve mañana para otro regalo!")
    }

    val days = if (opened) before + 1 else before
    val filled = if (days == 0) 0 else (days - 1) % 7 + 1

    GameScaffold(
        onHome = onHome,
        stars = progress.totalStars,
        mood = feedback.mood,
        starBursts = feedback.starBursts,
        confettiShots = feedback.confettiShots,
    ) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.align(Alignment.CenterHorizontally)) {
            val pulse = rememberPulse(ready && !opened)
            ArtImage(
                Art.GIFT,
                Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        val s = pulse + burst.value * 0.25f
                        scaleX = s
                        scaleY = s
                        rotationZ = burst.value * 8f
                    }
                    .let { base ->
                        if (ready && !opened) {
                            base.clickableNoRipple {
                                opened = true
                                store.claimGift(today)
                                feedback.win("¡Ganaste $reward estrellas!")
                                scope.launch {
                                    burst.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessLow))
                                    burst.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                                }
                            }
                        } else {
                            base
                        }
                    },
            )
        }
        Spacer(Modifier.height(Space.lg))
        if (opened) {
            StarRow(
                filled = reward,
                slots = reward,
                starSize = 56.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(7) { index ->
                when {
                    index == 6 -> ArtImage(
                        Art.GIFT,
                        Modifier
                            .size(44.dp)
                            .graphicsLayer { alpha = if (filled == 7) 1f else 0.45f },
                    )
                    index < filled -> ArtImage(Art.STAR, Modifier.size(36.dp))
                    else -> Box(
                        Modifier
                            .size(36.dp)
                            .padding(10.dp)
                            .clip(CircleShape)
                            .background(ClayColors.SurfaceStrong)
                    )
                }
            }
        }
        Spacer(Modifier.height(Space.lg))
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        role = Role.Button,
        onClick = onClick,
    )
}
