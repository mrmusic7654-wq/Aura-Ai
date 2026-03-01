package com.aura.ai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date = Date(),
    val tokenCount: Int = 0
)
