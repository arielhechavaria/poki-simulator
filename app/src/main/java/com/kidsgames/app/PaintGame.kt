package com.kidsgames.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.Radius
import com.kidsgames.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val PAINT_COLORS = listOf(
    ClayColors.Pink, ClayColors.Coral, ClayColors.Ochre, ClayColors.Mint, ClayColors.Lavender,
)

private val STAMPS = listOf(Art.STAR, Art.HEART, Art.APPLE, Art.DOG, Art.CAT)

/** Most stamps kept on the sheet, so a very long session never slows the screen down. */
private const val MAX_STAMPS = 80

private class PaintStroke(val color: Color, val width: Float) {
    val points = mutableStateListOf<Offset>()
}

private class PlacedStamp(val art: Art, val x: Float, val y: Float)

/** A part of a colouring picture, in the 100x100 picture grid. */
private sealed interface Part {
    fun path(): Path
    fun contains(point: Offset): Boolean
}

private class OvalPart(val x: Float, val y: Float, val w: Float, val h: Float) : Part {
    override fun path() = Path().apply { addOval(Rect(x, y, x + w, y + h)) }
    override fun contains(point: Offset): Boolean {
        val dx = (point.x - (x + w / 2f)) / (w / 2f)
        val dy = (point.y - (y + h / 2f)) / (h / 2f)
        return dx * dx + dy * dy <= 1f
    }
}

private class RectPart(val x: Float, val y: Float, val w: Float, val h: Float) : Part {
    override fun path() = Path().apply { addRect(Rect(x, y, x + w, y + h)) }
    override fun contains(point: Offset) = point.x in x..(x + w) && point.y in y..(y + h)
}

private class PolyPart(val points: List<Offset>) : Part {
    override fun path() = Path().apply {
        points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
        close()
    }

