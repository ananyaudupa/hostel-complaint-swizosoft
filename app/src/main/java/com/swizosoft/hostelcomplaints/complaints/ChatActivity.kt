package com.swizosoft.hostelcomplaints.complaints

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.swizosoft.hostelcomplaints.ai.GeminiService
import com.swizosoft.hostelcomplaints.databinding.ActivityChatBinding
import com.swizosoft.hostelcomplaints.models.ChatMessage
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var geminiService: GeminiService
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geminiService = GeminiService(this)
        setupRecyclerView()

        binding.sendBtn.setOnClickListener {
            sendMessage()
        }

        // Initial AI greeting
        addAiMessage("Hello! I'm your Hostel Assistant. Describe your problem, and I'll help you write a clear complaint and suggest the right category.")
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.chatRecyclerView.adapter = adapter
    }

    private var lastAnalysis: com.swizosoft.hostelcomplaints.ai.ComplaintAnalysis? = null
    private var lastDescription: String = ""

    private fun sendMessage() {
        val text = binding.chatInput.text.toString().trim()
        if (text.isEmpty()) return

        addUserMessage(text)
        binding.chatInput.setText("")
        
        // Check for confirmation (e.g., "Yes", "Ok", etc.)
        val affirmativeWords = listOf("yes", "yeah", "ok", "sure", "yep", "y", "yess", "yesls")
        if (affirmativeWords.contains(text.lowercase()) && lastAnalysis != null) {
            val intent = android.content.Intent().apply {
                putExtra("description", lastAnalysis?.enhancedDescription ?: lastDescription)
                putExtra("category", lastAnalysis?.category)
                putExtra("urgency", lastAnalysis?.urgency)
                putExtra("sentiment", lastAnalysis?.sentiment)
                putExtra("temporarySolution", lastAnalysis?.temporarySolution)
            }
            setResult(RESULT_OK, intent)
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Analyze the user's input to suggest category and urgency
                val response = geminiService.analyzeComplaint(text)
                lastAnalysis = response
                lastDescription = text
                
                val reply = "I understand. Based on your description, here is what I analyzed:\n\n" +
                        "• Suggested Category: **${response.category}**\n" +
                        "• Urgency: **${response.urgency}**\n\n" +
                        "**Enhanced Description for Maintenance:**\n" +
                        "\"${response.enhancedDescription}\"\n\n" +
                        "**Temporary Solution for You:**\n" +
                        "${response.temporarySolution}\n\n" +
                        "Would you like to use this for your complaint?"
                addAiMessage(reply)
            } catch (e: Exception) {
                addAiMessage("Sorry, I'm having trouble connecting. Please try again.")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
    }

    private fun addAiMessage(text: String) {
        messages.add(ChatMessage(text, false))
        adapter.notifyItemInserted(messages.size - 1)
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
    }
}
