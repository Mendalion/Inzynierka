package com.example.inzynierkaallegroolx.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.inzynierkaallegroolx.data.listings.ListingDao
import com.example.inzynierkaallegroolx.data.listings.ListingEntity
import com.example.inzynierkaallegroolx.data.messages.ConversationEntity
import com.example.inzynierkaallegroolx.data.messages.MessageEntity
import com.example.inzynierkaallegroolx.data.messages.MessageTemplateEntity
import com.example.inzynierkaallegroolx.data.messages.MessagesDao

@Database(entities = [ListingEntity::class, ConversationEntity::class, MessageEntity::class, MessageTemplateEntity::class], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun listings(): ListingDao
    abstract fun messages(): MessagesDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
