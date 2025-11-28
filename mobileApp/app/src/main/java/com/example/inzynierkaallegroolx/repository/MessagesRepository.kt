package com.example.inzynierkaallegroolx.repository

import android.content.Context
import com.example.inzynierkaallegroolx.data.AppDatabase
import com.example.inzynierkaallegroolx.data.messages.ConversationEntity
import com.example.inzynierkaallegroolx.data.messages.MessageEntity
import com.example.inzynierkaallegroolx.data.messages.MessageTemplateEntity
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ReplyBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesRepository(ctx: Context) {
    private val db = AppDatabase.getDatabase(ctx)
    private val dao get() = db.messageDao()

    suspend fun syncConversations(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val remoteDtos = ApiClient.messages.conversations()
            val entities = remoteDtos.map {
                ConversationEntity(
                    id = it.id,
                    platform = it.platform,
                    unreadCount = it.unreadCount,
                    lastMessageAt = it.lastMessageAt
                )
            }
            dao.upsertConversations(entities)
        }
    }

    //Pobiera szczegóły konwersacji, aktualizując cache
    suspend fun syncConversation(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val remote = ApiClient.messages.conversation(id)

            //aktualizujemy sama konwersację
            dao.upsertConversations(listOf(
                ConversationEntity(remote.id, remote.platform, remote.unreadCount, remote.lastMessageAt)
            ))

            //aktualizujemy wiadomości
            val messages = remote.messages.map {
                MessageEntity(it.id, remote.id, it.sender, it.body, it.sentAt)
            }
            dao.upsertMessages(messages)
        }
    }

    //odczyt z bazy danych
    suspend fun getConversations(): List<ConversationEntity> = withContext(Dispatchers.IO) {
        dao.conversations()
    }

    //odczyt wiadomości od konkretnej osoby
    suspend fun getMessages(conversationId: String): List<MessageEntity> = withContext(Dispatchers.IO) {
        dao.messages(conversationId)
    }


    //wysyłanie
    suspend fun reply(conversationId: String, body: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val msgDto = ApiClient.messages.reply(conversationId, ReplyBody(body))
            dao.upsertMessages(listOf(
                MessageEntity(msgDto.id, conversationId, msgDto.sender, msgDto.body, msgDto.sentAt)
            ))
        }
    }

    //Szablony Todo
    suspend fun syncTemplates(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val remote = ApiClient.messages.templates().map { MessageTemplateEntity(it.id, it.title, it.body) }
            dao.upsertTemplates(remote)
        }
    }

    suspend fun getTemplates() = withContext(Dispatchers.IO) { dao.templates() }
}