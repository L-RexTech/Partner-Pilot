package com.example.gemniapi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.example.gemniapi.database.ChatMessage
import com.example.gemniapi.databinding.ActivityMainChatsBinding


class ChatsViewHolder (
    private val binding: ActivityMainChatsBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(chat: ChatMessage) {
        val layoutParams = binding.clChat.layoutParams as ViewGroup.MarginLayoutParams
        val set = ConstraintSet()
        val layout: ConstraintLayout = binding.clChat as ConstraintLayout
        set.clone(layout)
        if (chat.sender == "Me") {
            layoutParams.setMargins(250, 25, 50, 25)
            binding.tvChat.setBackgroundResource(R.drawable.rounded_me_background)
            set.clear(binding.tvChat.id, ConstraintSet.START)
            set.applyTo(layout)
        } else {
            layoutParams.setMargins(50, 25, 250, 25)
            binding.clChat.setBackgroundResource(R.drawable.rounded_gemini_background)
            set.clear(binding.tvChat.id, ConstraintSet.END)
            set.applyTo(layout)
        }

        binding.tvChat.text = chat.text
    }
}

class ChatAdapter (
    private var recyclerView: RecyclerView,
    private var listOfChats: MutableList<ChatMessage>
) : RecyclerView.Adapter<ChatsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(
            ActivityMainChatsBinding
                .inflate(
                    LayoutInflater
                        .from(parent.context),
                    parent,
                    false
                )
        )
    }

    override fun getItemCount() = listOfChats.size

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.bind(listOfChats[position])
    }

    fun updateData(newListOfChats: MutableList<ChatMessage>) {
        listOfChats = newListOfChats
        notifyDataSetChanged()
    }

    fun addData (chat: ChatMessage) {
        listOfChats.add(0, chat)
        notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
    }
}