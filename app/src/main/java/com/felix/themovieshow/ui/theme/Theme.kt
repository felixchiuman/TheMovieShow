package com.felix.themovieshow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BackgroundDark = Color(0xFF190D22)
val SurfaceDark = Color(0xFF2A1830)
val AccentRed = Color(0xFFE63946)
val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB0A8B9)

private val DarkColors = darkColorScheme(
    background = BackgroundDark,
    surface = SurfaceDark,
    primary = AccentRed,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun TheMovieShowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}