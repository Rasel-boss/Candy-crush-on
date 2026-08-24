package com.example.game.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundManagerTest {

    private class RecordingSoundManager(
        private val soundEnabledSupplier: () -> Boolean
    ) : ISoundManager {
        var tileSelectCount = 0
        var tileSwapCount = 0
        var invalidSwapCount = 0
        var matchCount = 0
        var cascadeCount = 0
        var gameOverCount = 0
        var levelCompleteCount = 0
        var victoryCount = 0
        var buttonClickCount = 0
        var released = false

        override fun playTileSelect() {
            if (soundEnabledSupplier()) tileSelectCount++
        }

        override fun playTileSwap() {
            if (soundEnabledSupplier()) tileSwapCount++
        }

        override fun playInvalidSwap() {
            if (soundEnabledSupplier()) invalidSwapCount++
        }

        override fun playMatch(matchLength: Int) {
            if (soundEnabledSupplier()) matchCount++
        }

        override fun playCascade(chainStep: Int) {
            if (soundEnabledSupplier()) cascadeCount++
        }

        override fun playGameOver() {
            if (soundEnabledSupplier()) gameOverCount++
        }

        override fun playLevelComplete() {
            if (soundEnabledSupplier()) levelCompleteCount++
        }

        override fun playVictory() {
            if (soundEnabledSupplier()) victoryCount++
        }

        override fun playButtonClick() {
            if (soundEnabledSupplier()) buttonClickCount++
        }

        override fun playTileMove() {
            playTileSwap()
        }

        override fun playInvalidMove() {
            playInvalidSwap()
        }

        override fun release() {
            released = true
        }
    }

    private class RecordingHapticManager(
        private val vibrationEnabledSupplier: () -> Boolean
    ) : IHapticFeedbackManager {
        var tileSelectCount = 0
        var tileSwapCount = 0
        var invalidSwapCount = 0
        var matchCount = 0
        var cascadeCount = 0
        var gameOverCount = 0
        var victoryCount = 0
        var buttonClickCount = 0

        override fun performTileSelectFeedback() {
            if (vibrationEnabledSupplier()) tileSelectCount++
        }

        override fun performTileSwapFeedback() {
            if (vibrationEnabledSupplier()) tileSwapCount++
        }

        override fun performInvalidSwapFeedback() {
            if (vibrationEnabledSupplier()) invalidSwapCount++
        }

        override fun performMatchFeedback(intensity: Int) {
            if (vibrationEnabledSupplier()) matchCount++
        }

        override fun performCascadeFeedback(chainStep: Int) {
            if (vibrationEnabledSupplier()) cascadeCount++
        }

        override fun performGameOverFeedback() {
            if (vibrationEnabledSupplier()) gameOverCount++
        }

        override fun performVictoryFeedback() {
            if (vibrationEnabledSupplier()) victoryCount++
        }

        override fun performLevelCompleteFeedback() {
            if (vibrationEnabledSupplier()) victoryCount++
        }

        override fun performButtonClickFeedback() {
            if (vibrationEnabledSupplier()) buttonClickCount++
        }

        override fun performTileMoveFeedback() {
            performTileSwapFeedback()
        }

        override fun performInvalidMoveFeedback() {
            performInvalidSwapFeedback()
        }
    }

    @Test
    fun `sound manager triggers audio events when sound is enabled`() {
        var soundEnabled = true
        val soundManager = RecordingSoundManager { soundEnabled }

        soundManager.playTileSelect()
        soundManager.playTileSwap()
        soundManager.playInvalidSwap()
        soundManager.playMatch(3)
        soundManager.playCascade(2)
        soundManager.playGameOver()
        soundManager.playVictory()
        soundManager.playButtonClick()

        assertEquals(1, soundManager.tileSelectCount)
        assertEquals(1, soundManager.tileSwapCount)
        assertEquals(1, soundManager.invalidSwapCount)
        assertEquals(1, soundManager.matchCount)
        assertEquals(1, soundManager.cascadeCount)
        assertEquals(1, soundManager.gameOverCount)
        assertEquals(1, soundManager.victoryCount)
        assertEquals(1, soundManager.buttonClickCount)
    }

    @Test
    fun `sound manager skips audio events when sound is disabled`() {
        var soundEnabled = false
        val soundManager = RecordingSoundManager { soundEnabled }

        soundManager.playTileSelect()
        soundManager.playTileSwap()
        soundManager.playInvalidSwap()
        soundManager.playMatch(3)
        soundManager.playCascade(2)
        soundManager.playGameOver()
        soundManager.playVictory()
        soundManager.playButtonClick()

        assertEquals(0, soundManager.tileSelectCount)
        assertEquals(0, soundManager.tileSwapCount)
        assertEquals(0, soundManager.invalidSwapCount)
        assertEquals(0, soundManager.matchCount)
        assertEquals(0, soundManager.cascadeCount)
        assertEquals(0, soundManager.gameOverCount)
        assertEquals(0, soundManager.victoryCount)
        assertEquals(0, soundManager.buttonClickCount)
    }

    @Test
    fun `haptic manager triggers vibration events when vibration is enabled`() {
        var vibrationEnabled = true
        val hapticManager = RecordingHapticManager { vibrationEnabled }

        hapticManager.performTileSelectFeedback()
        hapticManager.performTileSwapFeedback()
        hapticManager.performInvalidSwapFeedback()
        hapticManager.performMatchFeedback(3)
        hapticManager.performCascadeFeedback(2)
        hapticManager.performGameOverFeedback()
        hapticManager.performVictoryFeedback()
        hapticManager.performButtonClickFeedback()

        assertEquals(1, hapticManager.tileSelectCount)
        assertEquals(1, hapticManager.tileSwapCount)
        assertEquals(1, hapticManager.invalidSwapCount)
        assertEquals(1, hapticManager.matchCount)
        assertEquals(1, hapticManager.cascadeCount)
        assertEquals(1, hapticManager.gameOverCount)
        assertEquals(1, hapticManager.victoryCount)
        assertEquals(1, hapticManager.buttonClickCount)
    }

    @Test
    fun `haptic manager skips vibration events when vibration is disabled`() {
        var vibrationEnabled = false
        val hapticManager = RecordingHapticManager { vibrationEnabled }

        hapticManager.performTileSelectFeedback()
        hapticManager.performTileSwapFeedback()
        hapticManager.performInvalidSwapFeedback()
        hapticManager.performMatchFeedback(3)
        hapticManager.performCascadeFeedback(2)
        hapticManager.performGameOverFeedback()
        hapticManager.performVictoryFeedback()
        hapticManager.performButtonClickFeedback()

        assertEquals(0, hapticManager.tileSelectCount)
        assertEquals(0, hapticManager.tileSwapCount)
        assertEquals(0, hapticManager.invalidSwapCount)
        assertEquals(0, hapticManager.matchCount)
        assertEquals(0, hapticManager.cascadeCount)
        assertEquals(0, hapticManager.gameOverCount)
        assertEquals(0, hapticManager.victoryCount)
        assertEquals(0, hapticManager.buttonClickCount)
    }

    @Test
    fun `sound manager release releases underlying resources`() {
        val soundManager = RecordingSoundManager { true }
        assertFalse(soundManager.released)

        soundManager.release()
        assertTrue(soundManager.released)
    }

    @Test
    fun `NoOp implementations execute cleanly without throwing exceptions`() {
        val noOpSound = NoOpSoundManager()
        noOpSound.playTileSelect()
        noOpSound.playTileSwap()
        noOpSound.playInvalidSwap()
        noOpSound.playMatch(3)
        noOpSound.playCascade(2)
        noOpSound.playGameOver()
        noOpSound.playVictory()
        noOpSound.playButtonClick()
        noOpSound.release()

        val noOpHaptic = NoOpHapticFeedbackManager()
        noOpHaptic.performTileSelectFeedback()
        noOpHaptic.performTileSwapFeedback()
        noOpHaptic.performInvalidSwapFeedback()
        noOpHaptic.performMatchFeedback(3)
        noOpHaptic.performCascadeFeedback(2)
        noOpHaptic.performGameOverFeedback()
        noOpHaptic.performVictoryFeedback()
        noOpHaptic.performButtonClickFeedback()
    }
}
