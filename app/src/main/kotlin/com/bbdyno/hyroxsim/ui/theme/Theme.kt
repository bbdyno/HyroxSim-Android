package com.bbdyno.hyroxsim.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** iOS DesignTokens.swift 에 대응하는 Compose 색상 토큰. */
object HyroxColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF0C0C0C)
    val Accent = Color(0xFFFFD700)          // gold
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFAAAAAA)
    val TextTertiary = Color(0xFF666666)

    val SegmentRun = Color(0xFF007AFF)       // systemBlue
    val SegmentRoxZone = Color(0xFFFF9500)   // systemOrange
    val SegmentStation = Color(0xFFFFD700)

    val DeltaOver = Color(0xFFFF3B30)
    val DeltaUnder = Color(0xFFFFD700)
}

private val HyroxDarkColorScheme = darkColorScheme(
    primary = HyroxColors.Accent,
    onPrimary = Color.Black,
    background = HyroxColors.Background,
    onBackground = HyroxColors.TextPrimary,
    surface = HyroxColors.Surface,
    onSurface = HyroxColors.TextPrimary,
)

@Composable
fun HyroxTheme(
    darkTheme: Boolean = true,  // always dark — matches iOS window.overrideUserInterfaceStyle = .dark
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HyroxDarkColorScheme,
        content = content,
    )
}
