package com.example.dictionary

import com.example.normalization.VietnameseNormalizer

data class ValidationResult(
    val isValid: Boolean,
    val normalizedWord: String = "",
    val errorMessage: String? = null
)

object DictionaryValidator {

    private val HTML_REGEX = Regex("<[^>]*>")
    private val URL_REGEX = Regex("https?://\\S+|www\\.\\S+")
    private val DIGIT_REGEX = Regex(".*\\d.*")
    private val CONTROL_CHAR_REGEX = Regex("[\\p{Cntrl}]")
    private val EMOJI_OR_SYMBOL_REGEX = Regex("[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}]")

    /**
     * Validates a raw word candidate.
     * Rejects:
     * - empty / whitespace
     * - length < 2 or > 20
     * - HTML tags
     * - URLs
     * - digits / numbers
     * - emojis & special symbols
     * - control characters
     * - characters not part of Vietnamese orthography
     */
    fun validate(rawWord: String): ValidationResult {
        val trimmed = rawWord.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, errorMessage = "Từ rỗng hoặc chỉ chứa khoảng trắng")
        }

        if (trimmed.length < 2) {
            return ValidationResult(false, errorMessage = "Từ quá ngắn (tối thiểu 2 ký tự)")
        }

        if (trimmed.length > 20) {
            return ValidationResult(false, errorMessage = "Từ quá dài (tối đa 20 ký tự)")
        }

        if (HTML_REGEX.containsMatchIn(trimmed)) {
            return ValidationResult(false, errorMessage = "Chứa mã HTML không hợp lệ")
        }

        if (URL_REGEX.containsMatchIn(trimmed)) {
            return ValidationResult(false, errorMessage = "Chứa liên kết URL không hợp lệ")
        }

        if (DIGIT_REGEX.matches(trimmed)) {
            return ValidationResult(false, errorMessage = "Chứa ký tự số")
        }

        if (CONTROL_CHAR_REGEX.containsMatchIn(trimmed)) {
            return ValidationResult(false, errorMessage = "Chứa ký tự điều khiển")
        }

        if (EMOJI_OR_SYMBOL_REGEX.containsMatchIn(trimmed)) {
            return ValidationResult(false, errorMessage = "Chứa biểu tượng cảm xúc hoặc ký tự đặc biệt")
        }

        val normalized = VietnameseNormalizer.normalize(trimmed)
        for (c in normalized) {
            if (!VietnameseNormalizer.isValidVietnameseChar(c)) {
                return ValidationResult(
                    false,
                    errorMessage = "Chứa ký tự '$c' không thuộc bảng chữ cái tiếng Việt"
                )
            }
        }

        return ValidationResult(true, normalizedWord = normalized)
    }
}
