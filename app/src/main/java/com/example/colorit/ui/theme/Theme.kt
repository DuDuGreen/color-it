package com.example.colorit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = ButtonOrange,
    secondary = CountryGrass,
    tertiary = CardYellow,
    background = CountrySky,
    surface = CardYellow,
    onPrimary = Color.White,
    onSecondary = TextDarkGreen,
    onTertiary = TextDarkGreen,
    onBackground = TextDarkGreen,
    onSurface = TextDarkGreen
)

private val LightColorScheme = lightColorScheme(
    primary = ButtonOrange,
    secondary = CountryGrass,
    tertiary = CardYellow,
    background = CountrySky,
    surface = CardYellow,
    onPrimary = Color.White,
    onSecondary = TextDarkGreen,
    onTertiary = TextDarkGreen,
    onBackground = TextDarkGreen,
    onSurface = TextDarkGreen
)

// Extra rounded corners for playful, child-friendly appearance
val PlayfulShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun ColorItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Standardize both dark and light theme to the bright countryside theme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PlayfulShapes,
        content = content
    )
}
