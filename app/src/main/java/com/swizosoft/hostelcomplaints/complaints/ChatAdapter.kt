package com.swizosoft.hostelcomplaints.complaints

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swizosoft.hostelcomplaints.databinding.ItemChatAiBinding
import com.swizosoft.hostelcomplaints.databinding.ItemChatUserBinding
import com.swizosoft.hostelcomplaints.models.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_AI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_USER) {
            val binding = ItemChatUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            UserViewHolder(binding)
        } else {
            val binding = ItemChatAiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AiViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.binding.messageText.text = message.text
        } else if (holder is AiViewHolder) {
            holder.binding.messageText.text = message.text
        }
    }

    override fun getItemCount() = messages.size

    class UserViewHolder(val binding: ItemChatUserBinding) : RecyclerView.ViewHolder(binding.root)
    class AiViewHolder(val binding: ItemChatAiBinding) : RecyclerView.ViewHolder(binding.root)
}
