package com.aura.ai.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _sessions = MutableLiveData<List<SessionItem>>(emptyList())
    val sessions: LiveData<List<SessionItem>> = _sessions
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _navigateToChat = MutableLiveData<String?>()
    val navigateToChat: LiveData<String?> = _navigateToChat
    
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    init {
        loadSessions()
    }
    
    fun loadSessions() {
        _isLoading.value = true
        chatRepository.getAllSessions().observeForever { sessions ->
            val items = sessions.map { session ->
                SessionItem(
                    id = session.id,
                    title = session.title,
                    lastActive = formatDate(session.updatedAt),
                    messageCount = session.messageCount,
                    isPinned = session.isPinned,
                    timeAgo = getTimeAgo(session.updatedAt)
                )
            }
            _sessions.postValue(items)
            _isLoading.postValue(false)
        }
    }
    
    private fun formatDate(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        val days = diff / (1000 * 60 * 60 * 24)
        return when {
            days < 1 -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "${days} days ago"
            else -> dateFormat.format(date)
        }
    }
    
    private fun getTimeAgo(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> dateFormat.format(date)
        }
    }
    
    fun createNewSession() {
        viewModelScope.launch {
            _isLoading.value = true
            val sessionId = chatRepository.createSession()
            _navigateToChat.value = sessionId
            _isLoading.value = false
        }
    }
    
    fun togglePin(sessionId: String, isPinned: Boolean) {
        viewModelScope.launch {
            chatRepository.togglePin(sessionId, isPinned)
            loadSessions()
        }
    }
    
    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.renameSession(sessionId, newTitle)
            loadSessions()
        }
    }
    
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            loadSessions()
        }
    }
    
    data class SessionItem(
        val id: String,
        val title: String,
        val lastActive: String,
        val messageCount: Int,
        val isPinned: Boolean,
        val timeAgo: String
    )
}
