package com.ariai.app.net

import com.ariai.app.data.ChatMessage
import com.ariai.app.data.Provider
import com.ariai.app.data.RequestLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class LlmClient {
    @Volatile var lastLogs = listOf<RequestLog>()
        private set

    fun log(method: String, url: String, status: Int, body: String) {
        lastLogs = (listOf(RequestLog(System.currentTimeMillis(), method, url, status, body.take(2000))) + lastLogs).take(80)
    }

    fun base(p: Provider): String = p.baseUrl.trim().trimEnd('/')

    fun chat(p: Provider, model: String, messages: List<ChatMessage>, system: String?): String {
        val url = base(p) + "/chat/completions"
        val arr = JSONArray()
        if (!system.isNullOrBlank()) {
            arr.put(JSONObject().put("role", "system").put("content", system))
        }
        messages.forEach { m ->
            val role = when (m.role) {
                ChatMessage.Role.User -> "user"
                ChatMessage.Role.Assistant -> "assistant"
                ChatMessage.Role.System -> "system"
            }
            val content: Any = if (!m.imageBase64.isNullOrBlank()) {
                JSONArray()
                    .put(JSONObject().put("type", "text").put("text", m.text))
                    .put(
                        JSONObject().put("type", "image_url")
                            .put("image_url", JSONObject().put("url", "data:image/jpeg;base64,${m.imageBase64}"))
                    )
            } else m.text
            arr.put(JSONObject().put("role", role).put("content", content))
        }
        val payload = JSONObject()
            .put("model", model.ifBlank { p.model })
            .put("messages", arr)
            .put("stream", false)
            .toString()
        val (code, body) = post(url, p, payload)
        log("POST", url, code, body)
        if (code !in 200..299) throw IllegalStateException("HTTP $code: ${body.take(500)}")
        val json = JSONObject(body)
        val err = json.optJSONObject("error")
        if (err != null) throw IllegalStateException(err.optString("message", body.take(400)))
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .optString("content")
            .ifBlank { json.toString().take(800) }
    }

    fun listModels(p: Provider): List<String> {
        val url = base(p) + "/models"
        val (code, body) = get(url, p)
        log("GET", url, code, body)
        if (code !in 200..299) throw IllegalStateException("HTTP $code: ${body.take(400)}")
        val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
        return (0 until data.length()).map { data.getJSONObject(it).optString("id") }.filter { it.isNotBlank() }.sorted()
    }

    fun searchDuck(q: String): String {
        val url = "https://html.duckduckgo.com/html/?q=" + java.net.URLEncoder.encode(q, "UTF-8")
        val (code, body) = raw("GET", url, null, emptyMap())
        log("GET", url, code, body.take(400))
        if (code !in 200..299) return ""
        val titleList = Regex("class=\"result__a\"[^>]*>(.*?)</a>", RegexOption.IGNORE_CASE)
            .findAll(body).map { it.groupValues[1].replace(Regex("<[^>]+>"), "").trim() }.filter { it.isNotBlank() }.take(5).toList()
        val snipList = Regex("class=\"result__snippet\"[^>]*>(.*?)</(?:a|td|div)", RegexOption.IGNORE_CASE)
            .findAll(body).map { it.groupValues[1].replace(Regex("<[^>]+>"), "").trim() }.take(5).toList()
        return titleList.mapIndexed { i, t -> "- $t: ${snipList.getOrNull(i).orEmpty()}" }.joinToString("\n").ifBlank { "" }
    }

    private fun post(url: String, p: Provider, json: String): Pair<Int, String> {
        val headers = mutableMapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${p.apiKey}"
        )
        parseHeaders(p.headers).forEach { (k, v) -> headers[k] = v }
        return raw("POST", url, json, headers)
    }

    private fun get(url: String, p: Provider): Pair<Int, String> {
        val headers = mutableMapOf("Authorization" to "Bearer ${p.apiKey}")
        parseHeaders(p.headers).forEach { (k, v) -> headers[k] = v }
        return raw("GET", url, null, headers)
    }

    private fun parseHeaders(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.lines().mapNotNull { line ->
            val i = line.indexOf(':')
            if (i <= 0) null else line.substring(0, i).trim() to line.substring(i + 1).trim()
        }.toMap()
    }

    private fun raw(method: String, url: String, body: String?, headers: Map<String, String>): Pair<Int, String> {
        val c = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 30000
            readTimeout = 120000
            doInput = true
            instanceFollowRedirects = true
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
            if (body != null) {
                doOutput = true
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { it.write(body) }
            }
        }
        val code = c.responseCode
        val stream = if (code in 200..299) c.inputStream else c.errorStream
        val text = stream?.let { BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).readText() } ?: ""
        c.disconnect()
        return code to text
    }
}
