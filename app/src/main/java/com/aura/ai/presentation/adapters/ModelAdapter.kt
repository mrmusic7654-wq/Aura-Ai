package com.aura.ai.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aura.ai.R
import com.aura.ai.databinding.ItemModelBinding
import com.aura.ai.presentation.viewmodels.ModelManagerViewModel

class ModelAdapter(
    private val onModelClick: (ModelManagerViewModel.ModelItem) -> Unit
) : ListAdapter<ModelManagerViewModel.ModelItem, ModelAdapter.ModelViewHolder>(ModelDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        return ModelViewHolder(ItemModelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    
    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ModelViewHolder(private val binding: ItemModelBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onModelClick(getItem(adapterPosition)) }
        }
        
        fun bind(model: ModelManagerViewModel.ModelItem) {
            binding.tvModelName.text = model.name
            binding.tvModelSize.text = String.format("%.2f MB", model.sizeMB)
            binding.tvStatus.text = when {
                model.isLoaded -> "✓ Loaded"
                model.hasTokenizer -> "✓ Ready"
                else -> "✗ Missing tokenizer"
            }
            binding.tvStatus.setTextColor(when {
                model.isLoaded -> android.graphics.Color.parseColor("#00D9C0")
                model.hasTokenizer -> android.graphics.Color.parseColor("#6C63FF")
                else -> android.graphics.Color.parseColor("#FF6584")
            })
        }
    }
    
    class ModelDiffCallback : DiffUtil.ItemCallback<ModelManagerViewModel.ModelItem>() {
        override fun areItemsTheSame(oldItem: ModelManagerViewModel.ModelItem, newItem: ModelManagerViewModel.ModelItem) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: ModelManagerViewModel.ModelItem, newItem: ModelManagerViewModel.ModelItem) = oldItem == newItem
    }
}
