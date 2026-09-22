package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameViewModel
import com.example.game.TileColor
import com.example.solver.InteractiveGuessRow
import com.example.solver.InteractiveSolverConclusion
import com.example.solver.SolverCandidate
import com.example.solver.WordKindFilter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveSolverScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.interactiveState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val greenColor = if (isDark) TileGreenDark else TileGreenLight
    val yellowColor = if (isDark) TileYellowDark else TileYellowLight
    val blueColor = if (isDark) TileBlueDark else TileBlueLight
    val grayColor = if (isDark) TileGrayDark else TileGrayLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thuật Toán Giải Ô Chữ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Giải từ đơn & từ ghép • 100% Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_from_solver")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.resetInteractiveSolver() },
                        modifier = Modifier.testTag("btn_reset_solver")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới lại từ đầu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Length & Word Kind Selector
            item {
                LengthSelectorCard(
                    currentLength = state.wordLength,
                    wordKindFilter = state.wordKindFilter,
                    onLengthSelected = { viewModel.setInteractiveWordLength(it) },
                    onWordKindFilterSelected = { viewModel.setInteractiveWordKindFilter(it) }
                )
            }

            // Section 2: Instruction / Color Legend Card
            item {
                ColorGuideCard(
                    greenColor = greenColor,
                    yellowColor = yellowColor,
                    blueColor = blueColor,
                    grayColor = grayColor
                )
            }

            // Section 3: Word Input Form
            item {
                WordInputCard(
                    targetLength = state.wordLength,
                    currentInput = state.currentInputWord,
                    errorMessage = state.errorMessage,
                    wordKindFilter = state.wordKindFilter,
                    onInputChange = { viewModel.updateInteractiveInputWord(it) },
                    onAddWord = { viewModel.addInteractiveRow() },
                    onInsertSpace = {
                        viewModel.updateInteractiveInputWord(state.currentInputWord + " ")
                    }
                )
            }

            // Section 4: Entered Guesses with Interactive Color Toggling
            if (state.guessRows.isNotEmpty()) {
                item {
                    Text(
                        text = "Các từ đã đoán (${state.guessRows.size}) - Chạm từng ô để đổi màu:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                itemsIndexed(state.guessRows) { rowIndex, row ->
                    InteractiveGuessRowCard(
                        rowIndex = rowIndex,
                        row = row,
                        isDark = isDark,
                        onTileClick = { charIndex ->
                            viewModel.cycleInteractiveTileColor(rowIndex, charIndex)
                        },
                        onDeleteRow = {
                            viewModel.removeInteractiveRow(rowIndex)
                        }
                    )
                }
            }

            // Section 5: Conclusion & Suggestions
            item {
                ConclusionSection(
                    conclusion = state.conclusion,
                    isAnalyzing = state.isAnalyzing,
                    greenColor = greenColor,
                    onSelectWord = { word ->
                        viewModel.fillSuggestedWordToInteractiveInput(word)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LengthSelectorCard(
    currentLength: Int,
    wordKindFilter: WordKindFilter,
    onLengthSelected: (Int) -> Unit,
    onWordKindFilterSelected: (WordKindFilter) -> Unit
) {
    var customLengthInput by remember(currentLength) { mutableStateOf(currentLength.toString()) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Số ô ký tự (Độ dài từ):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Stepper [-] [N ô] [+]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { if (currentLength > 2) onLengthSelected(currentLength - 1) },
                        enabled = currentLength > 2,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Giảm số ô")
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "$currentLength ô",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    IconButton(
                        onClick = { if (currentLength < 15) onLengthSelected(currentLength + 1) },
                        enabled = currentLength < 15,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tăng số ô")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct input field + quick chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customLengthInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(2)
                        customLengthInput = digits
                        val num = digits.toIntOrNull()
                        if (num != null && num in 2..15) {
                            onLengthSelected(num)
                        }
                    },
                    label = { Text("Nhập số ô (2-15)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(130.dp)
                        .testTag("input_custom_length")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Horizontal scrollable quick chips
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).forEach { len ->
                        val isSelected = currentLength == len
                        FilterChip(
                            selected = isSelected,
                            onClick = { onLengthSelected(len) },
                            label = {
                                Text(
                                    text = "$len",
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("chip_length_$len")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Word Kind Filter: ALL vs SINGLE vs COMPOUND
            Text(
                text = "Phạm vi giải thuật toán:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WordKindFilter.entries.forEach { filter ->
                    val isSelected = wordKindFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onWordKindFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            when (filter) {
                                WordKindFilter.ALL -> Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                WordKindFilter.SINGLE -> Icon(Icons.Default.ShortText, contentDescription = null, modifier = Modifier.size(16.dp))
                                WordKindFilter.COMPOUND -> Icon(Icons.Default.SpaceBar, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorGuideCard(
    greenColor: Color,
    yellowColor: Color,
    blueColor: Color,
    grayColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quy ước tô màu khi chạm ô chữ:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorGuideBadge(color = greenColor, name = "Xanh lá", desc = "Đúng vị trí")
                ColorGuideBadge(color = yellowColor, name = "Vàng", desc = "Sai vị trí")
                ColorGuideBadge(color = blueColor, name = "Xanh dương", desc = "Khác dấu")
                ColorGuideBadge(color = grayColor, name = "Xám", desc = "Không có")
            }
        }
    }
}

@Composable
private fun ColorGuideBadge(color: Color, name: String, desc: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun WordInputCard(
    targetLength: Int,
    currentInput: String,
    errorMessage: String?,
    wordKindFilter: WordKindFilter,
    onInputChange: (String) -> Unit,
    onAddWord: () -> Unit,
    onInsertSpace: () -> Unit
) {
    val placeholderText = when {
        wordKindFilter == WordKindFilter.COMPOUND || targetLength >= 7 -> "Ví dụ: học sinh, bàn ghế, xe máy..."
        wordKindFilter == WordKindFilter.SINGLE -> "Ví dụ: hoa, xuân, chim, nắng..."
        else -> "Nhập từ $targetLength ký tự (từ đơn hoặc ghép)..."
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhập từ đoán tiếp theo (đúng $targetLength ô):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                // Length counter badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (currentInput.length == targetLength)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${currentInput.length}/$targetLength ký tự",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (currentInput.length == targetLength)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = onInputChange,
                    placeholder = { Text(placeholderText, fontSize = 13.sp) },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_solver_word")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAddWord,
                    enabled = currentInput.trim().isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("btn_add_solver_row")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm từ")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick helper row: Insert space for compound words + clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInsertSpace,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.SpaceBar, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm dấu cách [ ␣ ] cho từ ghép", fontSize = 11.sp)
                }

                if (currentInput.isNotEmpty()) {
                    TextButton(
                        onClick = { onInputChange("") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Xóa trắng", fontSize = 11.sp)
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InteractiveGuessRowCard(
    rowIndex: Int,
    row: InteractiveGuessRow,
    isDark: Boolean,
    onTileClick: (Int) -> Unit,
    onDeleteRow: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#${rowIndex + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp)
            )

            // Horizontal row of clickable tiles (scrollable if word is long like compound words)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                row.word.forEachIndexed { charIndex, char ->
                    val tileColorEnum = row.colors[charIndex]
                    val tileComposeColor = when (tileColorEnum) {
                        TileColor.GREEN -> if (isDark) TileGreenDark else TileGreenLight
                        TileColor.YELLOW -> if (isDark) TileYellowDark else TileYellowLight
                        TileColor.BLUE -> if (isDark) TileBlueDark else TileBlueLight
                        TileColor.GRAY -> if (isDark) TileGrayDark else TileGrayLight
                    }
                    val animatedBg by animateColorAsState(
                        targetValue = tileComposeColor,
                        animationSpec = tween(durationMillis = 200),
                        label = "colorAnim"
                    )

                    val isSpace = char == ' '

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedBg)
                            .border(
                                width = if (isSpace) 2.dp else 1.5.dp,
                                color = if (isSpace) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onTileClick(charIndex) }
                            .testTag("tile_${rowIndex}_$charIndex")
                    ) {
                        Text(
                            text = if (isSpace) "␣" else char.toString().uppercase(),
                            style = if (isSpace) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDeleteRow,
                modifier = Modifier.testTag("btn_delete_row_$rowIndex")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa dòng",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ConclusionSection(
    conclusion: InteractiveSolverConclusion,
    isAnalyzing: Boolean,
    greenColor: Color,
    onSelectWord: (String) -> Unit
) {
    if (isAnalyzing) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        }
        return
    }

    when (conclusion) {
        is InteractiveSolverConclusion.Idle -> {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chưa có lượt đoán nào",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hãy nhập từ bạn đã đoán và chạm đổi màu các ô để ứng dụng phân tích toán học 100% offline.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is InteractiveSolverConclusion.ExactMatchFound -> {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = greenColor.copy(alpha = 0.15f)
                ),
                border = BorderStroke(2.dp, greenColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(greenColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ĐÃ ĐỦ DỮ KIỆN KẾT LUẬN CHÍNH XÁC 100%!",
                        style = MaterialTheme.typography.labelLarge,
                        color = greenColor,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = conclusion.exactWord.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Đây là từ duy nhất trong toàn bộ từ điển thỏa mãn hoàn toàn tất cả các màu và ràng buộc của bạn.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is InteractiveSolverConclusion.ConflictOrNoMatch -> {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mâu thuẫn dữ kiện hoặc không tìm thấy từ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = conclusion.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        is InteractiveSolverConclusion.SuggestionsAvailable -> {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Chưa đủ dữ kiện để kết luận 100%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Còn ${conclusion.remainingCount} từ khả dĩ phù hợp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (conclusion.bestEntropyWord != null) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Từ mang lượng thông tin (Entropy) cao nhất:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = conclusion.bestEntropyWord.uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(
                                    onClick = { onSelectWord(conclusion.bestEntropyWord) },
                                    modifier = Modifier.testTag("btn_use_entropy_word")
                                ) {
                                    Text("Chọn từ này")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = "Gợi ý các từ tối ưu tiếp theo (chọn để nhập nhanh):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    conclusion.topCandidates.take(10).forEachIndexed { idx, candidate ->
                        CandidateRowItem(
                            index = idx + 1,
                            candidate = candidate,
                            onSelect = { onSelectWord(candidate.word) }
                        )
                        if (idx < 9) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateRowItem(
    index: Int,
    candidate: SolverCandidate,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(24.dp)
            )
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = candidate.word.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val isCompound = candidate.word.contains(' ')
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isCompound)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (isCompound) "Từ ghép" else "Từ đơn",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompound)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "Điểm: ${"%.1f".format(candidate.priorityScore)}/100 • Entropy: ${"%.2f".format(candidate.entropy)} • Ưu tiên: ${"%.1f".format(candidate.relativeScoreWeight)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        FilledTonalButton(
            onClick = onSelect,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text("Chọn", fontSize = 12.sp)
        }
    }
}
