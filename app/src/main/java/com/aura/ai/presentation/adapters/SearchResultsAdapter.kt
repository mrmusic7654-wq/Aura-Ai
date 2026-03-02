package com.aura.ai.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aura.ai.R
import com.aura.ai.databinding.ItemSearchResultBinding
import com.aura.ai.presentation.viewmodels.SearchViewModel

class SearchResultsAdapter(
    private val onResultClick: (SearchViewModel.SearchResult) -> Unit
) : ListAdapter<SearchViewModel.SearchResult, SearchResultsAdapter.SearchViewHolder>(SearchDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    
    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SearchViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onResultClick(getItem(adapterPosition)) }
        }
        
        fun bind(result: SearchViewModel.SearchResult) {
            binding.tvSender.text = if (result.isFromUser) "You" else "Aura"
            binding.tvSender.setTextColor(if (result.isFromUser) 
                android.graphics.Color.parseColor("#6C63FF") else android.graphics.Color.parseColor("#FF6584"))
            binding.tvContent.text = result.snippet
            binding.tvTimestamp.text = result.timestamp
        }
    }
    
    class SearchDiffCallback : DiffUtil.ItemCallback<SearchViewModel.SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchViewModel.SearchResult, newItem: SearchViewModel.SearchResult) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SearchViewModel.SearchResult, newItem: SearchViewModel.SearchResult) = oldItem == newItem
    }
}
