package com.example.healthmate.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.healthmate.ChangePasswordActivity
import com.example.healthmate.ChatbotBody
import com.example.healthmate.ProfileActivity
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.emergency.EmergencySOSActivity
import com.example.healthmate.model.*
import com.example.healthmate.records.MedicalRecordsActivity
import com.example.healthmate.reminders.MedicationRemindersActivity
import kotlinx.coroutines.launch

// ==================== USER CHAT SCREEN ====================
@Composable
fun UserChatScreen() {
        ChatbotBody()
}

// ==================== USER HOME SCREEN ====================
@Composable
fun UserHomeScreen(
        onNavigateToAppointments: () -> Unit,
        onNavigateToRecords: () -> Unit,
        onNavigateToWellness: () -> Unit
) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var userName by remember { mutableStateOf("") }
        var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
        var records by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
        var articles by remember { mutableStateOf<List<WellnessResource>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
                scope.launch {
                        android.util.Log.d("UserHomeScreen", "========================================")
                        android.util.Log.d("UserHomeScreen", "Loading User Dashboard")
                        android.util.Log.d("UserHomeScreen", "========================================")

                        // Auto-update appointment statuses first
                        android.util.Log.d("UserHomeScreen", "Running auto-update appointment statuses...")
                        FirestoreHelper.autoUpdateAppointmentStatuses()

                        val userId = FirebaseAuthHelper.getCurrentUserId()
                        android.util.Log.d("UserHomeScreen", "Current User ID: $userId")

                        val user = FirestoreHelper.getUserById(userId)
                        userName = user?.name ?: ""
                        android.util.Log.d("UserHomeScreen", "User Name: $userName")

                        // Get all appointments and filter for upcoming only
                        val allAppointments = FirestoreHelper.getUserAppointments(userId)
                        android.util.Log.d("UserHomeScreen", "Total appointments fetched: ${allAppointments.size}")

                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                .format(java.util.Date())
                        android.util.Log.d("UserHomeScreen", "Today's date: $today")

                        // Log each appointment
                        allAppointments.forEachIndexed { index, app ->
                                android.util.Log.d("UserHomeScreen", "Appointment #$index:")
                                android.util.Log.d("UserHomeScreen", "  - ID: ${app.id}")
                                android.util.Log.d("UserHomeScreen", "  - Doctor: ${app.doctorName}")
                                android.util.Log.d("UserHomeScreen", "  - Date: ${app.date}")
                                android.util.Log.d("UserHomeScreen", "  - Time: ${app.time}")
                                android.util.Log.d("UserHomeScreen", "  - Status: ${app.status}")
                                android.util.Log.d("UserHomeScreen", "  - Date >= Today: ${app.date >= today}")
                                android.util.Log.d("UserHomeScreen", "  - Is CONFIRMED: ${app.status.equals("CONFIRMED", ignoreCase = true)}")
                        }

                        appointments = allAppointments.filter { appointment ->
                                val isConfirmed = appointment.status.equals("CONFIRMED", ignoreCase = true)
                                val isFutureOrToday = appointment.date >= today
                                val shouldShow = isConfirmed && isFutureOrToday

                                android.util.Log.d("UserHomeScreen", "Filtering appointment with ${appointment.doctorName}: isConfirmed=$isConfirmed, isFutureOrToday=$isFutureOrToday, shouldShow=$shouldShow")

                                shouldShow
                        }.take(3)

                        android.util.Log.d("UserHomeScreen", "Filtered upcoming appointments: ${appointments.size}")
                        appointments.forEach { app ->
                                android.util.Log.d("UserHomeScreen", "  - Showing: ${app.doctorName} on ${app.date}")
                        }

                        records = FirestoreHelper.getUserMedicalRecords(userId).take(3)
                        articles = FirestoreHelper.getWellnessResources().take(3)
                        isLoading = false

                        android.util.Log.d("UserHomeScreen", "========================================")
                        android.util.Log.d("UserHomeScreen", "Dashboard Loading Complete")
                        android.util.Log.d("UserHomeScreen", "========================================")
                }
        }

        if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return
        }

        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
                // Welcome Banner
                item { WelcomeBanner(userName = userName) }

                // Quick Actions
                item {
                        QuickActionsRow(
                                onMedicalRecords = {
                                        context.startActivity(
                                                Intent(context, MedicalRecordsActivity::class.java)
                                        )
                                },
                                onEmergency = {
                                        context.startActivity(
                                                Intent(context, EmergencySOSActivity::class.java)
                                        )
                                },
                                onReminders = {
                                        context.startActivity(
                                                Intent(
                                                        context,
                                                        MedicationRemindersActivity::class.java
                                                )
                                        )
                                }
                        )
                }

                // Upcoming Appointments Section
                item {
                        SectionHeader(
                                title = "Upcoming Appointments",
                                onViewAll = onNavigateToAppointments
                        )
                }

                if (appointments.isEmpty()) {
                        item {
                                EmptyStateCard(
                                        icon = Icons.Default.CalendarMonth,
                                        message = "No upcoming appointments"
                                )
                        }
                } else {
                        items(appointments) { appointment ->
                                AppointmentCard(
                                        appointment = appointment,
                                        onCancelled = {
                                                // Refresh appointments list
                                                scope.launch {
                                                        val userId = FirebaseAuthHelper.getCurrentUserId()
                                                        val allAppointments = FirestoreHelper.getUserAppointments(userId)
                                                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                                                .format(java.util.Date())

                                                        appointments = allAppointments.filter { app ->
                                                                app.status.equals("CONFIRMED", ignoreCase = true) &&
                                                                app.date >= today
                                                        }.take(3)
                                                }
                                        }
                                )
                        }
                }

                // Medical Records Section
                item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                                title = "Medical Records",
                                onViewAll = {
                                        context.startActivity(
                                                Intent(context, MedicalRecordsActivity::class.java)
                                        )
                                }
                        )
                }

                if (records.isEmpty()) {
                        item {
                                EmptyStateCard(
                                        icon = Icons.Default.FolderOpen,
                                        message = "No medical records yet"
                                )
                        }
                } else {
                        items(records) { record -> MedicalRecordCard(record = record) }
                }

                // Wellness Articles Section
                item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(title = "Health Tips", onViewAll = onNavigateToWellness)
                }

                if (articles.isEmpty()) {
                        item {
                                EmptyStateCard(
                                        icon = Icons.Default.Article,
                                        message = "No articles available"
                                )
                        }
                } else {
                        items(articles) { article -> ArticleCard(article = article) }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
        }
}

