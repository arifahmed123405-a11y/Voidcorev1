package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VoidDarkColorScheme = darkColorScheme(
    primary = VoidCyanPrimary,
    onPrimary = VoidDeepNavy,
    primaryContainer = VoidSurfaceCardElevated,
    onPrimaryContainer = VoidCyanPrimary,
    secondary = VoidVioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = VoidSurfaceCard,
    onSecondaryContainer = VoidLavender,
    tertiary = VoidWarmPeach,
    onTertiary = VoidDeepNavy,
    background = VoidDeepNavy,
    onBackground = TextPrimary,
    surface = VoidSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = VoidSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = VoidBorderGlow,
    error = VoidError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to VoidCore immersive dark aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VoidDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun VoidCoreTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}
