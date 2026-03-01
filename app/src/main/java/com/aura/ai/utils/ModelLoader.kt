package com.aura.ai.utils

import android.os.Environment
import android.util.Log
import java.io.File

object ModelLoader {
    private const val TAG = "ModelLoader"
    
    fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    
    fun createModelDirectory(): Boolean {
        return try {
            val dir = File(Constants.MODELS_FOLDER)
            if (!dir.exists()) {
                dir.mkdirs()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create directory: ${e.message}")
            false
        }
    }
    
    fun getModelFile(modelName: String = Constants.DEFAULT_MODEL_FILENAME): File {
        val modelFile = File(Constants.MODELS_FOLDER, modelName)
        if (!modelFile.exists()) {
            throw IllegalStateException("Model not found: ${modelFile.absolutePath}")
        }
        return modelFile
    }
    
    fun getTokenizerFile(): File {
        val tokenizerFile = File(Constants.MODELS_FOLDER, Constants.TOKENIZER_FILENAME)
        if (!tokenizerFile.exists()) {
            throw IllegalStateException("Tokenizer not found: ${tokenizerFile.absolutePath}")
        }
        return tokenizerFile
    }
    
    fun modelExists(modelName: String = Constants.DEFAULT_MODEL_FILENAME): Boolean {
        return File(Constants.MODELS_FOLDER, modelName).exists()
    }
    
    fun tokenizerExists(): Boolean {
        return File(Constants.MODELS_FOLDER, Constants.TOKENIZER_FILENAME).exists()
    }
    
    fun listAvailableModels(): List<ModelInfo> {
        val dir = File(Constants.MODELS_FOLDER)
        return if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.filter { it.isFile && it.extension == "onnx" }
                ?.map { file ->
                    ModelInfo(
                        name = file.name,
                        path = file.absolutePath,
                        sizeMB = file.length() / (1024.0 * 1024.0),
                        hasTokenizer = File(Constants.MODELS_FOLDER, 
                            file.name.replace(".onnx", ".json")).exists() || 
                            File(Constants.MODELS_FOLDER, Constants.TOKENIZER_FILENAME).exists()
                    )
                } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun getModelSizeInMB(modelName: String = Constants.DEFAULT_MODEL_FILENAME): Double {
        return try {
            val file = File(Constants.MODELS_FOLDER, modelName)
            if (file.exists()) file.length() / (1024.0 * 1024.0) else 0.0
        } catch (e: Exception) {
            0.0
        }
    }
    
    fun getSetupInstructions(): String {
        val availableModels = listAvailableModels()
        return """
            📁 MODEL SETUP GUIDE
            
            1. Create folder on your phone:
               /storage/emulated/0/AuraAI/models/
            
            2. Copy your model files there:
               • YourModel.onnx (any name)
               • tokenizer.json
            
            3. Tap "Refresh" below
            
            📊 Current Status:
            • Storage available: ${isExternalStorageAvailable()}
            • Folder exists: ${File(Constants.MODELS_FOLDER).exists()}
            • Models found: ${availableModels.size}
            
            ${if (availableModels.isNotEmpty()) "📋 Available models:\n" + availableModels.joinToString("\n") { "  • ${it.name} (${"%.2f".format(it.sizeMB)} MB)" } else ""}
        """.trimIndent()
    }
    
    data class ModelInfo(
        val name: String,
        val path: String,
        val sizeMB: Double,
        val hasTokenizer: Boolean
    )
}