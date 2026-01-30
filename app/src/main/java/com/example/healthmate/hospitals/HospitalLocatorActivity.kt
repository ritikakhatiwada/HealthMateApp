package com.example.healthmate.hospitals

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.healthmate.R
import com.example.healthmate.data.Hospital
import com.example.healthmate.ui.theme.*
import com.example.healthmate.util.LocationHelper
import com.example.healthmate.util.ThemeManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class HospitalLocatorActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                // Configure OSMDroid
                Configuration.getInstance().apply {
                        userAgentValue = packageName
                        load(
                                this@HospitalLocatorActivity,
                                getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
                        )
                }

                enableEdgeToEdge()
                setContent {
                        val themeManager = ThemeManager(this)
                        val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
                        HealthMateTheme(darkTheme = isDarkMode) {
                                HospitalLocatorScreen(onBack = { finish() })
                        }
                }
        }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HospitalLocatorScreen(onBack: () -> Unit) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val locationHelper = remember { LocationHelper(context) }
        val hospitalRepository = remember { HospitalRepository(context) }

        var hospitals by remember { mutableStateOf<List<Hospital>>(emptyList()) }
        var featuredHospitals by remember { mutableStateOf<List<Hospital>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var userLatitude by remember { mutableDoubleStateOf(0.0) }
        var userLongitude by remember { mutableDoubleStateOf(0.0) }
        var selectedHospital by remember { mutableStateOf<Hospital?>(null) }
        var selectedFilter by remember { mutableStateOf("All") }
        var selectedRadius by remember { mutableFloatStateOf(10f) } // Default 10km

        val locationPermissions =
                rememberMultiplePermissionsState(
                        permissions =
                                listOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                )

        // Function to load hospitals with current radius
        fun loadHospitals(lat: Double, lon: Double, radius: Float) {
                hospitals = hospitalRepository.getHospitalsWithinRadius(lat, lon, radius.toDouble())
                featuredHospitals =
                        hospitalRepository.getFeaturedHospitals(lat, lon, radius.toDouble(), 3)
        }

        // Load hospitals when permissions are granted
        LaunchedEffect(locationPermissions.allPermissionsGranted) {
                if (locationPermissions.allPermissionsGranted) {
                        isLoading = true
                        errorMessage = null
                        try {
                                val location = locationHelper.getCurrentLocation()
                                if (location != null) {
                                        userLatitude = location.latitude
                                        userLongitude = location.longitude

                                        // Load hospitals with current radius
                                        loadHospitals(
                                                location.latitude,
                                                location.longitude,
                                                selectedRadius
                                        )
                                } else {
                                        errorMessage = "Unable to get your location"
                                }
                        } catch (e: Exception) {
                                errorMessage = "Error: ${e.message}"
                        }
                        isLoading = false
                }
        }

        // Filter hospitals
        val filteredHospitals =
                when (selectedFilter) {
                        "Emergency" -> hospitals.filter { it.hasEmergency }
                        "Nearest" -> hospitals.sortedBy { it.distance }.take(5)
                        else -> hospitals
                }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Column {
                                                Text(
                                                        "Hospital Locator",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp
                                                )
                                                Text(
                                                        "Find nearby medical facilities",
                                                        fontSize = 12.sp,
                                                        color = Color.White.copy(alpha = 0.8f)
                                                )
                                        }
                                },
                                navigationIcon = {
                                        IconButton(onClick = onBack) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        "Back",
                                                        tint = Color.White
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                titleContentColor = Color.White
                                        )
                        )
                }
        ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when {
                                !locationPermissions.allPermissionsGranted -> {
                                        PermissionRequestScreen(
                                                onRequestPermission = {
                                                        locationPermissions
                                                                .launchMultiplePermissionRequest()
                                                }
                                        )
                                }
                                isLoading -> {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        CircularProgressIndicator(
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                                "Finding nearby hospitals...",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                }
                                        }
                                }
                                errorMessage != null -> {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally,
                                                        modifier = Modifier.padding(32.dp)
                                                ) {
                                                        Icon(
                                                                Icons.Default.ErrorOutline,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(64.dp),
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .error
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                                errorMessage ?: "An error occurred",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .error,
                                                                textAlign = TextAlign.Center
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Button(
                                                                onClick = {
                                                                        scope.launch {
                                                                                isLoading = true
                                                                                errorMessage = null
                                                                                try {
                                                                                        val location =
                                                                                                locationHelper
                                                                                                        .getCurrentLocation()
                                                                                        if (location !=
                                                                                                        null
                                                                                        ) {
                                                                                                userLatitude =
                                                                                                        location.latitude
                                                                                                userLongitude =
                                                                                                        location.longitude
                                                                                                hospitals =
                                                                                                        hospitalRepository
                                                                                                                .getSampleHospitals(
                                                                                                                        location.latitude,
                                                                                                                        location.longitude
                                                                                                                )
                                                                                        }
                                                                                } catch (
                                                                                        e:
                                                                                                Exception) {
                                                                                        errorMessage =
                                                                                                "Error: ${e.message}"
                                                                                }
                                                                                isLoading = false
                                                                        }
                                                                }
                                                        ) { Text("Retry") }
                                                }
                                        }
                                }
                                else -> {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                                // Map Section with OSMDroid
                                                Box(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .height(230.dp)
                                                ) {
                                                        OSMMapView(
                                                                userLat = userLatitude,
                                                                userLon = userLongitude,
                                                                hospitals = filteredHospitals,
                                                                selectedHospital = selectedHospital,
                                                                onHospitalSelected = {
                                                                        selectedHospital = it
                                                                }
                                                        )

                                                        // Location Badge
                                                        Surface(
                                                                modifier =
                                                                        Modifier.align(
                                                                                        Alignment
                                                                                                .TopStart
                                                                                )
                                                                                .padding(12.dp),
                                                                shape = RoundedCornerShape(20.dp),
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                shadowElevation = 4.dp
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        horizontal =
                                                                                                12.dp,
                                                                                        vertical =
                                                                                                6.dp
                                                                                ),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Icon(
                                                                                Icons.Default
                                                                                        .MyLocation,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint = Color.White,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                16.dp
                                                                                        )
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                6.dp
                                                                                        )
                                                                        )
                                                                        Text(
                                                                                "Your Location",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .labelSmall,
                                                                                color = Color.White,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .SemiBold
                                                                        )
                                                                }
                                                        }
                                                }

                                                // Radius Selector
                                                RadiusSelector(
                                                        selectedRadius = selectedRadius,
                                                        onRadiusChange = { newRadius ->
                                                                selectedRadius = newRadius
                                                                scope.launch {
                                                                        loadHospitals(
                                                                                userLatitude,
                                                                                userLongitude,
                                                                                newRadius
                                                                        )
                                                                }
                                                        }
                                                )

                                                // Featured Hospitals Section
                                                if (featuredHospitals.isNotEmpty()) {
                                                        FeaturedHospitalsSection(
                                                                hospitals = featuredHospitals,
                                                                onHospitalClick = {
                                                                        selectedHospital = it
                                                                }
                                                        )
                                                }

                                                // Filter Chips
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                horizontal = 16.dp,
                                                                                vertical = 12.dp
                                                                        ),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(8.dp)
                                                ) {
                                                        FilterChipItem(
                                                                text = "All (${hospitals.size})",
                                                                isSelected =
                                                                        selectedFilter == "All",
                                                                onClick = { selectedFilter = "All" }
                                                        )
                                                        FilterChipItem(
                                                                text = "Emergency",
                                                                isSelected =
                                                                        selectedFilter ==
                                                                                "Emergency",
                                                                onClick = {
                                                                        selectedFilter = "Emergency"
                                                                }
                                                        )
                                                        FilterChipItem(
                                                                text = "Nearest",
                                                                isSelected =
                                                                        selectedFilter == "Nearest",
                                                                onClick = {
                                                                        selectedFilter = "Nearest"
                                                                }
                                                        )
                                                }

                                                // Header
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .background(
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surfaceVariant
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.3f
                                                                                        )
                                                                        )
                                                                        .padding(
                                                                                horizontal = 16.dp,
                                                                                vertical = 10.dp
                                                                        ),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween,
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Icon(
                                                                        Icons.Outlined
                                                                                .LocalHospital,
                                                                        contentDescription = null,
                                                                        tint =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary,
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(8.dp)
                                                                )
                                                                Text(
                                                                        "Nearby Hospitals",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .titleMedium,
                                                                        fontWeight = FontWeight.Bold
                                                                )
                                                        }
                                                        Text(
                                                                "${filteredHospitals.size} found",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                }

                                                // Hospital List
                                                LazyColumn(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentPadding = PaddingValues(16.dp),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(12.dp)
                                                ) {
                                                        items(filteredHospitals) { hospital ->
                                                                HospitalCard(
                                                                        hospital = hospital,
                                                                        isSelected =
                                                                                selectedHospital
                                                                                        ?.id ==
                                                                                        hospital.id,
                                                                        onClick = {
                                                                                selectedHospital =
                                                                                        hospital
                                                                        },
                                                                        onCall = {
                                                                                if (hospital.phoneNumber
                                                                                                .isNotEmpty()
                                                                                ) {
                                                                                        val intent =
                                                                                                Intent(
                                                                                                                Intent.ACTION_DIAL
                                                                                                        )
                                                                                                        .apply {
                                                                                                                data =
                                                                                                                        Uri.parse(
                                                                                                                                "tel:${hospital.phoneNumber}"
                                                                                                                        )
                                                                                                        }
                                                                                        context.startActivity(
                                                                                                intent
                                                                                        )
                                                                                }
                                                                        },
                                                                        onDirections = {
                                                                                // Use geo: intent
                                                                                // for directions
                                                                                // (works with any
                                                                                // map
                                                                                // app)
                                                                                val uri =
                                                                                        Uri.parse(
                                                                                                "geo:${hospital.latitude},${hospital.longitude}?q=${hospital.latitude},${hospital.longitude}(${Uri.encode(hospital.name)})"
                                                                                        )
                                                                                val intent =
                                                                                        Intent(
                                                                                                Intent.ACTION_VIEW,
                                                                                                uri
                                                                                        )
                                                                                // Try to open with
                                                                                // any map app,
                                                                                // fallback to
                                                                                // browser
                                                                                if (intent.resolveActivity(
                                                                                                context.packageManager
                                                                                        ) != null
                                                                                ) {
                                                                                        context.startActivity(
                                                                                                intent
                                                                                        )
                                                                                } else {
                                                                                        // Fallback
                                                                                        // to
                                                                                        // OpenStreetMap in browser
                                                                                        val webUri =
                                                                                                Uri.parse(
                                                                                                        "https://www.openstreetmap.org/directions?from=$userLatitude,$userLongitude&to=${hospital.latitude},${hospital.longitude}"
                                                                                                )
                                                                                        context.startActivity(
                                                                                                Intent(
                                                                                                        Intent.ACTION_VIEW,
                                                                                                        webUri
                                                                                                )
                                                                                        )
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

@Composable
private fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
        Surface(
                onClick = onClick,
                color =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border =
                        if (!isSelected)
                                androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline
                                )
                        else null,
                shadowElevation = if (isSelected) 2.dp else 0.dp
        ) {
                Text(
                        text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
        }
}

@Composable
private fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
        Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
                Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(80.dp)
                ) {
                        Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxSize().padding(20.dp)
                        )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                        "Location Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                        "To find hospitals near you, we need access to your location. Your location data is only used to show nearby hospitals.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enable Location", modifier = Modifier.padding(vertical = 8.dp))
                }
        }
}

