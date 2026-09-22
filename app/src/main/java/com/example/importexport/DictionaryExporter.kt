package com.example.importexport

import android.content.Context
import android.net.Uri
import com.example.database.AppDatabase
import com.example.database.DictionaryWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter

object DictionaryExporter {

    /**
     * Exports words to a Uri opened via SAF.
     */
    suspend fun exportToUri(
        context: Context,
        uri: Uri,
        format: String, // "TXT", "CSV", "JSON"
        onlyEnabled: Boolean = true
    ): Int = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).dictionaryDao()
        val words: List<DictionaryWord> = if (onlyEnabled) {
            dao.getAllEnabledWordsSync()
        } else {
            dao.getAllWordsSync()
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val writer = BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8))

            when (format.uppercase()) {
                "JSON" -> {
                    val array = JSONArray()
                    for (w in words) {
                        val obj = JSONObject()
                        obj.put("word", w.text)
                        obj.put("type", w.wordType)
                        obj.put("frequency", w.frequency)
                        array.put(obj)
                    }
                    writer.write(array.toString(2))
                }
                "CSV" -> {
                    writer.write("word,type,frequency\n")
                    for (w in words) {
                        writer.write("\"${w.text}\",\"${w.wordType}\",${w.frequency}\n")
                    }
                }
                else -> { // Plain TXT
                    for (w in words) {
                        writer.write(w.text)
                        writer.newLine()
                    }
                }
            }
            writer.flush()
        }

        words.size
    }
}
