package dev.tapngo.app.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Color.dynamicColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) this.copy(alpha = 0.8f) else this
}

@Composable
fun dynamicColor(lightColor: () -> Color, darkColor: () -> Color): Color {
    return if (isSystemInDarkTheme()) darkColor() else lightColor()
}