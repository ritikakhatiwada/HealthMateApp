package com.example.myhealthmateaapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// MAIN SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalLocatorScreen(onBackClick: () -> Unit) {

    var selectedFilter by remember { mutableStateOf("All") }

    val hospitals = remember {
        listOf(
            Hospital(
                name = "City General Hospital",
                type = "Multi-Specialty",
                distance = "1.2 km",
                hasEmergency = true,
                is24x7 = true,
                address = "123 Main Street, Downtown",
                phone = "+1 (555) 123-4567",
                rating = 4.5
            ),
            Hospital(
                name = "St. Mary Medical Center",
                type = "General Hospital",
                distance = "2.5 km",
                hasEmergency = true,
                is24x7 = true,
                address = "456 Oak Avenue, Westside",
                phone = "+1 (555) 234-5678",
                rating = 4.8
            ),
            Hospital(
                name = "Green Valley Clinic",
                type = "Walk-in Clinic",
                distance = "800 m",
                hasEmergency = false,
                is24x7 = false,
                address = "789 Pine Road, Northside",
                phone = "+1 (555) 345-6789",
                rating = 4.2
            ),
            Hospital(
                name = "Heart Care Specialty Center",
                type = "Cardiology",
                distance = "3.1 km",
                hasEmergency = false,
                is24x7 = true,
                address = "321 Cedar Lane, Eastside",
                phone = "+1 (555) 456-7890",
                rating = 4.9
            )
        )
    }

    val filteredHospitals = when (selectedFilter) {
        "Emergency" -> hospitals.filter { it.hasEmergency }
        else -> hospitals
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hospital Locator", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Find nearby medical facilities", fontSize = 13.sp, color = Color.White.copy(0.9f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD32F2F)
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {

            // Map Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF7B9FE8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "Your Location",
                    color = Color.White,
                    modifier = Modifier.padding(top = 64.dp)
                )
            }

            // Filter Chips
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    text = "All Hospitals (${hospitals.size})",
                    isSelected = selectedFilter == "All"
                ) { selectedFilter = "All" }

                FilterChip(
                    text = "Emergency Only",
                    isSelected = selectedFilter == "Emergency"
                ) { selectedFilter = "Emergency" }
            }

            // Hospital List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredHospitals) {
                    HospitalCard(it)
                }
            }
        }
    }
}

// ============================================================
// FILTER CHIP
// ============================================================

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFFD32F2F) else Color.White,
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

// ============================================================
// HOSPITAL CARD
// ============================================================

@Composable
fun HospitalCard(hospital: Hospital) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(hospital.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(hospital.type, fontSize = 13.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(hospital.rating.toString(), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoBadge(hospital.distance, Icons.Default.Place, Color(0xFF1976D2))
                if (hospital.hasEmergency)
                    InfoBadge("Emergency", Icons.Default.Warning, Color(0xFFD32F2F))
                if (hospital.is24x7)
                    InfoBadge("24/7", Icons.Default.CheckCircle, Color(0xFF388E3C))
            }

            Spacer(Modifier.height(10.dp))

            Text("📍 ${hospital.address}", fontSize = 13.sp)
            Text("📞 ${hospital.phone}", fontSize = 13.sp)

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.LocationOn, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Directions")
                }
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Phone, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Call")
                }
            }
        }
    }
}

// ============================================================
// INFO BADGE
// ============================================================

@Composable
fun InfoBadge(text: String, icon: ImageVector, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 11.sp, color = color)
        }
    }
}

// ============================================================
// DATA CLASS
// ============================================================

data class Hospital(
    val name: String,
    val type: String,
    val distance: String,
    val hasEmergency: Boolean,
    val is24x7: Boolean,
    val address: String,
    val phone: String,
    val rating: Double
)

// ============================================================
// PREVIEW
// ============================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HospitalLocatorScreenPreview() {
    HospitalLocatorScreen(onBackClick = {})
}
