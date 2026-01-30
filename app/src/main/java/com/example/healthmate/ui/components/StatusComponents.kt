package com.example.healthmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import com.example.healthmate.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthmate.ui.theme.MedicalBlue500
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.ui.theme.StatusEmergency
import com.example.healthmate.ui.theme.StatusError
import com.example.healthmate.ui.theme.StatusInfo
import com.example.healthmate.ui.theme.StatusSuccess
import com.example.healthmate.ui.theme.StatusWarning
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp

/**
 * Status & Feedback Components for HealthMate
 *
 * Components for displaying application states, feedback,
 * and status indicators.
 */

/**
 * Status Badge
 *
 * Colored chip for displaying appointment/record status.
 *
 * @param status Status text (e.g., "CONFIRMED", "CANCELLED", "COMPLETED")
 * @param size Badge size variant
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.MEDIUM
) {
    val (backgroundColor, textColor) = when (status.uppercase()) {
        "CONFIRMED", "ACTIVE", "APPROVED" -> StatusSuccess to Color.White
        "PENDING", "SCHEDULED" -> StatusWarning to Color.White
        "CANCELLED", "REJECTED", "INACTIVE" -> StatusError to Color.White
        "COMPLETED", "DONE" -> MaterialTheme.colorScheme.primary to Color.White
        "EMERGENCY", "URGENT" -> StatusEmergency to Color.White
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val paddingHorizontal = when (size) {
        BadgeSize.SMALL -> Spacing.sm
        BadgeSize.MEDIUM -> Spacing.md
        BadgeSize.LARGE -> Spacing.lg
    }

    val paddingVertical = when (size) {
        BadgeSize.SMALL -> Spacing.xs
        BadgeSize.MEDIUM -> Spacing.sm
        BadgeSize.LARGE -> Spacing.md
    }

    val textStyle = when (size) {
        BadgeSize.SMALL -> MaterialTheme.typography.labelSmall
        BadgeSize.MEDIUM -> MaterialTheme.typography.labelMedium
        BadgeSize.LARGE -> MaterialTheme.typography.labelLarge
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = paddingHorizontal, vertical = paddingVertical)
    ) {
        Text(
            text = status.uppercase(),
            style = textStyle,
            color = textColor
        )
    }
}

enum class BadgeSize {
    SMALL, MEDIUM, LARGE
}

/**
 * Status Chip - Alias for StatusBadge
 */
@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.MEDIUM
) {
    StatusBadge(status, modifier, size)
}

/**
 * Empty State
 *
 * Display when no data is available.
 *
 * @param icon Icon to display
 * @param title Empty state title
 * @param message Descriptive message
 * @param actionLabel Optional action button label
 * @param onAction Optional action callback
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(Spacing.xl))
            PrimaryButton(
                text = actionLabel,
                onClick = onAction
            )
        }
    }
}

/**
 * Loading State
 *
 * Display while data is being loaded.
 *
 * @param message Loading message (default "Loading...")
 */
/**
 * Premium Loading State
 *
 * Replaces the simple spinner with a pulsing HealthMate logo
 * for a high-end medical look.
 */
@Composable
fun LoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_final),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Error State
 *
 * Display when an error occurs.
 *
 * @param title Error title (default "Something went wrong")
 * @param message Error message
 * @param actionLabel Action button label (default "Retry")
 * @param onAction Action callback
 */
@Composable
fun ErrorState(
    message: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    actionLabel: String = "Retry"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error, // Standard Error icon
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = StatusError
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        PrimaryButton(
            text = actionLabel,
            onClick = onAction
        )
    }
}

/**
 * Info Badge
 *
 * Small info indicator badge (e.g., notification count).
 *
 * @param count Badge count
 */
@Composable
fun InfoBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .size(20.dp)
                .background(
                    color = StatusError,
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 9) "9+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}
/**
 * Shimmer Effect Modifier
 *
 * Provides a smooth medical-grade shimmering animation for skeleton screens.
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    return this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim - 500f, translateAnim - 500f),
            end = Offset(translateAnim, translateAnim)
        )
    )
}

/**
 * Skeleton Rect
 *
 * Basic rectangular skeleton block.
 */
@Composable
fun SkeletonRect(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    width: Dp = 100.dp,
    cornerRadius: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerEffect()
    )
}

/**
 * Skeleton Circle
 *
 * Circular skeleton block (typically for avatars).
 */
@Composable
fun SkeletonCircle(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .shimmerEffect()
    )
}

/**
 * Appointment List Skeleton
 *
 * Specialized skeleton for the appointments tab.
 */
@Composable
fun AppointmentListSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {
        // Filter chips skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                SkeletonRect(
                    width = 80.dp,
                    height = 32.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .shimmerEffect()
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // List items
        repeat(5) {
            HealthMateCard(
                elevation = 2.dp,
                padding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonRect(
                        width = 48.dp,
                        height = 48.dp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonRect(width = 140.dp, height = 18.dp)
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        SkeletonRect(width = 90.dp, height = 14.dp)
                    }
                }
            }
        }
    }
}

/**
 * Doctor List Skeleton
 *
 * Specialized skeleton for doctor search results.
 */
@Composable
fun DoctorListSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {
        repeat(6) {
            HealthMateCard(
                elevation = 2.dp,
                padding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonRect(
                        width = 56.dp,
                        height = 56.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonRect(width = 160.dp, height = 20.dp)
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        SkeletonRect(width = 100.dp, height = 14.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        SkeletonRect(width = 120.dp, height = 12.dp)
                    }
                }
            }
        }
    }
}

/**
 * Medical Records Skeleton
 * 
 * Grid-based skeleton for document storage.
 */
@Composable
fun MedicalRecordsSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {
        // Search bar skeleton
        SkeletonRect(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(Spacing.xl))
        
        // Grid items
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                repeat(2) {
                    HealthMateCard(
                        elevation = 2.dp,
                        padding = 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            SkeletonRect(
                                width = 48.dp,
                                height = 48.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .shimmerEffect()
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            SkeletonRect(width = 80.dp, height = 12.dp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

/**
 * Dashboard Skeleton
 *
 * Full page skeleton representing the home screen structure.
 */
@Composable
fun DashboardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        // Welcome Banner Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SkeletonRect(width = 100.dp, height = 16.dp)
                Spacer(modifier = Modifier.height(Spacing.sm))
                SkeletonRect(width = 180.dp, height = 32.dp)
                Spacer(modifier = Modifier.height(Spacing.sm))
                SkeletonRect(width = 140.dp, height = 14.dp)
            }
            SkeletonCircle(size = 56.dp)
        }

        // Quick Actions Skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SkeletonRect(modifier = Modifier.weight(1f), height = 100.dp, cornerRadius = 16.dp)
            SkeletonRect(modifier = Modifier.weight(1f), height = 100.dp, cornerRadius = 16.dp)
        }

        // Section Title
        SkeletonRect(width = 150.dp, height = 24.dp)

        // List Item Skeletons
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonRect(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect(),
                    width = 48.dp,
                    height = 48.dp
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonRect(width = 120.dp, height = 16.dp)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    SkeletonRect(width = 80.dp, height = 12.dp)
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}
