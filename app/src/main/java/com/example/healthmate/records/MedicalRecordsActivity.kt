package com.example.healthmate.records

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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
import com.example.healthmate.BuildConfig
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.MedicalRecord
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.Purple40
import com.example.healthmate.util.ThemeManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject

class MedicalRecordsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) { MedicalRecordsScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen() {
    val context = LocalContext.current
    var records by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadRecords() {
        coroutineScope.launch {
            isLoading = true
            records = FirestoreHelper.getUserMedicalRecords(FirebaseAuthHelper.getCurrentUserId())
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadRecords() }

    // File picker for PDF
    val pdfPicker =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
                    uri: Uri? ->
                uri?.let { selectedUri ->
                    coroutineScope.launch {
                        isUploading = true
                        try {
                            // Copy file to cache
                            val inputStream = context.contentResolver.openInputStream(selectedUri)
                            val fileName = "medical_record_${System.currentTimeMillis()}.pdf"
                            val tempFile = File(context.cacheDir, fileName)
                            FileOutputStream(tempFile).use { output -> inputStream?.copyTo(output) }
                            inputStream?.close()

                            // Upload to Cloudinary
                            val fileUrl = uploadToCloudinary(tempFile)

                            if (fileUrl != null) {
                                // Save to Firestore
                                val record =
                                        MedicalRecord(
                                                userId = FirebaseAuthHelper.getCurrentUserId(),
                                                fileUrl = fileUrl,
                                                fileName = fileName,
                                                uploadedAt = System.currentTimeMillis()
                                        )
                                val result = FirestoreHelper.addMedicalRecord(record)
                                result.fold(
                                        onSuccess = {
                                            Toast.makeText(
                                                            context,
                                                            "Record uploaded successfully!",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            loadRecords()
                                        },
                                        onFailure = { error ->
                                            Toast.makeText(
                                                            context,
                                                            "Failed to save: ${error.message}",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        }
                                )
                            } else {
                                Toast.makeText(
                                                context,
                                                "Upload failed. Check Cloudinary credentials.",
                                                Toast.LENGTH_LONG
                                        )
                                        .show()
                            }

                            // Clean up temp file
                            tempFile.delete()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                        }
                        isUploading = false
                    }
                }
            }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Medical Records", color = Color.White) },
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
                        onClick = { pdfPicker.launch("application/pdf") },
                        containerColor = Purple40
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Add, "Upload PDF", tint = Color.White)
                    }
                }
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
                        Text(text = "Tap + to upload a PDF", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(records) { record ->
                            RecordCard(record = record) {
                                // Open PDF in browser/viewer
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
fun RecordCard(record: MedicalRecord, onClick: () -> Unit) {
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
                    tint = Purple40
            )
        }
    }
}

// Cloudinary upload function
suspend fun uploadToCloudinary(file: File): String? {
    return withContext(Dispatchers.IO) {
        try {
            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            val apiKey = BuildConfig.CLOUDINARY_API_KEY
            val apiSecret = BuildConfig.CLOUDINARY_API_SECRET

            if (cloudName.isEmpty() || apiKey.isEmpty()) {
                return@withContext null
            }

            val client = OkHttpClient()
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            // Create signature
            val signatureString = "timestamp=$timestamp$apiSecret"
            val signature =
                    java.security.MessageDigest.getInstance("SHA-1")
                            .digest(signatureString.toByteArray())
                            .joinToString("") { "%02x".format(it) }

            val requestBody =
                    MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart(
                                    "file",
                                    file.name,
                                    file.asRequestBody("application/pdf".toMediaType())
                            )
                            .addFormDataPart("api_key", apiKey)
                            .addFormDataPart("timestamp", timestamp)
                            .addFormDataPart("signature", signature)
                            .build()

            val request =
                    Request.Builder()
                            .url("https://api.cloudinary.com/v1_1/$cloudName/raw/upload")
                            .post(requestBody)
                            .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                jsonResponse.getString("secure_url")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
