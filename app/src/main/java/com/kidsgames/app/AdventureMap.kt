package com.kidsgames.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.CelebrationOverlay
import com.kidsgames.app.ui.ClaySurface
import com.kidsgames.app.ui.Mascot
import com.kidsgames.app.ui.StarCounter
import com.kidsgames.app.ui.shade
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import com.kidsgames.app.ui.theme.Radius
import com.kidsgames.app.ui.theme.Space
import kotlin.math.max

// Map geometry: each world sits one step below the previous one, alternating sides.
private val STEP = 210.dp
private val TOP_GAP = 96.dp
private val NODE = 128.dp
private val NODE_DEPTH = 10.dp
private val COLUMN_X = listOf(0.30f, 0.70f)

/**
 * Home screen: a winding path of worlds, one per game. A world opens once the child has
 * enough stars; the next world to play pulses, and the mascot announces new worlds.
 */
@Composable
fun AdventureMap(
    firstVisit: Boolean,
    onGreeted: () -> Unit,
    /** Open worlds when the map was last shown, or null on the first showing. */
    seenOpenWorlds: Int?,
    onSeenOpenWorlds: (Int) -> Unit,
    onOpen: (World) -> Unit,
    onAlbum: () -> Unit,
    onGift: () -> Unit,
    onParents: () -> Unit,
) {
    val progress = LocalProgress.current
    val giftReady = remember { progress.giftReady(currentDay()) }
    val feedback = rememberGameFeedback()
    val openCount = WORLDS.count { progress.isWorldOpen(it) }
    // Fixed for this visit, so updating what was seen does not change the words.
    val newWorld = remember { seenOpenWorlds != null && openCount > seenOpenWorlds }
    val greeting = remember { if (firstVisit) "¡Hola! ¿A qué quieres jugar?" else "¿A qué jugamos ahora?" }
    // The world to point at: the newest open one that still has levels to play.
    val current = WORLDS.indexOfLast { progress.isWorldOpen(it) && progress.levelsDone(it.game) < LEVEL_COUNT }
    // Grows when the child taps the mascot, which makes it say hello again.
    var pokes by remember { mutableIntStateOf(0) }
    val scroll = rememberScrollState()
    val density = LocalDensity.current

    LaunchedEffect(pokes) {
        when {
            pokes > 0 -> feedback.ask("¡Hola! ¡Vamos a jugar!")
            newWorld -> feedback.win("¡Abriste un mundo nuevo!")
            else -> feedback.ask(greeting)
        }
        if (pokes == 0) {
            onGreeted()
            onSeenOpenWorlds(openCount)
        }
    }
    LaunchedEffect(Unit) {
        if (current > 0) {
            val target = with(density) { (STEP * (current - 1)).toPx() }
            scroll.animateScrollTo(max(0, target.toInt()))
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = Space.md, vertical = Space.md),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Mascot(
                    mood = feedback.mood,
                    modifier = Modifier.size(112.dp),
                    onClick = {
                        feedback.tap()
                        pokes++
                    },
                )
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    StarCounter(progress.totalStars)
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                        val pulse = rememberPulse(giftReady)
                        HeaderButton(
                            art = Art.GIFT,
                            color = if (giftReady) ClayColors.Pink else ClayColors.SurfaceStrong,
                            modifier = Modifier.graphicsLayer {
                                scaleX = pulse
                                scaleY = pulse
                            },
                            onClick = {
                                feedback.tap()
                                if (giftReady) onGift() else feedback.ask("¡Vuelve mañana para otro regalo!")
                            },
                        )
                        HeaderButton(
                            art = Art.ALBUM,
                            color = ClayColors.Lavender,
                            onClick = {
                                feedback.tap()
                                onAlbum()
                            },
                        )
                    }
                }
            }
            WorldPath(
                progress = progress,
                current = current,
                onOpen = { world ->
                    feedback.tap()
                    onOpen(world)
                },
                onLocked = { world ->
                    val missing = world.starsToOpen - progress.totalStars
                    feedback.ask(
                        if (missing == 1) "¡Te falta una estrella para abrirlo!"
                        else "¡Te faltan $missing estrellas para abrirlo!"
                    )
                },
            )
        }
        CelebrationOverlay(starBursts = feedback.starBursts, confettiShots = feedback.confettiShots)
        ParentsLockButton(
            onClick = onParents,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Space.md),
        )
    }
}

/** Round clay button with a picture, for the collection shortcuts above the map. */
@Composable
private fun HeaderButton(art: Art, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ClaySurface(
        color = color,
        modifier = modifier.size(width = 72.dp, height = 80.dp),
        onClick = onClick,
        cornerRadius = Radius.pill,
        depth = 8.dp,
    ) {
        ArtImage(art, Modifier.size(48.dp))
    }
}

