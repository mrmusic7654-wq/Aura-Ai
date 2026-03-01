package com.aura.ai.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _messages = MutableLiveData<List<ChatRepository.ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatRepository.ChatMessage>> = _messages
    
    private val _sessionTitle = MutableLiveData("New Conversation")
    val sessionTitle: LiveData<String> = _sessionTitle
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private var currentSessionId: String? = null
    
    fun createNewSession() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sessionId = chatRepository.createSession()
                currentSessionId = sessionId
                loadSession(sessionId)
            } catch (e: Exception) {
                _error.value = "Failed to create session: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadSession(sessionId: String) {
        currentSessionId = sessionId
        _isLoading.value = true
        
        chatRepository.getMessagesForSession(sessionId).observeForever { messages ->
            _messages.value = messages
            _isLoading.value = false
        }
    }
    
    fun sendMessage(content: String) {
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                chatRepository.sendMessage(sessionId, content)
            } catch (e: Exception) {
                _error.value = "Failed to send: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}