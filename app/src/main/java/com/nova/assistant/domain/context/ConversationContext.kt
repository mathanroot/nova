package com.nova.assistant.domain.context

import com.nova.assistant.domain.intent.NovaIntent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationContext @Inject constructor() {
    var lastIntent: NovaIntent? = null
        private set
    var lastUserQuery: String? = null
        private set
    var lastAssistantResponse: String? = null
        private set
    var lastTargetApp: String? = null
        private set
    var lastTargetContact: String? = null
        private set

    fun updateContext(
        intent: NovaIntent,
        query: String,
        response: String,
        targetApp: String? = null,
        targetContact: String? = null
    ) {
        lastIntent = intent
        lastUserQuery = query
        lastAssistantResponse = response
        if (targetApp != null) lastTargetApp = targetApp
        if (targetContact != null) lastTargetContact = targetContact
    }

    fun clear() {
        lastIntent = null
        lastUserQuery = null
        lastAssistantResponse = null
        lastTargetApp = null
        lastTargetContact = null
    }

    /**
     * Resolves pronouns or elliptical follow-ups based on context.
     * e.g., "what about my storage", "turn it off", "open it again"
     */
    fun resolveContextualIntent(input: String): NovaIntent? {
        val trimmed = input.trim().lowercase()

        // Storage follow up
        if (trimmed.contains("what about") && trimmed.contains("storage")) {
            return NovaIntent.STORAGE_CHECK
        }
        if (trimmed.contains("what about") && trimmed.contains("battery")) {
            return NovaIntent.BATTERY_CHECK
        }
        if (trimmed.contains("what about") && (trimmed.contains("location") || trimmed.contains("coordinates"))) {
            return NovaIntent.LOCATION_GET
        }
        if (trimmed.contains("what about") && trimmed.contains("wifi")) {
            return NovaIntent.WIFI_STATUS
        }

        // Action follow ups: "turn it on", "turn it off"
        if (trimmed == "turn it on" || trimmed == "switch it on") {
            if (lastIntent == NovaIntent.FLASHLIGHT_OFF || lastIntent == NovaIntent.FLASHLIGHT_ON) {
                return NovaIntent.FLASHLIGHT_ON
            }
        }
        if (trimmed == "turn it off" || trimmed == "switch it off") {
            if (lastIntent == NovaIntent.FLASHLIGHT_ON || lastIntent == NovaIntent.FLASHLIGHT_OFF) {
                return NovaIntent.FLASHLIGHT_OFF
            }
        }

        return null
    }
}
