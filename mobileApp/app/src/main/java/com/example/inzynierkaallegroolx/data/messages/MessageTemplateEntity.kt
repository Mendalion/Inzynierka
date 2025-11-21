package com.example.inzynierkaallegroolx.data.messages

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_templates")
data class MessageTemplateEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String
)
