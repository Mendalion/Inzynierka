package com.example.inzynierkaallegroolx.data.messages

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessagesDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    suspend fun conversations(): List<ConversationEntity>
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY sentAt ASC")
    suspend fun messages(conversationId: String): List<MessageEntity>
    @Query("SELECT * FROM message_templates")
    suspend fun templates(): List<MessageTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversations(list: List<ConversationEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(list: List<MessageEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplates(list: List<MessageTemplateEntity>)

    @Query("DELETE FROM message_templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)
}
