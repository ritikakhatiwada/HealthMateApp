package com.example.healthmate.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * HealthMate Dimension System
 *
 * Standardized spacing, elevation, and size tokens for consistent layouts.
 * Replaces hardcoded values with semantic tokens for maintainability.
 */

// ============================
// SPACING SCALE
// ============================
/**
 * Spacing tokens for padding, margin, and gaps.
 * Use these instead of hardcoded dp values.
 *
 * Usage examples:
 * - Spacing.xs: Icon padding, tight gaps
 * - Spacing.sm: List item internal spacing
 * - Spacing.md: Form field spacing
 * - Spacing.lg: Card padding (default)
 * - Spacing.xl: Section spacing
 * - Spacing.xxl: Screen padding
 * - Spacing.xxxl: Major section breaks
 * - Spacing.huge: Hero spacing
 */
object Spacing {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp      // Extra small - icon padding, tight gaps
    val sm: Dp = 8.dp      // Small - list item spacing
    val md: Dp = 12.dp     // Medium - form fields, moderate gaps
    val lg: Dp = 16.dp     // Large - card padding (default)
    val xl: Dp = 20.dp     // Extra large - section spacing
    val xxl: Dp = 24.dp    // 2XL - screen padding, major sections
    val xxxl: Dp = 32.dp   // 3XL - large section breaks
    val huge: Dp = 48.dp   // Huge - hero spacing, major dividers
}

// ============================
// ELEVATION SCALE
// ============================
/**
 * Elevation tokens for card and surface shadows.
 * Subtle elevations maintain medical-grade professionalism.
 *
 * Usage examples:
 * - Elevation.none: Flat cards, background elements
 * - Elevation.low: Subtle cards (1dp)
 * - Elevation.medium: Standard cards (2dp) - default
 * - Elevation.high: Emphasized cards (4dp)
 * - Elevation.xHigh: Modals, dialogs (8dp)
 */
object Elevation {
    val none: Dp = 0.dp     // Flat, no shadow
    val low: Dp = 1.dp      // Very subtle elevation
    val medium: Dp = 2.dp   // Standard card elevation (default)
    val high: Dp = 4.dp     // Emphasized cards
    val xHigh: Dp = 8.dp    // Dialogs, floating elements
}

// ============================
// ICON SIZE SCALE
// ============================
/**
 * Icon size tokens for consistent iconography.
 *
 * Usage examples:
 * - IconSize.xs: Status indicators (16dp)
 * - IconSize.sm: Inline icons (20dp)
 * - IconSize.md: Standard icons (24dp) - default
 * - IconSize.lg: Feature icons (32dp)
 * - IconSize.xl: Hero icons (48dp)
 * - IconSize.xxl: Large illustrations (64dp)
 */
object IconSize {
    val xs: Dp = 16.dp      // Status indicators, badges
    val sm: Dp = 20.dp      // Inline icons, list trailing icons
    val md: Dp = 24.dp      // Standard Material icons (default)
    val lg: Dp = 32.dp      // Feature icons, quick actions
    val xl: Dp = 48.dp      // Hero icons, large actions
    val xxl: Dp = 64.dp     // Illustrations, empty states
}

// ============================
// BORDER WIDTH
// ============================
/**
 * Border width tokens for outlined elements.
 */
object BorderWidth {
    val thin: Dp = 1.dp     // Default borders, dividers
    val medium: Dp = 2.dp   // Emphasized borders, focused states
    val thick: Dp = 3.dp    // Strong emphasis
}

// ============================
// CORNER RADIUS (Convenience)
// ============================
/**
 * Standalone corner radius values.
 * Note: Prefer using HealthMateShapes for complete shape definitions.
 */
object CornerRadius {
    val none: Dp = 0.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val full: Dp = 9999.dp  // Fully rounded (pill shape)
}
