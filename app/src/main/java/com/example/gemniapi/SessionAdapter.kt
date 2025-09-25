package com.example.gemniapi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gemniapi.database.ChatSession
import com.example.gemniapi.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class SessionAdapter(
    private val onClick: (ChatSession) -> Unit,
    private val onExport: (ChatSession) -> Unit
) : ListAdapter<ChatSession, SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        return SessionViewHolder(
            ItemSessionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClick,
            onExport
        )
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class SessionDiffCallback : DiffUtil.ItemCallback<ChatSession>() {
    override fun areItemsTheSame(oldItem: ChatSession, newItem: ChatSession) =
        oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatSession, newItem: ChatSession) =
        oldItem == newItem
}