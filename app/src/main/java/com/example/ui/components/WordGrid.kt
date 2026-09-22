package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.game.GameState
import com.example.game.ResultTile

@Composable
fun WordGrid(
    state: GameState,
    colorBlindMode: Boolean = false,
    modifier: Modifier = Modifier,
    onTileClick: ((ResultTile) -> Unit)? = null
) {
    val tileSize = when (state.wordLength) {
        7 -> 42.dp
        6 -> 46.dp
        5 -> 52.dp
        else -> 58.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        for (rowIndex in 0 until state.maxAttempts) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    // 1. Completed rows with evaluation
                    rowIndex < state.rows.size -> {
                        val row = state.rows[rowIndex]
                        for (colIndex in 0 until state.wordLength) {
                            val tile = row.tiles.getOrNull(colIndex)
                            TileView(
                                letter = tile?.letter,
                                color = tile?.color,
                                size = tileSize,
                                colorBlindMode = colorBlindMode,
                                onClick = if (tile != null && onTileClick != null) {
                                    { onTileClick(tile) }
                                } else null
                            )
                        }
                    }

                    // 2. Active input row
                    rowIndex == state.rows.size -> {
                        for (colIndex in 0 until state.wordLength) {
                            val char = state.currentInput.getOrNull(colIndex)
                            TileView(
                                letter = char,
                                color = null,
                                size = tileSize,
                                colorBlindMode = colorBlindMode
                            )
                        }
                    }

                    // 3. Upcoming empty rows
                    else -> {
                        for (colIndex in 0 until state.wordLength) {
                            TileView(
                                letter = null,
                                color = null,
                                size = tileSize,
                                colorBlindMode = colorBlindMode
                            )
                        }
                    }
                }
            }
        }
    }
}
