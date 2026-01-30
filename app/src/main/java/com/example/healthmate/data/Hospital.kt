package com.example.healthmate.data

/**
 * Hospital data model for Hospital Locator feature
 */
data class Hospital(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phoneNumber: String = "",
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val distance: Double = 0.0, // Distance in km from user
    val isOpen: Boolean = true,
    val openingHours: String = "Open 24 hours",
    val photoUrl: String? = null,
    val types: List<String> = emptyList() // e.g., "hospital", "emergency_room", etc.
) {
    val formattedRating: String
        get() = String.format("%.1f", rating)

    val formattedDistance: String
        get() = when {
            distance < 1 -> "${(distance * 1000).toInt()} m"
            distance < 10 -> String.format("%.1f km", distance)
            else -> "${distance.toInt()} km"
        }

    val hasEmergency: Boolean
        get() = types.any { it.contains("emergency", ignoreCase = true) }
}
