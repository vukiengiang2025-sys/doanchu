package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solver.GameConstraints

@Composable
fun EliminationCheckerDialog(
    constraints: GameConstraints,
    onDismiss: () -> Unit
) {
    var queryWord by remember { mutableStateOf("") }
    var explanationResult by remember { mutableStateOf<String?>(null) }
    var isMatching by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = {
            Text("Kiểm tra lý do loại từ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Nhập một từ bất kỳ để kiểm tra xem từ đó có thỏa mãn các ràng buộc màu hiện tại không:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = queryWord,
                    onValueChange = {
                        queryWord = it
                        if (it.isNotBlank()) {
                            val reason = constraints.getEliminationReason(it.trim().lowercase())
                            explanationResult = reason
                            isMatching = (reason == null)
                        } else {
                            explanationResult = null
                        }
                    },
                    label = { Text("Nhập từ cần kiểm tra") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (explanationResult != null || (queryWord.isNotBlank() && isMatching)) {
                    val bg = if (isMatching) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                    val textColor = if (isMatching) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (isMatching) {
                                "HỢP LỆ: Từ \"$queryWord\" thỏa mãn 100% tất cả các ràng buộc xanh, vàng, xanh dương và xám hiện tại!"
                            } else {
                                explanationResult ?: ""
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    )
}
