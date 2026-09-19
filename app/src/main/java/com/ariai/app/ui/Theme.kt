package com.ariai.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AriPalette(
    val page: Color,
    val card: Color,
    val ink: Color,
    val mute: Color,
    val accent: Color,
    val accentSoft: Color,
    val chip: Color,
    val dangerBg: Color,
    val dangerInk: Color,
    val link: Color,
    val section: Color
)

private val LightPal = AriPalette(
    page = Color(0xFFE7F1EE),
    card = Color(0xF2FFFFFF),
    ink = Color(0xFF142421),
    mute = Color(0xFF5F746F),
    accent = Color(0xFF0F766E),
    accentSoft = Color(0xFFD1FAF4),
    chip = Color(0xFFDCEBE6),
    dangerBg = Color(0xFFFFE4DC),
    dangerInk = Color(0xFF9A3412),
    link = Color(0xFF0E7490),
    section = Color(0xFF0F766E)
)

private val DarkPal = AriPalette(
    page = Color(0xFF071310),
    card = Color(0xE6182A26),
    ink = Color(0xFFE7F4F0),
    mute = Color(0xFF8AA39C),
    accent = Color(0xFF2DD4BF),
    accentSoft = Color(0xFF12352F),
    chip = Color(0xFF1C2E2A),
    dangerBg = Color(0xFF3B1D16),
    dangerInk = Color(0xFFFDBA74),
    link = Color(0xFF5EEAD4),
    section = Color(0xFF5EEAD4)
)

val LocalAri = staticCompositionLocalOf { LightPal }

val Page: Color @Composable get() = LocalAri.current.page
val CardBg: Color @Composable get() = LocalAri.current.card
val Ink: Color @Composable get() = LocalAri.current.ink
val Mute: Color @Composable get() = LocalAri.current.mute
val Accent: Color @Composable get() = LocalAri.current.accent
val AccentSoft: Color @Composable get() = LocalAri.current.accentSoft
val Chip: Color @Composable get() = LocalAri.current.chip
val DangerBg: Color @Composable get() = LocalAri.current.dangerBg
val DangerInk: Color @Composable get() = LocalAri.current.dangerInk
val Link: Color @Composable get() = LocalAri.current.link
val Section: Color @Composable get() = LocalAri.current.section

@Composable
fun AriAiTheme(mode: String = "System", content: @Composable () -> Unit) {
    val dark = when (mode) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }
    val pal = if (dark) DarkPal else LightPal
    CompositionLocalProvider(LocalAri provides pal) {
        MaterialTheme(
            colorScheme = if (dark) darkColorScheme(
                primary = pal.accent, background = pal.page, surface = pal.card,
                onBackground = pal.ink, onSurface = pal.ink
            ) else lightColorScheme(
                primary = pal.accent, background = pal.page, surface = pal.card,
                onBackground = pal.ink, onSurface = pal.ink
            ),
            content = content
        )
    }
}
