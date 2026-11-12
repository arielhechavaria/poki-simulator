package com.kidsgames.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.kidsgames.app.ui.MascotMood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ENCOURAGEMENTS = listOf(
    "Casi, casi. Inténtalo otra vez",
    "Uy, ese no. Prueba con otro",
    "Tú puedes. Busca otra vez",
)

/**
 * Everything a game does to react to the child: voice, sound effect, mascot mood and
 * celebration effects. The three game engines share it so they all feel the same.
 */
@Stable
class GameFeedback(
    private val speaker: Speaker,
    private val sfx: Sfx,
    private val scope: CoroutineScope,
) {
    var mood by mutableStateOf(MascotMood.IDLE)
        private set

    /** Grows by one per star burst; feed it to the celebration overlay. */
    var starBursts by mutableIntStateOf(0)
        private set

    /** Grows by one per confetti rain; feed it to the celebration overlay. */
    var confettiShots by mutableIntStateOf(0)
        private set

    private var moodJob: Job? = null

    /** The mascot asks the question of the round. */
    fun ask(text: String) {
        speaker.say(text)
        hold(MascotMood.TALKING, speechMillis(text))
    }

    /** A light tap, such as turning a card. */
    fun tap() {
        sfx.play(Sound.TAP)
    }

    /** Right answer: chime, praise, star burst and a cheering mascot. */
    fun right(praise: String) {
        sfx.play(Sound.RIGHT)
        speaker.say(praise)
        starBursts++
        hold(MascotMood.CHEERING, 1300)
    }

    /** Wrong answer: a soft sound and spoken encouragement, never a failure. */
    fun wrong() {
        val text = ENCOURAGEMENTS.random()
        sfx.play(Sound.WRONG)
        speaker.say(text)
        hold(MascotMood.ENCOURAGING, speechMillis(text))
    }

    /** A harmless miss that needs no words, such as two cards that do not match. */
    fun miss() {
        sfx.play(Sound.WRONG)
        hold(MascotMood.ENCOURAGING, 900)
    }

    /** The whole board is done: fanfare, confetti and a longer cheer. */
    fun win(praise: String) {
        sfx.play(Sound.WIN)
        speaker.say(praise)
        starBursts++
        confettiShots++
        hold(MascotMood.CHEERING, 2600)
    }

    private fun hold(newMood: MascotMood, millis: Long) {
        moodJob?.cancel()
        moodJob = scope.launch {
            mood = newMood
            delay(millis)
            mood = MascotMood.IDLE
        }
    }

    // The voice gives no simple "finished" signal here, so estimate from the text length.
    private fun speechMillis(text: String): Long = 400L + 75L * text.length
}

@Composable
fun rememberGameFeedback(): GameFeedback {
    val speaker = rememberSpeaker()
    val sfx = LocalSfx.current
    val scope = rememberCoroutineScope()
    return remember(speaker, sfx, scope) { GameFeedback(speaker, sfx, scope) }
}
