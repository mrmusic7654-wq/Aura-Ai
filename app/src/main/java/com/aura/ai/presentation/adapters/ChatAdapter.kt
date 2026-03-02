package com.aura.ai.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aura.ai.R
import com.aura.ai.data.repository.ChatRepository
import com.aura.ai.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : ListAdapter<ChatRepository.ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatRepository.ChatMessage) {
            binding.tvMessage.text = message.content
            binding.tvTime.text = dateFormat.format(message.timestamp)
            binding.messageContainer.setBackgroundResource(
                if (message.isFromUser) R.drawable.bg_message_user else R.drawable.bg_message_bot
            )
        }
    }
    
    class ChatDiffCallback : DiffUtil.ItemCallback<ChatRepository.ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatRepository.ChatMessage, newItem: ChatRepository.ChatMessage) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatRepository.ChatMessage, newItem: ChatRepository.ChatMessage) = oldItem == newItem
    }
}
