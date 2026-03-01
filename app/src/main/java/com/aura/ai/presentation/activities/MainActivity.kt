package com.aura.ai.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aura.ai.R
import com.aura.ai.core.model.Qwen3Model
import com.aura.ai.databinding.ActivityMainBinding
import com.aura.ai.utils.Extensions.hide
import com.aura.ai.utils.Extensions.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var qwen3Model: Qwen3Model
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkModelStatus()
    }
    
    private fun setupUI() {
        binding.btnStartChat.setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }
        
        binding.btnModelManager.setOnClickListener {
            startActivity(Intent(this, ModelManagerActivity::class.java))
        }
        
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun checkModelStatus() {
        lifecycleScope.launch {
            binding.progressBar.show()
            delay(500)
            
            val currentModel = qwen3Model.getCurrentModel()
            val status = if (qwen3Model.isLoaded()) {
                "✓ Model loaded: $currentModel"
            } else {
                "⚠️ No model loaded. Go to Model Manager"
            }
            
            binding.tvModelStatus.text = status
            binding.progressBar.hide()
        }
    }
}