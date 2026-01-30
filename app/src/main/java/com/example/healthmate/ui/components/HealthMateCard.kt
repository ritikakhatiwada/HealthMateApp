package com.example.healthmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing

/**
 * HealthMate Card Components
 *
 * Flexible card system for medical app UI with variants
 * for different use cases.
 */

/**
 * Standard HealthMate Card
 *
 * Base card component with medical app styling.
 * - White Background (Surface)
 * - Rounded Corners (16dp)
 * - Soft Elevation (2-4dp)
 * - Clean spacing
 *
 * @param modifier Modifier for customization
 * @param onClick Optional click handler
 * @param padding Internal padding (default 16dp)
 * @param elevation Card elevation (default 2dp)
 * @param backgroundColor Card background color
 * @param content Card content
 */
@Composable
fun HealthMateCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 16.dp,
    elevation: Dp = 2.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shape: androidx.compose.ui.graphics.Shape = HealthMateShapes.CardLarge,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation,
                pressedElevation = elevation + 2.dp,
                hoveredElevation = elevation + 1.dp
            )
        ) {
            Column(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    }
}

/**
 * Info Card
 *
 * Card for displaying information with optional icon.
 *
 * @param title Card title
 * @param subtitle Optional subtitle text
 * @param icon Optional leading icon
 * @param iconTint Icon tint color
 * @param onClick Optional click handler
 */
@Composable
fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    HealthMateCard(
        modifier = modifier,
        onClick = onClick,
        padding = Spacing.lg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.lg))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Stat Card
 *
 * Card for displaying statistics/KPIs (Admin dashboard).
 *
 * @param label Stat label
 * @param value Stat value
 * @param icon Stat icon
 * @param color Theme color for icon
 * @param trend Optional trend indicator (e.g., "+12%")
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trend: String? = null
) {
    HealthMateCard(
        modifier = modifier,
        padding = Spacing.lg,
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (trend != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Action Card
 *
 * Clickable card with icon, title, and description.
 *
 * @param title Card title
 * @param description Card description
 * @param icon Card icon
 * @param backgroundColor Card background color
 * @param onClick Click handler
 */
@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    HealthMateCard(
        modifier = modifier,
        onClick = onClick,
        padding = Spacing.lg,
        backgroundColor = backgroundColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * List Item Card
 *
 * Generic list item with leading/trailing content.
 *
 * @param leading Leading content (typically icon or avatar)
 * @param title Item title
 * @param subtitle Optional subtitle
 * @param trailing Optional trailing content (typically action or status)
 * @param onClick Optional click handler
 */
@Composable
fun ListItemCard(
    leading: @Composable () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    HealthMateCard(
        modifier = modifier,
        onClick = onClick,
        padding = Spacing.lg
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leading()

                Spacer(modifier = Modifier.width(Spacing.lg))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
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
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(Spacing.md))
                Row(content = trailing)
            }
        }
    }
}
