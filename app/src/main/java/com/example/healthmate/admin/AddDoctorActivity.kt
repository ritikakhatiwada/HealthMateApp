package com.example.healthmate.admin

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Doctor
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.CloudinaryHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AddDoctorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HealthMateTheme { AddDoctorScreen(onBack = { finish() }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDoctorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form State
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") } // Simple default
    var bloodGroup by remember { mutableStateOf("O+") }

    var isLoading by remember { mutableStateOf(false) }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Image Picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploadingImage = true
                try {
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val tempFile = File(context.cacheDir, "doctor_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(tempFile).use { output -> inputStream?.copyTo(output) }
                    inputStream?.close()

                    val url = CloudinaryHelper.uploadProfileImage(tempFile)
                    if (url != null) {
                        profilePictureUrl = url
                    } else {
                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                    tempFile.delete()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                isUploadingImage = false
            }
        }
    }

    Scaffold(
            topBar = {
                HealthMateTopBar(
                        title = "Add New Doctor",
                        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        onNavigationClick = onBack
                )
            }
    ) { padding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(padding)
                                .padding(Spacing.lg)
                                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Profile Picture Section
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable(enabled = !isUploadingImage) {
                        imagePicker.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    when {
                        isUploadingImage -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp).padding(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        profilePictureUrl != null -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profilePictureUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Doctor Image",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Text(
                "Tap to set profile picture",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Specialization (e.g. Cardiologist)") },
                    modifier = Modifier.fillMaxWidth()
            )

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                        value = experience,
                        onValueChange = { experience = it },
                        label = { Text("Exp (Years)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Contact No.") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            OutlinedTextField(
                    value = education,
                    onValueChange = { education = it },
                    label = { Text("Education (MBBS, MD)") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Clinic Address") },
                    modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Bio") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5
            )

            // Blood Group & Gender (Simplified for now - can use Dropdowns later)
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group") },
                        modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                    onClick = {
                        if (name.isBlank() || specialization.isBlank()) {
                            Toast.makeText(
                                            context,
                                            "Name and Specialization are required",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                            return@Button
                        }

                        isLoading = true
                        scope.launch {
                            val newDoctor =
                                    Doctor(
                                            name = name,
                                            email = email,
                                            specialization = specialization,
                                            experience = experience,
                                            contactNumber = contactNumber,
                                            description = description,
                                            education = education,
                                            address = address,
                                            gender = gender,
                                            bloodGroup = bloodGroup,
                                            profilePicture = profilePictureUrl ?: ""
                                    )

                            // We need to update FirestoreHelper to support these fields,
                            // but since it effectively serializes the object, it might just work
                            // if we invoke existing addDoctor(doctor).
                            val result = FirestoreHelper.addDoctor(newDoctor)
                            isLoading = false

                            result.fold(
                                    onSuccess = {
                                        Toast.makeText(
                                                        context,
                                                        "Doctor Created!",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        onBack() // Close screen
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                                        context,
                                                        "Error: ${it.message}",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                    }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = HealthMateShapes.ButtonLarge,
                    enabled = !isLoading
            ) {
                if (isLoading)
                        CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                        )
                else Text("Save Doctor Profile")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
