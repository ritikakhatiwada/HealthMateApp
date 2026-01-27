package com.example.healthmate.emergency

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.EmergencyContact
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

class EmergencySOSActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) { EmergencySOSScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencySOSScreen() {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var phoneToCall by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Permission launcher
    val phonePermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isGranted ->
                if (isGranted) {
                    phoneToCall?.let { number ->
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                        context.startActivity(intent)
                    }
                } else {
                    // Fall back to dialer
                    phoneToCall?.let { number ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        context.startActivity(intent)
                    }
                }
            }

    fun makeCall(phoneNumber: String) {
        phoneToCall = phoneNumber
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED -> {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
                context.startActivity(intent)
            }
            else -> {
                // Use ACTION_DIAL as fallback (doesn't require permission)
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            contacts = FirestoreHelper.getEmergencyContacts()
            isLoading = false
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Emergency SOS", color = Color.White) },
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
                        Text(
                                text = "Contact admin to add numbers",
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
                        items(contacts) { contact ->
                            EmergencyContactCard(contact = contact) { makeCall(contact.number) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyContactCard(contact: EmergencyContact, onClick: () -> Unit) {
    val iconColor =
            when (contact.type.lowercase()) {
                "ambulance" -> Color(0xFFF44336)
                "hospital" -> Color(0xFF2196F3)
                "police" -> Color(0xFF3F51B5)
                else -> Color(0xFFFF9800)
            }

    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector =
                            when (contact.type.lowercase()) {
                                "ambulance" -> Icons.Default.LocalHospital
                                "hospital" -> Icons.Default.MedicalServices
                                "police" -> Icons.Default.LocalPolice
                                else -> Icons.Default.Phone
                            },
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = iconColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = contact.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = contact.number, fontSize = 16.sp, color = Color.Gray)
                Text(text = contact.type, fontSize = 12.sp, color = iconColor)
            }
            Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
            )
        }
    }
}
