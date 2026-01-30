package com.example.healthmate.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.ui.theme.StatusError

/**
 * Hospital-Grade Button Components for HealthMate
 *
 * Consistent, accessible buttons following Material 3 guidelines
 * with medical app refinements.
 */

/**
 * Primary Button - Medical Green
 *
 * Use for primary actions: Save, Submit, Book Appointment, etc.
 *
 * @param text Button label
 * @param onClick Action to perform
 * @param modifier Modifier for customization
 * @param enabled Whether button is enabled
 * @param isLoading Show loading spinner instead of text
 * @param icon Optional leading icon
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled && !isLoading,
        shape = HealthMateShapes.ButtonLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(
            horizontal = Spacing.lg
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Secondary Button - Outlined Style
 *
 * Use for secondary actions: Cancel, View Details, etc.
 *
 * @param text Button label
 * @param onClick Action to perform
 * @param modifier Modifier for customization
 * @param enabled Whether button is enabled
 * @param icon Optional leading icon
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = HealthMateShapes.ButtonLarge,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(
            horizontal = Spacing.lg
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Danger Button - For destructive actions
 *
 * Use for: Delete, Cancel Appointment, Remove, etc.
 * Outlined by default for safety (requires user attention).
 *
 * @param text Button label
 * @param onClick Action to perform
 * @param modifier Modifier for customization
 * @param enabled Whether button is enabled
 * @param outlined Use outlined style (default true for safety)
 * @param icon Optional leading icon
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = true,
    icon: ImageVector? = null
) {
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            shape = HealthMateShapes.ButtonLarge,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = StatusError
            ),
            contentPadding = PaddingValues(
                horizontal = Spacing.lg,
                vertical = Spacing.md
            )
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            shape = HealthMateShapes.ButtonLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = StatusError,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(
                horizontal = Spacing.lg,
                vertical = Spacing.md
            )
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Icon Action Button - Circular with background
 *
 * Use for: Quick actions, toolbar actions, etc.
 *
 * @param icon Icon to display
 * @param contentDescription Accessibility description
 * @param onClick Action to perform
 * @param modifier Modifier for customization
 * @param tint Icon tint color
 * @param backgroundColor Button background color
 */
@Composable
fun IconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = backgroundColor,
            contentColor = tint
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

/**
 * HealthMate Floating Action Button
 *
 * Use for: Add new record, Book appointment, Add reminder, etc.
 *
 * @param icon FAB icon
 * @param contentDescription Accessibility description
 * @param onClick Action to perform
 * @param modifier Modifier for customization
 */
@Composable
fun HealthMateFAB(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = HealthMateShapes.CardLarge,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}
