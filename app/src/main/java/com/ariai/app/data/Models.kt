package com.ariai.app.data

data class Provider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val model: String,
    val apiKey: String = "",
    val models: List<String> = emptyList(),
    val headers: String = ""
)

data class Assistant(
    val id: String,
    val name: String,
    val prompt: String = "You are a helpful assistant."
)

data class McpServer(
    val id: String,
    val name: String,
    val url: String,
    val enabled: Boolean = true,
    val transport: String = "http",
    val headers: String = ""
)

data class ChatMessage(
    val id: String,
    val role: Role,
    val text: String,
    val attachmentName: String? = null,
    val imageBase64: String? = null
) {
    enum class Role { User, Assistant, System }
}

data class Conversation(
    val id: String,
    val title: String,
    val preview: String,
    val updatedAt: Long,
    val messages: List<ChatMessage> = emptyList(),
    val providerId: String? = null
)

data class RequestLog(
    val time: Long,
    val method: String,
    val url: String,
    val status: Int,
    val body: String
)

data class PromptItem(val id: String, val title: String, val body: String)
data class QuickMsg(val id: String, val text: String)

enum class Screen {
    Chat, Settings, Preferences, General, Notifications, Theme,
    Assistant, Extensions, ModelSettings, Providers, Speech, Mcp,
    Statistics, SearchService, WebServer, Backup, About, Docs, Logs,
    ChatHistory, SearchChats, QuickMessages, Prompts, Skills, Workspace
}
