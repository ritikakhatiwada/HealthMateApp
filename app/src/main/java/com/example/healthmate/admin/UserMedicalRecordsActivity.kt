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
import com.example.healthmate.model.MedicalRecord
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
        setContent { HealthMateTheme { UserMedicalRecordsScreen(userId, userName) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMedicalRecordsScreen(userId: String, userName: String) {
    val context = LocalContext.current
    var records by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            records = FirestoreHelper.getUserMedicalRecords(userId)
            isLoading = false
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Column {
                                Text("Medical Records", color = Color.White, fontSize = 18.sp)
                                Text(
                                        userName,
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
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color(0xFF1976D2)
                                )
                )
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF1976D2)
                    )
                }
                records.isEmpty() -> {
                    Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No medical records", fontSize = 18.sp, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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

@Composable
fun AdminRecordCard(record: MedicalRecord, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Red
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = record.fileName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                )
                Text(
                        text = dateFormat.format(Date(record.uploadedAt)),
                        fontSize = 12.sp,
                        color = Color.Gray
                )
            }
            Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open",
                    tint = Color(0xFF1976D2)
            )
        }
    }
}
