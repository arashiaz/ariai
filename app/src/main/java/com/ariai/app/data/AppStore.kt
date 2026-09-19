package com.ariai.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AppStore(context: Context) {
    private val prefs = context.getSharedPreferences("ariai", Context.MODE_PRIVATE)

    fun providers(): List<Provider> {
        val raw = prefs.getString("providers", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Provider>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += Provider(
                id = o.getString("id"),
                name = o.getString("name"),
                baseUrl = o.optString("baseUrl"),
                model = o.optString("model"),
                apiKey = o.optString("apiKey"),
                isBuiltIn = o.optBoolean("isBuiltIn")
            )
        }
        if (list.none { it.isBuiltIn }) {
            val defaults = defaultProviders()
            saveProviders(defaults + list)
            return defaults + list
        }
        return list
    }

    fun saveProviders(list: List<Provider>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("baseUrl", p.baseUrl)
                    .put("model", p.model)
                    .put("apiKey", p.apiKey)
                    .put("isBuiltIn", p.isBuiltIn)
            )
        }
        prefs.edit().putString("providers", arr.toString()).apply()
    }

    fun addProvider(p: Provider) {
        saveProviders(providers() + p)
    }

    fun removeProvider(id: String) {
        saveProviders(providers().filter { it.id != id || it.isBuiltIn })
    }

    fun selectedProviderId(): String =
        prefs.getString("selected_provider", "openai-gpt4o") ?: "openai-gpt4o"

    fun selectProvider(id: String) {
        prefs.edit().putString("selected_provider", id).apply()
    }

    fun conversations(): List<Conversation> {
        val raw = prefs.getString("conversations", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Conversation>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val msgs = mutableListOf<ChatMessage>()
            val mArr = o.optJSONArray("messages") ?: JSONArray()
            for (j in 0 until mArr.length()) {
                val m = mArr.getJSONObject(j)
                msgs += ChatMessage(
                    id = m.getString("id"),
                    role = ChatMessage.Role.valueOf(m.getString("role")),
                    text = m.getString("text"),
                    attachmentName = m.optString("attachment").ifBlank { null }
                )
            }
            list += Conversation(
                id = o.getString("id"),
                title = o.getString("title"),
                preview = o.optString("preview"),
                updatedAt = o.optLong("updatedAt"),
                messages = msgs,
                mode = runCatching { ChatMode.valueOf(o.optString("mode", "Chat")) }.getOrDefault(ChatMode.Chat),
                providerId = o.optString("providerId").ifBlank { null }
            )
        }
        return list.sortedByDescending { it.updatedAt }
    }

    fun saveConversation(c: Conversation) {
        val all = conversations().filter { it.id != c.id }.toMutableList()
        all.add(0, c)
        persistConversations(all)
    }

    fun deleteConversation(id: String) {
        persistConversations(conversations().filter { it.id != id })
    }

    private fun persistConversations(list: List<Conversation>) {
        val arr = JSONArray()
        list.forEach { c ->
            val msgs = JSONArray()
            c.messages.forEach { m ->
                msgs.put(
                    JSONObject()
                        .put("id", m.id)
                        .put("role", m.role.name)
                        .put("text", m.text)
                        .put("attachment", m.attachmentName ?: "")
                )
            }
            arr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("title", c.title)
                    .put("preview", c.preview)
                    .put("updatedAt", c.updatedAt)
                    .put("messages", msgs)
                    .put("mode", c.mode.name)
                    .put("providerId", c.providerId ?: "")
            )
        }
        prefs.edit().putString("conversations", arr.toString()).apply()
    }

    companion object {
        fun defaultProviders() = listOf(
            Provider("openai-gpt4o", "GPT-4o", "https://api.openai.com/v1", "gpt-4o", isBuiltIn = true),
            Provider("openai-gpt4o-mini", "GPT-4o mini", "https://api.openai.com/v1", "gpt-4o-mini", isBuiltIn = true),
            Provider("claude-sonnet", "Claude 3.5 Sonnet", "https://api.anthropic.com", "claude-3-5-sonnet", isBuiltIn = true)
        )

        fun newId() = UUID.randomUUID().toString()
    }
}
