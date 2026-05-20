package com.swizosoft.hostelcomplaints.ai

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.swizosoft.hostelcomplaints.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(context: Context) {
    private val apiKey = context.getString(R.string.gemini_api_key)
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun analyzeComplaint(description: String): ComplaintAnalysis = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze the following hostel maintenance complaint description and provide a professional assessment.
            
            Categories: Electricity, Water, WiFi, Plumbing, Furniture, Cleanliness, Security, Other
            Urgency Levels:
            - Low: Minor issues that don't affect daily life much (e.g., slow internet, small stain).
            - Medium: Issues that are inconvenient but not dangerous (e.g., fan making noise, tap dripping).
            - High: Major issues that affect quality of life (e.g., no water, no power, broken furniture).
            - Critical: Immediate safety hazards or total infrastructure failure (e.g., fire, sparks, smoke, flooding, broken main lock, electrical shocks).
            
            Sentiments: Frustrated, Neutral, Calm

            Complaint Description: "$description"

            IMPORTANT GUIDELINES:
            1. If the complaint mentions immediate electrical dangers like "fire", "spark", "sparks", "smoke", "shocks", or "burning", the urgency MUST be 'Critical' or 'High' and Category MUST be 'Electricity'.
            2. For 'temporarySolution', provide highly specific safety/convenience advice based on the issue:
               - For electrical sparks/fire/smoke: Suggest looking for a main tripper/MCB switch nearby to turn it off immediately, avoiding touching any connected appliances or metallic surfaces, and waiting until the electrician arrives to ensure safety.
               - For water leakage/flooding: Suggest closing the local inlet valve, placing a bucket underneath, and moving belongings off the wet floor.
               - For other issues: Provide a matching safe, actionable step for the student.

            Return ONLY a valid JSON object:
            {
              "category": "...",
              "urgency": "...",
              "sentiment": "...",
              "enhancedDescription": "A professional, polite, and detailed description rewriting the student's raw complaint to make it extremely clear for the maintenance staff.",
              "temporarySolution": "A practical, safe step or workaround the student can perform for the time being until maintenance arrives to resolve the issue."
            }
        """.trimIndent()

        return@withContext try {
            val response = model.generateContent(prompt)
            val text = response.text ?: ""
            parseAnalysis(text, description)
        } catch (e: Exception) {
            android.util.Log.e("GeminiService", "Error analyzing complaint", e)
            ComplaintAnalysis(
                category = "Other",
                urgency = "Medium",
                sentiment = "Neutral",
                enhancedDescription = "Error: ${e.localizedMessage ?: "Unknown error"}",
                temporarySolution = "Please wait for maintenance to inspect."
            )
        }
    }

    private fun parseAnalysis(text: String, originalDescription: String): ComplaintAnalysis {
        // Clean the text from markdown blocks if present
        val cleanText = text.replace("```json", "").replace("```", "").trim()
        
        fun extractValue(key: String, default: String): String {
            // Regex handles optional spaces around colon, is case-insensitive, and supports multi-line content
            val regex = Regex("\"$key\"\\s*:\\s*\"(.*?)\"", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            val rawValue = regex.find(cleanText)?.groupValues?.get(1)?.trim() ?: return default
            // Clean up escaped newlines and double quotes from JSON string
            return rawValue.replace("\\n", "\n").replace("\\\"", "\"")
        }

        val category = extractValue("category", "Other")
        val urgency = extractValue("urgency", "Medium")
        val sentiment = extractValue("sentiment", "Neutral")
        val enhancedDescription = extractValue("enhancedDescription", originalDescription)
        val temporarySolution = extractValue("temporarySolution", "No immediate workaround available.")
        
        // Normalize results to ensure they match expected casing (e.g., "High", "Critical")
        return ComplaintAnalysis(
            category = category.lowercase().replaceFirstChar { it.uppercase() },
            urgency = urgency.lowercase().replaceFirstChar { it.uppercase() },
            sentiment = sentiment.lowercase().replaceFirstChar { it.uppercase() },
            enhancedDescription = enhancedDescription,
            temporarySolution = temporarySolution
        )
    }
}

data class ComplaintAnalysis(
    val category: String,
    val urgency: String,
    val sentiment: String,
    val enhancedDescription: String,
    val temporarySolution: String
)
