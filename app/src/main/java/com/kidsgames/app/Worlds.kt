package com.kidsgames.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.theme.ClayColors
import kotlin.math.roundToInt

/** Stored on the device as part of the progress keys: never rename an entry. */
enum class GameId { COLORS, ANIMALS, FRUITS, SHAPES, COUNTING, MEMORY, LETTERS, NUMBERS, SOUNDS, TRACE, PAINT, PUZZLE, SORT }

/** A stop on the adventure map: one game, its look, and the stars needed to open it. */
data class World(
    val game: GameId,
    val label: String,
    val art: Art,
    val color: Color,
    val starsToOpen: Int,
)

/** In map order. One easy level pays 5 to 15 stars, so the next worlds open quickly. */
val WORLDS = listOf(
    World(GameId.COLORS, "Colores", Art.PALETTE, ClayColors.Pink, starsToOpen = 0),
    World(GameId.FRUITS, "Frutas", Art.APPLE, ClayColors.Peach, starsToOpen = 3),
    World(GameId.ANIMALS, "Animales", Art.DOG, ClayColors.Mint, starsToOpen = 6),
    World(GameId.SHAPES, "Formas", Art.STAR, ClayColors.Lavender, starsToOpen = 10),
    World(GameId.COUNTING, "Contar", Art.NUMBERS, ClayColors.Ochre, starsToOpen = 15),
    World(GameId.MEMORY, "Memoria", Art.CARDS, ClayColors.Coral, starsToOpen = 20),
    World(GameId.LETTERS, "Letras", Art.BLOCKS, ClayColors.Lavender, starsToOpen = 25),
    World(GameId.NUMBERS, "Números", Art.ABACUS, ClayColors.Peach, starsToOpen = 30),
    World(GameId.SOUNDS, "Sonidos", Art.NOTE, ClayColors.Pink, starsToOpen = 36),
    World(GameId.TRACE, "Trazar", Art.CRAYON, ClayColors.Ochre, starsToOpen = 42),
    World(GameId.PAINT, "Pintar", Art.BRUSH, ClayColors.Pink, starsToOpen = 48),
    World(GameId.PUZZLE, "Puzzles", Art.PUZZLE, ClayColors.Mint, starsToOpen = 54),
    World(GameId.SORT, "Encajar", Art.SORTER, ClayColors.Lavender, starsToOpen = 60),
)

/** A memory board, a puzzle or a drawing takes much longer than a question, so fewer rounds. */
val GameId.roundsPerLevel: Int
    get() = when (this) {
        GameId.MEMORY, GameId.PAINT, GameId.PUZZLE -> 2
        GameId.SORT -> 3
        else -> 5
    }

/**
 * One play-through of a level. Games call [finishRound] after every round; the level is
 * over when all rounds are played, and its rating is the average stars per round.
 */
@Stable
class LevelRun(
    val rounds: Int,
    private val onRound: (stars: Int) -> Unit,
    private val onFinished: (rating: Int) -> Unit,
) {
    var roundsDone by mutableIntStateOf(0)
        private set
    var earned by mutableIntStateOf(0)
        private set

    val finished: Boolean get() = roundsDone >= rounds

    /** From 1 to 3, never zero. */
    val rating: Int get() = (earned.toFloat() / rounds).roundToInt().coerceIn(1, 3)

    fun finishRound(stars: Int) {
        if (finished) return
        val paid = stars.coerceIn(1, 3)
        earned += paid
        roundsDone++
        onRound(paid)
        if (finished) onFinished(rating)
    }
}
