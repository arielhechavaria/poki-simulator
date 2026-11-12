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
import com.kidsgames.app.ui.theme.KidsType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One answer option: either a coloured ball or an illustration, plus what the voice says. */
data class Choice(
    val spoken: String,
    val art: Art? = null,
    val color: Color? = null,
    /** A letter or number drawn as big text. */
    val text: String? = null,
    /** What the animal says, for the sounds game. */
    val sound: String? = null,
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

val VOWEL_ITEMS = listOf("A", "E", "I", "O", "U").map { Choice("la $it", text = "$it${it.lowercase()}") }

val LETTER_ITEMS = listOf("A", "E", "I", "O", "U", "M", "P", "S", "L", "T", "B", "D")
    .map { Choice("la $it", text = "$it${it.lowercase()}") }

val NUMBER_ITEMS_EASY = (1..5).map { Choice("el $it", text = "$it") }
val NUMBER_ITEMS_ALL = (1..10).map { Choice("el $it", text = "$it") }

/** Animals that have a well-known sound, for the sounds game. */
val SOUND_ITEMS = listOf(
    Choice("el perro", Art.DOG, sound = "¡Guau, guau!"),
    Choice("el gato", Art.CAT, sound = "¡Miau, miau!"),
    Choice("la vaca", Art.COW, sound = "¡Muuu, muuu!"),
    Choice("el cerdo", Art.PIG, sound = "¡Oinc, oinc!"),
    Choice("el pollito", Art.CHICK, sound = "¡Pío, pío!"),
    Choice("la rana", Art.FROG, sound = "¡Cruac, cruac!"),
    Choice("el león", Art.LION, sound = "¡Roaaar!"),
    Choice("el elefante", Art.ELEPHANT, sound = "¡Prrrrr!"),
)

@Composable
private fun ChoiceVisual(choice: Choice) {
    when {
        choice.text != null -> Text(choice.text, style = KidsType.number, color = ClayColors.Ink)
        choice.color != null -> ClayBall(choice.color, Modifier.size(120.dp))
        choice.art != null -> ArtImage(choice.art, Modifier.size(120.dp))
    }
}

/**
 * Generic "find the one the voice asks for" game. Colours, animals, fruits and
 * shapes all reuse it by passing a different item list. Level 1 shows 2 options,
 * level 2 shows 3 and level 3 shows 4.
 */
@Composable
fun ChoiceGame(
    items: List<Choice>,
    level: Int,
    run: LevelRun,
    onHome: () -> Unit,
    promptOf: (Choice) -> String = { "Toca " + it.spoken },
) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var mistakes by remember(round) { mutableIntStateOf(0) }
    val optionCount = (level + 1).coerceIn(2, 4)

    val target = remember(round) { items.random() }
    val options = remember(round) {
        (items - target).shuffled().take(optionCount - 1).plus(target).shuffled()
    }

    LaunchedEffect(round) { feedback.ask(promptOf(target)) }

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
            onClick = { feedback.ask(promptOf(target)) },
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
                // A lone card keeps the same width as the others, centred.
                if (rowOptions.size == 1) Spacer(Modifier.weight(0.5f))
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
                                val paid = starsForMistakes(mistakes)
                                feedback.right("¡Muy bien!")
                                scope.launch {
                                    delay(1200)
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
                        ChoiceVisual(choice)
                    }
                }
                if (rowOptions.size == 1) Spacer(Modifier.weight(0.5f))
            }
        }
    }
}
