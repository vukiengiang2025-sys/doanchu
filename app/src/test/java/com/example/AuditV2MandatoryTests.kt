package com.example

import com.example.game.GuessEvaluator
import com.example.game.TileColor
import com.example.normalization.Tone
import com.example.normalization.VietnameseNormalizer
import com.example.normalization.VowelIdentity
import com.example.solver.GameConstraints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuditV2MandatoryTests {

    // 1. Unicode & NFC normalization
    @Test
    fun testUnicodeAndNfcNormalization() {
        val nfc = "tiếng"
        val nfd = java.text.Normalizer.normalize(nfc, java.text.Normalizer.Form.NFD)
        assertNotEquals(nfc, nfd) // Raw string representations differ in byte sequence

        // VietnameseNormalizer must guarantee NFC equivalence
        val normNfc = VietnameseNormalizer.normalize(nfc)
        val normNfd = VietnameseNormalizer.normalize(nfd)
        assertEquals(normNfc, normNfd)
        assertEquals(normNfc.length, normNfd.length)
    }

    // 2. D != Đ distinction
    @Test
    fun testDvsDBarDistinction() {
        val modelD = VietnameseNormalizer.parseCharacterModel('d')
        val modelDBar = VietnameseNormalizer.parseCharacterModel('đ')

        assertTrue(modelD.isConsonant)
        assertTrue(modelDBar.isConsonant)
        assertFalse(modelD.isVietnameseSpecific)
        assertTrue(modelDBar.isVietnameseSpecific)

        // Ensure baseChar is distinct: D != Đ
        assertEquals('d', modelD.baseChar)
        assertEquals('đ', modelDBar.baseChar)
        assertNotEquals(modelD.character, modelDBar.character)

        // Evaluate guess with 'd' vs answer with 'đ'
        val tiles = GuessEvaluator.evaluate(answerRaw = "đi", guessRaw = "di")
        // 'd' does not match 'đ' -> must be GRAY, not GREEN, not YELLOW, not BLUE
        assertEquals(TileColor.GRAY, tiles[0].color)
        assertEquals(TileColor.GREEN, tiles[1].color)
    }

    // 3. Distinct Vowel Base: A != Ă != Â, O != Ô != Ơ, U != Ư
    @Test
    fun testVowelIdentityDistinctions() {
        val modelA = VietnameseNormalizer.parseCharacterModel('a')
        val modelABreve = VietnameseNormalizer.parseCharacterModel('ă')
        val modelACircumflex = VietnameseNormalizer.parseCharacterModel('â')

        assertNotEquals(modelA.vowelIdentity, modelABreve.vowelIdentity)
        assertNotEquals(modelA.vowelIdentity, modelACircumflex.vowelIdentity)
        assertNotEquals(modelABreve.vowelIdentity, modelACircumflex.vowelIdentity)

        assertEquals(VowelIdentity.A, modelA.vowelIdentity)
        assertEquals(VowelIdentity.A_BREVE, modelABreve.vowelIdentity)
        assertEquals(VowelIdentity.A_CIRCUMFLEX, modelACircumflex.vowelIdentity)

        // Blue rule test: "ba" vs "bă" -> 'a' vs 'ă' are DIFFERENT vowel bases -> GRAY, NOT BLUE!
        val tiles = GuessEvaluator.evaluate(answerRaw = "bă", guessRaw = "ba")
        assertEquals(TileColor.GREEN, tiles[0].color) // 'b' == 'b'
        assertEquals(TileColor.GRAY, tiles[1].color)  // 'a' vs 'ă' is NOT BLUE
    }

    // 4. Blue rule: Same VowelIdentity AND Different Tone
    @Test
    fun testBlueRuleStrictToneMismatch() {
        // Answer: "bà" (A, GRAVE), Guess: "bá" (A, ACUTE)
        // Same VowelIdentity (A), different Tone -> must be BLUE
        val tiles1 = GuessEvaluator.evaluate(answerRaw = "bà", guessRaw = "bá")
        assertEquals(TileColor.GREEN, tiles1[0].color)
        assertEquals(TileColor.BLUE, tiles1[1].color)

        // Answer: "bà" (A, GRAVE), Guess: "ba" (A, NONE)
        // Same VowelIdentity (A), different Tone -> must be BLUE
        val tiles2 = GuessEvaluator.evaluate(answerRaw = "bà", guessRaw = "ba")
        assertEquals(TileColor.GREEN, tiles2[0].color)
        assertEquals(TileColor.BLUE, tiles2[1].color)

        // Answer: "bà" (A, GRAVE), Guess: "bà" (A, GRAVE)
        // Same Tone -> GREEN, NOT BLUE
        val tiles3 = GuessEvaluator.evaluate(answerRaw = "bà", guessRaw = "bà")
        assertEquals(TileColor.GREEN, tiles3[0].color)
        assertEquals(TileColor.GREEN, tiles3[1].color)
    }

    // 5. Duplicate letters handling & multi-pass evaluation
    @Test
    fun testDuplicateLettersMultiPassResolution() {
        // Answer has ONE 'a': "hoa"
        // Guess has THREE 'a': "aaa"
        // Pos 2: 'a' matches -> GREEN
        // Pos 0: 'a' -> GRAY (quota exceeded)
        // Pos 1: 'a' -> GRAY (quota exceeded)
        val tiles = GuessEvaluator.evaluate(answerRaw = "hoa", guessRaw = "aaa")
        assertEquals(TileColor.GRAY, tiles[0].color)
        assertEquals(TileColor.GRAY, tiles[1].color)
        assertEquals(TileColor.GREEN, tiles[2].color)

        // Answer: "bàn" (one 'a'-family vowel: 'à')
        // Guess: "baba" (two 'a's: pos 1 'a', pos 3 'a')
        // Answer has 1 'à'. Guess pos 1 'a' gets BLUE (claims the 'à').
        // Guess pos 3 'a' must be GRAY (quota already claimed by pos 1).
        val tilesBaba = GuessEvaluator.evaluate(answerRaw = "bàn", guessRaw = "báa")
        // 'b' -> GREEN
        // 'á' -> BLUE (matched against 'à')
        // 'a' -> GRAY (cannot match again)
        assertEquals(TileColor.GREEN, tilesBaba[0].color)
        assertEquals(TileColor.BLUE, tilesBaba[1].color)
        assertEquals(TileColor.GRAY, tilesBaba[2].color)
    }

    // 6. Deterministic solver constraints & Elimination reason verification
    @Test
    fun testEliminationReasonVerification() {
        val constraints = GameConstraints(targetLength = 3)
        // Guess "bá" with BLUE for 'á' against answer "bà"
        val tiles = GuessEvaluator.evaluate(answerRaw = "bàc", guessRaw = "bác")
        constraints.addGuess("bác", tiles)

        // "bàc" matches
        assertTrue(constraints.matches("bàc"))

        // "bác" is eliminated because tone ACUTE is forbidden
        assertFalse(constraints.matches("bác"))
        val reasonBac = constraints.getEliminationReason("bác")
        assertNotNull(reasonBac)
        assertTrue(reasonBac!!.contains("LOẠI"))

        // "bục" is eliminated because missing vowel 'A'
        assertFalse(constraints.matches("bục"))
        val reasonBuc = constraints.getEliminationReason("bục")
        assertNotNull(reasonBuc)
        assertTrue(reasonBuc!!.contains("LOẠI"))
    }
}
