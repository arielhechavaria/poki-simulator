package com.kidsgames.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.kidsgames.app.ui.theme.ClayColors
import kotlin.math.max

/** Every illustration the games use. All of them are drawn in code, in the clay look. */
enum class Art {
    // Animals.
    DOG, CAT, COW, PIG, CHICK, FROG, LION, ELEPHANT, FISH,

    // Fruits.
    APPLE, BANANA, GRAPES, STRAWBERRY, ORANGE, WATERMELON, PEAR,

    // Shapes.
    STAR, HEART, TRIANGLE, SQUARE, CIRCLE, DIAMOND,

    // Things to count.
    CAR, BALLOON,

    // Home tiles.
    PALETTE, NUMBERS, CARDS, BLOCKS, ABACUS, NOTE, CRAYON, BRUSH, PUZZLE, SORTER,

    // Collection: the daily gift and the sticker album.
    GIFT, ALBUM,

    // Interface icons; these take the tint passed to [ArtImage].
    HOME, SPEAKER, REPLAY, LOCK, NEXT,
}

/** Draws [art] filling the smaller side of the box. [tint] only affects the interface icons. */
@Composable
fun ArtImage(art: Art, modifier: Modifier = Modifier, tint: Color = ClayColors.Ink) {
    Canvas(modifier) { drawArt(art, tint) }
}

/** A plain clay ball, used for the colour-naming game. */
@Composable
fun ClayBall(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        onGrid {
            groundShadow(30f)
            ball(color, 50f, 48f, 40f)
            ball(Color.White.copy(alpha = 0.35f), 36f, 32f, 8f)
        }
    }
}

private fun DrawScope.drawArt(art: Art, tint: Color) = onGrid {
    when (art) {
        Art.DOG -> dog()
        Art.CAT -> cat()
        Art.COW -> cow()
        Art.PIG -> pig()
        Art.CHICK -> chick()
        Art.FROG -> frog()
        Art.LION -> lion()
        Art.ELEPHANT -> elephant()
        Art.FISH -> fish()
        Art.APPLE -> apple()
        Art.BANANA -> banana()
        Art.GRAPES -> grapes()
        Art.STRAWBERRY -> strawberry()
        Art.ORANGE -> orange()
        Art.WATERMELON -> watermelon()
        Art.PEAR -> pear()
        Art.STAR -> clayPath(ClayColors.Ochre, starPath(Offset(50f, 54f), 46f), round = 5f)
        Art.HEART -> heart()
        Art.TRIANGLE -> clayPath(
            ClayColors.Mint.shade(0.12f),
            polygon(50f to 14f, 92f to 86f, 8f to 86f),
            round = 7f,
        )
        Art.SQUARE -> clayPath(
            ClayColors.Lavender,
            polygon(16f to 16f, 84f to 16f, 84f to 84f, 16f to 84f),
            round = 9f,
        )
        Art.CIRCLE -> ball(ClayColors.Coral, 50f, 50f, 40f)
        Art.DIAMOND -> clayPath(
            ClayColors.Teal.tint(0.15f),
            polygon(50f to 8f, 90f to 50f, 50f to 92f, 10f to 50f),
            round = 6f,
        )
        Art.CAR -> car()
        Art.BALLOON -> balloon()
        Art.PALETTE -> palette()
        Art.NUMBERS -> numbers()
        Art.CARDS -> cards()
        Art.BLOCKS -> blocks()
        Art.ABACUS -> abacus()
        Art.NOTE -> note()
        Art.CRAYON -> crayon()
        Art.BRUSH -> brush()
        Art.PUZZLE -> puzzle()
        Art.SORTER -> sorter()
        Art.GIFT -> gift()
        Art.ALBUM -> album()
        Art.HOME -> home(tint)
        Art.SPEAKER -> speaker(tint)
        Art.REPLAY -> replay(tint)
        Art.LOCK -> lock(tint)
        Art.NEXT -> next(tint)
    }
}

// ---------------------------------------------------------------------------------------------
// Drawing helpers. Everything below works on a 100x100 grid.
// ---------------------------------------------------------------------------------------------

/** Runs [block] on a 100x100 grid centred in the canvas and scaled to its smaller side. */
private inline fun DrawScope.onGrid(block: DrawScope.() -> Unit) {
    val unit = size.minDimension / 100f
    translate(left = (size.width - 100f * unit) / 2f, top = (size.height - 100f * unit) / 2f) {
        scale(scale = unit, pivot = Offset.Zero) { block() }
    }
}

