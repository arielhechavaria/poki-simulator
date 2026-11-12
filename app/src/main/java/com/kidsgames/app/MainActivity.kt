package com.kidsgames.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = Color(0xFFFFF8E1)
                ) {
                    App()
                }
            }
        }
    }
}

enum class GameId { COLORS, ANIMALS, FRUITS, SHAPES, COUNTING, MEMORY }

private data class Tile(
    val label: String,
    val emoji: String,
    val color: Color,
    val game: GameId,
)

private val TILES = listOf(
    Tile("Colores", "🎨", Color(0xFFE53935), GameId.COLORS),
    Tile("Animales", "🐶", Color(0xFF43A047), GameId.ANIMALS),
    Tile("Frutas", "🍎", Color(0xFFFB8C00), GameId.FRUITS),
    Tile("Formas", "⭐", Color(0xFF8E24AA), GameId.SHAPES),
    Tile("Contar", "🔢", Color(0xFF1E88E5), GameId.COUNTING),
    Tile("Memoria", "🧠", Color(0xFFEC407A), GameId.MEMORY),
)

@Composable
fun App() {
    // null means the home screen.
    var game by remember { mutableStateOf<GameId?>(null) }
    val goHome = { game = null }

    BackHandler(enabled = game != null) { goHome() }

    when (game) {
        null -> HomeScreen(onPick = { game = it })
        GameId.COLORS -> ChoiceGame(COLOR_ITEMS, goHome)
        GameId.ANIMALS -> ChoiceGame(ANIMAL_ITEMS, goHome)
        GameId.FRUITS -> ChoiceGame(FRUIT_ITEMS, goHome)
        GameId.SHAPES -> ChoiceGame(SHAPE_ITEMS, goHome)
        GameId.COUNTING -> CountingGame(goHome)
        GameId.MEMORY -> MemoryGame(goHome)
    }
}

@Composable
private fun HomeScreen(onPick: (GameId) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TILES.chunked(2).forEach { rowTiles ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowTiles.forEach { tile ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(tile.color)
                            .clickable { onPick(tile.game) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tile.emoji, fontSize = 64.sp)
                            Text(
                                tile.label,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                if (rowTiles.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}