    // Ray casting: a point is inside when a ray to the right crosses the edges an odd number of times.
    override fun contains(point: Offset): Boolean {
        var inside = false
        var j = points.size - 1
        for (i in points.indices) {
            val a = points[i]
            val b = points[j]
            if ((a.y > point.y) != (b.y > point.y) &&
                point.x < (b.x - a.x) * (point.y - a.y) / (b.y - a.y) + a.x
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}

/** Painted back to front, so later parts sit on top of earlier ones. */
private val PICTURES: List<List<Part>> = listOf(
    // A flower.
    listOf(
        RectPart(46f, 50f, 8f, 42f),
        OvalPart(26f, 68f, 22f, 12f),
        OvalPart(54f, 62f, 22f, 12f),
        OvalPart(42f, 6f, 16f, 22f),
        OvalPart(42f, 36f, 16f, 22f),
        OvalPart(22f, 24f, 22f, 16f),
        OvalPart(56f, 24f, 22f, 16f),
        OvalPart(40f, 22f, 20f, 20f),
    ),
    // A house under the sun.
    listOf(
        RectPart(20f, 48f, 60f, 42f),
        PolyPart(listOf(Offset(12f, 50f), Offset(50f, 16f), Offset(88f, 50f))),
        RectPart(42f, 64f, 16f, 26f),
        OvalPart(26f, 56f, 14f, 14f),
        OvalPart(60f, 56f, 14f, 14f),
        OvalPart(74f, 4f, 20f, 20f),
    ),
    // A caterpillar on a leaf.
    listOf(
        OvalPart(6f, 74f, 88f, 16f),
        OvalPart(8f, 48f, 24f, 24f),
        OvalPart(26f, 48f, 24f, 24f),
        OvalPart(44f, 48f, 24f, 24f),
        OvalPart(70f, 40f, 28f, 28f),
        OvalPart(8f, 8f, 22f, 22f),
    ),
)

/**
 * Three ways to make art, one per level: finger painting with brushes, stamping, and
 * colouring a ready-made picture. Nothing can go wrong; a finished sheet always earns 3 stars.
 */
@Composable
fun PaintGame(level: Int, run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var round by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(PAINT_COLORS[0]) }
    var thick by remember { mutableStateOf(true) }
    var stamp by remember { mutableStateOf(STAMPS[0]) }

    val strokes = remember(round) { mutableStateListOf<PaintStroke>() }
    val stamps = remember(round) { mutableStateListOf<PlacedStamp>() }
    val parts = PICTURES[round % PICTURES.size]
    val fills = remember(round) { mutableStateListOf<Color?>().apply { repeat(parts.size) { add(null) } } }

    val prompt = when (level) {
        1 -> "Pinta lo que quieras con tu dedo"
        2 -> "Elige un sello y toca la hoja"
        else -> "Colorea el dibujo. Toca cada parte"
    }
    LaunchedEffect(round) { feedback.ask(prompt) }

    fun finish(praise: String) {
        if (locked) return
        locked = true
        feedback.win(praise)
        scope.launch {
            delay(2200)
            run.finishRound(3)
            locked = false
            round++
        }
    }

    fun clearSheet() {
        strokes.clear()
        stamps.clear()
        for (i in fills.indices) fills[i] = null
        feedback.tap()
    }

    val brushWidth = with(density) { (if (thick) 36.dp else 14.dp).toPx() }
    val stampSize = 72.dp
    val stampPx = with(density) { stampSize.toPx() }
    val stampSpacing = with(density) { 56.dp.toPx() }

    fun addStamp(position: Offset) {
        if (locked) return
        if (stamps.size >= MAX_STAMPS) stamps.removeAt(0)
        stamps.add(PlacedStamp(stamp, position.x - stampPx / 2f, position.y - stampPx / 2f))
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
        // Tools along the top.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = Space.md),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            ClayButton(
                onClick = { feedback.ask(prompt) },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                color = ClayColors.Ochre,
            ) { ArtImage(Art.SPEAKER, Modifier.size(40.dp)) }
            if (level == 1) {
                ClayButton(
                    onClick = {
                        thick = !thick
                        feedback.tap()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    color = ClayColors.Lavender,
                ) {
                    Box(
                        Modifier
                            .size(if (thick) 36.dp else 14.dp)
                            .clip(CircleShape)
                            .background(ClayColors.Ink)
                    )
                }
            }
            ClayButton(
                onClick = { clearSheet() },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                color = ClayColors.Peach,
            ) { ArtImage(Art.REPLAY, Modifier.size(40.dp)) }
            if (level < 3) {
                ClayButton(
                    onClick = {
                        if (strokes.isEmpty() && stamps.isEmpty()) {
                            feedback.ask(prompt)
                        } else {
                            finish("¡Qué dibujo tan bonito!")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    color = ClayColors.Mint,
                ) { ArtImage(Art.NEXT, Modifier.size(40.dp)) }
            }
        }

        // The sheet.
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = Space.sm)
                .clip(RoundedCornerShape(Radius.xl))
                .background(ClayColors.SurfaceSoft)
                .border(2.dp, ClayColors.Hairline, RoundedCornerShape(Radius.xl))
                .pointerInput(level, round, brushWidth) {
                    when (level) {
                        1 -> {
                            // A tap leaves a dot; a drag leaves a line.
                            detectTapGestures { position ->
                                val dot = PaintStroke(color, brushWidth)
                                dot.points.add(position)
                                strokes.add(dot)
                            }
                        }
                        2 -> detectTapGestures { position ->
                            feedback.tap()
                            addStamp(position)
                        }
                        else -> detectTapGestures { position ->
                            if (locked) return@detectTapGestures
                            val edge = minOf(size.width, size.height).toFloat()
                            val unit = edge / 100f
                            val left = (size.width - edge) / 2f
                            val top = (size.height - edge) / 2f
                            val grid = Offset((position.x - left) / unit, (position.y - top) / unit)
                            val hit = parts.indexOfLast { it.contains(grid) }
                            if (hit >= 0) {
                                fills[hit] = color
                                if (fills.all { it != null }) {
                                    finish("¡Qué bonito quedó!")
                                } else {
                                    feedback.tap()
                                }
                            }
                        }
                    }
                }
                .pointerInput(level, round, brushWidth) {
                    if (level == 3) return@pointerInput
                    var last = Offset.Zero
                    detectDragGestures(
                        onDragStart = { position ->
                            if (level == 1) {
                                val line = PaintStroke(color, brushWidth)
                                line.points.add(position)
                                strokes.add(line)
                            } else {
                                last = position
                                addStamp(position)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (level == 1) {
                                strokes.lastOrNull()?.points?.add(change.position)
                            } else if ((change.position - last).getDistance() >= stampSpacing) {
                                last = change.position
                                addStamp(change.position)
                            }
                        },
                    )
                }
        ) {
            if (level == 3) {
                Canvas(Modifier.fillMaxSize()) {
                    val edge = minOf(size.width, size.height)
                    val unit = edge / 100f
                    translate(left = (size.width - edge) / 2f, top = (size.height - edge) / 2f) {
                        scale(scale = unit, pivot = Offset.Zero) {
                            parts.forEachIndexed { index, part ->
                                val path = part.path()
                                drawPath(path, fills[index] ?: ClayColors.Canvas)
                                drawPath(
                                    path,
                                    ClayColors.Ink,
                                    style = Stroke(width = 1.8f, join = StrokeJoin.Round),
                                )
                            }
                        }
                    }
                }
            } else {
                Canvas(Modifier.fillMaxSize()) {
                    strokes.forEach { stroke ->
                        val points = stroke.points
                        if (points.size == 1) {
                            drawCircle(stroke.color, radius = stroke.width / 2f, center = points[0])
                        } else if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                            }
                            drawPath(
                                path,
                                stroke.color,
                                style = Stroke(width = stroke.width, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            )
                        }
                    }
                }
                stamps.forEach { placed ->
                    ArtImage(
                        placed.art,
                        Modifier
                            .offset { IntOffset(placed.x.roundToInt(), placed.y.roundToInt()) }
                            .size(stampSize),
                    )
                }
            }
        }

        // The palette: colours, or stamps at level 2.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = Space.xs),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            if (level == 2) {
                STAMPS.forEach { art ->
                    ClayButton(
                        onClick = {
                            stamp = art
                            feedback.tap()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp),
                        color = if (art == stamp) ClayColors.Mint else ClayColors.SurfaceCard,
                    ) { ArtImage(art, Modifier.size(48.dp)) }
                }
            } else {
                PAINT_COLORS.forEach { paint ->
                    ClayButton(
                        onClick = {
                            color = paint
                            feedback.tap()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp),
                        color = paint,
                    ) {
                        if (paint == color) {
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(ClayColors.Canvas)
                            )
                        }
                    }
                }
            }
        }
    }
}
