package com.example.healthmate

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

class ForgetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) { ForgetPasswordBody(isDarkMode) }
        }
    }
}

@Composable
fun ForgetPasswordBody(isDarkMode: Boolean = false) {
    val context = LocalContext.current
    val activity = context as Activity
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Theme Colors
    val cardBgColor =
            if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White.copy(alpha = 0.95f)
    val textColor = if (isDarkMode) MaterialTheme.colorScheme.onSurface else Color.Black
    val secondaryTextColor =
            if (isDarkMode) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
                painter = painterResource(R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isDarkMode) 0.3f else 1f
        )

        Column(
                modifier =
                        Modifier.align(Alignment.Center)
                                .background(cardBgColor, RoundedCornerShape(20.dp))
                                .padding(24.dp)
                                .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = "Forgot Password?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                    text = "Enter your email to receive a reset link",
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let { error ->
                Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email", color = secondaryTextColor) },
                    placeholder = { Text("Enter your email", color = secondaryTextColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    colors =
                            OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = secondaryTextColor
                            )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                    onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Please enter your email"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            val result = FirebaseAuthHelper.sendPasswordResetEmail(email.trim())
                            isLoading = false
                            result.fold(
                                    onSuccess = {
                                        Toast.makeText(
                                                        context,
                                                        "Password reset email sent! Check your inbox.",
                                                        Toast.LENGTH_LONG
                                                )
                                                .show()
                                        activity.finish()
                                    },
                                    onFailure = { error ->
                                        errorMessage = error.message ?: "Failed to send reset email"
                                    }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor =
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            ),
                    enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Send Reset Link", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                Text("Remembered your password? ", fontSize = 14.sp, color = secondaryTextColor)
                Text(
                        text = "Login",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !isLoading) { activity.finish() }
                )
            }
        }
    }
}
