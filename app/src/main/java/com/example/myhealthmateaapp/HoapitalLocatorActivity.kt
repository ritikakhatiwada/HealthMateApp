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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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
                        Text(
                            "Hospital Locator",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Find nearby medical facilities",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
            // Map Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF7B9FE8))
            ) {
                // Map markers
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopStart)
                        .offset(40.dp, 40.dp)
                )
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset((-40).dp, 60.dp)
                )
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomStart)
                        .offset(60.dp, (-30).dp)
                )
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .offset((-60).dp, (-60).dp)
                )

                // Center location indicator
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Your Location",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier
                                .padding(12.dp)
                                .size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your Location",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        "Downtown Area",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    text = "All Hospitals (5)",
                    isSelected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" }
                )
                FilterChip(
                    text = "Emergency Only",
                    isSelected = selectedFilter == "Emergency",
                    onClick = { selectedFilter = "Emergency" }
                )
            }

            // Hospital List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredHospitals) { hospital ->
                    HospitalCard(hospital = hospital)
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFFD32F2F) else Color.White,
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF424242)
        )
    }
}

@Composable
fun HospitalCard(hospital: Hospital) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hospital.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = hospital.type,
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hospital.rating.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoBadge(
                    text = hospital.distance,
                    icon = Icons.Default.Place,
                    color = Color(0xFF1976D2)
                )
                if (hospital.hasEmergency) {
                    InfoBadge(
                        text = "Emergency",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFD32F2F)
                    )
                }
                if (hospital.is24x7) {
                    InfoBadge(
                        text = "24/7",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF388E3C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = "Address",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = hospital.address,
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
            }

            // Phone
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = hospital.phone,
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* Open directions */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Directions",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Directions", fontSize = 13.sp)
                }
                Button(
                    onClick = { /* Make call */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Call",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun InfoBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HospitalLocatorScreenPreview() {
    HospitalLocatorScreen(onBackClick = {})
}
