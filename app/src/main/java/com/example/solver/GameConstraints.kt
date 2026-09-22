package com.example.solver

import com.example.game.ResultTile
import com.example.game.TileColor
import com.example.normalization.Tone
import com.example.normalization.VietnameseNormalizer
import com.example.normalization.VowelIdentity
import kotlin.math.max

data class BlueVowelConstraint(
    val vowelBase: VowelIdentity,
    val forbiddenTone: Tone,
    val position: Int
)

class GameConstraints(val targetLength: Int) {
    val knownPositions = mutableMapOf<Int, Char>()
    val excludedPositions = mutableMapOf<Int, MutableSet<Char>>()
    val minCharOccurrences = mutableMapOf<Char, Int>()
    val maxCharOccurrences = mutableMapOf<Char, Int>()
    val excludedCharsCompletely = mutableSetOf<Char>()
    val blueVowelConstraints = mutableListOf<BlueVowelConstraint>()

    // Vowel identity minimum and maximum occurrences (to properly handle duplicate vowels with Blue/Gray)
    val minVowelIdentityOccurrences = mutableMapOf<VowelIdentity, Int>()
    val maxVowelIdentityOccurrences = mutableMapOf<VowelIdentity, Int>()

    fun addGuess(guessRaw: String, tiles: List<ResultTile>) {
        val guess = VietnameseNormalizer.normalize(guessRaw)
        require(guess.length == targetLength) { "Độ dài từ không khớp: ${guess.length} != $targetLength" }

        // Count positive (GREEN or YELLOW) occurrences per character in this guess
        val exactCounts = mutableMapOf<Char, Int>()
        val isCharGray = mutableMapOf<Char, Boolean>()

        // Count vowel identity occurrences (GREEN or YELLOW or BLUE)
        val vowelIdentityMatchedCounts = mutableMapOf<VowelIdentity, Int>()
        val vowelIdentityHasGray = mutableMapOf<VowelIdentity, Boolean>()

        for (i in 0 until targetLength) {
            val tile = tiles[i]
            val c = guess[i]
            val decomp = VietnameseNormalizer.decompose(c)

            when (tile.color) {
                TileColor.GREEN, TileColor.YELLOW -> {
                    exactCounts[c] = (exactCounts[c] ?: 0) + 1
                    if (decomp.isVowel && decomp.vowelBase != null) {
                        vowelIdentityMatchedCounts[decomp.vowelBase] = (vowelIdentityMatchedCounts[decomp.vowelBase] ?: 0) + 1
                    }
                }
                TileColor.BLUE -> {
                    // Blue indicates vowel identity matched in target, but with different tone
                    if (decomp.isVowel && decomp.vowelBase != null) {
                        vowelIdentityMatchedCounts[decomp.vowelBase] = (vowelIdentityMatchedCounts[decomp.vowelBase] ?: 0) + 1
                    }
                }
                TileColor.GRAY -> {
                    isCharGray[c] = true
                    if (decomp.isVowel && decomp.vowelBase != null) {
                        vowelIdentityHasGray[decomp.vowelBase] = true
                    }
                }
            }
        }

        // Handle occurrence counts & complete exclusions for exact characters
        for (i in 0 until targetLength) {
            val c = guess[i]
            val posCount = exactCounts[c] ?: 0
            val isGray = isCharGray[c] == true
            val tile = tiles[i]

            if (isGray) {
                if (posCount == 0 && tile.color != TileColor.BLUE) {
                    excludedCharsCompletely.add(c)
                } else {
                    minCharOccurrences[c] = max(minCharOccurrences[c] ?: 0, posCount)
                    maxCharOccurrences[c] = posCount
                }
            } else if (posCount > 0) {
                minCharOccurrences[c] = max(minCharOccurrences[c] ?: 0, posCount)
            }
        }

        // Handle occurrence counts for VowelIdentity
        for (i in 0 until targetLength) {
            val c = guess[i]
            val decomp = VietnameseNormalizer.decompose(c)
            if (decomp.isVowel && decomp.vowelBase != null) {
                val vId = decomp.vowelBase
                val matchedCount = vowelIdentityMatchedCounts[vId] ?: 0
                val hasGray = vowelIdentityHasGray[vId] == true

                if (hasGray) {
                    minVowelIdentityOccurrences[vId] = max(minVowelIdentityOccurrences[vId] ?: 0, matchedCount)
                    maxVowelIdentityOccurrences[vId] = matchedCount
                } else if (matchedCount > 0) {
                    minVowelIdentityOccurrences[vId] = max(minVowelIdentityOccurrences[vId] ?: 0, matchedCount)
                }
            }
        }

        // Apply positional constraints
        for (i in 0 until targetLength) {
            val tile = tiles[i]
            val c = guess[i]
            when (tile.color) {
                TileColor.GREEN -> {
                    knownPositions[i] = c
                }
                TileColor.YELLOW -> {
                    excludedPositions.getOrPut(i) { mutableSetOf() }.add(c)
                }
                TileColor.BLUE -> {
                    excludedPositions.getOrPut(i) { mutableSetOf() }.add(c)
                    val decomp = VietnameseNormalizer.decompose(c)
                    if (decomp.isVowel && decomp.vowelBase != null) {
                        blueVowelConstraints.add(BlueVowelConstraint(decomp.vowelBase, decomp.tone, i))
                    }
                }
                TileColor.GRAY -> {
                    excludedPositions.getOrPut(i) { mutableSetOf() }.add(c)
                }
            }
        }
    }

