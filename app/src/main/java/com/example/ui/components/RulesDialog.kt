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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.TileColor
import com.example.ui.theme.TileBlueLight
import com.example.ui.theme.TileGrayLight
import com.example.ui.theme.TileGreenLight
import com.example.ui.theme.TileYellowLight

@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đã hiểu")
            }
        },
        title = {
            Text("Luật chơi & Ý nghĩa màu sắc", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Đoán từ tiếng Việt bí mật trong 6 lượt thử. Sau mỗi lượt đoán, màu của từng ô chữ sẽ thay đổi:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

                RuleItem(
                    color = TileGreenLight,
                    title = "Xanh lá (🟩)",
                    description = "Đúng chữ cái, đúng dấu thanh và đúng vị trí."
                )

                RuleItem(
                    color = TileYellowLight,
                    title = "Màu vàng (🟨)",
                    description = "Chữ cái có trong từ (đúng dấu), nhưng nằm ở vị trí khác."
                )

                RuleItem(
                    color = TileBlueLight,
                    title = "Xanh dương (🟦)",
                    description = "Đúng nguyên âm cơ sở (a, ă, â, e, ê, i, o, ô, ơ, u, ư, y) nhưng SAI DẤU THANH (sắc, huyền, hỏi, ngã, nặng)."
                )

                RuleItem(
                    color = TileGrayLight,
                    title = "Màu xám (⬛)",
                    description = "Chữ cái không xuất hiện trong từ (hoặc đã vượt quá số lần xuất hiện)."
                )
            }
        }
    )
}

@Composable
private fun RuleItem(color: Color, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
