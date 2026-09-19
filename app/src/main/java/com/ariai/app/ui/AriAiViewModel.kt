package com.ariai.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ariai.app.data.AppStore
import com.ariai.app.data.ChatMessage
import com.ariai.app.data.ChatMode
import com.ariai.app.data.Conversation
import com.ariai.app.data.Provider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AriAiViewModel(app: Application) : AndroidViewModel(app) {
    private val store = AppStore(app)

    var providers by mutableStateOf(store.providers())
        private set
    var selectedProviderId by mutableStateOf(store.selectedProviderId())
        private set
    var conversations by mutableStateOf(store.conversations())
        private set
    var current by mutableStateOf<Conversation?>(null)
        private set
    var input by mutableStateOf("")
    var sending by mutableStateOf(false)
        private set
    var plusOpen by mutableStateOf(false)
    var pendingAttachment by mutableStateOf<String?>(null)
    var snack by mutableStateOf<String?>(null)

    val selectedProvider: Provider?
        get() = providers.find { it.id == selectedProviderId } ?: providers.firstOrNull()

    fun selectProvider(id: String) {
        selectedProviderId = id
        store.selectProvider(id)
        current = current?.copy(providerId = id)?.also { store.saveConversation(it); refresh() }
    }

    fun addProvider(name: String, baseUrl: String, model: String, apiKey: String) {
        val p = Provider(AppStore.newId(), name, baseUrl, model, apiKey, isBuiltIn = false)
        store.addProvider(p)
        providers = store.providers()
        selectProvider(p.id)
        snack = "مدل «$name» اضافه شد"
    }

    fun removeProvider(id: String) {
        store.removeProvider(id)
        providers = store.providers()
        if (selectedProviderId == id) selectProvider(providers.first().id)
    }

    fun openNewChat(mode: ChatMode = ChatMode.Chat, seed: String = "") {
        val c = Conversation(
            id = AppStore.newId(),
            title = if (mode == ChatMode.Chat) "گفتگوی جدید" else mode.labelFa,
            preview = "",
            updatedAt = System.currentTimeMillis(),
            mode = mode,
            providerId = selectedProviderId
        )
        current = c
        input = seed
        pendingAttachment = null
        store.saveConversation(c)
        refresh()
    }

    fun openConversation(id: String) {
        current = conversations.find { it.id == id }
        input = ""
        pendingAttachment = null
        current?.providerId?.let { selectedProviderId = it }
    }

    fun closeChat() {
        current = null
        input = ""
        pendingAttachment = null
        plusOpen = false
    }

    fun attach(kind: String) {
        pendingAttachment = kind
        plusOpen = false
        snack = when (kind) {
            "gallery" -> "تصویر از گالری آماده ارسال است"
            "camera" -> "عکس دوربین آماده ارسال است"
            "file" -> "فایل پیوست شد"
            "voice" -> "پیام صوتی ضبط شد"
            else -> "پیوست اضافه شد"
        }
    }

    fun send() {
        val text = input.trim()
        val conv = current ?: return
        if (text.isBlank() && pendingAttachment == null) return
        val attach = pendingAttachment
        val userText = buildString {
            if (attach != null) append("[$attach] ")
            append(if (text.isBlank()) "پیوست را بررسی کن" else text)
        }
        val userMsg = ChatMessage(AppStore.newId(), ChatMessage.Role.User, userText, attach)
        val updated = conv.copy(
            messages = conv.messages + userMsg,
            preview = userText.take(80),
            title = if (conv.messages.isEmpty()) userText.take(28) else conv.title,
            updatedAt = System.currentTimeMillis(),
            providerId = selectedProviderId
        )
        current = updated
        store.saveConversation(updated)
        refresh()
        input = ""
        pendingAttachment = null
        reply(updated, userText)
    }

    private fun reply(conv: Conversation, userText: String) {
        sending = true
        viewModelScope.launch {
            delay(500)
            val model = selectedProvider?.name ?: "AriAi"
            val modeHint = when (conv.mode) {
                ChatMode.Image -> "حالت تصویر فعال است. توصیف بصری و پرامپت ساخت تصویر می‌دهم."
                ChatMode.Write -> "حالت نوشتن فعال است. متن را بازنویسی و ساختاربندی می‌کنم."
                ChatMode.Code -> "حالت کد فعال است. راه‌حل و نمونه کد می‌دهم."
                ChatMode.Translate -> "حالت ترجمه فعال است."
                ChatMode.Summarize -> "حالت خلاصه فعال است."
                ChatMode.Analyze -> "حالت تحلیل فعال است."
                ChatMode.Chat -> ""
            }
            val answer = buildString {
                if (modeHint.isNotBlank()) appendLine(modeHint).appendLine()
                append("پاسخ $model:\n")
                append(simulate(userText, conv.mode))
            }
            val bot = ChatMessage(AppStore.newId(), ChatMessage.Role.Assistant, answer)
            val latest = (current?.takeIf { it.id == conv.id } ?: conv)
            val done = latest.copy(
                messages = latest.messages + bot,
                updatedAt = System.currentTimeMillis()
            )
            current = done
            store.saveConversation(done)
            refresh()
            sending = false
        }
    }

    private fun simulate(q: String, mode: ChatMode): String {
        val qn = q.removePrefix("[gallery] ").removePrefix("[camera] ").removePrefix("[file] ").removePrefix("[voice] ")
        return when (mode) {
            ChatMode.Code -> "```kotlin\nfun answer() = \"$qn\"\n```\nاین اسکلت را می‌توانی گسترش بدهی."
            ChatMode.Image -> "پرامپت پیشنهادی:\n\"$qn, cinematic lighting, ultra detailed, 8k\""
            ChatMode.Write -> "نسخه ویرایش‌شده:\n\n$qn\n\nلحن واضح‌تر و مستقیم‌تر شد."
            ChatMode.Translate -> "Translation:\n$qn"
            ChatMode.Summarize -> "خلاصه: ${qn.take(120)}"
            ChatMode.Analyze -> "نکات کلیدی درباره «${qn.take(40)}»:\n• زمینه\n• ریسک‌ها\n• پیشنهاد بعدی"
            ChatMode.Chat -> "متوجه شدم: «${qn.take(200)}». اگر جزئیات بیشتری بدهی دقیق‌تر جواب می‌دهم."
        }
    }

    fun deleteConversation(id: String) {
        store.deleteConversation(id)
        if (current?.id == id) current = null
        refresh()
    }

    private fun refresh() {
        conversations = store.conversations()
        providers = store.providers()
    }
}
