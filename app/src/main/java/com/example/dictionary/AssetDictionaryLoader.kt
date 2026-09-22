package com.example.dictionary

import android.content.Context
import com.example.database.DictionaryWord
import com.example.normalization.VietnameseNormalizer
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

object AssetDictionaryLoader {

    /**
     * Loads the bundled dictionary from assets/dictionary/vi_words.json.
     */
    fun loadBundledWords(context: Context): List<DictionaryWord> {
        val words = mutableListOf<DictionaryWord>()
        try {
            val inputStream = context.assets.open("dictionary/vi_words.json")
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val jsonString = reader.readText()
            reader.close()

            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val word = obj.getString("word")
                val type = obj.optString("type", "general")
                val freq = obj.optInt("frequency", 50)

                val validation = DictionaryValidator.validate(word)
                if (validation.isValid) {
                    val normalized = validation.normalizedWord
                    words.add(
                        DictionaryWord(
                            text = word.trim(),
                            normalizedText = normalized,
                            length = normalized.length,
                            wordType = type,
                            frequency = freq,
                            source = "bundled",
                            userAdded = false,
                            enabled = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return words
    }
}
