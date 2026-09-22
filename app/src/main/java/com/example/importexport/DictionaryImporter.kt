package com.example.importexport

import android.content.Context
import android.net.Uri
import com.example.database.AppDatabase
import com.example.database.DictionaryWord
import com.example.dictionary.DictionaryRepository
import com.example.dictionary.DictionaryValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class ImportReport(
    val totalLinesRead: Int,
    val validWordsCount: Int,
    val duplicateWordsCount: Int,
    val errorCount: Int,
    val sampleErrors: List<String>
)

object DictionaryImporter {

    /**
     * Imports words from a given Uri (file) in TXT, CSV, or JSON format.
     */
    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        format: String, // "TXT", "CSV", "JSON"
        dictionaryRepository: DictionaryRepository
    ): ImportReport = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.dictionaryDao()

        var totalRead = 0
        var validCount = 0
        var duplicateCount = 0
        var errorCount = 0
        val errors = mutableListOf<String>()

        val existingWords = dao.getAllWordsSync().map { it.normalizedText }.toMutableSet()
        val toInsert = mutableListOf<DictionaryWord>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

            if (format.equals("JSON", ignoreCase = true)) {
                val jsonString = reader.readText()
                try {
                    val array = JSONArray(jsonString)
                    totalRead = array.length()
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i)
                        if (obj != null) {
                            val word = obj.optString("word", "").trim()
                            val type = obj.optString("type", "general")
                            val freq = obj.optInt("frequency", 50)
                            processWordCandidate(word, type, freq, existingWords, toInsert, { validCount++ }, { duplicateCount++ }, { err ->
                                errorCount++
                                if (errors.size < 5) errors.add(err)
                            })
                        }
                    }
                } catch (e: Exception) {
                    errorCount++
                    errors.add("Lỗi cấu trúc JSON: ${e.message}")
                }
            } else {
                // Process line by line for TXT or CSV
                var line: String? = reader.readLine()
                var isFirstLine = true
                while (line != null) {
                    totalRead++
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        if (format.equals("CSV", ignoreCase = true)) {
                            // Check header
                            if (isFirstLine && (trimmed.contains("word") || trimmed.contains("type"))) {
                                isFirstLine = false
                                line = reader.readLine()
                                continue
                            }
                            val parts = trimmed.split(",")
                            val word = parts.getOrNull(0)?.trim() ?: ""
                            val type = parts.getOrNull(1)?.trim() ?: "general"
                            val freq = parts.getOrNull(2)?.trim()?.toIntOrNull() ?: 50

                            processWordCandidate(word, type, freq, existingWords, toInsert, { validCount++ }, { duplicateCount++ }, { err ->
                                errorCount++
                                if (errors.size < 5) errors.add(err)
                            })
                        } else {
                            // Plain TXT
                            processWordCandidate(trimmed, "general", 50, existingWords, toInsert, { validCount++ }, { duplicateCount++ }, { err ->
                                errorCount++
                                if (errors.size < 5) errors.add(err)
                            })
                        }
                    }
                    isFirstLine = false
                    line = reader.readLine()
                }
            }
        }

        // Batch insert into Room database
        if (toInsert.isNotEmpty()) {
            toInsert.chunked(250).forEach { chunk ->
                dao.insertAll(chunk)
            }
            dictionaryRepository.refreshMemoryCache()
        }

        ImportReport(
            totalLinesRead = totalRead,
            validWordsCount = validCount,
            duplicateWordsCount = duplicateCount,
            errorCount = errorCount,
            sampleErrors = errors
        )
    }

    private inline fun processWordCandidate(
        word: String,
        type: String,
        frequency: Int,
        existingWords: MutableSet<String>,
        toInsert: MutableList<DictionaryWord>,
        onValid: () -> Unit,
        onDuplicate: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = DictionaryValidator.validate(word)
        if (!validation.isValid) {
            onError("Từ '$word': ${validation.errorMessage}")
            return
        }

        val normalized = validation.normalizedWord
        if (existingWords.contains(normalized)) {
            onDuplicate()
            return
        }

        existingWords.add(normalized)
        toInsert.add(
            DictionaryWord(
                text = word,
                normalizedText = normalized,
                length = normalized.length,
                wordType = type,
                frequency = frequency,
                source = "imported",
                userAdded = true,
                enabled = true
            )
        )
        onValid()
    }
}
