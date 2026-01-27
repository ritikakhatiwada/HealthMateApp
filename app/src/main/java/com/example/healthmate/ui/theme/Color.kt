package com.example.healthmate.ui.theme

import androidx.compose.ui.graphics.Color

// Apollo-Style Green Healthcare Palette
val PrimaryGreen = Color(0xFF1DBF73)
val DarkGreen = Color(0xFF0E8F5B)
val SoftMint = Color(0xFFE6F6EF)
val AppBackground = Color(0xFFF7F9F8)
val CardWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1F2933)
val TextSecondary = Color(0xFF6B7280)

// Light Mode Colors
val LightPrimary = PrimaryGreen
val LightOnPrimary = Color.White
val LightSecondary = DarkGreen
val LightOnSecondary = Color.White
val LightBackground = AppBackground
val LightOnBackground = TextPrimary
val LightSurface = CardWhite
val LightOnSurface = TextPrimary
val LightSurfaceVariant = Color(0xFFFFFFFF) // Cards are white
val LightOnSurfaceVariant = TextSecondary
val LightOutline = Color(0xFFE5E7EB)

// Dark Mode Colors (Adapted for readability while keeping brand identity)
val DarkPrimary = Color(0xFF1DBF73) // Keep brand green
val DarkOnPrimary = Color.White
val DarkSecondary = Color(0xFF4FA3D1) // Slightly bluer for contrast on dark? Or keep Green.
val DarkOnSecondary = Color.White
// Let's keep it consistent but softer for dark mode backgrounds
val DarkBackground = Color(0xFF0F172A) // Deep Slate
val DarkOnBackground = Color(0xFFE2E8F0)
val DarkSurface = Color(0xFF1E293B) // Slate 800
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)

// Status Colors
val Success = Color(0xFF34C759)
val Warning = Color(0xFFF59E0B)
val Error = Color(0xFFEF4444)
val DividerColor = Color(0xFFE5E7EB)

// Legacy compatibility if needed (can be removed if unused)
val Purple40 = PrimaryGreen
val Purple80 = SoftMint
val PurpleGrey40 = TextSecondary
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink40 = Color(0xFF7D5260)
val Pink80 = Color(0xFFEFB8C8)
