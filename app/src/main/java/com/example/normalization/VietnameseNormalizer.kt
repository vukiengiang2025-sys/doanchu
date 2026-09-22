package com.example.normalization

import java.text.Normalizer
import java.util.Locale

object VietnameseNormalizer {

    private val VOWEL_MAP = mapOf(
        // A
        'a' to Pair(VowelBase.A, Tone.NONE),
        'á' to Pair(VowelBase.A, Tone.ACUTE),
        'à' to Pair(VowelBase.A, Tone.GRAVE),
        'ả' to Pair(VowelBase.A, Tone.HOOK_ABOVE),
        'ã' to Pair(VowelBase.A, Tone.TILDE),
        'ạ' to Pair(VowelBase.A, Tone.DOT_BELOW),
        // Ă
        'ă' to Pair(VowelBase.A_BREVE, Tone.NONE),
        'ắ' to Pair(VowelBase.A_BREVE, Tone.ACUTE),
        'ằ' to Pair(VowelBase.A_BREVE, Tone.GRAVE),
        'ẳ' to Pair(VowelBase.A_BREVE, Tone.HOOK_ABOVE),
        'ẵ' to Pair(VowelBase.A_BREVE, Tone.TILDE),
        'ặ' to Pair(VowelBase.A_BREVE, Tone.DOT_BELOW),
        // Â
        'â' to Pair(VowelBase.A_CIRCUMFLEX, Tone.NONE),
        'ấ' to Pair(VowelBase.A_CIRCUMFLEX, Tone.ACUTE),
        'ầ' to Pair(VowelBase.A_CIRCUMFLEX, Tone.GRAVE),
        'ẩ' to Pair(VowelBase.A_CIRCUMFLEX, Tone.HOOK_ABOVE),
        'ẫ' to Pair(VowelBase.A_CIRCUMFLEX, Tone.TILDE),
        'ậ' to Pair(VowelBase.A_CIRCUMFLEX, Tone.DOT_BELOW),
        // E
        'e' to Pair(VowelBase.E, Tone.NONE),
        'é' to Pair(VowelBase.E, Tone.ACUTE),
        'è' to Pair(VowelBase.E, Tone.GRAVE),
        'ẻ' to Pair(VowelBase.E, Tone.HOOK_ABOVE),
        'ẽ' to Pair(VowelBase.E, Tone.TILDE),
        'ẹ' to Pair(VowelBase.E, Tone.DOT_BELOW),
        // Ê
        'ê' to Pair(VowelBase.E_CIRCUMFLEX, Tone.NONE),
        'ế' to Pair(VowelBase.E_CIRCUMFLEX, Tone.ACUTE),
        'ề' to Pair(VowelBase.E_CIRCUMFLEX, Tone.GRAVE),
        'ể' to Pair(VowelBase.E_CIRCUMFLEX, Tone.HOOK_ABOVE),
        'ễ' to Pair(VowelBase.E_CIRCUMFLEX, Tone.TILDE),
        'ệ' to Pair(VowelBase.E_CIRCUMFLEX, Tone.DOT_BELOW),
        // I
        'i' to Pair(VowelBase.I, Tone.NONE),
        'í' to Pair(VowelBase.I, Tone.ACUTE),
        'ì' to Pair(VowelBase.I, Tone.GRAVE),
        'ỉ' to Pair(VowelBase.I, Tone.HOOK_ABOVE),
        'ĩ' to Pair(VowelBase.I, Tone.TILDE),
        'ị' to Pair(VowelBase.I, Tone.DOT_BELOW),
        // O
        'o' to Pair(VowelBase.O, Tone.NONE),
        'ó' to Pair(VowelBase.O, Tone.ACUTE),
        'ò' to Pair(VowelBase.O, Tone.GRAVE),
        'ỏ' to Pair(VowelBase.O, Tone.HOOK_ABOVE),
        'õ' to Pair(VowelBase.O, Tone.TILDE),
        'ọ' to Pair(VowelBase.O, Tone.DOT_BELOW),
        // Ô
        'ô' to Pair(VowelBase.O_CIRCUMFLEX, Tone.NONE),
        'ố' to Pair(VowelBase.O_CIRCUMFLEX, Tone.ACUTE),
        'ồ' to Pair(VowelBase.O_CIRCUMFLEX, Tone.GRAVE),
        'ổ' to Pair(VowelBase.O_CIRCUMFLEX, Tone.HOOK_ABOVE),
        'ỗ' to Pair(VowelBase.O_CIRCUMFLEX, Tone.TILDE),
        'ộ' to Pair(VowelBase.O_CIRCUMFLEX, Tone.DOT_BELOW),
        // Ơ
        'ơ' to Pair(VowelBase.O_HORN, Tone.NONE),
        'ớ' to Pair(VowelBase.O_HORN, Tone.ACUTE),
        'ờ' to Pair(VowelBase.O_HORN, Tone.GRAVE),
        'ở' to Pair(VowelBase.O_HORN, Tone.HOOK_ABOVE),
        'ỡ' to Pair(VowelBase.O_HORN, Tone.TILDE),
        'ợ' to Pair(VowelBase.O_HORN, Tone.DOT_BELOW),
        // U
        'u' to Pair(VowelBase.U, Tone.NONE),
        'ú' to Pair(VowelBase.U, Tone.ACUTE),
        'ù' to Pair(VowelBase.U, Tone.GRAVE),
        'ủ' to Pair(VowelBase.U, Tone.HOOK_ABOVE),
        'ũ' to Pair(VowelBase.U, Tone.TILDE),
        'ụ' to Pair(VowelBase.U, Tone.DOT_BELOW),
        // Ư
        'ư' to Pair(VowelBase.U_HORN, Tone.NONE),
        'ứ' to Pair(VowelBase.U_HORN, Tone.ACUTE),
        'ừ' to Pair(VowelBase.U_HORN, Tone.GRAVE),
        'ử' to Pair(VowelBase.U_HORN, Tone.HOOK_ABOVE),
        'ữ' to Pair(VowelBase.U_HORN, Tone.TILDE),
        'ự' to Pair(VowelBase.U_HORN, Tone.DOT_BELOW),
        // Y
        'y' to Pair(VowelBase.Y, Tone.NONE),
        'ý' to Pair(VowelBase.Y, Tone.ACUTE),
        'ỳ' to Pair(VowelBase.Y, Tone.GRAVE),
        'ỷ' to Pair(VowelBase.Y, Tone.HOOK_ABOVE),
        'ỹ' to Pair(VowelBase.Y, Tone.TILDE),
        'ỵ' to Pair(VowelBase.Y, Tone.DOT_BELOW)
    )

