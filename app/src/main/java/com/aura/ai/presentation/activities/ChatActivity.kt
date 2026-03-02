package com.aura.ai.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.aura.ai.R
import com.aura.ai.databinding.ActivityChatBinding
import com.aura.ai.presentation.adapters.ChatAdapter
import com.aura.ai.presentation.viewmodels.ChatViewModel
import com.aura.ai.utils.Extensions.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private var sessionId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionId = intent.getStringExtra("session_id")
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        sessionId?.let {
            viewModel.loadSession(it)
        } ?: run {
            viewModel.createNewSession()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.messages.observe(this, Observer { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerView.smoothScrollToPosition(messages.size - 1)
            }
        })
        
        viewModel.sessionTitle.observe(this, Observer { title ->
            binding.tvTitle.text = title
        })
        
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !isLoading
        })
        
        viewModel.error.observe(this, Observer { error ->
            error?.let {
                binding.root.showSnackbar(it)
                viewModel.clearError()
            }
        })
    }
    
    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.etMessage.text.clear()
            }
        }
        
        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_new_chat -> {
                viewModel.createNewSession()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    companion object {
        fun newIntent(context: Context, sessionId: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                putExtra("session_id", sessionId)
            }
        }
    }
}
