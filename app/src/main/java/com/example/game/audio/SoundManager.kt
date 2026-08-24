package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ISoundManager {
    fun playMatchSound()
    fun playSpecialCreatedSound()
    fun playDetonationSound()
    fun playComboSound()
    fun playVictorySound()
    fun playGameOverSound()
    fun release()
}

class SoundManager(
    private val context: Context? = null,
    private val soundEnabledFlow: StateFlow<Boolean> = MutableStateFlow(true)
) : ISoundManager {

    private var soundPool: SoundPool? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    override fun playMatchSound() {
        if (!soundEnabledFlow.value) return
    }

    override fun playSpecialCreatedSound() {
        if (!soundEnabledFlow.value) return
    }

    override fun playDetonationSound() {
        if (!soundEnabledFlow.value) return
    }

    override fun playComboSound() {
        if (!soundEnabledFlow.value) return
    }

    override fun playVictorySound() {
        if (!soundEnabledFlow.value) return
    }

    override fun playGameOverSound() {
        if (!soundEnabledFlow.value) return
    }

    override fun release() {
        soundPool?.release()
        soundPool = null
    }
}
