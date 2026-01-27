package com.example.healthmate.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Appointment
import com.example.healthmate.ui.theme.HealthMateTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class AdminAppointmentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HealthMateTheme { AdminAppointmentsScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen() {
    val context = LocalContext.current
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            appointments = FirestoreHelper.getAllAppointments()
            isLoading = false
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("All Appointments", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color(0xFF9C27B0)
                                )
                )
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF9C27B0)
                    )
                }
                appointments.isEmpty() -> {
                    Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No appointments", fontSize = 18.sp, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) { items(appointments) { appointment -> AdminAppointmentCard(appointment) } }
                }
            }
        }
    }
}

@Composable
fun AdminAppointmentCard(appointment: Appointment) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFF9C27B0)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                            text = "${appointment.date} at ${appointment.time}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = "Booked: ${dateFormat.format(Date(appointment.bookedAt))}",
                            fontSize = 12.sp,
                            color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Doctor", fontSize = 12.sp, color = Color.Gray)
                    Text(text = appointment.doctorName, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "User ID", fontSize = 12.sp, color = Color.Gray)
                    Text(
                            text = appointment.patientId.take(8) + "...",
                            fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
