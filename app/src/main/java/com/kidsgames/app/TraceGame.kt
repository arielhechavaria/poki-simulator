package com.kidsgames.app

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Letters to trace, from simple strokes to rounder and busier ones. */
private fun traceLetters(level: Int): List<String> = when (level) {
    1 -> listOf("I", "L", "T", "O", "U")
    2 -> listOf("A", "E", "I", "O", "U")
    else -> listOf("M", "P", "S", "B", "D", "C")
}

/** Share of the letter that must be painted before the round is won. */
private const val TRACE_GOAL = 0.8f

/** The letter as a filled outline that fits [size], plus points spread inside it. */
private class LetterShape(val path: androidx.compose.ui.graphics.Path, val samples: List<Offset>)

private fun buildLetter(letter: String, size: IntSize, step: Float): LetterShape? {
    if (size.width <= 0 || size.height <= 0) return null
    val paint = Paint().apply {
        typeface = Typeface.DEFAULT_BOLD
        textSize = 400f
    }
    val raw = android.graphics.Path()
    paint.getTextPath(letter, 0, letter.length, 0f, 0f, raw)
    val bounds = RectF()
    raw.computeBounds(bounds, true)
    val margin = 0.1f
    val target = RectF(
        size.width * margin,
        size.height * margin,
        size.width * (1f - margin),
        size.height * (1f - margin),
    )
    val matrix = Matrix().apply { setRectToRect(bounds, target, Matrix.ScaleToFit.CENTER) }
    raw.transform(matrix)

    val region = Region().apply { setPath(raw, Region(0, 0, size.width, size.height)) }
    val samples = ArrayList<Offset>()
    var y = step / 2f
    while (y < size.height) {
        var x = step / 2f
        while (x < size.width) {
            if (region.contains(x.toInt(), y.toInt())) samples.add(Offset(x, y))
            x += step
        }
        y += step
    }
    return LetterShape(raw.asComposePath(), samples)
}

/**
 * The child paints a big letter with a finger. The paint stays inside the letter and the
 * round ends once most of it is covered; there is no way to get it wrong.
 */
@Composable
fun TraceGame(level: Int, run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var round by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    val letters = remember(level) { traceLetters(level).shuffled() }
    val letter = letters[round % letters.size]

    val brush = with(density) { 44.dp.toPx() }
    val reach = with(density) { 30.dp.toPx() }
    val step = with(density) { 20.dp.toPx() }
    val shape = remember(letter, boxSize) { buildLetter(letter, boxSize, step) }
    val covered = remember(shape) { BooleanArray(shape?.samples?.size ?: 0) }
    var coveredCount by remember(shape) { mutableIntStateOf(0) }
    // A null entry lifts the finger, so separate strokes are not joined by a line.
    val points: SnapshotStateList<Offset?> = remember(letter, round) { mutableStateListOf() }

    val prompt = "Pinta la letra $letter con tu dedo"
    LaunchedEffect(round) { feedback.ask(prompt) }

    fun paintAt(position: Offset) {
        val current = shape ?: return
        if (locked) return
        points.add(position)
        val reach2 = reach * reach
        current.samples.forEachIndexed { index, sample ->
            if (!covered[index]) {
                val dx = sample.x - position.x
                val dy = sample.y - position.y
                if (dx * dx + dy * dy <= reach2) {
                    covered[index] = true
                    coveredCount++
                }
            }
        }
        if (current.samples.isNotEmpty() && coveredCount >= current.samples.size * TRACE_GOAL) {
            locked = true
            feedback.right("¡Muy bien! Esa es la $letter")
            scope.launch {
                delay(1500)
                run.finishRound(3)
                locked = false
                round++
            }
        }
    }

    GameScaffold(
        onHome = onHome,
        stars = LocalProgress.current.totalStars,
        mood = feedback.mood,
        starBursts = feedback.starBursts,
        confettiShots = feedback.confettiShots,
        roundsDone = run.roundsDone,
        roundsTotal = run.rounds,
    ) {
        Spacer(Modifier.height(16.dp))
        ClayButton(
            onClick = { feedback.ask(prompt) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 88.dp, height = 94.dp),
            color = ClayColors.Ochre,
        ) {
            ArtImage(Art.SPEAKER, Modifier.size(44.dp))
        }
        Spacer(Modifier.height(8.dp))
        Canvas(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { boxSize = it }
                .pointerInput(shape) {
                    detectDragGestures(
                        onDragStart = { paintAt(it) },
                        onDragEnd = { points.add(null) },
                        onDragCancel = { points.add(null) },
                        onDrag = { change, _ ->
                            change.consume()
                            paintAt(change.position)
                        },
                    )
                }
        ) {
            val letterPath = shape?.path ?: return@Canvas
            drawPath(letterPath, ClayColors.SurfaceStrong)
            clipPath(letterPath) {
                var previous: Offset? = null
                points.forEach { point ->
                    if (point != null && previous != null) {
                        drawLine(
                            color = ClayColors.Coral,
                            start = previous!!,
                            end = point,
                            strokeWidth = brush,
                            cap = StrokeCap.Round,
                        )
                    } else if (point != null) {
                        drawCircle(ClayColors.Coral, radius = brush / 2f, center = point)
                    }
                    previous = point
                }
            }
            drawPath(
                letterPath,
                ClayColors.Teal.copy(alpha = 0.35f),
                style = Stroke(width = 4f, join = StrokeJoin.Round),
            )
        }
    }
}
