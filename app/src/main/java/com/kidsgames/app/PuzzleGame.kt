package com.kidsgames.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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

private const val PUZZLE_PROMPT = "Arma el rompecabezas. Arrastra cada pieza a su lugar"

/** The pictures cut into pieces: all drawn by the app. */
private val PUZZLE_ARTS = listOf(
    Art.DOG, Art.CAT, Art.LION, Art.ELEPHANT, Art.FISH, Art.PIG, Art.COW, Art.FROG, Art.CHICK,
)

/**
 * A picture is cut into 4, 6 or 9 pieces lying shuffled below the board. The child drags
 * each one to its place; the board shows a faint copy of the picture to follow.
 */
@Composable
fun PuzzleGame(level: Int, run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var mistakes by remember(round) { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }

    val arts = remember(level) { PUZZLE_ARTS.shuffled() }
    val art = arts[round % arts.size]
    val cols = if (level == 1) 2 else 3
    val count = when (level) {
        1 -> 4
        2 -> 6
        else -> 9
    }

    LaunchedEffect(round) { feedback.ask(PUZZLE_PROMPT) }

    GameScaffold(
        onHome = onHome,
        stars = LocalProgress.current.totalStars,
        mood = feedback.mood,
        starBursts = feedback.starBursts,
        confettiShots = feedback.confettiShots,
        roundsDone = run.roundsDone,
        roundsTotal = run.rounds,
    ) {
        Spacer(Modifier.height(Space.md))
        ClayButton(
            onClick = { feedback.ask(PUZZLE_PROMPT) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 88.dp, height = 94.dp),
            color = ClayColors.Ochre,
        ) {
            ArtImage(Art.SPEAKER, Modifier.size(44.dp))
        }
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = Space.sm)
        ) {
            val density = LocalDensity.current
            val width = with(density) { maxWidth.toPx() }
            val height = with(density) { maxHeight.toPx() }
            val layout = remember(round, width, height) {
                fitLayout(width, height, cols, count, gap = with(density) { Space.lg.toPx() }, maxCell = with(density) { 150.dp.toPx() })
            }
            DragFit(
                layout = layout,
                slotContent = { index -> PuzzlePiece(art, cols, count / cols, index, layout.cell, ghost = true) },
                pieceContent = { index -> PuzzlePiece(art, cols, count / cols, index, layout.cell, ghost = false) },
                onPlaced = { _, all ->
                    if (all && !locked) {
                        locked = true
                        feedback.right("¡Lo lograste! ¡Qué bonito quedó!")
                        val paid = starsForMistakes(mistakes)
                        scope.launch {
                            delay(1800)
                            run.finishRound(paid)
                            locked = false
                            round++
                        }
                    } else {
                        feedback.tap()
                    }
                },
                onMiss = {
                    mistakes++
                    feedback.wrong()
                },
            )
        }
    }
}

/** One cell of the picture; [ghost] is the faint hint printed on the board itself. */
@Composable
private fun PuzzlePiece(art: Art, cols: Int, rows: Int, index: Int, cellPx: Float, ghost: Boolean) {
    val cell = with(LocalDensity.current) { cellPx.toDp() }
    val col = index % cols
    val row = index / cols
    Box(
        Modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (ghost) ClayColors.SurfaceStrong else ClayColors.SurfaceSoft)
    ) {
        // The whole picture is laid out at full size and shifted so only this cell shows.
        Box(
            Modifier
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .offset(x = -(cell * col), y = -(cell * row))
                .size(cell * cols, cell * rows)
                .graphicsLayer { alpha = if (ghost) 0.28f else 1f }
        ) {
            ArtImage(art, Modifier.fillMaxSize())
        }
    }
}
