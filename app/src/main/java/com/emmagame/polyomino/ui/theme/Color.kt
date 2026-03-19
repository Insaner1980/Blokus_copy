package com.emmagame.polyomino.ui.theme

import androidx.compose.ui.graphics.Color

// Player colors
data class PlayerColor(
    val base: Color,
    val dark: Color,
    val highlight: Color,
    val ghost: Color
)

val PlayerColors = listOf(
    PlayerColor(
        base = Color(0xFF4338CA),      // Indigo
        dark = Color(0xFF312E81),
        highlight = Color(0xFF6366F1),
        ghost = Color(0x4D4338CA)
    ),
    PlayerColor(
        base = Color(0xFFE88C0A),      // Amber
        dark = Color(0xFFA16207),
        highlight = Color(0xFFF5B040),
        ghost = Color(0x4DE88C0A)
    ),
    PlayerColor(
        base = Color(0xFFE8513D),      // Coral
        dark = Color(0xFF991B1B),
        highlight = Color(0xFFF08070),
        ghost = Color(0x4DE8513D)
    ),
    PlayerColor(
        base = Color(0xFF0D9488),      // Teal
        dark = Color(0xFF134E4A),
        highlight = Color(0xFF2DD4BF),
        ghost = Color(0x4D0D9488)
    )
)

// Surface colors
val AppBackground = Color(0xFFE4E4E8)
val BoardCellGap = Color(0xFFDCDCE0)
val BoardCellSurface = Color(0xFFFAFAFA)
val BoardFrame = Color(0xFF2C2C30)
val CardSurface = Color(0xFFFFFFFF)

// Text colors
val TextPrimary = Color(0xFF1A1A1E)
val TextSecondary = Color(0xFF4A4A52)
val TextMuted = Color(0xFF8A8A92)
val TextOnPlayer = Color(0xFFFFFFFF)

// Semantic colors
val AccentColor = Color(0xFF4338CA)
val DangerColor = Color(0xFFE8513D)
val SuccessColor = Color(0xFF0D9488)
val WarningColor = Color(0xFFE88C0A)

// Ghost invalid
val GhostInvalidFill = Color(0x40E8513D)
val GhostInvalidBorder = Color(0xFFE8513D)
