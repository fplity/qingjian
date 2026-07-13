package com.fplity.recitemate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LeafGreen,
    secondary = WarmOrange,
    tertiary = WarmOrange,
    background = NightCream,
    surface = NightSurface,
    onBackground = Color(0xFFF2F1EC),
    onSurface = Color(0xFFF2F1EC)
)

private val LightColorScheme = lightColorScheme(
    primary = LeafGreen,
    secondary = WarmOrange,
    tertiary = WarmOrange,
    background = Cream,
    surface = Color.White,
    surfaceVariant = PaleGreen,
    onPrimary = Color.White,
    onBackground = Ink,
    onSurface = Ink
)

@Composable
fun ReciteMateTheme(
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
