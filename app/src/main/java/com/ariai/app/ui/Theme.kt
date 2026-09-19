package com.ariai.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Page = Color(0xFFF3F4F8)
val CardBg = Color(0xFFFFFFFF)
val Ink = Color(0xFF1C1C1E)
val Mute = Color(0xFF8E8E93)
val Accent = Color(0xFF3D5AFE)
val AccentSoft = Color(0xFFE8EEFF)
val Chip = Color(0xFFF0F1F5)
val DangerBg = Color(0xFFFFE8E6)
val DangerInk = Color(0xFF5C2B29)
val Link = Color(0xFF3F51B5)
val Section = Color(0xFF5C6BC0)

@Composable
fun AriAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Accent,
            background = Page,
            surface = CardBg,
            onBackground = Ink,
            onSurface = Ink
        ),
        content = content
    )
}
