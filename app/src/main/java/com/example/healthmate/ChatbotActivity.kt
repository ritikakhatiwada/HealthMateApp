package com.example.healthmate

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmate.model.ChatMessage
import com.example.healthmate.presentation.common.MedicalDisclaimer
import com.example.healthmate.ui.components.HealthMateTextField
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatbotActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContent {
                        val themeManager = ThemeManager(this)
                        val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
                        HealthMateTheme(darkTheme = isDarkMode) { ChatbotBody() }
                }
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
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                Icon(
                                                        Icons.Default.SmartToy,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                        text = "HealthMate AI",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        }
                                },
                                navigationIcon = {
                                        IconButton(onClick = { activity?.finish() }) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back",
                                                        tint = Color.White
                                                )
                                        }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
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
                                contentPadding =
                                        PaddingValues(
                                                horizontal = Spacing.lg,
                                                vertical = Spacing.xl
                                        ),
                                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                        ) {
                                // Medical Disclaimer - Always shown at top
                                item {
                                        MedicalDisclaimer(
                                                modifier = Modifier.padding(bottom = Spacing.md)
                                        )
                                }

                                if (messages.isEmpty()) {
                                        item {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(top = 40.dp),
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Surface(
                                                                shape = CircleShape,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primaryContainer
                                                                                .copy(alpha = 0.4f),
                                                                modifier = Modifier.size(100.dp)
                                                        ) {
                                                                Icon(
                                                                        Icons.Default.SmartToy,
                                                                        contentDescription = null,
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        24.dp
                                                                                ),
                                                                        tint =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                )
                                                        }
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(Spacing.xl)
                                                        )
                                                        Text(
                                                                text = "HealthMate AI",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .headlineMedium,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(Spacing.sm)
                                                        )
                                                        Text(
                                                                text =
                                                                        "Your personalized health companion.\nHow can I help you today?",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyLarge,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant,
                                                                textAlign =
                                                                        androidx.compose.ui.text
                                                                                .style.TextAlign
                                                                                .Center,
                                                                lineHeight = 24.sp
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
                                Column(modifier = Modifier.padding(bottom = Spacing.sm)) {
                                        Text(
                                                text = "Quick questions",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier =
                                                        Modifier.padding(
                                                                horizontal = Spacing.lg,
                                                                vertical = Spacing.xs
                                                        )
                                        )
                                        LazyRow(
                                                contentPadding =
                                                        PaddingValues(horizontal = Spacing.lg),
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(Spacing.sm)
                                        ) {
                                                val topics =
                                                        listOf(
                                                                "What are flu symptoms?",
                                                                "How to reduce fever?",
                                                                "Tips for better sleep",
                                                                "Healthy diet plan"
                                                        )
                                                items(topics) { topic ->
                                                        QuickQuestionButton(
                                                                text = topic,
                                                                onClick = {
                                                                        viewModel.sendMessage(topic)
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }

                        // Modern Floating Input Area
                        Surface(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(
                                                        horizontal = Spacing.lg,
                                                        vertical = Spacing.md
                                                ),
                                color = Color.Transparent
                        ) {
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .background(
                                                                color = MaterialTheme.colorScheme.surface,
                                                                shape = RoundedCornerShape(24.dp)
                                                        )
                                                        .border(
                                                                width = 1.dp,
                                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                                shape = RoundedCornerShape(24.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Spacer(modifier = Modifier.width(12.dp))

                                        HealthMateTextField(
                                                value = userInput,
                                                onValueChange = { userInput = it },
                                                label = "",
                                                placeholder = "Ask me anything...",
                                                modifier = Modifier.weight(1f),
                                                enabled = !isLoading,
                                                imeAction = ImeAction.Send,
                                                onImeAction = {
                                                        if (userInput.isNotBlank() && !isLoading) {
                                                                viewModel.sendMessage(userInput)
                                                                userInput = ""
                                                        }
                                                },
                                                maxLines = 4
                                        )

                                        IconButton(
                                                onClick = {
                                                        if (userInput.isNotBlank() && !isLoading) {
                                                                viewModel.sendMessage(userInput)
                                                                userInput = ""
                                                        }
                                                },
                                                enabled = userInput.isNotBlank() && !isLoading,
                                                modifier =
                                                        Modifier.size(48.dp)
                                                                .background(
                                                                        color =
                                                                                if (userInput
                                                                                                .isNotBlank()
                                                                                )
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .surfaceVariant,
                                                                        shape = CircleShape
                                                                )
                                        ) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.Send,
                                                        contentDescription = "Send",
                                                        tint =
                                                                if (userInput.isNotBlank())
                                                                        Color.White
                                                                else
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        }
                                }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                }
        }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
        val isUser = message.isUser
        val bubbleColor =
                if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
        val alignment = if (isUser) Alignment.End else Alignment.Start
        val shape =
                if (isUser) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)

        Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalAlignment = alignment
        ) {
                Surface(
                        color = bubbleColor,
                        shape = shape,
                        shadowElevation = if (isUser) 2.dp else 0.dp,
                        modifier = Modifier.widthIn(max = 300.dp)
                ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                        text = message.text,
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyLarge,
                                        lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                        text = formatTimestamp(message.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor.copy(alpha = 0.6f),
                                        modifier = Modifier.align(Alignment.End)
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
                shape = RoundedCornerShape(20.dp),
                border =
                        androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.clickable(onClick = onClick)
        ) {
                Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                )
        }
}

fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
}
