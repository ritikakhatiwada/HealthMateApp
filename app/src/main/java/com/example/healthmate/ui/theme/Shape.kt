package com.example.healthmate.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * HealthMate Shape System
 */
val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),  // Input Fields
    medium = RoundedCornerShape(14.dp), // Buttons
    large = RoundedCornerShape(16.dp)   // Cards
)

// Named shapes for clarity
object HealthMateShapes {
    val CardLarge = RoundedCornerShape(16.dp)
    val ButtonLarge = RoundedCornerShape(14.dp)
    val InputField = RoundedCornerShape(12.dp)
    val Dialog = RoundedCornerShape(20.dp)
    val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}
