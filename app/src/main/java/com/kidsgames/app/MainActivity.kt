package com.kidsgames.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kidsgames.app.ui.ClayBackground
import com.kidsgames.app.ui.LocalMascotAccessory
import com.kidsgames.app.ui.theme.KidsTheme

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

/** Where the child is. Each change of screen starts that screen fresh. */
private sealed interface Screen {
    data object Map : Screen
    data object Album : Screen
    data object Gift : Screen
    data object Gate : Screen
    data object Parents : Screen
    data class Levels(val world: World) : Screen
    /** [attempt] grows on replay, so the same level starts over from round one. */
    data class Play(val world: World, val level: Int, val attempt: Int = 0) : Screen
}

@Composable
fun App() {
    val context = LocalContext.current
    val store = remember { ProgressStore.get(context) }
    val progress by store.progress.collectAsState(initial = null as Progress?)
    var screen by remember { mutableStateOf<Screen>(Screen.Map) }
    // The full hello is for the first time only; coming back to the map gets a short one.
    var greeted by remember { mutableStateOf(false) }
    // How many worlds were open when the map was last shown, to celebrate new ones.
    var seenOpenWorlds by remember { mutableStateOf<Int?>(null) }
    val sfx = rememberSfx()

    // Count play time only while the app is on screen and a game screen is showing.
    val lifecycle = (context as ComponentActivity).lifecycle
    var resumed by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ ->
            resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val counting = resumed && screen != Screen.Gate && screen != Screen.Parents
    LaunchedEffect(counting) {
        while (counting) {
            delay(PLAYTIME_TICK_SECONDS * 1000L)
            store.addPlaytime(currentDay(), PLAYTIME_TICK_SECONDS)
        }
    }

    BackHandler(enabled = screen != Screen.Map) {
        screen = when (val current = screen) {
            is Screen.Play -> Screen.Levels(current.world)
            Screen.Parents -> Screen.Gate
            else -> Screen.Map
        }
    }

    // Saved progress loads in a moment; the cream background shows until then.
    val loaded = progress ?: return
    CompositionLocalProvider(
        LocalSfx provides sfx,
        LocalProgress provides loaded,
        LocalProgressStore provides store,
        LocalMascotAccessory provides loaded.accessory,
    ) {
        val timeUp = loaded.timeUp(currentDay()) && screen != Screen.Gate && screen != Screen.Parents
        key(screen, timeUp) {
            if (timeUp) {
                TimeUpScreen(onParents = { screen = Screen.Gate })
            } else when (val current = screen) {
                Screen.Map -> AdventureMap(
                    firstVisit = !greeted,
                    onGreeted = { greeted = true },
                    seenOpenWorlds = seenOpenWorlds,
                    onSeenOpenWorlds = { seenOpenWorlds = it },
                    onOpen = { screen = Screen.Levels(it) },
                    onAlbum = { screen = Screen.Album },
                    onGift = { screen = Screen.Gift },
                    onParents = { screen = Screen.Gate },
                )
                Screen.Gate -> ParentsGate(onPass = { screen = Screen.Parents }, onHome = { screen = Screen.Map })
                Screen.Parents -> ParentsPanel(onHome = { screen = Screen.Map })
                Screen.Album -> StickerAlbum(onHome = { screen = Screen.Map })
                Screen.Gift -> DailyGift(onHome = { screen = Screen.Map })
                is Screen.Levels -> LevelPicker(
                    world = current.world,
                    onHome = { screen = Screen.Map },
                    onPlay = { level -> screen = Screen.Play(current.world, level) },
                )
                is Screen.Play -> LevelPlay(
                    world = current.world,
                    level = current.level,
                    onHome = { screen = Screen.Map },
                    onReplay = { screen = current.copy(attempt = current.attempt + 1) },
                    onNext = if (current.level < LEVEL_COUNT) {
                        { screen = Screen.Play(current.world, current.level + 1) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
