package com.example.healthmate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.healthmate.admin.AddDoctorActivity
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.screens.*
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.ThemeManager

class AdminDashBoardActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContent {
                        val themeManager = ThemeManager(this)
                        val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)

                        HealthMateTheme(darkTheme = isDarkMode) { AdminDashboardScreen() }
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
        val context = LocalContext.current
        var selectedTab by remember { mutableStateOf(0) }

        Scaffold(
                topBar = {
                        HealthMateTopBar(
                                title =
                                        when (selectedTab) {
                                                0 -> "Dashboard"
                                                1 -> "Doctors"
                                                2 -> "Appointments"
                                                3 -> "Settings"
                                                else -> "HealthMate Admin"
                                        }
                        )
                },
                bottomBar = {
                        NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                                NavigationBarItem(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        icon = { Icon(Icons.Default.Dashboard, "Home") },
                                        label = { Text("Home") },
                                        colors =
                                                NavigationBarItemDefaults.colors(
                                                        selectedIconColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        selectedTextColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        indicatorColor =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                )
                                )
                                NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        icon = { Icon(Icons.Default.MedicalServices, "Doctors") },
                                        label = { Text("Doctors") },
                                        colors =
                                                NavigationBarItemDefaults.colors(
                                                        selectedIconColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        selectedTextColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        indicatorColor =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                )
                                )
                                NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        icon = { Icon(Icons.Default.CalendarMonth, "Appts") },
                                        label = { Text("Appts") },
                                        colors =
                                                NavigationBarItemDefaults.colors(
                                                        selectedIconColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        selectedTextColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        indicatorColor =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                )
                                )
                                NavigationBarItem(
                                        selected = selectedTab == 3,
                                        onClick = { selectedTab = 3 },
                                        icon = { Icon(Icons.Default.Settings, "Settings") },
                                        label = { Text("Settings") },
                                        colors =
                                                NavigationBarItemDefaults.colors(
                                                        selectedIconColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        selectedTextColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        indicatorColor =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                )
                                )
                        }
                }
        ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                        when (selectedTab) {
                                0 -> AdminHomeScreen()
                                1 ->
                                        AdminDoctorsScreen(
                                                onAddDoctor = {
                                                        val intent =
                                                                Intent(
                                                                        context,
                                                                        AddDoctorActivity::class
                                                                                .java
                                                                )
                                                        context.startActivity(intent)
                                                }
                                        )
                                2 -> AdminAppointmentsScreen()
                                3 ->
                                        AdminSettingsScreen(
                                                onLogout = {
                                                        FirebaseAuthHelper.logout()
                                                        val intent =
                                                                Intent(
                                                                        context,
                                                                        LoginActivity::class.java
                                                                )
                                                        intent.flags =
                                                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        context.startActivity(intent)
                                                }
                                        )
                        }
                }
        }
}