private fun DrawScope.groundShadow(halfWidth: Float = 30f) {
    drawOval(
        color = ClayColors.Ink.copy(alpha = 0.12f),
        topLeft = Offset(50f - halfWidth, 90f),
        size = Size(halfWidth * 2f, 6f),
    )
}

private fun clayBrush(color: Color, x: Float, y: Float, w: Float, h: Float) = Brush.radialGradient(
    0f to color.tint(0.4f),
    0.6f to color,
    1f to color.shade(0.2f),
    center = Offset(x + w * 0.35f, y + h * 0.3f),
    radius = max(w, h) * 0.85f,
)

private fun DrawScope.oval(color: Color, x: Float, y: Float, w: Float, h: Float) = drawOval(
    brush = clayBrush(color, x, y, w, h),
    topLeft = Offset(x, y),
    size = Size(w, h),
)

private fun DrawScope.ball(color: Color, cx: Float, cy: Float, r: Float) =
    oval(color, cx - r, cy - r, 2f * r, 2f * r)

private fun DrawScope.roundRect(color: Color, x: Float, y: Float, w: Float, h: Float, r: Float) = drawRoundRect(
    brush = clayBrush(color, x, y, w, h),
    topLeft = Offset(x, y),
    size = Size(w, h),
    cornerRadius = CornerRadius(r, r),
)

/** Fills [path] with a light-to-dark clay gradient; [round] softens corners with a stroke. */
private fun DrawScope.clayPath(color: Color, path: Path, round: Float = 0f) {
    val b = path.getBounds()
    val brush = Brush.linearGradient(
        0f to color.tint(0.35f),
        1f to color.shade(0.18f),
        start = b.topLeft,
        end = b.bottomRight,
    )
    drawPath(path, brush)
    if (round > 0f) {
        drawPath(path, brush, style = Stroke(width = round * 2f, join = StrokeJoin.Round))
    }
}

private fun polygon(vararg points: Pair<Float, Float>): Path {
    val path = Path()
    points.forEachIndexed { i, (x, y) -> if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }
    path.close()
    return path
}

private fun DrawScope.eyes(leftX: Float, rightX: Float, y: Float, r: Float = 3.8f) {
    listOf(leftX, rightX).forEach { x ->
        drawCircle(ClayColors.Ink, radius = r, center = Offset(x, y))
        drawCircle(ClayColors.Canvas, radius = r * 0.38f, center = Offset(x - r * 0.3f, y - r * 0.35f))
    }
}

private fun DrawScope.smile(cx: Float, y: Float, width: Float) = drawArc(
    color = ClayColors.Ink,
    startAngle = 20f,
    sweepAngle = 140f,
    useCenter = false,
    topLeft = Offset(cx - width / 2f, y - width / 4f),
    size = Size(width, width / 2f),
    style = Stroke(width = 2.2f, cap = StrokeCap.Round),
)

private fun DrawScope.line(color: Color, x1: Float, y1: Float, x2: Float, y2: Float, width: Float) =
    drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = width, cap = StrokeCap.Round)

private fun DrawScope.shine(x: Float, y: Float, w: Float, h: Float) =
    drawOval(Color.White.copy(alpha = 0.4f), Offset(x, y), Size(w, h))

// ---------------------------------------------------------------------------------------------
// Animals
// ---------------------------------------------------------------------------------------------

private fun DrawScope.dog() {
    groundShadow()
    val fur = ClayColors.Peach
    oval(fur.shade(0.3f), 12f, 22f, 22f, 42f)
    oval(fur.shade(0.3f), 66f, 22f, 22f, 42f)
    ball(fur, 50f, 54f, 32f)
    oval(ClayColors.SurfaceSoft, 34f, 56f, 32f, 26f)
    oval(ClayColors.Ink, 43f, 57f, 14f, 9f)
    oval(ClayColors.Coral, 45f, 72f, 10f, 11f)
    smile(50f, 69f, 18f)
    eyes(37f, 63f, 46f)
}

