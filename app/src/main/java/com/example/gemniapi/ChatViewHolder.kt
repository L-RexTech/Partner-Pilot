package com.example.gemniapi

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gemniapi.database.ChatMessage
import com.example.gemniapi.databinding.ItemChatBinding
class ChatViewHolder(
    private val binding: ItemChatBinding,
    private val onLongClick: (ChatMessage) -> Boolean
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(chat: ChatMessage) {
        binding.apply {
            val layoutParams = clChat.layoutParams as ViewGroup.MarginLayoutParams
            val set = ConstraintSet()
            set.clone(clChat)

            if (chat.sender == "Me") {
                layoutParams.setMargins(250, 25, 50, 25)
                tvChat.setBackgroundResource(R.drawable.rounded_me_background)
                set.clear(tvChat.id, ConstraintSet.START)
            } else {
                layoutParams.setMargins(50, 25, 250, 25)
                tvChat.setBackgroundResource(R.drawable.rounded_gemini_background)
                set.clear(tvChat.id, ConstraintSet.END)
            }
            set.applyTo(clChat)

            tvChat.text = chat.text
            root.setOnLongClickListener { onLongClick(chat) }
        }
    }
}