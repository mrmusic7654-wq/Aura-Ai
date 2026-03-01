package com.aura.ai.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aura.ai.databinding.ItemModelBinding
import com.aura.ai.presentation.viewmodels.ModelManagerViewModel

class ModelAdapter(
    private val onModelClick: (ModelManagerViewModel.ModelItem) -> Unit
) : ListAdapter<ModelManagerViewModel.ModelItem, ModelAdapter.ModelViewHolder>(ModelDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val binding = ItemModelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModelViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ModelViewHolder(
        private val binding: ItemModelBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onModelClick(getItem(position))
                }
            }
        }
        
        fun bind(model: ModelManagerViewModel.ModelItem) {
            binding.apply {
                tvModelName.text = model.name
                tvModelSize.text = String.format("%.2f MB", model.sizeMB)
                
                tvStatus.text = when {
                    model.isLoaded -> "✓ Loaded"
                    model.hasTokenizer -> "✓ Ready"
                    else -> "✗ Missing tokenizer"
                }
                
                tvStatus.setTextColor(
                    if (model.isLoaded) android.graphics.Color.parseColor("#00D9C0")
                    else if (model.hasTokenizer) android.graphics.Color.parseColor("#6C63FF")
                    else android.graphics.Color.parseColor("#FF6584")
                )
                
                executePendingBindings()
            }
        }
    }
    
    class ModelDiffCallback : DiffUtil.ItemCallback<ModelManagerViewModel.ModelItem>() {
        override fun areItemsTheSame(
            oldItem: ModelManagerViewModel.ModelItem,
            newItem: ModelManagerViewModel.ModelItem
        ): Boolean {
            return oldItem.name == newItem.name
        }
        
        override fun areContentsTheSame(
            oldItem: ModelManagerViewModel.ModelItem,
            newItem: ModelManagerViewModel.ModelItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}