package com.example.healthmate.ui.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.healthmate.ChangePasswordActivity
import com.example.healthmate.ChatbotBody
import com.example.healthmate.ProfileActivity
import com.example.healthmate.R
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.emergency.EmergencySOSActivity
import com.example.healthmate.hospitals.HospitalLocatorActivity
import com.example.healthmate.model.Appointment
import com.example.healthmate.model.Reminder
import com.example.healthmate.records.MedicalRecordsActivity
import com.example.healthmate.reminders.MedicationRemindersActivity
import com.example.healthmate.ui.components.AddReminderDialog
import com.example.healthmate.ui.components.BadgeSize
import com.example.healthmate.ui.components.DashboardSkeleton
import com.example.healthmate.ui.components.ReminderCard
import com.example.healthmate.ui.components.StatusBadge
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.LocationHelper
import com.example.healthmate.util.ReminderUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ==================== LOCATION HEADER (Flipkart/Amazon Style) ====================
@Composable
fun LocationHeader(location: String, onLocationClick: () -> Unit = {}) {
        Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
        ) {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clickable(onClick = onLocationClick)
                                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = "Current Location",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                        text = location,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                        }
                        Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand location",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                        )
                }
        }
}

// ==================== USER CHAT SCREEN ====================
@Composable
fun UserChatScreen() {
        ChatbotBody()
}

