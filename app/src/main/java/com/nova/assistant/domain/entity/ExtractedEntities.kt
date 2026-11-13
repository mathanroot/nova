package com.nova.assistant.domain.entity

/**
 * Structured entities extracted from spoken/textual commands.
 */
data class ExtractedEntities(
    val percentage: Int? = null,
    val targetNumber: Double? = null,
    val targetAppName: String? = null,
    val contactName: String? = null,
    val phoneNumber: String? = null,
    val messageBody: String? = null,
    val durationSeconds: Int? = null,
    val timeHour: Int? = null,
    val timeMinute: Int? = null,
    val isPm: Boolean? = null,
    val mathExpression: String? = null,
    val mathResult: Double? = null,
    val unitValue: Double? = null,
    val unitFrom: String? = null,
    val unitTo: String? = null,
    val convertedValue: Double? = null
)
