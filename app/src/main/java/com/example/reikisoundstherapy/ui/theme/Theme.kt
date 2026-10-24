package com.example.reikisoundstherapy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Force Light Theme Color Palette (Always Day Mode)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF65589E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8E2FF),
    onPrimaryContainer = Color(0xFF201352),
    secondary = Color(0xFF43644E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCEADF),
    onSecondaryContainer = Color(0xFF00210E),
    background = Color(0xFFFAF7FC),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF2ECF7),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun ReikiSoundsTherapyTheme(
    darkTheme: Boolean = false, // Forced Light Mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
