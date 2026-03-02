package com.aura.ai.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aura.ai.R
import com.aura.ai.databinding.ItemSessionBinding
import com.aura.ai.presentation.viewmodels.SessionsViewModel

class SessionsAdapter(
    private val onSessionClick: (String) -> Unit,
    private val onSessionLongClick: (SessionsViewModel.SessionItem) -> Unit,
    private val onPinClick: (String, Boolean) -> Unit
) : ListAdapter<SessionsViewModel.SessionItem, SessionsAdapter.SessionViewHolder>(SessionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        return SessionViewHolder(ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    
    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SessionViewHolder(private val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onSessionClick(getItem(adapterPosition).id) }
            binding.root.setOnLongClickListener { onSessionLongClick(getItem(adapterPosition)); true }
            binding.btnPin.setOnClickListener { onPinClick(getItem(adapterPosition).id, !getItem(adapterPosition).isPinned) }
        }
        
        fun bind(session: SessionsViewModel.SessionItem) {
            binding.tvTitle.text = session.title
            binding.tvTime.text = session.timeAgo
            binding.tvMessageCount.text = "${session.messageCount} messages"
            binding.btnPin.setImageResource(if (session.isPinned) android.R.drawable.star_on else android.R.drawable.star_off)
        }
    }
    
    class SessionDiffCallback : DiffUtil.ItemCallback<SessionsViewModel.SessionItem>() {
        override fun areItemsTheSame(oldItem: SessionsViewModel.SessionItem, newItem: SessionsViewModel.SessionItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SessionsViewModel.SessionItem, newItem: SessionsViewModel.SessionItem) = oldItem == newItem
    }
}
