package com.example.statistics

import android.content.Context
import com.example.database.AppDatabase
import com.example.database.GameDao
import com.example.database.GameEntity
import com.example.database.GuessEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class GameStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val winRatePercent: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val attemptsDistribution: Map<Int, Int> = emptyMap() // attempt number -> count
)

class StatisticsRepository(
    context: Context,
    private val gameDao: GameDao = AppDatabase.getDatabase(context).gameDao()
) {
    val statsFlow: Flow<GameStatistics> = gameDao.getAllGamesFlow().map { games ->
        calculateStats(games)
    }

    suspend fun recordGame(
        answer: String,
        length: Int,
        status: String,
        attempts: Int,
        guesses: List<Pair<String, String>> // guess, serialized result
    ): Long = withContext(Dispatchers.IO) {
        val gameEntity = GameEntity(
            answer = answer,
            length = length,
            finishedAt = System.currentTimeMillis(),
            status = status,
            attempts = attempts
        )
        val gameId = gameDao.insertGame(gameEntity)

        guesses.forEachIndexed { index, pair ->
            gameDao.insertGuess(
                GuessEntity(
                    gameId = gameId,
                    guess = pair.first,
                    result = pair.second,
                    attemptNumber = index + 1
                )
            )
        }

        gameId
    }

    private fun calculateStats(games: List<GameEntity>): GameStatistics {
        if (games.isEmpty()) return GameStatistics()

        val played = games.size
        val wonGames = games.filter { it.status == "WON" }
        val won = wonGames.size
        val winRate = if (played > 0) (won * 100) / played else 0

        val distribution = mutableMapOf<Int, Int>()
        for (i in 1..6) distribution[i] = 0
        for (g in wonGames) {
            val att = g.attempts.coerceIn(1, 6)
            distribution[att] = (distribution[att] ?: 0) + 1
        }

        // Calculate streaks (sorted chronological)
        val sortedGames = games.sortedBy { it.startedAt }
        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0

        for (g in sortedGames) {
            if (g.status == "WON") {
                tempStreak++
                if (tempStreak > bestStreak) bestStreak = tempStreak
            } else {
                tempStreak = 0
            }
        }
        currentStreak = tempStreak

        return GameStatistics(
            gamesPlayed = played,
            gamesWon = won,
            winRatePercent = winRate,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            attemptsDistribution = distribution
        )
    }
}
