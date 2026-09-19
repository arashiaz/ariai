package com.ariai.app.data

data class Provider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val model: String,
    val apiKey: String = "",
    val isBuiltIn: Boolean = false
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
    val mode: ChatMode = ChatMode.Chat,
    val providerId: String? = null
)

enum class ChatMode(val labelFa: String, val starter: String) {
    Chat("گفتگو", ""),
    Image("تصویر", "یک تصویر توصیف کن و ایده ساخت آن را بده: "),
    Write("نوشتن", "متن زیر را حرفه‌ای بنویس: "),
    Code("کد", "این مسئله را با کد حل کن و توضیح بده:\n"),
    Translate("ترجمه", "این متن را ترجمه کن:\n"),
    Summarize("خلاصه", "این متن را خلاصه کن:\n"),
    Analyze("تحلیل", "این موضوع را تحلیل کن:\n")
}
