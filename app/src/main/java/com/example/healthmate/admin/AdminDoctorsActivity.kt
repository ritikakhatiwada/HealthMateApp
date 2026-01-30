package com.example.healthmate.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import kotlinx.coroutines.launch

class AdminDoctorsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthMateTheme {
                AdminDoctorsScreen(
                        onAddDoctor = {
                            startActivity(Intent(this, AddDoctorActivity::class.java))
                        },
                        onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorsScreen(onAddDoctor: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showSlotDialog by remember { mutableStateOf<Doctor?>(null) }
    val scope = rememberCoroutineScope()

    fun loadDoctors() {
        scope.launch {
            isLoading = true
            doctors = FirestoreHelper.getDoctors()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadDoctors() }

    Scaffold(
            topBar = {
                HealthMateTopBar(
                        title = "Doctors Management",
                        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        onNavigationClick = onBack
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddDoctor) { Icon(Icons.Default.Add, "Add") }
            }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (doctors.isEmpty()) {
                    Text("No doctors found.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(doctors) { doctor ->
                            DoctorCardWithSlots(
                                    doctor = doctor,
                                    onAddSlot = { showSlotDialog = doctor },
                                    onViewSlots = { /* TODO: View Slots */}
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Slot Dialog
    if (showSlotDialog != null) {
        AddSlotDialog(
                doctor = showSlotDialog!!,
                onDismiss = { showSlotDialog = null },
                onSave = { date, time ->
                    scope.launch {
                        val newSlot =
                                Slot(
                                        doctorId = showSlotDialog!!.id,
                                        doctorName = showSlotDialog!!.name,
                                        date = date,
                                        time = time,
                                        isBooked = false
                                )
                        FirestoreHelper.addSlot(newSlot)
                        Toast.makeText(context, "Slot Added!", Toast.LENGTH_SHORT).show()
                        showSlotDialog = null
                    }
                }
        )
    }
}

@Composable
fun DoctorCardWithSlots(doctor: Doctor, onAddSlot: () -> Unit, onViewSlots: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = HealthMateShapes.CardLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                    doctor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
            )
            Text(
                    doctor.specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                OutlinedButton(onClick = onAddSlot, modifier = Modifier.weight(1f)) {
                    Text("Add Slot")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onViewSlots, modifier = Modifier.weight(1f)) {
                    Text("View Slots")
                }
            }
        }
    }
}

@Composable
fun AddSlotDialog(doctor: Doctor, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Slot for ${doctor.name}") },
            text = {
                Column {
                    OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            placeholder = { Text("2024-02-01") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time (e.g. 10:00 - 11:00)") },
                            placeholder = { Text("10:00") }
                    )
                }
            },
            confirmButton = { Button(onClick = { onSave(date, time) }) { Text("Save") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
