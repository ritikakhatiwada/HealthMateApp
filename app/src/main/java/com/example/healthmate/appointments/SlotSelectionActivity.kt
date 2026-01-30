package com.example.healthmate.appointments

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.Appointment
import com.example.healthmate.model.Slot
import com.example.healthmate.ui.components.ConfirmationDialog
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.components.shimmerEffect
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class SlotSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val doctorId = intent.getStringExtra("doctorId") ?: ""
        val doctorName = intent.getStringExtra("doctorName") ?: ""
        val doctorSpecialization = intent.getStringExtra("doctorSpecialization") ?: ""

        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) {
                SlotSelectionScreen(doctorId, doctorName, doctorSpecialization)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotSelectionScreen(doctorId: String, doctorName: String, doctorSpecialization: String) {
    val context = LocalContext.current
    var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isBooking by remember { mutableStateOf(false) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf<Slot?>(null) }
    var nearestSlot by remember { mutableStateOf<Slot?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadSlots() {
        coroutineScope.launch {
            isLoading = true
            slots = FirestoreHelper.getSlotsByDoctor(doctorId)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadSlots() }

    fun bookSlot(slot: Slot) {
        coroutineScope.launch {
            isBooking = true
            val user = FirebaseAuthHelper.getCurrentUser()
            val patientName = if (user?.displayName.isNullOrBlank() || user?.displayName == "User") {
                FirestoreHelper.getUserById(user?.uid ?: "")?.name ?: "User"
            } else {
                user?.displayName ?: "User"
            }
            val appointment =
                Appointment(
                    patientId = FirebaseAuthHelper.getCurrentUserId(),
                    patientName = patientName,
                    doctorId = doctorId,
                    slotId = slot.id,
                    doctorName = doctorName,
                    date = slot.date,
                    time = slot.time
                )
            val result = FirestoreHelper.bookAppointment(appointment)
            isBooking = false

            result.fold(
                onSuccess = {
                    Toast.makeText(
                        context,
                        "Appointment booked successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    (context as? ComponentActivity)?.finish()
                },
                onFailure = { error ->
                    Toast.makeText(context, "Failed: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            )
        }
    }

    // Booking confirmation dialog
    if (showConfirmDialog && selectedSlot != null) {
        ConfirmationDialog(
            title = "Confirm Appointment",
            message = "Book appointment with $doctorName on ${selectedSlot?.date} at ${selectedSlot?.time}?",
            confirmText = "Book Appointment",
            onConfirm = {
                showConfirmDialog = false
                selectedSlot?.let { bookSlot(it) }
            },
            onDismiss = {
                showConfirmDialog = false
                selectedSlot = null
            }
        )
    }

    // Conflict dialog (slot already booked)
    if (showConflictDialog && nearestSlot != null) {
        ConfirmationDialog(
            title = "Slot Unavailable",
            message = "This slot is already booked. Would you like to book the nearest available slot?\n\nDate: ${nearestSlot?.date}\nTime: ${nearestSlot?.time}",
            confirmText = "Book This Slot",
            onConfirm = {
                showConflictDialog = false
                nearestSlot?.let { bookSlot(it) }
            },
            onDismiss = {
                showConflictDialog = false
                nearestSlot = null
            }
        )
    }

    // Extract unique dates from slots and sort them
    val uniqueDates = remember(slots) {
        slots.map { it.date }.distinct().sorted()
    }

    var selectedDate by remember(uniqueDates) {
        mutableStateOf(if (uniqueDates.isNotEmpty()) uniqueDates[0] else "")
    }

    // Filter slots by selected date
    val filteredSlots = remember(slots, selectedDate) {
        slots.filter { it.date == selectedDate }
    }

    // Get today's date for comparison
    val todayString = remember {
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    Scaffold(
        topBar = {
            HealthMateTopBar(
                title = "Book Appointment",
                subtitle = "$doctorName • $doctorSpecialization",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { (context as? ComponentActivity)?.finish() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            isLoading || isBooking -> {
                SlotSelectionSkeleton(
                    modifier = Modifier.padding(padding),
                    message = if (isLoading) "Loading available slots..." else "Booking your appointment..."
                )
            }
            slots.isEmpty() -> {
                EmptySlotState(modifier = Modifier.padding(padding))
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Doctor Info Card
                    DoctorInfoHeader(
                        doctorName = doctorName,
                        specialization = doctorSpecialization
                    )

                    // Calendar Section Header
                    SectionHeaderWithIcon(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Select Date",
                        subtitle = "Swipe to see more dates"
                    )

                    // Horizontal Swipeable Calendar
                    HorizontalDatePicker(
                        dates = uniqueDates,
                        selectedDate = selectedDate,
                        todayDate = todayString,
                        onDateSelected = { selectedDate = it }
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Time Slots Section
                    AnimatedVisibility(
                        visible = selectedDate.isNotEmpty(),
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                        exit = fadeOut(tween(200))
                    ) {
                        Column {
                            SectionHeaderWithIcon(
                                icon = Icons.Outlined.Schedule,
                                title = "Available Time Slots",
                                subtitle = formatDateForDisplay(selectedDate)
                            )

                            if (filteredSlots.isEmpty()) {
                                NoSlotsForDateCard()
                            } else {
                                TimeSlotGrid(
                                    slots = filteredSlots,
                                    allSlots = slots,
                                    onSlotClick = { slot ->
                                        if (slot.isBooked) {
                                            nearestSlot = slots.firstOrNull { !it.isBooked }
                                            if (nearestSlot != null) {
                                                showConflictDialog = true
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "No available slots at all",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            selectedSlot = slot
                                            showConfirmDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// DOCTOR INFO HEADER
// ============================================
@Composable
private fun DoctorInfoHeader(
    doctorName: String,
    specialization: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doctorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = specialization,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ============================================
// SECTION HEADER WITH ICON
// ============================================
@Composable
private fun SectionHeaderWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// HORIZONTAL DATE PICKER (IXIGO STYLE)
// ============================================
@Composable
private fun HorizontalDatePicker(
    dates: List<String>,
    selectedDate: String,
    todayDate: String,
    onDateSelected: (String) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll to selected date on first composition
    LaunchedEffect(dates, selectedDate) {
        val index = dates.indexOf(selectedDate)
        if (index >= 0) {
            listState.animateScrollToItem(maxOf(0, index - 1))
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
    ) {
        items(dates, key = { it }) { dateString ->
            DatePill(
                dateString = dateString,
                isSelected = selectedDate == dateString,
                isToday = dateString == todayDate,
                onClick = { onDateSelected(dateString) }
            )
        }
    }
}

@Composable
private fun DatePill(
    dateString: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isSelected -> Color.Transparent
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(72.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (isToday && !isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Parse and display date components
            val parsedDate = parseDateString(dateString)

            // Day of week
            Text(
                text = parsedDate.dayOfWeek,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Day number
            Text(
                text = parsedDate.day,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Month
            Text(
                text = parsedDate.month,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) contentColor.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Today indicator
            if (isToday) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                           else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ============================================
// TIME SLOT GRID (BOOKING.COM STYLE)
// ============================================
@Composable
private fun TimeSlotGrid(
    slots: List<Slot>,
    allSlots: List<Slot>,
    onSlotClick: (Slot) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxSize()
    ) {
        items(slots, key = { it.id }) { slot ->
            TimeSlotPill(
                slot = slot,
                onClick = { onSlotClick(slot) }
            )
        }
    }
}

@Composable
private fun TimeSlotPill(
    slot: Slot,
    onClick: () -> Unit
) {
    val isBooked = slot.isBooked

    val containerColor = when {
        isBooked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isBooked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.primary
    }

    val borderColor = when {
        isBooked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    }

    Surface(
        onClick = onClick,
        enabled = !isBooked,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (!isBooked) 1.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = slot.time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!isBooked) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            if (isBooked) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Booked",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ============================================
// EMPTY & LOADING STATES
// ============================================
@Composable
private fun EmptySlotState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = "No Slots Available",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = "This doctor has no available time slots at the moment.\nPlease try another doctor or check back later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun NoSlotsForDateCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "No slots available for this date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Please select another date",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SlotSelectionSkeleton(
    modifier: Modifier = Modifier,
    message: String
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {
        // Doctor info skeleton
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Date picker skeleton
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmerEffect()
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Section header skeleton
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Time slots grid skeleton
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            repeat(4) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Loading message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// ============================================
// HELPER FUNCTIONS
// ============================================
private data class ParsedDate(
    val dayOfWeek: String,
    val day: String,
    val month: String
)

private fun parseDateString(dateString: String): ParsedDate {
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        ParsedDate(
            dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            day = date.dayOfMonth.toString(),
            month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        )
    } catch (e: Exception) {
        // Fallback parsing for "YYYY-MM-DD" format
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthIndex = parts[1].toIntOrNull() ?: 1
            ParsedDate(
                dayOfWeek = "---",
                day = parts[2],
                month = monthNames.getOrElse(monthIndex) { "---" }
            )
        } else {
            ParsedDate("---", "--", "---")
        }
    }
}

private fun formatDateForDisplay(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
        date.format(formatter)
    } catch (e: Exception) {
        dateString
    }
}
