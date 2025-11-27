package com.example.inzynierkaallegroolx.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.inzynierkaallegroolx.data.listings.ListingDao
import com.example.inzynierkaallegroolx.data.listings.ListingEntity
//import com.example.inzynierkaallegroolx.data.messages.ConversationEntity
//import com.example.inzynierkaallegroolx.data.messages.MessageEntity
//import com.example.inzynierkaallegroolx.data.messages.MessageTemplateEntity
//import com.example.inzynierkaallegroolx.data.messages.MessagesDao

@Database(
    entities = [
        ListingEntity::class,
//        ConversationEntity::class,
//        MessageEntity::class,
//        MessageTemplateEntity::class
        // AuthState zazwyczaj nie jest encją Room, chyba że tak zdefiniowałeś
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
//    abstract fun conversationDao(): MessagesDao do usuniecia
//    abstract fun messageDao(): MessagesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() //dla bezpieczeństwa w dev
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}