// ==================== REDESIGNED USER HOME SCREEN ====================
@Composable
fun UserHomeScreen(
        onNavigateToAppointments: () -> Unit,
        onNavigateToRecords: () -> Unit,
        onNavigateToReminders: () -> Unit,
        onNavigateToWellness: () -> Unit,
        refreshKey: Int = 0 // Add refresh trigger
) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var userName by remember { mutableStateOf("") }
        var userProfilePicture by remember { mutableStateOf<String?>(null) }
        var userLocation by remember { mutableStateOf("Detecting location...") }
        var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
        var appointmentCount by remember { mutableIntStateOf(0) }
        var recordCount by remember { mutableIntStateOf(0) }
        var reminderCount by remember { mutableIntStateOf(0) }
        var isLoading by remember { mutableStateOf(true) }
        var animationTrigger by remember { mutableStateOf(false) }

        // Health tips rotation
        val healthTips =
                listOf(
                        "Stay hydrated! Drink at least 8 glasses of water today.",
                        "Take a 10-minute walk to boost your energy levels.",
                        "Remember to take deep breaths to reduce stress.",
                        "Get 7-8 hours of sleep for optimal health.",
                        "Eat more fruits and vegetables for better immunity."
                )
        var currentTipIndex by remember { mutableIntStateOf(0) }

        // Rotate health tips
        LaunchedEffect(Unit) {
                while (true) {
                        delay(8000)
                        currentTipIndex = (currentTipIndex + 1) % healthTips.size
                }
        }

        LaunchedEffect(refreshKey) { // Reload data when refreshKey changes
                scope.launch {
                        // Auto-update appointment statuses
                        FirestoreHelper.autoUpdateAppointmentStatuses()

                        val userId = FirebaseAuthHelper.getCurrentUserId()
                        val user = FirestoreHelper.getUserById(userId)
                        userName = user?.name ?: ""
                        userProfilePicture = user?.profilePicture

                        // Get user location
                        val locationHelper = LocationHelper(context)
                        if (locationHelper.hasLocationPermission()) {
                                try {
                                        val location = locationHelper.getCurrentLocation()
                                        location?.let {
                                                userLocation =
                                                        locationHelper.getShortLocationName(
                                                                it.latitude,
                                                                it.longitude
                                                        )
                                        }
                                                ?: run { userLocation = "Location unavailable" }
                                } catch (e: Exception) {
                                        userLocation = "Location unavailable"
                                }
                        } else {
                                userLocation = "Enable location"
                        }

                        // Get all appointments and filter for upcoming
                        val allAppointments = FirestoreHelper.getUserAppointments(userId)
                        val today =
                                java.text.SimpleDateFormat(
                                                "yyyy-MM-dd",
                                                java.util.Locale.getDefault()
                                        )
                                        .format(java.util.Date())

                        appointments =
                                allAppointments
                                        .filter { appointment ->
                                                appointment.status.equals(
                                                        "CONFIRMED",
                                                        ignoreCase = true
                                                ) && appointment.date >= today
                                        }
                                        .take(3)

                        appointmentCount =
                                allAppointments
                                        .filter {
                                                it.status.equals("CONFIRMED", ignoreCase = true) &&
                                                        it.date >= today
                                        }
                                        .size

                        recordCount = FirestoreHelper.getUserMedicalRecords(userId).size
                        reminderCount = FirestoreHelper.getUserReminders(userId).size

                        isLoading = false
                        delay(100)
                        animationTrigger = true
                }
        }

        if (isLoading) {
                DashboardSkeleton()
                return
        }

        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Spacing.xxl)
        ) {
                // Location Header (Flipkart/Amazon Style)
                item { LocationHeader(location = userLocation) }

                // Premium Hero Section with Gradient
                item {
                        AnimatedVisibility(
                                visible = animationTrigger,
                                enter =
                                        fadeIn(tween(400)) +
                                                slideInVertically(tween(400)) { -it / 2 }
                        ) {
                                PremiumHeroSection(
                                        userName = userName,
                                        profilePictureUrl = userProfilePicture,
                                        healthTip = healthTips[currentTipIndex]
                                )
                        }
                }

                // Health Stats Cards
                item {
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        AnimatedVisibility(
                                visible = animationTrigger,
                                enter = fadeIn(tween(600, delayMillis = 200))
                        ) {
                                HealthStatsRow(
                                        appointmentCount = appointmentCount,
                                        recordCount = recordCount,
                                        reminderCount = reminderCount,
                                        onAppointmentsClick = onNavigateToAppointments,
                                        onRecordsClick = onNavigateToRecords,
                                        onRemindersClick = onNavigateToReminders
                                )
                        }
                }

                // Quick Actions Grid - Modern Design
                item {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        AnimatedVisibility(
                                visible = animationTrigger,
                                enter = fadeIn(tween(600, delayMillis = 300))
                        ) {
                                Column {
                                        Text(
                                                text = "Quick Actions",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.padding(horizontal = Spacing.xl)
                                        )
                                        Spacer(modifier = Modifier.height(Spacing.md))
                                        ModernQuickActionsGrid(
                                                onMedicalRecords = onNavigateToRecords,
                                                onEmergencySOS = {
                                                        context.startActivity(
                                                                Intent(
                                                                        context,
                                                                        EmergencySOSActivity::class
                                                                                .java
                                                                )
                                                        )
                                                },
                                                onReminders = {
                                                        context.startActivity(
                                                                Intent(
                                                                        context,
                                                                        MedicationRemindersActivity::class
                                                                                .java
                                                                )
                                                        )
                                                },
                                                onHealthTips = onNavigateToWellness,
                                                onHospitalLocator = {
                                                        context.startActivity(
                                                                Intent(
                                                                        context,
                                                                        HospitalLocatorActivity::class
                                                                                .java
                                                                )
                                                        )
                                                }
                                        )
                                }
                        }
                }

                // Upcoming Appointments Section - Enhanced
                item {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        AnimatedVisibility(
                                visible = animationTrigger,
                                enter = fadeIn(tween(600, delayMillis = 400))
                        ) {
                                Column {
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(horizontal = Spacing.lg),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.CalendarMonth,
                                                                contentDescription = null,
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                modifier = Modifier.size(24.dp)
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.width(Spacing.sm)
                                                        )
                                                        Text(
                                                                text = "Upcoming Appointments",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleLarge,
                                                                fontWeight = FontWeight.Bold,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onBackground
                                                        )
                                                }
                                                TextButton(onClick = onNavigateToAppointments) {
                                                        Text(
                                                                text = "View All",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                fontWeight = FontWeight.SemiBold
                                                        )
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(Spacing.md))

                                        if (appointments.isEmpty()) {
                                                PremiumEmptyState(
                                                        icon = Icons.Default.EventAvailable,
                                                        title = "No Upcoming Appointments",
                                                        message =
                                                                "Book an appointment with our specialist doctors",
                                                        actionLabel = "Book Now",
                                                        onAction = onNavigateToAppointments
                                                )
                                        } else {
                                                appointments.forEach { appointment ->
                                                        PremiumAppointmentCard(
                                                                appointment = appointment,
                                                                onCancel = {
                                                                        scope.launch {
                                                                                val userId =
                                                                                        FirebaseAuthHelper
                                                                                                .getCurrentUserId()
                                                                                val allAppointments =
                                                                                        FirestoreHelper
                                                                                                .getUserAppointments(
                                                                                                        userId
                                                                                                )
                                                                                val today =
                                                                                        java.text
                                                                                                .SimpleDateFormat(
                                                                                                        "yyyy-MM-dd",
                                                                                                        java.util
                                                                                                                .Locale
                                                                                                                .getDefault()
                                                                                                )
                                                                                                .format(
                                                                                                        java.util
                                                                                                                .Date()
                                                                                                )

                                                                                appointments =
                                                                                        allAppointments
                                                                                                .filter {
                                                                                                        app
                                                                                                        ->
                                                                                                        app.status
                                                                                                                .equals(
                                                                                                                        "CONFIRMED",
                                                                                                                        ignoreCase =
                                                                                                                                true
                                                                                                                ) &&
                                                                                                                app.date >=
                                                                                                                        today
                                                                                                }
                                                                                                .take(
                                                                                                        3
                                                                                                )
                                                                        }
                                                                }
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(Spacing.md)
                                                        )
                                                }
                                        }
                                }
                        }
                }

                item { Spacer(modifier = Modifier.height(Spacing.lg)) }
        }
}

