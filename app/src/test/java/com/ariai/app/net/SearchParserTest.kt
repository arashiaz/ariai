package com.ariai.app.net

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchParserTest {
    @Test fun parsesTitlesAndSnippets() {
        val html = """
            <a class="result__a" href="#">First &amp; Result</a>
            <td class="result__snippet">A useful <b>summary</b>.</td>
            <a class="result__a" href="#">Second</a>
            <div class="result__snippet">Another &quot;summary&quot;.</div>
        """.trimIndent()

        assertEquals(
            """- First & Result: A useful summary.
- Second: Another "summary".""",
            SearchParser.parse(html)
        )
    }

    @Test fun omitsMissingSnippetWithoutDanglingColon() {
        val html = """<a class="result__a" href="#">Only title</a>"""
        assertEquals("- Only title", SearchParser.parse(html))
    }

    @Test fun respectsLimitAndEmptyInput() {
        val html = """
            <a class="result__a">One</a>
            <a class="result__a">Two</a>
            <a class="result__a">Three</a>
        """.trimIndent()

        assertEquals("- One\n- Two", SearchParser.parse(html, limit = 2))
        assertEquals("", SearchParser.parse("", limit = 5))
        assertEquals("", SearchParser.parse(html, limit = 0))
    }
}
