package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.solver.SolverWeights
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameSettings(
    val wordLength: Int = 4,
    val maxAttempts: Int = 6,
    val solverMode: String = "ADVANCED", // "OFF", "BASIC", "ADVANCED"
    val colorBlindMode: Boolean = false,
    val darkMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val weights: SolverWeights = SolverWeights()
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("viet_wordle_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private fun loadSettings(): GameSettings {
        return GameSettings(
            wordLength = prefs.getInt("pref_word_length", 4),
            maxAttempts = prefs.getInt("pref_max_attempts", 6),
            solverMode = prefs.getString("pref_solver_mode", "ADVANCED") ?: "ADVANCED",
            colorBlindMode = prefs.getBoolean("pref_color_blind", false),
            darkMode = prefs.getString("pref_dark_mode", "SYSTEM") ?: "SYSTEM",
            weights = SolverWeights(
                wordFrequencyWeight = prefs.getFloat("weight_wf", 0.30f).toDouble(),
                letterFrequencyWeight = prefs.getFloat("weight_lf", 0.25f).toDouble(),
                positionFrequencyWeight = prefs.getFloat("weight_pf", 0.20f).toDouble(),
                informationGainWeight = prefs.getFloat("weight_ig", 0.15f).toDouble(),
                patternFitWeight = prefs.getFloat("weight_fit", 0.10f).toDouble()
            )
        )
    }

    fun updateWordLength(length: Int) {
        prefs.edit().putInt("pref_word_length", length).apply()
        _settings.value = _settings.value.copy(wordLength = length)
    }

    fun updateMaxAttempts(attempts: Int) {
        prefs.edit().putInt("pref_max_attempts", attempts).apply()
        _settings.value = _settings.value.copy(maxAttempts = attempts)
    }

    fun updateSolverMode(mode: String) {
        prefs.edit().putString("pref_solver_mode", mode).apply()
        _settings.value = _settings.value.copy(solverMode = mode)
    }

    fun updateColorBlindMode(enabled: Boolean) {
        prefs.edit().putBoolean("pref_color_blind", enabled).apply()
        _settings.value = _settings.value.copy(colorBlindMode = enabled)
    }

    fun updateDarkMode(mode: String) {
        prefs.edit().putString("pref_dark_mode", mode).apply()
        _settings.value = _settings.value.copy(darkMode = mode)
    }

    fun updateWeights(weights: SolverWeights) {
        prefs.edit()
            .putFloat("weight_wf", weights.wordFrequencyWeight.toFloat())
            .putFloat("weight_lf", weights.letterFrequencyWeight.toFloat())
            .putFloat("weight_pf", weights.positionFrequencyWeight.toFloat())
            .putFloat("weight_ig", weights.informationGainWeight.toFloat())
            .putFloat("weight_fit", weights.patternFitWeight.toFloat())
            .apply()
        _settings.value = _settings.value.copy(weights = weights)
    }
}
