package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkMineColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldSecondary,
    secondary = EmeraldGreen,
    onSecondary = Color.Black,
    tertiary = SapphireBlue,
    background = DarkMineBackground,
    onBackground = Color.White,
    surface = DarkMineSurface,
    onSurface = Color.White,
    surfaceVariant = DarkMineCard,
    onSurfaceVariant = Color(0xFFE0E0E0)
)

private val LightMineColorScheme = lightColorScheme(
    primary = GoldSecondary,
    onPrimary = Color.White,
    primaryContainer = GoldPrimary,
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    tertiary = SapphireBlue,
    background = LightMineBackground,
    onBackground = Color(0xFF1C1B1F),
    surface = LightMineSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = LightMineCard,
    onSurfaceVariant = Color(0xFF333333)
)

@Composable
fun PixelMineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isRetroCrt: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isRetroCrt -> darkColorScheme(
            primary = CRTGreenText,
            onPrimary = Color.Black,
            background = CRTCannotBackground,
            surface = CRTCannotBackground,
            onBackground = CRTGreenText,
            onSurface = CRTGreenText
        )
        darkTheme -> DarkMineColorScheme
        else -> LightMineColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

