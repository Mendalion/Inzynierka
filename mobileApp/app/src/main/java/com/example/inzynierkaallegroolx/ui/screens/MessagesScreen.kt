package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inzynierkaallegroolx.ui.components.AppTopBar
import com.example.inzynierkaallegroolx.ui.components.ErrorText
import com.example.inzynierkaallegroolx.ui.components.LoadingBox
import com.example.inzynierkaallegroolx.viewmodel.MessagesViewModel

@Composable
fun MessagesScreen(
    viewModel: MessagesViewModel = viewModel(),
    navController: NavController,
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.conversationsState.collectAsState()

    //ładowanie danych przy starcie ekranu
    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold(
        topBar = {
            AppTopBar("Wiadomości", navController, showBackArrow = true, showAvatar = false)
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.items.isEmpty() && !state.isLoading) {
                Text(
                    text = "Brak konwersacji",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items) { conversation ->
                        ConversationItem(
                            platform = conversation.platform,
                            unreadCount = conversation.unreadCount,
                            lastMessageDate = conversation.lastMessageAt,
                            onClick = { onNavigateToDetail(conversation.id) }
                        )
                    }
                }
            }

            if (state.isLoading && state.items.isEmpty()) {
                LoadingBox()
            }

            state.error?.let {
                ErrorText(
                    error = it,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    platform: String,
    unreadCount: Int,
    lastMessageDate: String?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //czy potrzebny awatar?
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Oferta z serwisu $platform",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                lastMessageDate?.let {
                    Text(
                        text = "Ostatnia aktywność: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (unreadCount > 0) {
                Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text(
                        text = unreadCount.toString(),
                        modifier = Modifier.padding(4.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}