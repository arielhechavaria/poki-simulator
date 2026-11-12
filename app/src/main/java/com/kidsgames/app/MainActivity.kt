package com.kidsgames.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsgames.app.ui.ClayBackground
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsTheme
import com.kidsgames.app.ui.theme.KidsType
import com.kidsgames.app.ui.theme.Space

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KidsTheme {
                ClayBackground {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        App()
                    }
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
    Tile("Colores", "🎨", ClayColors.Pink, GameId.COLORS),
    Tile("Animales", "🐶", ClayColors.Mint, GameId.ANIMALS),
    Tile("Frutas", "🍎", ClayColors.Peach, GameId.FRUITS),
    Tile("Formas", "⭐", ClayColors.Lavender, GameId.SHAPES),
    Tile("Contar", "🔢", ClayColors.Ochre, GameId.COUNTING),
    Tile("Memoria", "🧠", ClayColors.Coral, GameId.MEMORY),
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
            .padding(horizontal = Space.md, vertical = Space.lg),
    ) {
        Text(
            text = "¡A jugar!",
            style = KidsType.display,
            color = ClayColors.Ink,
            modifier = Modifier.padding(start = Space.xs, bottom = Space.md),
        )
        TILES.chunked(2).forEachIndexed { rowIndex, rowTiles ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.xs),
                horizontalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                rowTiles.forEachIndexed { columnIndex, tile ->
                    // Hand-placed feel: neighbours lean in opposite directions.
                    val tilt = if ((rowIndex + columnIndex) % 2 == 0) -1.5f else 1.5f
                    GameTile(
                        tile = tile,
                        onClick = { onPick(tile.game) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .graphicsLayer { rotationZ = tilt },
                    )
                }
                if (rowTiles.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GameTile(tile: Tile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ClayCard(color = tile.color, modifier = modifier, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(ClayColors.Canvas.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(tile.emoji, fontSize = 48.sp)
            }
            Spacer(Modifier.height(Space.xs))
            Text(tile.label, style = KidsType.label, color = ClayColors.Ink)
        }
    }
}
