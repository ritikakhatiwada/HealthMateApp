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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.healthmate.BuildConfig
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.MedicalRecord
import com.example.healthmate.ui.components.EmptyState
import com.example.healthmate.ui.components.HealthMateFAB
import com.example.healthmate.ui.components.HealthMateTextField
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.components.LoadingState
import com.example.healthmate.ui.components.MedicalRecordsSkeleton
import com.example.healthmate.ui.components.MedicalRecordListItem
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.Spacing
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
    var allRecords by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var filteredRecords by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadRecords() {
        coroutineScope.launch {
            isLoading = true
            allRecords = FirestoreHelper.getUserMedicalRecords(FirebaseAuthHelper.getCurrentUserId())
            filteredRecords = allRecords
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadRecords() }

    // Filter records based on search query
    LaunchedEffect(searchQuery, allRecords) {
        filteredRecords = if (searchQuery.isEmpty()) {
            allRecords
        } else {
            allRecords.filter { record ->
                record.fileName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
            HealthMateTopBar(
                title = "Medical Records",
                subtitle = "Your health documents",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { (context as? ComponentActivity)?.finish() }
            )
        },
        floatingActionButton = {
            HealthMateFAB(
                icon = Icons.Default.Add,
                contentDescription = "Upload Medical Record",
                onClick = { pdfPicker.launch("application/pdf") }
            )
        }
    ) { padding ->
        if (isLoading) {
            MedicalRecordsSkeleton()
        } else if (allRecords.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Description,
                title = "No Medical Records",
                message = "Upload your medical documents for easy access anytime. Supported format: PDF",
                actionLabel = "Upload Record",
                onAction = { pdfPicker.launch("application/pdf") }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                ) {
                    HealthMateTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search",
                        placeholder = "Search by file name",
                        leadingIcon = Icons.Default.Search,
                        imeAction = ImeAction.Search
                    )
                }

                // Records Grid
                if (filteredRecords.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = "No Results Found",
                            message = "No records match your search query."
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(filteredRecords) { record ->
                            MedicalRecordListItem(
                                record = record,
                                onClick = {
                                    // Open PDF in browser/viewer
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(record.fileUrl))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// RecordCard removed - now using MedicalRecordListItem from ui.components package

// Cloudinary upload function
suspend fun uploadToCloudinary(file: File): String? {
    return withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("Cloudinary", "========================================")
            android.util.Log.d("Cloudinary", "Uploading file: ${file.name}")
            android.util.Log.d("Cloudinary", "File size: ${file.length()} bytes")

            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            val apiKey = BuildConfig.CLOUDINARY_API_KEY
            val apiSecret = BuildConfig.CLOUDINARY_API_SECRET

            android.util.Log.d("Cloudinary", "Cloud name: $cloudName")
            android.util.Log.d("Cloudinary", "API key configured: ${apiKey.isNotEmpty()}")

            if (cloudName.isEmpty() || apiKey.isEmpty()) {
                android.util.Log.e("Cloudinary", "Missing Cloudinary credentials!")
                return@withContext null
            }

            val client = OkHttpClient()
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            // Create signature with type parameter for public access
            val signatureString = "timestamp=$timestamp&type=upload$apiSecret"
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
                            .addFormDataPart("type", "upload")  // Makes file publicly accessible
                            .build()

            val request =
                    Request.Builder()
                            .url("https://api.cloudinary.com/v1_1/$cloudName/raw/upload")
                            .post(requestBody)
                            .build()

            android.util.Log.d("Cloudinary", "Sending upload request...")
            val response = client.newCall(request).execute()

            android.util.Log.d("Cloudinary", "Response code: ${response.code}")
            android.util.Log.d("Cloudinary", "Response message: ${response.message}")

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                android.util.Log.d("Cloudinary", "Response body: $responseBody")

                val jsonResponse = JSONObject(responseBody)
                val secureUrl = jsonResponse.getString("secure_url")

                android.util.Log.d("Cloudinary", "Upload successful!")
                android.util.Log.d("Cloudinary", "Secure URL: $secureUrl")
                android.util.Log.d("Cloudinary", "========================================")

                secureUrl
            } else {
                val errorBody = response.body?.string()
                android.util.Log.e("Cloudinary", "Upload failed!")
                android.util.Log.e("Cloudinary", "Error body: $errorBody")
                android.util.Log.d("Cloudinary", "========================================")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("Cloudinary", "Exception during upload: ${e.message}", e)
            android.util.Log.d("Cloudinary", "========================================")
            null
        }
    }
}
