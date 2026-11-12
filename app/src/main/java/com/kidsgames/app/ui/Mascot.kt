package com.kidsgames.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import com.kidsgames.app.ui.theme.ClayColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** What the mascot is doing. Every mood loops until the caller changes it. */
enum class MascotMood {
    /** Breathing and blinking. */
    IDLE,

    /** Mouth moving while the voice speaks. */
    TALKING,

    /** Hopping with the arms up after a right answer. */
    CHEERING,

    /** Head tilted, still talking: the gentle reaction to a wrong answer. */
    ENCOURAGING,
}

/** Extras the mascot can wear, unlocked with stars. Saved by name: never rename an entry. */
enum class Accessory(val label: String, val starsToUnlock: Int) {
    BOW("el moño", 10),
    GLASSES("las gafas", 25),
    HAT("el gorro", 45),
    CROWN("la corona", 70),
}

/** What the mascot wears everywhere it shows up; provided once at the top of the app. */
val LocalMascotAccessory = compositionLocalOf<Accessory?> { null }

/**
 * The app's own character: a lavender clay blob with a sprout on its head. Drawn on a
 * 100x100 grid scaled to the smaller side, so it stays sharp at any size.
 */
@Composable
fun Mascot(
    mood: MascotMood,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accessory: Accessory? = LocalMascotAccessory.current,
) {
    // One looping clock drives every periodic motion; all of them use whole cycles
    // per loop, so nothing jumps when the clock restarts.
    val clock = rememberInfiniteTransition(label = "mascotClock")
    val time by clock.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "mascotTime",
    )
    val cheer by animateFloatAsState(
        targetValue = if (mood == MascotMood.CHEERING) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "mascotCheer",
    )
    val talk by animateFloatAsState(
        targetValue = if (mood == MascotMood.TALKING || mood == MascotMood.ENCOURAGING) 1f else 0f,
        animationSpec = tween(160),
        label = "mascotTalk",
    )
    val tilt by animateFloatAsState(
        targetValue = if (mood == MascotMood.ENCOURAGING) 1f else 0f,
        animationSpec = tween(300),
        label = "mascotTilt",
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Canvas(modifier.then(clickModifier)) {
        val unit = size.minDimension / 100f
        translate(
            left = (size.width - 100f * unit) / 2f,
            top = (size.height - 100f * unit) / 2f,
        ) {
            drawMascot(unit = unit, time = time, cheer = cheer, talk = talk, tilt = tilt, accessory = accessory)
        }
    }
}

private fun DrawScope.drawMascot(
    unit: Float,
    time: Float,
    cheer: Float,
    talk: Float,
    tilt: Float,
    accessory: Accessory?,
) {
    fun at(x: Float, y: Float) = Offset(x * unit, y * unit)

    val body = ClayColors.Lavender
    val phase = time * 2f * PI.toFloat()
    val breath = sin(phase)
    val cheerAmount = cheer.coerceIn(0f, 1f)
    val hop = cheerAmount * abs(sin(phase * 3f))
    val blinking = time in 0.90f..0.95f

    // Shadow stays on the ground and shrinks while the mascot is in the air.
    val shadowHalf = 22f * (1f - 0.3f * hop)
    drawOval(
        color = ClayColors.Ink.copy(alpha = 0.12f),
        topLeft = at(50f - shadowHalf, 89f),
        size = Size(shadowHalf * 2f * unit, 6f * unit),
    )

    val lean = tilt * (7f + 2f * sin(phase * 2f)) + cheerAmount * 4f * sin(phase * 3f)
    translate(top = -hop * 9f * unit) {
        rotate(degrees = lean, pivot = at(50f, 88f)) {
            // Feet.
            listOf(38f, 62f).forEach { x ->
                drawOval(
                    color = body.shade(0.25f),
                    topLeft = at(x - 9f, 83.5f),
                    size = Size(18f * unit, 9f * unit),
                )
            }

            // Arms hang at the sides and go up when cheering.
            val armX = 18f - 3f * cheer
            val armY = 64f - 26f * cheer + cheerAmount * 2f * sin(phase * 6f)
            listOf(armX, 100f - armX).forEach { x ->
                drawCircle(color = body.shade(0.10f), radius = 7.5f * unit, center = at(x, armY))
            }

            // Sprout: a stem and two leaves that sway.
            val sway = 6f * breath
            drawLine(
                color = ClayColors.Mint.shade(0.35f),
                start = at(50f, 31f),
                end = at(50f, 19f),
                strokeWidth = 2.6f * unit,
                cap = StrokeCap.Round,
            )
            rotate(degrees = -32f + sway, pivot = at(50f, 20f)) {
                drawOval(ClayColors.Mint, topLeft = at(37f, 16f), size = Size(13f * unit, 8f * unit))
            }
            rotate(degrees = 32f + sway, pivot = at(50f, 20f)) {
                drawOval(
                    color = ClayColors.Mint.shade(0.12f),
                    topLeft = at(50f, 16f),
                    size = Size(13f * unit, 8f * unit),
                )
            }

            // Body: breathes from the feet up, lit from the top left.
            val bodyHeight = 60f * (1f + 0.025f * breath)
            drawOval(
                brush = Brush.radialGradient(
                    0f to body.tint(0.45f),
                    0.55f to body,
                    1f to body.shade(0.22f),
                    center = at(40f, 44f),
                    radius = 48f * unit,
                ),
                topLeft = at(19f, 88f - bodyHeight),
                size = Size(62f * unit, bodyHeight * unit),
            )
            // Belly patch.
            drawOval(
                color = ClayColors.Canvas.copy(alpha = 0.45f),
                topLeft = at(34f, 66f),
                size = Size(32f * unit, 19f * unit),
            )

            // Cheeks.
            listOf(29f, 71f).forEach { x ->
                drawCircle(
                    color = ClayColors.Pink.copy(alpha = 0.5f),
                    radius = 5f * unit,
                    center = at(x, 61f),
                )
            }

            drawEyes(unit, happy = cheerAmount > 0.5f, blinking = blinking)
            drawMouth(unit, cheering = cheerAmount > 0.5f, talk = talk, phase = phase)
            if (accessory != null) drawAccessory(unit, accessory)
        }
    }
}

