package com.ariai.app.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ariai.app.data.ChatMessage
import com.ariai.app.data.ChatMode
import com.ariai.app.data.Provider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AriAiApp(vm: AriAiViewModel) {
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var showProviders by remember { mutableStateOf(false) }

    LaunchedEffect(vm.snack) {
        vm.snack?.let {
            snack.showSnackbar(it)
            vm.snack = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            SideMenu(
                vm = vm,
                onNew = {
                    vm.openNewChat()
                    scope.launch { drawer.close() }
                },
                onOpen = {
                    vm.openConversation(it)
                    scope.launch { drawer.close() }
                },
                onProviders = {
                    showProviders = true
                    scope.launch { drawer.close() }
                },
                onClose = { scope.launch { drawer.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = Bg,
            snackbarHost = { SnackbarHost(snack) }
        ) { pad ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                if (showProviders && vm.current == null) {
                    ProvidersScreen(
                        vm = vm,
                        onBack = { showProviders = false }
                    )
                } else if (vm.current == null) {
                    HomeScreen(
                        vm = vm,
                        onMenu = { scope.launch { drawer.open() } },
                        onProviders = { showModelSheet = true }
                    )
                } else {
                    ChatScreen(
                        vm = vm,
                        onMenu = { scope.launch { drawer.open() } },
                        onProviders = { showModelSheet = true }
                    )
                }
                if (showModelSheet) {
                    ModelPicker(
                        vm = vm,
                        onManage = {
                            showModelSheet = false
                            vm.closeChat()
                            showProviders = true
                        },
                        onDismiss = { showModelSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun SideMenu(
    vm: AriAiViewModel,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onProviders: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0E141C),
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("AriAi", color = TextPri, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text("دستیار هوشمند تو", color = TextSec, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            MenuRow(Icons.Outlined.EditNote, "گفتگوی جدید", onNew)
            MenuRow(Icons.Outlined.AutoAwesome, "مدل‌ها و Providers", onProviders)
            Spacer(Modifier.height(16.dp))
            Text("تاریخچه", color = TextSec, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(vm.conversations, key = { it.id }) { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpen(c.id) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, null, tint = TextSec, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.title, color = TextPri, fontSize = 14.sp, maxLines = 1)
                            if (c.preview.isNotBlank()) {
                                Text(c.preview, color = TextSec, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "حذف",
                            tint = TextSec,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { vm.deleteConversation(c.id) }
                        )
                    }
                }
            }
            MenuRow(Icons.Outlined.Settings, "تنظیمات", onProviders)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Accent2, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, color = TextPri, fontWeight = FontWeight.Medium)
    }
    Spacer(Modifier.height(8.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(vm: AriAiViewModel, onMenu: () -> Unit, onProviders: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = "AriAi",
            subtitle = vm.selectedProvider?.name ?: "انتخاب مدل",
            onMenu = onMenu,
            onTitleClick = onProviders
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("چه کمکی از دستم برمیاد؟", color = TextPri, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("یک حالت انتخاب کن؛ چت با همان حالت باز می‌شود.", color = TextSec, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionChip("تصویر", Icons.Outlined.Image) { vm.openNewChat(ChatMode.Image) }
                ActionChip("نوشتن", Icons.Outlined.EditNote) { vm.openNewChat(ChatMode.Write) }
                ActionChip("کد", Icons.Outlined.Code) { vm.openNewChat(ChatMode.Code) }
                ActionChip("ترجمه", Icons.Outlined.Translate) { vm.openNewChat(ChatMode.Translate) }
                ActionChip("خلاصه", Icons.Outlined.ShortText) { vm.openNewChat(ChatMode.Summarize) }
                ActionChip("تحلیل", Icons.Outlined.Insights) { vm.openNewChat(ChatMode.Analyze) }
            }
        }
        Composer(
            vm = vm,
            onSend = {
                if (vm.current == null) vm.openNewChat(ChatMode.Chat, vm.input)
                vm.send()
            }
        )
    }
}

@Composable
private fun ActionChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFF2A3548), RoundedCornerShape(22.dp))
            .background(Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = TextPri, fontSize = 14.sp)
    }
}

@Composable
fun ChatScreen(vm: AriAiViewModel, onMenu: () -> Unit, onProviders: () -> Unit) {
    val conv = vm.current ?: return
    val list = rememberLazyListState()
    LaunchedEffect(conv.messages.size) {
        if (conv.messages.isNotEmpty()) list.animateScrollToItem(conv.messages.lastIndex)
    }
    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = conv.mode.labelFa,
            subtitle = vm.selectedProvider?.name ?: "مدل",
            onMenu = onMenu,
            onTitleClick = onProviders,
            showBack = true,
            onBack = { vm.closeChat() }
        )
        if (conv.messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(conv.mode.labelFa, color = TextPri, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        when (conv.mode) {
                            ChatMode.Image -> "موضوع تصویر را بنویس یا با + عکس بفرست"
                            ChatMode.Code -> "سوالت را بنویس تا کد بسازم"
                            ChatMode.Write -> "موضوع متن را بگو"
                            else -> "پیام بفرست. دکمه + برای پیوست کار می‌کند"
                        },
                        color = TextSec,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                state = list,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conv.messages, key = { it.id }) { Bubble(it) }
                if (vm.sending) {
                    item {
                        Text("در حال نوشتن…", color = TextSec, fontSize = 13.sp, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
        Composer(vm, onSend = { vm.send() })
    }
}

@Composable
private fun Bubble(m: ChatMessage) {
    val mine = m.role == ChatMessage.Role.User
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (mine) Accent else Card)
                .padding(12.dp)
        ) {
            if (m.attachmentName != null) {
                Text("پیوست: ${m.attachmentName}", color = if (mine) Color.White.copy(0.85f) else Accent2, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
            }
            Text(m.text, color = if (mine) Color.White else TextPri, fontSize = 15.sp)
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    subtitle: String,
    onMenu: () -> Unit,
    onTitleClick: () -> Unit,
    showBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = if (showBack) onBack else onMenu) {
            Icon(if (showBack) Icons.Outlined.Close else Icons.Outlined.Menu, null, tint = TextPri)
        }
        Column(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onTitleClick)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = TextPri, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = TextSec, modifier = Modifier.size(18.dp))
            }
            Text(subtitle, color = TextSec, fontSize = 12.sp)
        }
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
fun Composer(vm: AriAiViewModel, onSend: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(12.dp)
    ) {
        AnimatedVisibility(vm.plusOpen) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttachAction("گالری", Icons.Outlined.PhotoLibrary) { vm.attach("gallery") }
                AttachAction("دوربین", Icons.Outlined.CameraAlt) { vm.attach("camera") }
                AttachAction("فایل", Icons.Outlined.AttachFile) { vm.attach("file") }
                AttachAction("صدا", Icons.Outlined.Mic) { vm.attach("voice") }
            }
        }
        if (vm.pendingAttachment != null) {
            Row(
                Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Card)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("پیوست: ${vm.pendingAttachment}", color = Accent2, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Close,
                    null,
                    tint = TextSec,
                    modifier = Modifier.size(16.dp).clickable { vm.pendingAttachment = null }
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Card)
                .border(1.dp, Color(0xFF2A3548), RoundedCornerShape(28.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.plusOpen = !vm.plusOpen }) {
                Icon(
                    if (vm.plusOpen) Icons.Outlined.Close else Icons.Outlined.Add,
                    contentDescription = "پیوست",
                    tint = if (vm.plusOpen) Accent else TextPri
                )
            }
            BasicTextField(
                value = vm.input,
                onValueChange = { vm.input = it },
                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                textStyle = TextStyle(color = TextPri, fontSize = 16.sp),
                cursorBrush = SolidColor(Accent),
                decorationBox = { inner ->
                    if (vm.input.isEmpty()) Text("پیام…", color = TextSec, fontSize = 16.sp)
                    inner()
                }
            )
            val canSend = vm.input.isNotBlank() || vm.pendingAttachment != null
            Box(
                Modifier
                    .padding(end = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (canSend) Accent else Color(0xFF2A3548))
                    .clickable(enabled = canSend && !vm.sending, onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ArrowUpward, contentDescription = "ارسال", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AttachAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextPri, fontSize = 12.sp)
    }
}

@Composable
fun ProvidersScreen(vm: AriAiViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://api.openai.com/v1") }
    var model by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.Close, null, tint = TextPri)
            }
            Text("Providers", color = TextPri, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("مدل‌هایی که اضافه کردی را انتخاب کن. کلید API فقط روی همین دستگاه ذخیره می‌شود.", color = TextSec, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.providers, key = { it.id }) { p ->
                ProviderRow(p, selected = p.id == vm.selectedProviderId, onSelect = { vm.selectProvider(p.id) }, onDelete = {
                    if (!p.isBuiltIn) vm.removeProvider(p.id)
                })
            }
        }
        Text("افزودن مدل سفارشی", color = TextPri, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Field("نام", name) { name = it }
        Field("Base URL", url) { url = it }
        Field("Model id", model) { model = it }
        Field("API key", key) { key = it }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Accent)
                .clickable {
                    if (name.isNotBlank() && model.isNotBlank()) {
                        vm.addProvider(name.trim(), url.trim(), model.trim(), key.trim())
                        name = ""; model = ""; key = ""
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("افزودن Provider", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProviderRow(p: Provider, selected: Boolean, onSelect: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (selected) Accent else Color(0xFF2A3548), RoundedCornerShape(16.dp))
            .background(if (selected) Accent.copy(0.15f) else Card)
            .clickable(onClick = onSelect)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(p.name, color = TextPri, fontWeight = FontWeight.Medium)
            Text("${p.model} · ${p.baseUrl}", color = TextSec, fontSize = 12.sp, maxLines = 1)
        }
        if (!p.isBuiltIn) {
            Icon(Icons.Outlined.Delete, null, tint = TextSec, modifier = Modifier.clickable(onClick = onDelete))
        }
    }
}

@Composable
private fun ModelPicker(vm: AriAiViewModel, onManage: () -> Unit, onDismiss: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Surface)
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            Text("Providers", color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("مدل‌های تو — انتخاب کن تا در همین چت استفاده شود", color = TextSec, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            vm.providers.forEach { p ->
                ProviderRow(
                    p = p,
                    selected = p.id == vm.selectedProviderId,
                    onSelect = {
                        vm.selectProvider(p.id)
                        onDismiss()
                    },
                    onDelete = { if (!p.isBuiltIn) vm.removeProvider(p.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Card)
                    .clickable(onClick = onManage)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("مدیریت و افزودن مدل", color = Accent2, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Field(hint: String, value: String, onChange: (String) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .padding(12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = TextPri, fontSize = 15.sp),
            cursorBrush = SolidColor(Accent),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(hint, color = TextSec)
                inner()
            }
        )
    }
}
