package com.example.healthmate.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.healthmate.ui.theme.HealthMateTheme
import kotlinx.coroutines.launch

class AdminWellnessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HealthMateTheme { AdminWellnessScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWellnessScreen() {
    val context = LocalContext.current
    var resources by remember { mutableStateOf<List<WellnessResource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadResources() {
        coroutineScope.launch {
            isLoading = true
            resources = FirestoreHelper.getWellnessResources()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadResources() }

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
                TopAppBar(
                        title = { Text("Wellness Resources", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color(0xFFFF9800)
                                )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = Color(0xFFFF9800)
                ) { Icon(Icons.Default.Add, "Add Resource", tint = Color.White) }
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFFFF9800)
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
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No wellness resources", fontSize = 18.sp, color = Color.Gray)
                        Text(text = "Tap + to add a resource", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(resources) { resource ->
                            AdminWellnessCard(
                                    resource = resource,
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
fun AdminWellnessCard(resource: WellnessResource, onDelete: () -> Unit) {
    val isHelpline = resource.type.lowercase() == "helpline"

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = if (isHelpline) Icons.Default.Phone else Icons.Default.Article,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = resource.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                        text =
                                if (isHelpline) resource.helplineNumber
                                else resource.content.take(50) + "...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2
                )
                Text(text = resource.type, fontSize = 12.sp, color = Color(0xFFFF9800))
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
                    OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (type == "Article") {
                        OutlinedTextField(
                                value = content,
                                onValueChange = { content = it },
                                label = { Text("Content") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                maxLines = 5
                        )
                    } else {
                        OutlinedTextField(
                                value = helpline,
                                onValueChange = { helpline = it },
                                label = { Text("Helpline Number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                                value = content,
                                onValueChange = { content = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
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
