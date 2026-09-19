package com.ariai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ariai.app.data.ChatMessage
import com.ariai.app.data.McpServer
import com.ariai.app.data.Screen
import com.ariai.app.data.AppStore

@Composable
fun AriAiApp(vm: AriAiViewModel) {
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(vm.snack) {
        vm.snack?.let { snack.showSnackbar(it); vm.snack = null }
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Page)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            when (vm.screen) {
                Screen.Chat -> ChatPage(vm)
                Screen.Settings -> SettingsPage(vm)
                Screen.Preferences -> PrefsPage(vm)
                Screen.General -> GeneralPage(vm)
                Screen.Notifications -> NotifPage(vm)
                Screen.Theme -> ThemePage(vm)
                Screen.Assistant -> AssistantPage(vm)
                Screen.Extensions -> ExtPage(vm)
                Screen.ModelSettings -> ModelPage(vm)
                Screen.Providers -> ProvidersPage(vm)
                Screen.Speech -> SpeechPage(vm)
                Screen.Mcp -> McpPage(vm)
                Screen.Statistics -> StatsPage(vm)
                Screen.SearchService -> SimplePage(vm, "Search Service", "Set up search service")
                Screen.WebServer -> SimplePage(vm, "Web Server", "Allow you access AriAi via Web")
                Screen.Backup -> SimplePage(vm, "Data Backup", "Backup and restore app data")
                Screen.About -> SimplePage(vm, "About", "AriAi · local AI client")
                Screen.Docs -> SimplePage(vm, "Documentation", "View app usage instructions and help")
                Screen.Logs -> SimplePage(vm, "Request Logs", "Inspect recent API requests")
                Screen.ChatHistory -> HistoryPage(vm)
                Screen.SearchChats -> SearchChatsPage(vm)
                Screen.QuickMessages -> SimplePage(vm, "Quick Messages", "Manage shared quick message templates")
                Screen.Prompts -> SimplePage(vm, "Prompts", "Manage and use custom prompts")
                Screen.Skills -> SimplePage(vm, "Agent Skills", "Manage skill packages for AI")
                Screen.Workspace -> SimplePage(vm, "Workspace", "Manage local working directories")
            }
            if (vm.drawerOpen) Drawer(vm)
            if (vm.plusOpen) PlusSheet(vm)
            if (vm.providerSheet) ProviderSheet(vm)
            if (vm.mcpImport) ImportMcp(vm)
            vm.mcpDraft?.let { McpEditor(vm, it) }
            SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
    }
}

@Composable
private fun ChatPage(vm: AriAiViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBtn(Icons.Filled.Menu) { vm.drawerOpen = true }
            Text("New Chat", color = Ink, fontWeight = FontWeight.Medium, fontSize = 18.sp)
            Spacer(Modifier.weight(1f))
            IconBtn(Icons.Filled.List) { vm.go(Screen.ChatHistory) }
            IconBtn(Icons.Filled.Email) { vm.openNewChat() }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val msgs = vm.current?.messages.orEmpty()
            if (msgs.isEmpty()) {
                Box(Modifier.fillMaxSize())
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(msgs, key = { it.id }) { Bubble(it) }
                    if (vm.sending) item { Text("…", color = Mute, modifier = Modifier.padding(8.dp)) }
                }
            }
        }
        if (vm.pendingAttach != null) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(vm.pendingAttach!!, color = Accent, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.Close, null, Modifier.size(16.dp).clickable { vm.pendingAttach = null }, tint = Mute)
            }
        }
        Composer(vm)
    }
}

