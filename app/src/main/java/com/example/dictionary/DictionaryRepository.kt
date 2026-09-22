package com.example.dictionary

import android.content.Context
import com.example.database.AppDatabase
import com.example.database.DictionaryDao
import com.example.database.DictionaryWord
import com.example.normalization.VietnameseNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DictionaryRepository(
    private val context: Context,
    private val dictionaryDao: DictionaryDao = AppDatabase.getDatabase(context).dictionaryDao()
) {
    private val cacheMutex = Mutex()
    private val memoryCacheByLength = mutableMapOf<Int, List<String>>()
    private val memoryWordFrequencyMap = mutableMapOf<String, Int>()

    val allWordsFlow: Flow<List<DictionaryWord>> = dictionaryDao.getAllWordsFlow()
    val totalCountFlow: Flow<Int> = dictionaryDao.getWordCount()
    val activeCountFlow: Flow<Int> = dictionaryDao.getActiveWordCount()
    val userAddedCountFlow: Flow<Int> = dictionaryDao.getUserAddedCount()

    /**
     * Initializes dictionary on first launch or if database has fewer than 2000 words.
     */
    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val existingWords = dictionaryDao.getAllWordsSync()
        if (existingWords.size < 2000) {
            val bundledWords = AssetDictionaryLoader.loadBundledWords(context)
            if (bundledWords.isNotEmpty()) {
                // Insert in batches of 250
                bundledWords.chunked(250).forEach { chunk ->
                    dictionaryDao.insertAll(chunk)
                }
            }
        }
        refreshMemoryCache()
    }

    /**
     * Force re-syncs all bundled words from assets into the database.
     */
    suspend fun reloadBundledDictionary(): Int = withContext(Dispatchers.IO) {
        val bundledWords = AssetDictionaryLoader.loadBundledWords(context)
        if (bundledWords.isNotEmpty()) {
            bundledWords.chunked(250).forEach { chunk ->
                dictionaryDao.insertAll(chunk)
            }
        }
        refreshMemoryCache()
        bundledWords.size
    }

    /**
     * Refreshes in-memory cache of enabled words for instant solver queries without DB lag.
     */
    suspend fun refreshMemoryCache() = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            val enabledWords = dictionaryDao.getAllEnabledWordsSync()
            memoryCacheByLength.clear()
            memoryWordFrequencyMap.clear()

            for (w in enabledWords) {
                val list = memoryCacheByLength.getOrPut(w.length) { mutableListOf() }
                (list as MutableList).add(w.normalizedText)
                memoryWordFrequencyMap[w.normalizedText] = w.frequency
            }
        }
    }

    /**
     * Retrieves all cached enabled words of given length.
     */
    suspend fun getEnabledWords(length: Int): List<String> = withContext(Dispatchers.Default) {
        cacheMutex.withLock {
            memoryCacheByLength[length] ?: emptyList()
        }
    }

    /**
     * Returns word frequency map for scoring.
     */
    suspend fun getWordFrequencyMap(): Map<String, Int> = withContext(Dispatchers.Default) {
        cacheMutex.withLock {
            memoryWordFrequencyMap.toMap()
        }
    }

    /**
     * Checks if a word exists in the enabled dictionary.
     */
    suspend fun isWordInDictionary(word: String): Boolean = withContext(Dispatchers.Default) {
        val normalized = VietnameseNormalizer.normalize(word)
        cacheMutex.withLock {
            memoryCacheByLength[normalized.length]?.contains(normalized) == true
        }
    }

    /**
     * Selects a random answer word for the game of specified length.
     */
    suspend fun getRandomAnswer(length: Int): String? = withContext(Dispatchers.Default) {
        val words = getEnabledWords(length)
        if (words.isEmpty()) null else words.random()
    }

    /**
     * Searches words by query.
     */
    fun search(query: String): Flow<List<DictionaryWord>> {
        return dictionaryDao.searchWords(query.trim().lowercase())
    }

    /**
     * Adds a new user-defined word.
     */
    suspend fun addWord(
        text: String,
        type: String = "general",
        frequency: Int = 50,
        source: String = "user"
    ): Result<DictionaryWord> = withContext(Dispatchers.IO) {
        val validation = DictionaryValidator.validate(text)
        if (!validation.isValid) {
            return@withContext Result.failure(IllegalArgumentException(validation.errorMessage ?: "Từ không hợp lệ"))
        }

        val normalized = validation.normalizedWord
        val existing = dictionaryDao.findByNormalized(normalized)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("Từ '$normalized' đã tồn tại trong từ điển"))
        }

        val newWord = DictionaryWord(
            text = text.trim(),
            normalizedText = normalized,
            length = normalized.length,
            wordType = type,
            frequency = frequency,
            source = source,
            userAdded = true,
            enabled = true
        )
        val id = dictionaryDao.insert(newWord)
        refreshMemoryCache()
        Result.success(newWord.copy(id = id))
    }

    /**
     * Toggles word enabled status.
     */
    suspend fun toggleWordEnabled(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        dictionaryDao.updateEnabled(id, enabled)
        refreshMemoryCache()
    }

    /**
     * Deletes a word by id.
     */
    suspend fun deleteWord(id: Long) = withContext(Dispatchers.IO) {
        dictionaryDao.deleteById(id)
        refreshMemoryCache()
    }
}
