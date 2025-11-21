package com.example.inzynierkaallegroolx.data.messages

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val sender: String,
    val body: String,
    val sentAt: String
)