@Composable
private fun Composer(vm: AriAiViewModel) {
    Column(
        Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFEDEEF2))
            .padding(10.dp)
    ) {
        BasicTextField(
            value = vm.input,
            onValueChange = { vm.input = it },
            textStyle = TextStyle(color = Ink, fontSize = 16.sp, textAlign = TextAlign.Start),
            cursorBrush = SolidColor(Accent),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            decorationBox = { inner ->
                if (vm.input.isEmpty()) Text("Chat with AI", color = Mute, fontSize = 16.sp)
                inner()
            }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            val can = vm.input.isNotBlank() || vm.pendingAttach != null
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(if (can) Accent else Color(0xFFD8D9DE)).clickable(enabled = can) { vm.send() },
                contentAlignment = Alignment.Center
            ) {
                Text("↑", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconBtn(Icons.Filled.Add) { vm.plusOpen = true }
            Spacer(Modifier.weight(1f))
            IconBtn(Icons.Filled.Search) { vm.snack = "Search in chat" }
            IconBtn(Icons.Filled.Person) { vm.providerSheet = true }
        }
    }
}

@Composable
private fun Bubble(m: ChatMessage) {
    val mine = m.role == ChatMessage.Role.User
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.Start else Arrangement.End) {
        Column(
            Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(16.dp))
                .background(if (mine) AccentSoft else CardBg)
                .padding(12.dp)
        ) {
            if (m.attachmentName != null) Text(m.attachmentName, color = Accent, fontSize = 12.sp)
            Text(m.text, color = Ink, fontSize = 15.sp)
        }
    }
}

@Composable
private fun PlusSheet(vm: AriAiViewModel) {
    Overlay({ vm.plusOpen = false }) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Page).padding(20.dp)
        ) {
            Handle()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PlusTile("Upload File", Icons.Filled.Email) { vm.attach("file") }
                PlusTile("Photo", Icons.Filled.Star) { vm.attach("photo") }
                PlusTile("Take Picture", Icons.Filled.Add) { vm.attach("camera") }
            }
            Spacer(Modifier.height(16.dp))
            SheetRow("Extensions", Icons.Filled.Build) { vm.go(Screen.Extensions) }
            Spacer(Modifier.height(8.dp))
            SheetRow("Compress History", Icons.Filled.List) {
                vm.plusOpen = false
                vm.snack = "History compressed"
            }
        }
    }
}

@Composable
private fun PlusTile(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)).background(Chip), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Ink, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Ink, fontSize = 13.sp)
    }
}

@Composable
private fun ProviderSheet(vm: AriAiViewModel) {
    Overlay({ vm.providerSheet = false }) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.72f).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Page).padding(20.dp)
        ) {
            Handle()
            SearchBar("Enter model name to search", vm.modelQuery) { vm.modelQuery = it }
            Spacer(Modifier.height(16.dp))
            val list = vm.providers.filter { it.name.contains(vm.modelQuery, true) || it.model.contains(vm.modelQuery, true) }
            if (list.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                    Text("No available AI providers, please add in settings", color = Mute, fontSize = 15.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(list, key = { it.id }) { p ->
                        CardRow(p.name, p.model, Icons.Filled.Person, selected = p.id == vm.selectedProviderId) {
                            vm.selectProvider(p.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Drawer(vm: AriAiViewModel) {
    Box(Modifier.fillMaxSize().background(Color(0x66000000)).clickable { vm.drawerOpen = false }) {
        Column(
            Modifier.fillMaxHeight().fillMaxWidth(0.86f).align(Alignment.CenterStart)
                .background(Page).clickable(enabled = false) {}.padding(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Edit, null, tint = Ink, modifier = Modifier.size(16.dp).clickable {
                            vm.setUser(if (vm.userName == "User") "Ari" else "User")
                        })
                        Spacer(Modifier.width(6.dp))
                        Text(vm.userName, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Ink)
                    }
                    Text("👋 ${vm.greeting}", color = Mute, fontSize = 13.sp)
                }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFFE91E8C), Color(0xFF8BC34A)))))
            }
            Spacer(Modifier.height(18.dp))
            MenuLine("Search Chats", Icons.Filled.Search) { vm.go(Screen.SearchChats) }
            MenuLine("Chat History", Icons.Filled.List) { vm.go(Screen.ChatHistory) }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                Text("New", fontWeight = FontWeight.SemiBold, color = Ink)
                Icon(Icons.Filled.Add, null, tint = Ink, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Box {
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(AccentSoft).clickable { vm.newMenu = !vm.newMenu }.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) { Text("Chat", color = Ink, fontWeight = FontWeight.Medium) }
                    if (vm.newMenu) {
                        Column(
                            Modifier.padding(top = 44.dp).clip(RoundedCornerShape(14.dp)).background(CardBg).border(1.dp, Chip, RoundedCornerShape(14.dp)).padding(8.dp)
                        ) {
                            MenuLine("AI Translator", Icons.Filled.Edit) {
                                vm.newMenu = false
                                vm.openNewChat()
                                vm.input = "Translate: "
                            }
                            MenuLine("Image Generation", Icons.Filled.Star) {
                                vm.newMenu = false
                                vm.openNewChat()
                                vm.input = "Generate image: "
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            val filtered = vm.conversations
            if (filtered.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No conversations", color = Ink, fontSize = 16.sp)
                }
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(filtered, key = { it.id }) { c ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { vm.openConv(c.id) }.padding(10.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(c.title, color = Ink, maxLines = 1)
                                Text(c.preview, color = Mute, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFFE91E8C), Color(0xFF8BC34A)))))
                Spacer(Modifier.width(10.dp))
                Text(vm.selectedAssistant?.name ?: "Default Assistant", color = Ink, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Person, null, tint = Ink)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RoundIcon(Icons.Filled.Settings) { vm.go(Screen.Settings) }
                RoundIcon(Icons.Filled.List) { vm.go(Screen.Statistics) }
                RoundIcon(Icons.Filled.Person) { vm.go(Screen.Assistant) }
                RoundIcon(Icons.Filled.Star) { vm.go(Screen.ModelSettings) }
                RoundIcon(Icons.Filled.Person) { vm.go(Screen.Assistant) }
            }
        }
    }
}

