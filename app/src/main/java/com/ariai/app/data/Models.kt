package com.ariai.app.data

data class Provider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val model: String,
    val apiKey: String = ""
)

data class Assistant(
    val id: String,
    val name: String
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
    val attachmentName: String? = null
) {
    enum class Role { User, Assistant }
}

data class Conversation(
    val id: String,
    val title: String,
    val preview: String,
    val updatedAt: Long,
    val messages: List<ChatMessage> = emptyList(),
    val providerId: String? = null
)

enum class Screen {
    Chat, Settings, Preferences, General, Notifications, Theme,
    Assistant, Extensions, ModelSettings, Providers, Speech, Mcp,
    Statistics, SearchService, WebServer, Backup, About, Docs, Logs,
    ChatHistory, SearchChats, QuickMessages, Prompts, Skills, Workspace
}
