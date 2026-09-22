package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: DictionaryWord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<DictionaryWord>): List<Long>

    @Update
    suspend fun update(word: DictionaryWord)

    @Delete
    suspend fun delete(word: DictionaryWord)

    @Query("DELETE FROM dictionary_words WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM dictionary_words WHERE enabled = 1 ORDER BY frequency DESC, text ASC")
    fun getAllEnabledWords(): Flow<List<DictionaryWord>>

    @Query("SELECT * FROM dictionary_words WHERE enabled = 1 AND length = :length ORDER BY frequency DESC")
    fun getWordsByLength(length: Int): Flow<List<DictionaryWord>>

    @Query("SELECT * FROM dictionary_words WHERE enabled = 1 AND length = :length ORDER BY frequency DESC")
    suspend fun getWordsByLengthSync(length: Int): List<DictionaryWord>

    @Query("SELECT * FROM dictionary_words WHERE enabled = 1")
    suspend fun getAllEnabledWordsSync(): List<DictionaryWord>

    @Query("SELECT * FROM dictionary_words ORDER BY id DESC")
    fun getAllWordsFlow(): Flow<List<DictionaryWord>>

    @Query("SELECT * FROM dictionary_words ORDER BY text ASC")
    suspend fun getAllWordsSync(): List<DictionaryWord>

    @Query("SELECT * FROM dictionary_words WHERE text LIKE '%' || :query || '%' OR normalizedText LIKE '%' || :query || '%' ORDER BY text ASC")
    fun searchWords(query: String): Flow<List<DictionaryWord>>

    @Query("SELECT * FROM dictionary_words WHERE normalizedText = :normalized LIMIT 1")
    suspend fun findByNormalized(normalized: String): DictionaryWord?

    @Query("UPDATE dictionary_words SET enabled = :enabled, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateEnabled(id: Long, enabled: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM dictionary_words")
    fun getWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dictionary_words WHERE enabled = 1")
    fun getActiveWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dictionary_words WHERE userAdded = 1")
    fun getUserAddedCount(): Flow<Int>
}

@Dao
interface GameDao {
    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Long): GameEntity?

    @Query("SELECT * FROM games ORDER BY startedAt DESC")
    fun getAllGamesFlow(): Flow<List<GameEntity>>

    @Insert
    suspend fun insertGuess(guess: GuessEntity): Long

    @Query("SELECT * FROM guesses WHERE gameId = :gameId ORDER BY attemptNumber ASC")
    suspend fun getGuessesForGame(gameId: Long): List<GuessEntity>

    @Query("SELECT * FROM guesses WHERE gameId = :gameId ORDER BY attemptNumber ASC")
    fun getGuessesForGameFlow(gameId: Long): Flow<List<GuessEntity>>
}

@Dao
interface LetterStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<LetterStatisticsEntity>)

    @Query("DELETE FROM letter_statistics")
    suspend fun clearAll()

    @Query("SELECT * FROM letter_statistics")
    suspend fun getAllStats(): List<LetterStatisticsEntity>
}
