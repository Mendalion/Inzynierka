package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inzynierkaallegroolx.viewmodel.MessagesViewModel

@Composable
fun MessagesScreen(vm: MessagesViewModel = viewModel(), onOpen: (String) -> Unit) {
    val state by vm.listState.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = vm::loadConversations) { Text("Sync Conversations") }
        LazyColumn { items(state.items) { c ->
            Column(Modifier.clickable { onOpen(c.id) }.padding(8.dp)) {
                Text("${c.platform} #${c.id}")
                Text("Unread: ${c.unreadCount}")
            }
        } }
        state.error?.let { Text("Error: $it") }
    }
}
