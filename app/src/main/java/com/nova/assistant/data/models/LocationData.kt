package com.nova.assistant.data.models

/**
 * Real device GPS coordinate snapshot.
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
    val altitudeMeters: Double? = null,
    val speedMps: Float? = null,
    val bearingDegrees: Float? = null,
    val provider: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
