package com.kidsgames.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

private const val SORT_PROMPT = "Pon cada forma en su lugar"

/**
 * Loose shapes lie below a board of faint outlines; the child drags each shape into the
 * outline of the same shape. 3 shapes at level 1, then 4, then 6.
 */
@Composable
fun SortGame(level: Int, run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var mistakes by remember(round) { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }

    val count = when (level) {
        1 -> 3
        2 -> 4
        else -> 6
    }
    val cols = if (count == 4) 2 else 3
    val shapes = remember(round) { SHAPE_ITEMS.shuffled().take(count) }

    LaunchedEffect(round) { feedback.ask(SORT_PROMPT) }

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
            onClick = { feedback.ask(SORT_PROMPT) },
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
                fitLayout(width, height, cols, count, gap = with(density) { Space.xl.toPx() }, maxCell = with(density) { 120.dp.toPx() })
            }
            DragFit(
                layout = layout,
                slotContent = { index ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(ClayColors.SurfaceStrong)
                    ) {
                        ArtImage(
                            shapes[index].art ?: Art.STAR,
                            Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .graphicsLayer { alpha = 0.3f },
                        )
                    }
                },
                pieceContent = { index ->
                    ArtImage(shapes[index].art ?: Art.STAR, Modifier.fillMaxSize().padding(8.dp))
                },
                onPlaced = { index, all ->
                    if (all && !locked) {
                        locked = true
                        feedback.right("¡Todas en su lugar! ¡Muy bien!")
                        val paid = starsForMistakes(mistakes)
                        scope.launch {
                            delay(1500)
                            run.finishRound(paid)
                            locked = false
                            round++
                        }
                    } else {
                        feedback.tap()
                        feedback.ask(shapes[index].spoken)
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
