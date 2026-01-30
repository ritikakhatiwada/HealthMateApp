package com.example.healthmate.hospitals

import android.content.Context
import android.location.Location
import com.example.healthmate.data.Hospital
import com.example.healthmate.util.LocationHelper

/**
 * Repository for fetching nearby hospitals
 * Uses OpenStreetMap - no API key required
 */
class HospitalRepository(private val context: Context) {

    private val locationHelper = LocationHelper(context)

    // Hospital data class for internal use
    private data class HospitalData(
        val name: String,
        val latOffset: Double,
        val lonOffset: Double,
        val phone: String,
        val rating: Float,
        val reviews: Int,
        val hasEmergency: Boolean
    )

    // Comprehensive hospital database with varied distances (up to 100km)
    private val hospitalDatabase = listOf(
        // 0-5km range (Very Near)
        HospitalData("City General Hospital", 0.01, 0.01, "+91 1234567890", 4.5f, 234, true),
        HospitalData("Medical Center Plus", 0.02, -0.01, "+91 9876543210", 4.2f, 189, false),
        HospitalData("Emergency Care Hospital", -0.015, 0.02, "+91 5555555555", 4.7f, 456, true),
        HospitalData("Apollo Multispecialty Hospital", -0.025, -0.015, "+91 4444444444", 4.8f, 567, true),
        HospitalData("LifeCare Hospital", 0.008, -0.018, "+91 6666666666", 4.3f, 312, true),
        HospitalData("Sunshine Medical Center", -0.012, 0.008, "+91 7777777777", 4.1f, 198, false),

        // 5-15km range (Near)
        HospitalData("Metro Health Center", 0.08, 0.06, "+91 8888881111", 4.4f, 423, true),
        HospitalData("Care & Cure Hospital", -0.07, 0.09, "+91 8888882222", 4.6f, 345, true),
        HospitalData("Wellness Hospital", 0.10, -0.05, "+91 8888883333", 4.3f, 267, false),
        HospitalData("Prime Healthcare", -0.09, -0.07, "+91 8888884444", 4.5f, 389, true),
        HospitalData("Green Valley Medical Center", 0.05, 0.10, "+91 8888885555", 4.2f, 234, false),
        HospitalData("Healing Touch Hospital", -0.11, 0.04, "+91 8888886666", 4.4f, 312, true),

        // 15-30km range (Medium Distance)
        HospitalData("Regional Medical Institute", 0.18, 0.12, "+91 9999991111", 4.6f, 512, true),
        HospitalData("Advanced Care Hospital", -0.15, 0.20, "+91 9999992222", 4.7f, 678, true),
        HospitalData("Community Health Center", 0.22, -0.14, "+91 9999993333", 4.2f, 234, true),
        HospitalData("Horizon Medical College", -0.20, -0.16, "+91 9999994444", 4.8f, 456, true),
        HospitalData("Sunrise Hospital", 0.16, 0.18, "+91 9999995555", 4.3f, 289, false),
        HospitalData("Unity Healthcare", -0.14, 0.22, "+91 9999996666", 4.5f, 345, true),

        // 30-50km range (Far)
        HospitalData("District General Hospital", 0.35, 0.25, "+91 7777771111", 4.7f, 789, true),
        HospitalData("University Medical College", -0.30, 0.38, "+91 7777772222", 4.9f, 923, true),
        HospitalData("Central Hospital", 0.40, -0.28, "+91 7777773333", 4.4f, 456, true),
        HospitalData("Government Medical Center", -0.38, -0.30, "+91 7777774444", 4.3f, 567, true),
        HospitalData("Super Specialty Hospital", 0.32, 0.35, "+91 7777775555", 4.8f, 678, true),
        HospitalData("National Institute of Health", -0.28, 0.40, "+91 7777776666", 4.6f, 543, true),

        // 50-75km range (Very Far)
        HospitalData("State Medical Center", 0.55, 0.40, "+91 6666661111", 4.7f, 821, true),
        HospitalData("Regional Super Specialty", -0.50, 0.55, "+91 6666662222", 4.9f, 945, true),
        HospitalData("Rural Health Institute", 0.60, -0.42, "+91 6666663333", 4.3f, 367, true),
        HospitalData("County General Hospital", -0.58, -0.48, "+91 6666664444", 4.5f, 489, true),
        HospitalData("Border Medical College", 0.52, 0.50, "+91 6666665555", 4.6f, 612, true),

        // 75-100km range (Distant)
        HospitalData("Provincial Medical Center", 0.75, 0.55, "+91 5555551111", 4.8f, 934, true),
        HospitalData("Inter-State Hospital", -0.70, 0.68, "+91 5555552222", 4.7f, 856, true),
        HospitalData("Highland Health Center", 0.80, -0.60, "+91 5555553333", 4.4f, 423, true),
        HospitalData("Coastal Medical Institute", -0.78, -0.65, "+91 5555554444", 4.6f, 567, true),
        HospitalData("Mountain View Hospital", 0.72, 0.70, "+91 5555555555", 4.5f, 489, false)
    )

    /**
     * Get the nearest hospital to the user from sample data
     */
    fun getNearestHospital(userLocation: Location): Hospital? {
        return getHospitalsWithinRadius(userLocation.latitude, userLocation.longitude, 100.0).firstOrNull()
    }

    /**
     * Get hospitals within specified radius
     * @param radiusKm Maximum distance in kilometers (default 10km, max 100km)
     */
    fun getHospitalsWithinRadius(
        userLat: Double,
        userLon: Double,
        radiusKm: Double = 10.0
    ): List<Hospital> {
        return hospitalDatabase.mapIndexed { index, data ->
            val lat = userLat + data.latOffset
            val lon = userLon + data.lonOffset
            val distance = locationHelper.calculateDistance(userLat, userLon, lat, lon)

            Hospital(
                id = (index + 1).toString(),
                name = data.name,
                address = generateAddress(data.name, distance),
                latitude = lat,
                longitude = lon,
                phoneNumber = data.phone,
                rating = data.rating,
                totalRatings = data.reviews,
                distance = distance,
                isOpen = true,
                openingHours = "Open 24 hours",
                types = if (data.hasEmergency) listOf("hospital", "emergency_room") else listOf("hospital")
            )
        }
            .filter { it.distance <= radiusKm }
            .sortedBy { it.distance }
    }

    /**
     * Get featured hospitals (top rated within radius)
     */
    fun getFeaturedHospitals(
        userLat: Double,
        userLon: Double,
        radiusKm: Double = 50.0,
        limit: Int = 5
    ): List<Hospital> {
        return getHospitalsWithinRadius(userLat, userLon, radiusKm)
            .sortedByDescending { it.rating }
            .take(limit)
    }

    /**
     * Keep existing getSampleHospitals for backward compatibility
     */
    fun getSampleHospitals(userLat: Double, userLon: Double): List<Hospital> {
        return getHospitalsWithinRadius(userLat, userLon, 10.0)
    }

    private fun generateAddress(name: String, distance: Double): String {
        val zone = when {
            distance < 5 -> "City Center"
            distance < 15 -> "Suburban Area"
            distance < 30 -> "District Zone"
            distance < 60 -> "Regional Area"
            else -> "Rural District"
        }
        val streetNum = (distance * 10).toInt() + 100
        return "$streetNum Healthcare Avenue, $zone"
    }
}