// ============================================
// PREMIUM HERO SECTION
// ============================================
@Composable
private fun PremiumHeroSection(userName: String, profilePictureUrl: String?, healthTip: String) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(
                                        brush =
                                                Brush.verticalGradient(
                                                        colors =
                                                                listOf(
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                        MaterialTheme.colorScheme
                                                                                .primary.copy(
                                                                                alpha = 0.85f
                                                                        )
                                                                )
                                                )
                                )
                                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
                Column {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                // User Info
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = getGreeting(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                                text = userName.ifEmpty { "User" },
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 22.sp
                                        )
                                }

                                // Profile Picture
                                Surface(
                                        shape = CircleShape,
                                        modifier = Modifier.size(48.dp),
                                        color = Color.White.copy(alpha = 0.2f),
                                        border =
                                                BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
                                ) {
                                        if (profilePictureUrl != null) {
                                                AsyncImage(
                                                        model =
                                                                ImageRequest.Builder(
                                                                                LocalContext.current
                                                                        )
                                                                        .data(profilePictureUrl)
                                                                        .crossfade(true)
                                                                        .build(),
                                                        contentDescription = "Profile",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                )
                                        } else {
                                                Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.padding(10.dp)
                                                )
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // Glassmorphism Health Tip Card - More Compact
                        Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth(),
                                border =
                                        BorderStroke(
                                                1.dp,
                                                Brush.verticalGradient(
                                                        listOf(
                                                                Color.White.copy(alpha = 0.3f),
                                                                Color.Transparent
                                                        )
                                                )
                                        )
                        ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                        // Accent blur/glow
                                        Box(
                                                modifier =
                                                        Modifier.size(80.dp)
                                                                .align(Alignment.TopEnd)
                                                                .background(
                                                                        Brush.radialGradient(
                                                                                colors =
                                                                                        listOf(
                                                                                                Color.White
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.1f
                                                                                                        ),
                                                                                                Color.Transparent
                                                                                        )
                                                                        )
                                                                )
                                        )

                                        Row(
                                                modifier = Modifier.padding(Spacing.md),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Box(
                                                        modifier =
                                                                Modifier.size(40.dp)
                                                                        .background(
                                                                                color =
                                                                                        Color.White
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.2f
                                                                                                ),
                                                                                shape =
                                                                                        RoundedCornerShape(
                                                                                                10.dp
                                                                                        )
                                                                        ),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.Lightbulb,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(22.dp)
                                                        )
                                                }

                                                Spacer(modifier = Modifier.width(Spacing.sm))

                                                Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        text = "HEALTH TIP",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .labelSmall,
                                                                        color =
                                                                                Color.White.copy(
                                                                                        alpha = 0.7f
                                                                                ),
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        letterSpacing = 1.sp
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(4.dp)
                                                                )
                                                                Surface(
                                                                        shape = CircleShape,
                                                                        color = Color(0xFFFFD700),
                                                                        modifier =
                                                                                Modifier.size(5.dp)
                                                                ) {}
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Text(
                                                                text = healthTip,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Medium,
                                                                lineHeight = 18.sp
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

// ============================================
// HEALTH STATS ROW - FIXED (NOT SCROLLABLE)
// ============================================
@Composable
private fun HealthStatsRow(
    appointmentCount: Int,
    recordCount: Int,
    reminderCount: Int,
    onAppointmentsClick: () -> Unit,
    onRecordsClick: () -> Unit,
    onRemindersClick: () -> Unit
) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
                HealthStatCard(
                        icon = Icons.Default.CalendarMonth,
                        count = appointmentCount,
                        label = "Appointments",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onAppointmentsClick
                )
                HealthStatCard(
                        icon = Icons.Default.Description,
                        count = recordCount,
                        label = "Records",
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = onRecordsClick
                )
                HealthStatCard(
                        icon = Icons.Default.Alarm,
                        count = reminderCount,
                        label = "Reminders",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = onRemindersClick
                )
        }
}

