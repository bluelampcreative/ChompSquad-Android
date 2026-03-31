package com.bluelampcreative.chompsquad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dynamic colour (Material You) is intentionally disabled — ChompSquad uses
// fixed brand colours and must not be overridden by the system wallpaper.

private val LightColorScheme =
    lightColorScheme(
        primary = brandGreen,
        onPrimary = Color.White,
        primaryContainer = brandGreenContainer,
        onPrimaryContainer = Color(0xFF082008),
        secondary = brandGreenMid,
        onSecondary = Color.White,
        secondaryContainer = brandGreenContainer,
        onSecondaryContainer = Color(0xFF082008),

        // Tertiary = amber, debug/developer UI only.
        tertiary = debugAmber,
        onTertiary = Color.White,
        tertiaryContainer = debugAmberContainer,
        onTertiaryContainer = debugAmberOn,
        error = errorRed,
        onError = Color.White,
        errorContainer = errorRedContainer,
        onErrorContainer = errorRedOn,
        background = neutralBackground,
        onBackground = neutralOnSurface,
        surface = neutralBackground,
        onSurface = neutralOnSurface,
        surfaceVariant = neutralSurfaceVariant,
        onSurfaceVariant = neutralOnSurfaceVar,
        outline = neutralOutline,
        outlineVariant = neutralOutlineVariant,
        scrim = Color.Black,
        inverseSurface = Color(0xFF2F312E),
        inverseOnSurface = Color(0xFFF0F1EB),
        inversePrimary = brandGreenLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = brandGreenLight,
        onPrimary = brandGreenOnDark,
        primaryContainer = brandGreenDark,
        onPrimaryContainer = brandGreenContainer,
        secondary = Color(0xFFA5D6A7),
        onSecondary = Color(0xFF1B3A1B),
        secondaryContainer = Color(0xFF2E7D32),
        onSecondaryContainer = Color(0xFFC8E6C9),
        tertiary = Color(0xFFFCD34D),
        onTertiary = Color(0xFF3B2000),
        tertiaryContainer = Color(0xFF7C3500),
        onTertiaryContainer = Color(0xFFFFDDB3),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = errorRedOnDark,
        background = darkBackground,
        onBackground = darkOnBackground,
        surface = darkBackground,
        onSurface = darkOnBackground,
        surfaceVariant = darkSurfaceVariant,
        onSurfaceVariant = neutralOutlineVariant,
        outline = darkOutline,
        outlineVariant = darkSurfaceVariant,
        scrim = Color.Black,
        inverseSurface = darkOnBackground,
        inverseOnSurface = darkBackground,
        inversePrimary = brandGreen,
    )

@Composable
fun ChompSquadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
  MaterialTheme(
      colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
      shapes = ChompShapes,
      typography = ChompTypography,
      content = content,
  )
}
