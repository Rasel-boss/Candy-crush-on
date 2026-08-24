package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CandyBlue
import com.example.ui.theme.CandyGreen
import com.example.ui.theme.CandyOrange
import com.example.ui.theme.CandyPurple
import com.example.ui.theme.CandyRed
import com.example.ui.theme.CandyYellow

enum class CandyType(val displayName: String, val color: Color) {
    RED("Red", CandyRed),
    BLUE("Blue", CandyBlue),
    GREEN("Green", CandyGreen),
    YELLOW("Yellow", CandyYellow),
    PURPLE("Purple", CandyPurple),
    ORANGE("Orange", CandyOrange)
}
