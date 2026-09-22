package com.example.solver

import kotlin.math.max

object ProbabilityEngine {

    /**
     * Evaluates and scores all candidates in the candidateSet (Likely Answers).
     */
    fun scoreCandidates(
        candidates: List<String>,
        corpusStats: CorpusStatistics,
        weights: SolverWeights = SolverWeights(),
        wordFrequencyMap: Map<String, Int> = emptyMap()
    ): List<SolverCandidate> {
        if (candidates.isEmpty()) return emptyList()

        val maxWordFreq = max(1, wordFrequencyMap.values.maxOrNull() ?: 1).toDouble()
        val candidateCount = candidates.size

        // For <= 200 candidates, full simulation against all candidates is fast
        val evaluateFullEntropy = candidateCount <= 200
        val entropyEvaluationPool = if (evaluateFullEntropy) {
            candidates
        } else {
            candidates.take(100)
        }

        val rawResults = mutableListOf<Triple<String, Double, Pair<EntropyResult, CandidateScoreBreakdown>>>()

        for (word in candidates) {
            // 1. Word frequency score (0.05 .. 1.0)
            val rawWf = wordFrequencyMap[word] ?: 1
            val wfScore = (rawWf.toDouble() / maxWordFreq).coerceIn(0.05, 1.0)

            // 2. Letter frequency score (0.0 .. 1.0)
            val lfScore = LetterFrequencyAnalyzer.computeLetterScore(word, corpusStats)

            // 3. Position frequency score (0.0 .. 1.0)
            val pfScore = LetterFrequencyAnalyzer.computePositionScore(word, corpusStats)

            // 4. Information gain (Entropy)
            val entropyResult = EntropyCalculator.calculateEntropy(word, entropyEvaluationPool)
            val maxPossibleEntropy = max(1.0, kotlin.math.ln(max(2.0, entropyEvaluationPool.size.toDouble())) / kotlin.math.ln(2.0))
            val igScore = (entropyResult.entropy / maxPossibleEntropy).coerceIn(0.0, 1.0)

            // 5. Pattern fit score (unique letters reward)
            val uniqueRatio = word.toSet().size.toDouble() / word.length.toDouble()
            val fitScore = uniqueRatio.coerceIn(0.2, 1.0)

            // Absolute composite score out of 100
            val compositeScore = (
                wfScore * weights.wordFrequencyWeight +
                lfScore * weights.letterFrequencyWeight +
                pfScore * weights.positionFrequencyWeight +
                igScore * weights.informationGainWeight +
                fitScore * weights.patternFitWeight
            ) * 100.0

            val breakdown = CandidateScoreBreakdown(
                wordFrequencyScore = wfScore,
                letterFrequencyScore = lfScore,
                positionFrequencyScore = pfScore,
                informationGainScore = igScore,
                patternFitScore = fitScore
            )

            rawResults.add(Triple(word, compositeScore, Pair(entropyResult, breakdown)))
        }

        // Sort by priorityScore descending
        rawResults.sortByDescending { it.second }

        // Compute relative score weight (proportional share of score across candidate pool)
        val totalComposite = rawResults.sumOf { it.second }
        val finalCandidates = rawResults.map { (word, score, data) ->
            val (entropyRes, breakdown) = data
            val relScoreWeight = if (totalComposite > 0) (score / totalComposite) * 100.0 else 100.0 / candidates.size
            SolverCandidate(
                word = word,
                relativeScoreWeight = relScoreWeight,
                priorityScore = score,
                entropy = entropyRes.entropy,
                expectedRemaining = entropyRes.expectedRemainingCandidates,
                breakdown = breakdown
            )
        }

        return finalCandidates
    }

    /**
     * Evaluates best information guesses (Information Probes) from guess pool against answer pool.
     * Selects words that maximize Shannon Entropy and minimize Expected Remaining Candidates.
     */
    fun findBestInformationProbes(
        guessPool: List<String>,
        answerCandidates: List<String>,
        maxProbes: Int = 10
    ): List<InformationProbe> {
        if (answerCandidates.isEmpty()) return emptyList()
        if (answerCandidates.size == 1) {
            val sole = answerCandidates.first()
            return listOf(
                InformationProbe(
                    word = sole,
                    entropy = 0.0,
                    expectedRemainingCandidates = 1.0,
                    worstCaseRemainingCandidates = 1,
                    uniqueLettersCount = sole.toSet().size,
                    isPossibleAnswer = true
                )
            )
        }

        // Representative evaluation set if answerCandidates is huge
        val evaluationAnswers = if (answerCandidates.size > 200) {
            answerCandidates.take(150)
        } else {
            answerCandidates
        }

        // To maintain ultra-high performance, pick promising probe candidates:
        // Include all answerCandidates (or top 100 of them) + high-unique-letter words from guessPool
        val candidateAnswersSet = answerCandidates.toSet()
        val poolToEvaluate = mutableListOf<String>()
        poolToEvaluate.addAll(answerCandidates.take(120))

        val outsideGuesses = guessPool.filter { it !in candidateAnswersSet }
            .sortedByDescending { it.toSet().size }
            .take(80)
        poolToEvaluate.addAll(outsideGuesses)

        val probes = poolToEvaluate.distinct().map { guessWord ->
            val ent = EntropyCalculator.calculateEntropy(guessWord, evaluationAnswers)
            InformationProbe(
                word = guessWord,
                entropy = ent.entropy,
                expectedRemainingCandidates = ent.expectedRemainingCandidates,
                worstCaseRemainingCandidates = ent.worstCaseRemainingCandidates,
                uniqueLettersCount = ent.uniqueLettersCount,
                isPossibleAnswer = candidateAnswersSet.contains(guessWord)
            )
        }

        // Sort by Highest Entropy, then lowest expected remaining
        return probes.sortedWith(
            compareByDescending<InformationProbe> { it.entropy }
                .thenBy { it.expectedRemainingCandidates }
        ).take(maxProbes)
    }
}
