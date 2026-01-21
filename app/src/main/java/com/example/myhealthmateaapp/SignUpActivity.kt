package com.example.myhealthmateaapp

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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

class SignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignupBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupBody() {

    val context = LocalContext.current
    var dob by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Image(
                painter = painterResource(R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "Sign Up",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("First name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Last name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    placeholder = { Text("Date of Birth (MM/DD/YYYY)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password") },
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible)
                                        R.drawable.baseline_visibility_off_24
                                    else
                                        R.drawable.baseline_visibility_24
                                ),
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_LONG).show()
                        } else {
                            // TODO: Logic to save user to database goes here

                            // Navigate back to Login after successful signup
                            Toast.makeText(context, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                            (context as? ComponentActivity)?.finish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34C759)
                    )
                ) {
                    Text("Sign Up", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Already have an account? Login",
                    color = Color(0xFF1E88E5),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    SignupBody()
}