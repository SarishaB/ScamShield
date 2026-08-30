package com.akslabs.circletosearch.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ShieldViolet,
    onPrimary = Color(0xFF16052F),
    primaryContainer = Color(0xFF3C176F),
    onPrimaryContainer = ShieldVioletBright,
    secondary = ShieldBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF20254A),
    onSecondaryContainer = Color(0xFFD9DEFF),
    tertiary = ShieldCyan,
    onTertiary = Color(0xFF001E26),
    background = ShieldBackground,
    onBackground = ShieldText,
    surface = ShieldSurface,
    onSurface = ShieldText,
    surfaceVariant = ShieldSurfaceRaised,
    onSurfaceVariant = ShieldMuted,
    outline = Color(0xFF5A4C70),
    error = ShieldRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = ShieldViolet,
    onPrimary = Color.White,
    secondary = ShieldBlue,
    tertiary = ShieldCyan,
    background = ShieldBackground,
    onBackground = ShieldText,
    surface = ShieldSurface,
    onSurface = ShieldText
)

private const val MAX_FONT_SCALE = 1.3f

@Composable
fun CircleToSearchTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ShieldBackground.toArgb()
            window.navigationBarColor = ShieldBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    val currentDensity = LocalDensity.current
    val cappedDensity = if (currentDensity.fontScale > MAX_FONT_SCALE) {
        Density(density = currentDensity.density, fontScale = MAX_FONT_SCALE)
    } else currentDensity

    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
