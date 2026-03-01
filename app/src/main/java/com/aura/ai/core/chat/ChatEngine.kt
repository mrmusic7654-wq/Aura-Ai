package com.aura.ai.core.chat

import android.content.Context
import android.util.Log
import com.aura.ai.core.model.Qwen3Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatEngine(
    private val context: Context,
    private val qwen3Model: Qwen3Model
) {
    private val TAG = "ChatEngine"
    
    private val systemPrompt = "You are Aura AI, a helpful, friendly, and intelligent assistant powered by Qwen3. You provide thoughtful, accurate, and concise responses."
    
    suspend fun generateResponse(
        conversationHistory: List<ChatMessage>,
        newMessage: String
    ): String = withContext(Dispatchers.Default) {
        
        if (!qwen3Model.isLoaded()) {
            return@withContext "⚠️ Model not loaded. Please select a model in Model Manager first."
        }
        
        val prompt = buildPrompt(conversationHistory, newMessage)
        
        try {
            qwen3Model.generateResponse(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response: ${e.message}")
            "Sorry, I encountered an error: ${e.message}"
        }
    }
    
    private fun buildPrompt(
        history: List<ChatMessage>,
        newMessage: String
    ): String {
        val builder = StringBuilder()
        
        builder.append(systemPrompt)
        builder.append("\n\n")
        
        val recentHistory = history.takeLast(10)
        
        for (msg in recentHistory) {
            val role = if (msg.isFromUser) "User" else "Assistant"
            builder.append("$role: ${msg.content}\n")
        }
        
        builder.append("User: $newMessage\n")
        builder.append("Assistant:")
        
        return builder.toString()
    }
    
    fun unloadModel() {
        qwen3Model.unloadModel()
    }
    
    data class ChatMessage(
        val content: String,
        val isFromUser: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )
}