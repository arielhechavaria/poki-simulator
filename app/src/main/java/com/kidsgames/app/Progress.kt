package com.kidsgames.app

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.max

/** How many levels every game has; level N plays with harder rounds than level N-1. */
const val LEVEL_COUNT = 3

/** A round always pays at least one star: 3 with no mistakes, 2 with one, 1 otherwise. */
fun starsForMistakes(mistakes: Int): Int = (3 - mistakes).coerceIn(1, 3)

/**
 * What the child has earned so far. Stars only ever grow (until a parent resets them),
 * so a world or level that opened once stays open.
 */
@Immutable
data class Progress(
    val totalStars: Int = 0,
    /** Best rating (1 to 3) per finished level, keyed by [levelKey]. */
    private val bestRatings: Map<String, Int> = emptyMap(),
) {
    /** 0 while the level was never finished, else its best rating from 1 to 3. */
    fun rating(game: GameId, level: Int): Int = bestRatings[levelKey(game, level)] ?: 0

    fun isLevelOpen(game: GameId, level: Int): Boolean =
        level == 1 || rating(game, level - 1) > 0

    fun levelsDone(game: GameId): Int = (1..LEVEL_COUNT).count { rating(game, it) > 0 }

    /** The first level still to finish, or the last one when the world is complete. */
    fun nextLevel(game: GameId): Int =
        (1..LEVEL_COUNT).firstOrNull { rating(game, it) == 0 } ?: LEVEL_COUNT

    fun isWorldOpen(world: World): Boolean = totalStars >= world.starsToOpen
}

// Key names are stored on the device: renaming a GameId or these prefixes loses saved progress.
private const val BEST_PREFIX = "best_"
private val TOTAL_STARS = intPreferencesKey("stars_total")
private fun levelKey(game: GameId, level: Int) = "$BEST_PREFIX${game.name}_$level"

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

/**
 * Saves progress on the device. Writes run on their own scope so that leaving a screen
 * right after a round never drops the stars it paid.
 */
class ProgressStore private constructor(private val dataStore: DataStore<Preferences>) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val progress: Flow<Progress> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs ->
            val best = prefs.asMap().entries
                .filter { it.key.name.startsWith(BEST_PREFIX) }
                .mapNotNull { entry -> (entry.value as? Int)?.let { entry.key.name to it } }
                .toMap()
            Progress(totalStars = prefs[TOTAL_STARS] ?: 0, bestRatings = best)
        }

    fun addStars(stars: Int) {
        if (stars <= 0) return
        scope.launch {
            dataStore.edit { it[TOTAL_STARS] = (it[TOTAL_STARS] ?: 0) + stars }
        }
    }

    /** Keeps the best rating ever reached for the level; finishing it opens the next one. */
    fun finishLevel(game: GameId, level: Int, rating: Int) {
        val key = intPreferencesKey(levelKey(game, level))
        scope.launch {
            dataStore.edit { it[key] = max(it[key] ?: 0, rating.coerceIn(1, 3)) }
        }
    }

    companion object {
        @Volatile private var instance: ProgressStore? = null

        fun get(context: Context): ProgressStore =
            instance ?: synchronized(this) {
                instance ?: ProgressStore(context.applicationContext.progressDataStore).also { instance = it }
            }
    }
}

val LocalProgress = compositionLocalOf { Progress() }
val LocalProgressStore = staticCompositionLocalOf<ProgressStore> { error("ProgressStore not provided") }
