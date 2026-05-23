package com.example.colorit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colorful kid-friendly light color scheme
private val LightColorScheme = lightColorScheme(
    primary = AccentPurple,
    secondary = AccentPink,
    tertiary = AccentBlue,
    background = OffWhite,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = TextDark,
    onSurface = TextDark,
    primaryContainer = PastelPurple,
    secondaryContainer = PastelPink,
    tertiaryContainer = PastelBlue
)

// Glow-friendly colorful dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    secondary = NeonCyan,
    tertiary = NeonYellow,
    background = Color(0xFF120024), // Rich, glowing night dark violet
    surface = Color(0xFF220E3E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = AccentPurple,
    secondaryContainer = AccentPink
)

@Composable
fun ColorItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}