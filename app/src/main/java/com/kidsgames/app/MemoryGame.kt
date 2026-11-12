package com.kidsgames.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.sp
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import kotlinx.coroutines.delay

private val MEMORY_EMOJIS = listOf("🐶", "🐱", "🐸", "🐥", "🍎", "🍌", "🚗", "⭐")

/** Find the pairs: 3 pairs (6 cards) keeps it easy for a 4-year-old. */
@Composable
fun MemoryGame(onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    var round by remember { mutableIntStateOf(0) }
    var stars by remember { mutableIntStateOf(0) }

    val cards = remember(round) {
        MEMORY_EMOJIS.shuffled().take(3).let { picked -> (picked + picked).shuffled() }
    }
    val faceUp = remember(round) { mutableStateListOf<Int>() }
    val matched = remember(round) { mutableStateListOf<Int>() }
    val won = matched.size == cards.size

    LaunchedEffect(faceUp.size) {
        if (faceUp.size == 2) {
            delay(900)
            if (cards[faceUp[0]] == cards[faceUp[1]]) {
                matched.addAll(faceUp.toList())
                // The last pair is celebrated by the win effect below instead.
                if (matched.size < cards.size) feedback.right("¡Muy bien!")
            } else {
                feedback.miss()
            }
            faceUp.clear()
        }
    }

    LaunchedEffect(won) {
        if (won) {
            stars++
            feedback.win("¡Lo lograste!")
        }
    }

    GameScaffold(
        onHome = onHome,
        stars = stars,
        mood = feedback.mood,
        starBursts = feedback.starBursts,
        confettiShots = feedback.confettiShots,
    ) {
        cards.indices.toList().chunked(3).forEach { rowIndices ->
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
                        Text(if (visible) cards[index] else "❓", fontSize = 56.sp)
                    }
                }
            }
        }
        if (won) {
            ClayButton(
                onClick = { round++ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
            ) {
                Text("🔄", fontSize = 48.sp)
            }
        }
    }
}
