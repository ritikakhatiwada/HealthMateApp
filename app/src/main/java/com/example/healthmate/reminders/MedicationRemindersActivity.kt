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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Reminder
import com.example.healthmate.ui.components.AddReminderDialog
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.components.ReminderCard
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.ReminderUtils
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
                                    ReminderUtils.scheduleReminder(context, reminderId, medicineName, time)
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
            HealthMateTopBar(
                title = "Medication",
                subtitle = "Reminders & Schedule",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { (context as? ComponentActivity)?.finish() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = CircleShape
            ) { Icon(Icons.Default.Add, "Add Reminder", modifier = Modifier.size(28.dp)) }
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
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(120.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.padding(24.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        Text(
                            text = "No Reminders Set",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Keep track of your health by adding your daily medication schedule.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setup First Reminder")
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg)) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Daily Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(reminders) { reminder ->
                                ReminderCard(
                                    reminder = reminder,
                                    onDelete = {
                                        coroutineScope.launch {
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
}

// End of file
