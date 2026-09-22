package com.example.solver

import com.example.game.GuessEvaluator
import kotlin.math.ln
import kotlin.math.max

data class EntropyResult(
    val entropy: Double,
    val expectedRemainingCandidates: Double,
    val worstCaseRemainingCandidates: Int,
    val patternCount: Int,
    val uniqueLettersCount: Int
)

object EntropyCalculator {

    private val LOG2 = ln(2.0)

    private fun log2(value: Double): Double {
        return if (value <= 0.0) 0.0 else ln(value) / LOG2
    }

    /**
     * Calculates Shannon entropy for guessWord against the candidateSet.
     * CandidateSet contains the words that could currently be the answer.
     * H = -Σ p(x) * log2(p(x))
     *
     * Also calculates:
     * - ExpectedRemainingCandidates = Σ (count^2) / N
     * - WorstCaseRemainingCandidates = max(count)
     * - UniqueLettersCount
     */
    fun calculateEntropy(guessWord: String, candidateSet: List<String>): EntropyResult {
        val uniqueLetters = guessWord.toSet().size
        if (candidateSet.isEmpty()) {
            return EntropyResult(0.0, 0.0, 0, 0, uniqueLetters)
        }
        if (candidateSet.size == 1) {
            return EntropyResult(0.0, 1.0, 1, 1, uniqueLetters)
        }

        val totalCandidates = candidateSet.size.toDouble()
        val patternCounts = mutableMapOf<String, Int>()

        for (answer in candidateSet) {
            val tiles = GuessEvaluator.evaluate(answer, guessWord)
            val pattern = GuessEvaluator.patternToString(tiles)
            patternCounts[pattern] = (patternCounts[pattern] ?: 0) + 1
        }

        var entropy = 0.0
        var sumSquares = 0.0
        var worstCase = 0

        for ((_, count) in patternCounts) {
            val p = count / totalCandidates
            entropy -= p * log2(p)
            sumSquares += count.toDouble() * count.toDouble()
            worstCase = max(worstCase, count)
        }

        val expectedRemaining = sumSquares / totalCandidates

        return EntropyResult(
            entropy = entropy,
            expectedRemainingCandidates = expectedRemaining,
            worstCaseRemainingCandidates = worstCase,
            patternCount = patternCounts.size,
            uniqueLettersCount = uniqueLetters
        )
    }
}