@Composable
private fun HealthStatCard(
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
        var visible by remember { mutableStateOf(false) }
        val scale by
                animateFloatAsState(
                        targetValue = if (visible) 1f else 0.8f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "scale"
                )

        LaunchedEffect(Unit) {
                delay(100)
                visible = true
        }

        Surface(
                modifier = modifier.scale(scale).clickable(onClick = onClick),
                shape = RoundedCornerShape(20.dp),
                color = color.copy(alpha = 0.1f),
                tonalElevation = 0.dp
        ) {
                Column(
                        modifier = Modifier.padding(Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    )
                }
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                        )
                        Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                        )
                }
        }
}

// ============================================
// MODERN QUICK ACTIONS GRID WITH CUSTOM BACKGROUNDS
// ============================================
@Composable
private fun ModernQuickActionsGrid(
        onMedicalRecords: () -> Unit,
        onEmergencySOS: () -> Unit,
        onReminders: () -> Unit,
        onHealthTips: () -> Unit,
        onHospitalLocator: () -> Unit
) {
        Column(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                        IllustratedActionCard(
                                icon = null,
                                title = "Medical\nRecords",
                                gradient = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
                                backgroundImage = R.drawable.medical_records_bg,
                                onClick = onMedicalRecords,
                                modifier = Modifier.weight(1f)
                        )
                        IllustratedActionCard(
                                icon = Icons.Default.Emergency,
                                title = "Emergency\nSOS",
                                gradient = listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
                                backgroundImage = R.drawable.sos_background,
                                onClick = onEmergencySOS,
                                modifier = Modifier.weight(1f)
                        )
                }
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                        IllustratedActionCard(
                                icon = null,
                                title = "Medication\nReminders",
                                gradient = listOf(Color(0xFF4facfe), Color(0xFF00f2fe)),
                                backgroundImage = R.drawable.medication_bg,
                                onClick = onReminders,
                                modifier = Modifier.weight(1f)
                        )
                        IllustratedActionCard(
                                icon = null,
                                title = "Health\nTips",
                                gradient = listOf(Color(0xFF43e97b), Color(0xFF38f9d7)),
                                backgroundImage = R.drawable.health_tips_bg,
                                onClick = onHealthTips,
                                modifier = Modifier.weight(1f)
                        )
                }
                // Hospital Locator - Full width card
                HospitalLocatorCard(onClick = onHospitalLocator)
        }
}

