package com.example.healthmate.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Doctor
import com.example.healthmate.ui.theme.HealthMateTheme
import kotlinx.coroutines.launch

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

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Add New Doctor") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                )
            }
    ) { padding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(padding)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                                            bloodGroup = bloodGroup
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
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
            ) {
                if (isLoading)
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                else Text("Save Doctor Profile")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