@Composable
private fun SettingsPage(vm: AriAiViewModel) {
    PageScaffold("Settings", onBack = { vm.go(Screen.Chat) }) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(DangerBg).padding(16.dp)
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Please configure API and model", fontWeight = FontWeight.SemiBold, color = DangerInk, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.Info, null, tint = DangerInk)
                }
                Text("You haven't configured API and model yet, please configure first", color = DangerInk, fontSize = 13.sp, textAlign = TextAlign.End)
                Spacer(Modifier.height(8.dp))
                Text("Configure", color = Link, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { vm.go(Screen.Providers) })
            }
        }
        SectionLabel("General Settings")
        Group {
            SettingRow("Color Mode", vm.colorMode, Icons.Filled.Star) { vm.go(Screen.Theme) }
            SettingRow("Preferences", "Theme, notifications, UI and general settings", Icons.Filled.Settings) { vm.go(Screen.Preferences) }
            SettingRow("Assistant", "Set up personalized assistants (agents)", Icons.Filled.Person) { vm.go(Screen.Assistant) }
            SettingRow("Extensions", "Manage prompt injections, skills and more", Icons.Filled.Build) { vm.go(Screen.Extensions) }
        }
        SectionLabel("Models & Services")
        Group {
            SettingRow("Default Model and Prompt", "Set default models for each feature", Icons.Filled.Star) { vm.go(Screen.ModelSettings) }
            SettingRow("Providers", "Configure AI providers", Icons.Filled.Person) { vm.go(Screen.Providers) }
            SettingRow("Search Service", "Set up search service", Icons.Filled.Home) { vm.go(Screen.SearchService) }
            SettingRow("Speech Services", "Configure text-to-speech and speech recognition", Icons.Filled.Notifications) { vm.go(Screen.Speech) }
            SettingRow("MCP", "Configure MCP Servers", Icons.Filled.Build) { vm.go(Screen.Mcp) }
            SettingRow("Web Server", "Allow you access AriAi via Web", Icons.Filled.Settings) { vm.go(Screen.WebServer) }
        }
        SectionLabel("Data Settings")
        Group {
            SettingRow("Data Backup", "Backup and restore app data", Icons.Filled.Settings) { vm.go(Screen.Backup) }
            SettingRow("Storage Management", "files, ${vm.conversations.size} chats", Icons.Filled.Send) { vm.snack = "Cache cleared" }
        }
        SectionLabel("About")
        Group {
            SettingRow("About", "About this app", Icons.Filled.Info) { vm.go(Screen.About) }
            SettingRow("Documentation", "View app usage instructions and help", Icons.Filled.Info) { vm.go(Screen.Docs) }
            SettingRow("Request Logs", "Inspect logs", Icons.Filled.List) { vm.go(Screen.Logs) }
        }
    }
}