private fun DrawScope.cat() {
    groundShadow()
    val fur = ClayColors.Ochre
    clayPath(fur.shade(0.1f), polygon(20f to 42f, 24f to 10f, 48f to 28f), round = 3f)
    clayPath(fur.shade(0.1f), polygon(80f to 42f, 76f to 10f, 52f to 28f), round = 3f)
    clayPath(ClayColors.Pink.tint(0.3f), polygon(27f to 32f, 28f to 20f, 38f to 28f))
    clayPath(ClayColors.Pink.tint(0.3f), polygon(73f to 32f, 72f to 20f, 62f to 28f))
    oval(fur, 14f, 26f, 72f, 62f)
    eyes(35f, 65f, 52f)
    clayPath(ClayColors.Pink, polygon(45f to 60f, 55f to 60f, 50f to 66f), round = 1.5f)
    smile(50f, 68f, 14f)
    listOf(-1f, 1f).forEach { side ->
        line(ClayColors.Body.copy(alpha = 0.7f), 50f + side * 22f, 64f, 50f + side * 42f, 59f, 1.6f)
        line(ClayColors.Body.copy(alpha = 0.7f), 50f + side * 22f, 70f, 50f + side * 42f, 74f, 1.6f)
    }
}

private fun DrawScope.cow() {
    groundShadow()
    oval(ClayColors.Ochre.tint(0.3f), 18f, 14f, 12f, 18f)
    oval(ClayColors.Ochre.tint(0.3f), 70f, 14f, 12f, 18f)
    oval(ClayColors.SurfaceStrong, 4f, 34f, 24f, 14f)
    oval(ClayColors.SurfaceStrong, 72f, 34f, 24f, 14f)
    oval(ClayColors.SurfaceSoft, 20f, 20f, 60f, 66f)
    ball(ClayColors.Ink.copy(alpha = 0.85f), 36f, 34f, 9f)
    oval(ClayColors.Pink.tint(0.25f), 28f, 58f, 44f, 28f)
    oval(ClayColors.Ink, 38f, 68f, 6f, 9f)
    oval(ClayColors.Ink, 56f, 68f, 6f, 9f)
    eyes(38f, 62f, 48f)
}

private fun DrawScope.pig() {
    groundShadow()
    val skin = ClayColors.Pink.tint(0.45f)
    clayPath(skin.shade(0.1f), polygon(22f to 40f, 20f to 12f, 46f to 26f), round = 3f)
    clayPath(skin.shade(0.1f), polygon(78f to 40f, 80f to 12f, 54f to 26f), round = 3f)
    ball(skin, 50f, 54f, 36f)
    oval(ClayColors.Pink.tint(0.1f), 33f, 52f, 34f, 26f)
    oval(ClayColors.Ink.copy(alpha = 0.75f), 41f, 60f, 6f, 10f)
    oval(ClayColors.Ink.copy(alpha = 0.75f), 53f, 60f, 6f, 10f)
    eyes(34f, 66f, 44f)
}

private fun DrawScope.chick() {
    groundShadow(24f)
    val fur = ClayColors.Ochre
    line(ClayColors.Coral, 42f, 86f, 42f, 94f, 3f)
    line(ClayColors.Coral, 58f, 86f, 58f, 94f, 3f)
    oval(fur.shade(0.15f), 8f, 52f, 20f, 26f)
    oval(fur.shade(0.15f), 72f, 52f, 20f, 26f)
    ball(fur, 50f, 56f, 34f)
    listOf(-8f to 14f, 0f to 8f, 8f to 14f).forEach { (dx, top) ->
        oval(fur.shade(0.1f), 45f + dx, top, 10f, 16f)
    }
    eyes(38f, 62f, 50f, r = 3.4f)
    clayPath(ClayColors.Coral, polygon(42f to 56f, 58f to 56f, 50f to 68f), round = 1.5f)
}

private fun DrawScope.frog() {
    groundShadow()
    val skin = ClayColors.Success
    oval(skin, 12f, 36f, 76f, 54f)
    ball(skin, 30f, 34f, 15f)
    ball(skin, 70f, 34f, 15f)
    ball(ClayColors.SurfaceSoft, 30f, 33f, 9f)
    ball(ClayColors.SurfaceSoft, 70f, 33f, 9f)
    drawCircle(ClayColors.Ink, 4.6f, Offset(31f, 34f))
    drawCircle(ClayColors.Ink, 4.6f, Offset(69f, 34f))
    ball(ClayColors.Pink.copy(alpha = 0.55f), 25f, 66f, 6f)
    ball(ClayColors.Pink.copy(alpha = 0.55f), 75f, 66f, 6f)
    smile(50f, 64f, 36f)
}

