package com.example

import com.example.game.TileColor
import com.example.solver.InteractiveGuessRow
import com.example.solver.InteractiveSolverConclusion
import com.example.solver.InteractiveSolverEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractiveSolverEngineTest {

    @Test
    fun testInteractiveSolverExactMatch() = runBlocking {
        val dictionary = listOf("ngày", "ngọc", "ngồi", "hoa", "sông", "bình")
        val rows = listOf(
            InteractiveGuessRow(
                word = "ngày",
                colors = listOf(TileColor.GREEN, TileColor.GREEN, TileColor.GREEN, TileColor.GREEN)
            )
        )

        val result = InteractiveSolverEngine.analyze(
            dictionaryWords = dictionary,
            wordLength = 4,
            rows = rows
        )

        assertTrue(result is InteractiveSolverConclusion.ExactMatchFound)
        val match = result as InteractiveSolverConclusion.ExactMatchFound
        assertEquals("ngày", match.exactWord)
    }

    @Test
    fun testInteractiveSolverSuggestionsWhenMultipleRemain() = runBlocking {
        val dictionary = listOf("sáng", "nắng", "vàng", "làng")
        // Target length 4. Guessed "sáng": 's' GRAY, 'á' BLUE, 'n' GREEN, 'g' GREEN
        // 's' is excluded completely.
        // 'n' is known at position 2.
        // 'g' is known at position 3.
        // 'á' is BLUE (base vowel A, forbidden tone ACUTE).
        // Matches among dictionary: "vàng" (v - à - n - g), "làng" (l - à - n - g).
        // "sáng" is excluded (due to 's' GRAY and 'á' BLUE).
        // "nắng" has 'ắ' (A_BREVE) not A base vowel.
        val rows = listOf(
            InteractiveGuessRow(
                word = "sáng",
                colors = listOf(TileColor.GRAY, TileColor.BLUE, TileColor.GREEN, TileColor.GREEN)
            )
        )

        val result = InteractiveSolverEngine.analyze(
            dictionaryWords = dictionary,
            wordLength = 4,
            rows = rows
        )

        assertTrue(result is InteractiveSolverConclusion.SuggestionsAvailable)
        val suggestions = result as InteractiveSolverConclusion.SuggestionsAvailable
        assertEquals(2, suggestions.remainingCount)
    }

    @Test
    fun testInteractiveSolverConflict() = runBlocking {
        val dictionary = listOf("ngày", "ngọc")
        // Guessed "ngày" but marked all GRAY (impossible for "ngày" if it was in dict)
        val rows = listOf(
            InteractiveGuessRow(
                word = "ngày",
                colors = listOf(TileColor.GRAY, TileColor.GRAY, TileColor.GRAY, TileColor.GRAY)
            ),
            InteractiveGuessRow(
                word = "ngọc",
                colors = listOf(TileColor.GRAY, TileColor.GRAY, TileColor.GRAY, TileColor.GRAY)
            )
        )

        val result = InteractiveSolverEngine.analyze(
            dictionaryWords = dictionary,
            wordLength = 4,
            rows = rows
        )

        assertTrue(result is InteractiveSolverConclusion.ConflictOrNoMatch)
    }

    @Test
    fun testNextColorCycle() {
        assertEquals(TileColor.GREEN, InteractiveSolverEngine.getNextColor(TileColor.GRAY))
        assertEquals(TileColor.YELLOW, InteractiveSolverEngine.getNextColor(TileColor.GREEN))
        assertEquals(TileColor.BLUE, InteractiveSolverEngine.getNextColor(TileColor.YELLOW))
        assertEquals(TileColor.GRAY, InteractiveSolverEngine.getNextColor(TileColor.BLUE))
    }
}
