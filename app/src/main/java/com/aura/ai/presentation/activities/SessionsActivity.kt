package com.aura.ai.presentation.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.aura.ai.R
import com.aura.ai.databinding.ActivitySessionsBinding
import com.aura.ai.presentation.adapters.SessionsAdapter
import com.aura.ai.presentation.viewmodels.SessionsViewModel
import com.aura.ai.utils.Extensions.hide
import com.aura.ai.utils.Extensions.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SessionsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySessionsBinding
    private val viewModel: SessionsViewModel by viewModels()
    private lateinit var sessionsAdapter: SessionsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Conversations"
    }
    
    private fun setupRecyclerView() {
        sessionsAdapter = SessionsAdapter(
            onSessionClick = { sessionId ->
                startActivity(ChatActivity.newIntent(this, sessionId))
                finish()
            },
            onSessionLongClick = { session ->
                showSessionOptionsDialog(session)
            },
            onPinClick = { sessionId, isPinned ->
                viewModel.togglePin(sessionId, isPinned)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SessionsActivity)
            adapter = sessionsAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.sessions.observe(this, Observer { sessions ->
            sessionsAdapter.submitList(sessions)
            
            if (sessions.isEmpty()) {
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
        
        viewModel.navigateToChat.observe(this, Observer { sessionId ->
            sessionId?.let {
                startActivity(ChatActivity.newIntent(this, it))
                finish()
            }
        })
    }
    
    private fun setupListeners() {
        binding.fabNewChat.setOnClickListener {
            viewModel.createNewSession()
        }
    }
    
    private fun showSessionOptionsDialog(session: SessionsViewModel.SessionItem) {
        val options = arrayOf(
            if (session.isPinned) "Unpin" else "Pin",
            "Rename",
            "Delete"
        )
        
        AlertDialog.Builder(this)
            .setTitle(session.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.togglePin(session.id, !session.isPinned)
                    1 -> showRenameDialog(session)
                    2 -> showDeleteConfirmation(session)
                }
            }
            .show()
    }
    
    private fun showRenameDialog(session: SessionsViewModel.SessionItem) {
        val input = android.widget.EditText(this).apply {
            setText(session.title)
            selectAll()
        }
        
        AlertDialog.Builder(this)
            .setTitle("Rename Conversation")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    viewModel.renameSession(session.id, newTitle)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteConfirmation(session: SessionsViewModel.SessionItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Conversation")
            .setMessage("Are you sure you want to delete '${session.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSession(session.id)
            }
            .setNegativeButton("Cancel", null)
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