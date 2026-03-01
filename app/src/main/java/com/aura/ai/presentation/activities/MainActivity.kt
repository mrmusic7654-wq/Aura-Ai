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
import com.aura.ai.utils.ModelLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        checkModel()
    }
    
    private fun setupUI() {
        binding.btnStartChat.setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }
        
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        binding.btnCheckModel.setOnClickListener {
            checkModel()
        }
    }
    
    private fun checkModel() {
        lifecycleScope.launch {
            binding.progressBar.show()
            
            val result = withContext(Dispatchers.IO) {
                try {
                    ModelLoader.getModelFile(this@MainActivity, "qwen3-0.6b-int8.onnx")
                    val loaded = qwen3Model.loadModel()
                    qwen3Model.unloadModel()
                    
                    if (loaded) "✓ Qwen3 model ready (32K context!)" else "✗ Model failed to load"
                } catch (e: Exception) {
                    "✗ Error: ${e.message}"
                }
            }
            
            binding.progressBar.hide()
            Toast.makeText(this@MainActivity, result, Toast.LENGTH_LONG).show()
            binding.tvModelStatus.text = result
        }
    }
}
