package com.example.healthmate.model

/**
 * Data model representing a chat message in the conversation.
 * @param text The message content
 * @param isUser Whether the message is from the user (true) or AI (false)
 * @param timestamp The timestamp when the message was created
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

