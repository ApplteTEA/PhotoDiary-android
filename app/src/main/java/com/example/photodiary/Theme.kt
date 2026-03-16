package com.example.photodiary

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF7C5A3A),
    onPrimary = Color.White,
    background = Color(0xFFF8F4EE),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFFAF4),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFF3ECE2),
    onSurfaceVariant = Color(0xFF6E655C)
)

@Composable
fun PhotoDiaryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
