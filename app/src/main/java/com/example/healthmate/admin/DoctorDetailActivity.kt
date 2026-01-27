package com.example.healthmate.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Doctor
import com.example.healthmate.model.Slot
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.ThemeManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DoctorDetailActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                val doctorId = intent.getStringExtra("doctorId") ?: ""

                setContent {
                        val themeManager = ThemeManager(this)
                        val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
                        HealthMateTheme(darkTheme = isDarkMode) {
                                DoctorDetailScreen(doctorId = doctorId, onBack = { finish() })
                        }
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailScreen(doctorId: String, onBack: () -> Unit) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var doctor by remember { mutableStateOf<Doctor?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var isEditing by remember { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }
        var showAddSlotDialog by remember { mutableStateOf(false) }
        var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }

        // Editable State
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") } // Often read-only
        var specialization by remember { mutableStateOf("") }
        var experience by remember { mutableStateOf("") }
        var contactNumber by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        LaunchedEffect(doctorId) {
                scope.launch {
                        doctor = FirestoreHelper.getDoctorById(doctorId)
                        doctor?.let {
                                name = it.name
                                email = it.email
                                specialization = it.specialization
                                experience = it.experience
                                contactNumber = it.contactNumber
                                description = it.description
                        }
                        slots = FirestoreHelper.getSlotsByDoctor(doctorId)
                        isLoading = false
                }
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text(if (isEditing) "Edit Doctor" else "Doctor Details")
                                },
                                navigationIcon = {
                                        IconButton(onClick = onBack) {
                                                Icon(Icons.Default.ArrowBack, "Back")
                                        }
                                },
                                actions = {
                                        if (!isLoading) {
                                                IconButton(
                                                        onClick = {
                                                                if (isEditing) {
                                                                        // Save
                                                                        isSaving = true
                                                                        scope.launch {
                                                                                try {
                                                                                        FirebaseFirestore
                                                                                                .getInstance()
                                                                                                .collection(
                                                                                                        "doctors"
                                                                                                )
                                                                                                .document(
                                                                                                        doctorId
                                                                                                )
                                                                                                .update(
                                                                                                        mapOf(
                                                                                                                "name" to
                                                                                                                        name,
                                                                                                                "specialization" to
                                                                                                                        specialization,
                                                                                                                "experience" to
                                                                                                                        experience,
                                                                                                                "contactNumber" to
                                                                                                                        contactNumber,
                                                                                                                "description" to
                                                                                                                        description
                                                                                                        )
                                                                                                )
                                                                                                .await()
                                                                                        Toast.makeText(
                                                                                                        context,
                                                                                                        "Doctor updated!",
                                                                                                        Toast.LENGTH_SHORT
                                                                                                )
                                                                                                .show()
                                                                                        isEditing =
                                                                                                false
                                                                                        // Refresh
                                                                                        // doctor
                                                                                        // object
                                                                                        doctor =
                                                                                                doctor?.copy(
                                                                                                        name =
                                                                                                                name,
                                                                                                        specialization =
                                                                                                                specialization,
                                                                                                        experience =
                                                                                                                experience,
                                                                                                        contactNumber =
                                                                                                                contactNumber,
                                                                                                        description =
                                                                                                                description
                                                                                                )
                                                                                } catch (
                                                                                        e:
                                                                                                Exception) {
                                                                                        Toast.makeText(
                                                                                                        context,
                                                                                                        "Error: ${e.message}",
                                                                                                        Toast.LENGTH_SHORT
                                                                                                )
                                                                                                .show()
                                                                                }
                                                                                isSaving = false
                                                                        }
                                                                } else {
                                                                        isEditing = true
                                                                }
                                                        }
                                                ) {
                                                        if (isSaving) {
                                                                CircularProgressIndicator(
                                                                        modifier =
                                                                                Modifier.size(
                                                                                        24.dp
                                                                                ),
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                )
                                                        } else {
                                                                Icon(
                                                                        if (isEditing)
                                                                                Icons.Default.Save
                                                                        else Icons.Default.Edit,
                                                                        contentDescription =
                                                                                if (isEditing)
                                                                                        "Save"
                                                                                else "Edit"
                                                                )
                                                        }
                                                }
                                        }
                                }
                        )
                }
        ) { padding ->
                if (isLoading) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                } else if (doctor == null) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.Center
                        ) { Text("Doctor not found") }
                } else {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(padding)
                                                .padding(16.dp)
                                                .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                // Avatar
                                Box(
                                        modifier =
                                                Modifier.size(100.dp)
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer,
                                                                CircleShape
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                text = name.take(2).uppercase(),
                                                style = MaterialTheme.typography.headlineLarge,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }

                                if (!isEditing) {
                                        Text(
                                                text = name,
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                                text = email,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }

                                HorizontalDivider()

                                OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = isEditing,
                                        singleLine = true
                                )

                                OutlinedTextField(
                                        value = specialization,
                                        onValueChange = { specialization = it },
                                        label = { Text("Specialization") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = isEditing,
                                        singleLine = true
                                )

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                        OutlinedTextField(
                                                value = experience,
                                                onValueChange = { experience = it },
                                                label = { Text("Experience (Yrs)") },
                                                modifier = Modifier.weight(1f),
                                                enabled = isEditing,
                                                singleLine = true
                                        )
                                        OutlinedTextField(
                                                value = contactNumber,
                                                onValueChange = { contactNumber = it },
                                                label = { Text("Contact") },
                                                modifier = Modifier.weight(1f),
                                                enabled = isEditing,
                                                singleLine = true
                                        )
                                }

                                OutlinedTextField(
                                        value = description,
                                        onValueChange = { description = it },
                                        label = { Text("Description") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = isEditing,
                                        minLines = 3,
                                        maxLines = 5
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                                // Slot Management Section
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = "Available Slots",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = { showAddSlotDialog = true }) {
                                                Icon(
                                                        Icons.Default.Add,
                                                        "Add Slot",
                                                        tint = MaterialTheme.colorScheme.primary
                                                )
                                        }
                                }

                                if (slots.isEmpty()) {
                                        Text(
                                                text = "No slots available",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                } else {
                                        slots.forEach { slot ->
                                                Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors =
                                                                CardDefaults.cardColors(
                                                                        containerColor =
                                                                                if (slot.isBooked)
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .surfaceVariant
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .surface
                                                                ),
                                                        elevation = CardDefaults.cardElevation(1.dp)
                                                ) {
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(12.dp),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Column {
                                                                        Text(
                                                                                text =
                                                                                        "${slot.date}",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .bodyMedium,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .SemiBold
                                                                        )
                                                                        Text(
                                                                                text = slot.time,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .bodySmall
                                                                        )
                                                                }
                                                                if (slot.isBooked) {
                                                                        Text(
                                                                                text = "Booked",
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .error,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .labelSmall
                                                                        )
                                                                } else {
                                                                        IconButton(
                                                                                onClick = {
                                                                                        scope
                                                                                                .launch {
                                                                                                        FirestoreHelper
                                                                                                                .deleteSlot(
                                                                                                                        slot.id
                                                                                                                )
                                                                                                        // Refresh slots
                                                                                                        slots =
                                                                                                                FirestoreHelper
                                                                                                                        .getSlotsByDoctor(
                                                                                                                                doctorId
                                                                                                                        )
                                                                                                }
                                                                                }
                                                                        ) {
                                                                                Icon(
                                                                                        Icons.Default
                                                                                                .Delete,
                                                                                        "Delete",
                                                                                        tint =
                                                                                                Color.Red
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        if (showAddSlotDialog) {
                var date by remember { mutableStateOf("") }
                var time by remember { mutableStateOf("") }

                AlertDialog(
                        onDismissRequest = { showAddSlotDialog = false },
                        title = { Text("Add Slot") },
                        text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                                value = date,
                                                onValueChange = { date = it },
                                                label = { Text("Date (YYYY-MM-DD)") },
                                                placeholder = { Text("2024-03-25") },
                                                singleLine = true
                                        )
                                        OutlinedTextField(
                                                value = time,
                                                onValueChange = { time = it },
                                                label = { Text("Time (e.g. 10:00 - 11:00)") },
                                                placeholder = { Text("10:00 - 11:00") },
                                                singleLine = true
                                        )
                                }
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                if (date.isNotBlank() && time.isNotBlank()) {
                                                        scope.launch {
                                                                val newSlot =
                                                                        com.example.healthmate.model
                                                                                .Slot(
                                                                                        doctorId =
                                                                                                doctorId,
                                                                                        doctorName =
                                                                                                name, // Use current name
                                                                                        date = date,
                                                                                        time = time,
                                                                                        isBooked =
                                                                                                false
                                                                                )
                                                                FirestoreHelper.addSlot(newSlot)
                                                                showAddSlotDialog = false
                                                                slots =
                                                                        FirestoreHelper
                                                                                .getSlotsByDoctor(
                                                                                        doctorId
                                                                                )
                                                                Toast.makeText(
                                                                                context,
                                                                                "Slot added!",
                                                                                Toast.LENGTH_SHORT
                                                                        )
                                                                        .show()
                                                        }
                                                }
                                        }
                                ) { Text("Add") }
                        },
                        dismissButton = {
                                TextButton(onClick = { showAddSlotDialog = false }) {
                                        Text("Cancel")
                                }
                        }
                )
        }
}
