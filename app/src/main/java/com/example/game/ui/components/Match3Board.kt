package com.example.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.model.BoardPosition
import com.example.game.model.Match3Board
import com.example.ui.theme.BoardBackground
import com.example.ui.theme.BoardBorder

@Composable
fun Match3BoardView(
    board: Match3Board,
    selectedPosition: BoardPosition?,
    onTileClick: (BoardPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(BoardBackground)
            .border(3.dp, BoardBorder, RoundedCornerShape(16.dp))
            .padding(6.dp)
            .testTag("match3_board_view")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in 0 until board.rows) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0 until board.cols) {
                        val pos = BoardPosition(row, col)
                        val tile = board[pos]
                        val isSelected = selectedPosition == pos

                        CandyTileView(
                            tile = tile,
                            position = pos,
                            isSelected = isSelected,
                            onClick = { onTileClick(pos) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
