package com.nova.assistant.device.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.nova.assistant.data.models.LocationData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isGpsEnabled(): Boolean {
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getCurrentLocation(): Result<LocationData> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission is not granted"))
        }
        if (!isGpsEnabled()) {
            return Result.failure(IllegalStateException("GPS/Location services are disabled on this device"))
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc: Location? ->
                        if (loc != null) {
                            continuation.resume(Result.success(loc.toLocationData()))
                        } else {
                            // Fallback to last known location from LocationManager
                            val fallback = getFallbackLastKnownLocation()
                            if (fallback != null) {
                                continuation.resume(Result.success(fallback.toLocationData()))
                            } else {
                                continuation.resume(Result.failure(IllegalStateException("GPS fix unavailable. Please move to an open area.")))
                            }
                        }
                    }
                    .addOnFailureListener { exc ->
                        val fallback = getFallbackLastKnownLocation()
                        if (fallback != null) {
                            continuation.resume(Result.success(fallback.toLocationData()))
                        } else {
                            continuation.resume(Result.failure(exc))
                        }
                    }
            } catch (se: SecurityException) {
                continuation.resume(Result.failure(se))
            }
        }
    }

    private fun getFallbackLastKnownLocation(): Location? {
        return try {
            val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            when {
                gpsLoc != null && netLoc != null -> if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
                gpsLoc != null -> gpsLoc
                else -> netLoc
            }
        } catch (_: Exception) {
            null
        }
    }

    fun createMapsIntent(latitude: Double, longitude: Double): Intent {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Current+Location)")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun Location.toLocationData(): LocationData {
        return LocationData(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = if (hasAccuracy()) accuracy else null,
            altitudeMeters = if (hasAltitude()) altitude else null,
            speedMps = if (hasSpeed()) speed else null,
            bearingDegrees = if (hasBearing()) bearing else null,
            provider = provider,
            timestamp = time
        )
    }
}
