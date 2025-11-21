package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.repository.MessagesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ConversationsState(
    val loading: Boolean = false,
    val items: List<com.example.inzynierkaallegroolx.data.messages.ConversationEntity> = emptyList(),
    val error: String? = null
)

data class ConversationDetailState(
    val loading: Boolean = false,
    val messages: List<com.example.inzynierkaallegroolx.data.messages.MessageEntity> = emptyList(),
    val error: String? = null
)

class MessagesViewModel(app: Application): AndroidViewModel(app) {
    private val repo = MessagesRepository(app)

    private val _listState = MutableStateFlow(ConversationsState())
    val listState: StateFlow<ConversationsState> = _listState

    private val _detailState = MutableStateFlow(ConversationDetailState())
    val detailState: StateFlow<ConversationDetailState> = _detailState

    fun loadConversations() {
        _listState.value = _listState.value.copy(loading = true)
        viewModelScope.launch {
            val r = repo.syncConversations()
            val items = repo.listConversations()
            _listState.value = if (r.isSuccess) ConversationsState(items = items) else ConversationsState(error = r.exceptionOrNull()?.message)
        }
    }

    fun openConversation(id: String) {
        _detailState.value = ConversationDetailState(loading = true)
        viewModelScope.launch {
            val r = repo.syncConversation(id)
            val msgs = repo.listMessages(id)
            _detailState.value = if (r.isSuccess) ConversationDetailState(messages = msgs) else ConversationDetailState(error = r.exceptionOrNull()?.message)
        }
    }

    fun reply(id: String, body: String) {
        viewModelScope.launch {
            val r = repo.reply(id, body)
            if (r.isSuccess) openConversation(id) else _detailState.value = _detailState.value.copy(error = r.exceptionOrNull()?.message)
        }
    }
}
