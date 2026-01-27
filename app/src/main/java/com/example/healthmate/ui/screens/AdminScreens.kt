package com.example.healthmate.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthmate.ChangePasswordActivity
import com.example.healthmate.ProfileActivity
import com.example.healthmate.admin.AdminEmergencyActivity
import com.example.healthmate.admin.AdminUsersActivity
import com.example.healthmate.admin.AdminWellnessActivity
import com.example.healthmate.admin.DoctorDetailActivity
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.*
import com.example.healthmate.ui.components.HealthMateCard
import java.util.*
import kotlinx.coroutines.launch

// ==================== ADMIN HOME SCREEN ====================
@Composable
fun AdminHomeScreen() {
        val scope = rememberCoroutineScope()

        var userName by remember { mutableStateOf("") }
        var usersCount by remember { mutableStateOf(0) }
        var doctorsCount by remember { mutableStateOf(0) }
        var todaysAppointmentsCount by remember { mutableStateOf(0) }
        var todaysAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
                scope.launch {
                        val userId = FirebaseAuthHelper.getCurrentUserId()
                        val user = FirestoreHelper.getUserById(userId)
                        userName = user?.name ?: "Admin"
                        usersCount = FirestoreHelper.getUsersCount()
                        doctorsCount = FirestoreHelper.getDoctorsCount()
                        todaysAppointmentsCount = FirestoreHelper.getTodaysAppointmentsCount()
                        todaysAppointments = FirestoreHelper.getTodaysAppointments().take(5)
                        isLoading = false
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
                // 1. Welcome Header
                item { AdminWelcomeBanner(userName) }

                // 2. Stats Grid (KPI Cards)
                item {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                KPICard(
                                        modifier = Modifier.weight(1f),
                                        label = "Users",
                                        value = usersCount.toString(),
                                        icon = Icons.Default.People,
                                        color = MaterialTheme.colorScheme.primary
                                )
                                KPICard(
                                        modifier = Modifier.weight(1f),
                                        label = "Doctors",
                                        value = doctorsCount.toString(),
                                        icon = Icons.Default.MedicalServices,
                                        color = MaterialTheme.colorScheme.tertiary
                                )
                                KPICard(
                                        modifier = Modifier.weight(1f),
                                        label = "Today",
                                        value = todaysAppointmentsCount.toString(),
                                        icon = Icons.Default.CalendarToday,
                                        color = MaterialTheme.colorScheme.secondary
                                )
                        }
                }

                // 3. Quick Actions
                item {
                        Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                        )
                        AdminQuickActions()
                }

                // 4. Today's Overview
                item {
                        Text(
                                text = "Today's Appointments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                        )
                        if (todaysAppointments.isEmpty()) {
                                HealthMateCard {
                                        Row(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                        ) {
                                                Text(
                                                        "No appointments today",
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                }
                        }
                }
                items(todaysAppointments) { appointment -> AdminAppointmentItem(appointment) }
        }
}

@Composable
fun AdminWelcomeBanner(userName: String) {
        HealthMateCard(backgroundColor = MaterialTheme.colorScheme.primaryContainer) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Column {
                                Text(
                                        text = "Welcome Back,",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                        text = userName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                        }
                        Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(40.dp)
                        )
                }
        }
}

@Composable
fun KPICard(
        modifier: Modifier = Modifier,
        label: String,
        value: String,
        icon: ImageVector,
        color: Color
) {
        HealthMateCard(modifier = modifier, elevation = 2.dp) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                        Text(
                                text = value,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
        }
}

@Composable
fun AdminQuickActions() {
        val context = LocalContext.current
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                        modifier = Modifier.weight(1f),
                        label = "Users",
                        icon = Icons.Default.ManageAccounts,
                        onClick = {
                                context.startActivity(
                                        Intent(context, AdminUsersActivity::class.java)
                                )
                        }
                )
                QuickActionCard(
                        modifier = Modifier.weight(1f),
                        label = "Emergency",
                        icon = Icons.Default.Emergency,
                        color = Color(0xFFEF4444),
                        onClick = {
                                context.startActivity(
                                        Intent(context, AdminEmergencyActivity::class.java)
                                )
                        }
                )
                QuickActionCard(
                        modifier = Modifier.weight(1f),
                        label = "Wellness",
                        icon = Icons.Default.Article,
                        onClick = {
                                context.startActivity(
                                        Intent(context, AdminWellnessActivity::class.java)
                                )
                        }
                )
        }
}

