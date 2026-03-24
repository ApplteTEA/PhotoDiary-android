package com.appletea.photodiary

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
        background = Color(0xFFF8F4EE),
        onBackground = Color(0xFF1F1B16),
        surface = Color(0xFFFFFAF4),
        onSurface = Color(0xFF1F1B16),
        surfaceVariant = Color(0xFFF3ECE2),
        onSurfaceVariant = Color(0xFF6E655C)
    )

    DiaryTheme.SoftPink -> lightColorScheme(
        primary = Color(0xFF885A64),
        onPrimary = Color.White,
        background = Color(0xFFFBF1F4),
        onBackground = Color(0xFF2B1D21),
        surface = Color(0xFFFFF8FA),
        onSurface = Color(0xFF2B1D21),
        surfaceVariant = Color(0xFFF4E5EA),
        onSurfaceVariant = Color(0xFF776067)
    )

    DiaryTheme.SageGreen -> lightColorScheme(
        primary = Color(0xFF5E715D),
        onPrimary = Color.White,
        background = Color(0xFFF2F6F1),
        onBackground = Color(0xFF1D241D),
        surface = Color(0xFFF8FBF7),
        onSurface = Color(0xFF1D241D),
        surfaceVariant = Color(0xFFE4ECE2),
        onSurfaceVariant = Color(0xFF5F6B5E)
    )

    DiaryTheme.LightBlue -> lightColorScheme(
        primary = Color(0xFF58758C),
        onPrimary = Color.White,
        background = Color(0xFFF0F6FA),
        onBackground = Color(0xFF1A242D),
        surface = Color(0xFFF8FCFF),
        onSurface = Color(0xFF1A242D),
        surfaceVariant = Color(0xFFE2EDF4),
        onSurfaceVariant = Color(0xFF5B6A76)
    )

    DiaryTheme.Lavender -> lightColorScheme(
        primary = Color(0xFF6D6488),
        onPrimary = Color.White,
        background = Color(0xFFF4F2FA),
        onBackground = Color(0xFF231F2E),
        surface = Color(0xFFFAF8FF),
        onSurface = Color(0xFF231F2E),
        surfaceVariant = Color(0xFFEAE6F4),
        onSurfaceVariant = Color(0xFF645E76)
    )
}

@Composable
fun PhotoDiaryTheme(
    theme: DiaryTheme = DiaryTheme.Cream,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(theme),
        content = content
    )
}
