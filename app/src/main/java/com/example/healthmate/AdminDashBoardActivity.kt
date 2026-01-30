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
import com.example.healthmate.ui.components.BottomNavItem
import com.example.healthmate.ui.components.HealthMateBottomNav
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

        val navItems = listOf(
            BottomNavItem("Home", Icons.Default.Dashboard),
            BottomNavItem("Doctors", Icons.Default.MedicalServices),
            BottomNavItem("Appts", Icons.Default.CalendarMonth),
            BottomNavItem("Settings", Icons.Default.Settings)
        )

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
                        HealthMateBottomNav(
                                items = navItems,
                                selectedIndex = selectedTab,
                                onItemSelected = { selectedTab = it }
                        )
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
