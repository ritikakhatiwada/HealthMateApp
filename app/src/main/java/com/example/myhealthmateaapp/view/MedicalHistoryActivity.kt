package com.example.myhealthmateaapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// ============================================================
// MAIN SCREEN
// ============================================================
class MedicalHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface {
                    MedicalHistoryScreen(
                        navController = rememberNavController()
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(
    viewModel: MedicalHistoryViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error, uiState.downloadSuccess) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.downloadSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF5F9EA0))
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Medical History", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Your health records", fontSize = 13.sp, color = Color.White.copy(0.9f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.downloadMedicalRecords() }) {
                            Icon(Icons.Default.Download, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF5F9EA0))
                )

                PatientProfileSection(
                    profile = uiState.patientProfile,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            TabNavigationRow(
                tabs = viewModel.getTabs(),
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab
            )

            HorizontalDivider()

            if (uiState.filteredRecords.isEmpty()) {
                EmptyRecordsView(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredRecords, key = { it.id }) {
                        MedicalRecordCard(record = it, onClick = {})
                    }
                }
            }
        }
    }
}

// ============================================================
// PATIENT PROFILE
// ============================================================

@Composable
fun PatientProfileSection(profile: PatientProfile, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(profile.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(profile.patientId, fontSize = 12.sp, color = Color.White)
            Text("Age: ${profile.age} • ${profile.bloodType}", fontSize = 12.sp, color = Color.White)
        }
    }
}

// ============================================================
// TAB ROW
// ============================================================

@Composable
fun TabNavigationRow(
    tabs: List<MedicalHistoryTab>,
    selectedTab: MedicalHistoryTab,
    onTabSelected: (MedicalHistoryTab) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs) {
            TabChip(
                text = it.title,
                isSelected = it == selectedTab,
                onClick = { onTabSelected(it) }
            )
        }
    }
}

@Composable
fun TabChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF5F9EA0) else Color.Transparent,
        border = if (!isSelected) BorderStroke(1.dp, Color.Gray) else null
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.DarkGray
        )
    }
}

// ============================================================
// RECORD CARD
// ============================================================

@Composable
fun MedicalRecordCard(record: MedicalRecord, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(record.category.color, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getCategoryIcon(record.category),
                        null,
                        tint = getCategoryIconColor(record.category)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(record.title, fontWeight = FontWeight.Bold)
                    Text(record.date, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Doctor: ${record.doctorName}", fontSize = 13.sp)
            Text("Facility: ${record.facility}", fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(record.description, fontSize = 13.sp)
        }
    }
}

// ============================================================
// EMPTY VIEW
// ============================================================

@Composable
fun EmptyRecordsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
        Text("No records found", color = Color.Gray)
    }
}

// ============================================================
// DATA + VIEWMODEL
// ============================================================

enum class MedicalRecordCategory(val color: Color, val title: String) {
    GENERAL(Color(0xFFE0E0E0), "General"),
    LAB(Color(0xFFE3F2FD), "Lab"),
    PRESCRIPTION(Color(0xFFE8F5E9), "Prescription"),
    SURGERY(Color(0xFFFFEBEE), "Surgery")
}

data class MedicalRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val doctorName: String,
    val facility: String,
    val description: String,
    val category: MedicalRecordCategory
)

data class PatientProfile(
    val name: String = "John Doe",
    val patientId: String = "ID: 123456",
    val age: Int = 30,
    val bloodType: String = "O+"
)

data class MedicalHistoryTab(val title: String)

data class UiState(
    val patientProfile: PatientProfile = PatientProfile(),
    val selectedTab: MedicalHistoryTab = MedicalHistoryTab("All"),
    val filteredRecords: List<MedicalRecord> = emptyList(),
    val error: String? = null,
    val downloadSuccess: String? = null
)

class MedicalHistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        UiState(
            filteredRecords = listOf(
                MedicalRecord(
                    title = "Blood Test",
                    date = "Sep 15, 2024",
                    doctorName = "Dr. Lee",
                    facility = "City Lab",
                    description = "CBC & Lipid profile",
                    category = MedicalRecordCategory.LAB
                )
            )
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun clearMessages() {}
    fun downloadMedicalRecords() {}
    fun getTabs() = listOf(MedicalHistoryTab("All"), MedicalHistoryTab("Lab"))
    fun selectTab(tab: MedicalHistoryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
}

// ============================================================
// ICON HELPERS
// ============================================================

fun getCategoryIcon(category: MedicalRecordCategory): ImageVector =
    when (category) {
        MedicalRecordCategory.LAB -> Icons.Default.Science
        MedicalRecordCategory.PRESCRIPTION -> Icons.Default.Description
        MedicalRecordCategory.SURGERY -> Icons.Default.LocalHospital
        else -> Icons.Default.Note
    }


fun getCategoryIconColor(category: MedicalRecordCategory): Color =
    when (category) {
        MedicalRecordCategory.LAB -> Color(0xFF1976D2)
        MedicalRecordCategory.PRESCRIPTION -> Color(0xFF388E3C)
        MedicalRecordCategory.SURGERY -> Color(0xFFD32F2F)
        else -> Color.DarkGray
    }

// ============================================================
// PREVIEW
// ============================================================

@Preview(showBackground = true)
@Composable
fun MedicalHistoryPreview() {
    MedicalHistoryScreen(navController = rememberNavController())
}