    private val VALID_CONSONANTS = setOf(
        'b', 'c', 'd', 'đ', 'g', 'h', 'k', 'l', 'm', 'n',
        'p', 'q', 'r', 's', 't', 'v', 'x'
    )

    private val REVERSE_VOWEL_MAP = VOWEL_MAP.entries.associate { (char, pair) -> pair to char }

    /**
     * Composes a character from a VowelBase and Tone.
     */
    fun composeVowel(vowelBase: VowelBase, tone: Tone): Char {
        return REVERSE_VOWEL_MAP[Pair(vowelBase, tone)] ?: vowelBase.baseChar
    }

    /**
     * Applies a tone to the last vowel in a word. If the last vowel already has this tone, removes tone.
     */
    fun applyToneToWord(word: String, tone: Tone): String {
        if (word.isEmpty()) return word
        val chars = word.toCharArray()
        // Find last vowel index
        for (i in chars.indices.reversed()) {
            val d = decompose(chars[i])
            if (d.isVowel && d.vowelBase != null) {
                val newTone = if (d.tone == tone) Tone.NONE else tone
                chars[i] = composeVowel(d.vowelBase, newTone)
                return String(chars)
            }
        }
        return word
    }

    /**
     * Attempts Telex expansion on the last typed character.
     * Returns transformed string if a telex rule matched, or null if no rule matched.
     */
    fun tryTelexTransform(currentWord: String, newKey: Char): String? {
        if (currentWord.isEmpty()) return null
        val lowerKey = newKey.lowercaseChar()
        val lastChar = currentWord.last()
        val prefix = currentWord.dropLast(1)
        val d = decompose(lastChar)

        // 1. Double letter transformations: aa -> â, ee -> ê, oo -> ô, dd -> đ
        when {
            lastChar == 'a' && lowerKey == 'a' -> return prefix + "â"
            lastChar == 'a' && lowerKey == 'w' -> return prefix + "ă"
            lastChar == 'e' && lowerKey == 'e' -> return prefix + "ê"
            lastChar == 'o' && lowerKey == 'o' -> return prefix + "ô"
            lastChar == 'o' && lowerKey == 'w' -> return prefix + "ơ"
            lastChar == 'u' && lowerKey == 'w' -> return prefix + "ư"
            lastChar == 'd' && lowerKey == 'd' -> return prefix + "đ"
        }

        // 2. Telex tone keys: s, f, r, x, j applied to existing vowel
        if (d.isVowel && d.vowelBase != null) {
            val tone = when (lowerKey) {
                's' -> Tone.ACUTE
                'f' -> Tone.GRAVE
                'r' -> Tone.HOOK_ABOVE
                'x' -> Tone.TILDE
                'j' -> Tone.DOT_BELOW
                else -> null
            }
            if (tone != null) {
                val newChar = composeVowel(d.vowelBase, tone)
                return prefix + newChar
            }
        }

        return null
    }

