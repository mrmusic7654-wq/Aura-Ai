package com.aura.ai.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.ai.data.database.daos.ChatMessageDao
import com.aura.ai.data.database.daos.ChatSessionDao
import com.aura.ai.data.database.entities.ChatMessage
import com.aura.ai.data.database.entities.ChatSession

@Database(
    entities = [ChatSession::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
}