private fun DrawScope.lion() {
    groundShadow()
    val mane = ClayColors.Ochre.shade(0.4f)
    for (i in 0 until 10) {
        val angle = i * 36.0 * Math.PI / 180.0
        ball(mane, 50f + 33f * kotlin.math.cos(angle).toFloat(), 52f + 33f * kotlin.math.sin(angle).toFloat(), 13f)
    }
    ball(mane, 50f, 52f, 34f)
    ball(ClayColors.Ochre.shade(0.1f), 24f, 26f, 8f)
    ball(ClayColors.Ochre.shade(0.1f), 76f, 26f, 8f)
    ball(ClayColors.Ochre, 50f, 54f, 27f)
    oval(ClayColors.SurfaceSoft, 37f, 56f, 26f, 18f)
    clayPath(ClayColors.Ink, polygon(45f to 56f, 55f to 56f, 50f to 62f), round = 1.5f)
    smile(50f, 68f, 14f)
    eyes(40f, 60f, 48f, r = 3.4f)
}

private fun DrawScope.elephant() {
    groundShadow()
    val skin = ClayColors.Muted.tint(0.5f)
    oval(skin.shade(0.1f), 2f, 24f, 36f, 46f)
    oval(skin.shade(0.1f), 62f, 24f, 36f, 46f)
    ball(skin, 50f, 46f, 30f)
    // Trunk and tusks.
    oval(skin.shade(0.06f), 41f, 56f, 18f, 34f)
    oval(ClayColors.SurfaceSoft, 30f, 62f, 8f, 18f)
    oval(ClayColors.SurfaceSoft, 62f, 62f, 8f, 18f)
    eyes(39f, 61f, 42f, r = 3.2f)
}

private fun DrawScope.fish() {
    groundShadow(32f)
    val skin = ClayColors.Coral
    clayPath(skin.shade(0.15f), polygon(66f to 50f, 92f to 28f, 92f to 72f), round = 3f)
    clayPath(skin.shade(0.1f), polygon(36f to 32f, 52f to 14f, 62f to 34f), round = 2f)
    oval(skin, 8f, 28f, 66f, 44f)
    drawArc(
        color = ClayColors.Canvas.copy(alpha = 0.45f),
        startAngle = -60f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(40f, 34f),
        size = Size(20f, 32f),
        style = Stroke(width = 4f, cap = StrokeCap.Round),
    )
    ball(ClayColors.SurfaceSoft, 28f, 46f, 7f)
    drawCircle(ClayColors.Ink, 3.4f, Offset(27f, 46f))
    smile(20f, 58f, 10f)
}

// ---------------------------------------------------------------------------------------------
// Fruits
// ---------------------------------------------------------------------------------------------

private fun DrawScope.leaf(x: Float, y: Float, w: Float, h: Float, degrees: Float) {
    rotate(degrees, pivot = Offset(x, y + h / 2f)) { oval(ClayColors.Success, x, y, w, h) }
}

private fun DrawScope.apple() {
    groundShadow()
    line(ClayColors.Body, 50f, 28f, 53f, 12f, 3.4f)
    leaf(52f, 8f, 22f, 11f, -20f)
    ball(ClayColors.Error, 34f, 56f, 28f)
    ball(ClayColors.Error, 66f, 56f, 28f)
    oval(ClayColors.Error, 20f, 30f, 60f, 58f)
    shine(26f, 40f, 11f, 18f)
}

private fun DrawScope.banana() {
    groundShadow(34f)
    val skin = ClayColors.Ochre
    drawArc(
        color = skin.shade(0.12f),
        startAngle = 25f,
        sweepAngle = 130f,
        useCenter = false,
        topLeft = Offset(14f, 4f),
        size = Size(72f, 72f),
        style = Stroke(width = 26f, cap = StrokeCap.Round),
    )
    drawArc(
        color = skin,
        startAngle = 30f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(14f, 4f),
        size = Size(72f, 72f),
        style = Stroke(width = 20f, cap = StrokeCap.Round),
    )
    drawArc(
        color = skin.tint(0.5f),
        startAngle = 45f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(14f, 0f),
        size = Size(72f, 72f),
        style = Stroke(width = 5f, cap = StrokeCap.Round),
    )
    ball(ClayColors.Body, 17f, 58f, 5f)
    ball(ClayColors.Body, 83f, 58f, 5f)
}

