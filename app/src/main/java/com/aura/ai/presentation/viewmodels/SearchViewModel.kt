package com.aura.ai.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _searchResults = MutableLiveData<List<SearchResult>>(emptyList())
    val searchResults: LiveData<List<SearchResult>> = _searchResults
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _currentQuery = MutableLiveData("")
    val currentQuery: String get() = _currentQuery.value ?: ""
    
    private var searchJob: Job? = null
    private val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    
    fun search(query: String) {
        _currentQuery.value = query
        searchJob?.cancel()
        
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            chatRepository.searchMessages(query).observeForever { messages ->
                val results = messages.map { message ->
                    SearchResult(
                        id = message.id,
                        sessionId = message.sessionId,
                        content = message.content,
                        isFromUser = message.isFromUser,
                        timestamp = dateFormat.format(message.timestamp),
                        snippet = generateSnippet(message.content, query)
                    )
                }
                _searchResults.value = results
                _isLoading.value = false
            }
        }
    }
    
    private fun generateSnippet(content: String, query: String): String {
        val lowerContent = content.lowercase()
        val lowerQuery = query.lowercase()
        val index = lowerContent.indexOf(lowerQuery)
        return if (index >= 0) {
            val start = maxOf(0, index - 30)
            val end = minOf(content.length, index + query.length + 30)
            "..." + content.substring(start, end) + "..."
        } else {
            if (content.length > 60) content.substring(0, 57) + "..." else content
        }
    }
    
    data class SearchResult(
        val id: String,
        val sessionId: String,
        val content: String,
        val isFromUser: Boolean,
        val timestamp: String,
        val snippet: String
    )
}
