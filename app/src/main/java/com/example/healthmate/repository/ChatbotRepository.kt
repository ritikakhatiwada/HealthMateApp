package com.example.healthmate.repository

import android.util.Log
import com.example.healthmate.BuildConfig
import com.example.healthmate.model.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatbotRepository {

    private val tag = "ChatbotRepository"

    private val systemPrompt =
            """
        You are a helpful AI Health Assistant. Your role is to provide general health information,
        wellness tips, and basic medical guidance.

        IMPORTANT GUIDELINES:
        1. Only answer health-related questions.
        2. Do not give medical diagnosis or prescriptions.
        3. Always include a disclaimer that this is not medical advice.
        4. Recommend consulting a healthcare professional for serious symptoms.
        5. Be empathetic, clear, and concise.
    """.trimIndent()

    private fun createModel(): GenerativeModel {
        return GenerativeModel(
                // Reverting to the standard flash model. The 404 errors indicate an API Key
                // permission issue,
                // not a model availability issue (since both Pro and Flash failed).
                modelName = "gemini-1.5-flash",
                // IMPORTANT: Trimming the API key to remove any potential accidental
                // whitespace/newline
                apiKey = BuildConfig.GEMINI_API_KEY.trim(),
                generationConfig =
                        generationConfig {
                            temperature = 0.4f
                            topK = 20
                            topP = 0.8f
                            maxOutputTokens = 800
                        }
        )
    }

    /** Sends a message to Gemini API */
    suspend fun sendMessage(
            userMessage: String,
            conversationHistory: List<ChatMessage> = emptyList()
    ): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    Log.d(tag, "Sending message to Gemini: $userMessage")

                    val model = createModel()

                    // Construct history
                    val history =
                            mutableListOf(
                                    content(role = "user") { text(systemPrompt) },
                                    content(role = "model") {
                                        text(
                                                "Understood. I am ready to help with health-related queries."
                                        )
                                    }
                            )

                    // Add conversation history
                    // Take recent history to maintain context
                    conversationHistory.takeLast(20).forEach { msg ->
                        val role = if (msg.isUser) "user" else "model"
                        history.add(content(role = role) { text(msg.text) })
                    }

                    val chat = model.startChat(history = history)

                    val response = chat.sendMessage(userMessage)
                    val aiResponse =
                            response.text
                                    ?: return@withContext Result.failure(
                                            Exception("Empty response from Gemini")
                                    )

                    Log.d(tag, "Gemini response received successfully")
                    Result.success(aiResponse.trim())
                } catch (e: Exception) {
                    Log.e(tag, "Error sending message to Gemini", e)

                    // Handle the specific Serialization bug in the SDK where it fails to parse the
                    // error response
                    if (e.javaClass.name.contains("Serialization", ignoreCase = true) ||
                                    e.message?.contains("MissingFieldException") == true
                    ) {
                        return@withContext Result.failure(
                                Exception(
                                        "Service configuration error. Please ensure the Google Generative AI API is enabled for your project API Key."
                                )
                        )
                    }

                    // Extensive error handling as requested
                    val errorMessage =
                            when {
                                e.message?.contains("API_KEY_INVALID", ignoreCase = true) == true ->
                                        "Invalid API key. Please check your Gemini API key."
                                e.message?.contains("quota", ignoreCase = true) == true ->
                                        "API quota exceeded. Please try again in a few minutes."
                                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) ==
                                        true ->
                                        "Permission denied. Please check your API key permissions."
                                e.message?.contains("404") == true ||
                                        e.message?.contains("NOT_FOUND") == true ->
                                        "Model not available. Please ensure your API Key has access to 'gemini-1.5-flash'."
                                e.message?.contains("429") == true ||
                                        e.message?.contains("RESOURCE_EXHAUSTED") == true ->
                                        "Too many requests. Please wait and try again."
                                e.message?.contains("network", ignoreCase = true) == true ||
                                        e.message?.contains(
                                                "Unable to resolve host",
                                                ignoreCase = true
                                        ) == true ->
                                        "Network error. Please check your internet connection."
                                e.message?.contains("DEADLINE_EXCEEDED", ignoreCase = true) ==
                                        true -> "Request timeout. Please try again."
                                else ->
                                        "Sorry, something went wrong. Please try again. (${e.message})"
                            }

                    Result.failure(Exception(errorMessage, e))
                }
            }
}
