package com.aura.ai.utils

object Constants {
    // Model storage on device - USER ACCESSIBLE LOCATION
    const val MODELS_FOLDER = "/storage/emulated/0/AuraAI/models/"
    const val DEFAULT_MODEL_FILENAME = "qwen3-0.6b-int8.onnx"
    const val TOKENIZER_FILENAME = "tokenizer.json"
    
    // Model settings
    const val QWEN3_MAX_TOKENS = 32768
    const val QWEN3_VOCAB_SIZE = 151936
    const val QWEN3_EOS_TOKEN_ID = 151643
    
    // Generation
    const val DEFAULT_MAX_NEW_TOKENS = 512
    const val DEFAULT_TEMPERATURE = 0.7f
    const val DEFAULT_TOP_P = 0.9f
    
    // Database
    const val DATABASE_NAME = "aura_qwen3.db"
    
    // Permissions
    const val REQUEST_CODE_STORAGE_PERMISSION = 1001
}