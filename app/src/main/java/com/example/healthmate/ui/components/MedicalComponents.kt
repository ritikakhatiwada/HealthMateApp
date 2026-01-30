package com.example.healthmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.healthmate.ui.theme.MedicalBlue500
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.CloudinaryHelper

/**
 * Medical Components for HealthMate
 *
 * Specialty components for healthcare app UI including
 * profile avatars, section headers, quick actions, etc.
 */

/**
 * Profile Avatar - Apollo Hospital Style
 *
 * Circular avatar with medical green border for doctors and users.
 * Features:
 * - Async image loading with Coil
 * - Cloudinary integration for optimized images
 * - 300ms crossfade transition
 * - Fallback to initials-based placeholder
 * - Circular shape with 2dp border
 *
 * @param imageUrl Doctor/user photo URL (can be Cloudinary public ID or full URL)
 * @param name Full name for placeholder initials
 * @param size Avatar diameter (default 64dp)
 * @param showBorder Show medical green border (default true)
 * @param onClick Optional click handler
 */
@Composable
fun ProfileAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showBorder: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val finalImageUrl = when {
        imageUrl.isNullOrBlank() -> CloudinaryHelper.generateInitialsAvatar(name, size.value.toInt())
        imageUrl.startsWith("http") -> imageUrl // Full URL
        else -> CloudinaryHelper.getDoctorAvatar(imageUrl) // Cloudinary public ID
    }

    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(
            if (showBorder) {
                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            } else {
                Modifier
            }
        )
        .then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(finalImageUrl)
            .crossfade(300)
            .build(),
        contentDescription = "$name profile picture",
        modifier = avatarModifier,
        contentScale = ContentScale.Crop
    )
}

/**
 * Section Header
 *
 * Header for content sections with optional "View All" action.
 *
 * @param title Section title
 * @param subtitle Optional subtitle text
 * @param actionLabel Action button label (default "View All")
 * @param onActionClick Optional action callback
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String = "View All",
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun WelcomeBanner(
    userName: String,
    modifier: Modifier = Modifier,
    greeting: String = "Welcome Back",
    subtitle: String = "How can we help you today?",
    userPhotoUrl: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ProfileAvatar(
            imageUrl = userPhotoUrl,
            name = userName,
            size = 56.dp,
            showBorder = true
        )
    }
}

/**
 * Medical Hero - Premium Dashboard Section
 * 
 * A high-impact hero section with a gradient background, 
 * user avatar, and a "Daily Health Briefing" card.
 */
@Composable
fun MedicalHero(
    userName: String,
    modifier: Modifier = Modifier,
    userPhotoUrl: String? = null,
    dailyTip: String = "Remember to stay hydrated! 2L of water today."
) {
    HealthMateCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        backgroundColor = Color.Transparent, // We'll use brush for background
        elevation = 6.dp,
        padding = 0.dp // Internal padding handled by Column
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(Spacing.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                ProfileAvatar(
                    imageUrl = userPhotoUrl,
                    name = userName,
                    size = 52.dp,
                    showBorder = true
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Daily Health Briefing Overlay
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = dailyTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Quick Action Item Data Class
 */
data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Quick Action Grid
 *
 * Grid of action buttons for common tasks.
 * Displays 2 columns on phones, adapts to larger screens.
 *
 * @param actions List of quick actions
 */
@Composable
fun QuickActionGrid(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                rowActions.forEach { action ->
                    ActionCard(
                        title = action.label,
                        icon = action.icon,
                        onClick = action.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add spacer if odd number of items in last row
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Action Card - For Quick Actions Grid
 *
 * @param title Action label
 * @param icon Action icon
 * @param onClick Click handler
 */
@Composable
private fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HealthMateCard(
        modifier = modifier,
        onClick = onClick,
        padding = Spacing.lg
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconActionButton(
                icon = icon,
                contentDescription = title,
                onClick = onClick
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Time Slot Chip
 *
 * Chip for selecting appointment time slots.
 *
 * @param time Time label (e.g., "09:00 AM")
 * @param isSelected Whether this slot is selected
 * @param isBooked Whether this slot is already booked
 * @param onClick Click handler
 */
@Composable
fun TimeSlotChip(
    time: String,
    isSelected: Boolean,
    isBooked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isBooked -> MaterialTheme.colorScheme.surfaceVariant
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isBooked -> MaterialTheme.colorScheme.onSurfaceVariant
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    HealthMateCard(
        modifier = modifier,
        onClick = if (!isBooked) onClick else null,
        backgroundColor = backgroundColor,
        padding = Spacing.md,
        elevation = if (isSelected) 4.dp else 1.dp
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

/**
 * Time Slot Chip - Model Overload
 */
@Composable
fun TimeSlotChip(
    slot: com.example.healthmate.model.Slot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    TimeSlotChip(
        time = slot.time,
        isSelected = isSelected,
        isBooked = slot.isBooked,
        onClick = onClick,
        modifier = modifier
    )
}
