package com.akslabs.circletosearch.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
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

private val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = ShieldPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF27104F),
    secondary = ShieldBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE2FF),
    onSecondaryContainer = Color(0xFF111A43),
    tertiary = Color(0xFF006579),
    onTertiary = Color.White,
    background = Color(0xFFF9F7FC),
    onBackground = Color(0xFF1B191F),
    surface = Color(0xFFF9F7FC),
    onSurface = Color(0xFF1B191F),
    surfaceVariant = Color(0xFFE8E0EC),
    onSurfaceVariant = Color(0xFF4A454D),
    error = ShieldRed,
    onError = Color.White
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
