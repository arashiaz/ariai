package com.ariai.app.net

/** Pure SSE framing helper. JSON/provider-specific parsing stays outside this class. */
internal object SseFrames {
    fun <T> consume(lines: Sequence<String>, parse: (String) -> T, onPiece: (T) -> Unit): Boolean {
        var sawDone = false
        val fallback = StringBuilder()
        for (line in lines) {
            when {
                line.startsWith("data:") -> {
                    val data = line.removePrefix("data:").trim()
                    if (data == "[DONE]") { sawDone = true; break }
                    runCatching { parse(data) }.getOrNull()?.let(onPiece)
                }
                line.isNotBlank() && line.startsWith("{") -> fallback.append(line)
            }
        }
        if (!sawDone && fallback.isNotEmpty()) {
            runCatching { parse(fallback.toString()) }.getOrNull()?.let(onPiece)
        }
        return sawDone
    }
}