package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameStatus
import com.example.game.GameViewModel
import com.example.ui.components.EliminationCheckerDialog
import com.example.ui.components.RulesDialog
import com.example.ui.components.ScoreBreakdownDialog
import com.example.ui.components.SolverAssistantView
import com.example.ui.components.VietnameseKeyboard
import com.example.ui.components.WordGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToDictionary: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToInteractiveSolver: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val solverResult by viewModel.solverResult.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedCandidate by viewModel.selectedCandidateForDetail.collectAsState()
    val showRules by viewModel.showRulesDialog.collectAsState()
    val showElimination by viewModel.showEliminationDialog.collectAsState()
    val selectedTile by viewModel.selectedTileDetail.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ĐOÁN TỪ VIỆT",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${settings.wordLength} chữ cái • 6 lượt",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setRulesDialogVisible(true) },
                        modifier = Modifier.testTag("btn_rules")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Luật chơi"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToInteractiveSolver,
                        modifier = Modifier.testTag("btn_nav_interactive_solver")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Trợ thủ giải từ tự do",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.startNewGame() },
                        modifier = Modifier.testTag("btn_new_game")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Chơi ván mới"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToDictionary,
                        modifier = Modifier.testTag("btn_nav_dictionary")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Từ điển"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToStatistics,
                        modifier = Modifier.testTag("btn_nav_stats")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Thống kê"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("btn_nav_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable board and assistant section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Message banner
                AnimatedVisibility(visible = state.message != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (state.status) {
                                    GameStatus.WON -> MaterialTheme.colorScheme.primaryContainer
                                    GameStatus.LOST -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = state.message ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (state.status) {
                                GameStatus.WON -> MaterialTheme.colorScheme.onPrimaryContainer
                                GameStatus.LOST -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Word grid
                WordGrid(
                    state = state,
                    colorBlindMode = settings.colorBlindMode,
                    onTileClick = { tile -> viewModel.selectTileDetail(tile) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Mathematical Probability Solver Assistant
                SolverAssistantView(
                    solverResult = solverResult,
                    solverMode = settings.solverMode,
                    onSelectCandidate = { word -> viewModel.fillCandidateWord(word) },
                    onShowBreakdown = { candidate -> viewModel.showCandidateDetail(candidate) },
                    onCheckElimination = { viewModel.setEliminationDialogVisible(true) }
                )
            }

            // Vietnamese on-screen keyboard
            VietnameseKeyboard(
                keyboardColors = state.keyboardColors,
                onKeyPress = { char -> viewModel.inputKey(char) },
                onTonePress = { tone -> viewModel.applyTone(tone) },
                onDeletePress = { viewModel.deleteKey() },
                onSubmitPress = { viewModel.submitGuess() }
            )
        }
    }

    // Rules dialog
    if (showRules) {
        RulesDialog(onDismiss = { viewModel.setRulesDialogVisible(false) })
    }

    // Mathematical Score Breakdown dialog
    selectedCandidate?.let { candidate ->
        ScoreBreakdownDialog(
            candidate = candidate,
            weights = settings.weights,
            onDismiss = { viewModel.dismissCandidateDetail() }
        )
    }

    // Elimination checker dialog
    if (showElimination && solverResult != null) {
        EliminationCheckerDialog(
            constraints = solverResult!!.constraints,
            onDismiss = { viewModel.setEliminationDialogVisible(false) }
        )
    }

    // Tile detail explanation dialog
    selectedTile?.let { tile ->
        AlertDialog(
            onDismissRequest = { viewModel.selectTileDetail(null) },
            confirmButton = {
                Button(onClick = { viewModel.selectTileDetail(null) }) {
                    Text("Đóng")
                }
            },
            title = {
                Text(
                    text = "Ô chữ: '${tile.letter.uppercaseChar()}'",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Trạng thái: ${tile.color.displayName} ${tile.color.symbol}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = tile.explanation,
                        fontSize = 14.sp
                    )
                }
            }
        )
    }

    // Game Finished dialog
    if (state.status != GameStatus.PLAYING) {
        val isWon = state.status == GameStatus.WON
        AlertDialog(
            onDismissRequest = { /* Keep visible */ },
            title = {
                Text(
                    text = if (isWon) "🎉 CHIẾN THẮNG!" else "💔 KẾT THÚC!",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isWon) {
                            "Tuyệt vời! Bạn đã đoán chính xác từ \"${state.answer.uppercase()}\" sau ${state.rows.size} lượt!"
                        } else {
                            "Rất tiếc bạn đã hết lượt. Từ bí mật chính xác là: \"${state.answer.uppercase()}\"."
                        },
                        fontSize = 14.sp
                    )

                    // Color emoji matrix for sharing
                    Text(
                        text = "Kết quả:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = state.rows.joinToString("\n") { r ->
                            r.tiles.joinToString("") { it.color.symbol }
                        },
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.startNewGame() }) {
                    Text("Chơi ván mới")
                }
            },
            dismissButton = {
                Button(onClick = onNavigateToStatistics) {
                    Text("Xem thống kê")
                }
            }
        )
    }
}
