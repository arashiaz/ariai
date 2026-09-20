package com.ariai.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorsTest {
    @Test fun keyRejected() {
        assertTrue(Errors.friendly(IllegalStateException("HTTP 401")).contains("API key"))
    }

    @Test fun rateLimit() {
        assertTrue(Errors.friendly(RuntimeException("429 too many")).contains("Too many"))
    }

    @Test fun offline() {
        assertTrue(Errors.friendly(java.net.UnknownHostException("api.openai.com")).contains("internet"))
    }

    @Test fun redactBearer() {
        val out = Errors.redact("Authorization: Bearer sk-abcdefghijklmnop")
        assertFalse(out.contains("sk-abcdefgh"))
        assertTrue(out.contains("***"))
    }

    @Test fun emptyMessage() {
        val s = Errors.friendly(Exception(""))
        assertTrue(s.contains("Exception") || s.contains("failed"))
    }
}
