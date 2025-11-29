package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.data.messages.ConversationEntity
import com.example.inzynierkaallegroolx.data.messages.MessageEntity
import com.example.inzynierkaallegroolx.repository.MessagesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConversationsState(
    val isLoading: Boolean = false,
    val items: List<ConversationEntity> = emptyList(),
    val error: String? = null
)

data class ChatState(
    val isLoading: Boolean = false,
    val conversationId: String? = null,
    val messages: List<MessageEntity> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null
)

class MessagesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MessagesRepository(app)

    private val _conversationsState = MutableStateFlow(ConversationsState())
    val conversationsState: StateFlow<ConversationsState> = _conversationsState.asStateFlow()

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    //ładowanie listy konwersacji
    fun loadConversations() {
        viewModelScope.launch {
            _conversationsState.value = _conversationsState.value.copy(isLoading = true)

            //ładujemy dane z bazy
            val localItems = repo.getConversations()
            _conversationsState.value = ConversationsState(items = localItems, isLoading = true)

            //próbujemy zsynchronizować z serwerem
            val result = repo.syncConversations()

            //odswieżamy liste z bazy po synchronizacji
            val updatedItems = repo.getConversations()

            _conversationsState.value = ConversationsState(
                items = updatedItems,
                isLoading = false,
                error = result.exceptionOrNull()?.message
            )
        }
    }

    //konkretna rozmowa
    fun enterConversation(id: String) {
        viewModelScope.launch {
            _chatState.value = ChatState(conversationId = id, isLoading = true)

            //ładujemy lokalne wiadomości
            val localMsgs = repo.getMessages(id)
            _chatState.value = _chatState.value.copy(messages = localMsgs)

            //synchronizujemy szczegóły
            val result = repo.syncConversation(id)

            //odswieżamy
            val updatedMsgs = repo.getMessages(id)
            _chatState.value = _chatState.value.copy(
                messages = updatedMsgs,
                isLoading = false,
                error = result.exceptionOrNull()?.message
            )
        }
    }

    //wysyłanie wiadomości
    fun sendMessage(body: String) {
        val currentId = _chatState.value.conversationId ?: return
        if (body.isBlank()) return

        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isSending = true, error = null)

            val result = repo.reply(currentId, body)

            if (result.isSuccess) {
                //odświeżamy listę wiadomości z bazy
                val updatedMsgs = repo.getMessages(currentId)
                _chatState.value = _chatState.value.copy(
                    messages = updatedMsgs,
                    isSending = false
                )
            } else {
                _chatState.value = _chatState.value.copy(
                    isSending = false,
                    error = "Nie udało się wysłać: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun clearError() {
        _chatState.value = _chatState.value.copy(error = null)
    }
}