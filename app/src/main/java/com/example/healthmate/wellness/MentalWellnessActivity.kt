package com.example.healthmate.wellness

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.data.FirestoreHelper
import com.example.healthmate.model.WellnessResource
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.ui.theme.HealthMateTheme
import kotlinx.coroutines.launch

class MentalWellnessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HealthMateTheme { MentalWellnessScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentalWellnessScreen() {
    val context = LocalContext.current
    var resources by remember { mutableStateOf<List<WellnessResource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedResource by remember { mutableStateOf<WellnessResource?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            resources = FirestoreHelper.getWellnessResources()
            isLoading = false
        }
    }

    // Detail dialog
    selectedResource?.let { resource ->
        AlertDialog(
                onDismissRequest = { selectedResource = null },
                title = { Text(resource.title) },
                text = {
                    Column {
                        Text(resource.content)
                        if (resource.helplineNumber.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                    text = "Helpline: ${resource.helplineNumber}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                confirmButton = {
                    if (resource.helplineNumber.isNotBlank()) {
                        Button(
                                onClick = {
                                    val intent =
                                            Intent(
                                                    Intent.ACTION_DIAL,
                                                    Uri.parse("tel:${resource.helplineNumber}")
                                            )
                                    context.startActivity(intent)
                                }
                        ) {
                            Icon(Icons.Default.Call, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedResource = null }) { Text("Close") }
                }
        )
    }

    Scaffold(
            topBar = {
                HealthMateTopBar(
                        title = "Mental Wellness",
                        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        onNavigationClick = { (context as? ComponentActivity)?.finish() }
                )
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                    )
                }
                resources.isEmpty() -> {
                    Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Text(
                                text = "No resources available",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(resources) { resource ->
                            WellnessResourceCard(resource = resource) {
                                selectedResource = resource
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WellnessResourceCard(resource: WellnessResource, onClick: () -> Unit) {
    val isHelpline = resource.type.lowercase() == "helpline"

    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = HealthMateShapes.CardLarge,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (isHelpline)
                                            MaterialTheme.colorScheme.errorContainer.copy(
                                                    alpha = 0.1f
                                            )
                                    else MaterialTheme.colorScheme.surface
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = if (isHelpline) Icons.Default.Phone else Icons.AutoMirrored.Filled.Article,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint =
                            if (isHelpline) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = resource.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                Text(
                        text =
                                if (isHelpline) resource.helplineNumber
                                else resource.content.take(50) + "...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                )
                Text(
                        text = resource.type,
                        style = MaterialTheme.typography.labelMedium,
                        color =
                                if (isHelpline) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
