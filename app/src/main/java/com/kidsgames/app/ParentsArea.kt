package com.kidsgames.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.Art
import com.kidsgames.app.ui.ArtImage
import com.kidsgames.app.ui.ClayButton
import com.kidsgames.app.ui.ClayCard
import com.kidsgames.app.ui.ClaySurface
import com.kidsgames.app.ui.Mascot
import com.kidsgames.app.ui.MascotMood
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import com.kidsgames.app.ui.theme.Radius
import com.kidsgames.app.ui.theme.Space
import kotlinx.coroutines.delay
import java.util.Locale

private const val HOLD_MILLIS = 2500L

/** Seconds counted per tick of play time. */
const val PLAYTIME_TICK_SECONDS = 10

/** Limits a parent can pick, in minutes; 0 is "no limit". */
private val LIMIT_CHOICES = listOf(0, 15, 30, 45, 60)

/**
 * A small lock that opens the parents area. It is deliberately quiet: grey, small and
 * without a voice, so a 4-year-old has no reason to press it.
 */
@Composable
fun ParentsLockButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ClaySurface(
        color = ClayColors.SurfaceStrong,
        modifier = modifier.size(width = 56.dp, height = 62.dp),
        onClick = onClick,
        cornerRadius = Radius.pill,
        depth = 6.dp,
    ) {
        ArtImage(Art.LOCK, Modifier.size(28.dp), tint = ClayColors.Muted)
    }
}

/**
 * The gate: hold a button for a few seconds, then solve a sum. Holding is too long for a
 * 4-year-old and the sum is beyond them. A wrong answer starts the gate over.
 */
@Composable
fun ParentsGate(onPass: () -> Unit, onHome: () -> Unit) {
    var holding by remember { mutableStateOf(false) }
    var held by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    val a = remember(attempt) { (4..9).random() }
    val b = remember(attempt) { (4..9).random() }
    val options = remember(attempt) {
        val answer = a + b
        (listOf(answer) + generateSequence { (answer - 4..answer + 4).random() }
            .filter { it != answer }.distinct().take(3).toList()).shuffled()
    }
    val progress by animateFloatAsState(
        targetValue = if (holding && !held) 1f else 0f,
        animationSpec = tween(if (holding) HOLD_MILLIS.toInt() else 200),
        label = "hold",
    )

    LaunchedEffect(holding) {
        if (holding) {
            delay(HOLD_MILLIS)
            held = true
        }
    }

    ParentsFrame(title = "Zona para padres", onHome = onHome) {
        if (!held) {
            Text(
                "Mantén pulsado el botón unos segundos",
                style = KidsType.body,
                color = ClayColors.Body,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Space.lg))
            ClaySurface(
                color = ClayColors.Ink,
                modifier = Modifier
                    .size(width = 200.dp, height = 96.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            holding = true
                            tryAwaitRelease()
                            holding = false
                        })
                    },
                cornerRadius = Radius.pill,
            ) {
                Text("Mantener", style = KidsType.label, color = ClayColors.Canvas)
            }
            Spacer(Modifier.height(Space.md))
            Box(
                Modifier
                    .size(width = 200.dp, height = 10.dp)
                    .background(ClayColors.SurfaceStrong, RoundedCornerShape(Radius.pill)),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(ClayColors.Pink, RoundedCornerShape(Radius.pill)),
                )
            }
        } else {
            Text("¿Cuánto es $a + $b?", style = KidsType.display, color = ClayColors.Ink)
            Spacer(Modifier.height(Space.lg))
            options.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    row.forEach { value ->
                        ClayButton(
                            onClick = {
                                if (value == a + b) {
                                    onPass()
                                } else {
                                    held = false
                                    attempt++
                                }
                            },
                            modifier = Modifier.size(width = 120.dp, height = 88.dp),
                            color = ClayColors.Teal,
                        ) {
                            Text("$value", style = KidsType.title, color = ClayColors.Canvas)
                        }
                    }
                }
                Spacer(Modifier.height(Space.md))
            }
        }
    }
}