@Composable
fun QuickActionCard(
        modifier: Modifier = Modifier,
        label: String,
        icon: ImageVector,
        color: Color = MaterialTheme.colorScheme.primary,
        onClick: () -> Unit
) {
        HealthMateCard(modifier = modifier, onClick = onClick) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Box(
                                modifier =
                                        Modifier.size(40.dp)
                                                .background(color.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                        ) { Icon(icon, contentDescription = null, tint = color) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                        )
                }
        }
}

@Composable
fun AdminAppointmentItem(appointment: Appointment) {
        HealthMateCard(modifier = Modifier.padding(bottom = 8.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier =
                                        Modifier.background(
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                        RoundedCornerShape(8.dp)
                                                )
                                                .padding(8.dp)
                        ) {
                                Text(
                                        text = appointment.time,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = appointment.doctorName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                )
                                Text(
                                        text = appointment.patientName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        StatusChip(appointment.status)
                }
        }
}

// ==================== ADMIN DOCTORS SCREEN ====================
@Composable
fun AdminDoctorsScreen(onAddDoctor: () -> Unit) {
        val scope = rememberCoroutineScope()
        var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
                scope.launch {
                        doctors = FirestoreHelper.getDoctors()
                        isLoading = false
                }
        }

        Scaffold(
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = onAddDoctor,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                        ) { Icon(Icons.Default.Add, "Add Doctor") }
                }
        ) { padding ->
                if (isLoading) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                } else {
                        LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) { items(doctors) { doctor -> NewDoctorCard(doctor) } }
                }
        }
}

@Composable
fun NewDoctorCard(doctor: Doctor) {
        val context = LocalContext.current
        HealthMateCard(
                onClick = {
                        val intent = Intent(context, DoctorDetailActivity::class.java)
                        intent.putExtra("doctorId", doctor.id)
                        context.startActivity(intent)
                }
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Replaced Image with Avatar logic
                        Box(
                                modifier =
                                        Modifier.size(64.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        CircleShape
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                                Text(
                                        text = doctor.name.take(2).uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = doctor.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                )
                                Text(
                                        text = doctor.specialization,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                        text = "${doctor.experience} yrs exp",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        Icon(
                                Icons.Default.ChevronRight,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}

// ==================== ADMIN APPOINTMENTS SCREEN ====================
@Composable
fun AdminAppointmentsScreen() {
        val scope = rememberCoroutineScope()
        var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
                scope.launch {
                        appointments = FirestoreHelper.getAllAppointments()
                        isLoading = false
                }
        }

        if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                }
                return
        }

        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) { items(appointments) { appointment -> AdminAppointmentDetailCard(appointment) } }
}

@Composable
fun AdminAppointmentDetailCard(appointment: Appointment) {
        HealthMateCard {
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                                Icons.Default.Event,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                                text = "${appointment.date} at ${appointment.time}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                        )
                                }
                                StatusChip(status = appointment.status)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                                "Doctor: ${appointment.doctorName}",
                                style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                                "Patient: ${appointment.patientName}",
                                style = MaterialTheme.typography.bodyMedium
                        )
                }
        }
}

// ==================== ADMIN SETTINGS SCREEN ====================
@Composable
fun AdminSettingsScreen(onLogout: () -> Unit) {
        val context = LocalContext.current
        LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(80.dp)
                                                        .background(
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.1f),
                                                                CircleShape
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                Icons.Default.AdminPanelSettings,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(40.dp)
                                        )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                        "Admin Control",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                )
                        }
                }

                item {
                        Column(modifier = Modifier.padding(16.dp)) {
                                SettingsCategory("Account")
                                SettingsItem(
                                        icon = Icons.Default.Person,
                                        title = "Profile",
                                        onClick = {
                                                context.startActivity(
                                                        Intent(context, ProfileActivity::class.java)
                                                )
                                        }
                                )
                                SettingsItem(
                                        icon = Icons.Default.Lock,
                                        title = "Change Password",
                                        onClick = {
                                                context.startActivity(
                                                        Intent(
                                                                context,
                                                                ChangePasswordActivity::class.java
                                                        )
                                                )
                                        }
                                )

                                SettingsCategory("App")
                                SettingsItem(
                                        icon = Icons.Default.Logout,
                                        title = "Logout",
                                        textColor = MaterialTheme.colorScheme.error,
                                        onClick = onLogout
                                )
                        }
                }
        }
}

// Reusing Components
@Composable
fun SettingsCategory(title: String) {
        Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
        )
}

@Composable
fun SettingsItem(
        icon: ImageVector,
        title: String,
        textColor: Color = MaterialTheme.colorScheme.onSurface,
        onClick: () -> Unit
) {
        HealthMateCard(onClick = onClick, modifier = Modifier.padding(bottom = 8.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(icon, contentDescription = null, tint = textColor)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}
