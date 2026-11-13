package com.nova.assistant.data.models

/**
 * Real-time operational states of the NOVA voice assistant.
 * Drives the animated fluid orb, UI feedback, and audio processing pipelines.
 */
enum class AssistantState(val displayName: String) {
    IDLE("Ready"),
    WAKE_WORD_LISTENING("Listening for 'Hey Nova'"),
    WAKE_WORD_DETECTED("'Hey Nova' Detected!"),
    COMMAND_LISTENING("Listening..."),
    PROCESSING("Understanding..."),
    EXECUTING("Executing command..."),
    SPEAKING("Responding..."),
    ERROR("Error"),
    PAUSED("Paused")
}
