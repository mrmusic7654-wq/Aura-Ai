package com.aura.ai.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.aura.ai.data.database.daos.ChatMessageDao
import com.aura.ai.data.database.daos.ChatSessionDao
import com.aura.ai.data.database.entities.ChatMessage
import com.aura.ai.data.database.entities.ChatSession

@Database(
    entities = [ChatSession::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)  // Add this line!
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_qwen3.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
