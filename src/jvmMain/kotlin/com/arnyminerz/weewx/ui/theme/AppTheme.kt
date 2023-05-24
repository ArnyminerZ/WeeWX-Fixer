package com.arnyminerz.weewx.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.arnyminerz.weewx.ui.theme.fonts.jostFontFamily

private val TitlesTypography = TextStyle(fontFamily = jostFontFamily, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Medium)

private val AppTypography = Typography(
    titleLarge = TitlesTypography.copy(fontSize = 28.sp),
    titleMedium = TitlesTypography.copy(fontSize = 24.sp),
    titleSmall = TitlesTypography.copy(fontSize = 18.sp),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = AppTypography,
        content = content
    )
}
