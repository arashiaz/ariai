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
    page = Color(0xFFF3F4F8),
    card = Color(0xFFFFFFFF),
    ink = Color(0xFF1C1C1E),
    mute = Color(0xFF8E8E93),
    accent = Color(0xFF3D5AFE),
    accentSoft = Color(0xFFE8EEFF),
    chip = Color(0xFFF0F1F5),
    dangerBg = Color(0xFFFFE8E6),
    dangerInk = Color(0xFF5C2B29),
    link = Color(0xFF3F51B5),
    section = Color(0xFF5C6BC0)
)

private val DarkPal = AriPalette(
    page = Color(0xFF0E1116),
    card = Color(0xFF1A1F27),
    ink = Color(0xFFF2F4F8),
    mute = Color(0xFF9AA3B2),
    accent = Color(0xFF8AB4FF),
    accentSoft = Color(0xFF243044),
    chip = Color(0xFF242A33),
    dangerBg = Color(0xFF3A1F1C),
    dangerInk = Color(0xFFFFB4AB),
    link = Color(0xFF9FA8DA),
    section = Color(0xFF9FA8DA)
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
                primary = pal.accent,
                background = pal.page,
                surface = pal.card,
                onBackground = pal.ink,
                onSurface = pal.ink
            ) else lightColorScheme(
                primary = pal.accent,
                background = pal.page,
                surface = pal.card,
                onBackground = pal.ink,
                onSurface = pal.ink
            ),
            content = content
        )
    }
}