/** OSMDroid Map View - OpenStreetMap implementation No API key required, free and open source */
@Composable
private fun OSMMapView(
        userLat: Double,
        userLon: Double,
        hospitals: List<Hospital>,
        selectedHospital: Hospital?,
        onHospitalSelected: (Hospital) -> Unit
) {
        val context = LocalContext.current
        val userLocation = remember(userLat, userLon) { GeoPoint(userLat, userLon) }

        AndroidView(
                factory = { ctx ->
                        MapView(ctx).apply {
                                // Set tile source (OpenStreetMap)
                                setTileSource(TileSourceFactory.MAPNIK)

                                // Enable multi-touch controls
                                setMultiTouchControls(true)

                                // Set initial position and zoom
                                controller.setZoom(14.0)
                                controller.setCenter(userLocation)

                                // Add user location marker
                                val userMarker =
                                        Marker(this).apply {
                                                position = userLocation
                                                setAnchor(
                                                        Marker.ANCHOR_CENTER,
                                                        Marker.ANCHOR_BOTTOM
                                                )
                                                title = "Your Location"
                                                // Use default blue marker for user location
                                                icon =
                                                        ContextCompat.getDrawable(
                                                                ctx,
                                                                android.R
                                                                        .drawable
                                                                        .ic_menu_mylocation
                                                        )
                                        }
                                overlays.add(userMarker)

                                // Add hospital markers
                                hospitals.forEach { hospital ->
                                        val hospitalMarker =
                                                Marker(this).apply {
                                                        position =
                                                                GeoPoint(
                                                                        hospital.latitude,
                                                                        hospital.longitude
                                                                )
                                                        setAnchor(
                                                                Marker.ANCHOR_CENTER,
                                                                Marker.ANCHOR_BOTTOM
                                                        )
                                                        title = hospital.name
                                                        snippet = hospital.formattedDistance
                                                        // Use default red marker for hospitals
                                                        icon =
                                                                ContextCompat.getDrawable(
                                                                        ctx,
                                                                        android.R
                                                                                .drawable
                                                                                .ic_dialog_map
                                                                )

                                                        setOnMarkerClickListener { marker, _ ->
                                                                onHospitalSelected(hospital)
                                                                marker.showInfoWindow()
                                                                true
                                                        }
                                                }
                                        overlays.add(hospitalMarker)
                                }

                                invalidate()
                        }
                },
                update = { mapView ->
                        // Update map when data changes
                        mapView.overlays.clear()

                        // Re-add user marker
                        val userMarker =
                                Marker(mapView).apply {
                                        position = userLocation
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        title = "Your Location"
                                        icon =
                                                ContextCompat.getDrawable(
                                                        context,
                                                        android.R.drawable.ic_menu_mylocation
                                                )
                                }
                        mapView.overlays.add(userMarker)

                        // Re-add hospital markers
                        hospitals.forEach { hospital ->
                                val hospitalMarker =
                                        Marker(mapView).apply {
                                                position =
                                                        GeoPoint(
                                                                hospital.latitude,
                                                                hospital.longitude
                                                        )
                                                setAnchor(
                                                        Marker.ANCHOR_CENTER,
                                                        Marker.ANCHOR_BOTTOM
                                                )
                                                title = hospital.name
                                                snippet = hospital.formattedDistance
                                                icon =
                                                        ContextCompat.getDrawable(
                                                                context,
                                                                android.R.drawable.ic_dialog_map
                                                        )

                                                setOnMarkerClickListener { marker, _ ->
                                                        onHospitalSelected(hospital)
                                                        marker.showInfoWindow()
                                                        true
                                                }
                                        }
                                mapView.overlays.add(hospitalMarker)
                        }

                        // Center on selected hospital if any
                        selectedHospital?.let {
                                mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude))
                        }

                        mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
        )
}

