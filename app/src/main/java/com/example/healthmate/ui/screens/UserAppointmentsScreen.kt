package com.example.healthmate.ui.screens

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Appointment
import com.example.healthmate.model.Doctor
import com.example.healthmate.model.Slot
import com.example.healthmate.ui.components.AppointmentListItem
import com.example.healthmate.ui.components.AppointmentListSkeleton
import com.example.healthmate.ui.components.DoctorListItem
import com.example.healthmate.ui.components.DoctorListSkeleton
import com.example.healthmate.ui.components.EmptyState
import com.example.healthmate.ui.components.LoadingState
import com.example.healthmate.ui.components.SearchSlotListItem
import com.example.healthmate.ui.components.TimeSlotChip
import com.example.healthmate.ui.theme.Spacing
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
            FirestoreHelper.autoUpdateAppointmentStatuses()
            val userId = FirebaseAuthHelper.getCurrentUserId()
            appointments = FirestoreHelper.getUserAppointments(userId)
            isLoading = false
        }
    }

    LaunchedEffect(selectedFilter, appointments) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        filteredAppointments =
                when (selectedFilter) {
                    "Upcoming" ->
                            appointments.filter { 
                                it.status.equals("CONFIRMED", ignoreCase = true) && it.date >= today 
                            }
                    "Completed" ->
                            appointments.filter { 
                                it.status.equals("COMPLETED", ignoreCase = true) || 
                                (it.status.equals("CONFIRMED", ignoreCase = true) && it.date < today)
                            }
                    "Cancelled" ->
                            appointments.filter { it.status.equals("CANCELLED", ignoreCase = true) }
                    else -> appointments
                }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            filters.forEach { filter ->
                FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                )
            }
        }

        // Confirmation Dialog State
        var showDeleteDialog by remember { mutableStateOf(false) }
        var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }

        // Confirmation Dialog
        if (showDeleteDialog && appointmentToDelete != null) {
            AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Cancel Appointment?") },
                    text = {
                        Column {
                            Text("Are you sure you want to cancel this appointment?")
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                    "Doctor: ${appointmentToDelete?.doctorName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                            )
                            Text(
                                    "Date: ${appointmentToDelete?.date} at ${appointmentToDelete?.time}",
                                    style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                                onClick = {
                                    scope.launch {
                                        appointmentToDelete?.let {
                                            FirestoreHelper.deleteAppointment(it.id)
                                            appointments =
                                                    appointments.filter { apt -> apt.id != it.id }
                                        }
                                        showDeleteDialog = false
                                        appointmentToDelete = null
                                    }
                                }
                        ) { Text("Yes, Cancel", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("No, Keep It") }
                    }
            )
        }

        if (isLoading) {
            AppointmentListSkeleton()
        } else if (filteredAppointments.isEmpty()) {
            EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title =
                            "No ${if (selectedFilter == "All") "" else selectedFilter} Appointments",
                    message =
                            when (selectedFilter) {
                                "Upcoming" -> "You don't have any upcoming appointments."
                                "Completed" -> "No completed appointments yet."
                                "Cancelled" -> "You haven't cancelled any appointments."
                                else -> "You don't have any appointments yet."
                            }
            )
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(filteredAppointments) { appointment ->
                    AppointmentListItem(
                            appointment = appointment,
                            showActions = true,
                            onCancel = {
                                scope.launch {
                                    FirestoreHelper.cancelAppointment(
                                            appointment.id,
                                            appointment.slotId
                                    )
                                    val userId = FirebaseAuthHelper.getCurrentUserId()
                                    appointments = FirestoreHelper.getUserAppointments(userId)
                                }
                            },
                            onDelete = {
                                appointmentToDelete = appointment
                                showDeleteDialog = true
                            }
                    )
                }
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookByDateFlow(onBack: () -> Unit) {
    var selectedDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (showDatePicker) {
        DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date =
                                            Instant.ofEpochMilli(millis)
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDate()
                                    selectedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                }
                                showDatePicker = false
                            }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
        ) { DatePicker(state = datePickerState) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack) { Text("< Back") }

        Text("Select Date", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("Select Booking Date") },
                placeholder = { Text("Click to choose a date") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false,
                colors =
                        OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        slots = FirestoreHelper.getAvailableSlotsByDate(selectedDate)
                        isLoading = false
                        if (slots.isEmpty())
                                Toast.makeText(
                                                context,
                                                "No slots found for $selectedDate",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                    }
                },
                enabled = selectedDate.isNotEmpty() && !isLoading,
                modifier = Modifier.fillMaxWidth()
        ) { Text(if (isLoading) "Searching..." else "Find Available Slots") }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(slots) { slot ->
                SearchSlotListItem(
                        slot = slot,
                        onClick = {
                            scope.launch {
                                val user = FirebaseAuthHelper.getCurrentUser()
                                if (user != null) {
                                    val patientName = if (user.displayName.isNullOrBlank() || user.displayName == "User") {
                                        FirestoreHelper.getUserById(user.uid)?.name ?: "User"
                                    } else {
                                        user.displayName!!
                                    }
                                    val appointment =
                                            Appointment(
                                                    patientId = user.uid,
                                                    patientName = patientName,
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
        FirestoreMigration.migrateSlotFields()
        doctors = FirestoreHelper.getDoctors()
        filteredDoctors = doctors
        isLoading = false
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

            if (isLoading) {
                DoctorListSkeleton()
            } else {
                LazyColumn(
                        contentPadding = PaddingValues(vertical = Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(filteredDoctors) { doctor ->
                        DoctorListItem(
                                doctor = doctor,
                                showSpecialty = true,
                                showExperience = true,
                                onClick = {
                                    selectedDoctor = doctor
                                    scope.launch {
                                        isLoading = true
                                        val slots = FirestoreHelper.getAvailableSlots(doctor.id)
                                        availableSlots = slots
                                        isLoading = false
                                        if (slots.isEmpty()) {
                                            Toast.makeText(
                                                            context,
                                                            "No available slots for this doctor",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
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
                LoadingState(message = "Loading available slots...")
            } else if (availableSlots.isEmpty()) {
                EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "No Available Slots",
                        message = "This doctor has no available time slots at the moment."
                )
            } else {
                LazyColumn(
                        contentPadding = PaddingValues(vertical = Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(availableSlots) { slot ->
                        TimeSlotChip(
                                slot = slot,
                                onClick = {
                                    scope.launch {
                                        val user = FirebaseAuthHelper.getCurrentUser()
                                        if (user != null) {
                                            val patientName = if (user.displayName.isNullOrBlank() || user.displayName == "User") {
                                                FirestoreHelper.getUserById(user.uid)?.name ?: "User"
                                            } else {
                                                user.displayName!!
                                            }
                                            val appointment =
                                                    Appointment(
                                                            patientId = user.uid,
                                                            patientName = patientName,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingModeCard(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        onClick: () -> Unit
) {
    Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
    ) {
        Row(
                modifier = Modifier.padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(Spacing.lg))
            Column {
                Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