@Composable
private fun PrefsPage(vm: AriAiViewModel) {
    PageScaffold("Preferences", onBack = { vm.go(Screen.Settings) }) {
        Group {
            SettingRow("Theme", "Dynamic color, theme, AMOLED dark mode", Icons.Filled.Star) { vm.go(Screen.Theme) }
            SettingRow("Notifications", "Update alerts, message generation notifications", Icons.Filled.Notifications) { vm.go(Screen.Notifications) }
            SettingRow("General", "Interaction behavior, scrolling, input settings", Icons.Filled.Settings) { vm.go(Screen.General) }
            SettingRow("UI Preferences", "Message display, fonts, code blocks", Icons.Filled.Settings) { vm.snack = "UI preferences saved" }
            SettingRow("Network", "User-Agent and network request settings", Icons.Filled.Home) { vm.snack = "Network defaults" }
        }
    }
}

@Composable
private fun GeneralPage(vm: AriAiViewModel) {
    PageScaffold("General", onBack = { vm.go(Screen.Preferences) }) {
        Group {
            ToggleRow("New chat on launch", "Create a new conversation when the app starts", "new_chat_launch", vm)
            ToggleRow("Send on Enter", "Press Enter to send message instead of adding a new line", "send_enter", vm)
            ToggleRow("Show Message Jumper", "Display quick jump buttons on the right when scrolling", "jumper", vm)
            ToggleRow("Message Jumper Position", "Move Message Jumper to left side", "jumper_left", vm)
            ToggleRow("Auto Scroll", "Automatically scroll to the bottom when AI is generating", "autoscroll", vm)
            ToggleRow("Use App Icon-Style Loading Indicator", "Use the animated app icon instead of circular loading", "icon_loading", vm)
            ToggleRow("Enable Blur Effect", "Enable blur effect on chat input bar", "blur", vm)
            ToggleRow("Message Generation Haptic Effect", "Enable haptic feedback when messages are generated", "haptic", vm)
            ToggleRow("Skip Image Editing", "Skip crop interface after importing images", "skip_crop", vm)
            ToggleRow("Paste Long Text as File", "When pasting text exceeds the threshold, save as file", "paste_file", vm)
            ToggleRow("Volume Key Page Scroll", "Scroll pages with volume keys", "volume_scroll", vm)
        }
        SectionLabel("TTS Settings")
        Group {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.End) {
                Text("Default playback speed", fontWeight = FontWeight.Medium, color = Ink)
                Text("Applied locally to every TTS provider", color = Mute, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("x${"%.1f".format(0.5 + vm.ttsSpeed * 0.25)}", color = Mute, fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Row {
                        (0..10).forEach { i ->
                            Box(
                                Modifier.padding(2.dp).size(10.dp).clip(CircleShape)
                                    .background(if (i <= vm.ttsSpeed) AccentSoft else Chip)
                                    .clickable { vm.ttsSpeed = i; vm.store.setInt("tts_speed", i) }
                            )
                        }
                    }
                }
            }
            ToggleRow("TTS Read Quoted Content Only", "TTS will only read content within quotation marks", "tts_quotes", vm)
            ToggleRow("TTS Skip Bracketed Content", "TTS will not read content inside brackets", "tts_brackets", vm)
        }
    }
}

