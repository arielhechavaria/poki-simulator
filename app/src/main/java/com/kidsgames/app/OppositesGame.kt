package com.kidsgames.app

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
import com.kidsgames.app.ui.ClayBall
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.GameScaffold
import com.kidsgames.app.ui.theme.ClayColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What is compared in a question. */
private enum class Kind { SIZE, HEIGHT, AMOUNT }

/**
 * One question: two cards, the child taps the one that matches the spoken word.
 */
private class Question(
    val kind: Kind,
    val prompt: String,
    /** The card index (0 left, 1 right) that is right. */
    val answer: Int,
    val art: Art,
    /** For AMOUNT: how many items the many/few cards show, indexed by card. */
    val counts: IntArray,
    /** For SIZE: size in dp per card. For HEIGHT: 0 = high, 1 = low per card. */
    val values: IntArray,
)

private val SIZE_ART = listOf(Art.DOG, Art.CAT, Art.ELEPHANT, Art.APPLE, Art.CAR, Art.FISH)
private val AMOUNT_ART = listOf(Art.BALLOON, Art.STAR, Art.APPLE)

private fun newQuestion(kinds: List<Kind>): Question {
    val kind = kinds.random()
    val answer = (0..1).random()
    return when (kind) {
        Kind.SIZE -> {
            val big = (0..1).random() == 0
            val sizes = if (answer == 0) intArrayOf(if (big) 130 else 60, if (big) 60 else 130)
            else intArrayOf(if (big) 60 else 130, if (big) 130 else 60)
            Question(kind, if (big) "Toca el grande" else "Toca el pequeño", answer, SIZE_ART.random(), IntArray(2), sizes)
        }
        Kind.HEIGHT -> {
            val up = (0..1).random() == 0
            // 0 = high, 1 = low.
            val spots = if (up) IntArray(2) { if (it == answer) 0 else 1 } else IntArray(2) { if (it == answer) 1 else 0 }
            Question(kind, if (up) "Toca la pelota de arriba" else "Toca la pelota de abajo", answer, Art.CIRCLE, IntArray(2), spots)
        }
        Kind.AMOUNT -> {
            val many = (0..1).random() == 0
            val manyCount = (5..7).random()
            val fewCount = (1..2).random()
            val counts = IntArray(2) { if ((it == answer) == many) manyCount else fewCount }
            Question(kind, if (many) "Toca donde hay muchos" else "Toca donde hay pocos", answer, AMOUNT_ART.random(), counts, IntArray(2))
        }
    }
}

/**
 * Opposites: big and small, up and down, many and few. Level 1 compares sizes, level 2 adds
 * amounts, level 3 adds up and down. A wrong tap is only a gentle nudge.
 */
@Composable
fun OppositesGame(level: Int, run: LevelRun, onHome: () -> Unit) {
    val feedback = rememberGameFeedback()
    val scope = rememberCoroutineScope()
    var round by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var mistakes by remember(round) { mutableIntStateOf(0) }
    val kinds = when {
        level <= 1 -> listOf(Kind.SIZE)
        level == 2 -> listOf(Kind.SIZE, Kind.AMOUNT)
        else -> listOf(Kind.SIZE, Kind.AMOUNT, Kind.HEIGHT)
    }
    val question = remember(round) { newQuestion(kinds) }

    LaunchedEffect(round) { feedback.ask(question.prompt) }

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
            onClick = { feedback.ask(question.prompt) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 88.dp, height = 94.dp),
            color = ClayColors.Ochre,
        ) {
            ArtImage(Art.SPEAKER, Modifier.size(44.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            (0..1).forEach { index ->
                ClayCard(
                    color = ClayColors.SurfaceCard,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    enabled = !locked,
                    onClick = {
                        if (index == question.answer) {
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
                    OppositeCard(question, index)
                }
            }
        }
    }
}

@Composable
private fun OppositeCard(question: Question, index: Int) {
    when (question.kind) {
        Kind.SIZE -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ArtImage(question.art, Modifier.size(question.values[index].dp))
        }
        Kind.HEIGHT -> Box(
            Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            contentAlignment = if (question.values[index] == 0) Alignment.TopCenter else Alignment.BottomCenter,
        ) {
            ClayBall(ClayColors.Coral, Modifier.size(72.dp))
        }
        Kind.AMOUNT -> ItemRows(question.art, question.counts[index])
    }
}

/** The items in rows of up to three, centred. */
@Composable
private fun ItemRows(art: Art, count: Int) {
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        (0 until count).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.Center) {
                row.forEach { ArtImage(art, Modifier.size(48.dp)) }
            }
        }
    }
}
