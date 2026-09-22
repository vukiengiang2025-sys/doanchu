package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solver.InformationProbe
import com.example.solver.SolverCandidate
import com.example.solver.SolverResult
import java.util.Locale

@Composable
fun SolverAssistantView(
    solverResult: SolverResult?,
    solverMode: String, // "OFF", "BASIC", "ADVANCED"
    onSelectCandidate: (String) -> Unit,
    onShowBreakdown: (SolverCandidate) -> Unit,
    onCheckElimination: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (solverMode == "OFF" || solverResult == null) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bộ giải phân tích logic & thống kê",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Còn ${solverResult.remainingCandidatesCount} từ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCheckElimination,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Kiểm tra từ bị loại",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (solverResult.topCandidates.isEmpty()) {
                Text(
                    text = "Không tìm thấy từ nào trong từ điển khớp tất cả ràng buộc.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                // Section 1: LIKELY ANSWERS
                Text(
                    text = "ỨNG VIÊN ĐÁP ÁN KHẢ DĨ (Điểm ưu tiên & Tỷ trọng điểm tương đối):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(solverResult.topCandidates.take(8)) { candidate ->
                        CandidateChip(
                            candidate = candidate,
                            showAdvancedMetrics = (solverMode == "ADVANCED"),
                            onClick = { onSelectCandidate(candidate.word) },
                            onInfoClick = { onShowBreakdown(candidate) }
                        )
                    }
                }

                // Section 2: BEST INFORMATION PROBES (if Mode ADVANCED and multiple candidates remain)
                if (solverMode == "ADVANCED" && solverResult.bestInformationProbes.isNotEmpty() && solverResult.remainingCandidatesCount > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TỪ NÊN THỬ ĐỂ THU THẬP THÔNG TIN TỐI ĐA (Shannon Entropy):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(solverResult.bestInformationProbes.take(5)) { probe ->
                            ProbeChip(
                                probe = probe,
                                onClick = { onSelectCandidate(probe.word) }
                            )
                        }
                    }
                }

                // Footnote clarification regarding probability semantics
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "* % hiển thị là tỷ trọng điểm thống kê trong tập ứng viên hiện tại, KHÔNG phải xác suất thực nghiệm của đáp án.",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CandidateChip(
    candidate: SolverCandidate,
    showAdvancedMetrics: Boolean,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column {
                Text(
                    text = candidate.word.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = String.format(Locale.US, "Điểm: %.1f", candidate.priorityScore),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format(Locale.US, "(%.1f%%)", candidate.relativeScoreWeight),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showAdvancedMetrics) {
                    Text(
                        text = String.format(Locale.US, "H: %.2f bits", candidate.entropy),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Xem chi tiết điểm",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProbeChip(
    probe: InformationProbe,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = probe.word.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                if (probe.isPossibleAnswer) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "★",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = String.format(Locale.US, "H: %.2f bits", probe.entropy),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = String.format(Locale.US, "Còn ~%.1f (tệ nhất %d)", probe.expectedRemainingCandidates, probe.worstCaseRemainingCandidates),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
