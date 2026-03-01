package com.aura.ai.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aura.ai.R
import com.aura.ai.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupUI()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }
    
    private fun setupUI() {
        binding.btnModelManager.setOnClickListener {
            startActivity(Intent(this, ModelManagerActivity::class.java))
        }
        
        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Aura AI")
            .setMessage("""
                Aura AI - Your Personal Assistant
                
                Version 1.0
                
                This app loads AI models from:
                /storage/emulated/0/AuraAI/models/
                
                Place your .onnx models and tokenizer.json in that folder to get started.
                
                Features:
                • 32K token context
                • Multiple conversations
                • Search all chats
                • Dark theme
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}