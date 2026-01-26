package com.example.myhealthmateaapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// ---------------------------------------------------------
// NAVIGATION ROUTES (REQUIRED)
// ---------------------------------------------------------
sealed class Screen(val route: String) {
    object EmergencyServices : Screen("emergency_services")
    object SymptomAnalyzer : Screen("symptom_analyzer")
    object AIHealthAssistant : Screen("ai_health_assistant")
    object Appointments : Screen("appointments")
    object MedicineReminder : Screen("medicine_reminder")
    object HospitalLocator : Screen("hospital_locator")
    object MedicalHistory : Screen("medical_history")
    object MedicineExpiry : Screen("medicine_expiry")
    object PrescriptionReader : Screen("prescription_reader")
}

// ---------------------------------------------------------
// HOME SCREEN
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color(0xFF6C63FF)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("HealthMate", fontWeight = FontWeight.Bold)
                            Text(
                                "Your health companion",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = null)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {

            // Welcome Card
            item {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6C63FF), Color(0xFF9D8FFF))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                "Welcome back!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "How can we help you today?",
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Search symptoms, medications…",
                                        style = LocalTextStyle.current.copy(color = Color.White)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(0.2f),
                                    focusedContainerColor = Color.White.copy(0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Emergency Card
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            navController.navigate(Screen.EmergencyServices.route)
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Emergency Services", fontWeight = FontWeight.Bold)
                            Text("Get immediate help 24/7", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Features Title
            item {
                Text(
                    "Features",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Feature Rows
            item {
                FeatureRow(
                    left = Feature(
                        "Symptom Analyzer",
                        "AI symptom checker",
                        Icons.Default.Search,
                        Color(0xFF6C63FF)
                    ) { navController.navigate(Screen.SymptomAnalyzer.route) },
                    right = Feature(
                        "AI Assistant",
                        "Chat with AI doctor",
                        Icons.Default.Face,
                        Color(0xFF9D8FFF)
                    ) { navController.navigate(Screen.AIHealthAssistant.route) }
                )
            }

            item {
                FeatureRow(
                    left = Feature(
                        "Appointments",
                        "Book visits",
                        Icons.Default.DateRange,
                        Color(0xFF4CAF50)
                    ) { navController.navigate(Screen.Appointments.route) },
                    right = Feature(
                        "Medicine Reminder",
                        "Track medicines",
                        Icons.Default.Notifications,
                        Color(0xFFFF9800)
                    ) { navController.navigate(Screen.MedicineReminder.route) }
                )
            }

            item {
                FeatureRow(
                    left = Feature(
                        "Hospital Locator",
                        "Find hospitals",
                        Icons.Default.LocationOn,
                        Color(0xFFFF4444)
                    ) { navController.navigate(Screen.HospitalLocator.route) },
                    right = Feature(
                        "Medical History",
                        "View records",
                        Icons.Default.Info,
                        Color(0xFF26C6DA)
                    ) { navController.navigate(Screen.MedicalHistory.route) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ---------------------------------------------------------
// FEATURE MODELS + UI
// ---------------------------------------------------------
data class Feature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun FeatureRow(left: Feature, right: Feature) {
    Column {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(left, Modifier.weight(1f), left.onClick)
            FeatureCard(right, Modifier.weight(1f), right.onClick)
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun FeatureCard(feature: Feature, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(feature.icon, contentDescription = null, tint = feature.color)
            Column {
                Text(feature.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(feature.subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// ---------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}
