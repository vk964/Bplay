package com.bel9ja.boardclub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Bel9jaColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = Navy950,
    secondary = Yellow,
    onSecondary = Navy950,
    tertiary = Green,
    onTertiary = Navy950,
    background = Navy950,
    onBackground = Cream,
    surface = Navy900,
    onSurface = Cream,
    surfaceVariant = Navy800,
    onSurfaceVariant = Muted,
    outline = Navy700,
    error = RedSoft,
    onError = Cream
)

@Composable
fun Bel9jaBoardClubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Bel9jaColorScheme,
        typography = Bel9jaTypography,
        content = content
    )
}