@Composable
private fun HospitalCard(
        hospital: Hospital,
        isSelected: Boolean,
        onClick: () -> Unit,
        onCall: () -> Unit,
        onDirections: () -> Unit
) {
        val animatedElevation by
                animateDpAsState(
                        targetValue = if (isSelected) 8.dp else 2.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "elevation"
                )

        Card(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        if (isSelected)
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.3f
                                                )
                                        else MaterialTheme.colorScheme.surface
                        ),
                border =
                        if (isSelected)
                                androidx.compose.foundation.BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                )
                        else null
        ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                        ) {
                                Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.Top
                                ) {
                                        Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(48.dp)
                                        ) {
                                                Icon(
                                                        Icons.Default.LocalHospital,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(12.dp)
                                                )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                        text = hospital.name,
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Text(
                                                        text = hospital.address,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                )
                                        }
                                }

                                Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                        Text(
                                                text = hospital.formattedDistance,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color =
                                                        MaterialTheme.colorScheme
                                                                .onSecondaryContainer,
                                                modifier =
                                                        Modifier.padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                        )
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rating and badges
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                // Rating
                                // Row(verticalAlignment = Alignment.CenterVertically) {
                                //         Icon(
                                //                 Icons.Default.Star,
                                //                 contentDescription = null,
                                //                 tint = Color(0xFFFFC107),
                                //                 modifier = Modifier.size(16.dp)
                                //         )
                                //         Spacer(modifier = Modifier.width(4.dp))
                                //         Text(
                                //                 text = hospital.formattedRating,
                                //                 style = MaterialTheme.typography.labelMedium,
                                //                 fontWeight = FontWeight.SemiBold
                                //         )
                                //         if (hospital.totalRatings > 0) {
                                //                 Text(
                                //                         text = " (${hospital.totalRatings})",
                                //                         style =
                                // MaterialTheme.typography.labelSmall,
                                //                         color =
                                //                                 MaterialTheme.colorScheme
                                //                                         .onSurfaceVariant
                                //                 )
                                //         }
                                // }

                                Spacer(modifier = Modifier.weight(1f))

                                // Emergency badge
                                if (hospital.hasEmergency) {
                                        Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AlertRed.copy(alpha = 0.15f)
                                        ) {
                                                Row(
                                                        modifier =
                                                                Modifier.padding(
                                                                        horizontal = 6.dp,
                                                                        vertical = 3.dp
                                                                ),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                Icons.Default.Warning,
                                                                contentDescription = null,
                                                                tint = AlertRed,
                                                                modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                                "Emergency",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelSmall,
                                                                color = AlertRed,
                                                                fontWeight = FontWeight.SemiBold
                                                        )
                                                }
                                        }
                                }

                                // Open status
                                Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color =
                                                if (hospital.isOpen) SafeGreen.copy(alpha = 0.15f)
                                                else AlertRed.copy(alpha = 0.15f)
                                ) {
                                        Text(
                                                text = if (hospital.isOpen) "Open" else "Closed",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color =
                                                        if (hospital.isOpen) SafeGreen
                                                        else AlertRed,
                                                modifier =
                                                        Modifier.padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                        )
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                OutlinedButton(
                                        onClick = onCall,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        enabled = hospital.phoneNumber.isNotEmpty()
                                ) {
                                        Icon(
                                                Icons.Default.Phone,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Call")
                                }

                                Button(
                                        onClick = onDirections,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.primary
                                                )
                                ) {
                                        Icon(
                                                Icons.Default.Directions,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Directions")
                                }
                        }
                }
        }
}

