package com.kidsgames.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One answer option: either a coloured circle or an emoji, plus what the voice says. */
data class Choice(
    val spoken: String,
    val emoji: String? = null,
    val color: Color? = null,
)

val COLOR_ITEMS = listOf(
    Choice("el rojo", color = Color(0xFFE53935)),
    Choice("el azul", color = Color(0xFF1E88E5)),
    Choice("el verde", color = Color(0xFF43A047)),
    Choice("el amarillo", color = Color(0xFFFDD835)),
    Choice("el naranja", color = Color(0xFFFB8C00)),
    Choice("el morado", color = Color(0xFF8E24AA)),
    Choice("el rosa", color = Color(0xFFEC407A)),
)

val ANIMAL_ITEMS = listOf(
    Choice("el perro", "🐶"),
    Choice("el gato", "🐱"),
    Choice("la vaca", "🐮"),
    Choice("el cerdo", "🐷"),
    Choice("el pollito", "🐥"),
    Choice("la rana", "🐸"),
    Choice("el león", "🦁"),
    Choice("el elefante", "🐘"),
    Choice("el pez", "🐟"),
)

val FRUIT_ITEMS = listOf(
    Choice("la manzana", "🍎"),
    Choice("el plátano", "🍌"),
    Choice("las uvas", "🍇"),
    Choice("la fresa", "🍓"),
    Choice("la naranja", "🍊"),
    Choice("la sandía", "🍉"),
    Choice("la pera", "🍐"),
)

val SHAPE_ITEMS = listOf(
    Choice("la estrella", "⭐"),
    Choice("el corazón", "❤️"),
    Choice("el triángulo", "🔺"),
    Choice("el cuadrado", "⬛"),
    Choice("el círculo", "⭕"),
    Choice("el rombo", "🔷"),
)

@Composable
private fun ChoiceVisual(choice: Choice) {
    if (choice.color != null) {
        Box(
            Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(choice.color)
        )
    } else {
        Text(choice.emoji.orEmpty(), fontSize = 72.sp)
    }
}

/**
 * Generic "find the one the voice asks for" game. Colours, animals, fruits and
 * shapes all reuse it by passing a different item list.
 */
@Composable
fun ChoiceGame(items: List<Choice>, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var stars by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }

    val target = remember(round) { items.random() }
    val options = remember(round) {
        (items - target).shuffled().take(3).plus(target).shuffled()
    }

    LaunchedEffect(round) { feedback.ask("Toca " + target.spoken) }

    GameScaffold(
        onHome = onHome,
        stars = stars,
        mood = feedback.mood,
        starBursts = feedback.starBursts,
        confettiShots = feedback.confettiShots,
    ) {
        Spacer(Modifier.height(16.dp))
        ClayButton(
            onClick = { feedback.ask("Toca " + target.spoken) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 88.dp, height = 94.dp),
            color = ClayColors.Ochre,
        ) {
            Text("🔊", fontSize = 40.sp)
        }
        Spacer(Modifier.height(8.dp))
        options.chunked(2).forEach { rowOptions ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowOptions.forEach { choice ->
                    ClayCard(
                        color = ClayColors.SurfaceCard,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        enabled = !locked,
                        onClick = {
                            if (choice == target) {
                                locked = true
                                stars++
                                feedback.right("¡Muy bien!")
                                scope.launch {
                                    delay(1200)
                                    locked = false
                                    round++
                                }
                            } else {
                                feedback.wrong()
                            }
                        },
                    ) {
                        ChoiceVisual(choice)
                    }
                }
            }
        }
    }
}