@Composable
private fun HospitalLocatorCard(onClick: () -> Unit) {
        var pressed by remember { mutableStateOf(false) }
        val scale by
                animateFloatAsState(
                        targetValue = if (pressed) 0.98f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "scale"
                )

        Surface(
                onClick = {
                        pressed = true
                        onClick()
                },
                modifier = Modifier.fillMaxWidth().height(80.dp).scale(scale),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
        ) {
                Box(modifier = Modifier.fillMaxSize()) {
                        // Gradient background
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        brush =
                                                                Brush.horizontalGradient(
                                                                        colors =
                                                                                listOf(
                                                                                        Color(
                                                                                                0xFF1976D2
                                                                                        ),
                                                                                        Color(
                                                                                                0xFF42A5F5
                                                                                        )
                                                                                )
                                                                )
                                                )
                        )

                        Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                                shape = CircleShape,
                                                color = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(48.dp)
                                        ) {
                                                Icon(
                                                        Icons.Default.LocalHospital,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.padding(12.dp)
                                                )
                                        }
                                        Spacer(modifier = Modifier.width(Spacing.md))
                                        Column {
                                                Text(
                                                        "Find Nearby Hospitals",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                )
                                                Text(
                                                        "Locate hospitals, clinics & emergency rooms",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White.copy(alpha = 0.8f)
                                                )
                                        }
                                }
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                )
                        }
                }
        }

        LaunchedEffect(pressed) {
                if (pressed) {
                        delay(150)
                        pressed = false
                }
        }
}

@Composable
private fun IllustratedActionCard(
        icon: ImageVector?,
        title: String,
        gradient: List<Color>,
        backgroundImage: Int?,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
        var pressed by remember { mutableStateOf(false) }
        val scale by
                animateFloatAsState(
                        targetValue = if (pressed) 0.95f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "scale"
                )

        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                // Card with background image
                Surface(
                        onClick = {
                                pressed = true
                                onClick()
                        },
                        modifier = Modifier.fillMaxWidth().height(85.dp).scale(scale),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent
                ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                                // Background image if provided
                                if (backgroundImage != null) {
                                        Image(
                                                painter = painterResource(id = backgroundImage),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                        )
                                        // Very light gradient overlay (10-20% to make image very
                                        // visible)
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .background(
                                                                        brush =
                                                                                Brush.verticalGradient(
                                                                                        colors =
                                                                                                listOf(
                                                                                                        gradient[
                                                                                                                        0]
                                                                                                                .copy(
                                                                                                                        alpha =
                                                                                                                                0.1f
                                                                                                                ),
                                                                                                        gradient[
                                                                                                                        1]
                                                                                                                .copy(
                                                                                                                        alpha =
                                                                                                                                0.2f
                                                                                                                )
                                                                                                )
                                                                                )
                                                                )
                                        )
                                } else {
                                        // Fallback to gradient background
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .background(
                                                                        brush =
                                                                                Brush.linearGradient(
                                                                                        colors =
                                                                                                gradient
                                                                                )
                                                                )
                                        )
                                }

                                // Icon overlay for visual representation
                                if (icon != null) {
                                        Surface(
                                                shape = CircleShape,
                                                color = Color.White.copy(alpha = 0.2f),
                                                modifier =
                                                        Modifier.padding(Spacing.sm)
                                                                .size(36.dp)
                                                                .align(Alignment.TopStart)
                                        ) {
                                                Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.padding(8.dp)
                                                )
                                        }
                                }
                        }
                }

                // Title below the card
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                        text = title.replace("\n", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                )
        }

        LaunchedEffect(pressed) {
                if (pressed) {
                        delay(150)
                        pressed = false
                }
        }
}

