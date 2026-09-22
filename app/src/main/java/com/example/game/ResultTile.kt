package com.example.game

enum class TileColor(val displayName: String, val symbol: String) {
    GREEN("Đúng vị trí", "🟩"),
    YELLOW("Đúng chữ, sai vị trí", "🟨"),
    BLUE("Đúng nguyên âm, sai dấu", "🟦"),
    GRAY("Không có trong từ", "⬛")
}

data class ResultTile(
    val position: Int,
    val letter: Char,
    val color: TileColor,
    val explanation: String
)
