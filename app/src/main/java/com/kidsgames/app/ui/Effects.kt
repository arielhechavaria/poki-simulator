package com.kidsgames.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.theme.ClayColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val PARTY_COLORS = listOf(
    ClayColors.Pink,
    ClayColors.Lavender,
    ClayColors.Peach,
    ClayColors.Ochre,
    ClayColors.Mint,
    ClayColors.Coral,
)

private const val STAR_BURST_MS = 900
private const val CONFETTI_MS = 2400

/** A five-pointed star centered on [center], pointing up. */
internal fun starPath(center: Offset, outerRadius: Float, innerRadius: Float = outerRadius * 0.46f): Path {
    val path = Path()
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = -PI / 2 + i * PI / 5
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private class BurstStar(val angle: Float, val reach: Float, val scale: Float, val color: Color)

private class ConfettiPiece(
    val x: Float,
    val speed: Float,
    val sway: Float,
    val swayPhase: Float,
    val spin: Float,
    val scale: Float,
    val color: Color,
)

/**
 * Full-screen celebration layer. It plays a star burst each time [starBursts] grows and a
 * confetti rain each time [confettiShots] grows. It draws nothing at rest and never takes
 * touches, so it can sit on top of a game.
 */
@Composable
fun CelebrationOverlay(starBursts: Int, confettiShots: Int, modifier: Modifier = Modifier) {
    // 1 = finished. Each effect restarts from 0 when its counter changes.
    val burst = remember { Animatable(1f) }
    val rain = remember { Animatable(1f) }

    LaunchedEffect(starBursts) {
        if (starBursts > 0) {
            burst.snapTo(0f)
            burst.animateTo(1f, tween(STAR_BURST_MS, easing = LinearEasing))
        }
    }
    LaunchedEffect(confettiShots) {
        if (confettiShots > 0) {
            rain.snapTo(0f)
            rain.animateTo(1f, tween(CONFETTI_MS, easing = LinearEasing))
        }
    }

    val stars = remember(starBursts) {
        List(12) { index ->
            BurstStar(
                angle = (index / 12f + Random.nextFloat() * 0.05f) * 2f * PI.toFloat(),
                reach = 0.65f + Random.nextFloat() * 0.35f,
                scale = 0.7f + Random.nextFloat() * 0.6f,
                color = if (index % 3 == 0) ClayColors.Pink else ClayColors.Ochre,
            )
        }
    }
    val pieces = remember(confettiShots) {
        List(70) {
            ConfettiPiece(
                x = Random.nextFloat(),
                speed = 0.75f + Random.nextFloat() * 0.5f,
                sway = 0.02f + Random.nextFloat() * 0.05f,
                swayPhase = Random.nextFloat() * 2f * PI.toFloat(),
                spin = 1f + Random.nextFloat() * 3f,
                scale = 0.7f + Random.nextFloat() * 0.6f,
                color = PARTY_COLORS.random(),
            )
        }
    }

    Canvas(modifier.fillMaxSize()) {
        if (burst.value < 1f) drawStarBurst(stars, burst.value)
        if (rain.value < 1f) drawConfetti(pieces, rain.value)
    }
}

private fun DrawScope.drawStarBurst(stars: List<BurstStar>, progress: Float) {
    val origin = Offset(size.width / 2f, size.height * 0.45f)
    val maxReach = size.minDimension * 0.46f
    // Fast at first, then slowing down as the stars fly out.
    val travel = 1f - (1f - progress) * (1f - progress)
    val alpha = (1f - progress * progress).coerceIn(0f, 1f)
    val grow = sin(progress * PI.toFloat()).coerceAtLeast(0f)
    stars.forEach { star ->
        val distance = travel * star.reach * maxReach
        val center = Offset(
            origin.x + cos(star.angle) * distance,
            origin.y + sin(star.angle) * distance,
        )
        val radius = 18.dp.toPx() * star.scale * (0.4f + 0.6f * grow)
        rotate(degrees = progress * 140f, pivot = center) {
            drawPath(starPath(center, radius), star.color, alpha = alpha)
        }
    }
}

private fun DrawScope.drawConfetti(pieces: List<ConfettiPiece>, progress: Float) {
    val alpha = ((1f - progress) / 0.2f).coerceIn(0f, 1f)
    val pieceWidth = 10.dp.toPx()
    val pieceHeight = 16.dp.toPx()
    pieces.forEach { piece ->
        val fall = progress * piece.speed
        val x = (piece.x + piece.sway * sin(fall * 9f + piece.swayPhase)) * size.width
        val y = -pieceHeight + fall * (size.height + pieceHeight * 2f)
        // Squashing the width fakes the paper flipping as it falls.
        val flip = abs(cos(fall * piece.spin * 2f * PI.toFloat()))
        val width = pieceWidth * piece.scale * (0.2f + 0.8f * flip)
        val height = pieceHeight * piece.scale
        rotate(degrees = fall * piece.spin * 200f, pivot = Offset(x, y)) {
            drawRoundRect(
                color = piece.color,
                topLeft = Offset(x - width / 2f, y - height / 2f),
                size = Size(width, height),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                alpha = alpha,
            )
        }
    }
}
