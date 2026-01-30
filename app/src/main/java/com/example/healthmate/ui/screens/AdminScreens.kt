package com.example.healthmate.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.healthmate.ChangePasswordActivity
import com.example.healthmate.ProfileActivity
import com.example.healthmate.admin.AdminEmergencyActivity
import com.example.healthmate.admin.AdminUsersActivity
import com.example.healthmate.admin.AdminWellnessActivity
import com.example.healthmate.admin.DoctorDetailActivity
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.*
import com.example.healthmate.presentation.admin.DayAppointmentCount
import com.example.healthmate.presentation.admin.WeeklyAppointmentChart
import com.example.healthmate.presentation.admin.UtilizationBar
import com.example.healthmate.ui.components.HealthMateCard
import com.example.healthmate.ui.components.StatusChip
import com.example.healthmate.ui.theme.Spacing
import java.text.SimpleDateFormat
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
        var weeklyData by remember { mutableStateOf<List<DayAppointmentCount>>(emptyList()) }
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

                        // Calculate weekly appointment data for chart
                        val allAppointments = FirestoreHelper.getAllAppointments()
                        weeklyData = calculateWeeklyAppointments(allAppointments)

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
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.xl)
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

                // 3. Weekly Appointment Chart
                item {
                        HealthMateCard {
                                WeeklyAppointmentChart(
                                        data = weeklyData,
                                        modifier = Modifier.padding(16.dp)
                                )
                        }
                }

                // 4. Utilization Overview
                item {
                        HealthMateCard {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                                text = "System Utilization",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        val totalCapacity = doctorsCount * 8 // Assuming 8 slots per doctor
                                        val utilizationRate = if (totalCapacity > 0) {
                                                todaysAppointmentsCount.toFloat() / totalCapacity
                                        } else 0f

                                        UtilizationBar(
                                                label = "Today's Slot Utilization",
                                                progress = utilizationRate,
                                                value = "${(utilizationRate * 100).toInt()}%",
                                                color = when {
                                                        utilizationRate < 0.3f -> Color(0xFFEF4444) // Red - Low
                                                        utilizationRate < 0.7f -> Color(0xFFF59E0B) // Amber - Medium
                                                        else -> Color(0xFF22C55E) // Green - High
                                                }
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        UtilizationBar(
                                                label = "Active Doctors",
                                                progress = if (doctorsCount > 0) 1f else 0f,
                                                value = "$doctorsCount active",
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }
                        }
                }

                // 5. Quick Actions
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
        icon: ImageVector??,
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
                                if (icon != null) {
                                        Icon(
                                                icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        icon = Icons.Default.Spa,
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
        icon: ImageVector?,
        color: Color = MaterialTheme.colorScheme.primary,
        onClick: () -> Unit
) {
        HealthMateCard(
            modifier = modifier.height(100.dp),
            onClick = onClick
        ) {
                Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                ) {
                        if (icon != null) {
                                Box(
                                        modifier =
                                                Modifier.size(44.dp)
                                                        .background(
                                                                color.copy(alpha = 0.1f),
                                                                CircleShape
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) { Icon(icon, contentDescription = null, tint = color) }
                                Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
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
                        // Profile Picture / Avatar
                        Box(
                                modifier =
                                        Modifier.size(64.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        CircleShape
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                                if (doctor.profilePicture.isNotEmpty()) {
                                        AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                        .data(doctor.profilePicture)
                                                        .crossfade(true)
                                                        .build(),
                                                contentDescription = "Doctor",
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                        )
                                } else {
                                        Text(
                                                text = doctor.name.take(2).uppercase(),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
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
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }

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

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val upcomingAppointments = appointments.filter { 
            it.status.equals("CONFIRMED", ignoreCase = true) && it.date >= today 
        }
        val completedAppointments = appointments.filter { 
            it.status.equals("COMPLETED", ignoreCase = true) || (it.status.equals("CONFIRMED", ignoreCase = true) && it.date < today) 
        }
        val cancelledAppointments = appointments.filter {
            it.status.equals("CANCELLED", ignoreCase = true)
        }

        Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                ) {
                        Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = { Text("Upcoming (${upcomingAppointments.size})") }
                        )
                        Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = { Text("Completed (${completedAppointments.size})") }
                        )
                        Tab(
                                selected = selectedTabIndex == 2,
                                onClick = { selectedTabIndex = 2 },
                                text = { Text("Cancelled (${cancelledAppointments.size})") }
                        )
                }

                val displayAppointments = when (selectedTabIndex) {
                    0 -> upcomingAppointments
                    1 -> completedAppointments
                    2 -> cancelledAppointments
                    else -> emptyList()
                }

                if (displayAppointments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                        "No appointments found",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                } else {
                        LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                items(displayAppointments) { appointment ->
                                        AdminAppointmentDetailCard(
                                                appointment = appointment,
                                                onClick = { selectedAppointment = appointment }
                                        )
                                }
                        }
                }
        }

        if (selectedAppointment != null) {
                AdminAppointmentDetailDialog(
                        appointment = selectedAppointment!!,
                        onDismiss = { selectedAppointment = null }
                )
        }
}

@Composable
fun AdminAppointmentDetailCard(appointment: Appointment, onClick: () -> Unit) {
        HealthMateCard(onClick = onClick) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
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

                        Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                "Doctor",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                                appointment.doctorName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                "Patient",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                                appointment.patientName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        }
                }
        }
}