private fun DrawScope.grapes() {
    groundShadow(26f)
    val skin = ClayColors.Lavender.shade(0.12f)
    line(ClayColors.Body, 50f, 24f, 52f, 10f, 3f)
    leaf(52f, 6f, 26f, 12f, -15f)
    listOf(
        34f to 34f, 50f to 34f, 66f to 34f,
        42f to 50f, 58f to 50f,
        34f to 50f, 66f to 50f,
        50f to 66f, 50f to 82f,
    ).forEachIndexed { i, (x, y) ->
        // Alternate a slightly lighter tone so the grapes read as separate balls.
        ball(skin.tint(if (i % 2 == 0) 0f else 0.1f), x, y, 11.5f)
    }
    shine(30f, 28f, 6f, 9f)
}

private fun DrawScope.strawberry() {
    groundShadow(28f)
    val body = Path().apply {
        moveTo(50f, 92f)
        cubicTo(18f, 74f, 12f, 40f, 26f, 34f)
        cubicTo(40f, 28f, 60f, 28f, 74f, 34f)
        cubicTo(88f, 40f, 82f, 74f, 50f, 92f)
        close()
    }
    clayPath(ClayColors.Error, body)
    listOf(
        36f to 48f, 54f to 46f, 66f to 56f, 28f to 62f, 46f to 62f,
        60f to 74f, 40f to 76f, 50f to 86f,
    ).forEach { (x, y) -> drawOval(ClayColors.Ochre.tint(0.3f), Offset(x - 1.6f, y - 2.6f), Size(3.2f, 5.2f)) }
    leaf(26f, 22f, 24f, 11f, 15f)
    leaf(50f, 22f, 24f, 11f, -15f)
    leaf(38f, 14f, 24f, 11f, 0f)
}

private fun DrawScope.orange() {
    groundShadow()
    leaf(52f, 10f, 24f, 12f, -20f)
    ball(ClayColors.Warning, 50f, 56f, 36f)
    listOf(36f to 62f, 56f to 50f, 64f to 72f, 44f to 78f, 70f to 58f, 34f to 48f).forEach { (x, y) ->
        drawCircle(ClayColors.Warning.shade(0.2f), 1.3f, Offset(x, y))
    }
    shine(28f, 36f, 11f, 18f)
}

private fun DrawScope.watermelon() {
    groundShadow(40f)
    val center = Offset(50f, 40f)
    fun half(color: Color, r: Float) = drawArc(
        brush = Brush.radialGradient(
            0f to color.tint(0.25f),
            1f to color.shade(0.12f),
            center = center,
            radius = r,
        ),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(center.x - r, center.y - r),
        size = Size(2f * r, 2f * r),
    )
    half(ClayColors.Success, 46f)
    half(ClayColors.Mint, 41f)
    half(ClayColors.Error, 37f)
    listOf(36f to 54f, 50f to 62f, 64f to 54f, 43f to 72f, 58f to 74f).forEach { (x, y) ->
        drawOval(ClayColors.Ink, Offset(x - 2.2f, y - 3.4f), Size(4.4f, 6.8f))
    }
}

private fun DrawScope.pear() {
    groundShadow(28f)
    val skin = ClayColors.Success.tint(0.35f)
    line(ClayColors.Body, 50f, 20f, 52f, 8f, 3.2f)
    leaf(52f, 6f, 22f, 11f, -15f)
    ball(skin, 50f, 34f, 19f)
    ball(skin, 50f, 64f, 29f)
    oval(skin, 36f, 36f, 28f, 30f)
    shine(34f, 56f, 9f, 16f)
}

// ---------------------------------------------------------------------------------------------
// Shapes and things to count
// ---------------------------------------------------------------------------------------------

private fun DrawScope.heart() {
    val path = Path().apply {
        moveTo(50f, 88f)
        cubicTo(6f, 58f, 8f, 20f, 34f, 20f)
        cubicTo(44f, 20f, 50f, 28f, 50f, 34f)
        cubicTo(50f, 28f, 56f, 20f, 66f, 20f)
        cubicTo(92f, 20f, 94f, 58f, 50f, 88f)
        close()
    }
    clayPath(ClayColors.Pink, path, round = 2f)
    shine(24f, 30f, 10f, 16f)
}

