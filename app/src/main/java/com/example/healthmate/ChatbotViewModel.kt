package com.example.healthmate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.model.ChatMessage
import com.example.healthmate.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Chatbot feature following MVVM architecture.
 * Handles UI state and delegates API calls to the Repository layer.
 */
class ChatbotViewModel(
    private val repository: ChatbotRepository = ChatbotRepository()
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello! I'm your AI Health Assistant. I'm here to answer your health questions and provide general medical guidance. How can I help you today?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Sends a user message and gets AI response through the repository.
     * @param userMessage The message from the user
     */
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Add user message to the list
        val userChatMessage = ChatMessage(text = userMessage, isUser = true)
        _messages.value = _messages.value + userChatMessage

        // Set loading state
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            // Get conversation history (excluding the current user message we just added)
            val conversationHistory = _messages.value.dropLast(1)
            
            // Call repository to get AI response
            repository.sendMessage(userMessage, conversationHistory)
                .onSuccess { aiResponse ->
                // Add AI response
                val aiChatMessage = ChatMessage(text = aiResponse, isUser = false)
                _messages.value = _messages.value + aiChatMessage
                }
                .onFailure { exception ->
                // Handle errors gracefully
                val errorMessage = ChatMessage(
                        text = "I apologize, but I'm experiencing technical difficulties. " +
                                "Please check your internet connection and try again. " +
                                "Error: ${exception.message}",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
                    _error.value = exception.message
                }
                .also {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears the error state
     */
    fun clearError() {
        _error.value = null
    }
}