    /**
     * Checks if a candidate word satisfies all current constraints.
     */
    fun matches(candidateRaw: String): Boolean {
        val candidate = VietnameseNormalizer.normalize(candidateRaw)
        if (candidate.length != targetLength) return false

        // 1. Check known positions (GREEN)
        for ((pos, char) in knownPositions) {
            if (candidate[pos] != char) return false
        }

        // 2. Check excluded positions
        for ((pos, chars) in excludedPositions) {
            if (candidate[pos] in chars) return false
        }

        // 3. Check completely excluded characters
        for (c in excludedCharsCompletely) {
            if (candidate.contains(c)) return false
        }

        // 4. Check min occurrences of exact character
        for ((char, minCount) in minCharOccurrences) {
            val count = candidate.count { it == char }
            if (count < minCount) return false
        }

        // 5. Check max occurrences of exact character
        for ((char, maxCount) in maxCharOccurrences) {
            val count = candidate.count { it == char }
            if (count > maxCount) return false
        }

        // 6. Check min occurrences of VowelIdentity
        for ((vId, minCount) in minVowelIdentityOccurrences) {
            val count = candidate.count { c ->
                val d = VietnameseNormalizer.decompose(c)
                d.isVowel && d.vowelBase == vId
            }
            if (count < minCount) return false
        }

        // 7. Check max occurrences of VowelIdentity
        for ((vId, maxCount) in maxVowelIdentityOccurrences) {
            val count = candidate.count { c ->
                val d = VietnameseNormalizer.decompose(c)
                d.isVowel && d.vowelBase == vId
            }
            if (count > maxCount) return false
        }

        // 8. Check blue vowel constraints (must contain base vowel with different tone)
        for (blue in blueVowelConstraints) {
            val hasMatchingVowel = candidate.any { c ->
                val d = VietnameseNormalizer.decompose(c)
                d.isVowel && d.vowelBase == blue.vowelBase && d.tone != blue.forbiddenTone
            }
            if (!hasMatchingVowel) return false
        }

        return true
    }

    /**
     * Returns a human-readable explanation of why a candidate was eliminated, or null if it matches.
     */
    fun getEliminationReason(candidateRaw: String): String? {
        val candidate = VietnameseNormalizer.normalize(candidateRaw)
        if (candidate.length != targetLength) {
            return "Độ dài ${candidate.length} không khớp với độ dài yêu cầu $targetLength"
        }

        for ((pos, char) in knownPositions) {
            if (candidate[pos] != char) {
                return "LOẠI: Vị trí ${pos + 1} yêu cầu ký tự '$char', từ có '${candidate[pos]}'"
            }
        }

        for ((pos, chars) in excludedPositions) {
            if (candidate[pos] in chars) {
                return "LOẠI: Ký tự '${candidate[pos]}' không thể nằm ở vị trí ${pos + 1}"
            }
        }

        for (c in excludedCharsCompletely) {
            if (candidate.contains(c)) {
                return "LOẠI: Chứa ký tự '$c' đã bị loại hoàn toàn"
            }
        }

        for ((char, minCount) in minCharOccurrences) {
            val count = candidate.count { it == char }
            if (count < minCount) {
                return "LOẠI: Thiếu ký tự '$char' (yêu cầu ít nhất $minCount, từ có $count)"
            }
        }

        for ((char, maxCount) in maxCharOccurrences) {
            val count = candidate.count { it == char }
            if (count > maxCount) {
                return "LOẠI: Thừa ký tự '$char' (tối đa $maxCount, từ có $count)"
            }
        }

        for ((vId, minCount) in minVowelIdentityOccurrences) {
            val count = candidate.count { c ->
                val d = VietnameseNormalizer.decompose(c)
                d.isVowel && d.vowelBase == vId
            }
            if (count < minCount) {
                return "LOẠI: Thiếu nguyên âm '${vId.displayName}' (yêu cầu ít nhất $minCount, từ có $count)"
            }
        }

        for ((vId, maxCount) in maxVowelIdentityOccurrences) {
            val count = candidate.count { c ->
                val d = VietnameseNormalizer.decompose(c)
                d.isVowel && d.vowelBase == vId
            }
            if (count > maxCount) {
                return "LOẠI: Thừa nguyên âm '${vId.displayName}' (tối đa $maxCount, từ có $count)"
            }
        }

        for (blue in blueVowelConstraints) {
            val hasMatchingVowel = candidate.any { c ->
                val d = VietnameseNormalizer.decompose(c)
                d.isVowel && d.vowelBase == blue.vowelBase && d.tone != blue.forbiddenTone
            }
            if (!hasMatchingVowel) {
                return "LOẠI: Phải chứa nguyên âm '${blue.vowelBase.displayName}' với dấu thanh khác dấu '${blue.forbiddenTone.vietnameseName}'"
            }
        }

        return null
    }
}