@Composable
fun WelcomeBanner(userName: String) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                brush =
                                                        Brush.horizontalGradient(
                                                                colors =
                                                                        listOf(
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary,
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .tertiary
                                                                        )
                                                        ),
                                                shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(24.dp)
                ) {
                        Column {
                                Text(
                                        text =
                                                "👋 Welcome Back${if (userName.isNotBlank()) ", $userName" else ""}!",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        text = "How can we help you today?",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.9f)
                                )
                        }
                }
        }
}

@Composable
fun QuickActionsRow(
        onMedicalRecords: () -> Unit,
        onEmergency: () -> Unit,
        onReminders: () -> Unit
) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickActionItem(
                        icon = Icons.Default.FolderOpen,
                        label = "Records",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onMedicalRecords
                )
                QuickActionItem(
                        icon = Icons.Default.Emergency,
                        label = "Emergency",
                        color = Color(0xFFE53935),
                        onClick = onEmergency
                )
                QuickActionItem(
                        icon = Icons.Default.Alarm,
                        label = "Reminders",
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = onReminders
                )
        }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                        Modifier.clip(RoundedCornerShape(16.dp))
                                .clickable(onClick = onClick)
                                .padding(12.dp)
        ) {
                Box(
                        modifier =
                                Modifier.size(56.dp)
                                        .background(color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = color,
                                modifier = Modifier.size(28.dp)
                        )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                )
        }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewAll) {
                        Text("View All")
                        Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                        )
                }
        }
}

@Composable
fun EmptyStateCard(icon: ImageVector, message: String) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}

@Composable
fun AppointmentCard(appointment: Appointment, onCancelled: () -> Unit = {}) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        var showCancelDialog by remember { mutableStateOf(false) }

        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(48.dp)
                                                        .background(
                                                                MaterialTheme.colorScheme.primaryContainer,
                                                                RoundedCornerShape(12.dp)
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                        )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = appointment.doctorName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                                text = "${appointment.date} • ${appointment.time}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                                StatusChip(status = appointment.status)
                        }

                        // Show cancel button only for CONFIRMED appointments
                        if (appointment.status.equals("CONFIRMED", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                        onClick = { showCancelDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFFE53935)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                ) {
                                        Icon(
                                                Icons.Default.Cancel,
                                                contentDescription = "Cancel",
                                                modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cancel", style = MaterialTheme.typography.labelMedium)
                                }
                        }
                }
        }

        if (showCancelDialog) {
                AlertDialog(
                        onDismissRequest = { showCancelDialog = false },
                        title = { Text("Cancel Appointment") },
                        text = {
                                Text("Are you sure you want to cancel this appointment with ${appointment.doctorName}?")
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                scope.launch {
                                                        val result = FirestoreHelper.cancelAppointment(
                                                                appointment.id,
                                                                appointment.slotId
                                                        )
                                                        result.fold(
                                                                onSuccess = {
                                                                        Toast.makeText(
                                                                                context,
                                                                                "Appointment cancelled",
                                                                                Toast.LENGTH_SHORT
                                                                        ).show()
                                                                        showCancelDialog = false
                                                                        onCancelled()
                                                                },
                                                                onFailure = { e ->
                                                                        Toast.makeText(
                                                                                context,
                                                                                "Error: ${e.message}",
                                                                                Toast.LENGTH_SHORT
                                                                        ).show()
                                                                }
                                                        )
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFE53935)
                                        )
                                ) {
                                        Text("Yes, Cancel")
                                }
                        },
                        dismissButton = {
                                TextButton(onClick = { showCancelDialog = false }) {
                                        Text("Keep")
                                }
                        }
                )
        }
}

