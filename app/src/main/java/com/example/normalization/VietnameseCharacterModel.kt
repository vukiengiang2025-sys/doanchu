package com.example.normalization

/**
 * Exact representation of Vietnamese vowel identities:
 * Exactly: A, Ă, Â, E, Ê, I, O, Ô, Ơ, U, Ư, Y
 *
 * CRITICAL RULE:
 * A != Ă, A != Â, O != Ô, O != Ơ, U != Ư
 */
enum class VowelIdentity(val baseChar: Char, val displayName: String) {
    A('a', "A"),
    A_BREVE('ă', "Ă"),
    A_CIRCUMFLEX('â', "Â"),
    E('e', "E"),
    E_CIRCUMFLEX('ê', "Ê"),
    I('i', "I"),
    O('o', "O"),
    O_CIRCUMFLEX('ô', "Ô"),
    O_HORN('ơ', "Ơ"),
    U('u', "U"),
    U_HORN('ư', "Ư"),
    Y('y', "Y")
}

// Backward compatibility alias so existing usages of VowelBase continue to compile cleanly
typealias VowelBase = VowelIdentity

/**
 * Six distinct Vietnamese tones:
 * NGANG (None), SẮC (Acute), HUYỀN (Grave), HỎI (Hook), NGÃ (Tilde), NẶNG (Dot below)
 */
enum class Tone(val vietnameseName: String) {
    NONE("Ngang"),
    ACUTE("Sắc"),
    GRAVE("Huyền"),
    HOOK_ABOVE("Hỏi"),
    TILDE("Ngã"),
    DOT_BELOW("Nặng")
}

/**
 * Rich model for Vietnamese character analysis.
 * Explicitly preserves:
 * - 'd' vs 'đ' (D is consonant D, Đ is consonant Đ - D != Đ)
 * - VowelIdentity (A, Ă, Â, etc.)
 * - Tone
 * - IsConsonant
 * - IsVietnameseSpecific (như đ, ă, â, ê, ô, ơ, ư hoặc có dấu thanh)
 */
data class VietnameseCharacterModel(
    val character: Char,
    val vowelIdentity: VowelIdentity?,
    val tone: Tone,
    val isVowel: Boolean,
    val isConsonant: Boolean,
    val isVietnameseSpecific: Boolean,
    val baseChar: Char
)

// Backward compatibility data class
data class VietnameseChar(
    val originalChar: Char,
    val isVowel: Boolean,
    val vowelBase: VowelIdentity?,
    val tone: Tone,
    val baseChar: Char
)
