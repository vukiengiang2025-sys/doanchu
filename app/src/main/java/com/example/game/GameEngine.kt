package com.example.game

import com.example.normalization.VietnameseNormalizer

enum class GameStatus {
    PLAYING,
    WON,
    LOST
}

data class GuessRow(
    val guess: String,
    val tiles: List<ResultTile>
)

data class GameState(
    val wordLength: Int = 4,
    val maxAttempts: Int = 6,
    val answer: String = "",
    val currentInput: String = "",
    val rows: List<GuessRow> = emptyList(),
    val status: GameStatus = GameStatus.PLAYING,
    val message: String? = null,
    val keyboardColors: Map<Char, TileColor> = emptyMap(),
    val isEvaluating: Boolean = false
)

class GameEngine(
    val wordLength: Int = 4,
    val maxAttempts: Int = 6,
    val answer: String
) {
    var state: GameState = GameState(
        wordLength = wordLength,
        maxAttempts = maxAttempts,
        answer = answer
    )
        private set

    /**
     * Appends a character to currentInput if not full.
     */
    fun inputChar(char: Char): GameState {
        if (state.status != GameStatus.PLAYING) return state
        if (state.currentInput.length >= wordLength) return state

        val updatedInput = state.currentInput + char.lowercaseChar()
        state = state.copy(currentInput = updatedInput, message = null)
        return state
    }

    /**
     * Removes the last character from currentInput.
     */
    fun deleteChar(): GameState {
        if (state.status != GameStatus.PLAYING) return state
        if (state.currentInput.isEmpty()) return state

        val updatedInput = state.currentInput.dropLast(1)
        state = state.copy(currentInput = updatedInput, message = null)
        return state
    }

    /**
     * Clears current input line.
     */
    fun clearInput(): GameState {
        if (state.status != GameStatus.PLAYING) return state
        state = state.copy(currentInput = "", message = null)
        return state
    }

    /**
     * Submits the current guess.
     * Evaluates against answer and updates game status and keyboard colors.
     */
    fun submitGuess(isValidDictionaryWord: Boolean): Pair<GameState, Boolean> {
        if (state.status != GameStatus.PLAYING) return Pair(state, false)

        val guess = state.currentInput
        if (guess.length != wordLength) {
            state = state.copy(message = "Từ phải có đúng $wordLength chữ cái!")
            return Pair(state, false)
        }

        if (!isValidDictionaryWord) {
            state = state.copy(message = "Từ '$guess' không có trong từ điển!")
            return Pair(state, false)
        }

        // Evaluate guess
        val tiles = GuessEvaluator.evaluate(answer, guess)
        val newRow = GuessRow(guess, tiles)
        val updatedRows = state.rows + newRow

        // Update keyboard colors (GREEN overrides YELLOW/BLUE/GRAY; YELLOW overrides BLUE/GRAY; BLUE overrides GRAY)
        val updatedKeyboard = state.keyboardColors.toMutableMap()
        for (tile in tiles) {
            val char = tile.letter
            val existing = updatedKeyboard[char]
            if (existing == null) {
                updatedKeyboard[char] = tile.color
            } else if (tile.color == TileColor.GREEN) {
                updatedKeyboard[char] = TileColor.GREEN
            } else if (tile.color == TileColor.YELLOW && existing != TileColor.GREEN) {
                updatedKeyboard[char] = TileColor.YELLOW
            } else if (tile.color == TileColor.BLUE && existing != TileColor.GREEN && existing != TileColor.YELLOW) {
                updatedKeyboard[char] = TileColor.BLUE
            }
        }

        val isWon = tiles.all { it.color == TileColor.GREEN }
        val isLost = !isWon && updatedRows.size >= maxAttempts

        val newStatus = when {
            isWon -> GameStatus.WON
            isLost -> GameStatus.LOST
            else -> GameStatus.PLAYING
        }

        val msg = when {
            isWon -> "Chúc mừng! Bạn đã đoán đúng từ '$answer'!"
            isLost -> "Hết lượt! Đáp án chính xác là: '$answer'"
            else -> null
        }

        state = state.copy(
            rows = updatedRows,
            currentInput = "",
            status = newStatus,
            message = msg,
            keyboardColors = updatedKeyboard
        )

        return Pair(state, true)
    }
}
