package com.example.healthmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing

/**
 * Dialog & Modal Components for HealthMate
 *
 * Consistent dialogs and bottom sheets for user interactions.
 */

/**
 * Confirmation Dialog
 *
 * Standard confirmation dialog with optional dangerous action styling.
 *
 * @param title Dialog title
 * @param message Confirmation message
 * @param confirmText Confirm button text (default "Confirm")
 * @param dismissText Dismiss button text (default "Cancel")
 * @param isDangerous Use red styling for destructive actions (default false)
 * @param onConfirm Confirm action callback
 * @param onDismiss Dismiss action callback
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDangerous: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            if (isDangerous) {
                DangerButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    outlined = true
                )
            } else {
                PrimaryButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    }
                )
            }
        },
        dismissButton = {
            SecondaryButton(
                text = dismissText,
                onClick = onDismiss
            )
        },
        shape = HealthMateShapes.Dialog,
        modifier = modifier
    )
}

/**
 * Info Dialog
 *
 * Simple informational dialog with optional icon.
 *
 * @param title Dialog title
 * @param message Information message
 * @param icon Optional icon to display
 * @param onDismiss Dismiss action callback
 * @param buttonText Button text (default "OK")
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    buttonText: String = "OK"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            PrimaryButton(
                text = buttonText,
                onClick = onDismiss
            )
        },
        shape = HealthMateShapes.Dialog,
        modifier = modifier
    )
}

/**
 * HealthMate Bottom Sheet
 *
 * Modal bottom sheet for additional options or content.
 * Uses Material 3 ModalBottomSheet.
 *
 * @param title Bottom sheet title
 * @param onDismiss Dismiss action callback
 * @param sheetState Bottom sheet state
 * @param content Sheet content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMateBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = HealthMateShapes.BottomSheet,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Content
            content()

            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

/**
 * Action Sheet Dialog
 *
 * Dialog with multiple action options.
 *
 * @param title Dialog title
 * @param actions List of action options
 * @param onDismiss Dismiss action callback
 */
@Composable
fun ActionSheetDialog(
    title: String,
    actions: List<ActionSheetItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                actions.forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (action.icon != null) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = if (action.isDangerous) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            Spacer(modifier = Modifier.width(Spacing.md))
                        }

                        if (action.isDangerous) {
                            DangerButton(
                                text = action.label,
                                onClick = {
                                    action.onClick()
                                    onDismiss()
                                },
                                outlined = true
                            )
                        } else {
                            SecondaryButton(
                                text = action.label,
                                onClick = {
                                    action.onClick()
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            SecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        shape = HealthMateShapes.Dialog,
        modifier = modifier
    )
}

/**
 * Action Sheet Item Data Class
 */
data class ActionSheetItem(
    val label: String,
    val icon: ImageVector? = null,
    val isDangerous: Boolean = false,
    val onClick: () -> Unit
)
