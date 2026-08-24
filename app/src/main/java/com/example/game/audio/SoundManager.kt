package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Interface defining audio feedback capabilities for the Puzzle Master game.
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
 * Procedural audio engine using streaming [AudioTrack] (MODE_STREAM).
 * Delivers zero-latency synthesized tactile feedback for swaps, matches,
 * cascades, and game transitions without requiring external assets.
 *
 * @param soundEnabledSupplier Lambda returning whether sound is currently enabled.
 */
class SoundManager(
    private val soundEnabledSupplier: () -> Boolean = { true }
) : ISoundManager {

    private val sampleRate = 22050
    private val executor = Executors.newSingleThreadExecutor()

    private val tileSelectPcm: ShortArray = generateTonePcm(frequency = 740.0, durationMs = 25, volume = 0.40f, isDecay = true)
    private val tileSwapPcm: ShortArray = generateTonePcm(frequency = 880.0, durationMs = 35, volume = 0.50f, isDecay = true)
    private val invalidSwapPcm: ShortArray = generateDoubleTonePcm(freq = 220.0, pulseMs = 25, gapMs = 15, volume = 0.45f)
    private val buttonClickPcm: ShortArray = generateTonePcm(frequency = 650.0, durationMs = 20, volume = 0.40f, isDecay = true)
    private val match3Pcm: ShortArray = generateArpeggioPcm(frequencies = doubleArrayOf(523.25, 659.25, 783.99), noteDurationMs = 45, volume = 0.55f)
    private val match4Pcm: ShortArray = generateArpeggioPcm(frequencies = doubleArrayOf(523.25, 659.25, 783.99, 1046.50), noteDurationMs = 40, volume = 0.60f)
    private val match5Pcm: ShortArray = generateArpeggioPcm(frequencies = doubleArrayOf(659.25, 783.99, 1046.50, 1318.51), noteDurationMs = 40, volume = 0.65f)
    private val cascade2Pcm: ShortArray = generateArpeggioPcm(frequencies = doubleArrayOf(587.33, 739.99, 880.00), noteDurationMs = 50, volume = 0.60f)
    private val cascade3Pcm: ShortArray = generateArpeggioPcm(frequencies = doubleArrayOf(659.25, 830.61, 987.77, 1318.51), noteDurationMs = 45, volume = 0.65f)
    private val cascade4Pcm: ShortArray = generateArpeggioPcm(frequencies = doubleArrayOf(783.99, 987.77, 1174.66, 1567.98), noteDurationMs = 40, volume = 0.70f)
    private val gameOverPcm: ShortArray = generateArpeggioPcm(frequencies = doubleArrayOf(440.0, 392.0, 349.23, 293.66), noteDurationMs = 90, volume = 0.55f)
    private val victoryPcm: ShortArray = generateArpeggioPcm(
        frequencies = doubleArrayOf(523.25, 659.25, 783.99, 1046.50),
        noteDurationMs = 85,
        volume = 0.60f
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

    private fun generateTonePcm(
        frequency: Double,
        durationMs: Int,
        volume: Float,
        isDecay: Boolean
    ): ShortArray {
        val numSamples = (durationMs * sampleRate) / 1000
        val pcm = ShortArray(numSamples)
        val angularFreq = 2.0 * PI * frequency / sampleRate

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val envelope = if (isDecay) (1.0 - progress * 0.85).toFloat() else 1.0f
            val sample = sin(i * angularFreq) * Short.MAX_VALUE * volume * envelope
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

        for (i in 0 until pulseSamples) {
            pcm[i] = (sin(i * angularFreq) * Short.MAX_VALUE * volume).toInt().toShort()
        }
        val secondStart = pulseSamples + gapSamples
        for (i in 0 until pulseSamples) {
            pcm[secondStart + i] = (sin(i * angularFreq) * Short.MAX_VALUE * volume).toInt().toShort()
        }
        return pcm
    }

    private fun generateArpeggioPcm(
        frequencies: DoubleArray,
        noteDurationMs: Int,
        volume: Float
    ): ShortArray {
        val noteSamples = (noteDurationMs * sampleRate) / 1000
        val totalSamples = noteSamples * frequencies.size
        val pcm = ShortArray(totalSamples)

        for ((index, freq) in frequencies.withIndex()) {
            val offset = index * noteSamples
            val angularFreq = 2.0 * PI * freq / sampleRate
            for (i in 0 until noteSamples) {
                val envelope = 1.0f - (i.toFloat() / noteSamples) * 0.3f
                val sample = sin(i * angularFreq) * Short.MAX_VALUE * volume * envelope
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
