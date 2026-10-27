package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantDarkPrimary,
    onPrimary = ElegantDarkOnPrimary,
    primaryContainer = ElegantDarkPrimaryContainer,
    onPrimaryContainer = ElegantDarkOnPrimaryContainer,
    secondary = ElegantDarkSecondary,
    onSecondary = ElegantDarkOnSecondary,
    secondaryContainer = ElegantDarkSecondaryContainer,
    onSecondaryContainer = ElegantDarkOnSecondaryContainer,
    tertiary = ElegantDarkTertiary,
    onTertiary = ElegantDarkOnTertiary,
    tertiaryContainer = ElegantDarkTertiaryContainer,
    onTertiaryContainer = ElegantDarkOnTertiaryContainer,
    background = ElegantDarkBackground,
    onBackground = ElegantDarkOnSurface,
    surface = ElegantDarkSurface,
    onSurface = ElegantDarkOnSurface,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantDarkOnSurfaceVariant,
    outline = ElegantDarkOutline,
    outlineVariant = ElegantDarkOutlineVariant,
    error = ElegantDarkError,
    onError = ElegantDarkOnError,
    errorContainer = ElegantDarkErrorContainer,
    onErrorContainer = ElegantDarkOnErrorContainer
)

@Composable
fun AutoContactSaverTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ElegantDarkColorScheme,
        typography = Typography,
        content = content
    )
}

