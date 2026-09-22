package com.example

import com.example.game.GuessEvaluator
import com.example.game.ResultTile
import com.example.game.TileColor
import com.example.normalization.Tone
import com.example.normalization.VietnameseNormalizer
import com.example.normalization.VowelBase
import com.example.solver.CandidateFilter
import com.example.solver.EntropyCalculator
import com.example.solver.GameConstraints
import com.example.solver.LetterFrequencyAnalyzer
import com.example.solver.ProbabilityEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VietnameseGameLogicTest {

    @Test
    fun testVietnameseNormalizerDecomposition() {
        val d1 = VietnameseNormalizer.decompose('á')
        assertTrue(d1.isVowel)
        assertEquals(VowelBase.A, d1.vowelBase)
        assertEquals(Tone.ACUTE, d1.tone)

        val d2 = VietnameseNormalizer.decompose('ằ')
        assertTrue(d2.isVowel)
        assertEquals(VowelBase.A_BREVE, d2.vowelBase)
        assertEquals(Tone.GRAVE, d2.tone)

        val d3 = VietnameseNormalizer.decompose('n') // consonant
        assertFalse(d3.isVowel)
        val c = VietnameseNormalizer.decompose('m')
        assertFalse(c.isVowel)
        assertEquals(Tone.NONE, c.tone)
    }

    @Test
    fun testGuessEvaluatorExactGreen() {
        val answer = "ngày"
        val guess = "ngày"
        val tiles = GuessEvaluator.evaluate(answer, guess)

        assertEquals(4, tiles.size)
        assertTrue(tiles.all { it.color == TileColor.GREEN })
    }

    @Test
    fun testGuessEvaluatorBlueToneMismatch() {
        // Answer: "năm", Guess: "nằm"
        // 'n' -> GREEN, 'm' -> GREEN
        // 'ằ' vs 'ă': same base vowel (ă), different tone (huyền vs ngang) -> BLUE!
        val answer = "năm"
        val guess = "nằm"
        val tiles = GuessEvaluator.evaluate(answer, guess)

        assertEquals(TileColor.GREEN, tiles[0].color)
        assertEquals(TileColor.BLUE, tiles[1].color)
        assertEquals(TileColor.GREEN, tiles[2].color)
    }

    @Test
    fun testGuessEvaluatorYellowAndGray() {
        // Answer: "xanh", Guess: "hành"
        // pos 3: 'h' exact match with 'h' in "xanh" -> GREEN (prioritized over yellow!)
        // pos 2: 'n' exact match with 'n' in "xanh" -> GREEN
        // pos 1: 'à' vs 'a': same base vowel 'a', different tone -> BLUE
        // pos 0: 'h': already matched at pos 3 -> GRAY
        val answer = "xanh"
        val guess = "hành"
        val tiles = GuessEvaluator.evaluate(answer, guess)

        assertEquals(TileColor.GRAY, tiles[0].color)
        assertEquals(TileColor.BLUE, tiles[1].color)
        assertEquals(TileColor.GREEN, tiles[2].color)
        assertEquals(TileColor.GREEN, tiles[3].color)
    }

    @Test
    fun testGuessEvaluatorYellow() {
        // Answer: "cơm", Guess: "mực"
        // 'm': pos 0 in guess, exists in answer at pos 2 -> YELLOW
        // 'ự' vs 'ơ': different base vowels -> GRAY
        // 'c': pos 2 in guess, exists in answer at pos 0 -> YELLOW
        val answer = "cơm"
        val guess = "mực"
        val tiles = GuessEvaluator.evaluate(answer, guess)

        assertEquals(TileColor.YELLOW, tiles[0].color)
        assertEquals(TileColor.GRAY, tiles[1].color)
        assertEquals(TileColor.YELLOW, tiles[2].color)
    }

    @Test
    fun testGuessEvaluatorDuplicateHandling() {
        // Answer: "hoa", Guess: "aaa"
        // Exact match at pos 2 ('a' == 'a') -> GREEN
        // Pos 0 and 1 -> GRAY because answer only has 1 'a'
        val answer = "hoa"
        val guess = "aaa"
        val tiles = GuessEvaluator.evaluate(answer, guess)

        assertEquals(TileColor.GRAY, tiles[0].color)
        assertEquals(TileColor.GRAY, tiles[1].color)
        assertEquals(TileColor.GREEN, tiles[2].color)
    }

    @Test
    fun testGameConstraintsAndCandidateFiltering() {
        val constraints = GameConstraints(targetLength = 3)
        // Add guess: "nằm" evaluated against answer "năm" -> [GREEN, BLUE, GREEN]
        val answer = "năm"
        val guess = "nằm"
        val tiles = GuessEvaluator.evaluate(answer, guess)
        constraints.addGuess(guess, tiles)

        // Candidate "năm" should match!
        assertTrue(constraints.matches("năm"))

        // Candidate "nằm" shouldn't match because tone must differ from huyền
        assertFalse(constraints.matches("nằm"))

        // Candidate "nắm" has base vowel 'ă' with tone sắc (different from huyền) -> matches constraint!
        assertTrue(constraints.matches("nắm"))

        // Candidate "cơm" should fail (lacks 'n', 'm', 'ă')
        assertFalse(constraints.matches("cơm"))
        assertNotNull(constraints.getEliminationReason("cơm"))
    }

    @Test
    fun testEntropyCalculator() {
        val candidatePool = listOf("xanh", "vàng", "tươi", "lành", "phúc", "sáng")
        val entropyResult = EntropyCalculator.calculateEntropy("xanh", candidatePool)

        // Entropy should be non-negative
        assertTrue(entropyResult.entropy >= 0.0)
        assertTrue(entropyResult.patternCount >= 1)
        assertTrue(entropyResult.expectedRemainingCandidates <= candidatePool.size.toDouble())
    }

    @Test
    fun testProbabilityEngineRelativePercentages() {
        val candidates = listOf("sông", "biển", "xanh", "ngày")
        val stats = LetterFrequencyAnalyzer.analyze(candidates, 4)
        val ranked = ProbabilityEngine.scoreCandidates(candidates, stats)

        assertEquals(candidates.size, ranked.size)
        val totalProb = ranked.sumOf { it.relativeProbability }
        // Probabilities should sum to approximately 100%
        assertEquals(100.0, totalProb, 0.5)
    }
}
