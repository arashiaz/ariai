package com.ariai.app.net

/** Pure DuckDuckGo HTML result extraction, kept separate from networking for testability. */
internal object SearchParser {
    private val titlePattern = Regex(
        """class="result__a"[^>]*>(.*?)</a>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    private val snippetPattern = Regex(
        """class="result__snippet"[^>]*>(.*?)</(?:a|td|div)>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    private val tagPattern = Regex("<[^>]+>")

    fun parse(body: String, limit: Int = 5): String {
        if (body.isBlank() || limit <= 0) return ""
        val titles = titlePattern.findAll(body)
            .map { match -> clean(match.groupValues[1]) }
            .filter(String::isNotBlank)
            .take(limit)
            .toList()
        val snippets = snippetPattern.findAll(body)
            .map { match -> clean(match.groupValues[1]) }
            .take(limit)
            .toList()

        return titles.mapIndexed { index, title ->
            val snippet = snippets.getOrNull(index).orEmpty()
            if (snippet.isBlank()) "- $title" else "- $title: $snippet"
        }.joinToString("\n")
    }

    private fun clean(value: String): String =
        value.replace(tagPattern, "")
            .replace("&amp;", "&", ignoreCase = true)
            .replace("&quot;", "\"", ignoreCase = true)
            .replace("&#39;", "'", ignoreCase = true)
            .replace("&lt;", "<", ignoreCase = true)
            .replace("&gt;", ">", ignoreCase = true)
            .replace(Regex("\\s+"), " ")
            .trim()
}
