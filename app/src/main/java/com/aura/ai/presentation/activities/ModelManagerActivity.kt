package com.aura.ai.presentation.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.aura.ai.R
import com.aura.ai.databinding.ActivityModelManagerBinding
import com.aura.ai.presentation.adapters.ModelAdapter
import com.aura.ai.presentation.viewmodels.ModelManagerViewModel
import com.aura.ai.utils.Constants
import com.aura.ai.utils.Extensions.hide
import com.aura.ai.utils.Extensions.show
import com.aura.ai.utils.StorageHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModelManagerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityModelManagerBinding
    private val viewModel: ModelManagerViewModel by viewModels()
    private lateinit var modelAdapter: ModelAdapter
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkPermission()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModelManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        checkPermission()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Model Manager"
    }
    
    private fun setupRecyclerView() {
        modelAdapter = ModelAdapter { model ->
            viewModel.selectModel(model)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ModelManagerActivity)
            adapter = modelAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.models.observe(this, Observer { models ->
            modelAdapter.submitList(models)
            
            if (models.isEmpty()) {
                binding.tvEmpty.show()
                binding.recyclerView.hide()
                binding.btnLoadModel.isEnabled = false
            } else {
                binding.tvEmpty.hide()
                binding.recyclerView.show()
                binding.btnLoadModel.isEnabled = true
            }
        })
        
        viewModel.selectedModel.observe(this, Observer { model ->
            binding.tvSelectedModel.text = model?.let {
                "Selected: ${it.name} (${"%.2f".format(it.sizeMB)} MB)"
            } ?: "No model selected"
        })
        
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        })
        
        viewModel.statusMessage.observe(this, Observer { message ->
            binding.tvStatus.text = message
        })
        
        viewModel.navigateToChat.observe(this, Observer { sessionId ->
            sessionId?.let {
                startActivity(ChatActivity.newIntent(this, it))
                finish()
            }
        })
    }
    
    private fun setupListeners() {
        binding.btnRefresh.setOnClickListener {
            refreshModels()
        }
        
        binding.btnLoadModel.setOnClickListener {
            viewModel.loadSelectedModel()
        }
        
        binding.btnOpenFolder.setOnClickListener {
            openModelsFolder()
        }
        
        binding.btnRequestPermission.setOnClickListener {
            StorageHelper.requestStoragePermission(this)
        }
    }
    
    private fun checkPermission() {
        if (StorageHelper.hasStoragePermission(this)) {
            binding.permissionLayout.hide()
            binding.contentLayout.show()
            refreshModels()
        } else {
            binding.permissionLayout.show()
            binding.contentLayout.hide()
        }
    }
    
    private fun refreshModels() {
        if (StorageHelper.hasStoragePermission(this)) {
            viewModel.refreshModels()
        }
    }
    
    private fun openModelsFolder() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(Constants.MODELS_FOLDER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            AlertDialog.Builder(this)
                .setTitle("Cannot open folder")
                .setMessage("Please manually navigate to:\n${Constants.MODELS_FOLDER}")
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_STORAGE_PERMISSION) {
            checkPermission()
        }
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