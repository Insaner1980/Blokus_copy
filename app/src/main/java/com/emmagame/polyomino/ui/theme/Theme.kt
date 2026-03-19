package com.emmagame.polyomino.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalPlayerColors = staticCompositionLocalOf { PlayerColors }

private val PolyominoColorScheme = lightColorScheme(
    primary = AccentColor,
    onPrimary = TextOnPlayer,
    secondary = SuccessColor,
    onSecondary = TextOnPlayer,
    tertiary = WarningColor,
    onTertiary = TextOnPlayer,
    error = DangerColor,
    onError = TextOnPlayer,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = BoardCellSurface,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
)

@Composable
fun PolyominoTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalPlayerColors provides PlayerColors
    ) {
        MaterialTheme(
            colorScheme = PolyominoColorScheme,
            typography = PolyominoTypography,
            shapes = PolyominoShapes,
            content = content
        )
    }
}
