package com.example.solver

import com.example.game.TileColor
import com.example.normalization.VietnameseNormalizer

object InteractiveSolverEngine {

    /**
     * Cycles color sequentially: GRAY -> GREEN -> YELLOW -> BLUE -> GRAY.
     */
    fun getNextColor(current: TileColor): TileColor {
        return when (current) {
            TileColor.GRAY -> TileColor.GREEN
            TileColor.GREEN -> TileColor.YELLOW
            TileColor.YELLOW -> TileColor.BLUE
            TileColor.BLUE -> TileColor.GRAY
        }
    }

    /**
     * Analyzes interactive guesses against offline dictionary candidates.
     * Pure offline, deterministic mathematical evaluation.
     */
    suspend fun analyze(
        dictionaryWords: List<String>,
        wordLength: Int,
        rows: List<InteractiveGuessRow>,
        weights: SolverWeights = SolverWeights(),
        wordFrequencyMap: Map<String, Int> = emptyMap(),
        guessDictionaryWords: List<String> = emptyList()
    ): InteractiveSolverConclusion {
        if (rows.isEmpty()) {
            return InteractiveSolverConclusion.Idle
        }

        val evaluatedGuesses = rows.map { row ->
            Pair(row.word, row.toResultTiles())
        }

        val solverResult = ProbabilitySolver.solve(
            dictionaryWords = dictionaryWords,
            targetLength = wordLength,
            guesses = evaluatedGuesses,
            weights = weights,
            wordFrequencyMap = wordFrequencyMap,
            guessDictionaryWords = guessDictionaryWords
        )

        val remaining = solverResult.remainingCandidatesCount
        val candidates = solverResult.topCandidates

        return when {
            remaining == 0 -> {
                InteractiveSolverConclusion.ConflictOrNoMatch(
                    "Không tìm thấy từ tiếng Việt nào trong từ điển thỏa mãn các màu bạn đã tô. Hãy kiểm tra xem có ô nào bị tô nhầm màu không!"
                )
            }
            remaining == 1 -> {
                val winner = candidates.first()
                InteractiveSolverConclusion.ExactMatchFound(
                    exactWord = winner.word,
                    details = winner
                )
            }
            else -> {
                val bestEntropyWord = solverResult.bestInformationProbes.firstOrNull()?.word
                    ?: candidates.maxByOrNull { it.entropy }?.word
                InteractiveSolverConclusion.SuggestionsAvailable(
                    remainingCount = remaining,
                    topCandidates = candidates,
                    bestEntropyWord = bestEntropyWord,
                    bestInformationProbes = solverResult.bestInformationProbes
                )
            }
        }
    }
}
