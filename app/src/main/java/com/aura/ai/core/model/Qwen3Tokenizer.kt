package com.aura.ai.core.model

import android.util.Log
import com.aura.ai.utils.ModelLoader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileReader

class Qwen3Tokenizer {
    private val TAG = "Qwen3Tokenizer"
    
    private val bosToken = "<|endoftext|>"
    private val eosToken = "<|endoftext|>"
    private val unkToken = "<|endoftext|>"
    
    private var vocab: Map<String, Int> = mapOf()
    private var idToToken: Map<Int, String> = mapOf()
    
    init {
        loadTokenizer()
    }
    
    private fun loadTokenizer() {
        try {
            val tokenizerFile = ModelLoader.getTokenizerFile()
            val reader = FileReader(tokenizerFile)
            val tokenizerJson = Gson().fromJson<Map<String, Any>>(
                reader,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            @Suppress("UNCHECKED_CAST")
            val model = tokenizerJson["model"] as? Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val vocabMap = model?.get("vocab") as? Map<String, Double>
            
            if (vocabMap != null) {
                vocab = vocabMap.mapValues { it.value.toInt() }
                idToToken = vocab.entries.associate { it.value to it.key }
                Log.d(TAG, "Loaded vocab with ${vocab.size} tokens")
            }
            
            reader.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load tokenizer: ${e.message}")
            createFallbackTokenizer()
        }
    }
    
    private fun createFallbackTokenizer() {
        Log.w(TAG, "Using fallback tokenizer")
        vocab = mapOf()
        idToToken = mapOf()
    }
    
    fun encode(text: String, addSpecialTokens: Boolean = true): List<Long> {
        if (vocab.isEmpty()) {
            return simpleEncode(text)
        }
        
        val tokens = mutableListOf<Long>()
        
        if (addSpecialTokens) {
            vocab[bosToken]?.let { tokens.add(it.toLong()) }
        }
        
        val words = text.split(Regex("""(?<=\w)\s+(?=\w)"""))
        
        for (word in words) {
            vocab[word.lowercase()]?.let { tokenId ->
                tokens.add(tokenId.toLong())
            } ?: run {
                vocab[unkToken]?.let { tokens.add(it.toLong()) }
            }
        }
        
        return tokens
    }
    
    private fun simpleEncode(text: String): List<Long> {
        return text.map { it.code.toLong() }
    }
    
    fun decode(tokens: List<Long>, skipSpecialTokens: Boolean = true): String {
        if (idToToken.isEmpty()) {
            return simpleDecode(tokens)
        }
        
        val text = StringBuilder()
        
        for (tokenId in tokens) {
            val token = idToToken[tokenId.toInt()]
            if (token != null) {
                if (skipSpecialTokens && token.startsWith("<|") && token.endsWith("|>")) {
                    continue
                }
                text.append(token)
                if (!token.startsWith("##") && !token.startsWith("Ġ")) {
                    text.append(" ")
                }
            }
        }
        
        return text.toString().trim()
    }
    
    private fun simpleDecode(tokens: List<Long>): String {
        return tokens.map { it.toInt().toChar() }.joinToString("")
    }
    
    fun countTokens(text: String): Int {
        return encode(text).size
    }
}