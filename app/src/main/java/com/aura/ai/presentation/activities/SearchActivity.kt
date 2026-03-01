package com.aura.ai.presentation.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.aura.ai.R
import com.aura.ai.databinding.ActivitySearchBinding
import com.aura.ai.presentation.adapters.SearchResultsAdapter
import com.aura.ai.presentation.viewmodels.SearchViewModel
import com.aura.ai.utils.Extensions.hide
import com.aura.ai.utils.Extensions.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAdapter: SearchResultsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupSearchListener()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search Conversations"
    }
    
    private fun setupRecyclerView() {
        searchAdapter = SearchResultsAdapter { message ->
            startActivity(ChatActivity.newIntent(this, message.sessionId))
            finish()
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.searchResults.observe(this, Observer { results ->
            searchAdapter.submitList(results)
            
            if (results.isEmpty()) {
                if (viewModel.currentQuery.isNotEmpty()) {
                    binding.tvEmpty.text = "No messages found for \"${viewModel.currentQuery}\""
                } else {
                    binding.tvEmpty.text = "Type to search your conversations"
                }
                binding.tvEmpty.show()
                binding.recyclerView.hide()
            } else {
                binding.tvEmpty.hide()
                binding.recyclerView.show()
            }
        })
        
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        })
    }
    
    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.btnClear.setOnClickListener {
            binding.etSearch.text.clear()
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