// ============================================
// PREMIUM APPOINTMENT CARD
// ============================================
@Composable
private fun PremiumAppointmentCard(appointment: Appointment, onCancel: () -> Unit) {
        Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 1.dp
        ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                        ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(48.dp)
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(12.dp)
                                                )
                                        }
                                        Spacer(modifier = Modifier.width(Spacing.md))
                                        Column {
                                                Text(
                                                        text = appointment.doctorName,
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                        text = "Specialist",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                }
                                StatusBadge(status = appointment.status, size = BadgeSize.SMALL)
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                        ) {
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.xs))
                                        Text(
                                                text = formatDate(appointment.date),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Medium
                                        )
                                }
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.xs))
                                        Text(
                                                text = appointment.time,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Medium
                                        )
                                }
                        }

                        if (appointment.status != "Cancelled" && appointment.status != "Completed"
                        ) {
                                Spacer(modifier = Modifier.height(Spacing.lg))
                                TextButton(
                                        onClick = onCancel,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                ButtonDefaults.textButtonColors(
                                                        contentColor =
                                                                MaterialTheme.colorScheme.error
                                                )
                                ) {
                                        Text(
                                                "Cancel Appointment",
                                                style = MaterialTheme.typography.bodySmall
                                        )
                                }
                        }
                }
        }
}

// ============================================
// PREMIUM EMPTY STATE
// ============================================
@Composable
private fun PremiumEmptyState(
        icon: ImageVector,
        title: String,
        message: String,
        actionLabel: String,
        onAction: () -> Unit
) {
        Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
                Column(
                        modifier = Modifier.padding(Spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Surface(
                                shape = CircleShape,
                                color =
                                        MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.5f
                                        ),
                                modifier = Modifier.size(64.dp)
                        ) {
                                Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.fillMaxSize().padding(16.dp)
                                )
                        }
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Button(
                                onClick = onAction,
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                        )
                        ) {
                                Text(
                                        text = actionLabel,
                                        modifier =
                                                Modifier.padding(
                                                        horizontal = Spacing.md,
                                                        vertical = Spacing.xs
                                                )
                                )
                        }
                }
        }
}

// ============================================
// HELPER FUNCTIONS
// ============================================
private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
        }
}

private fun formatDate(dateString: String): String {
        return try {
                val inputFormat =
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat =
                        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
                dateString
        }
}

