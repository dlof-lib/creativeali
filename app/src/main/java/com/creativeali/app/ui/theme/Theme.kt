package com.creativeali.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CaGreenDark = Color(0xFF0F5C1E)
val CaGreen = Color(0xFF1E8A2E)
val CaLime = Color(0xFF8DC63F)
val CaGold = Color(0xFFF5B300)

private val LightColors = lightColorScheme(
    primary = CaGreen,
    onPrimary = Color.White,
    secondary = CaGold,
    onSecondary = Color.Black,
    tertiary = CaLime,
    background = Color(0xFFF7FBF6),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = CaLime,
    onPrimary = Color.Black,
    secondary = CaGold,
    onSecondary = Color.Black,
    tertiary = CaGreen,
    background = Color(0xFF0B140C),
    surface = Color(0xFF121D13),
)

@Composable
fun CreativeAliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
