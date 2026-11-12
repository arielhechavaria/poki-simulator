package com.kidsgames.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One step of a daily routine: its picture and what the voice says when it is tapped. */
private data class Step(val art: Art, val spoken: String)

/** A routine in the right order. Level 1 uses the first three steps, later levels all four. */
private data class Routine(val intro: String, val steps: List<Step>)

private val ROUTINES = listOf(
    Routine(
        "Por la mañana",
        listOf(
            Step(Art.SUN, "Me despierto"),
            Step(Art.TOOTHBRUSH, "Me lavo los dientes"),
            Step(Art.BOWL, "Desayuno"),
            Step(Art.SHIRT, "Me visto"),
        ),
    ),
    Routine(
        "Por la noche",
        listOf(
            Step(Art.BOWL, "Ceno"),
            Step(Art.TOOTHBRUSH, "Me lavo los dientes"),
            Step(Art.BED, "Me acuesto"),
            Step(Art.MOON, "Duermo"),
        ),
    ),
)

/**
 * A day's routine appears shuffled; the child taps the pictures in the order they happen.
 * A wrong tap is a gentle nudge, and the pictures already placed stay placed with their number.
 */
@Composable
fun RoutineGame(level: Int, run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var mistakes by remember(round) { mutableIntStateOf(0) }
    // How many steps were tapped in the right order so far.
    var placed by remember(round) { mutableIntStateOf(0) }

    val routine = remember(round) { ROUTINES[round % ROUTINES.size] }
    val steps = remember(round) { routine.steps.take(if (level <= 1) 3 else 4) }
    val shuffled = remember(round) { steps.shuffled() }
    val prompt = "${routine.intro}. ¿Qué haces primero? Toca en orden"

    LaunchedEffect(round) { feedback.ask(prompt) }

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
        shuffled.chunked(2).forEach { rowSteps ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // A lone card keeps the same width as the others, centred.
                if (rowSteps.size == 1) Spacer(Modifier.weight(0.5f))
                rowSteps.forEach { step ->
                    val position = steps.indexOf(step)
                    val done = position < placed
                    ClayCard(
                        color = if (done) ClayColors.Mint else ClayColors.SurfaceCard,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        enabled = !locked && !done,
                        onClick = {
                            if (position == placed) {
                                placed++
                                if (placed == steps.size) {
                                    locked = true
                                    val paid = starsForMistakes(mistakes)
                                    feedback.right("${step.spoken}. ¡Muy bien!")
                                    scope.launch {
                                        delay(1600)
                                        run.finishRound(paid)
                                        locked = false
                                        round++
                                    }
                                } else {
                                    feedback.tap()
                                    feedback.ask(step.spoken)
                                }
                            } else {
                                mistakes++
                                feedback.wrong()
                            }
                        },
                    ) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(), contentAlignment = Alignment.Center) {
                            ArtImage(step.art, Modifier.size(120.dp))
                            if (done) {
                                Text(
                                    "${position + 1}",
                                    style = KidsType.display,
                                    color = ClayColors.Ink,
                                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                )
                            }
                        }
                    }
                }
                if (rowSteps.size == 1) Spacer(Modifier.weight(0.5f))
            }
        }
    }
}