// ==================== USER REMINDERS SCREEN ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRemindersScreen() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showAddDialog by remember { mutableStateOf(false) }

        fun loadReminders() {
                scope.launch {
                        isLoading = true
                        val userId = FirebaseAuthHelper.getCurrentUserId()
                        reminders = FirestoreHelper.getUserReminders(userId)
                        isLoading = false
                }
        }

        LaunchedEffect(Unit) { loadReminders() }

        if (showAddDialog) {
                AddReminderDialog(
                        onDismiss = { showAddDialog = false },
                        onAdd = { medicineName, time ->
                                scope.launch {
                                        val reminder = Reminder(
                                                userId = FirebaseAuthHelper.getCurrentUserId(),
                                                medicineName = medicineName,
                                                time = time
                                        )
                                        val result = FirestoreHelper.addReminder(reminder)
                                        result.fold(
                                                onSuccess = { reminderId ->
                                                        ReminderUtils.scheduleReminder(context, reminderId, medicineName, time)
                                                        Toast.makeText(context, "Reminder added!", Toast.LENGTH_SHORT).show()
                                                        loadReminders()
                                                },
                                                onFailure = { error ->
                                                        Toast.makeText(context, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        )
                                }
                                showAddDialog = false
                        }
                )
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text("Medication Reminders", fontWeight = FontWeight.Bold)
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.background,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.onBackground
                                        )
                        )
                },
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = { showAddDialog = true },
                                containerColor = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                        ) { Icon(Icons.Default.Add, "Add Reminder", tint = Color.White) }
                },
                containerColor = MaterialTheme.colorScheme.background
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
                                                modifier =
                                                        Modifier.fillMaxSize().padding(Spacing.xxl),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                        ) {
                                                Surface(
                                                        shape = CircleShape,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer.copy(
                                                                        alpha = 0.3f
                                                                ),
                                                        modifier = Modifier.size(120.dp)
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Alarm,
                                                                contentDescription = null,
                                                                modifier = Modifier.padding(32.dp),
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                }
                                                Spacer(modifier = Modifier.height(Spacing.xl))
                                                Text(
                                                        text = "No reminders yet",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .headlineSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(Spacing.sm))
                                                Text(
                                                        text =
                                                                "Stay on track with your meds. Tap the button below to add your first reminder.",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        textAlign =
                                                                androidx.compose.ui.text.style
                                                                        .TextAlign.Center
                                                )
                                        }
                                }
                                else -> {
                                        LazyColumn(
                                                modifier = Modifier.fillMaxSize(),
                                                contentPadding =
                                                        PaddingValues(
                                                                horizontal = Spacing.lg,
                                                                vertical = Spacing.md
                                                        ),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(Spacing.md)
                                        ) {
                                                items(reminders) { reminder ->
                                                        ReminderCard(
                                                                reminder = reminder,
                                                                onDelete = {
                                                                        scope.launch {
                                                                                FirestoreHelper.deleteReminder(reminder.id)
                                                                                ReminderUtils.cancelReminder(context, reminder.id)
                                                                                loadReminders()
                                                                        }
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}


// ==================== USER SETTINGS SCREEN ====================
@Composable
fun UserSettingsScreen(onLogout: () -> Unit) {
        val context = LocalContext.current

        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
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
                                icon = null,
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                text = "Preferences",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                        )
                }

                item {
                        val scope = rememberCoroutineScope()
                        var isDarkMode by remember { mutableStateOf(false) }

                        // Load current theme preference
                        LaunchedEffect(Unit) {
                                val themeManager = com.example.healthmate.util.ThemeManager(context)
                                isDarkMode = themeManager.isDarkMode.first()
                        }

                        SettingsItemWithToggle(
                                icon = Icons.Default.DarkMode,
                                title = "Night Mode",
                                subtitle =
                                        if (isDarkMode) "Dark theme enabled"
                                        else "Light theme enabled",
                                checked = isDarkMode,
                                onCheckedChange = { enabled ->
                                        isDarkMode = enabled
                                        scope.launch {
                                                val themeManager =
                                                        com.example.healthmate.util.ThemeManager(
                                                                context
                                                        )
                                                themeManager.setDarkMode(enabled)
                                                // Restart activity to apply theme
                                                (context as? Activity)?.recreate()
                                        }
                                }
                        )
                }

                item {
                        SettingsItem(
                                icon = null,
                                title = "Notifications",
                                subtitle = "Manage notification preferences",
                                onClick = {
                                        // TODO: Navigate to NotificationsActivity when created
                                        Toast.makeText(
                                                        context,
                                                        "Notification settings coming soon",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
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
                                Icon(Icons.AutoMirrored.Filled.Logout, "Logout")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Logout", modifier = Modifier.padding(vertical = 8.dp))
                        }
                }
        }
}

@Composable
fun SettingsItem(icon: ImageVector?, title: String, subtitle: String, onClick: () -> Unit) {
        Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
                shape = HealthMateShapes.InputField,
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        if (icon != null) {
                                Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Navigate",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}

@Composable
fun SettingsItemWithToggle(
        icon: ImageVector?,
        title: String,
        subtitle: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = HealthMateShapes.InputField,
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        if (icon != null) {
                                Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        Switch(checked = checked, onCheckedChange = onCheckedChange)
                }
        }
}