@Composable
private fun NotifPage(vm: AriAiViewModel) {
    PageScaffold("Notifications", onBack = { vm.go(Screen.Preferences) }) {
        Group {
            SettingRow("Show Updates", "Update reminders are enabled", Icons.Filled.Notifications) { vm.snack = "Updates on" }
            ToggleRow("Enable notification after message generated", "Show a notification when a message is generated if the app is not in the foreground", "notif_gen", vm)
        }
    }
}

@Composable
private fun ThemePage(vm: AriAiViewModel) {
    PageScaffold("Color Mode", onBack = { vm.go(Screen.Settings) }) {
        Group {
            listOf("System", "Light", "Dark").forEach { mode ->
                SettingRow(mode, if (vm.colorMode == mode) "Selected" else "", Icons.Filled.Star) {
                    vm.colorMode = mode
                    vm.store.setStr("color_mode", mode)
                }
            }
        }
    }
}

@Composable
private fun AssistantPage(vm: AriAiViewModel) {
    var q by remember { mutableStateOf("") }
    PageScaffold("Assistant Settings", onBack = { vm.go(Screen.Settings) }, extra = {
        IconBtn(Icons.Filled.Add) { vm.addAssistant("Default Assistant") }
    }) {
        SearchBar("Search assistants", q) { q = it }
        vm.assistants.filter { it.name.contains(q, true) }.forEach { a ->
            CardRow(a.name, "", Icons.Filled.MoreVert, leading = {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFFE91E8C), Color(0xFF8BC34A)))))
            }) {
                vm.selectedAssistantId = a.id
                vm.store.setStr("sel_assistant", a.id)
                vm.go(Screen.Chat)
            }
        }
    }
}

@Composable
private fun ExtPage(vm: AriAiViewModel) {
    PageScaffold("Extensions", onBack = { vm.go(Screen.Settings) }) {
        SectionLabel("Extensions")
        Group {
            SettingRow("Quick Messages", "Manage shared quick message templates", Icons.Filled.Star) { vm.go(Screen.QuickMessages) }
            SettingRow("Prompts", "Manage and use custom prompts", Icons.Filled.Info) { vm.go(Screen.Prompts) }
            SettingRow("Agent Skills", "Manage skill packages for AI to load on demand", Icons.Filled.Build) { vm.go(Screen.Skills) }
            SettingRow("Workspace", "Manage local working directories accessible by Agent", Icons.Filled.Home) { vm.go(Screen.Workspace) }
        }
    }
}

