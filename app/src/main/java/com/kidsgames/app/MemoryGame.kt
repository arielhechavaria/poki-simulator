package com.kidsgames.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val MEMORY_EMOJIS = listOf("🐶", "🐱", "🐸", "🐥", "🍎", "🍌", "🚗", "⭐")

/** Find the pairs: 3 pairs (6 cards) keeps it easy for a 4-year-old. */
@Composable
fun MemoryGame(onHome: () -> Unit) {
    val speaker = rememberSpeaker()
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
                speaker.say("¡Muy bien!")
            }
            faceUp.clear()
        }
    }

    LaunchedEffect(won) {
        if (won) {
            stars++
            speaker.say("¡Lo lograste!")
        }
    }

    GameScaffold(onHome = onHome, stars = stars) {
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
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (visible) Color.White else Color(0xFF1E88E5))
                            .clickable {
                                if (!won && faceUp.size < 2 && index !in faceUp && index !in matched) {
                                    faceUp.add(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (visible) cards[index] else "❓", fontSize = 56.sp)
                    }
                }
            }
        }
        if (won) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF43A047))
                    .clickable { round++ },
                contentAlignment = Alignment.Center
            ) {
                Text("🔄", fontSize = 48.sp)
            }
        }
    }
}
