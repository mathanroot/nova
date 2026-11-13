package com.nova.assistant.domain.intent

import com.nova.assistant.domain.entity.ExtractedEntities

data class IntentMatchResult(
    val intent: NovaIntent,
    val confidence: Float,
    val matchedPhrase: String,
    val entities: ExtractedEntities = ExtractedEntities()
)
