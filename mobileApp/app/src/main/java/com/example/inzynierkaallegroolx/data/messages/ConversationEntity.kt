package com.example.inzynierkaallegroolx.data.messages

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val platform: String,
    val unreadCount: Int,
    val lastMessageAt: String?
)