    /**
     * Standardizes string into NFC Unicode, lowercase, and trims leading/trailing spaces.
     */
    fun normalize(text: String): String {
        return Normalizer.normalize(text.trim().lowercase(Locale.forLanguageTag("vi")), Normalizer.Form.NFC)
    }

    /**
     * Decomposes a character into vowelBase, tone, and consonant/base info.
     */
    fun decompose(c: Char): VietnameseChar {
        val lower = c.lowercaseChar()
        val mapped = VOWEL_MAP[lower]
        return if (mapped != null) {
            VietnameseChar(
                originalChar = lower,
                isVowel = true,
                vowelBase = mapped.first,
                tone = mapped.second,
                baseChar = mapped.first.baseChar
            )
        } else {
            VietnameseChar(
                originalChar = lower,
                isVowel = false,
                vowelBase = null,
                tone = Tone.NONE,
                baseChar = lower
            )
        }
    }

    /**
     * Parses a character into VietnameseCharacterModel with exact distinction:
     * - D != Đ (c == 'đ' has isConsonant = true, baseChar = 'đ', isVietnameseSpecific = true)
     * - VowelIdentity (A, Ă, Â, E, Ê, I, O, Ô, Ơ, U, Ư, Y)
     * - Tone (NONE, ACUTE, GRAVE, HOOK_ABOVE, TILDE, DOT_BELOW)
     */
    fun parseCharacterModel(c: Char): VietnameseCharacterModel {
        val lower = c.lowercaseChar()
        val mapped = VOWEL_MAP[lower]
        return if (mapped != null) {
            val (vowelId, tone) = mapped
            val isSpecific = vowelId != VowelIdentity.A && vowelId != VowelIdentity.E &&
                vowelId != VowelIdentity.I && vowelId != VowelIdentity.O &&
                vowelId != VowelIdentity.U && vowelId != VowelIdentity.Y ||
                tone != Tone.NONE
            VietnameseCharacterModel(
                character = lower,
                vowelIdentity = vowelId,
                tone = tone,
                isVowel = true,
                isConsonant = false,
                isVietnameseSpecific = isSpecific,
                baseChar = vowelId.baseChar
            )
        } else {
            val isConsonant = VALID_CONSONANTS.contains(lower)
            VietnameseCharacterModel(
                character = lower,
                vowelIdentity = null,
                tone = Tone.NONE,
                isVowel = false,
                isConsonant = isConsonant,
                isVietnameseSpecific = (lower == 'đ'),
                baseChar = lower // 'đ' stays 'đ', 'd' stays 'd'
            )
        }
    }

    /**
     * Checks if c1 and c2 have the same vowel base (e.g. both A or both Ă or both Â)
     * but have different tones (e.g. à vs á, or a vs à).
     */
    fun isSameBaseVowelDifferentTone(c1: Char, c2: Char): Boolean {
        val d1 = decompose(c1)
        val d2 = decompose(c2)
        return d1.isVowel && d2.isVowel && d1.vowelBase == d2.vowelBase && d1.tone != d2.tone
    }

    /**
     * Returns true if character is valid in Vietnamese orthography.
     */
    fun isValidVietnameseChar(c: Char): Boolean {
        val lower = c.lowercaseChar()
        return VOWEL_MAP.containsKey(lower) || VALID_CONSONANTS.contains(lower) || lower == ' '
    }

    /**
     * Validates if a word contains only valid Vietnamese letters (no numbers, punctuation, control chars).
     */
    fun isValidWord(word: String): Boolean {
        val normalized = normalize(word)
        if (normalized.isEmpty() || normalized.length > 20) return false
        return normalized.all { isValidVietnameseChar(it) }
    }

    /**
     * Strips all tones and converts accented characters to basic Latin (for search/indexing).
     */
    fun removeAccents(text: String): String {
        val normalized = normalize(text)
        val sb = StringBuilder()
        for (c in normalized) {
            val d = decompose(c)
            if (d.isVowel) {
                // Convert vowel base letter to basic latin: ă, â -> a; ê -> e; ô, ơ -> o; ư -> u
                when (d.vowelBase) {
                    VowelBase.A, VowelBase.A_BREVE, VowelBase.A_CIRCUMFLEX -> sb.append('a')
                    VowelBase.E, VowelBase.E_CIRCUMFLEX -> sb.append('e')
                    VowelBase.I -> sb.append('i')
                    VowelBase.O, VowelBase.O_CIRCUMFLEX, VowelBase.O_HORN -> sb.append('o')
                    VowelBase.U, VowelBase.U_HORN -> sb.append('u')
                    VowelBase.Y -> sb.append('y')
                    null -> sb.append(c)
                }
            } else if (c == 'đ') {
                sb.append('d')
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}
