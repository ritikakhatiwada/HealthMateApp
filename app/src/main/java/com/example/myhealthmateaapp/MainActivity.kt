package com.example.myhealthmateaapp







import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthmate.ui.theme.HealthMateTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmate.viewmodel.MedicineViewModel

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthMateTheme {
                HealthMateApp()
            }
        }
    }
}

@Composable
fun HealthMateApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // 1. HOME SCREEN
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // 2. SYMPTOM ANALYZER
        composable(Screen.SymptomAnalyzer.route) {
            SymptomAnalyzerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. APPOINTMENTS LIST
        composable(Screen.Appointments.route) {
            AppointmentScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onBookNewAppointment = {
                    navController.navigate(Screen.BookAppointment.route)
                }
            )
        }

        // 4. BOOK APPOINTMENT
        composable(Screen.BookAppointment.route) {
            BookAppointmentScreen(
                onBackClick = { navController.popBackStack() },
                onConfirmClick = { doctor, date, time, location ->
                    // Logic to save...
                    navController.popBackStack()
                }
            )
        }

        // 5. MEDICINE REMINDER
        composable(Screen.MedicineReminder.route) {
            val viewModel: MedicineViewModel = viewModel()

            MedicineReminderScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onAddMedicineClick = {
                    navController.navigate(Screen.AddMedicine.route)
                }
            )
        }

        // 6. ADD MEDICINE SCREEN
        composable(Screen.AddMedicine.route) {
            val viewModel: MedicineViewModel = viewModel()

            AddMedicineScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 7. HOSPITAL LOCATOR
        composable(Screen.HospitalLocator.route) {
            HospitalLocatorScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 8. MEDICAL HISTORY
        composable(Screen.MedicalHistory.route) {
            MedicalHistoryScreen(navController = navController)
        }

        // 9. EMERGENCY SERVICES (Updated)
        composable(Screen.EmergencyServices.route) {
            EmergencyServicesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onViewMedicalProfile = {
                    // Navigate to Medical History when "Edit Medical Info" is clicked
                    navController.navigate(Screen.MedicalHistory.route)
                }
            )
        }

        // --- PLACEHOLDERS FOR OTHER SCREENS ---
        composable(Screen.AIHealthAssistant.route) {
            AIHealthAssistantScreen(navController = navController)
        }

        composable(Screen.MedicineExpiry.route) {
            MedicineExpiryScreen(navController = navController)
        }
        composable(Screen.PrescriptionReader.route) {
            PrescriptionReaderScreen(navController = navController)
        }
    }
}

// --- PLACEHOLDER FUNCTIONS ---
// EmergencyServicesScreen placeholder removed because you now have the real file.

@Composable
fun AIHealthAssistantScreen(navController: NavController) {
    PlaceholderScreen("AI Assistant")
}

@Composable
fun MedicineExpiryScreen(navController: NavController) {
    PlaceholderScreen("Medicine Expiry")
}

@Composable
fun PrescriptionReaderScreen(navController: NavController) {
    PlaceholderScreen("Prescription Reader")
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyHealthMateaAppTheme {
        Greeting("Android")
    }
}