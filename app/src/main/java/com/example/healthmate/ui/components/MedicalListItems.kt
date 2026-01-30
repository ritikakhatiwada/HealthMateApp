package com.example.healthmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.healthmate.model.Appointment
import com.example.healthmate.model.Doctor
import com.example.healthmate.model.MedicalRecord
import com.example.healthmate.model.Reminder
import com.example.healthmate.model.WellnessResource
import com.example.healthmate.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Medical List Item Components for HealthMate
 *
 * Specialized list items for medical data display with Apollo hospital-grade styling.
 */

/**
 * Appointment List Item
 *
 * Displays appointment details with doctor info and status.
 *
 * @param appointment Appointment data
 * @param showActions Show action buttons (cancel, reschedule)
 * @param onCancel Cancel appointment callback
 * @param onReschedule Reschedule appointment callback
 * @param onClick Item click callback
 */
@Composable
fun AppointmentListItem(
        appointment: Appointment,
        modifier: Modifier = Modifier,
        showActions: Boolean = true,
        onCancel: (() -> Unit)? = null,
        onReschedule: (() -> Unit)? = null,
        onDelete: (() -> Unit)? = null,
        onClick: (() -> Unit)? = null
) {
    HealthMateCard(modifier = modifier, onClick = onClick, padding = Spacing.lg) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Doctor name
                    Text(
                            text = appointment.doctorName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                    )

                    // Status badge moved to top right
                    StatusBadge(status = appointment.status, size = BadgeSize.SMALL)
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Date and time
                Text(
                        text = "${appointment.date} at ${appointment.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showActions) {
                    val isConfirmed = appointment.status.equals("CONFIRMED", ignoreCase = true)
                    val isDeletable =
                            appointment.status.equals("CANCELLED", ignoreCase = true) || 
                            appointment.status.equals("COMPLETED", ignoreCase = true)

                    if (isConfirmed || (isDeletable && onDelete != null)) {
                        Spacer(modifier = Modifier.height(Spacing.md))

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            if (isConfirmed) {
                                if (onReschedule != null) {
                                    SecondaryButton(
                                            text = "Reschedule",
                                            onClick = onReschedule,
                                            modifier = Modifier.weight(1f)
                                    )
                                }
                                if (onCancel != null) {
                                    DangerButton(
                                            text = "Cancel",
                                            onClick = onCancel,
                                            outlined = true,
                                            modifier = Modifier.weight(1f)
                                    )
                                }
                            } else if (isDeletable && onDelete != null) {
                                DangerButton(
                                        text = "Delete Record",
                                        onClick = onDelete,
                                        outlined = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Doctor List Item
 *
 * Displays doctor profile with photo, name, specialty, and experience. Uses Apollo hospital-style
 * circular avatar.
 *
 * @param doctor Doctor data
 * @param onClick Item click callback
 * @param showSpecialty Show specialty (default true)
 * @param showExperience Show experience (default true)
 */
@Composable
fun DoctorListItem(
        doctor: Doctor,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        showSpecialty: Boolean = true,
        showExperience: Boolean = true
) {
    HealthMateCard(modifier = modifier, onClick = onClick, padding = Spacing.lg) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Doctor photo - Apollo style circular avatar
            ProfileAvatar(
                    imageUrl = doctor.profilePicture,
                    name = doctor.name,
                    size = 64.dp,
                    showBorder = true
            )

            Spacer(modifier = Modifier.width(Spacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                // Doctor name
                Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )

                if (showSpecialty && doctor.specialization.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                            text = doctor.specialization,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }

                if (showExperience && doctor.experience.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                            text = "${doctor.experience} experience",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (doctor.education.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                            text = doctor.education,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Search Slot List Item
 *
 * Used when searching for available slots by date across all doctors.
 */
@Composable
fun SearchSlotListItem(
        slot: com.example.healthmate.model.Slot,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    HealthMateCard(modifier = modifier, onClick = onClick, padding = Spacing.lg) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = slot.doctorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = "Time: ${slot.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                )
            }

            PrimaryButton(text = "Book", onClick = onClick, modifier = Modifier.width(80.dp))
        }
    }
}

/**
 * Medical Record List Item
 *
 * Displays medical record file with type badge and date.
 *
 * @param record Medical record data
 * @param onClick Item click callback
 */
@Composable
fun MedicalRecordListItem(
        record: MedicalRecord,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    HealthMateCard(modifier = modifier, onClick = onClick, padding = Spacing.lg) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = record.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // File type and upload date
                val uploadDate =
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date(record.uploadedAt))

                Text(
                        text = "Uploaded on $uploadDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // File type badge
            val fileExtension = record.fileName.substringAfterLast('.', "")
            if (fileExtension.isNotEmpty()) {
                StatusBadge(status = fileExtension.uppercase(), size = BadgeSize.SMALL)
            }
        }
    }
}

/**
 * Reminder List Item
 *
 * Displays medication reminder with toggle switch.
 *
 * @param reminder Reminder data
 * @param onToggle Active state toggle callback
 * @param onEdit Edit reminder callback
 * @param onDelete Delete reminder callback
 */
@Composable
fun ReminderListItem(
        reminder: Reminder,
        onToggle: (Boolean) -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    HealthMateCard(modifier = modifier, padding = Spacing.lg) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = reminder.medicineName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                        text = reminder.time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit reminder",
                            tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete reminder",
                            tint = MaterialTheme.colorScheme.error
                    )
                }

                Switch(checked = reminder.isActive, onCheckedChange = onToggle)
            }
        }
    }
}

/**
 * Wellness Article Item
 *
 * Displays health article/tip with category.
 *
 * @param article Wellness resource data
 * @param onClick Item click callback
 */
@Composable
fun WellnessArticleItem(
        article: WellnessResource,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    HealthMateCard(modifier = modifier, onClick = onClick, padding = Spacing.lg) {
        Column {
            // Article title
            Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Article content preview
            Text(
                    text = article.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Category badge
            StatusBadge(status = article.type, size = BadgeSize.SMALL)
        }
    }
}
