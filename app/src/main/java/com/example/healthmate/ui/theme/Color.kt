package com.example.healthmate.ui.theme

import androidx.compose.ui.graphics.Color

// ============================
// MEDICAL BLUE DESIGN SYSTEM
// ============================

// Primary Palette (Calm Medical Blue)
val MedicalBlue500 = Color(0xFF2A7FBA)
val MedicalBlue700 = Color(0xFF1F5F8B)

// Secondary Palette (Soft Medical Teal)
val MedicalTeal500 = Color(0xFF2EC4B6)

// Neutral Palette
val NeutralWhite = Color(0xFFFFFFFF)
val NeutralBlueWhite = Color(0xFFF5F9FC) // Background
val TextPrimary = Color(0xFF1C1C1E)
val TextSecondary = Color(0xFF6B7280)
val BorderGray = Color(0xFFE5E7EB)

// Semantic Palette
val MedicalRed = Color(0xFFE63946)
val MedicalGreen = Color(0xFF2E7D32)
val MedicalInfo = Color(0xFF3B82F6)

// ============================
// SEMANTIC COLOR TOKENS
// ============================
val AppBackground = NeutralBlueWhite
val CardSurface = NeutralWhite
val DividerColor = BorderGray
val Overlay = Color(0x99000000)

// ============================
// LIGHT MODE COLOR SCHEME
// ============================
val LightPrimary = MedicalBlue500
val LightOnPrimary = NeutralWhite
val LightSecondary = MedicalTeal500
val LightOnSecondary = NeutralWhite
val LightBackground = AppBackground
val LightOnBackground = TextPrimary
val LightSurface = CardSurface
val LightOnSurface = TextPrimary
val LightError = MedicalRed
val LightOnError = NeutralWhite

// ============================
// DARK MODE COLOR SCHEME
// ============================
val DarkPrimary = Color(0xFF4A9ED7) // Slightly lighter for dark mode
val DarkOnPrimary = Color(0xFF111827)
val DarkSecondary = Color(0xFF4FD1C5)
val DarkOnSecondary = Color(0xFF111827)
val DarkBackground = Color(0xFF111827)
val DarkOnBackground = Color(0xFFF9FAFB)
val DarkSurface = Color(0xFF1F2937)
val DarkOnSurface = Color(0xFFF9FAFB)
val DarkError = MedicalRed
val DarkOnError = NeutralWhite

// ============================
// COMPATIBILITY TOKENS
// ============================
// These ensure existing component code doesn't break
val MedicalGreen600 = MedicalBlue500
val MedicalGreen700 = MedicalBlue700
val MedicalGreen800 = MedicalBlue700
val StatusSuccess = MedicalGreen
val StatusWarning = Color(0xFFF59E0B)
val StatusError = MedicalRed
val StatusEmergency = MedicalRed
val StatusInfo = MedicalBlue500

// ============================
// PROFESSIONAL HEALTHCARE PALETTE
// ============================
val HealthcareTeal = Color(0xFF00897B)
val HealthcareTealLight = Color(0xFF4DB6AC)
val HealthcareTealDark = Color(0xFF00695C)
val SoftMedicalBlue = Color(0xFF42A5F5)
val AlertRed = Color(0xFFE53935)
val SafeGreen = Color(0xFF43A047)
val HeroGradientStart = Color(0xFF00897B)
val HeroGradientEnd = Color(0xFF00695C)

// Hospital Locator Colors
val HospitalMarkerColor = Color(0xFFE53935)
val LocationPinColor = Color(0xFF1976D2)
