package com.nova.assistant.data.models

/**
 * Domain model representing a conversation turn between the user and NOVA.
 */
data class ConversationItem(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userCommand: String,
    val assistantResponse: String,
    val isSuccess: Boolean = true,
    val intentId: String = "UNKNOWN"
)
