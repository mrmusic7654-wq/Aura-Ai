package com.aura.ai.utils

import android.content.Context
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
            false
        }
    }
    
    // FIXED: Removed Context parameter - only takes modelName
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
}
