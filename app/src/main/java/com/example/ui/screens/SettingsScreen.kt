package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.SettingsRepository
import com.example.solver.SolverWeights
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val settings by settingsRepository.settings.collectAsState()

    var wfWeight by remember(settings.weights) { mutableFloatStateOf(settings.weights.wordFrequencyWeight.toFloat()) }
    var lfWeight by remember(settings.weights) { mutableFloatStateOf(settings.weights.letterFrequencyWeight.toFloat()) }
    var pfWeight by remember(settings.weights) { mutableFloatStateOf(settings.weights.positionFrequencyWeight.toFloat()) }
    var igWeight by remember(settings.weights) { mutableFloatStateOf(settings.weights.informationGainWeight.toFloat()) }
    var fitWeight by remember(settings.weights) { mutableFloatStateOf(settings.weights.patternFitWeight.toFloat()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_settings")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Gameplay settings section
            Text(
                text = "CẤU HÌNH TRÒ CHƠI",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Độ dài từ bí mật:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(4, 5, 6, 7).forEach { len ->
                            FilterChip(
                                selected = settings.wordLength == len,
                                onClick = { settingsRepository.updateWordLength(len) },
                                label = { Text("$len chữ cái") }
                            )
                        }
                    }

                    HorizontalDivider()

                    Text("Chế độ gợi ý giải từ (Toán học & Xác suất):", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("OFF", "Tắt", "Tắt bộ giải"),
                            Triple("BASIC", "Cơ bản", "Chỉ gợi ý từ"),
                            Triple("ADVANCED", "Nâng cao", "Đầy đủ Entropy & xác suất")
                        ).forEach { (mode, label, _) ->
                            FilterChip(
                                selected = settings.solverMode == mode,
                                onClick = { settingsRepository.updateSolverMode(mode) },
                                label = { Text(label) }
                            )
                        }
                    }

                    HorizontalDivider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Chế độ hỗ trợ khiếm thị màu", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                "Hiển thị ký hiệu biểu tượng trên từng ô màu",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.colorBlindMode,
                            onCheckedChange = { settingsRepository.updateColorBlindMode(it) }
                        )
                    }
                }
            }

            // Solver weights tuning
            Text(
                text = "TRỌNG SỐ THUẬT TOÁN XÁC SUẤT (KHÔNG DÙNG AI)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    WeightSliderItem("Tần suất từ (Ngữ liệu)", wfWeight) {
                        wfWeight = it
                        applyWeights(settingsRepository, wfWeight, lfWeight, pfWeight, igWeight, fitWeight)
                    }
                    WeightSliderItem("Tần suất chữ cái", lfWeight) {
                        lfWeight = it
                        applyWeights(settingsRepository, wfWeight, lfWeight, pfWeight, igWeight, fitWeight)
                    }
                    WeightSliderItem("Tần suất vị trí", pfWeight) {
                        pfWeight = it
                        applyWeights(settingsRepository, wfWeight, lfWeight, pfWeight, igWeight, fitWeight)
                    }
                    WeightSliderItem("Độ lợi thông tin (Entropy)", igWeight) {
                        igWeight = it
                        applyWeights(settingsRepository, wfWeight, lfWeight, pfWeight, igWeight, fitWeight)
                    }
                    WeightSliderItem("Mẫu phong phú ký tự", fitWeight) {
                        fitWeight = it
                        applyWeights(settingsRepository, wfWeight, lfWeight, pfWeight, igWeight, fitWeight)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = {
                            wfWeight = 0.30f
                            lfWeight = 0.25f
                            pfWeight = 0.20f
                            igWeight = 0.15f
                            fitWeight = 0.10f
                            applyWeights(settingsRepository, wfWeight, lfWeight, pfWeight, igWeight, fitWeight)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Khôi phục mặc định")
                    }
                }
            }
        }
    }
}

private fun applyWeights(
    repo: SettingsRepository,
    wf: Float,
    lf: Float,
    pf: Float,
    ig: Float,
    fit: Float
) {
    repo.updateWeights(
        SolverWeights(
            wordFrequencyWeight = wf.toDouble(),
            letterFrequencyWeight = lf.toDouble(),
            positionFrequencyWeight = pf.toDouble(),
            informationGainWeight = ig.toDouble(),
            patternFitWeight = fit.toDouble()
        )
    )
}

@Composable
private fun WeightSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(title, fontSize = 13.sp)
            Text(
                String.format(Locale.US, "%.2f", value),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.0f..1.0f
        )
    }
}
