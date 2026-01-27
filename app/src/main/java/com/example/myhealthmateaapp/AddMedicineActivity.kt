package com.example.myhealthmateaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
class AddMedicineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AddMedicineScreen(
                onBackClick = {
                    finish() // closes activity
                },
                onSaveClick = { medicineData ->
                    // TODO: Save to Firebase / DB later
                    finish()
                }
            )
        }
    }
}


// Helper Data Class
data class MedicineFormData(
    val name: String,
    val dosage: String,
    val timesPerDay: String,
    val startDate: String,
    val endDate: String,
    val notes: String,
)

// Main Screen (UI Only)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    onBackClick: () -> Unit,
    onSaveClick: (MedicineFormData) -> Unit
) {
    var medicineName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var timesPerDay by remember { mutableStateOf("Once daily") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val frequencies = listOf(
        "Once daily",
        "Twice daily",
        "3 times daily",
        "4 times daily"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Medicine", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD84315)
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (medicineName.isBlank() || dosage.isBlank() || startDate.isBlank()) {
                        showError = true
                        errorMessage = "Please fill all required fields"
                    } else {
                        showError = false
                        onSaveClick(
                            MedicineFormData(
                                name = medicineName,
                                dosage = dosage,
                                timesPerDay = timesPerDay,
                                startDate = startDate,
                                endDate = endDate.ifEmpty { startDate },
                                notes = notes
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF9A9A)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Medicine", fontSize = 16.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            if (showError) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Medicine Name
            OutlinedTextField(
                value = medicineName,
                onValueChange = { medicineName = it },
                label = { Text("Medicine Name *") },
                placeholder = { Text("e.g., Aspirin") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Dosage
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage *") },
                placeholder = { Text("e.g., 500mg") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Times per Day Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = timesPerDay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Times per Day *") },
                    trailingIcon = {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    frequencies.forEach { frequency ->
                        DropdownMenuItem(
                            text = { Text(frequency) },
                            onClick = {
                                timesPerDay = frequency
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Start Date
            OutlinedTextField(
                value = startDate,
                onValueChange = {
                    if (it.length <= 10) startDate = it
                },
                label = { Text("Start Date *") },
                placeholder = { Text("mm/dd/yyyy") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // End Date
            OutlinedTextField(
                value = endDate,
                onValueChange = {
                    if (it.length <= 10) endDate = it
                },
                label = { Text("End Date") },
                placeholder = { Text("mm/dd/yyyy") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("e.g., Take with food") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun AddMedicineScreenPreview() {
    AddMedicineScreen(
        onBackClick = {},
        onSaveClick = {}
    )
}