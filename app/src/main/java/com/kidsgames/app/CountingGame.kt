package com.kidsgames.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.sp
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val NUMBER_COLORS = listOf(ClayColors.Mint, ClayColors.Peach, ClayColors.Lavender)
private val COUNT_EMOJIS = listOf("🍎", "⭐", "🐟", "🚗", "🎈", "🐥")

/** Shows 1-9 objects; the child taps the matching number. */
@Composable
fun CountingGame(onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var stars by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }

    val count = remember(round) { Random.nextInt(1, 10) }
    val emoji = remember(round) { COUNT_EMOJIS.random() }
    val options = remember(round) {
        (1..9).filter { it != count }.shuffled().take(2).plus(count).shuffled()
    }

    LaunchedEffect(round) { feedback.ask("¿Cuántos hay?") }

    GameScaffold(
        onHome = onHome,
        stars = stars,
        mood = feedback.mood,
        starBursts = feedback.starBursts,
        confettiShots = feedback.confettiShots,
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            List(count) { emoji }.chunked(3).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.Center) {
                    rowItems.forEach { item -> Text(item, fontSize = 64.sp) }
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
                            stars++
                            feedback.right("$count. ¡Muy bien!")
                            scope.launch {
                                delay(1500)
                                locked = false
                                round++
                            }
                        } else {
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
