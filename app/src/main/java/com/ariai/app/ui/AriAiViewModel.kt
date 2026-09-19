package com.ariai.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ariai.app.data.AppStore
import com.ariai.app.data.Assistant
import com.ariai.app.data.ChatMessage
import com.ariai.app.data.Conversation
import com.ariai.app.data.McpServer
import com.ariai.app.data.Provider
import com.ariai.app.data.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class AriAiViewModel(app: Application) : AndroidViewModel(app) {
    val store = AppStore(app)

    var screen by mutableStateOf(Screen.Chat)
    var drawerOpen by mutableStateOf(false)
    var plusOpen by mutableStateOf(false)
    var providerSheet by mutableStateOf(false)
    var newMenu by mutableStateOf(false)
    var mcpImport by mutableStateOf(false)
    var mcpDraft by mutableStateOf<McpServer?>(null)

    var providers by mutableStateOf(store.providers())
    var assistants by mutableStateOf(store.assistants())
    var mcp by mutableStateOf(store.mcp())
    var conversations by mutableStateOf(store.conversations())
    var current by mutableStateOf<Conversation?>(null)
    var input by mutableStateOf("")
    var sending by mutableStateOf(false)
    var pendingAttach by mutableStateOf<String?>(null)
    var snack by mutableStateOf<String?>(null)
    var selectedProviderId by mutableStateOf(store.str("sel_provider"))
    var selectedAssistantId by mutableStateOf(store.str("sel_assistant", assistants.firstOrNull()?.id ?: ""))
    var modelQuery by mutableStateOf("")
    var chatQuery by mutableStateOf("")
    var importJson by mutableStateOf("")
    var userName by mutableStateOf(store.str("user_name", "User"))

    val flags = mutableStateMapOf<String, Boolean>().apply {
        listOf(
            "new_chat_launch" to true,
            "send_enter" to false,
            "jumper" to true,
            "jumper_left" to false,
            "autoscroll" to true,
            "icon_loading" to true,
            "blur" to false,
            "haptic" to false,
            "skip_crop" to true,
            "paste_file" to false,
            "volume_scroll" to false,
            "tts_quotes" to false,
            "tts_brackets" to false,
            "notif_gen" to false,
            "suggestions" to true
        ).forEach { (k, d) -> put(k, store.bool(k, d)) }
    }

    var ttsSpeed by mutableStateOf(store.int("tts_speed", 6))
    var colorMode by mutableStateOf(store.str("color_mode", "System"))
    var speechId by mutableStateOf(store.str("speech", "sys"))
    var chatModel by mutableStateOf(store.str("chat_model"))
    var fastModel by mutableStateOf(store.str("fast_model"))
    var translateModel by mutableStateOf(store.str("tr_model"))
    var ocrModel by mutableStateOf(store.str("ocr_model"))
    var compressModel by mutableStateOf(store.str("cmp_model"))

    val selectedProvider get() = providers.find { it.id == selectedProviderId }
    val selectedAssistant get() = assistants.find { it.id == selectedAssistantId } ?: assistants.firstOrNull()

    val greeting: String
        get() {
            val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                h < 12 -> "Good morning"
                h < 18 -> "Good afternoon"
                else -> "Good evening"
            }
        }

    init {
        store.inc("launches")
        if (store.bool("new_chat_launch", true) && current == null) {
            /* stay on empty new chat */
        }
    }

    fun toggle(k: String) {
        val v = !(flags[k] ?: false)
        flags[k] = v
        store.setBool(k, v)
    }

    fun go(s: Screen) {
        screen = s
        drawerOpen = false
        plusOpen = false
        providerSheet = false
        newMenu = false
    }

    fun openNewChat() {
        current = null
        input = ""
        pendingAttach = null
        go(Screen.Chat)
    }

    fun openConv(id: String) {
        current = conversations.find { it.id == id }
        go(Screen.Chat)
    }

    fun attach(kind: String) {
        pendingAttach = kind
        plusOpen = false
        snack = when (kind) {
            "file" -> "File ready to send"
            "photo" -> "Photo attached"
            "camera" -> "Picture captured"
            else -> "Attached"
        }
    }

    fun send() {
        val text = input.trim()
        if (text.isBlank() && pendingAttach == null) return
        var conv = current
        if (conv == null) {
            conv = Conversation(AppStore.id(), text.take(32).ifBlank { "New Chat" }, text, System.currentTimeMillis(), emptyList(), selectedProviderId.ifBlank { null })
        }
        val body = buildString {
            pendingAttach?.let { append("[$it] ") }
            append(if (text.isBlank()) "Please review the attachment" else text)
        }
        val user = ChatMessage(AppStore.id(), ChatMessage.Role.User, body, pendingAttach)
        conv = conv.copy(messages = conv.messages + user, preview = body.take(80), updatedAt = System.currentTimeMillis(), title = if (conv.messages.isEmpty()) body.take(32) else conv.title)
        current = conv
        store.saveConversation(conv)
        refresh()
        input = ""
        pendingAttach = null
        reply(conv, body)
    }

    private fun reply(conv: Conversation, q: String) {
        sending = true
        viewModelScope.launch {
            delay(450)
            val model = selectedProvider?.name ?: "AriAi"
            val ans = ChatMessage(AppStore.id(), ChatMessage.Role.Assistant, "$model:\n${q.take(400)}")
            val latest = current?.takeIf { it.id == conv.id } ?: conv
            val done = latest.copy(messages = latest.messages + ans, updatedAt = System.currentTimeMillis())
            current = done
            store.saveConversation(done)
            store.setInt("msg_count", store.int("msg_count") + 2)
            refresh()
            sending = false
        }
    }

    fun addProvider(name: String, url: String, model: String, key: String) {
        val p = Provider(AppStore.id(), name, url, model, key)
        val list = providers + p
        store.saveProviders(list)
        providers = list
        selectedProviderId = p.id
        store.setStr("sel_provider", p.id)
        snack = "Provider added"
    }

    fun removeProvider(id: String) {
        val list = providers.filter { it.id != id }
        store.saveProviders(list)
        providers = list
        if (selectedProviderId == id) {
            selectedProviderId = list.firstOrNull()?.id ?: ""
            store.setStr("sel_provider", selectedProviderId)
        }
    }

    fun selectProvider(id: String) {
        selectedProviderId = id
        store.setStr("sel_provider", id)
        providerSheet = false
    }

    fun addAssistant(name: String) {
        val a = Assistant(AppStore.id(), name)
        val list = assistants + a
        store.saveAssistants(list)
        assistants = list
    }

    fun saveMcp(s: McpServer) {
        val list = mcp.filter { it.id != s.id } + s
        store.saveMcp(list)
        mcp = list
        mcpDraft = null
        snack = "MCP saved"
    }

    fun importMcp(raw: String) {
        if (raw.isBlank()) {
            snack = "Paste JSON first"
            return
        }
        saveMcp(McpServer(AppStore.id(), "Imported MCP", "", true, "http", raw.take(200)))
        importJson = ""
        mcpImport = false
    }

    fun deleteConv(id: String) {
        store.deleteConversation(id)
        if (current?.id == id) current = null
        refresh()
    }

    fun setUser(n: String) {
        userName = n
        store.setStr("user_name", n)
    }

    fun pickSpeech(id: String) {
        speechId = id
        store.setStr("speech", id)
    }

    fun pickSlot(slot: String, id: String) {
        when (slot) {
            "chat" -> { chatModel = id; store.setStr("chat_model", id) }
            "fast" -> { fastModel = id; store.setStr("fast_model", id) }
            "tr" -> { translateModel = id; store.setStr("tr_model", id) }
            "ocr" -> { ocrModel = id; store.setStr("ocr_model", id) }
            "cmp" -> { compressModel = id; store.setStr("cmp_model", id) }
        }
        snack = "Model selected"
    }

    fun modelName(id: String) = providers.find { it.id == id }?.name ?: "Select Model"

    private fun refresh() {
        conversations = store.conversations()
        providers = store.providers()
    }
}
