package com.example.healthmate.repository

import com.example.healthmate.BuildConfig
import com.example.healthmate.model.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository layer for handling Gemini API calls.
 * This follows the MVVM pattern by separating data access logic from ViewModel.
 */
class ChatbotRepository {
    
    // Health-focused system prompt to ensure the chatbot only answers health-related questions
    private val systemPrompt = """
        You are a helpful AI Health Assistant. Your role is to provide general health information, 
        wellness tips, and basic medical guidance. 
        
        IMPORTANT GUIDELINES:
        1. Only answer questions related to health, wellness, fitness, nutrition, mental health, 
           symptoms, medications, and general medical topics.
        2. If asked about non-health topics (like weather, sports, politics, etc.), politely decline 
           and redirect to health-related questions.
        3. Always include disclaimers that your advice is for informational purposes only and 
           not a substitute for professional medical advice.
        4. For serious symptoms or medical emergencies, always recommend consulting a healthcare professional.
        5. Be empathetic, clear, and concise in your responses.
        6. If you're unsure about something, admit it and suggest consulting a healthcare provider.
        
        Remember: You are a basic health assistant providing general guidance, not a replacement 
        for professional medical care.
    """.trimIndent()

    private val generativeModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    /**
     * Sends a message to the Gemini API and returns the AI response.
     * @param userMessage The user's message
     * @param conversationHistory Previous messages in the conversation for context
     * @return The AI's response message
     */
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Build conversation history with system prompt
            val conversationContext = mutableListOf<com.google.ai.client.generativeai.type.Content>()
            
            // Add system prompt as the first message
            conversationContext.add(
                content("user") {
                    text(systemPrompt)
                }
            )
            conversationContext.add(
                content("model") {
                    text("I understand. I'm your AI Health Assistant, ready to help with health-related questions only.")
                }
            )
            
            // Add conversation history
            conversationHistory.forEach { message ->
                if (message.isUser) {
                    conversationContext.add(
                        content("user") { text(message.text) }
                    )
                } else {
                    conversationContext.add(
                        content("model") { text(message.text) }
                    )
                }
            }
            
            // Add current user message
            conversationContext.add(
                content("user") { text(userMessage) }
            )

            // Generate response
            val response = generativeModel.generateContent(conversationContext)
            val aiResponse = response.text 
                ?: "I'm sorry, I couldn't generate a response. Please try again."

            Result.success(aiResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

