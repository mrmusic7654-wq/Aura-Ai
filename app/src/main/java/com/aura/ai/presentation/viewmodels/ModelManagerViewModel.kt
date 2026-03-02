package com.aura.ai.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.core.model.Qwen3Model
import com.aura.ai.data.repository.ChatRepository
import com.aura.ai.utils.ModelLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelManagerViewModel @Inject constructor(
    private val qwen3Model: Qwen3Model,
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _models = MutableLiveData<List<ModelItem>>(emptyList())
    val models: LiveData<List<ModelItem>> = _models
    
    private val _selectedModel = MutableLiveData<ModelItem?>()
    val selectedModel: LiveData<ModelItem?> = _selectedModel
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage
    
    private val _navigateToChat = MutableLiveData<String?>()
    val navigateToChat: LiveData<String?> = _navigateToChat
    
    fun refreshModels() {
        viewModelScope.launch {
            _isLoading.value = true
            val availableModels = ModelLoader.listAvailableModels()
            _models.value = availableModels.map { info ->
                ModelItem(
                    name = info.name,
                    path = info.path,
                    sizeMB = info.sizeMB,
                    hasTokenizer = info.hasTokenizer,
                    isLoaded = info.name == qwen3Model.getCurrentModel()
                )
            }
            _statusMessage.value = "Found ${availableModels.size} model(s)"
            _isLoading.value = false
        }
    }
    
    fun selectModel(model: ModelItem) {
        _selectedModel.value = model
        _statusMessage.value = "Selected: ${model.name}"
    }
    
    fun loadSelectedModel() {
        val model = _selectedModel.value ?: run {
            _statusMessage.value = "Please select a model first"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Loading ${model.name}..."
            
            qwen3Model.unloadModel()
            val success = qwen3Model.loadModel(model.name)
            
            if (success) {
                _statusMessage.value = "✅ ${model.name} loaded successfully!"
                val sessionId = chatRepository.createSession("New Chat with ${model.name}")
                _navigateToChat.value = sessionId
            } else {
                _statusMessage.value = "❌ Failed to load: ${qwen3Model.getModelError() ?: "Unknown error"}"
            }
            refreshModels()
            _isLoading.value = false
        }
    }
    
    data class ModelItem(
        val name: String,
        val path: String,
        val sizeMB: Double,
        val hasTokenizer: Boolean,
        val isLoaded: Boolean
    )
}
