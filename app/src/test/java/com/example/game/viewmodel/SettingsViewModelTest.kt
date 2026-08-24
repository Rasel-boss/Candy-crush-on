package com.example.game.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        viewModel = SettingsViewModel()
    }

    @Test
    fun `sound is enabled by default`() {
        val state = viewModel.settingsState.value
        assertTrue(state.soundEnabled)
    }

    @Test
    fun `vibration is enabled by default`() {
        val state = viewModel.settingsState.value
        assertTrue(state.vibrationEnabled)
    }

    @Test
    fun `sound can be disabled and enabled again via setSoundEnabled`() {
        viewModel.setSoundEnabled(false)
        assertFalse(viewModel.settingsState.value.soundEnabled)

        viewModel.setSoundEnabled(true)
        assertTrue(viewModel.settingsState.value.soundEnabled)
    }

    @Test
    fun `vibration can be disabled and enabled again via setVibrationEnabled`() {
        viewModel.setVibrationEnabled(false)
        assertFalse(viewModel.settingsState.value.vibrationEnabled)

        viewModel.setVibrationEnabled(true)
        assertTrue(viewModel.settingsState.value.vibrationEnabled)
    }

    @Test
    fun `toggleSound inverts current sound enabled status`() {
        assertTrue(viewModel.settingsState.value.soundEnabled)

        viewModel.toggleSound()
        assertFalse(viewModel.settingsState.value.soundEnabled)

        viewModel.toggleSound()
        assertTrue(viewModel.settingsState.value.soundEnabled)
    }

    @Test
    fun `toggleVibration inverts current vibration enabled status`() {
        assertTrue(viewModel.settingsState.value.vibrationEnabled)

        viewModel.toggleVibration()
        assertFalse(viewModel.settingsState.value.vibrationEnabled)

        viewModel.toggleVibration()
        assertTrue(viewModel.settingsState.value.vibrationEnabled)
    }
}
