package com.example.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dictionary_words",
    indices = [
        Index(value = ["normalizedText"], unique = true),
        Index(value = ["length"]),
        Index(value = ["enabled"]),
        Index(value = ["frequency"])
    ]
)
data class DictionaryWord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val normalizedText: String,
    val length: Int,
    val wordType: String = "general",
    val frequency: Int = 1,
    val source: String = "bundled",
    val userAdded: Boolean = false,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val answer: String,
    val length: Int,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val status: String = "PLAYING", // "WON", "LOST", "PLAYING"
    val attempts: Int = 0
)

@Entity(
    tableName = "guesses",
    indices = [Index(value = ["gameId"])]
)
data class GuessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val guess: String,
    val result: String,
    val attemptNumber: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_dictionary",
    indices = [Index(value = ["normalizedText"], unique = true)]
)
data class UserDictionaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val normalizedText: String,
    val note: String = "",
    val source: String = "manual",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "letter_statistics")
data class LetterStatisticsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val letter: String,
    val position: Int,
    val frequency: Double,
    val totalOccurrences: Int
)
