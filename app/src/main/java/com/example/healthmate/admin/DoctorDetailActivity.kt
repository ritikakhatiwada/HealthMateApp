package com.example.healthmate.admin

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Doctor
import com.example.healthmate.model.Slot
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.CloudinaryHelper
import com.example.healthmate.util.ThemeManager
import com.example.healthmate.util.FirestoreMigration
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DoctorDetailActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val doctorId = intent.getStringExtra("doctorId") ?: ""
                enableEdgeToEdge()
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
        var showDeleteConfirm by remember { mutableStateOf(false) }
        var isDeletingDoctor by remember { mutableStateOf(false) }
        var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }

        // Editable State
        var profilePictureUrl by remember { mutableStateOf<String?>(null) }
        var isUploadingImage by remember { mutableStateOf(false) }

        // Image Picker
        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                scope.launch {
                    isUploadingImage = true
                    try {
                        val inputStream = context.contentResolver.openInputStream(selectedUri)
                        val tempFile = File(context.cacheDir, "doctor_${System.currentTimeMillis()}.jpg")
                        FileOutputStream(tempFile).use { output -> inputStream?.copyTo(output) }
                        inputStream?.close()

                        val url = CloudinaryHelper.uploadProfileImage(tempFile)
                        if (url != null) {
                            FirestoreHelper.updateDoctor(doctorId, mapOf("profilePicture" to url))
                            profilePictureUrl = url
                            Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                        }
                        tempFile.delete()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    isUploadingImage = false
                }
            }
        }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") } // Often read-only
        var specialization by remember { mutableStateOf("") }
        var experience by remember { mutableStateOf("") }
        var contactNumber by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        LaunchedEffect(doctorId) {
                scope.launch {
                        // Run migration first to ensure old slots work
                        FirestoreMigration.migrateSlotFields()

                        doctor = FirestoreHelper.getDoctorById(doctorId)
                        doctor?.let {
                                name = it.name
                                email = it.email
                                specialization = it.specialization
                                experience = it.experience
                                contactNumber = it.contactNumber
                                description = it.description
                                profilePictureUrl = it.profilePicture.ifEmpty { null }
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
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                },
                floatingActionButton = {
                        if (!isLoading && doctor != null) {
                                FloatingActionButton(
                                        onClick = { showDeleteConfirm = true },
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ) {
                                        Icon(Icons.Default.Delete, "Delete Doctor")
                                }
                        }
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
                                // Avatar / Profile Picture
                                Box(
                                        modifier =
                                                Modifier.size(120.dp)
                                                        .clickable(enabled = isEditing && !isUploadingImage) {
                                                                imagePicker.launch("image/*")
                                                        },
                                        contentAlignment = Alignment.Center
                                ) {
                                        Surface(
                                                shape = CircleShape,
                                                modifier = Modifier.fillMaxSize(),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                                        ) {
                                                when {
                                                        isUploadingImage -> {
                                                                CircularProgressIndicator(
                                                                        modifier = Modifier.size(40.dp).padding(20.dp),
                                                                        color = MaterialTheme.colorScheme.primary
                                                                )
                                                        }
                                                        profilePictureUrl != null -> {
                                                                AsyncImage(
                                                                        model = ImageRequest.Builder(context)
                                                                                .data(profilePictureUrl)
                                                                                .crossfade(true)
                                                                                .build(),
                                                                        contentDescription = "Doctor Image",
                                                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                                        contentScale = ContentScale.Crop
                                                                )
                                                        }
                                                        else -> {
                                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                                        Text(
                                                                                text = name.take(2).uppercase(),
                                                                                style = MaterialTheme.typography.headlineLarge,
                                                                                color = MaterialTheme.colorScheme.primary
                                                                        )
                                                                }
                                                        }
                                                }
                                        }

                                        if (isEditing) {
                                                Surface(
                                                        modifier = Modifier.align(Alignment.BottomEnd).size(36.dp),
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shadowElevation = 4.dp
                                                ) {
                                                        Icon(
                                                                Icons.Default.CameraAlt,
                                                                null,
                                                                tint = Color.White,
                                                                modifier = Modifier.padding(8.dp)
                                                        )
                                                }
                                        }
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
                                                                                                        val result = FirestoreHelper
                                                                                                                .deleteSlot(
                                                                                                                        slot.id
                                                                                                                )
                                                                                                        result.fold(
                                                                                                                onSuccess = {
                                                                                                                        // Refresh slots
                                                                                                                        slots =
                                                                                                                                FirestoreHelper
                                                                                                                                        .getSlotsByDoctor(
                                                                                                                                                doctorId
                                                                                                                                        )
                                                                                                                        Toast.makeText(
                                                                                                                                context,
                                                                                                                                "Slot deleted",
                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                        ).show()
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
                AddSlotDialog(
                        doctorId = doctorId,
                        doctorName = name,
                        onDismiss = { showAddSlotDialog = false },
                        onSlotAdded = {
                                scope.launch {
                                        slots = FirestoreHelper.getSlotsByDoctor(doctorId)
                                }
                                showAddSlotDialog = false
                        }
                )
        }

        if (showDeleteConfirm) {
                AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete Doctor Profile") },
                        text = { Text("Are you sure you want to permanently delete Dr. $name? This will also remove all associated time slots.") },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                isDeletingDoctor = true
                                                scope.launch {
                                                        val result = FirestoreHelper.deleteDoctor(doctorId)
                                                        isDeletingDoctor = false
                                                        result.fold(
                                                                onSuccess = {
                                                                        Toast.makeText(context, "Doctor deleted", Toast.LENGTH_SHORT).show()
                                                                        onBack()
                                                                },
                                                                onFailure = { e ->
                                                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                }
                                                        )
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                        if (isDeletingDoctor) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                        else Text("Delete")
                                }
                        },
                        dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                        }
                )
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSlotDialog(
        doctorId: String,
        doctorName: String,
        onDismiss: () -> Unit,
        onSlotAdded: () -> Unit
) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var selectedDate by remember { mutableStateOf("") }
        var selectedStartHour by remember { mutableStateOf(9) }
        var selectedStartMinute by remember { mutableStateOf(0) }
        var selectedEndHour by remember { mutableStateOf(10) }
        var selectedEndMinute by remember { mutableStateOf(0) }
        var showDatePicker by remember { mutableStateOf(false) }
        var showStartTimePicker by remember { mutableStateOf(false) }
        var showEndTimePicker by remember { mutableStateOf(false) }

        val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = System.currentTimeMillis()
        )

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Add Slot") },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Date Picker Button
                                OutlinedButton(
                                        onClick = { showDatePicker = true },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                text = if (selectedDate.isEmpty())
                                                        "Select Date"
                                                else "Date: $selectedDate"
                                        )
                                }

                                // Start Time Picker Button
                                OutlinedButton(
                                        onClick = { showStartTimePicker = true },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text("Start Time: ${String.format("%02d:%02d", selectedStartHour, selectedStartMinute)}")
                                }

                                // End Time Picker Button
                                OutlinedButton(
                                        onClick = { showEndTimePicker = true },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text("End Time: ${String.format("%02d:%02d", selectedEndHour, selectedEndMinute)}")
                                }

                                if (selectedDate.isNotEmpty()) {
                                        Text(
                                                text = "Slot: $selectedDate at ${String.format("%02d:%02d", selectedStartHour, selectedStartMinute)} - ${String.format("%02d:%02d", selectedEndHour, selectedEndMinute)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = {
                                        if (selectedDate.isNotEmpty()) {
                                                val timeRange = "${String.format("%02d:%02d", selectedStartHour, selectedStartMinute)} - ${String.format("%02d:%02d", selectedEndHour, selectedEndMinute)}"
                                                scope.launch {
                                                        val newSlot = Slot(
                                                                doctorId = doctorId,
                                                                doctorName = doctorName,
                                                                date = selectedDate,
                                                                time = timeRange,
                                                                isBooked = false
                                                        )
                                                        val result = FirestoreHelper.addSlot(newSlot)
                                                        result.fold(
                                                                onSuccess = {
                                                                        Toast.makeText(
                                                                                context,
                                                                                "Slot added successfully!",
                                                                                Toast.LENGTH_SHORT
                                                                        ).show()
                                                                        onSlotAdded()
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
                                        } else {
                                                Toast.makeText(
                                                        context,
                                                        "Please select a date",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        }
                                },
                                enabled = selectedDate.isNotEmpty()
                        ) { Text("Add") }
                },
                dismissButton = {
                        TextButton(onClick = onDismiss) {
                                Text("Cancel")
                        }
                }
        )

        // Date Picker Dialog
        if (showDatePicker) {
                DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                        val calendar = Calendar.getInstance().apply {
                                                                timeInMillis = millis
                                                        }
                                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                        selectedDate = dateFormat.format(calendar.time)
                                                }
                                                showDatePicker = false
                                        }
                                ) { Text("OK") }
                        },
                        dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                        Text("Cancel")
                                }
                        }
                ) {
                        DatePicker(state = datePickerState)
                }
        }

        // Start Time Picker Dialog
        if (showStartTimePicker) {
                TimePickerDialog(
                        onDismiss = { showStartTimePicker = false },
                        onConfirm = { hour, minute ->
                                selectedStartHour = hour
                                selectedStartMinute = minute
                                showStartTimePicker = false
                        },
                        initialHour = selectedStartHour,
                        initialMinute = selectedStartMinute,
                        title = "Select Start Time"
                )
        }

        // End Time Picker Dialog
        if (showEndTimePicker) {
                TimePickerDialog(
                        onDismiss = { showEndTimePicker = false },
                        onConfirm = { hour, minute ->
                                selectedEndHour = hour
                                selectedEndMinute = minute
                                showEndTimePicker = false
                        },
                        initialHour = selectedEndHour,
                        initialMinute = selectedEndMinute,
                        title = "Select End Time"
                )
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
        onDismiss: () -> Unit,
        onConfirm: (hour: Int, minute: Int) -> Unit,
        initialHour: Int,
        initialMinute: Int,
        title: String
) {
        val timePickerState = rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute,
                is24Hour = false
        )

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(title) },
                text = {
                        TimePicker(state = timePickerState)
                },
                confirmButton = {
                        TextButton(
                                onClick = {
                                        onConfirm(timePickerState.hour, timePickerState.minute)
                                }
                        ) { Text("OK") }
                },
                dismissButton = {
                        TextButton(onClick = onDismiss) {
                                Text("Cancel")
                        }
                }
        )
}
