package com.example.healthmate.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Appointment
import com.example.healthmate.model.Doctor
import com.example.healthmate.model.Slot
import com.example.healthmate.util.FirestoreDiagnostic
import com.example.healthmate.util.FirestoreMigration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAppointmentsScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("My Appointments", "Book New")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> MyAppointmentsTab()
            1 -> BookAppointmentTab()
        }
    }
}

@Composable
fun MyAppointmentsTab() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var filteredAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Upcoming", "Completed", "Cancelled")

    LaunchedEffect(Unit) {
        scope.launch {
            android.util.Log.d("MyAppointments", "========================================")
            android.util.Log.d("MyAppointments", "Loading My Appointments Tab")
            android.util.Log.d("MyAppointments", "========================================")

            // Auto-update appointment statuses first
            android.util.Log.d("MyAppointments", "Running auto-update appointment statuses...")
            val updateResult = FirestoreHelper.autoUpdateAppointmentStatuses()
            updateResult.fold(
                onSuccess = { count ->
                    android.util.Log.d("MyAppointments", "Auto-updated $count appointments to COMPLETED")
                },
                onFailure = { e ->
                    android.util.Log.e("MyAppointments", "Auto-update failed: ${e.message}")
                }
            )

            val userId = FirebaseAuthHelper.getCurrentUserId()
            android.util.Log.d("MyAppointments", "Current User ID: $userId")

            appointments = FirestoreHelper.getUserAppointments(userId)
            android.util.Log.d("MyAppointments", "Total appointments fetched: ${appointments.size}")

            appointments.forEachIndexed { index, app ->
                android.util.Log.d("MyAppointments", "Appointment #$index:")
                android.util.Log.d("MyAppointments", "  - ID: ${app.id}")
                android.util.Log.d("MyAppointments", "  - Patient ID: ${app.patientId}")
                android.util.Log.d("MyAppointments", "  - Patient Name: ${app.patientName}")
                android.util.Log.d("MyAppointments", "  - Doctor: ${app.doctorName}")
                android.util.Log.d("MyAppointments", "  - Date: ${app.date}")
                android.util.Log.d("MyAppointments", "  - Time: ${app.time}")
                android.util.Log.d("MyAppointments", "  - Status: ${app.status}")
                android.util.Log.d("MyAppointments", "  - Slot ID: ${app.slotId}")
            }

            filteredAppointments = appointments
            android.util.Log.d("MyAppointments", "Initial filtered appointments: ${filteredAppointments.size}")

            isLoading = false
            android.util.Log.d("MyAppointments", "========================================")
            android.util.Log.d("MyAppointments", "My Appointments Loading Complete")
            android.util.Log.d("MyAppointments", "========================================")
        }
    }

    LaunchedEffect(selectedFilter, appointments) {
        filteredAppointments = when (selectedFilter) {
            "Upcoming" -> appointments.filter { it.status.equals("CONFIRMED", ignoreCase = true) }
            "Completed" -> appointments.filter { it.status.equals("COMPLETED", ignoreCase = true) }
            "Cancelled" -> appointments.filter { it.status.equals("CANCELLED", ignoreCase = true) }
            else -> appointments
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredAppointments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No ${if (selectedFilter == "All") "" else selectedFilter.lowercase()} appointments",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAppointments) { appointment ->
                    DetailedAppointmentCard(
                        appointment = appointment,
                        onCancel = {
                            // Refresh appointments list
                            scope.launch {
                                val userId = FirebaseAuthHelper.getCurrentUserId()
                                appointments = FirestoreHelper.getUserAppointments(userId)
                                filteredAppointments = when (selectedFilter) {
                                    "Upcoming" -> appointments.filter { it.status.equals("CONFIRMED", ignoreCase = true) }
                                    "Completed" -> appointments.filter { it.status.equals("COMPLETED", ignoreCase = true) }
                                    "Cancelled" -> appointments.filter { it.status.equals("CANCELLED", ignoreCase = true) }
                                    else -> appointments
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedAppointmentCard(appointment: Appointment, onCancel: (Appointment) -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.doctorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.date,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(status = appointment.status)
            }

            // Show cancel button only for CONFIRMED appointments
            if (appointment.status.equals("CONFIRMED", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(12.dp))
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
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel Appointment")
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Appointment") },
            text = {
                Text("Are you sure you want to cancel this appointment with ${appointment.doctorName} on ${appointment.date}?")
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
                                        "Appointment cancelled successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showCancelDialog = false
                                    onCancel(appointment)
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
                    Text("Keep Appointment")
                }
            }
        )
    }
}

@Composable
fun BookAppointmentTab() {
    var step by remember { mutableStateOf(0) }
    var bookingMode by remember { mutableStateOf<BookingMode?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (step == 0) {
            Text(
                    "Book an Appointment",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
            )

            BookingModeCard(
                    title = "Search by Date",
                    description = "Find available doctors for a specific time.",
                    icon = Icons.Default.CalendarMonth,
                    onClick = {
                        bookingMode = BookingMode.BY_DATE
                        step = 1
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BookingModeCard(
                    title = "Search by Doctor",
                    description = "Choose your preferred doctor first.",
                    icon = Icons.Default.Person,
                    onClick = {
                        bookingMode = BookingMode.BY_DOCTOR
                        step = 1
                    }
            )
        } else {
            when (bookingMode) {
                BookingMode.BY_DATE -> BookByDateFlow(onBack = { step = 0 })
                BookingMode.BY_DOCTOR -> BookByDoctorFlow(onBack = { step = 0 })
                null -> step = 0
            }
        }
    }
}

enum class BookingMode {
    BY_DATE,
    BY_DOCTOR
}

@Composable
fun BookByDateFlow(onBack: () -> Unit) {
    var selectedDate by remember { mutableStateOf("") }
    var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack) { Text("< Back") }

        Text("Select Date", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
                value = selectedDate,
                onValueChange = { selectedDate = it },
                label = { Text("YYYY-MM-DD") },
                placeholder = { Text("2024-02-01") },
                modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        slots = FirestoreHelper.getAvailableSlotsByDate(selectedDate)
                        isLoading = false
                        if (slots.isEmpty())
                                Toast.makeText(context, "No slots found", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = selectedDate.length >= 8 && !isLoading,
                modifier = Modifier.fillMaxWidth()
        ) { Text(if (isLoading) "Searching..." else "Find Available Slots") }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(slots) { slot ->
                SlotCard(
                        slot = slot,
                        onBook = {
                            scope.launch {
                                val user = FirebaseAuthHelper.getCurrentUser()
                                if (user != null) {
                                    val appointment =
                                            Appointment(
                                                    patientId = user.uid,
                                                    patientName = user.displayName ?: "User",
                                                    doctorId = slot.doctorId,
                                                    doctorName = slot.doctorName,
                                                    slotId = slot.id,
                                                    date = slot.date,
                                                    time = slot.time
                                            )
                                    val result = FirestoreHelper.bookAppointment(appointment)
                                    result.fold(
                                            onSuccess = {
                                                Toast.makeText(
                                                                context,
                                                                "Booking Confirmed!",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                onBack()
                                            },
                                            onFailure = { e ->
                                                Toast.makeText(
                                                                context,
                                                                "Failed: ${e.message}",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            }
                                    )
                                }
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun BookByDoctorFlow(onBack: () -> Unit) {
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var filteredDoctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var availableSlots by remember { mutableStateOf<List<Slot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true

        // First, migrate old slots to use 'isBooked' field
        android.util.Log.d("UserAppointments", "Running slot field migration...")
        val migrationResult = FirestoreMigration.migrateSlotFields()
        migrationResult.fold(
            onSuccess = { count ->
                android.util.Log.d("UserAppointments", "Migration completed: $count slots updated")
                Toast.makeText(context, "Database updated: $count slots migrated", Toast.LENGTH_SHORT).show()
            },
            onFailure = { e ->
                android.util.Log.e("UserAppointments", "Migration failed: ${e.message}", e)
            }
        )

        doctors = FirestoreHelper.getDoctors()
        filteredDoctors = doctors
        isLoading = false

        // Debug: Run comprehensive diagnostics
        scope.launch {
            android.util.Log.d("UserAppointments", "Running Firestore diagnostics...")
            FirestoreDiagnostic.runDiagnostics()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            filteredDoctors = doctors
        } else {
            filteredDoctors =
                    doctors.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.specialization.contains(searchQuery, ignoreCase = true)
                    }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = { if (selectedDoctor != null) selectedDoctor = null else onBack() }) {
            Text(if (selectedDoctor != null) "< Back to Doctors" else "< Back")
        }

        if (selectedDoctor == null) {
            Text("Select Doctor", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Doctor or Specialty") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) CircularProgressIndicator()
            else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredDoctors) { doctor ->
                        DoctorCardPlaceholder(
                                name = doctor.name,
                                specialty = doctor.specialization,
                                onClick = {
                                    selectedDoctor = doctor
                                    scope.launch {
                                        isLoading = true
                                        android.util.Log.d("UserAppointments", "Fetching slots for doctor: ${doctor.id}, name: ${doctor.name}")

                                        // Run test query for this specific doctor
                                        FirestoreDiagnostic.testQuery(doctor.id)

                                        val slots = FirestoreHelper.getAvailableSlots(doctor.id)
                                        android.util.Log.d("UserAppointments", "Fetched ${slots.size} slots")
                                        availableSlots = slots
                                        isLoading = false
                                        if (slots.isEmpty()) {
                                            Toast.makeText(context, "No available slots for this doctor", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        } else {
            Text(
                    "Available Slots for ${selectedDoctor!!.name}",
                    style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (availableSlots.isEmpty()) {
                Text("No available slots for this doctor.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableSlots) { slot ->
                        SlotCard(
                                slot = slot,
                                onBook = {
                                    scope.launch {
                                        val user = FirebaseAuthHelper.getCurrentUser()
                                        if (user != null) {
                                            val appointment =
                                                    Appointment(
                                                            patientId = user.uid,
                                                            patientName = user.displayName
                                                                            ?: "User",
                                                            doctorId = slot.doctorId,
                                                            doctorName = slot.doctorName,
                                                            slotId = slot.id,
                                                            date = slot.date,
                                                            time = slot.time
                                                    )
                                            val result =
                                                    FirestoreHelper.bookAppointment(appointment)
                                            result.fold(
                                                    onSuccess = {
                                                        Toast.makeText(
                                                                        context,
                                                                        "Booking Confirmed!",
                                                                        Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                        onBack()
                                                    },
                                                    onFailure = { e ->
                                                        Toast.makeText(
                                                                        context,
                                                                        "Failed: ${e.message}",
                                                                        Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                    }
                                            )
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlotCard(slot: Slot, onBook: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
    ) {
        Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                        slot.doctorName.ifEmpty { "Doctor" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                )
                Text("${slot.date} at ${slot.time}", style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onBook) { Text("Book") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingModeCard(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DoctorCardPlaceholder(name: String, specialty: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(name, fontWeight = FontWeight.Bold)
                Text(specialty)
            }
        }
    }
}
