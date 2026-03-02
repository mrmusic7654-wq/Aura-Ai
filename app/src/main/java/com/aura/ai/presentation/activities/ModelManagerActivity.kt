package com.aura.ai.presentation.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
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
import com.aura.ai.utils.StorageHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModelManagerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityModelManagerBinding
    private val viewModel: ModelManagerViewModel by viewModels()
    private lateinit var modelAdapter: ModelAdapter
    
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
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.btnLoadModel.isEnabled = false
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.btnLoadModel.isEnabled = true
            }
        })
        
        viewModel.selectedModel.observe(this, Observer { model ->
            binding.tvSelectedModel.text = model?.let {
                "Selected: ${it.name} (${"%.2f".format(it.sizeMB)} MB)"
            } ?: "No model selected"
        })
        
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
        binding.btnRefresh.setOnClickListener { refreshModels() }
        binding.btnLoadModel.setOnClickListener { viewModel.loadSelectedModel() }
        binding.btnOpenFolder.setOnClickListener { openModelsFolder() }
        binding.btnRequestPermission.setOnClickListener { StorageHelper.requestStoragePermission(this) }
    }
    
    private fun checkPermission() {
        if (StorageHelper.hasStoragePermission(this)) {
            binding.permissionLayout.visibility = View.GONE
            binding.contentLayout.visibility = View.VISIBLE
            refreshModels()
        } else {
            binding.permissionLayout.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.GONE
        }
    }
    
    private fun refreshModels() {
        if (StorageHelper.hasStoragePermission(this)) {
            viewModel.refreshModels()
        }
    }
    
    private fun openModelsFolder() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.MODELS_FOLDER)))
        } catch (e: Exception) {
            AlertDialog.Builder(this)
                .setTitle("Cannot open folder")
                .setMessage("Please manually navigate to:\n${Constants.MODELS_FOLDER}")
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_STORAGE_PERMISSION) {
            checkPermission()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