@Composable
private fun ModelPage(vm: AriAiViewModel) {
    PageScaffold("Model Settings", onBack = { vm.go(Screen.Settings) }) {
        SectionLabel("Chat Model")
        ModelPick("Chat Model", "Global default chat model", vm.chatModel, vm) { vm.pickSlot("chat", it) }
        SectionLabel("Fast Model")
        ModelPick("Fast Model", "Model used for titles, chat suggestions, and other fast tasks", vm.fastModel, vm) { vm.pickSlot("fast", it) }
        CardRow("Thinking Budget", "Reasoning", Icons.Filled.Star) { vm.snack = "Thinking budget" }
        ToggleRow("Enable Chat Suggestions", "", "suggestions", vm)
        SectionLabel("Translation Model")
        ModelPick("Translation Model", "The model used for translation features", vm.translateModel, vm) { vm.pickSlot("tr", it) }
        SectionLabel("OCR Model")
        ModelPick("OCR Model", "Model used for optical character recognition on images", vm.ocrModel, vm) { vm.pickSlot("ocr", it) }
        SectionLabel("Compress Model")
        ModelPick("Compress Model", "Model for compressing conversation history", vm.compressModel, vm) { vm.pickSlot("cmp", it) }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { vm.go(Screen.Prompts) }) {
                Icon(Icons.Filled.Edit, null, tint = Mute)
                Text("Prompts", color = Mute, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.clip(RoundedCornerShape(16.dp)).background(AccentSoft).padding(8.dp)) {
                    Icon(Icons.Filled.Person, null, tint = Accent)
                }
                Text("Model", color = Ink, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ModelPick(title: String, hint: String, id: String, vm: AriAiViewModel, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    CardRow(title, if (id.isBlank()) "Select Model" else vm.modelName(id), Icons.Filled.Person) { open = !open }
    Text(hint, color = Mute, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp), textAlign = TextAlign.End)
    if (open) {
        if (vm.providers.isEmpty()) Text("Add a provider first", color = Mute, modifier = Modifier.padding(8.dp))
        vm.providers.forEach { p ->
            CardRow(p.name, p.model, Icons.Filled.Check) { onPick(p.id); open = false }
        }
    }
}

@Composable
private fun ProvidersPage(vm: AriAiViewModel) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://api.openai.com/v1") }
    var model by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    PageScaffold("Providers", onBack = { vm.go(Screen.Settings) }) {
        if (vm.providers.isEmpty()) {
            Text("No available AI providers, please add below", color = Mute)
        }
        vm.providers.forEach { p ->
            CardRow(p.name, p.model, Icons.Filled.Person) { vm.selectProvider(p.id); vm.go(Screen.Chat) }
        }
        SectionLabel("Add provider")
        Field("Name", name) { name = it }
        Field("Base URL", url) { url = it }
        Field("Model id", model) { model = it }
        Field("API key", key) { key = it }
        PrimaryBtn("Save") {
            if (name.isNotBlank() && model.isNotBlank()) {
                vm.addProvider(name, url, model, key)
                name = ""; model = ""; key = ""
            }
        }
    }
}

@Composable
private fun SpeechPage(vm: AriAiViewModel) {
    PageScaffold("Speech", onBack = { vm.go(Screen.Settings) }, extra = {
        IconBtn(Icons.Filled.Add) { vm.snack = "Add speech provider" }
    }) {
        SpeechCard("System TTS", "System TTS", "S", vm.speechId == "sys") { vm.pickSpeech("sys") }
        SpeechCard("AiHubMix", "OpenAI", "A", vm.speechId == "mix") { vm.pickSpeech("mix") }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Phone, null, tint = Mute)
                Text("Speech Recognition", color = Mute, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.clip(RoundedCornerShape(16.dp)).background(AccentSoft).padding(8.dp)) {
                    Icon(Icons.Filled.Notifications, null, tint = Accent)
                }
                Text("Text to Speech", color = Ink, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SpeechCard(title: String, sub: String, badge: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(18.dp))
            .background(if (selected) AccentSoft else CardBg).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Ink)
            Text(sub, color = Mute, fontSize = 13.sp)
            if (selected) {
                Box(Modifier.padding(top = 6.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFC8E6C9)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("Selected", color = Color(0xFF2E7D32), fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(Modifier.size(36.dp).clip(CircleShape).background(if (badge == "S") Color(0xFF90CAF9) else Color(0xFF5C6BC0)), contentAlignment = Alignment.Center) {
            Text(badge, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun McpPage(vm: AriAiViewModel) {
    PageScaffold("MCP", onBack = { vm.go(Screen.Settings) }, extra = {
        Row {
            IconBtn(Icons.Filled.Add) {
                vm.mcpDraft = McpServer(AppStore.id(), "", "", true, "http", "")
            }
            IconBtn(Icons.Filled.Send) { vm.mcpImport = true }
        }
    }) {
        if (vm.mcp.isEmpty()) Text("No MCP servers. Tap + to add.", color = Mute)
        vm.mcp.forEach { s ->
            CardRow(s.name.ifBlank { "Unnamed" }, s.url, Icons.Filled.Build) { vm.mcpDraft = s }
        }
    }
}

@Composable
private fun McpEditor(vm: AriAiViewModel, s: McpServer) {
    var name by remember { mutableStateOf(s.name) }
    var url by remember { mutableStateOf(s.url) }
    var enabled by remember { mutableStateOf(s.enabled) }
    var transport by remember { mutableStateOf(s.transport) }
    var headers by remember { mutableStateOf(s.headers) }
    Overlay({ vm.mcpDraft = null }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Page).padding(20.dp).verticalScroll(rememberScrollState())) {
            Handle()
            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.weight(1f)) { Tab("Tools", false) {} }
                Box(Modifier.weight(1f)) { Tab("Basic Settings", true) {} }
            }
            Spacer(Modifier.height(12.dp))
            Label("Enable")
            Text("Whether to enable this MCP server", color = Mute, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            Switch(enabled, { enabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = Accent))
            Label("Name")
            Text("Display name for the MCP server", color = Mute, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            Field("Name", name) { name = it }
            Label("Transport Type")
            Text("Select the transport protocol type for the MCP server", color = Mute, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                Pill("SSE", transport == "sse") { transport = "sse" }
                Spacer(Modifier.width(8.dp))
                Pill("Streamable HTTP", transport != "sse") { transport = "http" }
            }
            Label("Server URL")
            Text("URL address for Streamable HTTP server", color = Mute, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            Field("URL", url) { url = it }
            Label("Custom Headers")
            Field("Headers", headers) { headers = it }
            Text("Save", color = Link, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable {
                vm.saveMcp(s.copy(name = name, url = url, enabled = enabled, transport = transport, headers = headers))
            }.padding(8.dp))
        }
    }
}

@Composable
private fun ImportMcp(vm: AriAiViewModel) {
    Overlay({ vm.mcpImport = false }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.75f).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Page).padding(20.dp)) {
            Handle()
            Text("Import MCP Server", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            Text("Paste MCP Server JSON config, supports standard mcpServers format", color = Mute, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f).fillMaxWidth().border(1.dp, Chip, RoundedCornerShape(12.dp)).padding(12.dp)) {
                BasicTextField(
                    value = vm.importJson,
                    onValueChange = { vm.importJson = it },
                    textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                    modifier = Modifier.fillMaxSize(),
                    decorationBox = { inner ->
                        if (vm.importJson.isEmpty()) Text("{ { ... } :\"mcpServers\" }", color = Mute)
                        inner()
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Accent).clickable { vm.importMcp(vm.importJson) }.padding(horizontal = 22.dp, vertical = 10.dp)) {
                    Text("Import", color = Color.White, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.width(16.dp))
                Text("Cancel", color = Ink, modifier = Modifier.clickable { vm.mcpImport = false })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsPage(vm: AriAiViewModel) {
    PageScaffold("Statistics", onBack = { vm.go(Screen.Chat) }) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardBg).padding(16.dp)) {
            Text("Chat Heatmap", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(7 * 18) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Chip))
                }
            }
            Text("More  ● ● ○   Less", color = Mute, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Total Messages", vm.store.int("msg_count").toString(), Icons.Filled.Email, Modifier.weight(1f))
            StatCard("Total Conversations", vm.conversations.size.toString(), Icons.Filled.List, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Output Tokens", "0", Icons.Filled.Star, Modifier.weight(1f))
            StatCard("Input Tokens", "0", Icons.Filled.Star, Modifier.weight(1f))
        }
        StatCard("App Launch Count", vm.store.int("launches").toString(), Icons.Filled.Star, Modifier.fillMaxWidth())
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(CardBg).padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Text(title, color = Mute, fontSize = 13.sp)
    }
}

@Composable
private fun HistoryPage(vm: AriAiViewModel) {
    PageScaffold("Chat History", onBack = { vm.go(Screen.Chat) }) {
        if (vm.conversations.isEmpty()) Text("No conversations", color = Mute)
        vm.conversations.forEach { c ->
            CardRow(c.title, c.preview, Icons.Filled.Email) { vm.openConv(c.id) }
        }
    }
}

@Composable
private fun SearchChatsPage(vm: AriAiViewModel) {
    PageScaffold("Search Chats", onBack = { vm.go(Screen.Chat) }) {
        SearchBar("Search chats", vm.chatQuery) { vm.chatQuery = it }
        vm.conversations.filter { it.title.contains(vm.chatQuery, true) || it.preview.contains(vm.chatQuery, true) }.forEach { c ->
            CardRow(c.title, c.preview, Icons.Filled.Search) { vm.openConv(c.id) }
        }
    }
}

@Composable
private fun SimplePage(vm: AriAiViewModel, title: String, body: String) {
    PageScaffold(title, onBack = { vm.go(Screen.Settings) }) {
        Group { Text(body, color = Mute, modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.End) }
        PrimaryBtn("OK") { vm.go(Screen.Settings) }
    }
}

/* ——— chrome ——— */

@Composable
private fun PageScaffold(title: String, onBack: () -> Unit, extra: @Composable () -> Unit = {}, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            extra()
            Spacer(Modifier.weight(1f))
            Text(title, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(40.dp).clip(CircleShape).background(CardBg).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ArrowBack, null, tint = Ink)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Overlay(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0x66000000)).clickable(onClick = onDismiss), contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.clickable(enabled = false) {}) { content() }
    }
}

@Composable private fun Handle() {
    Box(Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(36.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFC5C6CC)))
    }
}

