package com.example.game.model

data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val particlesEnabled: Boolean = true
)
