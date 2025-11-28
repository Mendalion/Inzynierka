package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inzynierkaallegroolx.ui.components.LoadingBox
import com.example.inzynierkaallegroolx.viewmodel.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    conversationId: String,
    viewModel: MessagesViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.chatState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    //menu rozszerzane dla dodania zdjec/template
    var showPlusMenu by remember { mutableStateOf(false) }

    LaunchedEffect(conversationId) {
        viewModel.enterConversation(conversationId)
    }
    //automatycznie przewijane w dół
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Czat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        snackbarHost = {
            state.error?.let {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
                ) { Text(it) }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.messages) { msg ->
                    MessageBubble(
                        text = msg.body,
                        isMe = msg.sender == "ME",
                        timestamp = msg.sentAt
                    )
                }
            }

            if (state.isLoading && state.messages.isEmpty()) {
                LoadingBox()
            }

            //input wpisywania
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //Przycisk Plus (TODO)-template-zdjecia
                    Box {
                        IconButton(onClick = { showPlusMenu = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj")
                        }
                        DropdownMenu(
                            expanded = showPlusMenu,
                            onDismissRequest = { showPlusMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Dodaj zdjęcie") },
                                onClick = { showPlusMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Wybierz szablon") },
                                onClick = { showPlusMenu = false }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Napisz wiadomość...") },
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        },
                        enabled = messageText.isNotBlank() && !state.isSending
                    ) {
                        if (state.isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Wyślij")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, isMe: Boolean, timestamp: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = timestamp,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp),
            color = Color.Gray
        )
    }
}