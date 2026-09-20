package com.ariai.app.util

object Errors {
    fun friendly(e: Throwable): String {
        val m = (e.javaClass.simpleName + " " + e.message.orEmpty())
        return when {
            m.contains("401") || m.contains("invalid_api_key", true) || m.contains("authentication", true) ->
                "This API key was rejected. Open Providers and save a valid key."
            m.contains("403") -> "Access denied for this key or model."
            m.contains("429") -> "Too many requests. Wait a moment, or switch model."
            m.contains("404") -> "URL or model not found. Check the base URL and fetch models."
            m.contains("UnknownHost") || m.contains("Unable to resolve") || m.contains("Failed to connect") ->
                "No internet, or the base URL is wrong."
            m.contains("timeout", true) || m.contains("timed out", true) -> "Timed out. Stop and try again."
            m.contains("Canceled") || m.contains("cancel", true) -> "Stopped."
            else -> m.take(160).ifBlank { "Request failed. Check the provider and try again." }
        }
    }

    fun redact(s: String): String =
        s.replace(
            Regex("(?i)(api[_-]?key|authorization|bearer|x-api-key|x-goog-api-key)\\s*[:=]\\s*\\\\"?[^,;\\\\s}\\\\"']+"),
            "$1=***"
        )
            .replace(
                Regex("(?i)(\\"(?:api[_-]?key|authorization|x-api-key|x-goog-api-key)\\"\\s*:\\s*\\")[^\\"]*(\\")"),
                "$1***$2"
            )
            .replace(Regex("(?i)([?&](?:api[_-]?key|key|token|access_token|authorization)=)[^&\\s]+"), "$1***")
            .replace(Regex("sk-[A-Za-z0-9]{8,}"), "sk-***")
}
