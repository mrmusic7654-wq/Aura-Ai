package com.aura.ai.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aura.ai.databinding.ItemSessionBinding
import com.aura.ai.presentation.viewmodels.SessionsViewModel

class SessionsAdapter(
    private val onSessionClick: (String) -> Unit,
    private val onSessionLongClick: (SessionsViewModel.SessionItem) -> Unit,
    private val onPinClick: (String, Boolean) -> Unit
) : ListAdapter<SessionsViewModel.SessionItem, SessionsAdapter.SessionViewHolder>(SessionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SessionViewHolder(
        private val binding: ItemSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSessionClick(getItem(position).id)
                }
            }
            
            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSessionLongClick(getItem(position))
                }
                true
            }
            
            binding.btnPin.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val session = getItem(position)
                    onPinClick(session.id, !session.isPinned)
                }
            }
        }
        
        fun bind(session: SessionsViewModel.SessionItem) {
            binding.apply {
                tvTitle.text = session.title
                tvTime.text = session.timeAgo
                tvMessageCount.text = "${session.messageCount} messages"
                
                btnPin.setImageResource(
                    if (session.isPinned) 
                        android.R.drawable.star_on 
                    else 
                        android.R.drawable.star_off
                )
                
                executePendingBindings()
            }
        }
    }
    
    class SessionDiffCallback : DiffUtil.ItemCallback<SessionsViewModel.SessionItem>() {
        override fun areItemsTheSame(
            oldItem: SessionsViewModel.SessionItem,
            newItem: SessionsViewModel.SessionItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(
            oldItem: SessionsViewModel.SessionItem,
            newItem: SessionsViewModel.SessionItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}