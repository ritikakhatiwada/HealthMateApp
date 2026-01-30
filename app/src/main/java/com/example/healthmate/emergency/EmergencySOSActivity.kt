package com.example.healthmate.emergency

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.data.Hospital
import com.example.healthmate.hospitals.HospitalRepository
import com.example.healthmate.model.EmergencyContact
import com.example.healthmate.ui.theme.*
import com.example.healthmate.util.LocationHelper
import com.example.healthmate.util.ThemeManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmergencySOSActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) {
                EnhancedEmergencySOSScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EnhancedEmergencySOSScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State variables
    var contacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("User") }
    var userLocation by remember { mutableStateOf("Detecting location...") }
    var userLatitude by remember { mutableDoubleStateOf(0.0) }
    var userLongitude by remember { mutableDoubleStateOf(0.0) }
    var nearestHospital by remember { mutableStateOf<Hospital?>(null) }
    var sosActivated by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableIntStateOf(5) }
    var showHelpOnWay by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val locationHelper = remember { LocationHelper(context) }
    val hospitalRepository = remember { HospitalRepository(context) }

    // Permissions
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
        )
    )

    // Phone permission launcher
    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:108"))
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
            context.startActivity(intent)
        }
    }

    // Load data
    LaunchedEffect(Unit) {
        scope.launch {
            // Get user info
            val userId = FirebaseAuthHelper.getCurrentUserId()
            val user = FirestoreHelper.getUserById(userId)
            userName = user?.name ?: "User"

            // Get emergency contacts
            contacts = FirestoreHelper.getEmergencyContacts()

            // Get location
            if (locationHelper.hasLocationPermission()) {
                try {
                    val location = locationHelper.getCurrentLocation()
                    location?.let {
                        userLatitude = it.latitude
                        userLongitude = it.longitude
                        userLocation = locationHelper.getAddressFromLocation(it.latitude, it.longitude)

                        // Get nearest hospital
                        nearestHospital = hospitalRepository.getSampleHospitals(it.latitude, it.longitude).firstOrNull()
                    }
                } catch (e: Exception) {
                    userLocation = "Location unavailable"
                }
            } else {
                userLocation = "Enable location"
            }

            isLoading = false
        }
    }

    // SOS Countdown
    LaunchedEffect(sosActivated) {
        if (sosActivated) {
            countdownValue = 5
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            // Trigger emergency actions
            triggerEmergency(
                context = context,
                userName = userName,
                userLocation = userLocation,
                latitude = userLatitude,
                longitude = userLongitude,
                contacts = contacts,
                locationHelper = locationHelper
            )
            showHelpOnWay = true
            sosActivated = false
        }
    }

    // Make call function
    fun makeCall(phoneNumber: String) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED -> {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
                context.startActivity(intent)
            }
            else -> {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                context.startActivity(intent)
            }
        }
    }

    // Emergency SOS Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AlertRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Confirm Emergency SOS",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        "This will:",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("• Call emergency services (108)")
                    Text("• Send SMS to your emergency contacts")
                    Text("• Share your current location")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Only use this for real emergencies.",
                        fontWeight = FontWeight.Bold,
                        color = AlertRed
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        sosActivated = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRed
                    )
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm SOS", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Emergency SOS",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AlertRed,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (showHelpOnWay) {
            // Help is on the way screen
            HelpOnWayScreen(
                nearestHospital = nearestHospital,
                userLatitude = userLatitude,
                userLongitude = userLongitude,
                onDismiss = { showHelpOnWay = false },
                onDirections = { hospital ->
                    val uri = Uri.parse(
                        "https://www.google.com/maps/dir/?api=1" +
                                "&origin=$userLatitude,$userLongitude" +
                                "&destination=${hospital.latitude},${hospital.longitude}" +
                                "&travelmode=driving"
                    )
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Location Card
                item {
                    LocationDisplayCard(
                        location = userLocation,
                        isLoading = isLoading
                    )
                }

                // Large SOS Button
                item {
                    SOSButton(
                        isActivated = sosActivated,
                        countdownValue = countdownValue,
                        onActivate = {
                            if (!permissions.allPermissionsGranted) {
                                permissions.launchMultiplePermissionRequest()
                            } else {
                                // Show confirmation dialog before activating SOS
                                showConfirmDialog = true
                            }
                        },
                        onCancel = { sosActivated = false }
                    )
                }

                // Quick Actions
                item {
                    Text(
                        "Quick Emergency Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    QuickEmergencyActions(
                        onAmbulance = { makeCall("108") },
                        onPolice = { makeCall("100") },
                        onFireBrigade = { makeCall("101") }
                    )
                }

                // Nearest Hospital
                if (nearestHospital != null) {
                    item {
                        Text(
                            "Nearest Hospital",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        NearestHospitalCard(
                            hospital = nearestHospital!!,
                            onCall = { makeCall(nearestHospital!!.phoneNumber) },
                            onDirections = {
                                val uri = Uri.parse(
                                    "https://www.google.com/maps/dir/?api=1" +
                                            "&origin=$userLatitude,$userLongitude" +
                                            "&destination=${nearestHospital!!.latitude},${nearestHospital!!.longitude}" +
                                            "&travelmode=driving"
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        )
                    }
                }

                // Emergency Contacts
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Emergency Contacts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${contacts.size} contacts",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (contacts.isEmpty()) {
                    item {
                        EmptyContactsCard()
                    }
                } else {
                    items(contacts) { contact ->
                        EmergencyContactCard(
                            contact = contact,
                            onClick = { makeCall(contact.number) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationDisplayCard(location: String, isLoading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Your Location",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (isLoading) {
                    Text(
                        "Detecting location...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        location,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SOSButton(
    isActivated: Boolean,
    countdownValue: Int,
    onActivate: () -> Unit,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Outer pulse ring
            if (isActivated) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(AlertRed.copy(alpha = 0.2f))
                )
            }

            // SOS Button
            Surface(
                onClick = if (isActivated) onCancel else onActivate,
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                color = AlertRed,
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isActivated) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                countdownValue.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 56.sp
                            )
                            Text(
                                "TAP TO CANCEL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "SOS",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            if (isActivated) "Sending emergency alert..." else "Tap to activate emergency SOS",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActivated) AlertRed else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isActivated) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun QuickEmergencyActions(
    onAmbulance: () -> Unit,
    onPolice: () -> Unit,
    onFireBrigade: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.LocalHospital,
            label = "Ambulance",
            number = "108",
            color = AlertRed,
            onClick = onAmbulance,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Default.LocalPolice,
            label = "Police",
            number = "100",
            color = Color(0xFF3F51B5),
            onClick = onPolice,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Default.LocalFireDepartment,
            label = "Fire",
            number = "101",
            color = Color(0xFFFF5722),
            onClick = onFireBrigade,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    number: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Text(
                number,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NearestHospitalCard(
    hospital: Hospital,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        hospital.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        hospital.formattedDistance,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SafeGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        "Open",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SafeGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call")
                }
                Button(
                    onClick = onDirections,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Directions, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Directions")
                }
            }
        }
    }
}

@Composable
private fun EmptyContactsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.ContactPhone,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No emergency contacts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Contact admin to add numbers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EmergencyContactCard(contact: EmergencyContact, onClick: () -> Unit) {
    val iconColor = when (contact.type.lowercase()) {
        "ambulance" -> AlertRed
        "hospital" -> Color(0xFF2196F3)
        "police" -> Color(0xFF3F51B5)
        else -> Color(0xFFFF9800)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when (contact.type.lowercase()) {
                        "ambulance" -> Icons.Default.LocalHospital
                        "hospital" -> Icons.Default.MedicalServices
                        "police" -> Icons.Default.LocalPolice
                        else -> Icons.Default.Phone
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = contact.number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = CircleShape,
                color = SafeGreen,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.White,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun HelpOnWayScreen(
    nearestHospital: Hospital?,
    userLatitude: Double,
    userLongitude: Double,
    onDismiss: () -> Unit,
    onDirections: (Hospital) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SafeGreen.copy(alpha = 0.9f),
                        SafeGreen
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Help is on the way!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Emergency services have been notified.\nSMS with your location has been sent to your emergency contacts.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            if (nearestHospital != null) {
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Nearest Hospital",
                            style = MaterialTheme.typography.labelMedium,
                            color = SafeGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            nearestHospital.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            nearestHospital.formattedDistance,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onDirections(nearestHospital) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                        ) {
                            Icon(Icons.Default.Directions, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Get Directions")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
            ) {
                Text("Back to Emergency Screen", modifier = Modifier.padding(8.dp))
            }
        }
    }
}

// Trigger emergency function
private fun triggerEmergency(
    context: android.content.Context,
    userName: String,
    userLocation: String,
    latitude: Double,
    longitude: Double,
    contacts: List<EmergencyContact>,
    locationHelper: LocationHelper
) {
    // 1. Call ambulance
    try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
            PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:108"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 2. Send SMS to emergency contacts
    try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED) {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val message = LocationHelper.formatEmergencySms(userName, userLocation, latitude, longitude)

            contacts.forEach { contact ->
                try {
                    smsManager.sendTextMessage(
                        contact.number,
                        null,
                        message,
                        null,
                        null
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
