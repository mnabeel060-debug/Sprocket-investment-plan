package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = NavySurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = AccentCyan,
    tertiary = AccentGold,
    onTertiary = Color.Black,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    error = LossRed,
    onError = Color.White
)

@Composable
fun ForexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