@Composable
fun StatusChip(status: String) {
        val (bgColor, textColor) =
                when (status.uppercase()) {
                        "CONFIRMED" ->
                                MaterialTheme.colorScheme.primaryContainer to
                                        MaterialTheme.colorScheme.primary
                        "COMPLETED" -> Color(0xFFE8F5E9) to Color(0xFF4CAF50)
                        "PENDING" -> Color(0xFFFFF3E0) to Color(0xFFFF9800)
                        "CANCELLED" -> Color(0xFFFFEBEE) to Color(0xFFE53935)
                        else ->
                                MaterialTheme.colorScheme.surfaceVariant to
                                        MaterialTheme.colorScheme.onSurfaceVariant
                }

        Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
                Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
        }
}

@Composable
fun MedicalRecordCard(record: MedicalRecord) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
                                                .background(
                                                        Color(0xFFFFEBEE),
                                                        RoundedCornerShape(12.dp)
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                                Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFFE53935)
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = record.fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                        text = "PDF Document",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "View",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                        )
                }
        }
}

@Composable
fun ArticleCard(article: WellnessResource) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.tertiaryContainer,
                                                        RoundedCornerShape(12.dp)
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Article,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = article.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                        text =
                                                article.content.take(50) +
                                                        if (article.content.length > 50) "..."
                                                        else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                )
                        }
                }
        }
}

// ==================== USER REMINDERS SCREEN ====================
@Composable
fun UserRemindersScreen() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
                scope.launch {
                        val userId = FirebaseAuthHelper.getCurrentUserId()
                        reminders = FirestoreHelper.getUserReminders(userId)
                        isLoading = false
                }
        }

        Scaffold(
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = {
                                        context.startActivity(
                                                Intent(
                                                        context,
                                                        MedicationRemindersActivity::class.java
                                                )
                                        )
                                },
                                containerColor = MaterialTheme.colorScheme.primary
                        ) { Icon(Icons.Default.Add, "Add Reminder", tint = Color.White) }
                }
        ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when {
                                isLoading -> {
                                        CircularProgressIndicator(
                                                modifier = Modifier.align(Alignment.Center),
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }
                                reminders.isEmpty() -> {
                                        Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.Alarm,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(80.dp),
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant.copy(
                                                                        alpha = 0.5f
                                                                )
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                        text = "No reminders set",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                        text = "Tap + to add a medication reminder",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                        }
                                }
                                else -> {
                                        LazyColumn(
                                                modifier = Modifier.fillMaxSize(),
                                                contentPadding = PaddingValues(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                items(reminders) { reminder ->
                                                        ReminderCard(reminder = reminder)
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun ReminderCard(reminder: Reminder) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        RoundedCornerShape(12.dp)
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = reminder.medicineName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                        text = "Daily at ${reminder.time}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        Switch(
                                checked = reminder.isActive,
                                onCheckedChange = { /* Toggle reminder */},
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor =
                                                        MaterialTheme.colorScheme.primary,
                                                checkedTrackColor =
                                                        MaterialTheme.colorScheme.primaryContainer
                                        )
                        )
                }
        }
}

// ==================== USER SETTINGS SCREEN ====================
@Composable
fun UserSettingsScreen(onLogout: () -> Unit) {
        val context = LocalContext.current

        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                item {
                        Text(
                                text = "Account",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                        )
                }

                item {
                        SettingsItem(
                                icon = Icons.Default.Person,
                                title = "Profile",
                                subtitle = "View and edit your profile",
                                onClick = {
                                        context.startActivity(
                                                Intent(context, ProfileActivity::class.java)
                                        )
                                }
                        )
                }

                item {
                        SettingsItem(
                                icon = Icons.Default.Lock,
                                title = "Change Password",
                                subtitle = "Update your password",
                                onClick = {
                                        context.startActivity(
                                                Intent(context, ChangePasswordActivity::class.java)
                                        )
                                }
                        )
                }

                item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                text = "Health",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                        )
                }

                item {
                        SettingsItem(
                                icon = Icons.Default.FolderOpen,
                                title = "Medical Records",
                                subtitle = "View and upload medical documents",
                                onClick = {
                                        context.startActivity(
                                                Intent(context, MedicalRecordsActivity::class.java)
                                        )
                                }
                        )
                }

                item {
                        SettingsItem(
                                icon = Icons.Default.Emergency,
                                title = "Emergency SOS",
                                subtitle = "Quick access to emergency contacts",
                                onClick = {
                                        context.startActivity(
                                                Intent(context, EmergencySOSActivity::class.java)
                                        )
                                }
                        )
                }

                item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                shape = RoundedCornerShape(12.dp)
                        ) {
                                Icon(Icons.Default.Logout, "Logout")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Logout", modifier = Modifier.padding(vertical = 8.dp))
                        }
                }
        }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
        Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Box(
                                modifier =
                                        Modifier.size(44.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        RoundedCornerShape(12.dp)
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                                Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                )
                                Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}
