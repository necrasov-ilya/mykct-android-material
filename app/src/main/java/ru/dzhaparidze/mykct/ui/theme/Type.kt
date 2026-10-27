package ru.dzhaparidze.mykct.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.dzhaparidze.mykct.R

val OpenRunde = FontFamily(
    Font(R.font.open_runde_regular, FontWeight.Normal),
    Font(R.font.open_runde_medium, FontWeight.Medium),
    Font(R.font.open_runde_semibold, FontWeight.SemiBold),
    Font(R.font.open_runde_bold, FontWeight.Bold),
)

private val DefaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = OpenRunde),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = OpenRunde),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = OpenRunde),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = OpenRunde),
    headlineMedium = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.2).sp),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = OpenRunde),
    bodyLarge = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp),
    labelMedium = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 15.sp),
    labelSmall = TextStyle(fontFamily = OpenRunde, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)
