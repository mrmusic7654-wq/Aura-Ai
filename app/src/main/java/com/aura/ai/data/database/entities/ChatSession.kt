package com.aura.ai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val messageCount: Int = 0,
    val isPinned: Boolean = false
)
