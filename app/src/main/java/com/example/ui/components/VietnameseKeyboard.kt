package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.TileColor
import com.example.normalization.Tone
import com.example.ui.theme.TileBlueDark
import com.example.ui.theme.TileBlueLight
import com.example.ui.theme.TileGrayDark
import com.example.ui.theme.TileGrayLight
import com.example.ui.theme.TileGreenDark
import com.example.ui.theme.TileGreenLight
import com.example.ui.theme.TileYellowDark
import com.example.ui.theme.TileYellowLight

@Composable
fun VietnameseKeyboard(
    keyboardColors: Map<Char, TileColor>,
    onKeyPress: (Char) -> Unit,
    onTonePress: (Tone) -> Unit,
    onDeletePress: () -> Unit,
    onSubmitPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Tone actions bar
    val tones = listOf(
        Pair("´ Sắc", Tone.ACUTE),
        Pair("` Huyền", Tone.GRAVE),
        Pair("? Hỏi", Tone.HOOK_ABOVE),
        Pair("~ Ngã", Tone.TILDE),
        Pair(". Nặng", Tone.DOT_BELOW)
    )

    // Vietnamese special keys row
    val specialVowels = listOf('ă', 'â', 'ê', 'ô', 'ơ', 'ư', 'đ')

    // Standard QWERTY rows
    val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
    val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
    val row3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            // 1. Tones bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tones.forEach { (label, tone) ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onTonePress(tone) }
                            .testTag("key_tone_${tone.name}")
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 2. Vietnamese vowels & Đ
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                specialVowels.forEach { char ->
                    KeyButton(
                        label = char.uppercaseChar().toString(),
                        color = keyboardColors[char],
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(char) }
                    )
                }
            }

            // 3. QWERTY Row 1
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row1.forEach { char ->
                    KeyButton(
                        label = char.uppercaseChar().toString(),
                        color = keyboardColors[char],
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(char) }
                    )
                }
            }

            // 4. QWERTY Row 2
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                row2.forEach { char ->
                    KeyButton(
                        label = char.uppercaseChar().toString(),
                        color = keyboardColors[char],
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(char) }
                    )
                }
            }

            // 5. QWERTY Row 3 (with Enter and Backspace)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Enter / Submit button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onSubmitPress() }
                        .testTag("key_enter")
                ) {
                    Text(
                        text = "ĐOÁN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                row3.forEach { char ->
                    KeyButton(
                        label = char.uppercaseChar().toString(),
                        color = keyboardColors[char],
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(char) }
                    )
                }

                // Delete / Backspace button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1))
                        .clickable { onDeletePress() }
                        .testTag("key_backspace")
                ) {
                    Text(
                        text = "⌫",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    color: TileColor?,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when (color) {
        TileColor.GREEN -> if (isDark) TileGreenDark else TileGreenLight
        TileColor.YELLOW -> if (isDark) TileYellowDark else TileYellowLight
        TileColor.BLUE -> if (isDark) TileBlueDark else TileBlueLight
        TileColor.GRAY -> if (isDark) TileGrayDark else TileGrayLight
        null -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    }

    val textColor = when {
        color != null -> Color.White
        isDark -> Color.White
        else -> Color(0xFF0F172A)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable { onClick() }
            .testTag("key_$label")
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
