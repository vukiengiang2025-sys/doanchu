package com.example.solver

import com.example.game.ResultTile
import com.example.game.TileColor
import com.example.normalization.VietnameseNormalizer

/**
 * Filter mode for word types in solver.
 */
enum class WordKindFilter(val displayName: String) {
    ALL("Tất cả từ"),
    SINGLE("Chỉ từ đơn"),
    COMPOUND("Chỉ từ ghép")
}

/**
 * Represents a user-entered word in Interactive Solver mode,
 * with each character manually colored by the user.
 */
data class InteractiveGuessRow(
    val id: String = java.util.UUID.randomUUID().toString(),
    val word: String,
    val colors: List<TileColor>
) {
    init {
        require(word.length == colors.size) { "Số màu (${colors.size}) phải bằng số ký tự của từ (${word.length})" }
    }

    fun toResultTiles(): List<ResultTile> {
        return word.mapIndexed { index, char ->
            val col = colors[index]
            val charDisplay = if (char == ' ') "dấu cách" else "'$char'"
            ResultTile(
                letter = char,
                color = col,
                position = index,
                explanation = "Màu của $charDisplay: ${col.displayName}"
            )
        }
    }
}

sealed class InteractiveSolverConclusion {
    /** Chưa có dữ liệu đoán hoặc chưa bấm phân tích */
    object Idle : InteractiveSolverConclusion()

    /** Đã tìm thấy chính xác 100% duy nhất 1 từ */
    data class ExactMatchFound(
        val exactWord: String,
        val details: SolverCandidate
    ) : InteractiveSolverConclusion()

    /** Còn nhiều hơn 1 từ: gợi ý Likely Answers và Information Probes */
    data class SuggestionsAvailable(
        val remainingCount: Int,
        val topCandidates: List<SolverCandidate>,
        val bestEntropyWord: String?,
        val bestInformationProbes: List<InformationProbe> = emptyList()
    ) : InteractiveSolverConclusion()

    /** Không tìm thấy từ nào trong từ điển thỏa mãn (mâu thuẫn tô màu hoặc từ lạ) */
    data class ConflictOrNoMatch(
        val message: String
    ) : InteractiveSolverConclusion()
}

data class InteractiveSolverState(
    val wordLength: Int = 4,
    val wordKindFilter: WordKindFilter = WordKindFilter.ALL,
    val allowCompoundSpaces: Boolean = true,
    val guessRows: List<InteractiveGuessRow> = emptyList(),
    val currentInputWord: String = "",
    val isAnalyzing: Boolean = false,
    val conclusion: InteractiveSolverConclusion = InteractiveSolverConclusion.Idle,
    val errorMessage: String? = null
)