@Composable
private fun WorldPath(
    progress: Progress,
    current: Int,
    onOpen: (World) -> Unit,
    onLocked: (World) -> Unit,
) {
    // Below the last circle centre: half a circle, its lip, the label and the stars.
    val height = TOP_GAP + STEP * (WORLDS.size - 1) + 160.dp
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = maxWidth
        fun centerX(index: Int) = width * COLUMN_X[index % COLUMN_X.size]
        fun centerY(index: Int) = TOP_GAP + STEP * index

        Canvas(Modifier.fillMaxSize()) {
            val points = WORLDS.indices.map { Offset(centerX(it).toPx(), centerY(it).toPx()) }
            val opened = WORLDS.map { progress.isWorldOpen(it) }
            drawTrail(points, opened)
        }

        WORLDS.forEachIndexed { index, world ->
            val open = progress.isWorldOpen(world)
            WorldNode(
                world = world,
                open = open,
                levelsDone = progress.levelsDone(world.game),
                pulsing = index == current,
                onClick = { if (open) onOpen(world) else onLocked(world) },
                modifier = Modifier.offset(
                    x = centerX(index) - NODE_LABEL_WIDTH / 2,
                    // The node's circle centre sits on the trail point.
                    y = centerY(index) - NODE / 2,
                ),
            )
        }
    }
}

/**
 * The trail between worlds: a thick clay road with a dashed centre line. The stretch
 * leading to an open world is marked in ochre, the rest stays cream.
 */
private fun DrawScope.drawTrail(points: List<Offset>, opened: List<Boolean>) {
    val road = 28.dp.toPx()
    val bend = 110.dp.toPx()
    for (i in 0 until points.size - 1) {
        val from = points[i]
        val to = points[i + 1]
        val segment = Path().apply {
            moveTo(from.x, from.y)
            cubicTo(from.x, from.y + bend, to.x, to.y - bend, to.x, to.y)
        }
        // Lip under the road, then the road itself, then the dashed line.
        translate(top = 5.dp.toPx()) {
            drawPath(segment, ClayColors.SurfaceStrong.shade(0.18f), style = Stroke(road, cap = StrokeCap.Round))
        }
        drawPath(segment, ClayColors.SurfaceStrong, style = Stroke(road, cap = StrokeCap.Round))
        drawPath(
            segment,
            if (opened[i + 1]) ClayColors.Ochre else ClayColors.Canvas,
            style = Stroke(
                width = 7.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14.dp.toPx(), 16.dp.toPx())),
            ),
        )
    }
}

private val NODE_LABEL_WIDTH = 168.dp

@Composable
private fun WorldNode(
    world: World,
    open: Boolean,
    levelsDone: Int,
    pulsing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = rememberPulse(pulsing)
    Column(
        modifier
            .width(NODE_LABEL_WIDTH)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClaySurface(
            color = if (open) world.color else ClayColors.SurfaceStrong,
            modifier = Modifier.size(width = NODE, height = NODE + NODE_DEPTH),
            onClick = onClick,
            cornerRadius = Radius.pill,
            depth = NODE_DEPTH,
        ) {
            Box(
                Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ClayColors.Canvas.copy(alpha = if (open) 0.85f else 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                ArtImage(
                    world.art,
                    Modifier
                        .size(76.dp)
                        .graphicsLayer { alpha = if (open) 1f else 0.3f },
                )
                if (!open) ArtImage(Art.LOCK, Modifier.size(48.dp), tint = ClayColors.Muted)
            }
        }
        Text(
            text = world.label,
            style = KidsType.label,
            color = if (open) ClayColors.Ink else ClayColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Space.xxs),
        )
        if (open) {
            // One star per finished level; soft dots for the levels still to play.
            StarRow(filled = levelsDone, slots = LEVEL_COUNT, starSize = 24.dp)
        } else {
            StarsToOpen(world.starsToOpen)
        }
    }
}

/** Dark pill with a star and the number of stars that opens the world. */
@Composable
internal fun StarsToOpen(stars: Int) {
    ClaySurface(
        color = ClayColors.Teal,
        cornerRadius = Radius.pill,
        depth = 4.dp,
    ) {
        Row(
            Modifier.padding(horizontal = Space.sm, vertical = Space.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtImage(Art.STAR, Modifier.size(22.dp))
            Spacer(Modifier.width(Space.xxs))
            Text("$stars", style = KidsType.label, color = ClayColors.Canvas)
        }
    }
}
