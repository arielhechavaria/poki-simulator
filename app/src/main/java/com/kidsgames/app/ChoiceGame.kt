package com.kidsgames.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.ClayBall
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One answer option: either a coloured ball or an illustration, plus what the voice says. */
data class Choice(
    val spoken: String,
    val art: Art? = null,
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
    Choice("el perro", Art.DOG),
    Choice("el gato", Art.CAT),
    Choice("la vaca", Art.COW),
    Choice("el cerdo", Art.PIG),
    Choice("el pollito", Art.CHICK),
    Choice("la rana", Art.FROG),
    Choice("el león", Art.LION),
    Choice("el elefante", Art.ELEPHANT),
    Choice("el pez", Art.FISH),
)

val FRUIT_ITEMS = listOf(
    Choice("la manzana", Art.APPLE),
    Choice("el plátano", Art.BANANA),
    Choice("las uvas", Art.GRAPES),
    Choice("la fresa", Art.STRAWBERRY),
    Choice("la naranja", Art.ORANGE),
    Choice("la sandía", Art.WATERMELON),
    Choice("la pera", Art.PEAR),
)

val SHAPE_ITEMS = listOf(
    Choice("la estrella", Art.STAR),
    Choice("el corazón", Art.HEART),
    Choice("el triángulo", Art.TRIANGLE),
    Choice("el cuadrado", Art.SQUARE),
    Choice("el círculo", Art.CIRCLE),
    Choice("el rombo", Art.DIAMOND),
)

@Composable
private fun ChoiceVisual(choice: Choice) {
    when {
        choice.color != null -> ClayBall(choice.color, Modifier.size(120.dp))
        choice.art != null -> ArtImage(choice.art, Modifier.size(120.dp))
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
            ArtImage(Art.SPEAKER, Modifier.size(44.dp))
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
