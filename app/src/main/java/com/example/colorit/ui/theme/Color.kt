package com.example.colorit.ui.theme

import androidx.compose.ui.graphics.Color

// Cozy Pink Theme Palette (Keep for legacy reference/fallback if needed)
val CozyRose = Color(0xFFFF85A1)
val CozyBlush = Color(0xFFFFC2D1)
val CozyBlushLight = Color(0xFFFFE5EC)
val CozyDeepRose = Color(0xFFB93C5D)
val CozyCreamBackground = Color(0xFFFFF9FA)

// Countryside Hand-Drawn Theme Palette (Nibbli-inspired)
val CountrySky = Color(0xFFBADFFF)             // Soft pastel blue sky
val CountryGrass = Color(0xFFB5EDB3)           // Bright pastel grass green
val CountryGrassDark = Color(0xFF8ED28A)       // Background rolling green hills
val CountryOutline = Color(0xFF1E351E)         // Thick dark outline forest green
val CardYellow = Color(0xFFFFFEEB)             // Retro cream card base
val ButtonOrange = Color(0xFFFF8A47)           // Warm orange action play button
val ButtonOrangePressed = Color(0xFFE66E29)    // Pressed orange state

// Texts
val TextDarkGreen = Color(0xFF1E351E)          // High contrast dark green text
val TextLightGreen = Color(0xFF6B9F67)         // Medium green subtext
val TextWhite = Color(0xFFFFFFFF)
val TextSkyBlue = Color(0xFF90A4CE)
val TextDark = Color(0xFF1E351E)               // Unified text dark

// Emojis / Accents
val GlowPurple = Color(0xFF9C4FFF)
val GlowCyan = Color(0xFF00E5FF)
val GlowPink = Color(0xFFFF2E93)
val GlowYellow = Color(0xFFFFD54F)
val GlowMint = Color(0xFF26A69A)

// Pastel Primary Palette
val PastelPink = Color(0xFFFFC6FF)
val PastelPinkDark = Color(0xFFFF9EF4)
val PastelBlue = Color(0xFFBDE0FE)
val PastelBlueDark = Color(0xFF90C2FE)
val PastelYellow = Color(0xFFFDFFB6)
val PastelPeach = Color(0xFFFFD1A9)
val PastelMint = Color(0xFFCAFFBF)
val PastelPurple = Color(0xFFE8AEFF)

// Neutrals
val BubbleBackground = Color(0xFFFFFDFD)
val SoftGray = Color(0xFFF7F0F2)
val CozyDeskSurface = Color(0xFFE8DFE1)

// Dark mode kids alternative
val DarkPastelPink = Color(0xFFDD99AA)
val DarkPastelBlue = Color(0xFF99BBDD)
val DarkPastelYellow = Color(0xFFDDDDAA)
val DarkPastelMint = Color(0xFF99DDA9)
val DarkPastelBackground = Color(0xFF090C1F)
val DarkPastelSurface = Color(0xFF151932)
val DarkTextLight = Color(0xFFFFF0F2)

// Unified list of 100+ colors sorted by luminance for app-wide use
val AppColorSpectrum = listOf(
    CozyRose, CozyBlush, CozyBlushLight, PastelYellow, PastelMint, PastelBlue, PastelPurple, PastelPeach,
    Color(0xFFFFB7B2), Color(0xFFFFDAC1), Color(0xFFE2F0CB), Color(0xFFB5EAD7), Color(0xFFC7CEEA),
    Color(0xFFFFADAD), Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF), Color(0xFF9BF6FF),
    Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFC6FF), Color(0xFFE8AEFF),
    Color(0xFFFF595E), Color(0xFFFF924C), Color(0xFFFFCA3A), Color(0xFF8AC926), Color(0xFF1982C4),
    Color(0xFF6A4C93), Color(0xFFFF3F8D), Color(0xFF00C4B4), Color(0xFF00F5D4), Color(0xFF7B2CBF),
    Color(0xFFFF1493), Color(0xFF00FF00), Color(0xFF00FFFF), Color(0xFFFF4500), Color(0xFFFFD700),
    Color(0xFF39FF14), Color(0xFF4CC9F0), Color(0xFFF72585), Color(0xFF7209B7), Color(0xFF3F37C9),
    Color(0xFFDDA15E), Color(0xFFBC6C25), Color(0xFF8C5E58), Color(0xFF606C38), Color(0xFF283618),
    Color(0xFF4A3B32), Color(0xFFE29578), Color(0xFFF1FAEE), Color(0xFFA8DADC), Color(0xFF457B9D),
    Color(0xFFD8E2DC), Color(0xFFFFCAD4), Color(0xFFF4ACB7), Color(0xFF9E2A2B), Color(0xFFE09F3E),
    Color(0xFF9A8C98), Color(0xFFC9ADA7), Color(0xFFF2E9E1), Color(0xFF5E503F), Color(0xFF0A0908),
    Color(0xFF3D0C11), Color(0xFF0F4C5C), Color(0xFF1D3557), Color(0xFF4E2C5A), Color(0xFF1A5F7A),
    Color(0xFF57CC99), Color(0xFF38A3A5), Color(0xFF90E0EF), Color(0xFF0096C7), Color(0xFF03045E),
    Color(0xFF10002B), Color(0xFF240046), Color(0xFF3C096C), Color(0xFF5A189A), Color(0xFF7B2CBF),
    Color(0xFF9D4EDD), Color(0xFFC77DFF), Color(0xFFE0AAFF), Color(0xFF0F0F0F), Color(0xFF1B4965),
    Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Cyan, Color.Magenta,
    Color(0xFFFFA500), Color(0xFF800080), Color(0xFF008000), Color(0xFF000080), Color(0xFF800000),
    Color(0xFF808000), Color(0xFF008080), Color(0xFF808080), Color(0xFFC0C0C0), Color(0xFFE5E5E5),
    Color.Black, Color.White
).distinct()
 .sortedByDescending { 0.2126f * it.red + 0.7152f * it.green + 0.0722f * it.blue }

