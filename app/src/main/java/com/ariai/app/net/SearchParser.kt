package com.ariai.app.net

/** Pure DuckDuckGo HTML result extraction, kept separate from networking for testability. */
internal object SearchParser {
    private val resultPattern = Regex(
        """class="result__(a|snippet)"[^>]*>(.*?)</(?:a|td|div)>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    private val tagPattern = Regex("<[^>]+>")

    fun parse(body: String, limit: Int = 5): String {
        if (body.isBlank() || limit <= 0) return ""

        val results = mutableListOf<String>()
        var title: String? = null
        var snippet: String? = null

        for (match in resultPattern.findAll(body)) {
            val kind = match.groupValues[1].lowercase()
            val value = clean(match.groupValues[2])
            if (value.isBlank()) continue

            if (kind == "a") {
                if (title != null) {
                    results += format(title, snippet)
                    if (results.size >= limit) break
                }
                title = value
                snippet = null
            } else if (title != null) {
                snippet = value
            }
        }

        if (results.size < limit && title != null) {
            results += format(title, snippet)
        }

        return results.joinToString("\n")
    }

    private fun format(title: String, snippet: String?): String =
        if (snippet.isNullOrBlank()) "- $title" else "- $title: $snippet"

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
