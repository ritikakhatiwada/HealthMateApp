package com.example.healthmate

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

class SignUpActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContent {
                        val themeManager = ThemeManager(this)
                        val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
                        HealthMateTheme(darkTheme = isDarkMode) { SignUpBody(isDarkMode) }
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpBody(isDarkMode: Boolean = false) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val activity = context as Activity
        val coroutineScope = rememberCoroutineScope()

        // Theme Colors
        val cardBgColor =
                if (isDarkMode) MaterialTheme.colorScheme.surface
                else Color.White.copy(alpha = 0.95f)
        val textColor = if (isDarkMode) MaterialTheme.colorScheme.onSurface else Color.Black
        val secondaryTextColor =
                if (isDarkMode) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray

        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                                        text = "Create Account",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
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
                                        value = name,
                                        onValueChange = {
                                                name = it
                                                errorMessage = null
                                        },
                                        label = { Text("Full Name", color = secondaryTextColor) },
                                        placeholder = {
                                                Text("Enter your name", color = secondaryTextColor)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        enabled = !isLoading,
                                        colors =
                                                OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = textColor,
                                                        unfocusedTextColor = textColor,
                                                        focusedBorderColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = secondaryTextColor
                                                )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                        value = email,
                                        onValueChange = {
                                                email = it
                                                errorMessage = null
                                        },
                                        label = { Text("Email", color = secondaryTextColor) },
                                        placeholder = {
                                                Text("Enter your email", color = secondaryTextColor)
                                        },
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        enabled = !isLoading,
                                        colors =
                                                OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = textColor,
                                                        unfocusedTextColor = textColor,
                                                        focusedBorderColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = secondaryTextColor
                                                )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                        value = password,
                                        onValueChange = {
                                                password = it
                                                errorMessage = null
                                        },
                                        label = { Text("Password", color = secondaryTextColor) },
                                        placeholder = {
                                                Text(
                                                        "Enter your password",
                                                        color = secondaryTextColor
                                                )
                                        },
                                        visualTransformation =
                                                if (passwordVisible) VisualTransformation.None
                                                else PasswordVisualTransformation(),
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Password
                                                ),
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = {
                                                                passwordVisible = !passwordVisible
                                                        }
                                                ) {
                                                        Icon(
                                                                painter =
                                                                        painterResource(
                                                                                if (passwordVisible)
                                                                                        R.drawable
                                                                                                .outline_visibility_off_24
                                                                                else
                                                                                        R.drawable
                                                                                                .baseline_visibility_24
                                                                        ),
                                                                contentDescription = null,
                                                                tint = secondaryTextColor
                                                        )
                                                }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        enabled = !isLoading,
                                        colors =
                                                OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = textColor,
                                                        unfocusedTextColor = textColor,
                                                        focusedBorderColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = secondaryTextColor
                                                )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = {
                                                confirmPassword = it
                                                errorMessage = null
                                        },
                                        label = {
                                                Text("Confirm Password", color = secondaryTextColor)
                                        },
                                        placeholder = {
                                                Text(
                                                        "Re-enter your password",
                                                        color = secondaryTextColor
                                                )
                                        },
                                        visualTransformation =
                                                if (confirmPasswordVisible)
                                                        VisualTransformation.None
                                                else PasswordVisualTransformation(),
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Password
                                                ),
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = {
                                                                confirmPasswordVisible =
                                                                        !confirmPasswordVisible
                                                        }
                                                ) {
                                                        Icon(
                                                                painter =
                                                                        painterResource(
                                                                                if (confirmPasswordVisible
                                                                                )
                                                                                        R.drawable
                                                                                                .outline_visibility_off_24
                                                                                else
                                                                                        R.drawable
                                                                                                .baseline_visibility_24
                                                                        ),
                                                                contentDescription = null,
                                                                tint = secondaryTextColor
                                                        )
                                                }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        enabled = !isLoading,
                                        colors =
                                                OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = textColor,
                                                        unfocusedTextColor = textColor,
                                                        focusedBorderColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = secondaryTextColor
                                                )
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                        onClick = {
                                                when {
                                                        name.isBlank() -> {
                                                                errorMessage =
                                                                        "Please enter your name"
                                                                return@Button
                                                        }
                                                        email.isBlank() -> {
                                                                errorMessage =
                                                                        "Please enter your email"
                                                                return@Button
                                                        }
                                                        password.isBlank() -> {
                                                                errorMessage =
                                                                        "Please enter a password"
                                                                return@Button
                                                        }
                                                        password.length < 6 -> {
                                                                errorMessage =
                                                                        "Password must be at least 6 characters"
                                                                return@Button
                                                        }
                                                        password != confirmPassword -> {
                                                                errorMessage =
                                                                        "Passwords do not match"
                                                                return@Button
                                                        }
                                                }

                                                isLoading = true
                                                errorMessage = null

                                                coroutineScope.launch {
                                                        val result =
                                                                FirebaseAuthHelper.signUp(
                                                                        email = email.trim(),
                                                                        password = password,
                                                                        name = name.trim()
                                                                )
                                                        isLoading = false
                                                        result.fold(
                                                                onSuccess = {
                                                                        Toast.makeText(
                                                                                        context,
                                                                                        "Account created successfully!",
                                                                                        Toast.LENGTH_SHORT
                                                                                )
                                                                                .show()
                                                                        val intent =
                                                                                Intent(
                                                                                        context,
                                                                                        UserDashBoardActivity::class
                                                                                                .java
                                                                                )
                                                                        intent.flags =
                                                                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                                        context.startActivity(
                                                                                intent
                                                                        )
                                                                        activity.finish()
                                                                },
                                                                onFailure = { error ->
                                                                        errorMessage =
                                                                                error.message
                                                                                        ?: "Sign up failed"
                                                                }
                                                        )
                                                }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        disabledContainerColor =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.6f)
                                                ),
                                        enabled = !isLoading
                                ) {
                                        if (isLoading) {
                                                CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = Color.White
                                                )
                                        } else {
                                                Text(
                                                        "Sign Up",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row {
                                        Text(
                                                "Already have an account? ",
                                                fontSize = 14.sp,
                                                color = secondaryTextColor
                                        )
                                        Text(
                                                text = "Login",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier =
                                                        Modifier.clickable(enabled = !isLoading) {
                                                                activity.finish()
                                                        }
                                        )
                                }
                        }
                }
        }
}
