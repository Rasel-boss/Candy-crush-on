package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Interface defining audio feedback capabilities for the Candy Crush Lite game.
 */
interface ISoundManager {
    fun playTileSelect()
    fun playTileSwap()
    fun playInvalidSwap()
    fun playMatch(matchLength: Int = 3)
    fun playCascade(chainStep: Int = 2)
    fun playGameOver()
    fun playLevelComplete()
    fun playVictory()
    fun playButtonClick()
    fun playTileMove()
    fun playInvalidMove()
    fun release()
}

/**
 * High-performance procedural audio engine using streaming [AudioTrack] (MODE_STREAM).
 * Delivers zero-latency synthesized tactile feedback for swaps, matches,
 * cascades, and game transitions without requiring external copyrighted assets.
 *
 * @param soundEnabledSupplier Lambda returning whether sound is currently enabled.
 */
class SoundManager(
    private val soundEnabledSupplier: () -> Boolean = { true }
) : ISoundManager {

    private val sampleRate = 22050
    private val executor = Executors.newSingleThreadExecutor()

    // Warm, marimba-style tile select pop
    private val tileSelectPcm: ShortArray = generateChimePcm(
        frequency = 880.0,
        durationMs = 24,
        volume = 0.38f,
        harmonic2 = 0.22f,
        decayRate = 4.0
    )

    // Smooth ascending frequency glide for tile swap
    private val tileSwapPcm: ShortArray = generateGlidePcm(
        startFreq = 880.0,
        endFreq = 1174.66,
        durationMs = 35,
        volume = 0.42f
    )

    // Soft rounded double thud for invalid swaps
    private val invalidSwapPcm: ShortArray = generateDoubleTonePcm(
        freq = 196.0,
        pulseMs = 22,
        gapMs = 14,
        volume = 0.40f
    )

    // Crisp mechanical UI click
    private val buttonClickPcm: ShortArray = generateChimePcm(
        frequency = 1046.50,
        durationMs = 16,
        volume = 0.35f,
        harmonic2 = 0.15f,
        decayRate = 6.0
    )

    // Ascending major arpeggio for 3-candy match (C5, E5, G5)
    private val match3Pcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(523.25, 659.25, 783.99),
        noteDurationMs = 42,
        volume = 0.50f,
        harmonic2 = 0.18f
    )

    // Sparkling 4-candy match arpeggio (C5, E5, G5, C6)
    private val match4Pcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(523.25, 659.25, 783.99, 1046.50),
        noteDurationMs = 38,
        volume = 0.55f,
        harmonic2 = 0.20f
    )

    // 5-candy pentatonic fanfare (E5, G5, A5, C6, E6)
    private val match5Pcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(659.25, 783.99, 880.00, 1046.50, 1318.51),
        noteDurationMs = 36,
        volume = 0.60f,
        harmonic2 = 0.22f
    )

    // Progressive cascade chains (D-major / E-major / G-major sparkling chime ladders)
    private val cascade2Pcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(587.33, 739.99, 880.00),
        noteDurationMs = 45,
        volume = 0.52f,
        harmonic2 = 0.18f
    )
    private val cascade3Pcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(659.25, 830.61, 987.77, 1318.51),
        noteDurationMs = 40,
        volume = 0.56f,
        harmonic2 = 0.20f
    )
    private val cascade4Pcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(783.99, 987.77, 1174.66, 1567.98),
        noteDurationMs = 36,
        volume = 0.62f,
        harmonic2 = 0.22f
    )

    // Empathetic, gentle descending game over progression
    private val gameOverPcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(440.0, 392.0, 349.23, 293.66),
        noteDurationMs = 85,
        volume = 0.48f,
        harmonic2 = 0.12f
    )

    // Triumphant 6-note victory fanfare with bell chime resonance
    private val victoryPcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98),
        noteDurationMs = 75,
        volume = 0.55f,
        harmonic2 = 0.24f
    )

    private val minBufferSize: Int = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private var audioTrack: AudioTrack? = null

    init {
        try {
            audioTrack = AudioTrack.Builder()
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
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Throwable) {
            Log.w("SoundManager", "AudioTrack streaming initialization failed: ${e.message}")
        }
    }

    override fun playTileSelect() {
        if (!soundEnabledSupplier()) return
        streamPcm(tileSelectPcm)
    }

    override fun playTileSwap() {
        if (!soundEnabledSupplier()) return
        streamPcm(tileSwapPcm)
    }

    override fun playInvalidSwap() {
        if (!soundEnabledSupplier()) return
        streamPcm(invalidSwapPcm)
    }

    override fun playMatch(matchLength: Int) {
        if (!soundEnabledSupplier()) return
        val pcm = when {
            matchLength >= 5 -> match5Pcm
            matchLength == 4 -> match4Pcm
            else -> match3Pcm
        }
        streamPcm(pcm)
    }

    override fun playCascade(chainStep: Int) {
        if (!soundEnabledSupplier()) return
        val pcm = when {
            chainStep >= 4 -> cascade4Pcm
            chainStep == 3 -> cascade3Pcm
            else -> cascade2Pcm
        }
        streamPcm(pcm)
    }

    override fun playGameOver() {
        if (!soundEnabledSupplier()) return
        streamPcm(gameOverPcm)
    }

    override fun playLevelComplete() {
        playVictory()
    }

    override fun playVictory() {
        if (!soundEnabledSupplier()) return
        streamPcm(victoryPcm)
    }

    override fun playButtonClick() {
        if (!soundEnabledSupplier()) return
        streamPcm(buttonClickPcm)
    }

    override fun playTileMove() {
        playTileSwap()
    }

    override fun playInvalidMove() {
        playInvalidSwap()
    }

    private fun streamPcm(pcm: ShortArray) {
        if (executor.isShutdown) return
        executor.execute {
            try {
                val track = audioTrack ?: return@execute
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                }
                track.write(pcm, 0, pcm.size, AudioTrack.WRITE_BLOCKING)
            } catch (e: Throwable) {
                Log.d("SoundManager", "Audio streaming playback skipped: ${e.message}")
            }
        }
    }

    override fun release() {
        try {
            executor.shutdownNow()
            audioTrack?.let { track ->
                try {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop()
                        track.flush()
                    }
                    track.release()
                } catch (e: Throwable) {
                    Log.d("SoundManager", "Error stopping AudioTrack: ${e.message}")
                }
            }
            audioTrack = null
        } catch (e: Throwable) {
            Log.d("SoundManager", "Error releasing SoundManager: ${e.message}")
        }
    }

    private fun generateChimePcm(
        frequency: Double,
        durationMs: Int,
        volume: Float,
        harmonic2: Float = 0.20f,
        decayRate: Double = 3.5
    ): ShortArray {
        val numSamples = (durationMs * sampleRate) / 1000
        val pcm = ShortArray(numSamples)
        val angularFreq = 2.0 * PI * frequency / sampleRate
        val attackSamples = (sampleRate * 0.003).toInt().coerceAtLeast(1)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val attack = if (i < attackSamples) (i.toDouble() / attackSamples).toFloat() else 1.0f
            val decay = exp(-decayRate * t * (1000.0 / durationMs)).toFloat()
            val envelope = attack * decay

            val fundamental = sin(i * angularFreq)
            val secondHarmonic = sin(i * angularFreq * 2.0) * harmonic2
            val sample = (fundamental + secondHarmonic) / (1.0f + harmonic2) * Short.MAX_VALUE * volume * envelope

            pcm[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateGlidePcm(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Float
    ): ShortArray {
        val numSamples = (durationMs * sampleRate) / 1000
        val pcm = ShortArray(numSamples)
        val attackSamples = (sampleRate * 0.002).toInt().coerceAtLeast(1)

        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * currentFreq / sampleRate

            val attack = if (i < attackSamples) (i.toDouble() / attackSamples).toFloat() else 1.0f
            val decay = (1.0f - (progress * 0.7f).toFloat())
            val envelope = attack * decay

            val sample = sin(phase) * Short.MAX_VALUE * volume * envelope
            pcm[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateDoubleTonePcm(
        freq: Double,
        pulseMs: Int,
        gapMs: Int,
        volume: Float
    ): ShortArray {
        val pulseSamples = (pulseMs * sampleRate) / 1000
        val gapSamples = (gapMs * sampleRate) / 1000
        val totalSamples = (pulseSamples * 2) + gapSamples
        val pcm = ShortArray(totalSamples)
        val angularFreq = 2.0 * PI * freq / sampleRate
        val attackSamples = (sampleRate * 0.003).toInt().coerceAtLeast(1)

        for (i in 0 until pulseSamples) {
            val attack = if (i < attackSamples) (i.toDouble() / attackSamples).toFloat() else 1.0f
            val decay = 1.0f - (i.toFloat() / pulseSamples) * 0.5f
            pcm[i] = (sin(i * angularFreq) * Short.MAX_VALUE * volume * attack * decay).toInt().toShort()
        }
        val secondStart = pulseSamples + gapSamples
        for (i in 0 until pulseSamples) {
            val attack = if (i < attackSamples) (i.toDouble() / attackSamples).toFloat() else 1.0f
            val decay = 1.0f - (i.toFloat() / pulseSamples) * 0.5f
            pcm[secondStart + i] = (sin(i * angularFreq) * Short.MAX_VALUE * volume * attack * decay).toInt().toShort()
        }
        return pcm
    }

    private fun generateArpeggioPcm(
        frequencies: DoubleArray,
        noteDurationMs: Int,
        volume: Float,
        harmonic2: Float = 0.18f
    ): ShortArray {
        val noteSamples = (noteDurationMs * sampleRate) / 1000
        val totalSamples = noteSamples * frequencies.size
        val pcm = ShortArray(totalSamples)

        for ((index, freq) in frequencies.withIndex()) {
            val offset = index * noteSamples
            val angularFreq = 2.0 * PI * freq / sampleRate
            val attackSamples = (sampleRate * 0.003).toInt().coerceAtLeast(1)

            for (i in 0 until noteSamples) {
                val progress = i.toDouble() / noteSamples
                val attack = if (i < attackSamples) (i.toDouble() / attackSamples).toFloat() else 1.0f
                val decay = (1.0f - (progress.toFloat() * 0.35f)).coerceIn(0f, 1f)
                val envelope = attack * decay

                val fundamental = sin(i * angularFreq)
                val secondHarmonic = sin(i * angularFreq * 2.0) * harmonic2
                val sample = (fundamental + secondHarmonic) / (1.0f + harmonic2) * Short.MAX_VALUE * volume * envelope

                pcm[offset + i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return pcm
    }
}

/**
 * No-op sound manager implementation for testing and preview modes.
 */
class NoOpSoundManager : ISoundManager {
    override fun playTileSelect() {}
    override fun playTileSwap() {}
    override fun playInvalidSwap() {}
    override fun playMatch(matchLength: Int) {}
    override fun playCascade(chainStep: Int) {}
    override fun playGameOver() {}
    override fun playLevelComplete() {}
    override fun playVictory() {}
    override fun playButtonClick() {}
    override fun playTileMove() {}
    override fun playInvalidMove() {}
    override fun release() {}
}

