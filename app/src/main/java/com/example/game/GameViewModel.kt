package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionary.DictionaryRepository
import com.example.normalization.Tone
import com.example.normalization.VietnameseNormalizer
import com.example.settings.GameSettings
import com.example.settings.SettingsRepository
import com.example.solver.ProbabilitySolver
import com.example.solver.SolverCandidate
import com.example.solver.SolverResult
import com.example.statistics.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val dictionaryRepository = DictionaryRepository(application)
    val settingsRepository = SettingsRepository(application)
    val statisticsRepository = StatisticsRepository(application)

    private var engine: GameEngine? = null

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _solverResult = MutableStateFlow<SolverResult?>(null)
    val solverResult: StateFlow<SolverResult?> = _solverResult.asStateFlow()

    private val _selectedCandidateForDetail = MutableStateFlow<SolverCandidate?>(null)
    val selectedCandidateForDetail: StateFlow<SolverCandidate?> = _selectedCandidateForDetail.asStateFlow()

    private val _showRulesDialog = MutableStateFlow(false)
    val showRulesDialog: StateFlow<Boolean> = _showRulesDialog.asStateFlow()

    private val _showEliminationDialog = MutableStateFlow(false)
    val showEliminationDialog: StateFlow<Boolean> = _showEliminationDialog.asStateFlow()

    private val _selectedTileDetail = MutableStateFlow<ResultTile?>(null)
    val selectedTileDetail: StateFlow<ResultTile?> = _selectedTileDetail.asStateFlow()

    // Interactive Solver State
    private val _interactiveState = MutableStateFlow(com.example.solver.InteractiveSolverState())
    val interactiveState: StateFlow<com.example.solver.InteractiveSolverState> = _interactiveState.asStateFlow()

    val settings: StateFlow<GameSettings> = settingsRepository.settings

    init {
        viewModelScope.launch {
            dictionaryRepository.ensureInitialized()
            startNewGame()
        }
    }

    fun startNewGame() {
        viewModelScope.launch {
            val currentSettings = settings.value
            val answer = dictionaryRepository.getRandomAnswer(currentSettings.wordLength) ?: "ngày"

            val newEngine = GameEngine(
                wordLength = currentSettings.wordLength,
                maxAttempts = currentSettings.maxAttempts,
                answer = answer
            )
            engine = newEngine
            _gameState.value = newEngine.state
            _solverResult.value = null

            // Initial solver computation
            updateSolver()
        }
    }

    fun inputKey(char: Char) {
        val eng = engine ?: return
        if (eng.state.status != GameStatus.PLAYING) return

        // Check if Telex expansion applies
        val telexResult = VietnameseNormalizer.tryTelexTransform(eng.state.currentInput, char)
        if (telexResult != null) {
            // Replace with transformed word
            eng.clearInput()
            for (c in telexResult) {
                eng.inputChar(c)
            }
            _gameState.value = eng.state
        } else {
            _gameState.value = eng.inputChar(char)
        }
    }

    fun applyTone(tone: Tone) {
        val eng = engine ?: return
        if (eng.state.status != GameStatus.PLAYING) return
        val modified = VietnameseNormalizer.applyToneToWord(eng.state.currentInput, tone)
        eng.clearInput()
        for (c in modified) {
            eng.inputChar(c)
        }
        _gameState.value = eng.state
    }

    fun deleteKey() {
        val eng = engine ?: return
        _gameState.value = eng.deleteChar()
    }

    fun fillCandidateWord(word: String) {
        val eng = engine ?: return
        if (eng.state.status != GameStatus.PLAYING) return
        eng.clearInput()
        for (c in word) {
            eng.inputChar(c)
        }
        _gameState.value = eng.state
    }

    fun submitGuess() {
        val eng = engine ?: return
        val currentInput = eng.state.currentInput
        if (currentInput.length != eng.wordLength) {
            _gameState.value = eng.state.copy(message = "Từ phải đủ ${eng.wordLength} chữ cái!")
            return
        }

        viewModelScope.launch {
            val isValid = dictionaryRepository.isWordInDictionary(currentInput)
            val (newState, submitted) = eng.submitGuess(isValid)
            _gameState.value = newState

            if (submitted) {
                // Record game in statistics if finished
                if (newState.status != GameStatus.PLAYING) {
                    val guessesData = newState.rows.map { row ->
                        Pair(row.guess, GuessEvaluator.patternToString(row.tiles))
                    }
                    statisticsRepository.recordGame(
                        answer = newState.answer,
                        length = newState.wordLength,
                        status = newState.status.name,
                        attempts = newState.rows.size,
                        guesses = guessesData
                    )
                }

                // Update solver
                updateSolver()
            }
        }
    }

    private fun updateSolver() {
        val eng = engine ?: return
        val currentSettings = settings.value
        if (currentSettings.solverMode == "OFF") return

        viewModelScope.launch {
            val enabledWords = dictionaryRepository.getEnabledWords(eng.wordLength)
            val guessesHistory = eng.state.rows.map { Pair(it.guess, it.tiles) }
            val wordFreqMap = dictionaryRepository.getWordFrequencyMap()

            val result = ProbabilitySolver.solve(
                dictionaryWords = enabledWords,
                targetLength = eng.wordLength,
                guesses = guessesHistory,
                weights = currentSettings.weights,
                wordFrequencyMap = wordFreqMap
            )
            _solverResult.value = result
        }
    }

    fun showCandidateDetail(candidate: SolverCandidate) {
        _selectedCandidateForDetail.value = candidate
    }

    fun dismissCandidateDetail() {
        _selectedCandidateForDetail.value = null
    }

    fun setRulesDialogVisible(visible: Boolean) {
        _showRulesDialog.value = visible
    }

    fun setEliminationDialogVisible(visible: Boolean) {
        _showEliminationDialog.value = visible
    }

    fun selectTileDetail(tile: ResultTile?) {
        _selectedTileDetail.value = tile
    }

    // ==========================================
    // Interactive External Solver Actions
    // ==========================================

    fun setInteractiveWordLength(length: Int) {
        val clamped = length.coerceIn(2, 20)
        _interactiveState.value = _interactiveState.value.copy(
            wordLength = clamped,
            guessRows = emptyList(),
            currentInputWord = "",
            conclusion = com.example.solver.InteractiveSolverConclusion.Idle,
            errorMessage = null
        )
    }

    fun setInteractiveWordKindFilter(filter: com.example.solver.WordKindFilter) {
        _interactiveState.value = _interactiveState.value.copy(
            wordKindFilter = filter,
            errorMessage = null
        )
        triggerInteractiveAnalysis()
    }

    fun updateInteractiveInputWord(text: String) {
        _interactiveState.value = _interactiveState.value.copy(
            currentInputWord = text,
            errorMessage = null
        )
    }

    fun addInteractiveRow() {
        val cur = _interactiveState.value
        val raw = cur.currentInputWord.trim()
        val normalized = VietnameseNormalizer.normalize(raw)

        if (normalized.length != cur.wordLength) {
            _interactiveState.value = cur.copy(
                errorMessage = "Từ phải có đúng ${cur.wordLength} ô ký tự (bạn đã nhập ${normalized.length} ký tự)"
            )
            return
        }

        // Validate that each character is a valid Vietnamese letter or space
        for (c in normalized) {
            if (!VietnameseNormalizer.isValidVietnameseChar(c)) {
                _interactiveState.value = cur.copy(
                    errorMessage = "Từ chứa ký tự '$c' không hợp lệ trong tiếng Việt"
                )
                return
            }
        }

        // Check word kind constraints if set
        if (cur.wordKindFilter == com.example.solver.WordKindFilter.SINGLE && normalized.contains(' ')) {
            _interactiveState.value = cur.copy(
                errorMessage = "Bạn đang chọn chế độ 'Chỉ từ đơn', từ không được chứa khoảng trắng"
            )
            return
        }
        if (cur.wordKindFilter == com.example.solver.WordKindFilter.COMPOUND && !normalized.contains(' ')) {
            _interactiveState.value = cur.copy(
                errorMessage = "Bạn đang chọn chế độ 'Chỉ từ ghép', từ cần chứa khoảng trắng (ví dụ: học sinh, bàn ghế)"
            )
            return
        }

        // Default all tiles to GRAY initially
        val defaultColors = List(cur.wordLength) { TileColor.GRAY }
        val newRow = com.example.solver.InteractiveGuessRow(
            word = normalized,
            colors = defaultColors
        )

        val updatedRows = cur.guessRows + newRow
        _interactiveState.value = cur.copy(
            guessRows = updatedRows,
            currentInputWord = "",
            errorMessage = null
        )

        // Automatically trigger analysis
        triggerInteractiveAnalysis()
    }

    fun cycleInteractiveTileColor(rowIndex: Int, charIndex: Int) {
        val cur = _interactiveState.value
        if (rowIndex !in cur.guessRows.indices) return
        val targetRow = cur.guessRows[rowIndex]
        if (charIndex !in targetRow.colors.indices) return

        val nextColor = com.example.solver.InteractiveSolverEngine.getNextColor(targetRow.colors[charIndex])
        val newColors = targetRow.colors.toMutableList()
        newColors[charIndex] = nextColor

        val updatedRow = targetRow.copy(colors = newColors)
        val updatedRows = cur.guessRows.toMutableList()
        updatedRows[rowIndex] = updatedRow

        _interactiveState.value = cur.copy(guessRows = updatedRows)
        triggerInteractiveAnalysis()
    }

    fun setInteractiveTileColor(rowIndex: Int, charIndex: Int, color: TileColor) {
        val cur = _interactiveState.value
        if (rowIndex !in cur.guessRows.indices) return
        val targetRow = cur.guessRows[rowIndex]
        if (charIndex !in targetRow.colors.indices) return

        val newColors = targetRow.colors.toMutableList()
        newColors[charIndex] = color

        val updatedRow = targetRow.copy(colors = newColors)
        val updatedRows = cur.guessRows.toMutableList()
        updatedRows[rowIndex] = updatedRow

        _interactiveState.value = cur.copy(guessRows = updatedRows)
        triggerInteractiveAnalysis()
    }

    fun removeInteractiveRow(rowIndex: Int) {
        val cur = _interactiveState.value
        if (rowIndex !in cur.guessRows.indices) return

        val updatedRows = cur.guessRows.toMutableList()
        updatedRows.removeAt(rowIndex)

        _interactiveState.value = cur.copy(guessRows = updatedRows)
        if (updatedRows.isEmpty()) {
            _interactiveState.value = _interactiveState.value.copy(
                conclusion = com.example.solver.InteractiveSolverConclusion.Idle
            )
        } else {
            triggerInteractiveAnalysis()
        }
    }

    fun resetInteractiveSolver() {
        val cur = _interactiveState.value
        _interactiveState.value = cur.copy(
            guessRows = emptyList(),
            currentInputWord = "",
            conclusion = com.example.solver.InteractiveSolverConclusion.Idle,
            errorMessage = null
        )
    }

    fun fillSuggestedWordToInteractiveInput(word: String) {
        _interactiveState.value = _interactiveState.value.copy(
            currentInputWord = word,
            errorMessage = null
        )
    }

    fun triggerInteractiveAnalysis() {
        val cur = _interactiveState.value
        if (cur.guessRows.isEmpty()) {
            _interactiveState.value = cur.copy(conclusion = com.example.solver.InteractiveSolverConclusion.Idle)
            return
        }

        viewModelScope.launch {
            _interactiveState.value = cur.copy(isAnalyzing = true)
            var enabledWords = dictionaryRepository.getEnabledWords(cur.wordLength)
            when (cur.wordKindFilter) {
                com.example.solver.WordKindFilter.SINGLE -> {
                    enabledWords = enabledWords.filter { !it.contains(' ') }
                }
                com.example.solver.WordKindFilter.COMPOUND -> {
                    enabledWords = enabledWords.filter { it.contains(' ') }
                }
                com.example.solver.WordKindFilter.ALL -> {
                    // Include both single and compound words
                }
            }
            val freqMap = dictionaryRepository.getWordFrequencyMap()

            val conclusion = com.example.solver.InteractiveSolverEngine.analyze(
                dictionaryWords = enabledWords,
                wordLength = cur.wordLength,
                rows = cur.guessRows,
                weights = settings.value.weights,
                wordFrequencyMap = freqMap
            )

            _interactiveState.value = _interactiveState.value.copy(
                conclusion = conclusion,
                isAnalyzing = false
            )
        }
    }
}
