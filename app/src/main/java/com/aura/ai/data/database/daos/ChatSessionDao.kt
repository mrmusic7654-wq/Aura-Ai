package com.aura.ai.data.database.daos

import androidx.room.*
import com.aura.ai.data.database.entities.ChatSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    
    @Insert
    suspend fun insert(session: ChatSession): Long  // Returns Long, not String
    
    @Update
    suspend fun update(session: ChatSession)
    
    @Delete
    suspend fun delete(session: ChatSession)
    
    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>
    
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: String): ChatSession?
    
    @Query("UPDATE chat_sessions SET title = :title WHERE id = :sessionId")
    suspend fun updateTitle(sessionId: String, title: String)
    
    @Query("UPDATE chat_sessions SET isPinned = :isPinned WHERE id = :sessionId")
    suspend fun setPinned(sessionId: String, isPinned: Boolean)
    
    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)
}
