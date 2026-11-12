package com.kidsgames.app

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

enum class Sound { TAP, RIGHT, WRONG, WIN }

private const val SAMPLE_RATE = 22050

// Note frequencies in Hz.
private const val E4 = 329.63
private const val G4 = 392.00
private const val C5 = 523.25
private const val E5 = 659.25
private const val G5 = 783.99
private const val A5 = 880.00
private const val C6 = 1046.50
private const val E6 = 1318.51

/**
 * Short sound effects synthesized in code, so the app ships no audio files. Each sound is
 * rendered once into a static [AudioTrack] and replayed from the start on every [play].
 * Audio problems are swallowed: a missing sound must never stop a game.
 */
class Sfx {
    private val tracks: Map<Sound, AudioTrack?> = Sound.values().associateWith { buildTrack(render(it)) }

    fun play(sound: Sound) {
        val track = tracks[sound] ?: return
        try {
            // A static track stays "playing" after it ends, so rewind it explicitly.
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (e: IllegalStateException) {
            // Track was released or the audio system is unavailable.
        }
    }

    fun release() {
        tracks.values.forEach { it?.release() }
    }

    private fun render(sound: Sound): ShortArray = when (sound) {
        Sound.TAP -> tone(listOf(A5 to 60), volume = 0.25)
        Sound.RIGHT -> tone(listOf(C5 to 110, E5 to 110, G5 to 240), volume = 0.45)
        // Two soft low notes: a gentle "not that one", never a buzzer.
        Sound.WRONG -> tone(listOf(G4 to 150, E4 to 230), volume = 0.28)
        Sound.WIN -> tone(listOf(C5 to 110, E5 to 110, G5 to 110, C6 to 110, E6 to 420), volume = 0.45)
    }

    /** Renders [notes] (frequency to milliseconds) one after another as 16-bit mono PCM. */
    private fun tone(notes: List<Pair<Double, Int>>, volume: Double): ShortArray {
        val lengths = notes.map { it.second * SAMPLE_RATE / 1000 }
        // A little silence at the end avoids a click on devices that cut the last samples.
        val tail = SAMPLE_RATE * 40 / 1000
        val attack = SAMPLE_RATE * 6 / 1000
        val out = ShortArray(lengths.sum() + tail)
        var offset = 0
        notes.forEachIndexed { index, note ->
            val frequency = note.first
            val length = lengths[index]
            for (i in 0 until length) {
                val time = i.toDouble() / SAMPLE_RATE
                val fadeIn = min(1.0, i.toDouble() / attack)
                val fadeOut = (1.0 - i.toDouble() / length).pow(2)
                // A second harmonic makes the sine sound closer to a marimba.
                val wave = (sin(2 * PI * frequency * time) + 0.3 * sin(4 * PI * frequency * time)) / 1.3
                out[offset + i] = (wave * fadeIn * fadeOut * volume * Short.MAX_VALUE).toInt().toShort()
            }
            offset += length
        }
        return out
    }

    private fun buildTrack(pcm: ShortArray): AudioTrack? = try {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        if (track.state == AudioTrack.STATE_INITIALIZED) {
            track
        } else {
            track.release()
            null
        }
    } catch (e: RuntimeException) {
        // IllegalArgumentException or UnsupportedOperationException from the builder.
        null
    }
}

/** App-wide sound effects, provided once so a sound keeps playing across screen changes. */
val LocalSfx = staticCompositionLocalOf<Sfx> { error("LocalSfx was not provided") }

@Composable
fun rememberSfx(): Sfx {
    val sfx = remember { Sfx() }
    DisposableEffect(sfx) {
        onDispose { sfx.release() }
    }
    return sfx
}
