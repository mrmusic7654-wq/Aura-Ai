package com.aura.ai.utils

import android.os.Environment
import java.io.File

object ModelLoader {
    
    data class ModelInfo(
        val name: String,
        val path: String,
        val sizeMB: Double,
        val hasTokenizer: Boolean
    )
    
    fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    
    fun createModelDirectory(): Boolean {
        return try {
            val dir = File(Constants.MODELS_FOLDER)
            if (!dir.exists()) dir.mkdirs() else true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getModelFile(modelName: String = Constants.DEFAULT_MODEL_FILENAME): File {
        return File(Constants.MODELS_FOLDER, modelName).also {
            if (!it.exists()) throw IllegalStateException("Model not found: ${it.absolutePath}")
        }
    }
    
    fun getTokenizerFile(): File {
        return File(Constants.MODELS_FOLDER, Constants.TOKENIZER_FILENAME).also {
            if (!it.exists()) throw IllegalStateException("Tokenizer not found: ${it.absolutePath}")
        }
    }
    
    fun modelExists(modelName: String = Constants.DEFAULT_MODEL_FILENAME): Boolean {
        return File(Constants.MODELS_FOLDER, modelName).exists()
    }
    
    fun tokenizerExists(): Boolean {
        return File(Constants.MODELS_FOLDER, Constants.TOKENIZER_FILENAME).exists()
    }
    
    fun listAvailableModels(): List<ModelInfo> {
        val dir = File(Constants.MODELS_FOLDER)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        
        return dir.listFiles()?.filter { it.isFile && it.extension == "onnx" }
            ?.map { file ->
                ModelInfo(
                    name = file.name,
                    path = file.absolutePath,
                    sizeMB = file.length() / (1024.0 * 1024.0),
                    hasTokenizer = File(Constants.MODELS_FOLDER, 
                        file.name.replace(".onnx", ".json")).exists() || tokenizerExists()
                )
            } ?: emptyList()
    }
}
