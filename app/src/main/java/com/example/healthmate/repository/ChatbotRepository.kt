package com.example.healthmate.repository

import android.util.Log
import com.example.healthmate.BuildConfig
import com.example.healthmate.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatbotRepository {

    private val tag = "ChatbotRepository"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

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

    private val modelName = "gemini-3-flash-preview"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    /** Sends a message to Gemini API */
    suspend fun sendMessage(
            userMessage: String,
            conversationHistory: List<ChatMessage> = emptyList()
    ): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    Log.d(tag, "Sending message to Gemini via REST: $userMessage")

                    val apiKey = BuildConfig.GEMINI_API_KEY.trim()
                    val url = "$baseUrl?key=$apiKey"

                    // Construct JSON payload
                    val contents = JSONArray()

                    // Add System Prompt & Initial Setup
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                    })
                    contents.put(JSONObject().apply {
                        put("role", "model")
                        put("parts", JSONArray().put(JSONObject().put("text", "Understood. I am ready to help with health-related queries.")))
                    })

                    // Add History
                    conversationHistory.takeLast(20).forEach { msg ->
                        contents.put(JSONObject().apply {
                            put("role", if (msg.isUser) "user" else "model")
                            put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                        })
                    }

                    // Add Current Message
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
                    })

                    val jsonPayload = JSONObject().apply {
                        put("contents", contents)
                        put("generationConfig", JSONObject().apply {
                            put("temperature", 0.4)
                            put("topK", 20)
                            put("topP", 0.8)
                            put("maxOutputTokens", 800)
                        })
                    }

                    val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            val errorBody = response.body?.string() ?: "Unknown error"
                            Log.e(tag, "API Error: $errorBody")
                            return@withContext Result.failure(Exception(parseError(errorBody)))
                        }

                        val responseBody = response.body?.string() ?: throw Exception("Empty response body")
                        val jsonResponse = JSONObject(responseBody)
                        val candidates = jsonResponse.optJSONArray("candidates") ?: throw Exception("No candidates found")
                        
                        if (candidates.length() == 0) throw Exception("Empty candidates list")
                        
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content") ?: throw Exception("No content in candidate")
                        val parts = content.optJSONArray("parts") ?: throw Exception("No parts in candidate content")
                        
                        if (parts.length() == 0) throw Exception("Empty parts list")
                        
                        val text = parts.getJSONObject(0).optString("text")
                        
                        Log.d(tag, "Gemini response received successfully")
                        Result.success(text.trim())
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error sending message to Gemini", e)
                    Result.failure(e)
                }
            }

    private fun parseError(errorJson: String): String {
        return try {
            val obj = JSONObject(errorJson)
            val error = obj.optJSONObject("error")
            error?.optString("message") ?: "Unknown API Error"
        } catch (e: Exception) {
            "Error parsing API response: ${e.message}"
        }
    }
}
