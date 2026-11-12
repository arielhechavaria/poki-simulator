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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

/** The two top-level sections of the portal. */
enum class Kind(val title: String, val emoji: String, val color: Color) {
    LEARN("Aprender", "📚", Color(0xFF43A047)),
    FUN("Divertirse", "🎮", Color(0xFFFB8C00)),
}

enum class GameId { COLORS, ANIMALS, FRUITS, SHAPES, COUNTING, MEMORY }

sealed interface Screen {
    data object Home : Screen
    data class Category(val kind: Kind) : Screen
    data class Native(val id: GameId) : Screen
    data class Web(val game: WebGame) : Screen
}

private data class Tile(
    val label: String,
    val emoji: String,
    val color: Color,
    val target: Screen,
)

private val LEARN_TILES = listOf(
    Tile("Colores", "🎨", Color(0xFFE53935), Screen.Native(GameId.COLORS)),
    Tile("Animales", "🐶", Color(0xFF43A047), Screen.Native(GameId.ANIMALS)),
    Tile("Frutas", "🍎", Color(0xFFFB8C00), Screen.Native(GameId.FRUITS)),
    Tile("Formas", "⭐", Color(0xFF8E24AA), Screen.Native(GameId.SHAPES)),
    Tile("Contar", "🔢", Color(0xFF1E88E5), Screen.Native(GameId.COUNTING)),
    Tile("Memoria", "🧠", Color(0xFFEC407A), Screen.Native(GameId.MEMORY)),
)

private val FUN_TILES = WEB_GAMES.map { game ->
    Tile(game.label, game.emoji, game.color, Screen.Web(game))
}

@Composable
fun App() {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    fun back() {
        screen = when (screen) {
            Screen.Home -> Screen.Home
            is Screen.Category -> Screen.Home
            is Screen.Native -> Screen.Category(Kind.LEARN)
            is Screen.Web -> Screen.Category(Kind.FUN)
        }
    }

    BackHandler(enabled = screen != Screen.Home) { back() }

    when (val current = screen) {
        Screen.Home -> HomeScreen(onPick = { screen = Screen.Category(it) })
        is Screen.Category -> TileScreen(
            title = current.kind.title,
            tiles = if (current.kind == Kind.LEARN) LEARN_TILES else FUN_TILES,
            onPick = { screen = it },
            onBack = { back() }
        )
        is Screen.Native -> when (current.id) {
            GameId.COLORS -> ChoiceGame(COLOR_ITEMS) { back() }
            GameId.ANIMALS -> ChoiceGame(ANIMAL_ITEMS) { back() }
            GameId.FRUITS -> ChoiceGame(FRUIT_ITEMS) { back() }
            GameId.SHAPES -> ChoiceGame(SHAPE_ITEMS) { back() }
            GameId.COUNTING -> CountingGame { back() }
            GameId.MEMORY -> MemoryGame { back() }
        }
        is Screen.Web -> WebGameScreen(current.game) { back() }
    }
}

@Composable
private fun HomeScreen(onPick: (Kind) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Kind.entries.forEach { kind ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(36.dp))
                    .background(kind.color)
                    .clickable { onPick(kind) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(kind.emoji, fontSize = 96.sp)
                    Text(
                        kind.title,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TileScreen(
    title: String,
    tiles: List<Tile>,
    onPick: (Screen) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("🏠", fontSize = 32.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(title, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(64.dp))
        }
        Spacer(Modifier.height(8.dp))
        tiles.chunked(2).forEach { rowTiles ->
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
                            .clickable { onPick(tile.target) },
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
