package com.example.quickqrabc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quickqrabc.R
import com.example.quickqrabc.data.QRHistoryEntity
import com.example.quickqrabc.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onItemClick: (QRHistoryEntity, View) -> Unit
) : ListAdapter<QRHistoryEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: QRHistoryEntity) {
            binding.textContent.text = item.content
            binding.textTimestamp.text = formatTimestamp(item.createdAt)
            
            // Set icon based on type
            val iconRes = if (item.type == "scan") {
                R.drawable.ic_qr_scanner
            } else {
                R.drawable.ic_qr_generate
            }
            binding.iconType.setImageResource(iconRes)
            
            // Set click listeners
            binding.root.setOnClickListener {
                // Handle item click if needed
            }
            
            binding.btnMore.setOnClickListener {
                onItemClick(item, it)
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            return format.format(date)
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<QRHistoryEntity>() {
        override fun areItemsTheSame(oldItem: QRHistoryEntity, newItem: QRHistoryEntity): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: QRHistoryEntity, newItem: QRHistoryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
