package com.ariai.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AppStore(context: Context) {
    private val p = context.getSharedPreferences("ariai", Context.MODE_PRIVATE)

    fun bool(k: String, d: Boolean = false) = p.getBoolean(k, d)
    fun setBool(k: String, v: Boolean) = p.edit().putBoolean(k, v).apply()
    fun str(k: String, d: String = "") = p.getString(k, d) ?: d
    fun setStr(k: String, v: String) = p.edit().putString(k, v).apply()
    fun int(k: String, d: Int = 0) = p.getInt(k, d)
    fun setInt(k: String, v: Int) = p.edit().putInt(k, v).apply()
    fun inc(k: String) = setInt(k, int(k) + 1)

    fun exportJson(): String {
        val o = JSONObject()
        p.all.forEach { (k, v) -> o.put(k, v) }
        return o.toString(2)
    }

    fun importJson(raw: String) {
        val o = JSONObject(raw)
        val e = p.edit()
        o.keys().forEach { k ->
            when (val v = o.get(k)) {
                is Boolean -> e.putBoolean(k, v)
                is Int -> e.putInt(k, v)
                is Long -> e.putLong(k, v)
                else -> e.putString(k, v.toString())
            }
        }
        e.apply()
    }

    fun providers(): List<Provider> = parseArr("providers") { o ->
        val models = mutableListOf<String>()
        val m = o.optJSONArray("models")
        if (m != null) for (i in 0 until m.length()) models += m.getString(i)
        Provider(
            o.getString("id"), o.getString("name"), o.optString("baseUrl"),
            o.optString("model"), o.optString("apiKey"), models, o.optString("headers")
        )
    }

    fun saveProviders(list: List<Provider>) = saveArr("providers", list) { x ->
        JSONObject().put("id", x.id).put("name", x.name).put("baseUrl", x.baseUrl)
            .put("model", x.model).put("apiKey", x.apiKey)
            .put("models", JSONArray(x.models)).put("headers", x.headers)
    }

    fun assistants(): List<Assistant> {
        val list = parseArr("assistants") { o ->
            Assistant(o.getString("id"), o.getString("name"), o.optString("prompt", "You are a helpful assistant."))
        }
        return if (list.isEmpty()) {
            val d = listOf(Assistant("a1", "Default Assistant", "You are a helpful assistant."))
            saveAssistants(d); d
        } else list
    }

    fun saveAssistants(list: List<Assistant>) = saveArr("assistants", list) { x ->
        JSONObject().put("id", x.id).put("name", x.name).put("prompt", x.prompt)
    }

    fun mcp(): List<McpServer> = parseArr("mcp") { o ->
        McpServer(o.getString("id"), o.getString("name"), o.optString("url"), o.optBoolean("enabled", true), o.optString("transport", "http"), o.optString("headers"))
    }

    fun saveMcp(list: List<McpServer>) = saveArr("mcp", list) { x ->
        JSONObject().put("id", x.id).put("name", x.name).put("url", x.url).put("enabled", x.enabled).put("transport", x.transport).put("headers", x.headers)
    }

    fun prompts(): List<PromptItem> = parseArr("prompts") { o ->
        PromptItem(o.getString("id"), o.getString("title"), o.optString("body"))
    }

    fun savePrompts(list: List<PromptItem>) = saveArr("prompts", list) { x ->
        JSONObject().put("id", x.id).put("title", x.title).put("body", x.body)
    }

    fun quick(): List<QuickMsg> = parseArr("quick") { o ->
        QuickMsg(o.getString("id"), o.getString("text"))
    }

    fun saveQuick(list: List<QuickMsg>) = saveArr("quick", list) { x ->
        JSONObject().put("id", x.id).put("text", x.text)
    }

    fun conversations(): List<Conversation> {
        val list = parseArr("conversations") { o ->
            val msgs = mutableListOf<ChatMessage>()
            val mArr = o.optJSONArray("messages") ?: JSONArray()
            for (j in 0 until mArr.length()) {
                val m = mArr.getJSONObject(j)
                msgs += ChatMessage(
                    m.getString("id"),
                    ChatMessage.Role.valueOf(m.getString("role")),
                    m.getString("text"),
                    m.optString("attachment").ifBlank { null },
                    m.optString("image").ifBlank { null }
                )
            }
            Conversation(o.getString("id"), o.getString("title"), o.optString("preview"), o.optLong("updatedAt"), msgs, o.optString("providerId").ifBlank { null })
        }
        return list.sortedByDescending { it.updatedAt }
    }

    fun saveConversation(c: Conversation) {
        persist(conversations().filter { it.id != c.id } + c)
    }

    fun deleteConversation(id: String) = persist(conversations().filter { it.id != id })

    private fun persist(list: List<Conversation>) = saveArr("conversations", list) { c ->
        val msgs = JSONArray()
        c.messages.forEach { m ->
            msgs.put(
                JSONObject().put("id", m.id).put("role", m.role.name).put("text", m.text)
                    .put("attachment", m.attachmentName ?: "").put("image", m.imageBase64 ?: "")
            )
        }
        JSONObject().put("id", c.id).put("title", c.title).put("preview", c.preview)
            .put("updatedAt", c.updatedAt).put("messages", msgs).put("providerId", c.providerId ?: "")
    }

    private fun <T> parseArr(key: String, map: (JSONObject) -> T): List<T> {
        val arr = JSONArray(p.getString(key, "[]"))
        return (0 until arr.length()).map { map(arr.getJSONObject(it)) }
    }

    private fun <T> saveArr(key: String, list: List<T>, map: (T) -> JSONObject) {
        val arr = JSONArray()
        list.forEach { arr.put(map(it)) }
        p.edit().putString(key, arr.toString()).apply()
    }

    companion object {
        fun id() = UUID.randomUUID().toString()
    }
}
