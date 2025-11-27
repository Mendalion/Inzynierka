//package com.example.inzynierkaallegroolx.repository
//
//import android.content.Context
//import com.example.inzynierkaallegroolx.data.AppDatabase
//import com.example.inzynierkaallegroolx.data.messages.ConversationEntity
//import com.example.inzynierkaallegroolx.data.messages.MessageEntity
//import com.example.inzynierkaallegroolx.data.messages.MessageTemplateEntity
//import com.example.inzynierkaallegroolx.network.ApiClient
//import com.example.inzynierkaallegroolx.network.ReplyBody
//import com.example.inzynierkaallegroolx.network.TemplateCreateBody
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class MessagesRepository(ctx: Context) {
//    private val db = AppDatabase.getDatabase(ctx)
//
//    private val dao get() = db.messagesDao()
//
//    suspend fun syncConversations(): Result<Unit> = withContext(Dispatchers.IO) {
//        runCatching {
//            val remote = ApiClient.messages.conversations().map { ConversationEntity(it.id, it.platform, it.unreadCount, it.lastMessageAt) }
//            dao.upsertConversations(remote)
//        }
//    }
//
//    suspend fun syncConversation(id: String): Result<Unit> = withContext(Dispatchers.IO) {
//        runCatching {
//            val remote = ApiClient.messages.conversation(id)
//            dao.upsertConversations(listOf(ConversationEntity(remote.id, remote.platform, remote.unreadCount, remote.lastMessageAt)))
//            dao.upsertMessages(remote.messages.map { MessageEntity(it.id, remote.id, it.sender, it.body, it.sentAt) })
//        }
//    }
//
//    suspend fun listConversations() = withContext(Dispatchers.IO) { dao.conversations() }
//    suspend fun listMessages(conversationId: String) = withContext(Dispatchers.IO) { dao.messages(conversationId) }
//
//    suspend fun reply(conversationId: String, body: String): Result<Unit> = withContext(Dispatchers.IO) {
//        runCatching {
//            val msg = ApiClient.messages.reply(conversationId, ReplyBody(body))
//            dao.upsertMessages(listOf(MessageEntity(msg.id, conversationId, msg.sender, msg.body, msg.sentAt)))
//        }
//    }
//
//    suspend fun syncTemplates(): Result<Unit> = withContext(Dispatchers.IO) {
//        runCatching {
//            val remote = ApiClient.messages.templates().map { MessageTemplateEntity(it.id, it.title, it.body) }
//            dao.upsertTemplates(remote)
//        }
//    }
//
//    suspend fun createTemplate(title: String, body: String): Result<Unit> = withContext(Dispatchers.IO) {
//        runCatching {
//            val tpl = ApiClient.messages.createTemplate(TemplateCreateBody(title, body))
//            dao.upsertTemplates(listOf(MessageTemplateEntity(tpl.id, tpl.title, tpl.body)))
//        }
//    }
//
//    suspend fun deleteTemplate(id: String): Result<Unit> = withContext(Dispatchers.IO) {
//        runCatching {
//            ApiClient.messages.deleteTemplate(id)
//            dao.deleteTemplate(id)
//        }
//    }
//
//    suspend fun templates() = withContext(Dispatchers.IO) { dao.templates() }
//}