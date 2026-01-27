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
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.screens.*
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.ThemeManager

class UserDashBoardActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContent {
                        val themeManager = ThemeManager(this)
                        val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)

                        HealthMateTheme(darkTheme = isDarkMode) { UserDashboardScreen() }
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen() {
        val context = LocalContext.current
        var selectedTab by remember { mutableStateOf(0) }

        Scaffold(
                topBar = {
                        HealthMateTopBar(
                                title =
                                        when (selectedTab) {
                                                0 -> "Home"
                                                1 -> "Appointments"
                                                2 -> "AI Chat"
                                                3 -> "Reminders"
                                                4 -> "Settings"
                                                else -> "HealthMate"
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
                                        icon = { Icon(Icons.Default.Home, "Home") },
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
                                        icon = {
                                                Icon(Icons.Default.CalendarMonth, "Appointments")
                                        },
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
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        icon = { Icon(Icons.Default.SmartToy, "Chat") },
                                        label = { Text("AI Chat") },
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
                                        icon = { Icon(Icons.Default.Alarm, "Reminders") },
                                        label = { Text("Reminders") },
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
                                        selected = selectedTab == 4,
                                        onClick = { selectedTab = 4 },
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
                                0 ->
                                        UserHomeScreen(
                                                onNavigateToAppointments = { selectedTab = 1 },
                                                onNavigateToRecords = { /* Navigate to Medical Records */
                                                },
                                                onNavigateToWellness = { /* Navigate to Articles */}
                                        )
                                1 -> UserAppointmentsScreen()
                                2 -> UserChatScreen()
                                3 -> UserRemindersScreen()
                                4 ->
                                        UserSettingsScreen(
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
