package com.example.myhealthmateaapp





import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhealthmateaapp.ui.theme.MyHealthMateaAppTheme
import java.text.SimpleDateFormat
import java.util.*

class MedicineExpiryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyHealthMateaAppTheme {
                MedicineExpiryTrackerScreen()
            }
        }
    }
}

// Data Models
data class Medicine(
    val id: Long,
    val name: String,
    val quantity: String,
    val expiryDate: Long,
    val category: String,
    val location: String,
    val status: ExpiryStatus
)

enum class ExpiryStatus {
    EXPIRED, EXPIRING_SOON, GOOD
}

// Main Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineExpiryTrackerScreen() {
    var medicines by remember { mutableStateOf(getSampleMedicines()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MedicineTopAppBar() },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFEF6C00),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Medicine") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Dashboard Summary
            DashboardSummary(medicines = medicines)

            // Medicine List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(medicines, key = { it.id }) { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        onDelete = {
                            medicines = medicines.filter { it.id != medicine.id }
                        }
                    )
                }
            }
        }

        // Add Medicine Dialog
        if (showAddDialog) {
            AddMedicineDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { newMedicine ->
                    medicines = medicines + newMedicine
                    showAddDialog = false
                }
            )
        }
    }
}

// Top App Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineTopAppBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Medicine Expiry Tracker",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Track expiration dates",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFEF6C00)
        )
    )
}

// Dashboard Summary
@Composable
fun DashboardSummary(medicines: List<Medicine>) {
    val totalCount = medicines.size
    val expiringSoonCount = medicines.count { it.status == ExpiryStatus.EXPIRING_SOON }
    val expiredCount = medicines.count { it.status == ExpiryStatus.EXPIRED }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("Total", totalCount.toString(), Color(0xFF424242))
            SummaryItem("Expiring Soon", expiringSoonCount.toString(), Color(0xFFF57C00))
            SummaryItem("Expired", expiredCount.toString(), Color(0xFFD32F2F))
        }
    }
}

@Composable
private fun SummaryItem(label: String, count: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// Medicine Card
@Composable
fun MedicineCard(
    medicine: Medicine,
    onDelete: () -> Unit
) {
    val (backgroundColor, borderColor) = when (medicine.status) {
        ExpiryStatus.EXPIRED -> Color(0xFFFFEBEE) to Color(0xFFEF9A9A)
        ExpiryStatus.EXPIRING_SOON -> Color(0xFFFFF8E1) to Color(0xFFFFE082)
        ExpiryStatus.GOOD -> Color(0xFFE8F5E9) to Color(0xFFA5D6A7)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(end = 32.dp)
            ) {
                // Medicine Name & Quantity
                Text(
                    text = medicine.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = medicine.quantity,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Badge
                StatusBadge(medicine.status)

                Spacer(modifier = Modifier.height(12.dp))

                // Expiry Message
                InfoRow(
                    icon = Icons.Default.DateRange,
                    text = getExpiryMessage(medicine.expiryDate),
                    isBold = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Expiry Date
                InfoRow(
                    icon = Icons.Default.DateRange,
                    text = "Expires: ${formatDate(medicine.expiryDate)}"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category
                InfoRow(
                    icon = Icons.Default.Star,
                    text = medicine.category
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Location
                InfoRow(
                    icon = Icons.Default.Place,
                    text = medicine.location
                )
            }

            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ExpiryStatus) {
    val (text, emoji, color) = when (status) {
        ExpiryStatus.EXPIRED -> Triple("Expired", "🔴", Color(0xFFC62828))
        ExpiryStatus.EXPIRING_SOON -> Triple("Expiring Soon", "🟡", Color(0xFFF57C00))
        ExpiryStatus.GOOD -> Triple("Good", "🟢", Color(0xFF2E7D32))
    }

    Text(
        text = "$emoji $text",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isBold: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isBold) Color(0xFF424242) else Color.Gray,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// Add Medicine Dialog
@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (Medicine) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add New Medicine",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., 50 tablets") }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Pain Relief") }
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Storage Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Medicine Cabinet") }
                )
                OutlinedTextField(
                    value = selectedDays,
                    onValueChange = { selectedDays = it },
                    label = { Text("Days Until Expiry") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., 30") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && quantity.isNotBlank()) {
                        val daysUntilExpiry = selectedDays.toIntOrNull() ?: 30
                        val expiryDate = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, daysUntilExpiry)
                        }.timeInMillis

                        val status = when {
                            daysUntilExpiry < 0 -> ExpiryStatus.EXPIRED
                            daysUntilExpiry <= 30 -> ExpiryStatus.EXPIRING_SOON
                            else -> ExpiryStatus.GOOD
                        }

                        val newMedicine = Medicine(
                            id = System.currentTimeMillis(),
                            name = name,
                            quantity = quantity,
                            expiryDate = expiryDate,
                            category = category.ifBlank { "General" },
                            location = location.ifBlank { "Not specified" },
                            status = status
                        )
                        onAdd(newMedicine)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF6C00)
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

// Utility Functions
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun getExpiryMessage(expiryTimestamp: Long): String {
    val today = Calendar.getInstance().timeInMillis
    val diffDays = ((expiryTimestamp - today) / (1000 * 60 * 60 * 24)).toInt()

    return when {
        diffDays < 0 -> "Expired ${kotlin.math.abs(diffDays)} days ago"
        else -> "$diffDays days remaining"
    }
}

// Sample Data
fun getSampleMedicines(): List<Medicine> {
    val calendar = Calendar.getInstance()

    return listOf(
        Medicine(
            id = 1,
            name = "Cough Syrup",
            quantity = "200ml bottle",
            expiryDate = calendar.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, -53)
            }.timeInMillis,
            category = "Cold & Flu",
            location = "Medicine Cabinet",
            status = ExpiryStatus.EXPIRED
        ),
        Medicine(
            id = 2,
            name = "Ibuprofen",
            quantity = "50 tablets",
            expiryDate = calendar.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 25)
            }.timeInMillis,
            category = "Pain Relief",
            location = "Kitchen Counter",
            status = ExpiryStatus.EXPIRING_SOON
        ),
        Medicine(
            id = 3,
            name = "Vitamin D",
            quantity = "60 capsules",
            expiryDate = calendar.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 340)
            }.timeInMillis,
            category = "Supplements",
            location = "Refrigerator",
            status = ExpiryStatus.GOOD
        ),
        Medicine(
            id = 4,
            name = "Bandages",
            quantity = "20 count",
            expiryDate = calendar.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 360)
            }.timeInMillis,
            category = "First Aid",
            location = "Medicine Cabinet",
            status = ExpiryStatus.GOOD
        ),
        Medicine(
            id = 5,
            name = "Antibiotic Cream",
            quantity = "30g tube",
            expiryDate = calendar.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, -90)
            }.timeInMillis,
            category = "First Aid",
            location = "Medicine Cabinet",
            status = ExpiryStatus.EXPIRED
        )
    )
}

// Preview
@Preview(showBackground = true)
@Composable
fun MedicineExpiryTrackerPreview() {
    MyHealthMateaAppTheme {
        MedicineExpiryTrackerScreen()
    }
}