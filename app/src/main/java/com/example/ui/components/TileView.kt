package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.TileColor
import com.example.ui.theme.TileBlueDark
import com.example.ui.theme.TileBlueLight
import com.example.ui.theme.TileEmptyBorderDark
import com.example.ui.theme.TileEmptyBorderLight
import com.example.ui.theme.TileFilledBorderDark
import com.example.ui.theme.TileFilledBorderLight
import com.example.ui.theme.TileGrayDark
import com.example.ui.theme.TileGrayLight
import com.example.ui.theme.TileGreenDark
import com.example.ui.theme.TileGreenLight
import com.example.ui.theme.TileYellowDark
import com.example.ui.theme.TileYellowLight

@Composable
fun TileView(
    letter: Char?,
    color: TileColor?,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    colorBlindMode: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    val targetBgColor = when (color) {
        TileColor.GREEN -> if (isDark) TileGreenDark else TileGreenLight
        TileColor.YELLOW -> if (isDark) TileYellowDark else TileYellowLight
        TileColor.BLUE -> if (isDark) TileBlueDark else TileBlueLight
        TileColor.GRAY -> if (isDark) TileGrayDark else TileGrayLight
        null -> Color.Transparent
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 350),
        label = "TileBgColor"
    )

    val textColor = when {
        color != null -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        color != null -> Color.Transparent
        letter != null -> if (isDark) TileFilledBorderDark else TileFilledBorderLight
        else -> if (isDark) TileEmptyBorderDark else TileEmptyBorderLight
    }

    val borderWidth = if (letter != null && color == null) 2.dp else 1.5.dp

    val symbol = if (colorBlindMode && color != null) {
        when (color) {
            TileColor.GREEN -> "✔"
            TileColor.YELLOW -> "↔"
            TileColor.BLUE -> "~"
            TileColor.GRAY -> "✖"
        }
    } else null

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(animatedBgColor)
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("tile_${letter ?: "empty"}_${color?.name ?: "empty"}")
    ) {
        if (letter != null) {
            Text(
                text = letter.uppercaseChar().toString(),
                fontSize = if (size < 48.dp) 20.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        if (symbol != null) {
            Text(
                text = symbol,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-3).dp, y = (-2).dp)
            )
        }
    }
}
