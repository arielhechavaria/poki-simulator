package com.kidsgames.app

import androidx.compose.foundation.layout.Arrangement
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

private const val ORDER_PROMPT = "Toca los números del más chico al más grande"

/**
 * Four numbers from 1 to 10 appear shuffled; the child taps them from smallest to biggest.
 * A wrong tap is only a gentle nudge, and the numbers already placed stay placed.
 */
@Composable
fun OrderGame(run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var mistakes by remember(round) { mutableIntStateOf(0) }
    // How many numbers were tapped in the right order so far.
    var placed by remember(round) { mutableIntStateOf(0) }

    val numbers = remember(round) { (1..10).shuffled().take(4) }
    val sorted = remember(round) { numbers.sorted() }

    LaunchedEffect(round) { feedback.ask(ORDER_PROMPT) }

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
            onClick = { feedback.ask(ORDER_PROMPT) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 88.dp, height = 94.dp),
            color = ClayColors.Ochre,
        ) {
            ArtImage(Art.SPEAKER, Modifier.size(44.dp))
        }
        Spacer(Modifier.height(8.dp))
        numbers.chunked(2).forEach { rowNumbers ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowNumbers.forEach { number ->
                    val done = sorted.indexOf(number) < placed
                    ClayCard(
                        color = if (done) ClayColors.Mint else ClayColors.SurfaceCard,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        enabled = !locked && !done,
                        onClick = {
                            if (number == sorted[placed]) {
                                placed++
                                if (placed == sorted.size) {
                                    locked = true
                                    val paid = starsForMistakes(mistakes)
                                    feedback.right("$number. ¡Muy bien!")
                                    scope.launch {
                                        delay(1500)
                                        run.finishRound(paid)
                                        locked = false
                                        round++
                                    }
                                } else {
                                    feedback.tap()
                                    feedback.ask("$number")
                                }
                            } else {
                                mistakes++
                                feedback.wrong()
                            }
                        },
                    ) {
                        Text("$number", style = KidsType.number, color = ClayColors.Ink)
                    }
                }
            }
        }
    }
}
