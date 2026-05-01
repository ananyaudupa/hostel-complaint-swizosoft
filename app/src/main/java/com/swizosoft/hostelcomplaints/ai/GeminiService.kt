package com.swizosoft.hostelcomplaints.ai

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.swizosoft.hostelcomplaints.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(context: Context) {
    private val apiKey = context.getString(R.string.gemini_api_key)
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey
    )

    suspend fun analyzeComplaint(description: String): ComplaintAnalysis = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze the following hostel maintenance complaint description and provide:
            1. Correct Category (choose one: Electricity, Water, WiFi, Plumbing, Furniture, Cleanliness, Security, Other)
            2. Urgency Level (choose one: Low, Medium, High, Critical)
            3. Sentiment (choose one: Frustrated, Neutral, Calm)

            Complaint Description: "$description"

            Return the result in JSON format:
            {
              "category": "...",
              "urgency": "...",
              "sentiment": "..."
            }
        """.trimIndent()

        return@withContext try {
            val response = model.generateContent(prompt)
            val text = response.text ?: ""
            // Simple parsing (could use Gson for more robustness)
            parseAnalysis(text)
        } catch (e: Exception) {
            ComplaintAnalysis("Other", "Medium", "Neutral")
        }
    }

    private fun parseAnalysis(text: String): ComplaintAnalysis {
        // Very basic manual parsing for demonstration. In production, use a JSON library.
        val category = Regex("\"category\": \"(.*?)\"").find(text)?.groupValues?.get(1) ?: "Other"
        val urgency = Regex("\"urgency\": \"(.*?)\"").find(text)?.groupValues?.get(1) ?: "Medium"
        val sentiment = Regex("\"sentiment\": \"(.*?)\"").find(text)?.groupValues?.get(1) ?: "Neutral"
        return ComplaintAnalysis(category, urgency, sentiment)
    }
}

data class ComplaintAnalysis(
    val category: String,
    val urgency: String,
    val sentiment: String
)
