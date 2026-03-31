package com.bluelampcreative.chompsquad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Dynamic colour (Material You) is intentionally disabled — ChompSquad uses
// fixed brand colours and must not be overridden by the system wallpaper.

private val LightColorScheme =
    lightColorScheme(
        primary = brandGreen,
        onPrimary = Color.White,
        primaryContainer = brandGreenContainer,
        onPrimaryContainer = brandGreenUltraDark,
        secondary = brandGreenMid,
        onSecondary = Color.White,
        secondaryContainer = brandGreenContainer,
        onSecondaryContainer = brandGreenUltraDark,

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
        inverseSurface = lightInverseSurface,
        inverseOnSurface = lightInverseOnSurface,
        inversePrimary = brandGreenLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = brandGreenLight,
        onPrimary = brandGreenOnDark,
        primaryContainer = brandGreenDark,
        onPrimaryContainer = brandGreenContainer,
        secondary = darkSecondary,
        onSecondary = darkOnSecondary,
        secondaryContainer = darkSecondaryContainer,
        onSecondaryContainer = darkOnSecondaryContainer,
        tertiary = darkTertiary,
        onTertiary = darkOnTertiary,
        tertiaryContainer = darkTertiaryContainer,
        onTertiaryContainer = darkOnTertiaryContainer,
        error = darkError,
        onError = darkOnError,
        errorContainer = darkErrorContainer,
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
