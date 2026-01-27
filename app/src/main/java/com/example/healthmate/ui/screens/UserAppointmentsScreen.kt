package com.example.healthmate.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch

@Composable
fun UserAppointmentsScreen() {
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
                                        val slots = FirestoreHelper.getAvailableSlots(doctor.id)
                                        availableSlots = slots
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

            if (availableSlots.isEmpty()) {
                Text("No available slots.")
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
