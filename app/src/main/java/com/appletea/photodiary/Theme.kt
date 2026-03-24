package com.appletea.photodiary

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class DiaryTheme(
    val key: String,
    val displayName: String
) {
    Cream("cream", "Cream"),
    SoftPink("soft_pink", "Soft Pink"),
    SageGreen("sage_green", "Sage Green"),
    LightBlue("light_blue", "Light Blue"),
    Lavender("lavender", "Lavender");

    companion object {
        fun fromKey(key: String?): DiaryTheme = values().firstOrNull { it.key == key } ?: Cream
    }
}

private fun colorSchemeFor(theme: DiaryTheme): ColorScheme = when (theme) {
    DiaryTheme.Cream -> lightColorScheme(
        primary = Color(0xFF7C5A3A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE9D8C5),
        background = Color(0xFFF8F4EE),
        onBackground = Color(0xFF1F1B16),
        surface = Color(0xFFFFFAF4),
        onSurface = Color(0xFF1F1B16),
        surfaceVariant = Color(0xFFF3ECE2),
        onSurfaceVariant = Color(0xFF6E655C),
        secondaryContainer = Color(0xFFF0E7DB),
        tertiaryContainer = Color(0xFFE8DED0),
        outline = Color(0xFFC4B5A3)
    )

    DiaryTheme.SoftPink -> lightColorScheme(
        primary = Color(0xFF885A64),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF0D9DF),
        background = Color(0xFFFBF1F4),
        onBackground = Color(0xFF2B1D21),
        surface = Color(0xFFFFF8FA),
        onSurface = Color(0xFF2B1D21),
        surfaceVariant = Color(0xFFF4E5EA),
        onSurfaceVariant = Color(0xFF776067),
        secondaryContainer = Color(0xFFF5E8EC),
        tertiaryContainer = Color(0xFFEEDCE2),
        outline = Color(0xFFC7A9B0)
    )

    DiaryTheme.SageGreen -> lightColorScheme(
        primary = Color(0xFF5E715D),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDCE8D8),
        background = Color(0xFFF2F6F1),
        onBackground = Color(0xFF1D241D),
        surface = Color(0xFFF8FBF7),
        onSurface = Color(0xFF1D241D),
        surfaceVariant = Color(0xFFE4ECE2),
        onSurfaceVariant = Color(0xFF5F6B5E),
        secondaryContainer = Color(0xFFE7EFE4),
        tertiaryContainer = Color(0xFFDDE7DA),
        outline = Color(0xFFADB9AB)
    )

    DiaryTheme.LightBlue -> lightColorScheme(
        primary = Color(0xFF58758C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD8E8F1),
        background = Color(0xFFF0F6FA),
        onBackground = Color(0xFF1A242D),
        surface = Color(0xFFF8FCFF),
        onSurface = Color(0xFF1A242D),
        surfaceVariant = Color(0xFFE2EDF4),
        onSurfaceVariant = Color(0xFF5B6A76),
        secondaryContainer = Color(0xFFE7F0F5),
        tertiaryContainer = Color(0xFFDDE8EF),
        outline = Color(0xFFAABCCA)
    )

    DiaryTheme.Lavender -> lightColorScheme(
        primary = Color(0xFF6D6488),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0DCF0),
        background = Color(0xFFF4F2FA),
        onBackground = Color(0xFF231F2E),
        surface = Color(0xFFFAF8FF),
        onSurface = Color(0xFF231F2E),
        surfaceVariant = Color(0xFFEAE6F4),
        onSurfaceVariant = Color(0xFF645E76),
        secondaryContainer = Color(0xFFEDE8F5),
        tertiaryContainer = Color(0xFFE2DDED),
        outline = Color(0xFFB5ADC8)
    )
}

private val DiaryTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.15).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    )
)

@Composable
fun PhotoDiaryTheme(
    theme: DiaryTheme = DiaryTheme.Cream,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(theme),
        typography = DiaryTypography,
        content = content
    )
}
