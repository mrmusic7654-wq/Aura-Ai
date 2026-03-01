package com.aura.ai.data.repository

import com.aura.ai.core.chat.ChatEngine
import com.aura.ai.core.model.Qwen3Tokenizer
import com.aura.ai.data.database.AppDatabase
import com.aura.ai.data.database.entities.ChatMessage as DbMessage
import com.aura.ai.data.database.entities.ChatSession as DbSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val database: AppDatabase,
    private val chatEngine: ChatEngine,
    private val tokenizer: Qwen3Tokenizer
) {
    
    private val sessionDao = database.chatSessionDao()
    private val messageDao = database.chatMessageDao()
    
    suspend fun createSession(title: String = "New Conversation"): String {
        val session = DbSession(
            title = title,
            createdAt = Date(),
            updatedAt = Date()
        )
        val id = sessionDao.insert(session)  // Returns Long
        return session.id  // Return the UUID string, not the Long
    }
    
    suspend fun sendMessage(sessionId: String, content: String): ChatMessage {
        val tokenCount = tokenizer.countTokens(content)
        
        val userMessage = DbMessage(
            sessionId = sessionId,
            content = content,
            isFromUser = true,
            tokenCount = tokenCount
        )
        messageDao.insert(userMessage)
        
        val history = getConversationHistory(sessionId)
        val response = chatEngine.generateResponse(history, content)
        
        val responseTokens = tokenizer.countTokens(response)
        
        val aiMessage = DbMessage(
            sessionId = sessionId,
            content = response,
            isFromUser = false,
            tokenCount = responseTokens
        )
        messageDao.insert(aiMessage)
        
        updateSessionMetadata(sessionId)
        
        return ChatMessage(
            id = aiMessage.id,
            sessionId = sessionId,
            content = response,
            isFromUser = false,
            timestamp = aiMessage.timestamp
        )
    }
    
    private suspend fun getConversationHistory(sessionId: String): List<ChatEngine.ChatMessage> {
        return messageDao.getMessagesForSessionSync(sessionId).map { msg ->
            ChatEngine.ChatMessage(
                content = msg.content,
                isFromUser = msg.isFromUser,
                timestamp = msg.timestamp.time
            )
        }
    }
    
    private suspend fun updateSessionMetadata(sessionId: String) {
        val session = sessionDao.getSession(sessionId) ?: return
        
        if (session.messageCount == 0) {
            val firstMessage = messageDao.getLastMessage(sessionId)
            firstMessage?.let {
                val newTitle = if (it.content.length > 30) {
                    it.content.substring(0, 27) + "..."
                } else {
                    it.content
                }
                sessionDao.updateTitle(sessionId, newTitle)
            }
        }
        
        val messageCount = messageDao.getMessageCount(sessionId)
        val updatedSession = session.copy(
            messageCount = messageCount,
            updatedAt = Date()
        )
        sessionDao.update(updatedSession)
    }
    
    fun getAllSessions(): Flow<List<ChatSession>> {
        return sessionDao.getAllSessions().map { sessions ->
            sessions.map { session ->
                ChatSession(
                    id = session.id,
                    title = session.title,
                    createdAt = session.createdAt,
                    updatedAt = session.updatedAt,
                    messageCount = session.messageCount,
                    isPinned = session.isPinned
                )
            }
        }
    }
    
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForSession(sessionId).map { messages ->
            messages.map { msg ->
                ChatMessage(
                    id = msg.id,
                    sessionId = msg.sessionId,
                    content = msg.content,
                    isFromUser = msg.isFromUser,
                    timestamp = msg.timestamp
                )
            }
        }
    }
    
    fun searchMessages(query: String): Flow<List<ChatMessage>> {
        return messageDao.searchMessages(query).map { messages ->
            messages.map { msg ->
                ChatMessage(
                    id = msg.id,
                    sessionId = msg.sessionId,
                    content = msg.content,
                    isFromUser = msg.isFromUser,
                    timestamp = msg.timestamp
                )
            }
        }
    }
    
    suspend fun deleteSession(sessionId: String) {
        messageDao.deleteMessagesForSession(sessionId)
        sessionDao.deleteById(sessionId)
    }
    
    suspend fun togglePin(sessionId: String, isPinned: Boolean) {
        sessionDao.setPinned(sessionId, isPinned)
    }
    
    suspend fun renameSession(sessionId: String, newTitle: String) {
        sessionDao.updateTitle(sessionId, newTitle)
    }
    
    data class ChatMessage(
        val id: String,
        val sessionId: String,
        val content: String,
        val isFromUser: Boolean,
        val timestamp: Date
    )
    
    data class ChatSession(
        val id: String,
        val title: String,
        val createdAt: Date,
        val updatedAt: Date,
        val messageCount: Int,
        val isPinned: Boolean
    )
}
