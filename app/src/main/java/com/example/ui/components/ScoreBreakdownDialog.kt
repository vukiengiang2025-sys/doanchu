package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solver.SolverCandidate
import com.example.solver.SolverWeights
import java.util.Locale

@Composable
fun ScoreBreakdownDialog(
    candidate: SolverCandidate,
    weights: SolverWeights,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Từ: \"${candidate.word.uppercase()}\"",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = String.format(Locale.US, "%.1f%%", candidate.relativeProbability),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Giải thích toán học & xác suất không dùng AI (Shannon Entropy + Corpus Bayesian)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

                MetricItem(
                    label = "Tổng điểm tổng hợp",
                    value = String.format(Locale.US, "%.3f", candidate.totalScore),
                    progress = candidate.totalScore.toFloat()
                )

                MetricItem(
                    label = "Độ lợi thông tin (Entropy)",
                    value = String.format(Locale.US, "%.2f bits", candidate.entropy),
                    progress = (candidate.entropy / 4.0).coerceIn(0.0, 1.0).toFloat()
                )

                MetricItem(
                    label = "Ước lượng ứng viên còn lại",
                    value = String.format(Locale.US, "%.1f từ", candidate.expectedRemaining),
                    progress = null
                )

                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

                Text("Chi tiết thành phần tính điểm:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                SubScoreItem(
                    name = "Tần suất từ (Ngữ liệu)",
                    weight = weights.wordFrequencyWeight,
                    score = candidate.breakdown.wordFrequencyScore
                )
                SubScoreItem(
                    name = "Tần suất ký tự",
                    weight = weights.letterFrequencyWeight,
                    score = candidate.breakdown.letterFrequencyScore
                )
                SubScoreItem(
                    name = "Tần suất theo vị trí",
                    weight = weights.positionFrequencyWeight,
                    score = candidate.breakdown.positionFrequencyScore
                )
                SubScoreItem(
                    name = "Độ lợi thông tin (Entropy)",
                    weight = weights.informationGainWeight,
                    score = candidate.breakdown.informationGainScore
                )
                SubScoreItem(
                    name = "Độ phong phú mẫu ký tự",
                    weight = weights.patternFitWeight,
                    score = candidate.breakdown.patternFitScore
                )
            }
        }
    )
}

@Composable
private fun MetricItem(label: String, value: String, progress: Float?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        if (progress != null) {
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
        }
    }
}

@Composable
private fun SubScoreItem(name: String, weight: Double, score: Double) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "$name (x${String.format(Locale.US, "%.2f", weight)})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            String.format(Locale.US, "%.2f", score),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