@Composable
fun AdminAppointmentDetailDialog(appointment: Appointment, onDismiss: () -> Unit) {
        val scope = rememberCoroutineScope()
        var patient by remember { mutableStateOf<User?>(null) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(appointment.patientId) {
                scope.launch {
                        patient = FirestoreHelper.getUserById(appointment.patientId)
                        isLoading = false
                }
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Appointment Details") },
                text = {
                        if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                }
                        } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        DetailItem(label = "Patient Name", value = appointment.patientName)
                                        patient?.let {
                                                DetailItem(label = "Phone", value = it.phoneNumber.ifEmpty { "N/A" })
                                                DetailItem(label = "Age/Gender", value = "${it.age} yrs / ${it.gender}")
                                                DetailItem(label = "Blood Group", value = it.bloodGroup.ifEmpty { "N/A" })
                                                DetailItem(label = "Address", value = it.address.ifEmpty { "N/A" })
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        DetailItem(label = "Doctor", value = appointment.doctorName)
                                        DetailItem(label = "Date", value = appointment.date)
                                        DetailItem(label = "Time", value = appointment.time)
                                        DetailItem(label = "Status", value = appointment.status)
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = onDismiss) { Text("Close") }
                }
        )
}

@Composable
fun DetailItem(label: String, value: String) {
        Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                        "$label: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(100.dp)
                )
                Text(
                        value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        Column(modifier = Modifier.padding(Spacing.lg)) {
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
                                        icon = Icons.AutoMirrored.Filled.Logout,
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
        icon: ImageVector?,
        title: String,
        textColor: Color = MaterialTheme.colorScheme.onSurface,
        onClick: () -> Unit
) {
        HealthMateCard(onClick = onClick, modifier = Modifier.padding(bottom = 8.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        if (icon != null) {
                                Icon(icon, contentDescription = null, tint = textColor)
                                Spacer(modifier = Modifier.width(16.dp))
                        }
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

// ==================== HELPER FUNCTIONS ====================

/**
 * Calculate weekly appointment data for the chart.
 * Returns appointment counts for the last 7 days.
 */
private fun calculateWeeklyAppointments(appointments: List<Appointment>): List<DayAppointmentCount> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val result = mutableListOf<DayAppointmentCount>()

        // Go back 6 days to get last 7 days including today
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        repeat(7) {
                val dateStr = dateFormat.format(calendar.time)
                val dayLabel = dayLabelFormat.format(calendar.time)

                // Count appointments for this date
                val count = appointments.count { appointment ->
                        appointment.date == dateStr
                }

                result.add(DayAppointmentCount(dayLabel = dayLabel, date = dateStr, count = count))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return result
}
