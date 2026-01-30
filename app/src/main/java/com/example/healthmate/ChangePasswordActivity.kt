package com.example.healthmate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.components.HealthMateTopBar
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.ThemeManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChangePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) { ChangePasswordScreen(onBack = { finish() }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                HealthMateTopBar(
                        title = "Change Password",
                        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        onNavigationClick = onBack
                )
            }
    ) { padding ->
        Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                    onClick = {
                        if (currentPassword.isBlank() ||
                                        newPassword.isBlank() ||
                                        confirmPassword.isBlank()
                        ) {
                            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT)
                                    .show()
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            Toast.makeText(
                                            context,
                                            "New passwords do not match",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                            return@Button
                        }
                        if (newPassword.length < 6) {
                            Toast.makeText(
                                            context,
                                            "Password must be at least 6 characters",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                            return@Button
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user != null && user.email != null) {
                                    // Re-authenticate
                                    val credential =
                                            EmailAuthProvider.getCredential(
                                                    user.email!!,
                                                    currentPassword
                                            )
                                    user.reauthenticate(credential).await()

                                    // Update Password
                                    user.updatePassword(newPassword).await()

                                    Toast.makeText(
                                                    context,
                                                    "Password updated successfully!",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT)
                                            .show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG)
                                        .show()
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Update Password")
                }
            }
        }
    }
}
