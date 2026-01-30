package com.example.healthmate.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

class AdminWellnessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = com.example.healthmate.util.ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) { AdminWellnessScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWellnessScreen() {
    val context = LocalContext.current
    var resources by remember { mutableStateOf<List<WellnessResource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedResource by remember { mutableStateOf<WellnessResource?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadResources() {
        coroutineScope.launch {
            isLoading = true
            resources = FirestoreHelper.getWellnessResources()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadResources() }

    // Detail Dialog
    selectedResource?.let { resource ->
        WellnessDetailDialog(
            resource = resource,
            onDismiss = { selectedResource = null }
        )
    }

    // Add Resource Dialog
    if (showAddDialog) {
        AddWellnessResourceDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, content, type, helpline ->
                    coroutineScope.launch {
                        val resource =
                                WellnessResource(
                                        title = title,
                                        content = content,
                                        type = type,
                                        helplineNumber = helpline
                                )
                        val result = FirestoreHelper.addWellnessResource(resource)
                        result.fold(
                                onSuccess = {
                                    Toast.makeText(context, "Resource added!", Toast.LENGTH_SHORT)
                                            .show()
                                    loadResources()
                                },
                                onFailure = { error ->
                                    Toast.makeText(
                                                    context,
                                                    "Failed: ${error.message}",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                                }
                        )
                    }
                    showAddDialog = false
                }
        )
    }

    Scaffold(
            topBar = {
                HealthMateTopBar(
                        title = "Wellness Resources",
                        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        onNavigationClick = { (context as? ComponentActivity)?.finish() }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) { Icon(Icons.Default.Add, "Add Resource") }
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
                                text = "No wellness resources",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text = "Tap + to add a resource",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                            AdminWellnessCard(
                                    resource = resource,
                                    onClick = { selectedResource = resource },
                                    onDelete = {
                                        coroutineScope.launch {
                                            FirestoreHelper.deleteWellnessResource(resource.id)
                                            loadResources()
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

@Composable
fun AdminWellnessCard(resource: WellnessResource, onClick: () -> Unit, onDelete: () -> Unit) {
    val isHelpline = resource.type.lowercase() == "helpline"

    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = HealthMateShapes.CardLarge,
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
                    tint = MaterialTheme.colorScheme.primary
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
                        color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWellnessResourceDialog(
        onDismiss: () -> Unit,
        onAdd: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Article") }
    var helpline by remember { mutableStateOf("") }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Wellness Resource") },
            text = {
                Column {
                    Row {
                        FilterChip(
                                selected = type == "Article",
                                onClick = { type = "Article" },
                                label = { Text("Article") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                                selected = type == "Helpline",
                                onClick = { type = "Helpline" },
                                label = { Text("Helpline") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val tfColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = tfColors
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (type == "Article") {
                        OutlinedTextField(
                                value = content,
                                onValueChange = { content = it },
                                label = { Text("Content") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                maxLines = 5,
                                colors = tfColors
                        )
                    } else {
                        OutlinedTextField(
                                value = helpline,
                                onValueChange = { helpline = it },
                                label = { Text("Helpline Number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = tfColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                                value = content,
                                onValueChange = { content = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                colors = tfColors
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onAdd(title, content, type, helpline) },
                        enabled =
                                title.isNotBlank() &&
                                        (content.isNotBlank() || helpline.isNotBlank())
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun WellnessDetailDialog(resource: WellnessResource, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(resource.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Type: ${resource.type}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (resource.type.lowercase() == "helpline") {
                    Text(
                        text = "Helpline: ${resource.helplineNumber}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = resource.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
