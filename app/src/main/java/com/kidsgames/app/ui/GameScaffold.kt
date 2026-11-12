package com.kidsgames.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import com.kidsgames.app.ui.theme.Radius
import com.kidsgames.app.ui.theme.Space

/**
 * Common game frame: big home button on the left, the mascot in the middle and the star
 * counter on the right, with the celebration effects drawn over everything.
 */
@Composable
fun GameScaffold(
    onHome: () -> Unit,
    stars: Int,
    mood: MascotMood = MascotMood.IDLE,
    starBursts: Int = 0,
    confettiShots: Int = 0,
    roundsDone: Int = 0,
    roundsTotal: Int = 0,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(Space.md)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 6dp taller than wide: the lip sits below a round body.
                ClayButton(onClick = onHome, modifier = Modifier.size(width = 72.dp, height = 78.dp)) {
                    ArtImage(Art.HOME, Modifier.size(36.dp), tint = ClayColors.Canvas)
                }
                Spacer(Modifier.weight(1f))
                Mascot(mood = mood, modifier = Modifier.size(92.dp))
                Spacer(Modifier.weight(1f))
                StarCounter(stars)
            }
            if (roundsTotal > 0) {
                RoundTrail(
                    done = roundsDone,
                    total = roundsTotal,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Space.xs),
                )
            }
            content()
        }
        // On top of the game, but it never takes touches.
        CelebrationOverlay(starBursts = starBursts, confettiShots = confettiShots)
    }
}

/** One slot per round of the level: a star for each round played, a soft dot for the rest. */
@Composable
fun RoundTrail(done: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            if (index < done) {
                ArtImage(Art.STAR, Modifier.size(30.dp))
            } else {
                Box(
                    Modifier
                        .size(30.dp)
                        .padding(Space.xs)
                        .clip(CircleShape)
                        .background(ClayColors.SurfaceStrong)
                )
            }
        }
    }
}

/** Star total in a dark pill; it pops every time a star is earned. */
@Composable
fun StarCounter(stars: Int, modifier: Modifier = Modifier) {
    val pop = remember { Animatable(1f) }
    LaunchedEffect(stars) {
        if (stars > 0) {
            pop.snapTo(1.35f)
            pop.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }
    ClaySurface(
        color = ClayColors.Teal,
        modifier = modifier.graphicsLayer {
            scaleX = pop.value
            scaleY = pop.value
        },
        cornerRadius = Radius.pill,
        depth = 6.dp,
    ) {
        Row(
            Modifier.padding(horizontal = Space.lg, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtImage(Art.STAR, Modifier.size(28.dp))
            Spacer(Modifier.width(Space.xs))
            Text(text = "$stars", style = KidsType.title, color = ClayColors.Canvas)
        }
    }
}
