package com.example.myhealthmateaapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.R

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LoginBody() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBody() {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Background Image
            Image(
                    painter = painterResource(R.drawable.background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
            )

            Column(
                    modifier =
                            Modifier.align(Alignment.Center)
                                    .background(
                                            Color.White.copy(alpha = 0.9f),
                                            RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp)
                                    .fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Login", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(20.dp))

                // Email
                OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("Enter your email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        visualTransformation =
                                if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                        painter =
                                                painterResource(
                                                        if (passwordVisible)
                                                                R.drawable
                                                                        .baseline_visibility_off_24
                                                        else R.drawable.baseline_visibility_24
                                                ),
                                        contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Remember me + Forgot password
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })

                    Text("Remember me", fontSize = 13.sp)

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                            text = "Forgot Password?",
                            fontSize = 13.sp,
                            color = Color(0xFF1E88E5),
                            modifier =
                                    Modifier.clickable {
                                        Intent(context, ForgetPasswordActivity::class.java)
                                    }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔐 LOGIN BUTTON → HOME
                Button(
                        onClick = {
                            context.startActivity(Intent(context, HomeActivity()::class.java))
                            (context as ComponentActivity).finish()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                ) { Text(text = "Login", color = Color.White, fontSize = 16.sp) }

                Spacer(modifier = Modifier.height(12.dp))

                // Sign Up
                Text(
                        text = "Don't have an account? Sign Up",
                        color = Color(0xFF1E88E5),
                        fontSize = 14.sp,
                        modifier =
                                Modifier.clickable {
                                    context.startActivity(
                                            Intent(context, SignUpActivity::class.java)
                                    )
                                }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginBody()
}
