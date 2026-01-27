package com.example.healthmate.admin

import android.os.Bundle
import android.widget.Toast
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
import com.example.healthmate.model.EmergencyContact
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

class AdminEmergencyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) { AdminEmergencyScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEmergencyScreen() {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadContacts() {
        coroutineScope.launch {
            isLoading = true
            contacts = FirestoreHelper.getEmergencyContacts()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadContacts() }

    // Add Contact Dialog
    if (showAddDialog) {
        AddEmergencyContactDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, number, type ->
                    coroutineScope.launch {
                        val contact = EmergencyContact(name = name, number = number, type = type)
                        val result = FirestoreHelper.addEmergencyContact(contact)
                        result.fold(
                                onSuccess = {
                                    Toast.makeText(context, "Contact added!", Toast.LENGTH_SHORT)
                                            .show()
                                    loadContacts()
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
                        title = { Text("Emergency Contacts", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary
                ) { Icon(Icons.Default.Add, "Add Contact", tint = Color.White) }
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                    )
                }
                contacts.isEmpty() -> {
                    Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No emergency contacts", fontSize = 18.sp, color = Color.Gray)
                        Text(text = "Tap + to add a contact", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(contacts) { contact ->
                            AdminEmergencyCard(
                                    contact = contact,
                                    onDelete = {
                                        coroutineScope.launch {
                                            FirestoreHelper.deleteEmergencyContact(contact.id)
                                            loadContacts()
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
fun AdminEmergencyCard(contact: EmergencyContact, onDelete: () -> Unit) {
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
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFFF44336)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = contact.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = contact.number, fontSize = 14.sp, color = Color.Gray)
                Text(text = contact.type, fontSize = 12.sp, color = Color(0xFFF44336))
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
fun AddEmergencyContactDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("SOS") }
    val types = listOf("SOS", "Ambulance", "Hospital", "Police", "Other")

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Emergency Contact") },
            text = {
                Column {
                    OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                            value = number,
                            onValueChange = { number = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Type", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        types.take(3).forEach { t ->
                            FilterChip(
                                    selected = type == t,
                                    onClick = { type = t },
                                    label = { Text(t, fontSize = 12.sp) }
                            )
                        }
                    }
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        types.drop(3).forEach { t ->
                            FilterChip(
                                    selected = type == t,
                                    onClick = { type = t },
                                    label = { Text(t, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onAdd(name, number, type) },
                        enabled = name.isNotBlank() && number.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
