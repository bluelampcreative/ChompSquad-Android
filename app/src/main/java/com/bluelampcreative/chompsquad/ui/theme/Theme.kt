package com.bluelampcreative.chompsquad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dynamic colour (Material You) is intentionally disabled — ChompSquad uses
// fixed brand colours and must not be overridden by the system wallpaper.

private val LightColorScheme = lightColorScheme(
    primary                = Brand_Green,
    onPrimary              = Color.White,
    primaryContainer       = Brand_Green_Container,
    onPrimaryContainer     = Color(0xFF082008),

    secondary              = Brand_Green_Mid,
    onSecondary            = Color.White,
    secondaryContainer     = Brand_Green_Container,
    onSecondaryContainer   = Color(0xFF082008),

    // Tertiary = amber, debug/developer UI only.
    tertiary               = Debug_Amber,
    onTertiary             = Color.White,
    tertiaryContainer      = Debug_Amber_Container,
    onTertiaryContainer    = Debug_Amber_On,

    error                  = Error_Red,
    onError                = Color.White,
    errorContainer         = Error_Red_Container,
    onErrorContainer       = Error_Red_On,

    background             = Neutral_Background,
    onBackground           = Neutral_OnSurface,
    surface                = Neutral_Background,
    onSurface              = Neutral_OnSurface,
    surfaceVariant         = Neutral_SurfaceVariant,
    onSurfaceVariant       = Neutral_OnSurfaceVar,

    outline                = Neutral_Outline,
    outlineVariant         = Neutral_OutlineVariant,
    scrim                  = Color.Black,
    inverseSurface         = Color(0xFF2F312E),
    inverseOnSurface       = Color(0xFFF0F1EB),
    inversePrimary         = Brand_Green_Light,
)

private val DarkColorScheme = darkColorScheme(
    primary                = Brand_Green_Light,
    onPrimary              = Brand_Green_OnDark,
    primaryContainer       = Brand_Green_Dark,
    onPrimaryContainer     = Brand_Green_Container,

    secondary              = Color(0xFFA5D6A7),
    onSecondary            = Color(0xFF1B3A1B),
    secondaryContainer     = Color(0xFF2E7D32),
    onSecondaryContainer   = Color(0xFFC8E6C9),

    tertiary               = Color(0xFFFCD34D),
    onTertiary             = Color(0xFF3B2000),
    tertiaryContainer      = Color(0xFF7C3500),
    onTertiaryContainer    = Color(0xFFFFDDB3),

    error                  = Color(0xFFF2B8B5),
    onError                = Color(0xFF601410),
    errorContainer         = Color(0xFF8C1D18),
    onErrorContainer       = Error_Red_Container,

    background             = Dark_Background,
    onBackground           = Dark_OnBackground,
    surface                = Dark_Background,
    onSurface              = Dark_OnBackground,
    surfaceVariant         = Dark_SurfaceVariant,
    onSurfaceVariant       = Neutral_OutlineVariant,

    outline                = Dark_Outline,
    outlineVariant         = Dark_SurfaceVariant,
    scrim                  = Color.Black,
    inverseSurface         = Dark_OnBackground,
    inverseOnSurface       = Dark_Background,
    inversePrimary         = Brand_Green,
)

@Composable
fun ChompSquadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        shapes      = ChompShapes,
        typography  = ChompTypography,
        content     = content,
    )
}
