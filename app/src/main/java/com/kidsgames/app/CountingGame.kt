package com.kidsgames.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val NUMBER_COLORS = listOf(ClayColors.Mint, ClayColors.Peach, ClayColors.Lavender)
private val COUNT_ART = listOf(Art.APPLE, Art.STAR, Art.FISH, Art.CAR, Art.BALLOON, Art.CHICK)

/** Highest count and number of answer buttons for each level. */
private fun countingSetup(level: Int): Pair<Int, Int> = when (level) {
    1 -> 3 to 2
    2 -> 5 to 3
    else -> 9 to 3
}

/** Shows some objects; the child taps the matching number. Counts grow with the level. */
@Composable
fun CountingGame(level: Int, run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var mistakes by remember(round) { mutableIntStateOf(0) }
    val (maxCount, optionCount) = countingSetup(level)

    val count = remember(round) { Random.nextInt(1, maxCount + 1) }
    val art = remember(round) { COUNT_ART.random() }
    val options = remember(round) {
        // Wrong answers come from just past the range too, so 3 is not always the biggest.
        (1..maxCount + 1).filter { it != count }.shuffled().take(optionCount - 1).plus(count).shuffled()
    }

    LaunchedEffect(round) { feedback.ask("¿Cuántos hay?") }

    GameScaffold(
        onHome = onHome,
        stars = LocalProgress.current.totalStars,
        mood = feedback.mood,
        starBursts = feedback.starBursts,
        confettiShots = feedback.confettiShots,
        roundsDone = run.roundsDone,
        roundsTotal = run.rounds,
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            List(count) { art }.chunked(3).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.Center) {
                    rowItems.forEach { item -> ArtImage(item, Modifier.size(84.dp)) }
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            options.forEachIndexed { index, number ->
                ClayCard(
                    color = NUMBER_COLORS[index % NUMBER_COLORS.size],
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    enabled = !locked,
                    onClick = {
                        if (number == count) {
                            locked = true
                            val paid = starsForMistakes(mistakes)
                            feedback.right("$count. ¡Muy bien!")
                            scope.launch {
                                delay(1500)
                                run.finishRound(paid)
                                locked = false
                                round++
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
