package com.example.healthmate

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmate.model.ChatMessage
import com.example.healthmate.ui.theme.HealthMateTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class ChatbotActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContent { HealthMateTheme { ChatbotBody() } }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotBody(viewModel: ChatbotViewModel = viewModel()) {
        val context = LocalContext.current
        val activity = context as? Activity
        val messages by viewModel.messages.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        var userInput by remember { mutableStateOf("") }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                        coroutineScope.launch { listState.animateScrollToItem(messages.size - 1) }
                }
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                                IconButton(onClick = { activity?.finish() }) {
                                                        Icon(
                                                                Icons.Default.ArrowBack,
                                                                contentDescription = "Back",
                                                                tint = Color.White
                                                        )
                                                }
                                                Box(
                                                        modifier =
                                                                Modifier.size(36.dp)
                                                                        .background(
                                                                                Color.White.copy(
                                                                                        alpha = 0.2f
                                                                                ),
                                                                                CircleShape
                                                                        ),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Icon(
                                                                Icons.Default.SmartToy,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(20.dp)
                                                        )
                                                }
                                                Column {
                                                        Text(
                                                                text = "HealthMate AI",
                                                                color = Color.White,
                                                                fontSize = 16.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                                text = "Online",
                                                                color =
                                                                        Color.White.copy(
                                                                                alpha = 0.8f
                                                                        ),
                                                                fontSize = 12.sp
                                                        )
                                                }
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                        )
                        )
                },
                containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        LazyColumn(
                                state = listState,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                if (messages.isEmpty()) {
                                        item {
                                                Box(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(top = 40.dp),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Text(
                                                                "Hi 👋 How can I help you today?",
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                }
                                        }
                                }

                                items(messages) { message -> ChatMessageItem(message = message) }

                                if (isLoading) {
                                        item { TypingIndicator() }
                                }
                        }

                        // Quick Questions (only if few messages)
                        if (messages.size <= 1) {
                                LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                        val topics =
                                                listOf(
                                                        "Flu symptoms",
                                                        "Reduce fever",
                                                        "Better sleep",
                                                        "Healthy diet"
                                                )
                                        items(topics) { topic ->
                                                QuickQuestionButton(
                                                        text = topic,
                                                        onClick = { viewModel.sendMessage(topic) }
                                                )
                                        }
                                }
                        }

                        // Input Area
                        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        OutlinedTextField(
                                                value = userInput,
                                                onValueChange = { userInput = it },
                                                placeholder = { Text("Type a message...") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(24.dp),
                                                colors =
                                                        OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                unfocusedBorderColor =
                                                                        MaterialTheme.colorScheme
                                                                                .outline
                                                        ),
                                                maxLines = 3
                                        )

                                        FloatingActionButton(
                                                onClick = {
                                                        if (userInput.isNotBlank() && !isLoading) {
                                                                viewModel.sendMessage(userInput)
                                                                userInput = ""
                                                        }
                                                },
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White,
                                                elevation =
                                                        FloatingActionButtonDefaults.elevation(
                                                                2.dp
                                                        ),
                                                modifier = Modifier.size(48.dp)
                                        ) {
                                                Icon(
                                                        Icons.Default.Send,
                                                        contentDescription = "Send",
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
        val isUser = message.isUser
        val bubbleColor =
                if (isUser) MaterialTheme.colorScheme.primary
                else Color(0xFFE6F6EF) // Soft Mint for Bot
        val textColor = if (isUser) Color.White else Color(0xFF1F2933)
        val alignment = if (isUser) Alignment.End else Alignment.Start
        val shape =
                if (isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                Surface(
                        color = bubbleColor,
                        shape = shape,
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 280.dp)
                ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                        text = message.text,
                                        color = textColor,
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp
                                )
                                Text(
                                        text = formatTimestamp(message.timestamp),
                                        fontSize = 10.sp,
                                        color = textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                                )
                        }
                }
        }
}

@Composable
fun TypingIndicator() {
        Surface(
                color = Color(0xFFE6F6EF),
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                modifier = Modifier.padding(top = 4.dp)
        ) {
                Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                        repeat(3) {
                                Box(
                                        modifier =
                                                Modifier.size(6.dp)
                                                        .background(
                                                                MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                        )
                                )
                        }
                }
        }
}

@Composable
fun QuickQuestionButton(text: String, onClick: () -> Unit) {
        Surface(
                shape = RoundedCornerShape(16.dp),
                border =
                        androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                color = Color.White,
                modifier = Modifier.clickable(onClick = onClick)
        ) {
                Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                )
        }
}

fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
}