private fun DrawScope.car() {
    groundShadow(40f)
    val paint = ClayColors.Coral
    clayPath(paint.shade(0.1f), polygon(24f to 50f, 34f to 26f, 66f to 26f, 80f to 50f), round = 4f)
    clayPath(ClayColors.SurfaceSoft, polygon(32f to 48f, 38f to 33f, 49f to 33f, 49f to 48f), round = 1f)
    clayPath(ClayColors.SurfaceSoft, polygon(54f to 48f, 54f to 33f, 63f to 33f, 71f to 48f), round = 1f)
    roundRect(paint, 8f, 46f, 84f, 28f, 12f)
    listOf(30f, 70f).forEach { x ->
        ball(ClayColors.Ink, x, 76f, 11f)
        ball(ClayColors.Muted.tint(0.4f), x, 76f, 4.6f)
    }
    ball(ClayColors.Ochre, 86f, 58f, 3.6f)
}

private fun DrawScope.balloon() {
    val paint = ClayColors.Pink
    val string = Path().apply {
        moveTo(50f, 76f)
        quadraticBezierTo(40f, 84f, 52f, 90f)
        quadraticBezierTo(62f, 95f, 50f, 99f)
    }
    drawPath(string, ClayColors.Body, style = Stroke(width = 1.8f, cap = StrokeCap.Round))
    clayPath(paint.shade(0.15f), polygon(50f to 70f, 44f to 80f, 56f to 80f), round = 1.5f)
    oval(paint, 22f, 6f, 56f, 68f)
    shine(30f, 16f, 10f, 20f)
}

// ---------------------------------------------------------------------------------------------
// Home tiles
// ---------------------------------------------------------------------------------------------

private fun DrawScope.palette() {
    groundShadow(34f)
    oval(ClayColors.Peach.tint(0.15f), 6f, 12f, 88f, 74f)
    ball(ClayColors.Canvas, 36f, 66f, 9f)
    ball(ClayColors.Pink, 26f, 38f, 8.5f)
    ball(ClayColors.Ochre, 46f, 28f, 8.5f)
    ball(ClayColors.Mint, 68f, 32f, 8.5f)
    ball(ClayColors.Lavender, 78f, 54f, 8.5f)
}

private fun DrawScope.numbers() {
    groundShadow(38f)
    val colors = listOf(ClayColors.Lavender, ClayColors.Ochre, ClayColors.Pink)
    listOf(10f to 32f, 38f to 50f, 66f to 68f).forEachIndexed { i, (x, h) ->
        val top = 90f - h
        roundRect(colors[i], x, top, 24f, h, 8f)
        // One, two and three dots, so the blocks teach the count too.
        val dotsY = (0..i).map { top + 10f + it * 12f }
        dotsY.forEach { drawCircle(ClayColors.Canvas.copy(alpha = 0.9f), 3.6f, Offset(x + 12f, it)) }
    }
}

private fun DrawScope.cards() {
    groundShadow(34f)
    rotate(-12f, pivot = Offset(36f, 54f)) { roundRect(ClayColors.Lavender, 12f, 20f, 44f, 62f, 9f) }
    rotate(-12f, pivot = Offset(36f, 54f)) {
        drawCircle(ClayColors.Canvas.copy(alpha = 0.6f), 8f, Offset(34f, 51f))
    }
    rotate(10f, pivot = Offset(64f, 54f)) {
        roundRect(ClayColors.SurfaceSoft, 42f, 22f, 44f, 62f, 9f)
        clayPath(ClayColors.Ochre, starPath(Offset(64f, 53f), 14f), round = 1.5f)
    }
}

private fun DrawScope.blocks() {
    groundShadow(36f)
    val letterColor = ClayColors.Canvas.copy(alpha = 0.95f)
    rotate(-8f, pivot = Offset(32f, 62f)) {
        roundRect(ClayColors.Lavender, 8f, 36f, 46f, 46f, 9f)
        line(letterColor, 22f, 70f, 31f, 48f, 5f)
        line(letterColor, 31f, 48f, 40f, 70f, 5f)
        line(letterColor, 25f, 62f, 37f, 62f, 4f)
    }
    rotate(9f, pivot = Offset(68f, 56f)) {
        roundRect(ClayColors.Ochre, 46f, 20f, 46f, 46f, 9f)
        drawCircle(letterColor, 11f, Offset(69f, 43f), style = Stroke(width = 5f))
    }
}

