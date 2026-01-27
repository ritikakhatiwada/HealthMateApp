package com.example.healthmate.appointments

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Appointment
import com.example.healthmate.model.Slot
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.Purple40
import kotlinx.coroutines.launch

class SlotSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val doctorId = intent.getStringExtra("doctorId") ?: ""
        val doctorName = intent.getStringExtra("doctorName") ?: ""
        val doctorSpecialization = intent.getStringExtra("doctorSpecialization") ?: ""

        enableEdgeToEdge()
        setContent {
            HealthMateTheme { SlotSelectionScreen(doctorId, doctorName, doctorSpecialization) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotSelectionScreen(doctorId: String, doctorName: String, doctorSpecialization: String) {
    val context = LocalContext.current
    var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isBooking by remember { mutableStateOf(false) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf<Slot?>(null) }
    var nearestSlot by remember { mutableStateOf<Slot?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadSlots() {
        coroutineScope.launch {
            isLoading = true
            slots = FirestoreHelper.getSlotsByDoctor(doctorId)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadSlots() }

    fun bookSlot(slot: Slot) {
        coroutineScope.launch {
            isBooking = true
            val appointment =
                    Appointment(
                            patientId = FirebaseAuthHelper.getCurrentUserId(),
                            doctorId = doctorId,
                            slotId = slot.id,
                            doctorName = doctorName,
                            date = slot.date,
                            time = slot.time
                    )
            val result = FirestoreHelper.bookAppointment(appointment)
            isBooking = false

            result.fold(
                    onSuccess = {
                        Toast.makeText(
                                        context,
                                        "Appointment booked successfully!",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                        (context as? ComponentActivity)?.finish()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "Failed: ${error.message}", Toast.LENGTH_SHORT)
                                .show()
                    }
            )
        }
    }

    // Conflict dialog
    if (showConflictDialog && nearestSlot != null) {
        AlertDialog(
                onDismissRequest = { showConflictDialog = false },
                title = { Text("Slot Unavailable") },
                text = {
                    Column {
                        Text("This slot is already booked.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Would you like to book the nearest available slot?")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Date: ${nearestSlot?.date}", fontWeight = FontWeight.Bold)
                        Text(text = "Time: ${nearestSlot?.time}", fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(
                            onClick = {
                                showConflictDialog = false
                                nearestSlot?.let { bookSlot(it) }
                            }
                    ) { Text("Book This Slot") }
                },
                dismissButton = {
                    TextButton(onClick = { showConflictDialog = false }) { Text("Cancel") }
                }
        )
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Column {
                                Text("Select Time Slot", color = Color.White, fontSize = 18.sp)
                                Text(
                                        doctorName,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40)
                )
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading || isBooking -> {
                    CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Purple40
                    )
                }
                slots.isEmpty() -> {
                    Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No slots available", fontSize = 18.sp, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(slots) { slot ->
                            SlotCard(slot = slot) {
                                if (slot.isBooked) {
                                    // Find nearest available slot
                                    nearestSlot = slots.firstOrNull { !it.isBooked }
                                    if (nearestSlot != null) {
                                        showConflictDialog = true
                                    } else {
                                        Toast.makeText(
                                                        context,
                                                        "No available slots",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                    }
                                } else {
                                    bookSlot(slot)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotCard(slot: Slot, onClick: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = if (slot.isBooked) Color.LightGray else Color.White
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector =
                            if (slot.isBooked) Icons.Default.EventBusy
                            else Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (slot.isBooked) Color.Gray else Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = slot.date, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = slot.time, fontSize = 14.sp, color = Color.Gray)
            }
            if (slot.isBooked) {
                Text(text = "Booked", color = Color.Red, fontSize = 12.sp)
            } else {
                Text(text = "Available", color = Color(0xFF4CAF50), fontSize = 12.sp)
            }
        }
    }
}
