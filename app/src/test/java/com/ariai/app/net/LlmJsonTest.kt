package com.ariai.app.net

import org.junit.Assert.assertEquals
import org.junit.Test

class LlmJsonTest {
    @Test fun parsesOpenAiModelsAndSorts() {
        val body = """{"data":[{"id":"gpt-z"},{"id":"gpt-a"},{"id":"gpt-a"}]}"""
        assertEquals(listOf("gpt-a", "gpt-z"), LlmJson.parseModelIds(body))
    }

    @Test fun parsesGeminiNamesAndRemovesPrefix() {
        val body = """{"models":[{"name":"models/gemini-2.0-flash"},{"name":"models/gemini-1.5-pro"}]}"""
        assertEquals(listOf("gemini-1.5-pro", "gemini-2.0-flash"), LlmJson.parseModelIds(body))
    }

    @Test fun ignoresBlankIds() {
        val body = """{"data":[{"id":""},{"id":"valid"}]}"""
        assertEquals(listOf("valid"), LlmJson.parseModelIds(body))
    }
}