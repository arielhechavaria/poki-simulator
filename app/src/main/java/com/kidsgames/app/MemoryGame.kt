package com.kidsgames.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import kotlinx.coroutines.delay

private val MEMORY_ART = listOf(Art.DOG, Art.CAT, Art.FROG, Art.CHICK, Art.APPLE, Art.BANANA, Art.CAR, Art.STAR)

/** Board stars: few misses for its size pay 3, a few more pay 2, and any finish pays 1. */
private fun starsForMisses(misses: Int, pairs: Int): Int = when {
    misses <= pairs -> 3
    misses <= pairs * 2 -> 2
    else -> 1
}

/** Find the pairs: 2 pairs on level 1, 3 on level 2 and 4 on level 3. */
@Composable
fun MemoryGame(level: Int, run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    var round by remember { mutableIntStateOf(0) }
    val pairs = (level + 1).coerceIn(2, 4)
    // Six cards fit best in rows of three; four and eight in rows of two.
    val columns = if (pairs == 3) 3 else 2

    val cards = remember(round) {
        MEMORY_ART.shuffled().take(pairs).let { picked -> (picked + picked).shuffled() }
    }
    val faceUp = remember(round) { mutableStateListOf<Int>() }
    val matched = remember(round) { mutableStateListOf<Int>() }
    var misses by remember(round) { mutableIntStateOf(0) }
    val won = matched.size == cards.size

    LaunchedEffect(faceUp.size) {
        if (faceUp.size == 2) {
            delay(900)
            if (cards[faceUp[0]] == cards[faceUp[1]]) {
                matched.addAll(faceUp.toList())
                // The last pair is celebrated by the win effect below instead.
                if (matched.size < cards.size) feedback.right("¡Muy bien!")
            } else {
                misses++
                feedback.miss()
            }
            faceUp.clear()
        }
    }

    LaunchedEffect(won, round) {
        if (won) {
            feedback.win("¡Lo lograste!")
            delay(2200)
            run.finishRound(starsForMisses(misses, pairs))
            round++
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
        cards.indices.toList().chunked(columns).forEach { rowIndices ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowIndices.forEach { index ->
                    val visible = index in faceUp || index in matched
                    ClayCard(
                        color = if (visible) ClayColors.SurfaceCard else ClayColors.Lavender,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = {
                            if (!won && faceUp.size < 2 && index !in faceUp && index !in matched) {
                                feedback.tap()
                                faceUp.add(index)
                            }
                        },
                    ) {
                        if (visible) {
                            ArtImage(cards[index], Modifier.fillMaxSize(0.75f))
                        } else {
                            Text("?", style = KidsType.number, color = ClayColors.Canvas)
                        }
                    }
                }
            }
        }
    }
}
