package com.example.pocketbudget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Green40,
    primaryContainer = GreenContainerLight,
    onPrimaryContainer = GreenContainerDark,
    inversePrimary = Green80,
    secondary = Slate40,
    secondaryContainer = Color(0xFFCFE9DD),
    onSecondaryContainer = Color(0xFF0B3B2C),
    background = Color(0xFFF5FAF7),
    surface = Color(0xFFF5FAF7),
    surfaceContainer = Color(0xFFE9F1ED),
    surfaceContainerHigh = Color(0xFFE4EDE8),
    surfaceContainerHighest = Color(0xFFDFE9E4),
)

private val DarkColors = darkColorScheme(
    primary = Green80,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenContainerLight,
    inversePrimary = Green40,
    secondary = Slate80,
    secondaryContainer = Color(0xFF2F4A3F),
    onSecondaryContainer = Color(0xFFCFE9DD),
    background = Color(0xFF0F1513),
    surface = Color(0xFF0F1513),
    surfaceContainer = Color(0xFF1B2320),
    surfaceContainerHigh = Color(0xFF222B27),
    surfaceContainerHighest = Color(0xFF29322E),
)

@Composable
fun PocketBudgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