private fun DrawScope.abacus() {
    groundShadow(38f)
    roundRect(ClayColors.SurfaceStrong, 8f, 14f, 84f, 72f, 12f)
    val colors = listOf(ClayColors.Coral, ClayColors.Ochre, ClayColors.Mint)
    listOf(30f, 50f, 70f).forEachIndexed { row, y ->
        line(ClayColors.Teal, 16f, y, 84f, y, 3f)
        repeat(row + 2) { i -> ball(colors[row], 26f + i * 14f, y, 6.5f) }
    }
}

private fun DrawScope.note() {
    groundShadow(30f)
    ball(ClayColors.Pink, 36f, 72f, 15f)
    roundRect(ClayColors.Pink, 46f, 18f, 8f, 56f, 3f)
    clayPath(ClayColors.Pink, polygon(50f to 18f, 80f to 34f, 80f to 46f, 50f to 32f), round = 2f)
    listOf(10f, 18f).forEach { r ->
        drawArc(
            color = ClayColors.Teal.copy(alpha = 0.55f),
            startAngle = -40f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(70f - r, 66f - r),
            size = Size(2f * r, 2f * r),
            style = Stroke(width = 3f, cap = StrokeCap.Round),
        )
    }
}

private fun DrawScope.crayon() {
    groundShadow(34f)
    // A little scribble the crayon just drew.
    val scribble = Path().apply {
        moveTo(14f, 84f)
        cubicTo(26f, 70f, 34f, 92f, 46f, 80f)
    }
    drawPath(scribble, ClayColors.Coral, style = Stroke(width = 5f, cap = StrokeCap.Round))
    rotate(-40f, pivot = Offset(52f, 48f)) {
        roundRect(ClayColors.Ochre, 42f, 8f, 20f, 58f, 5f)
        roundRect(ClayColors.Canvas.copy(alpha = 0.85f), 42f, 24f, 20f, 8f, 2f)
        clayPath(ClayColors.Ochre, polygon(42f to 66f, 62f to 66f, 52f to 86f), round = 1.5f)
    }
}

private fun DrawScope.brush() {
    groundShadow(34f)
    // A blob of paint the brush just left.
    oval(ClayColors.Pink, 10f, 70f, 36f, 18f)
    ball(ClayColors.Pink.tint(0.2f), 24f, 74f, 4f)
    rotate(40f, pivot = Offset(54f, 46f)) {
        roundRect(ClayColors.Peach.shade(0.2f), 47f, 34f, 14f, 56f, 6f)
        roundRect(ClayColors.Ochre, 45f, 28f, 18f, 12f, 3f)
        clayPath(ClayColors.Pink, polygon(45f to 30f, 63f to 30f, 59f to 8f, 49f to 8f), round = 2f)
    }
}

private fun DrawScope.puzzle() {
    groundShadow(36f)
    // Two pieces that fit together, each with a round tab.
    roundRect(ClayColors.Lavender, 10f, 24f, 44f, 56f, 8f)
    ball(ClayColors.Lavender, 54f, 52f, 8f)
    roundRect(ClayColors.Mint, 50f, 24f, 40f, 56f, 8f)
    ball(ClayColors.SurfaceSoft, 50f, 52f, 8f)
    ball(ClayColors.Ochre, 28f, 46f, 7f)
    ball(ClayColors.Coral, 72f, 58f, 7f)
    shine(16f, 30f, 14f, 6f)
}

private fun DrawScope.sorter() {
    groundShadow(36f)
    // A toy box with a round hole, a star and a square dropping in.
    roundRect(ClayColors.Peach, 12f, 44f, 76f, 44f, 10f)
    roundRect(ClayColors.Peach.tint(0.15f), 8f, 36f, 84f, 16f, 7f)
    ball(ClayColors.Teal, 34f, 70f, 9f)
    roundRect(ClayColors.Teal, 54f, 61f, 18f, 18f, 4f)
    clayPath(ClayColors.Ochre, polygon(66f to 6f, 70f to 18f, 83f to 18f, 73f to 26f, 77f to 38f, 66f to 31f, 55f to 38f, 59f to 26f, 49f to 18f, 62f to 18f), round = 2f)
    shine(18f, 52f, 14f, 6f)
}

