package com.example.healthmate.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import com.example.healthmate.model.MedicalRecord
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.ui.theme.HealthMateTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class UserMedicalRecordsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("userId") ?: ""
        val userName = intent.getStringExtra("userName") ?: "User"

        enableEdgeToEdge()
        setContent {
            val themeManager = com.example.healthmate.util.ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) { UserMedicalRecordsScreen(userId, userName) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMedicalRecordsScreen(userId: String, userName: String) {
    val context = LocalContext.current
    var records by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var userProfile by remember { mutableStateOf<com.example.healthmate.model.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            userProfile = FirestoreHelper.getUserById(userId)
            records = FirestoreHelper.getUserMedicalRecords(userId)
            isLoading = false
        }
    }

    Scaffold(
            topBar = {
                HealthMateTopBar(
                        title = "Medical Records",
                        subtitle = userName,
                        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        onNavigationClick = { (context as? ComponentActivity)?.finish() }
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
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        // User Profile Header
                        item {
                            UserProfileHeader(userProfile, userName)
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            Text(
                                "Medical History",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (records.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.md))
                                    Text(
                                        text = "No medical records found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(records) { record ->
                                AdminRecordCard(record = record) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(record.fileUrl))
                                    context.startActivity(intent)
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
fun AdminRecordCard(record: MedicalRecord, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = HealthMateShapes.CardLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = record.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                )
                Text(
                        text = dateFormat.format(Date(record.uploadedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun UserProfileHeader(user: com.example.healthmate.model.User?, defaultName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HealthMateShapes.CardLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (user?.name ?: defaultName).take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = user?.name ?: defaultName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user?.email ?: "No email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem(modifier = Modifier.weight(1f), label = "Age", value = user?.age?.ifEmpty { "N/A" } ?: "N/A")
                InfoItem(modifier = Modifier.weight(1f), label = "Gender", value = user?.gender?.ifEmpty { "N/A" } ?: "N/A")
                InfoItem(modifier = Modifier.weight(1f), label = "Blood", value = user?.bloodGroup?.ifEmpty { "N/A" } ?: "N/A")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoItem(label = "Phone", value = user?.phoneNumber?.ifEmpty { "N/A" } ?: "N/A")
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem(label = "Address", value = user?.address?.ifEmpty { "N/A" } ?: "N/A")
        }
    }
}

@Composable
fun InfoItem(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
