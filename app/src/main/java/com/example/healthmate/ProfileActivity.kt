package com.example.healthmate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) { ProfileScreen(onBack = { finish() }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // UI State
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Load Data
    LaunchedEffect(Unit) {
        scope.launch {
            val userId = FirebaseAuthHelper.getCurrentUserId()
            val user = FirestoreHelper.getUserById(userId)
            if (user != null) {
                name = user.name
                email = user.email
                age = user.age
                gender = user.gender
                bloodGroup = user.bloodGroup
                phoneNumber = user.phoneNumber
                address = user.address
            }
            isLoading = false
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Edit Profile") },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                        },
                        actions = {
                            IconButton(
                                    onClick = {
                                        if (name.isBlank()) {
                                            Toast.makeText(
                                                            context,
                                                            "Name cannot be empty",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            return@IconButton
                                        }

                                        isSaving = true
                                        scope.launch {
                                            val userId = FirebaseAuthHelper.getCurrentUserId()
                                            val updates =
                                                    mapOf(
                                                            "name" to name,
                                                            "age" to age,
                                                            "gender" to gender,
                                                            "bloodGroup" to bloodGroup,
                                                            "phoneNumber" to phoneNumber,
                                                            "address" to address
                                                    )

                                            val result =
                                                    FirestoreHelper.updateUserProfile(
                                                            userId,
                                                            updates
                                                    )
                                            isSaving = false

                                            result.fold(
                                                    onSuccess = {
                                                        Toast.makeText(
                                                                        context,
                                                                        "Profile updated!",
                                                                        Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                    },
                                                    onFailure = { e ->
                                                        Toast.makeText(
                                                                        context,
                                                                        "Error: ${e.message}",
                                                                        Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                    }
                                            )
                                        }
                                    },
                                    enabled = !isLoading && !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(Icons.Default.Save, "Save")
                                }
                            }
                        }
                )
            }
    ) { padding ->
        if (isLoading) {
            Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(padding)
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar Placeholder
                Box(
                        modifier =
                                Modifier.size(100.dp)
                                        .background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                CircleShape
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                )

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                    )
                    OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                    )
                }

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Group") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                    )
                    OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                    )
                }

                OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                )
            }
        }
    }
}