/** Page frame of the adult screens: a title, a home button and a scrolling column. */
@Composable
private fun ParentsFrame(title: String, onHome: () -> Unit, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(Space.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ClayButton(
                onClick = onHome,
                modifier = Modifier.size(width = 72.dp, height = 80.dp),
                color = ClayColors.Ochre,
            ) {
                ArtImage(Art.HOME, Modifier.size(36.dp))
            }
            Spacer(Modifier.size(Space.md))
            Text(title, style = KidsType.title, color = ClayColors.Ink)
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
    }
}

/** Settings and summary for the adult: time limit, progress, Spanish voice, reset. */
@Composable
fun ParentsPanel(onHome: () -> Unit) {
    val progress = LocalProgress.current
    val store = LocalProgressStore.current
    val context = LocalContext.current
    val voice = rememberSpanishVoiceStatus()
    var confirmReset by remember { mutableStateOf(false) }
    val minutesToday = progress.secondsToday(currentDay()) / 60

    ParentsFrame(title = "Zona para padres", onHome = onHome) {
        SectionCard("Tiempo de juego al día") {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                LIMIT_CHOICES.forEach { minutes ->
                    val selected = progress.limitMinutes == minutes
                    ClayButton(
                        onClick = { store.setLimit(minutes) },
                        modifier = Modifier.weight(1f),
                        color = if (selected) ClayColors.Ink else ClayColors.SurfaceStrong,
                    ) {
                        Text(
                            if (minutes == 0) "Sin" else "$minutes",
                            style = KidsType.label,
                            color = if (selected) ClayColors.Canvas else ClayColors.Ink,
                        )
                    }
                }
            }
            Spacer(Modifier.height(Space.xs))
            Text(
                (if (progress.limitMinutes == 0) "Sin límite (minutos)." else "Límite en minutos.") +
                    " Hoy ha jugado $minutesToday min.",
                style = KidsType.body,
                color = ClayColors.Body,
            )
        }
        SectionCard("Progreso") {
            val worldsOpen = WORLDS.count { progress.isWorldOpen(it) }
            SummaryLine("Estrellas", "${progress.totalStars}")
            SummaryLine("Pegatinas", "${progress.stickersEarned()} de ${WORLDS.size * LEVEL_COUNT}")
            SummaryLine("Mundos abiertos", "$worldsOpen de ${WORLDS.size}")
            SummaryLine("Días con regalo", "${progress.giftCount}")
        }
        SectionCard("Voz en español") {
            Text(
                when (voice) {
                    null -> "Comprobando…"
                    VoiceStatus.OFFLINE_READY -> "Lista: funciona sin internet."
                    VoiceStatus.ONLINE_ONLY -> "Solo funciona con internet. Instala los datos de voz para usarla sin conexión."
                    VoiceStatus.MISSING -> "No está instalada. Instálala para que la app hable."
                },
                style = KidsType.body,
                color = ClayColors.Body,
            )
            if (voice != null && voice != VoiceStatus.OFFLINE_READY) {
                Spacer(Modifier.height(Space.xs))
                ClayButton(onClick = { openVoiceInstall(context) }, color = ClayColors.Teal) {
                    Text(
                        "Instalar voz",
                        style = KidsType.label,
                        color = ClayColors.Canvas,
                        modifier = Modifier.padding(horizontal = Space.md),
                    )
                }
            }
        }
        SectionCard("Borrar progreso") {
            if (!confirmReset) {
                ClayButton(onClick = { confirmReset = true }, color = ClayColors.Coral) {
                    Text(
                        "Borrar",
                        style = KidsType.label,
                        color = ClayColors.Ink,
                        modifier = Modifier.padding(horizontal = Space.md),
                    )
                }
            } else {
                Text("Se borran estrellas, pegatinas y regalos. ¿Seguro?", style = KidsType.body, color = ClayColors.Body)
                Spacer(Modifier.height(Space.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                    ClayButton(
                        onClick = {
                            store.resetProgress()
                            confirmReset = false
                        },
                        color = ClayColors.Coral,
                    ) {
                        Text("Sí, borrar", style = KidsType.label, color = ClayColors.Ink, modifier = Modifier.padding(horizontal = Space.sm))
                    }
                    ClayButton(onClick = { confirmReset = false }, color = ClayColors.SurfaceStrong) {
                        Text("No", style = KidsType.label, color = ClayColors.Ink, modifier = Modifier.padding(horizontal = Space.sm))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    ClayCard(color = ClayColors.SurfaceCard, modifier = Modifier.fillMaxWidth().padding(bottom = Space.md)) {
        Column(Modifier.fillMaxWidth().padding(Space.md)) {
            Text(title, style = KidsType.label, color = ClayColors.Ink)
            Spacer(Modifier.height(Space.xs))
            content()
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = KidsType.body, color = ClayColors.Body)
        Text(value, style = KidsType.label, color = ClayColors.Ink)
    }
}

/**
 * Shown instead of the games once the daily limit is reached: the mascot says goodbye in a
 * friendly way, never as a punishment. The small lock still opens the parents area.
 */
@Composable
fun TimeUpScreen(onParents: () -> Unit) {
    val feedback = rememberGameFeedback()
    LaunchedEffect(Unit) { feedback.ask("¡Ya jugamos mucho hoy! ¡Hasta mañana!") }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Mascot(
                mood = if (feedback.mood == MascotMood.IDLE) MascotMood.CHEERING else feedback.mood,
                modifier = Modifier.size(240.dp),
                onClick = { feedback.ask("¡Hasta mañana!") },
            )
            Spacer(Modifier.height(Space.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                ArtImage(Art.MOON, Modifier.size(96.dp))
                ArtImage(Art.BED, Modifier.size(96.dp))
            }
        }
        ParentsLockButton(
            onClick = onParents,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Space.md),
        )
    }
}

private enum class VoiceStatus { OFFLINE_READY, ONLINE_ONLY, MISSING }

/** Asks the phone's speech engine whether a Spanish voice exists and works offline. */
@Composable
private fun rememberSpanishVoiceStatus(): VoiceStatus? {
    val context = LocalContext.current
    var status by remember { mutableStateOf<VoiceStatus?>(null) }
    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { result ->
            status = if (result != TextToSpeech.SUCCESS) {
                VoiceStatus.MISSING
            } else {
                spanishStatus(engine)
            }
        }
        onDispose { engine?.shutdown() }
    }
    return status
}

private fun spanishStatus(engine: TextToSpeech?): VoiceStatus {
    val tts = engine ?: return VoiceStatus.MISSING
    val installed = tts.isLanguageAvailable(Locale.forLanguageTag("es-ES")) >= TextToSpeech.LANG_AVAILABLE ||
        tts.isLanguageAvailable(Locale.forLanguageTag("es")) >= TextToSpeech.LANG_AVAILABLE
    if (!installed) return VoiceStatus.MISSING
    val voices = runCatching { tts.voices.orEmpty() }.getOrDefault(emptySet())
    val spanish = voices.filter { it.locale.language == "es" }
    // Some engines list no voices; then trust the language check.
    if (spanish.isEmpty()) return VoiceStatus.OFFLINE_READY
    return if (spanish.any { !it.isNetworkConnectionRequired }) VoiceStatus.OFFLINE_READY else VoiceStatus.ONLINE_ONLY
}

/** Opens the system screen to install voice data, or the speech settings as a fallback. */
private fun openVoiceInstall(context: Context) {
    val attempts = listOf(
        Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA),
        Intent("com.android.settings.TTS_SETTINGS"),
        Intent(android.provider.Settings.ACTION_SETTINGS),
    )
    for (intent in attempts) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: ActivityNotFoundException) {
            // Try the next screen.
        }
    }
}
