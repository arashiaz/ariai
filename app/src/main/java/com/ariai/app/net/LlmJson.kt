package com.ariai.app.net

import org.json.JSONObject

/** Pure JSON parsing helpers shared by the network client and unit tests. */
internal object LlmJson {
    fun parseModelIds(body: String): List<String> {
        val json = JSONObject(body)
        val data = json.optJSONArray("data") ?: json.optJSONArray("models") ?: return emptyList()
        return (0 until data.length()).map { i ->
            val o = data.getJSONObject(i)
            o.optString("id").ifBlank { o.optString("name") }
                .removePrefix("models/")
        }.filter { it.isNotBlank() }.distinct().sorted()
    }
}