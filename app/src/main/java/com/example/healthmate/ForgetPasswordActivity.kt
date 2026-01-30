package com.example.healthmate

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
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
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xxl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Premium Logo
            Image(
                painter = painterResource(id = R.drawable.logo_final),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "Enter your email to receive a secure reset link",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xxxl))

            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email Address") },
                placeholder = { Text("e.g. name@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = HealthMateShapes.InputField,
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

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
                                    "Reset link sent to your email!",
                                    Toast.LENGTH_LONG
                                ).show()
                                activity.finish()
                            },
                            onFailure = { error ->
                                errorMessage = error.message ?: "Failed to send reset email"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = HealthMateShapes.ButtonLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Send Reset Link",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remembered password? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !isLoading) { activity.finish() }
                )
            }
        }
    }
}