private fun DrawScope.gift() {
    groundShadow(34f)
    roundRect(ClayColors.Pink, 14f, 42f, 72f, 46f, 9f)
    roundRect(ClayColors.Pink.tint(0.12f), 10f, 28f, 80f, 22f, 8f)
    // Ribbon: one band down the box and a bow on top.
    roundRect(ClayColors.Ochre, 43f, 28f, 14f, 60f, 4f)
    rotate(-28f, pivot = Offset(40f, 20f)) { oval(ClayColors.Ochre, 22f, 10f, 28f, 18f) }
    rotate(28f, pivot = Offset(60f, 20f)) { oval(ClayColors.Ochre, 50f, 10f, 28f, 18f) }
    ball(ClayColors.Ochre.shade(0.12f), 50f, 24f, 6.5f)
    shine(20f, 34f, 14f, 6f)
}

private fun DrawScope.album() {
    groundShadow(36f)
    roundRect(ClayColors.Teal.tint(0.1f), 10f, 12f, 80f, 76f, 10f)
    roundRect(ClayColors.Canvas, 20f, 20f, 62f, 60f, 6f)
    // Three stickers stuck on the page.
    ball(ClayColors.Pink, 38f, 38f, 11f)
    clayPath(ClayColors.Ochre, starPath(Offset(65f, 38f), 12f), round = 1.5f)
    roundRect(ClayColors.Mint, 28f, 56f, 24f, 16f, 6f)
    ball(ClayColors.Lavender, 68f, 64f, 9f)
}

// ---------------------------------------------------------------------------------------------
// Interface icons
// ---------------------------------------------------------------------------------------------

private fun DrawScope.home(tint: Color) {
    clayPath(tint, polygon(50f to 10f, 94f to 50f, 6f to 50f), round = 4f)
    roundRect(tint, 20f, 46f, 60f, 44f, 6f)
    roundRect(ClayColors.Canvas, 42f, 62f, 16f, 28f, 5f)
}

private fun DrawScope.speaker(tint: Color) {
    drawPath(
        polygon(10f to 38f, 28f to 38f, 50f to 18f, 50f to 82f, 28f to 62f, 10f to 62f),
        tint,
    )
    val wave = Stroke(width = 6f, cap = StrokeCap.Round)
    drawArc(tint, -50f, 100f, false, Offset(40f, 32f), Size(32f, 36f), style = wave)
    drawArc(tint, -50f, 100f, false, Offset(34f, 16f), Size(52f, 68f), style = wave)
}

private fun DrawScope.replay(tint: Color) {
    val arc = Stroke(width = 12f, cap = StrokeCap.Round)
    drawArc(tint, startAngle = -60f, sweepAngle = 300f, useCenter = false, topLeft = Offset(16f, 16f), size = Size(68f, 68f), style = arc)
    // Arrow head where the circle ends (top left), pointing along the clockwise direction.
    drawPath(polygon(52f to 16f, 28f to 14f, 41f to 38f), tint)
}

private fun DrawScope.lock(tint: Color) {
    val shackle = Stroke(width = 11f, cap = StrokeCap.Round)
    drawArc(tint, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(30f, 12f), size = Size(40f, 40f), style = shackle)
    drawLine(tint, Offset(30f, 32f), Offset(30f, 46f), strokeWidth = 11f)
    drawLine(tint, Offset(70f, 32f), Offset(70f, 46f), strokeWidth = 11f)
    roundRect(tint, 16f, 44f, 68f, 48f, 12f)
    drawCircle(ClayColors.Canvas, 7f, Offset(50f, 63f))
    roundRect(ClayColors.Canvas, 46f, 64f, 8f, 16f, 3f)
}

/** A rounded "play" arrow pointing right. */
private fun DrawScope.next(tint: Color) {
    val arrow = polygon(32f to 18f, 82f to 50f, 32f to 82f)
    drawPath(arrow, tint)
    drawPath(arrow, tint, style = Stroke(width = 12f, join = StrokeJoin.Round))
}
