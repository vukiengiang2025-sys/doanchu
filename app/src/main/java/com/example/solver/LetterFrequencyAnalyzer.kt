package com.example.solver

import com.example.normalization.VietnameseNormalizer

data class CorpusStatistics(
    val totalWords: Int,
    val letterCounts: Map<Char, Int>,
    val positionLetterCounts: Map<Int, Map<Char, Int>>,
    val totalLetters: Int
)

object LetterFrequencyAnalyzer {

    /**
     * Builds statistical models from a list of words of a specific length.
     */
    fun analyze(words: List<String>, wordLength: Int): CorpusStatistics {
        val letterCounts = mutableMapOf<Char, Int>()
        val posCounts = mutableMapOf<Int, MutableMap<Char, Int>>()
        for (i in 0 until wordLength) {
            posCounts[i] = mutableMapOf()
        }

        var totalLetters = 0
        for (wRaw in words) {
            val w = VietnameseNormalizer.normalize(wRaw)
            if (w.length != wordLength) continue

            for (i in 0 until wordLength) {
                val c = w[i]
                letterCounts[c] = (letterCounts[c] ?: 0) + 1
                val pMap = posCounts[i]!!
                pMap[c] = (pMap[c] ?: 0) + 1
                totalLetters++
            }
        }

        return CorpusStatistics(
            totalWords = words.size,
            letterCounts = letterCounts,
            positionLetterCounts = posCounts,
            totalLetters = totalLetters
        )
    }

    /**
     * Computes letter frequency score (0.0 .. 1.0) for a candidate based on unique letters.
     */
    fun computeLetterScore(word: String, stats: CorpusStatistics): Double {
        if (stats.totalLetters == 0) return 0.5
        val uniqueChars = word.toSet()
        val sum = uniqueChars.sumOf { (stats.letterCounts[it] ?: 0).toDouble() }
        // Normalize against highest letter count
        val maxLetter = stats.letterCounts.values.maxOrNull()?.toDouble() ?: 1.0
        return (sum / (uniqueChars.size * maxLetter)).coerceIn(0.0, 1.0)
    }

    /**
     * Computes positional frequency score (0.0 .. 1.0) for a candidate word.
     */
    fun computePositionScore(word: String, stats: CorpusStatistics): Double {
        if (stats.totalWords == 0) return 0.5
        var total = 0.0
        for (i in word.indices) {
            val char = word[i]
            val count = stats.positionLetterCounts[i]?.get(char) ?: 0
            val maxAtPos = stats.positionLetterCounts[i]?.values?.maxOrNull() ?: 1
            total += count.toDouble() / maxAtPos.toDouble()
        }
        return (total / word.length).coerceIn(0.0, 1.0)
    }
}
