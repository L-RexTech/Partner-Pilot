package com.example.gemniapi

import androidx.recyclerview.widget.RecyclerView
import com.example.gemniapi.database.ChatSession
import com.example.gemniapi.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionViewHolder(
    private val binding: ItemSessionBinding,
    private val onClick: (ChatSession) -> Unit,
    private val onExport: (ChatSession) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(session: ChatSession) {
        binding.apply {
            tvTitle.text = session.title
            tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(session.createdAt))

            root.setOnClickListener { onClick(session) }
            btnExport.setOnClickListener { onExport(session) }
        }
    }
}