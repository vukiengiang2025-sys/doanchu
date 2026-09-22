package com.example.solver

import com.example.game.ResultTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Output of ProbabilitySolver:
 * Clearly separates:
 * 1. LIKELY ANSWERS (Candidates that meet 100% of game constraints, ranked by statistical priority score)
 * 2. BEST INFORMATION GUESSES (Words that yield highest Shannon entropy to prune candidates most rapidly)
 */
data class SolverResult(
    val remainingCandidatesCount: Int,
    val topCandidates: List<SolverCandidate>, // Mode A: Likely Answers
    val bestInformationProbes: List<InformationProbe>, // Mode B: Best Information Probes
    val constraints: GameConstraints,
    val eliminatedCount: Int
)

object ProbabilitySolver {

    /**
     * Solves and ranks candidates given a list of prior guesses and their evaluated tiles.
     * Guaranteed 100% offline, deterministic, pure mathematical & statistical algorithm.
     * Supports both answer candidates and external guess pool for maximum entropy probe finding.
     */
    suspend fun solve(
        dictionaryWords: List<String>,
        targetLength: Int,
        guesses: List<Pair<String, List<ResultTile>>>,
        weights: SolverWeights = SolverWeights(),
        wordFrequencyMap: Map<String, Int> = emptyMap(),
        guessDictionaryWords: List<String> = emptyList()
    ): SolverResult = withContext(Dispatchers.Default) {
        val constraints = GameConstraints(targetLength)

        // Accumulate constraints from each guess
        for ((guess, tiles) in guesses) {
            constraints.addGuess(guess, tiles)
        }

        // Filter words that match targetLength
        val lengthFiltered = dictionaryWords.filter { it.length == targetLength }

        // Filter candidate set with accumulated constraints (Likely Answers)
        val matchingCandidates = CandidateFilter.filter(lengthFiltered, constraints)
        val eliminatedCount = lengthFiltered.size - matchingCandidates.size

        // Analyze corpus statistics
        val corpusStats = LetterFrequencyAnalyzer.analyze(lengthFiltered, targetLength)

        // Score and rank candidates (Mode A)
        val rankedCandidates = ProbabilityEngine.scoreCandidates(
            candidates = matchingCandidates,
            corpusStats = corpusStats,
            weights = weights,
            wordFrequencyMap = wordFrequencyMap
        )

        // Prepare pool for Information Probes (Mode B)
        val guessPool = if (guessDictionaryWords.isNotEmpty()) {
            guessDictionaryWords.filter { it.length == targetLength }
        } else {
            lengthFiltered
        }

        val bestProbes = ProbabilityEngine.findBestInformationProbes(
            guessPool = guessPool,
            answerCandidates = matchingCandidates,
            maxProbes = 8
        )

        SolverResult(
            remainingCandidatesCount = matchingCandidates.size,
            topCandidates = rankedCandidates,
            bestInformationProbes = bestProbes,
            constraints = constraints,
            eliminatedCount = eliminatedCount
        )
    }

    /**
     * Explains why a specific word is or isn't a valid candidate under current constraints.
     */
    fun explainWord(word: String, constraints: GameConstraints): String {
        return constraints.getEliminationReason(word) ?: "Từ này PHÙ HỢP hoàn toàn với tất cả ràng buộc hiện tại!"
    }
}
