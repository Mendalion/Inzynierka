package com.example.inzynierkaallegroolx.network

import retrofit2.http.*

data class ConversationDto(val id: String, val platform: String, val unreadCount: Int, val lastMessageAt: String?)
data class MessageDto(val id: String, val sender: String, val body: String, val sentAt: String)
data class ReplyBody(val body: String)

data class TemplateDto(val id: String, val title: String, val body: String)
data class TemplateCreateBody(val title: String, val body: String)

interface MessagesApi {
    @GET("messages/conversations") suspend fun conversations(): List<ConversationDto>
    @GET("messages/conversations/{id}") suspend fun conversation(@Path("id") id: String): ConversationWithMessagesDto
    @POST("messages/conversations/{id}/reply") suspend fun reply(@Path("id") id: String, @Body body: ReplyBody): MessageDto
    @GET("messages/unread/count") suspend fun unread(): UnreadCountDto
    @GET("messages/templates") suspend fun templates(): List<TemplateDto>
    @POST("messages/templates") suspend fun createTemplate(@Body body: TemplateCreateBody): TemplateDto
    @DELETE("messages/templates/{id}") suspend fun deleteTemplate(@Path("id") id: String): Map<String, Any>
}

data class ConversationWithMessagesDto(val id: String, val platform: String, val unreadCount: Int, val lastMessageAt: String?, val messages: List<MessageDto>)
data class UnreadCountDto(val unread: Int)
