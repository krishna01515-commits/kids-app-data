package com.kidslearning.chutki.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundEffectHelper {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun playCorrectChime() {
        scope.launch {
            // Play upbeat arpeggio: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046Hz)
            playToneSequence(
                listOf(
                    Pair(523.25, 90),
                    Pair(659.25, 90),
                    Pair(783.99, 90),
                    Pair(1046.50, 220)
                )
            )
        }
    }

    fun playWrongBoing() {
        scope.launch {
            // Play gentle downward boing (320Hz down to 180Hz)
            playGlissando(startFreq = 340.0, endFreq = 180.0, durationMs = 260)
        }
    }

    fun playPopSound() {
        scope.launch {
            // Short bubble pop (880Hz to 1200Hz in 60ms)
            playGlissando(startFreq = 700.0, endFreq = 1250.0, durationMs = 60)
        }
    }

    fun playStarTwinkle() {
        scope.launch {
            playToneSequence(
                listOf(
                    Pair(1046.50, 70),
                    Pair(1318.51, 80),
                    Pair(1567.98, 160)
                )
            )
        }
    }

    fun playFanfare() {
        scope.launch {
            playToneSequence(
                listOf(
                    Pair(523.25, 120),
                    Pair(523.25, 120),
                    Pair(523.25, 120),
                    Pair(659.25, 280),
                    Pair(783.99, 180),
                    Pair(1046.50, 450)
                )
            )
        }
    }

    fun playComboSound(combo: Int) {
        scope.launch {
            val baseFreq = (480.0 + (combo * 70.0)).coerceAtMost(1200.0)
            playToneSequence(
                listOf(
                    Pair(baseFreq, 70),
                    Pair(baseFreq * 1.25, 80),
                    Pair(baseFreq * 1.5, 150)
                )
            )
        }
    }

    fun playPianoNote(freq: Double, durationMs: Int = 260) {
        scope.launch {
            playToneSequence(listOf(Pair(freq, durationMs)))
        }
    }

    fun playTrainWhistle() {
        scope.launch {
            playToneSequence(
                listOf(
                    Pair(587.33, 160),
                    Pair(739.99, 160),
                    Pair(587.33, 260)
                )
            )
        }
    }

    fun playSoftTryAgain() {
        scope.launch {
            // Soft friendly 2-tone prompt (no harsh boing)
            playToneSequence(
                listOf(
                    Pair(440.0, 90),
                    Pair(392.0, 140)
                )
            )
        }
    }

    fun playAnimalSound(type: String) {
        scope.launch {
            when (type.lowercase()) {
                "cat" -> playGlissando(650.0, 480.0, 220) // Meow
                "cow" -> playGlissando(180.0, 150.0, 340) // Moo
                "bird" -> {
                    playGlissando(1800.0, 2400.0, 70)
                    playGlissando(2000.0, 2600.0, 80)
                } // Chirp
                "lion" -> playGlissando(160.0, 110.0, 380) // Roar
                else -> playPopSound()
            }
        }
    }

    private fun playToneSequence(tones: List<Pair<Double, Int>>) {
        try {
            val sampleRate = 44100
            var totalSamples = 0
            tones.forEach { totalSamples += (sampleRate * it.second) / 1000 }

            val buffer = ShortArray(totalSamples)
            var currentIdx = 0

            tones.forEach { (freq, durationMs) ->
                val count = (sampleRate * durationMs) / 1000
                for (i in 0 until count) {
                    val angle = 2.0 * Math.PI * i * freq / sampleRate
                    // Apply smooth envelope to avoid clicking
                    val envelope = when {
                        i < 200 -> i / 200.0
                        i > count - 400 -> (count - i) / 400.0
                        else -> 1.0
                    }
                    val sample = (sin(angle) * 16000.0 * envelope).toInt().toShort()
                    if (currentIdx < totalSamples) {
                        buffer[currentIdx++] = sample
                    }
                }
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(totalSamples * 1000L / sampleRate + 50)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio track fallback
        }
    }

    private fun playGlissando(startFreq: Double, endFreq: Double, durationMs: Int) {
        try {
            val sampleRate = 44100
            val count = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(count)

            var phase = 0.0
            for (i in 0 until count) {
                val t = i.toDouble() / count
                val currentFreq = startFreq + (endFreq - startFreq) * t
                phase += 2.0 * Math.PI * currentFreq / sampleRate
                val envelope = sin(Math.PI * t) // smooth hump
                val sample = (sin(phase) * 16000.0 * envelope).toInt().toShort()
                buffer[i] = sample
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 50)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore
        }
    }
}

