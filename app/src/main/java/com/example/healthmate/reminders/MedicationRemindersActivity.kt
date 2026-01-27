package com.example.healthmate.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.example.healthmate.model.Reminder
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.Purple40
import java.util.*
import kotlinx.coroutines.launch

class MedicationRemindersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HealthMateTheme { MedicationRemindersScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationRemindersScreen() {
    val context = LocalContext.current
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadReminders() {
        coroutineScope.launch {
            isLoading = true
            reminders = FirestoreHelper.getUserReminders(FirebaseAuthHelper.getCurrentUserId())
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadReminders() }

    // Add reminder dialog
    if (showAddDialog) {
        AddReminderDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { medicineName, time ->
                    coroutineScope.launch {
                        val reminder =
                                Reminder(
                                        userId = FirebaseAuthHelper.getCurrentUserId(),
                                        medicineName = medicineName,
                                        time = time
                                )
                        val result = FirestoreHelper.addReminder(reminder)
                        result.fold(
                                onSuccess = { reminderId ->
                                    // Schedule notification
                                    scheduleReminder(context, reminderId, medicineName, time)
                                    Toast.makeText(context, "Reminder added!", Toast.LENGTH_SHORT)
                                            .show()
                                    loadReminders()
                                },
                                onFailure = { error ->
                                    Toast.makeText(
                                                    context,
                                                    "Failed: ${error.message}",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
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
                        title = { Text("Medication Reminders", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = Purple40
                ) { Icon(Icons.Default.Add, "Add Reminder", tint = Color.White) }
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Purple40
                    )
                }
                reminders.isEmpty() -> {
                    Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No reminders set", fontSize = 18.sp, color = Color.Gray)
                        Text(
                                text = "Tap + to add a medication reminder",
                                fontSize = 14.sp,
                                color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reminders) { reminder ->
                            ReminderCard(
                                    reminder = reminder,
                                    onDelete = {
                                        coroutineScope.launch {
                                            FirestoreHelper.deleteReminder(reminder.id)
                                            cancelReminder(context, reminder.id)
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

@Composable
fun ReminderCard(reminder: Reminder, onDelete: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Purple40
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = reminder.medicineName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Daily at ${reminder.time}", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var medicineName by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Time Picker
    val timePickerDialog =
            android.app.TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val amPm = if (hourOfDay < 12) "AM" else "PM"
                        val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                        // Store as HH:mm for processing, or format nicely for display
                        // We'll store as HH:mm (24h) for easy sorting, but format for display if
                        // needed
                        // For now, let's stick to 24h format for simplicity in backend, or update
                        // Model
                        val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                        selectedTime = formattedTime
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false // false for AM/PM mode
            )

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Medication Reminder") },
            text = {
                Column {
                    OutlinedTextField(
                            value = medicineName,
                            onValueChange = { medicineName = it },
                            label = { Text("Medicine Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                            value = selectedTime,
                            onValueChange = {},
                            label = { Text("Time") },
                            placeholder = { Text("Select Time") },
                            modifier =
                                    Modifier.fillMaxWidth().clickable { timePickerDialog.show() },
                            enabled = false, // Disable typing, force picker
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor =
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                    )
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            if (medicineName.isNotBlank() && selectedTime.isNotBlank()) {
                                onAdd(medicineName, selectedTime)
                            }
                        },
                        enabled = medicineName.isNotBlank() && selectedTime.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Schedule alarm for reminder
fun scheduleReminder(context: Context, reminderId: String, medicineName: String, time: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent =
            Intent(context, ReminderReceiver::class.java).apply {
                putExtra("reminderId", reminderId)
                putExtra("medicineName", medicineName)
            }

    val pendingIntent =
            PendingIntent.getBroadcast(
                    context,
                    reminderId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

    // Parse time
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val calendar =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)

                // If time has passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

    // Set repeating alarm (daily)
    alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
    )
}

fun cancelReminder(context: Context, reminderId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent =
            PendingIntent.getBroadcast(
                    context,
                    reminderId.hashCode(),
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
    pendingIntent?.let { alarmManager.cancel(it) }
}
