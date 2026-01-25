package com.example.myhealthmateaapp



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// 1. MAIN SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(
    // We default to a new instance for Preview purposes, but in App logic you pass the existing one
    viewModel: MedicalHistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle messages
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
                // Top Bar
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Medical History",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Your health records",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.downloadMedicalRecords() },
                            enabled = !uiState.isDownloading
                        ) {
                            if (uiState.isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Download Records",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF5F9EA0)
                    )
                )

                // Patient Profile Section
                PatientProfileSection(
                    profile = uiState.patientProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF5F9EA0))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
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
            // Tab Navigation
            TabNavigationRow(
                tabs = viewModel.getTabs(),
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Records List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredRecords.isEmpty()) {
                EmptyRecordsView(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.filteredRecords,
                        key = { it.id }
                    ) { record ->
                        MedicalRecordCard(
                            record = record,
                            onClick = {
                                // Example navigation
                                // navController.navigate("record_detail/${record.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// PATIENT PROFILE SECTION
// ============================================================

@Composable
fun PatientProfileSection(
    profile: PatientProfile,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // Patient Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = profile.patientId,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Age: ${profile.age} • ${profile.bloodType}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

// ============================================================
// TAB NAVIGATION
// ============================================================

@Composable
fun TabNavigationRow(
    tabs: List<MedicalHistoryTab>,
    selectedTab: MedicalHistoryTab,
    onTabSelected: (MedicalHistoryTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs) { tab ->
            TabChip(
                text = tab.title,
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
fun TabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(20.dp)),
        color = if (isSelected) Color(0xFF5F9EA0) else Color.Transparent,
        border = if (!isSelected) {
            BorderStroke(1.dp, Color(0xFFBDBDBD))
        } else null,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color(0xFF666666),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ============================================================
// MEDICAL RECORD CARD
// ============================================================

@Composable
fun MedicalRecordCard(
    record: MedicalRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(record.getCategoryEnum().color)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(record.getCategoryEnum()),
                        contentDescription = null,
                        tint = getCategoryIconColor(record.getCategoryEnum()),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Title and Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF757575)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = record.getFormattedDate(),
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                // Category Badge
                CategoryBadge(category = record.getCategoryEnum())
            }

            Spacer(Modifier.height(12.dp))

            // Doctor and Facility
            Text(
                text = "Doctor: ${record.doctorName}",
                fontSize = 13.sp,
                color = Color(0xFF424242)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Facility: ${record.facility}",
                fontSize = 13.sp,
                color = Color(0xFF424242)
            )

            Spacer(Modifier.height(12.dp))

            // Description
            Text(
                text = record.description,
                fontSize = 13.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )

            // Attachments and View Button (This was the cut-off part)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Text("View Details", color = Color(0xFF5F9EA0))
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF5F9EA0)
                    )
                }
            }
        }
    }
}

// ============================================================
// HELPER COMPONENTS & DUMMY DATA
// ============================================================

@Composable
fun CategoryBadge(category: MedicalRecordCategory) {
    Surface(
        color = Color(category.color).copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            color = getCategoryIconColor(category),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyRecordsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(Modifier.height(16.dp))
        Text("No records found", color = Color.Gray)
    }
}

// --- DATA CLASSES & VIEW MODEL ---

enum class MedicalRecordCategory(val color: Long, val title: String) {
    GENERAL(0xFFE0E0E0, "General"),
    LAB(0xFFE3F2FD, "Lab Report"),
    PRESCRIPTION(0xFFE8F5E9, "Prescription"),
    SURGERY(0xFFFFEBEE, "Surgery");
}

data class MedicalRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val doctorName: String,
    val facility: String,
    val description: String,
    val category: MedicalRecordCategory,
    val attachments: List<String> = emptyList()
) {
    fun getCategoryEnum() = category
    fun getFormattedDate() = date
}

data class PatientProfile(
    val name: String = "John Doe",
    val patientId: String = "ID: 123-456",
    val age: Int = 30,
    val bloodType: String = "O+"
)

data class MedicalHistoryTab(val title: String)

data class UiState(
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val error: String? = null,
    val downloadSuccess: String? = null,
    val patientProfile: PatientProfile = PatientProfile(),
    val selectedTab: MedicalHistoryTab = MedicalHistoryTab("All"),
    val filteredRecords: List<MedicalRecord> = emptyList()
)

class MedicalHistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Load dummy data
        val tabs = listOf(MedicalHistoryTab("All"), MedicalHistoryTab("Labs"), MedicalHistoryTab("Prescriptions"))
        val records = listOf(
            MedicalRecord(
                title = "Annual Physical",
                date = "Oct 24, 2024",
                doctorName = "Dr. Smith",
                facility = "City Hospital",
                description = "Routine checkup. All vitals normal.",
                category = MedicalRecordCategory.GENERAL
            ),
            MedicalRecord(
                title = "Blood Test Results",
                date = "Sep 15, 2024",
                doctorName = "Dr. Lee",
                facility = "Downtown Lab",
                description = "CBC and Lipid Panel.",
                category = MedicalRecordCategory.LAB
            )
        )
        _uiState.value = UiState(filteredRecords = records)
    }

    fun downloadMedicalRecords() {
        // Mock download
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, downloadSuccess = null)
    }

    fun getTabs() = listOf(MedicalHistoryTab("All"), MedicalHistoryTab("Labs"), MedicalHistoryTab("Prescriptions"))

    fun selectTab(tab: MedicalHistoryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
}

// Helpers for icons
fun getCategoryIcon(category: MedicalRecordCategory): ImageVector {
    return when (category) {
        MedicalRecordCategory.LAB -> Icons.Default.Science
        MedicalRecordCategory.PRESCRIPTION -> Icons.Default.Description
        MedicalRecordCategory.SURGERY -> Icons.Default.LocalHospital
        else -> Icons.Default.Note
    }
}

fun getCategoryIconColor(category: MedicalRecordCategory): Color {
    return when (category) {
        MedicalRecordCategory.LAB -> Color(0xFF1976D2)
        MedicalRecordCategory.PRESCRIPTION -> Color(0xFF388E3C)
        MedicalRecordCategory.SURGERY -> Color(0xFFD32F2F)
        else -> Color(0xFF616161)
    }
}

@Preview(showBackground = true)
@Composable
fun MedicalHistoryPreview() {
    MedicalHistoryScreen(navController = rememberNavController())
}
