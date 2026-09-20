package com.ariai.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AriAi glass visual system.
 *
 * Intentionally opt-in: existing screens keep their current appearance until the
 * product is ready to switch the shell over. This keeps the visual experiment
 * reversible while we harden the underlying UX.
 */
object AriGlass {
    val Background = Color(0xFFF4F3FA)
    val Surface = Color(0xCCFFFFFF)
    val SurfaceStrong = Color(0xE6FFFFFF)
    val Stroke = Color(0x33FFFFFF)
    val StrokeDark = Color(0x18000000)
    val Ink = Color(0xFF17151F)
    val Muted = Color(0xFF706C7C)
    val Accent = Color(0xFF6C5CE7)
    val Accent2 = Color(0xFF8B7CF6)
    val AccentSoft = Color(0x186C5CE7)
    val Highlight = Color(0x99FFFFFF)
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    radius: Int = 22,
    emphasized: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(radius.dp)
    val surface = if (emphasized) AriGlass.SurfaceStrong else AriGlass.Surface

    var m = modifier
        .clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    surface,
                    Color.White.copy(alpha = if (emphasized) .76f else .62f)
                )
            )
        )
        .border(BorderStroke(1.dp, AriGlass.StrokeDark), shape)
        .border(BorderStroke(1.dp, AriGlass.Stroke), shape)
        .padding(16.dp)

    if (onClick != null) m = m.clickable(onClick = onClick)

    Box(m) { content() }
}

@Composable
fun GlassModeCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassSurface(modifier, radius = 20, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(AriGlass.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = AriGlass.Accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.size(11.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = AriGlass.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = AriGlass.Muted, fontSize = 11.sp)
            }
            Text("›", color = AriGlass.Muted, fontSize = 20.sp)
        }
    }
}
