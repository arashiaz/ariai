package com.ariai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(raw: String, light: Boolean) {
    val ink = Ink
    val chip = Chip
    val link = Link
    val blocks = splitFences(raw)
    Column(Modifier.fillMaxWidth()) {
        blocks.forEach { b ->
            if (b.code) {
                SelectionContainer {
                    Text(
                        b.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = ink,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(chip)
                            .padding(10.dp)
                            .horizontalScroll(rememberScrollState())
                    )
                }
            } else {
                Text(inlineMd(b.text, chip, link), color = ink, fontSize = 15.sp)
            }
        }
    }
}

private data class Block(val text: String, val code: Boolean)

private fun splitFences(s: String): List<Block> {
    val out = mutableListOf<Block>()
    val parts = s.split("```")
    parts.forEachIndexed { i, p ->
        val t = if (i % 2 == 1) p.substringAfter('\n', p) else p
        if (t.isNotBlank()) out += Block(t.trimEnd(), i % 2 == 1)
    }
    if (out.isEmpty()) out += Block(s, false)
    return out
}

private fun inlineMd(s: String, chip: Color, link: Color) = buildAnnotatedString {
    val regex = Regex("`([^`]+)`|\\*\\*([^*]+)\\*\\*|\\*([^*]+)\\*|\\[([^]]+)]\\(([^)]+)\\)")
    var last = 0
    regex.findAll(s).forEach { m ->
        append(s.substring(last, m.range.first))
        when {
            m.groupValues[1].isNotEmpty() -> {
                pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = chip))
                append(m.groupValues[1]); pop()
            }
            m.groupValues[2].isNotEmpty() -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(m.groupValues[2]); pop()
            }
            m.groupValues[3].isNotEmpty() -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Medium))
                append(m.groupValues[3]); pop()
            }
            else -> {
                pushStyle(SpanStyle(color = link, textDecoration = TextDecoration.Underline))
                append(m.groupValues[4]); pop()
            }
        }
        last = m.range.last + 1
    }
    append(s.substring(last))
}