@Composable
private fun IconBtn(icon: ImageVector, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).clip(CircleShape).background(AccentSoft).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun MenuLine(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(title, color = Ink, fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        Icon(icon, null, tint = Ink, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SectionLabel(t: String) {
    Text(t, color = Section, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp, end = 4.dp), textAlign = TextAlign.End)
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardBg)) { content() }
}

@Composable
private fun SettingRow(title: String, sub: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(title, color = Ink, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            if (sub.isNotBlank()) Text(sub, color = Mute, fontSize = 13.sp, textAlign = TextAlign.End)
        }
        Spacer(Modifier.width(12.dp))
        Icon(icon, null, tint = Ink, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ToggleRow(title: String, sub: String, key: String, vm: AriAiViewModel) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Switch(vm.flags[key] == true, { vm.toggle(key) }, colors = SwitchDefaults.colors(checkedTrackColor = Accent))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(title, color = Ink, fontWeight = FontWeight.Medium)
            if (sub.isNotBlank()) Text(sub, color = Mute, fontSize = 13.sp, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun CardRow(title: String, sub: String, icon: ImageVector, selected: Boolean = false, leading: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp))
            .background(if (selected) AccentSoft else CardBg).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Mute, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(title, color = Ink, fontWeight = FontWeight.Medium)
            if (sub.isNotBlank()) Text(sub, color = Mute, fontSize = 12.sp)
        }
        if (leading != null) {
            Spacer(Modifier.width(10.dp))
            leading()
        }
    }
}

