package com.example.healthmate.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.*

/**
 * Location helper utility for HealthMate app
 * Provides location services, geocoding, and distance calculations
 */
class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L // Update interval in milliseconds
    ).apply {
        setMinUpdateIntervalMillis(5000L)
        setWaitForAccurateLocation(false)
        setMaxUpdates(1)
    }.build()

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get the current location as a one-time request
     */
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // Last location is null, request a fresh one
                        requestFreshLocation(continuation)
                    }
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    private fun requestFreshLocation(continuation: kotlinx.coroutines.CancellableContinuation<Location?>) {
        try {
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(result.lastLocation)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )

            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Get continuous location updates as a Flow
     */
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        val continuousRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).apply {
            setMinUpdateIntervalMillis(3000L)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                continuousRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    /**
     * Get human-readable address from location coordinates
     */
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val address = formatAddress(addresses.firstOrNull())
                        continuation.resume(address)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = formatAddress(addresses?.firstOrNull())
                    continuation.resume(address)
                }
            } catch (e: Exception) {
                continuation.resume("Location unavailable")
            }
        }
    }

    /**
     * Get short location name (city/locality)
     */
    suspend fun getShortLocationName(latitude: Double, longitude: Double): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val name = addresses.firstOrNull()?.let { addr ->
                            addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Unknown"
                        } ?: "Unknown"
                        continuation.resume(name)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val name = addresses?.firstOrNull()?.let { addr ->
                        addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Unknown"
                    } ?: "Unknown"
                    continuation.resume(name)
                }
            } catch (e: Exception) {
                continuation.resume("Unknown")
            }
        }
    }

    private fun formatAddress(address: Address?): String {
        if (address == null) return "Location unavailable"

        return buildString {
            address.getAddressLine(0)?.let { append(it) }
                ?: run {
                    address.locality?.let { append(it) }
                    address.adminArea?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                    address.countryName?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                }
        }.ifEmpty { "Location unavailable" }
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @return distance in kilometers
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Format distance for display
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1 -> "${(distanceKm * 1000).toInt()} m"
            distanceKm < 10 -> String.format("%.1f km", distanceKm)
            else -> "${distanceKm.toInt()} km"
        }
    }

    /**
     * Generate Google Maps URL for location
     */
    fun getGoogleMapsUrl(latitude: Double, longitude: Double): String {
        return "https://maps.google.com/?q=$latitude,$longitude"
    }

    /**
     * Generate Google Maps directions URL
     */
    fun getDirectionsUrl(
        fromLat: Double,
        fromLon: Double,
        toLat: Double,
        toLon: Double
    ): String {
        return "https://www.google.com/maps/dir/?api=1&origin=$fromLat,$fromLon&destination=$toLat,$toLon&travelmode=driving"
    }

    companion object {
        // Emergency ambulance numbers for India
        const val AMBULANCE_NUMBER_108 = "108"
        const val AMBULANCE_NUMBER_102 = "102"
        const val EMERGENCY_NUMBER = "112"

        /**
         * Format emergency SMS with location
         */
        fun formatEmergencySms(
            userName: String,
            address: String,
            latitude: Double,
            longitude: Double
        ): String {
            return buildString {
                appendLine("EMERGENCY ALERT from $userName!")
                appendLine()
                appendLine("Location: $address")
                appendLine("Maps: https://maps.google.com/?q=$latitude,$longitude")
                appendLine()
                append("Please send help immediately!")
            }
        }
    }
}