// ============================================
// RADIUS SELECTOR
// ============================================
@Composable
private fun RadiusSelector(selectedRadius: Float, onRadiusChange: (Float) -> Unit) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        Icons.Outlined.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        text = "Search Radius",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                )
                        }
                        Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                                Text(
                                        text = "${selectedRadius.toInt()} km",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier =
                                                Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 4.dp
                                                )
                                )
                        }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                        value = selectedRadius,
                        onValueChange = onRadiusChange,
                        valueRange = 5f..100f,
                        steps = 18, // 5, 10, 15, 20... 100
                        colors =
                                SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor =
                                                MaterialTheme.colorScheme.surfaceVariant
                                )
                )

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Text(
                                "5 km",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                "50 km",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                "100 km",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}

// ============================================
// FEATURED HOSPITALS SECTION
// ============================================
@Composable
private fun FeaturedHospitalsSection(
        hospitals: List<Hospital>,
        onHospitalClick: (Hospital) -> Unit
) {
        Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Icon(
                        //         imageVector = Icons.Default.Star,
                        //         contentDescription = null,
                        //         tint = Color(0xFFFFC107),
                        //         modifier = Modifier.size(20.dp)
                        // )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = "Top Rated Hospitals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                }

                LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        items(hospitals) { hospital ->
                                FeaturedHospitalChip(
                                        hospital = hospital,
                                        onClick = { onHospitalClick(hospital) }
                                )
                        }
                }
        }
}

@Composable
private fun FeaturedHospitalChip(hospital: Hospital, onClick: () -> Unit) {
        Surface(
                onClick = onClick,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
                Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                        ) {
                                Icon(
                                        Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.fillMaxSize().padding(10.dp)
                                )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                                Text(
                                        text = hospital.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Icon(
                                        //         Icons.Default.Star,
                                        //         contentDescription = null,
                                        //         tint = Color(0xFFFFC107),
                                        //         modifier = Modifier.size(14.dp)
                                        // )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                                text = hospital.formattedRating,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                                text = " | ${hospital.formattedDistance}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                        }
                }
        }
}
