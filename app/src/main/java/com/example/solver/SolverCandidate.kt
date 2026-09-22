package com.example.solver

/**
 * Breakdown of statistical prioritization components for a candidate answer.
 */
data class CandidateScoreBreakdown(
    val wordFrequencyScore: Double,
    val letterFrequencyScore: Double,
    val positionFrequencyScore: Double,
    val informationGainScore: Double,
    val patternFitScore: Double
)

/**
 * Candidate answer scored by statistical priority.
 *
 * NOTE ON PROBABILITY SEMANTICS:
 * relativeScoreWeight is a relative statistical score within the currently filtered candidate set,
 * NOT an empirical true probability of the answer.
 */
data class SolverCandidate(
    val word: String,
    val relativeScoreWeight: Double, // Relative percentage (e.g. 18.2%), normalized across current candidates
    val priorityScore: Double,      // Absolute composite score (e.g. 82.4 / 100)
    val entropy: Double,
    val expectedRemaining: Double,
    val breakdown: CandidateScoreBreakdown
) {
    // Backward compatibility property for existing views
    val relativeProbability: Double get() = relativeScoreWeight
    val totalScore: Double get() = priorityScore
}

/**
 * An Information Probe / Guess: a word that may or may not be an answer itself,
 * but maximizes Shannon Entropy to cut down the search space most effectively.
 */
data class InformationProbe(
    val word: String,
    val entropy: Double,
    val expectedRemainingCandidates: Double,
    val worstCaseRemainingCandidates: Int,
    val uniqueLettersCount: Int,
    val isPossibleAnswer: Boolean
)

data class SolverWeights(
    val wordFrequencyWeight: Double = 0.30,
    val letterFrequencyWeight: Double = 0.25,
    val positionFrequencyWeight: Double = 0.20,
    val informationGainWeight: Double = 0.15,
    val patternFitWeight: Double = 0.10
)
