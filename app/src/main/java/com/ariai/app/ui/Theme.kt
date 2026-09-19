package com.ariai.app.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF0B0F14)
val Surface = Color(0xFF121821)
val Card = Color(0xFF1A2230)
val Accent = Color(0xFF7C5CFF)
val Accent2 = Color(0xFF3ECFCF)
val TextPri = Color(0xFFF2F4F8)
val TextSec = Color(0xFF9AA4B2)

private val scheme: ColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = Bg,
    onBackground = TextPri,
    surface = Surface,
    onSurface = TextPri,
    surfaceVariant = Card,
    onSurfaceVariant = TextSec,
    secondary = Accent2
)

@Composable
fun AriAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