@Composable
private fun SheetRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Chip).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(title, color = Ink, fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        Icon(icon, null, tint = Ink)
    }
}

@Composable
private fun SearchBar(hint: String, value: String, onChange: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Chip).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, null, tint = Mute)
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = Ink, fontSize = 15.sp, textAlign = TextAlign.End),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) Text(hint, color = Mute, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    inner()
                }
            }
        )
    }
}

@Composable
private fun Field(hint: String, value: String, onChange: (String) -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, Chip, RoundedCornerShape(14.dp)).background(CardBg).padding(14.dp)) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = Ink, fontSize = 15.sp, textAlign = TextAlign.End),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(hint, color = Mute, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                inner()
            }
        )
    }
}

@Composable
private fun PrimaryBtn(t: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp).clip(RoundedCornerShape(14.dp)).background(Accent).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(t, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Label(t: String) {
    Text(t, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), textAlign = TextAlign.End)
}

@Composable
private fun Tab(t: String, on: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(t, color = if (on) Ink else Mute, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.height(2.dp).fillMaxWidth(0.4f).background(if (on) Accent else Color.Transparent))
    }
}

@Composable
private fun Pill(t: String, on: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(20.dp)).background(if (on) AccentSoft else Chip).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (on) Icon(Icons.Filled.Check, null, Modifier.size(16.dp), tint = Accent)
        Spacer(Modifier.width(4.dp))
        Text(t, color = Ink, fontSize = 13.sp)
    }
}
