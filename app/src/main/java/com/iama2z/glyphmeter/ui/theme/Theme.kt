package com.iama2z.glyphmeter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---- Nothing-inspired dark palette ----
// Nothing's aesthetic is black, white, and red accents.

/** Background — pure black like the Nothing Phone's back panel. */
private val NothingBlack = Color(0xFF000000)

/** Surface — a slightly lighter card/surface colour. */
private val NothingSurface = Color(0xFF121212)

/** Primary accent — Nothing's signature red/orange dot LED colour. */
private val NothingAccent = Color(0xFFFF3B30)

/** On-surface text — off-white for readability on dark backgrounds. */
private val NothingWhite = Color(0xFFF5F5F5)

/** Muted secondary text colour. */
private val NothingGrey = Color(0xFF888888)

private val DarkColorScheme = darkColorScheme(
    primary = NothingAccent,
    onPrimary = NothingBlack,
    background = NothingBlack,
    onBackground = NothingWhite,
    surface = NothingSurface,
    onSurface = NothingWhite,
    secondary = NothingGrey,
    onSecondary = NothingBlack
)

/**
 * The app's Compose theme.
 *
 * Always uses the dark colour scheme — Nothing devices are designed around
 * black surfaces and white/red accents.
 */
@Composable
fun GlyphDecibelMeterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
