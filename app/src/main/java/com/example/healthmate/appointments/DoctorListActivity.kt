package com.example.healthmate.appointments

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.healthmate.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Doctor
import com.example.healthmate.ui.components.DoctorListItem
import com.example.healthmate.ui.components.EmptyState
import com.example.healthmate.ui.components.HealthMateTextField
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.components.LoadingState
import com.example.healthmate.ui.components.DoctorListSkeleton
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

class DoctorListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) {
                DoctorListScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorListScreen() {
    val context = LocalContext.current
    var allDoctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var filteredDoctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecialty by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            allDoctors = FirestoreHelper.getDoctors()
            filteredDoctors = allDoctors
            isLoading = false
        }
    }

    // Filter doctors based on search and specialty
    LaunchedEffect(searchQuery, selectedSpecialty, allDoctors) {
        filteredDoctors = allDoctors.filter { doctor ->
            val matchesSearch = searchQuery.isEmpty() ||
                doctor.name.contains(searchQuery, ignoreCase = true) ||
                doctor.specialization.contains(searchQuery, ignoreCase = true)
            val matchesSpecialty = selectedSpecialty == null ||
                doctor.specialization.equals(selectedSpecialty, ignoreCase = true)
            matchesSearch && matchesSpecialty
        }
    }

    // Get unique specialties for filter chips
    val specialties = remember(allDoctors) {
        allDoctors.map { it.specialization }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            HealthMateTopBar(
                title = "Select Doctor",
                subtitle = "Choose your preferred specialist",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { (context as? ComponentActivity)?.finish() }
            )
        }
    ) { padding ->
        if (isLoading) {
            DoctorListSkeleton()
        } else if (allDoctors.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Person,
                title = "No Doctors Available",
                message = "There are no doctors registered in the system. Please check back later."
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Hero Image
                Image(
                    painter = painterResource(id = R.drawable.appointment_hero),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentScale = ContentScale.Fit
                )

                // Search Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                ) {
                    HealthMateTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search",
                        placeholder = "Search by name or specialty",
                        leadingIcon = Icons.Default.Search,
                        imeAction = ImeAction.Search
                    )
                }

                // Specialty Filter Chips
                if (specialties.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        FilterChip(
                            selected = selectedSpecialty == null,
                            onClick = { selectedSpecialty = null },
                            label = { Text("All") }
                        )
                        specialties.take(3).forEach { specialty ->
                            FilterChip(
                                selected = selectedSpecialty == specialty,
                                onClick = {
                                    selectedSpecialty = if (selectedSpecialty == specialty) null else specialty
                                },
                                label = { Text(specialty) }
                            )
                        }
                    }
                }

                // Doctor List
                if (filteredDoctors.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = "No Results Found",
                            message = "No doctors match your search criteria. Try adjusting your filters."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(filteredDoctors) { doctor ->
                            DoctorListItem(
                                doctor = doctor,
                                onClick = {
                                    val intent = Intent(context, SlotSelectionActivity::class.java)
                                    intent.putExtra("doctorId", doctor.id)
                                    intent.putExtra("doctorName", doctor.name)
                                    intent.putExtra("doctorSpecialization", doctor.specialization)
                                    context.startActivity(intent)
                                },
                                showSpecialty = true,
                                showExperience = true
                            )
                        }
                    }
                }
            }
        }
    }
}

// DoctorCard removed - now using DoctorListItem from ui.components package
