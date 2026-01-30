package com.example.healthmate
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.ui.theme.HealthMateTheme
import com.example.healthmate.ui.theme.HealthMateShapes
import com.example.healthmate.ui.theme.Spacing
import com.example.healthmate.util.ThemeManager
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.MaterialTheme
import android.app.Activity

class ResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager(this)
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
            HealthMateTheme(darkTheme = isDarkMode) {
                ResetPasswordBody(isDarkMode)
            }
        }
    }
}

@Composable
fun ResetPasswordBody(isDarkMode: Boolean = false) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = if (isDarkMode) 0.3f else 1.0f
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    if (isDarkMode) MaterialTheme.colorScheme.surface 
                    else Color.White.copy(alpha = 0.95f), 
                    HealthMateShapes.CardLarge
                )
                .padding(Spacing.xl)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Reset Password", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Enter the OTP and your new password", fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(20.dp))

            // OTP Input
            OutlinedTextField(
                value = otp, onValueChange = { otp = it },
                label = { Text("OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = HealthMateShapes.InputField
            )

            Spacer(modifier = Modifier.height(10.dp))

            // New Password Input
            OutlinedTextField(
                value = newPassword, onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = painterResource(if (passwordVisible) R.drawable.baseline_visibility_24 else R.drawable.outline_visibility_off_24), contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = HealthMateShapes.InputField
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(painter = painterResource(if (confirmPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.outline_visibility_off_24), contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = HealthMateShapes.InputField
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Reset Button
            Button(
                onClick = {
                    if (email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    } else if (newPassword != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_SHORT).show()

                        // Navigate to Login and clear history so user can't go back to Reset screen
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = HealthMateShapes.ButtonLarge,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Reset Password", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login Link
            Text(
                text = "Back to Login",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    (context as? Activity)?.finish()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordPreview() {
    ResetPasswordBody()
}