package com.example.healthmate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.ui.components.HealthMatePasswordField
import com.example.healthmate.ui.components.HealthMateTextField
import com.example.healthmate.ui.components.PrimaryButton
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

/**
 * SignUp Activity
 *
 * Hospital-grade registration screen with medical green branding.
 */
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) {
                SignUpScreen()
            }
        }
    }
}

/**
 * SignUp Screen - Hospital-Grade Design
 *
 * Features:
 * - Medical green gradient background
 * - Professional form layout with 4 fields
 * - Field-level validation
 * - Password confirmation
 * - Loading states
 *
 * Business logic preserved:
 * - FirebaseAuthHelper.signUp()
 * - Navigation to UserDashboard
 */
@Composable
fun SignUpScreen() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()

    // Medical green gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Subtle Medical Design: Soft teal gradients in corners
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tealColor = Color(0xFF2EC4B6).copy(alpha = 0.05f)
            drawCircle(
                color = tealColor,
                radius = size.width * 0.4f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f)
            )
            drawCircle(
                color = tealColor,
                radius = size.width * 0.6f,
                center = androidx.compose.ui.geometry.Offset(-size.width * 0.1f, size.height * 0.9f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xxl)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium Logo
            Image(
                painter = painterResource(id = R.drawable.logo_final),
                contentDescription = "HealthMate Logo",
                modifier = Modifier
                    .size(70.dp)
                    .padding(bottom = Spacing.sm)
            )

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Join our secure medical network",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(Spacing.xxxl))

            // Full Name Field
            HealthMateTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = "Full Name",
                placeholder = "John Doe",
                isError = nameError != null,
                errorMessage = nameError,
                enabled = !isLoading,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Email Field
            HealthMateTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = "Email Address",
                placeholder = "name@example.com",
                keyboardType = KeyboardType.Email,
                isError = emailError != null,
                errorMessage = emailError,
                enabled = !isLoading,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Password Field
            HealthMatePasswordField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "Password",
                isError = passwordError != null,
                errorMessage = passwordError,
                enabled = !isLoading,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Confirm Password Field
            HealthMatePasswordField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = "Confirm Password",
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                enabled = !isLoading,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(Spacing.xxxl))

            // Sign Up Button
            PrimaryButton(
                text = "Create Account",
                onClick = {
                    // Validation
                    var hasError = false

                    if (name.isBlank()) {
                        nameError = "Name is required"
                        hasError = true
                    }

                    if (email.isBlank()) {
                        emailError = "Email is required"
                        hasError = true
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Invalid email format"
                        hasError = true
                    }

                    if (password.isBlank()) {
                        passwordError = "Password is required"
                        hasError = true
                    } else if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters"
                        hasError = true
                    }

                    if (confirmPassword.isBlank()) {
                        confirmPasswordError = "Please confirm your password"
                        hasError = true
                    } else if (password != confirmPassword) {
                        confirmPasswordError = "Passwords do not match"
                        hasError = true
                    }

                    if (hasError) return@PrimaryButton

                    // Proceed with sign up
                    isLoading = true

                    coroutineScope.launch {
                        val result = FirebaseAuthHelper.signUp(
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
                                ).show()

                                // Navigate to User Dashboard
                                val intent = Intent(context, UserDashBoardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                activity.finish()
                            },
                            onFailure = { error ->
                                emailError = error.message ?: "Sign up failed. Please try again."
                            }
                        )
                    }
                },
                isLoading = isLoading,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Login Link
            Row(
                modifier = Modifier.padding(bottom = Spacing.xxl)
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !isLoading) {
                        activity.finish()
                    }
                )
            }
        }
    }
}