private fun DrawScope.drawEyes(unit: Float, happy: Boolean, blinking: Boolean) {
    val stroke = Stroke(width = 2.6f * unit, cap = StrokeCap.Round)
    listOf(39f, 61f).forEach { x ->
        val y = 52f
        when {
            // Smiling eyes: an upward arc.
            happy -> drawArc(
                color = ClayColors.Ink,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset((x - 5f) * unit, (y - 4f) * unit),
                size = Size(10f * unit, 9f * unit),
                style = stroke,
            )

            blinking -> drawLine(
                color = ClayColors.Ink,
                start = Offset((x - 5f) * unit, y * unit),
                end = Offset((x + 5f) * unit, y * unit),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round,
            )

            else -> {
                drawCircle(ClayColors.Ink, radius = 5.5f * unit, center = Offset(x * unit, y * unit))
                drawCircle(
                    color = ClayColors.Canvas,
                    radius = 1.8f * unit,
                    center = Offset((x - 1.6f) * unit, (y - 1.9f) * unit),
                )
            }
        }
    }
}

private fun DrawScope.drawMouth(unit: Float, cheering: Boolean, talk: Float, phase: Float) {
    when {
        // Wide open smile with a tongue.
        cheering -> {
            drawArc(
                color = ClayColors.Ink,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(41f * unit, 52f * unit),
                size = Size(18f * unit, 18f * unit),
            )
            drawArc(
                color = ClayColors.Coral,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(45f * unit, 60f * unit),
                size = Size(10f * unit, 9f * unit),
            )
        }

        // Mouth opening and closing with the voice.
        talk > 0.05f -> {
            val open = talk * (0.35f + 0.65f * abs(sin(phase * 5f)))
            val halfHeight = 1.5f + 5f * open
            drawOval(
                color = ClayColors.Ink,
                topLeft = Offset(44.5f * unit, (64f - halfHeight) * unit),
                size = Size(11f * unit, halfHeight * 2f * unit),
            )
        }

        else -> drawArc(
            color = ClayColors.Ink,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(43f * unit, 57f * unit),
            size = Size(14f * unit, 10f * unit),
            style = Stroke(width = 2.6f * unit, cap = StrokeCap.Round),
        )
    }
}

/** Drawn on top of the body, in the mascot's own 100x100 grid. */
private fun DrawScope.drawAccessory(unit: Float, accessory: Accessory) {
    fun at(x: Float, y: Float) = Offset(x * unit, y * unit)
    fun polygon(vararg points: Pair<Float, Float>) = Path().apply {
        points.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * unit, y * unit) else lineTo(x * unit, y * unit) }
        close()
    }

    when (accessory) {
        Accessory.BOW -> {
            // A coral bow on the right side of the head.
            rotate(-24f, pivot = at(66f, 33f)) {
                drawOval(ClayColors.Coral, topLeft = at(52f, 27f), size = Size(14f * unit, 12f * unit))
            }
            rotate(24f, pivot = at(66f, 33f)) {
                drawOval(ClayColors.Coral.shade(0.1f), topLeft = at(66f, 27f), size = Size(14f * unit, 12f * unit))
            }
            drawCircle(ClayColors.Coral.shade(0.22f), radius = 4.2f * unit, center = at(66f, 33f))
        }

        Accessory.GLASSES -> {
            val rim = Stroke(width = 2.4f * unit, cap = StrokeCap.Round)
            listOf(39f, 61f).forEach { x ->
                drawCircle(ClayColors.Teal, radius = 9f * unit, center = at(x, 52f), style = rim)
                drawCircle(Color.White.copy(alpha = 0.22f), radius = 8f * unit, center = at(x, 52f))
            }
            drawLine(ClayColors.Teal, at(48f, 51f), at(52f, 51f), strokeWidth = 2.4f * unit, cap = StrokeCap.Round)
        }

        Accessory.HAT -> {
            // A party hat with a pompom.
            drawPath(polygon(38f to 33f, 62f to 33f, 50f to 4f), ClayColors.Pink)
            drawPath(polygon(44f to 20f, 56f to 20f, 53f to 13f, 47f to 13f), ClayColors.Ochre)
            drawCircle(ClayColors.Ochre, radius = 4f * unit, center = at(50f, 5f))
        }

        Accessory.CROWN -> {
            drawPath(
                polygon(36f to 34f, 36f to 20f, 43f to 27f, 50f to 17f, 57f to 27f, 64f to 20f, 64f to 34f),
                ClayColors.Ochre,
            )
            drawRect(ClayColors.Ochre.shade(0.15f), topLeft = at(36f, 30f), size = Size(28f * unit, 4f * unit))
            drawCircle(ClayColors.Pink, radius = 2.2f * unit, center = at(50f, 25f))
        }
    }
}
