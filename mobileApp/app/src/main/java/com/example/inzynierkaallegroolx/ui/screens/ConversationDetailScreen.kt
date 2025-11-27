//package com.example.inzynierkaallegroolx.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.inzynierkaallegroolx.repository.MessagesRepository
//import com.example.inzynierkaallegroolx.viewmodel.MessagesViewModel
//import kotlinx.coroutines.launch
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.List
//
//@Composable
//fun ConversationDetailScreen(id: String, vm: MessagesViewModel = viewModel(), onBack: () -> Unit) {
//    val state by vm.detailState.collectAsState()
//    var replyBody by remember { mutableStateOf("") }
//    val ctx = LocalContext.current
//    val scope = rememberCoroutineScope()
//    var expanded by remember { mutableStateOf(false) }
//    var templates by remember { mutableStateOf(listOf<com.example.inzynierkaallegroolx.data.messages.MessageTemplateEntity>()) }
//    LaunchedEffect(id) { vm.openConversation(id); scope.launch { templates = MessagesRepository(ctx).templates() } }
//    Column(Modifier.fillMaxSize().padding(16.dp)) {
//        Button(onClick = onBack) { Text("Back") }
//        LazyColumn(modifier = Modifier.weight(1f)) { items(state.messages) { m -> Text("${m.sender}: ${m.body}") } }
//        Row { OutlinedTextField(replyBody, { replyBody = it }, label = { Text("Reply") }, modifier = Modifier.weight(1f))
//            IconButton(onClick = { expanded = true }) { androidx.compose.material3.Icon(Icons.Default.List, contentDescription = "Templates") }
//            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//                templates.forEach { t -> DropdownMenuItem(text = { Text(t.title) }, onClick = { replyBody = t.body; expanded = false }) }
//            }
//        }
//        Button(onClick = { vm.reply(id, replyBody); replyBody = "" }, enabled = replyBody.isNotBlank()) { Text("Send") }
//        state.error?.let { Text("Error: $it") }
//    }
//}
