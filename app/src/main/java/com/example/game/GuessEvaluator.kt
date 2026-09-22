package com.example.game

import com.example.normalization.VietnameseNormalizer

object GuessEvaluator {

    /**
     * Evaluates a guess against an answer according to the Vietnamese Wordle color rules:
     * 🟩 GREEN: Exact character match (same base + same tone) at the exact position.
     * 🟨 YELLOW: Exact character exists elsewhere in the answer (and not already matched).
     * 🟦 BLUE: Same base vowel exists in the answer but with a different tone.
     * ⬛ GRAY: Letter does not exist in the answer or all occurrences have already been matched.
     *
     * Handles duplicate letter counts deterministically (priority: GREEN > YELLOW > BLUE > GRAY).
     */
    fun evaluate(answerRaw: String, guessRaw: String): List<ResultTile> {
        val answer = VietnameseNormalizer.normalize(answerRaw)
        val guess = VietnameseNormalizer.normalize(guessRaw)
        val length = guess.length
        require(length == answer.length) { "Độ dài từ đoán (${guess.length}) phải khớp với đáp án (${answer.length})" }

        val result = arrayOfNulls<ResultTile>(length)
        val answerMatched = BooleanArray(length) { false }
        val guessMatched = BooleanArray(length) { false }

        // Step 1: Exact matches (GREEN)
        for (i in 0 until length) {
            val gChar = guess[i]
            val aChar = answer[i]
            if (gChar == aChar) {
                result[i] = ResultTile(
                    position = i,
                    letter = gChar,
                    color = TileColor.GREEN,
                    explanation = "Chữ '$gChar' đúng ký tự, đúng dấu và đúng vị trí $i"
                )
                answerMatched[i] = true
                guessMatched[i] = true
            }
        }

        // Step 2: Yellow matches (exact character, different position)
        for (i in 0 until length) {
            if (guessMatched[i]) continue
            val gChar = guess[i]

            // Find an unmatched exact character in answer at different position
            var matchedAnswerIndex = -1
            for (j in 0 until length) {
                if (!answerMatched[j] && answer[j] == gChar) {
                    matchedAnswerIndex = j
                    break
                }
            }

            if (matchedAnswerIndex != -1) {
                result[i] = ResultTile(
                    position = i,
                    letter = gChar,
                    color = TileColor.YELLOW,
                    explanation = "Chữ '$gChar' có trong từ nhưng sai vị trí"
                )
                answerMatched[matchedAnswerIndex] = true
                guessMatched[i] = true
            }
        }

        // Step 3: Blue matches (same base vowel, different tone)
        for (i in 0 until length) {
            if (guessMatched[i]) continue
            val gChar = guess[i]
            val gDecomp = VietnameseNormalizer.decompose(gChar)

            if (gDecomp.isVowel) {
                var matchedAnswerIndex = -1
                for (j in 0 until length) {
                    if (!answerMatched[j]) {
                        val aDecomp = VietnameseNormalizer.decompose(answer[j])
                        if (aDecomp.isVowel && aDecomp.vowelBase == gDecomp.vowelBase && aDecomp.tone != gDecomp.tone) {
                            matchedAnswerIndex = j
                            break
                        }
                    }
                }

                if (matchedAnswerIndex != -1) {
                    val answerChar = answer[matchedAnswerIndex]
                    val answerTone = VietnameseNormalizer.decompose(answerChar).tone
                    result[i] = ResultTile(
                        position = i,
                        letter = gChar,
                        color = TileColor.BLUE,
                        explanation = "Nguyên âm '${gDecomp.vowelBase?.baseChar}' đúng loại nhưng sai dấu (đoán: ${gDecomp.tone.vietnameseName}, từ có: ${answerTone.vietnameseName})"
                    )
                    answerMatched[matchedAnswerIndex] = true
                    guessMatched[i] = true
                }
            }
        }

        // Step 4: All remaining unmatched letters are GRAY
        for (i in 0 until length) {
            if (result[i] == null) {
                val gChar = guess[i]
                result[i] = ResultTile(
                    position = i,
                    letter = gChar,
                    color = TileColor.GRAY,
                    explanation = "Chữ '$gChar' không tồn tại trong từ (hoặc đã đủ số lượng xuất hiện)"
                )
            }
        }

        return result.map { it!! }
    }

    /**
     * Converts a result pattern into a compact string representation for pattern matching and entropy.
     * e.g. "G-Y-B-X"
     */
    fun patternToString(tiles: List<ResultTile>): String {
        return tiles.joinToString("") { tile ->
            when (tile.color) {
                TileColor.GREEN -> "G"
                TileColor.YELLOW -> "Y"
                TileColor.BLUE -> "B"
                TileColor.GRAY -> "X"
            }
        }
    }
}
