package com.aura.ai.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aura.ai.data.repository.ChatRepository
import com.aura.ai.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : ListAdapter<ChatRepository.ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ChatViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: ChatRepository.ChatMessage) {
            binding.apply {
                tvMessage.text = message.content
                tvTime.text = dateFormat.format(message.timestamp)
                
                val backgroundRes = if (message.isFromUser) {
                    R.drawable.bg_message_user
                } else {
                    R.drawable.bg_message_bot
                }
                messageContainer.setBackgroundResource(backgroundRes)
                
                val layoutParams = messageContainer.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                if (message.isFromUser) {
                    layoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                    layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                } else {
                    layoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                }
                messageContainer.layoutParams = layoutParams
                
                executePendingBindings()
            }
        }
    }
    
    class ChatDiffCallback : DiffUtil.ItemCallback<ChatRepository.ChatMessage>() {
        override fun areItemsTheSame(
            oldItem: ChatRepository.ChatMessage,
            newItem: ChatRepository.ChatMessage
        ): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(
            oldItem: ChatRepository.ChatMessage,
            newItem: ChatRepository.ChatMessage
        ): Boolean {
            return oldItem.content == newItem.content && oldItem.timestamp == newItem.timestamp
        }
    }
}