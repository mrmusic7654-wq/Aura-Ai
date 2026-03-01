package com.aura.ai.core.model

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.aura.ai.utils.Constants
import com.aura.ai.utils.ModelLoader
import java.nio.LongBuffer
import java.util.*

class Qwen3Model(private val context: Context) {
    private val TAG = "Qwen3Model"
    
    private val maxLength = Constants.QWEN3_MAX_TOKENS
    private val vocabSize = Constants.QWEN3_VOCAB_SIZE
    private val eosTokenId = Constants.QWEN3_EOS_TOKEN_ID
    
    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var tokenizer: Qwen3Tokenizer? = null
    private var isModelLoaded = false
    private var modelLoadError: String? = null
    private var currentModelName: String? = null
    
    init {
        ortEnvironment = OrtEnvironment.getEnvironment()
    }
    
    fun loadModel(modelName: String = Constants.DEFAULT_MODEL_FILENAME): Boolean {
        return try {
            if (!ModelLoader.isExternalStorageAvailable()) {
                modelLoadError = "External storage not available"
                return false
            }
            
            ModelLoader.createModelDirectory()
            
            if (!ModelLoader.modelExists(modelName)) {
                modelLoadError = "Model not found: $modelName"
                return false
            }
            
            if (!ModelLoader.tokenizerExists()) {
                modelLoadError = "Tokenizer not found"
                return false
            }
            
            tokenizer = Qwen3Tokenizer()
            
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            sessionOptions.setIntraOpNumThreads(4)
            
            val modelFile = ModelLoader.getModelFile(modelName)
            ortSession = ortEnvironment?.createSession(modelFile.absolutePath, sessionOptions)
            
            currentModelName = modelName
            isModelLoaded = true
            modelLoadError = null
            
            Log.d(TAG, "Model loaded: ${modelFile.absolutePath}")
            true
            
        } catch (e: Exception) {
            modelLoadError = e.message
            Log.e(TAG, "Failed to load model: ${e.message}")
            false
        }
    }
    
    fun generateResponse(
        prompt: String,
        maxNewTokens: Int = Constants.DEFAULT_MAX_NEW_TOKENS,
        temperature: Float = Constants.DEFAULT_TEMPERATURE,
        topP: Float = Constants.DEFAULT_TOP_P
    ): String {
        if (!isModelLoaded || ortSession == null) {
            return "⚠️ Model not ready\n\n${modelLoadError ?: "Please select a model in Settings → Model Manager"}"
        }
        
        return try {
            val inputIds = tokenizer?.encode(prompt) ?: return "Tokenizer failed"
            
            val maxInputTokens = maxLength - maxNewTokens
            val trimmedInput = if (inputIds.size > maxInputTokens) {
                inputIds.takeLast(maxInputTokens)
            } else {
                inputIds
            }
            
            val generatedTokens = generateTokens(trimmedInput, maxNewTokens, temperature, topP)
            tokenizer?.decode(generatedTokens) ?: "Failed to decode"
            
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed: ${e.message}")
            "Error: ${e.message}"
        }
    }
    
    private fun generateTokens(
        inputIds: List<Long>,
        maxNewTokens: Int,
        temperature: Float,
        topP: Float
    ): List<Long> {
        val generated = mutableListOf<Long>()
        generated.addAll(inputIds)
        
        try {
            val session = ortSession ?: return generated
            
            for (i in 0 until maxNewTokens) {
                val inputArray = generated.toLongArray()
                val inputTensor = OnnxTensor.createTensor(
                    ortEnvironment,
                    LongBuffer.wrap(inputArray),
                    longArrayOf(1, inputArray.size.toLong())
                )
                
                val inputs = mapOf("input_ids" to inputTensor)
                val results = session.run(inputs)
                
                val logitsTensor = results[0] as OnnxTensor
                val logitsBuffer = logitsTensor.floatBuffer
                
                val lastTokenLogits = FloatArray(vocabSize)
                val offset = (inputArray.size - 1) * vocabSize
                for (j in 0 until vocabSize) {
                    lastTokenLogits[j] = logitsBuffer.get(offset + j)
                }
                
                val nextTokenId = sampleTopP(lastTokenLogits, temperature, topP)
                generated.add(nextTokenId.toLong())
                
                inputTensor.close()
                results.close()
                
                if (nextTokenId == eosTokenId) {
                    break
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during generation: ${e.message}")
        }
        
        return generated
    }
    
    private fun sampleTopP(logits: FloatArray, temperature: Float, topP: Float): Int {
        val scaledLogits = if (temperature > 0) {
            logits.map { it / temperature }
        } else {
            logits.toList()
        }
        
        val maxLogit = scaledLogits.maxOrNull() ?: 0f
        val expLogits = scaledLogits.map { Math.exp((it - maxLogit).toDouble()).toFloat() }
        val sumExp = expLogits.sum()
        val probabilities = expLogits.map { it / sumExp }
        
        val indexedProbs = probabilities.withIndex()
            .sortedByDescending { it.value }
        
        var cumulativeProb = 0.0f
        val candidates = mutableListOf<IndexedValue<Float>>()
        
        for (item in indexedProbs) {
            cumulativeProb += item.value
            candidates.add(item)
            if (cumulativeProb >= topP) break
        }
        
        val candidateSum = candidates.sumOf { it.value.toDouble() }.toFloat()
        val normalizedProbs = candidates.map { it.index to (it.value / candidateSum) }
        
        val random = Random()
        val randomValue = random.nextFloat()
        var accum = 0.0f
        
        for ((index, prob) in normalizedProbs) {
            accum += prob
            if (randomValue < accum) {
                return index
            }
        }
        
        return indexedProbs.first().index
    }
    
    fun unloadModel() {
        try {
            ortSession?.close()
            ortSession = null
            isModelLoaded = false
            currentModelName = null
            Log.d(TAG, "Model unloaded")
        } catch (e: Exception) {
            Log.e(TAG, "Error unloading model: ${e.message}")
        }
    }
    
    fun isLoaded(): Boolean = isModelLoaded && ortSession != null
    fun getCurrentModel(): String? = currentModelName
    fun getModelError(): String? = modelLoadError
}