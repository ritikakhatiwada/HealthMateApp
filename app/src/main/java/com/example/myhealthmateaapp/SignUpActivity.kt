package com.example.myhealthmateaapp

import android.app.DatePickerDialog
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhealthmateaapp.ui.theme.MyHealthMateaAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/* ============================
   ACTIVITY (Firebase logic)
   ============================ */

class SignUpActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        setContent {
            MyHealthMateaAppTheme {
                SignupScreen(
                    onSignupClick = { firstName, lastName, dob, email, password ->

                        // ---------------- Validation ----------------
                        if (firstName.isBlank() || lastName.isBlank() || dob.isBlank()
                            || email.isBlank() || password.isBlank()
                        ) {
                            Toast.makeText(this, "All fields are required", Toast.LENGTH_LONG).show()
                            return@SignupScreen
                        }

                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(this, "Invalid email address", Toast.LENGTH_LONG).show()
                            return@SignupScreen
                        }

                        if (password.length < 6) {
                            Toast.makeText(
                                this,
                                "Password must be at least 6 characters",
                                Toast.LENGTH_LONG
                            ).show()
                            return@SignupScreen
                        }

                        // ---------------- Firebase Signup ----------------
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                val userId = auth.currentUser!!.uid

                                val userData = mapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "dob" to dob,
                                    "email" to email
                                )

                                db.collection("users")
                                    .document(userId)
                                    .set(userData)

                                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                    },
                    onLoginClick = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

/* ============================
   UI (Compose)
   ============================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupClick: (
        firstName: String,
        lastName: String,
        dob: String,
        email: String,
        password: String
    ) -> Unit,
    onLoginClick: () -> Unit
) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ---------------- Background Image ----------------
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // ---------------- Signup Card ----------------
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Sign Up",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ---------------- Date of Birth ----------------
                OutlinedTextField(
                    value = dob,
                    onValueChange = {},
                    readOnly = true, // ✅ IMPORTANT
                    placeholder = { Text("Date of Birth") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    calendar.set(year, month, day)
                                    dob = dateFormat.format(calendar.time)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_calendar_today_24),
                            contentDescription = "Select Date"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sign Up Button
                Button(
                    onClick = {
                        onSignupClick(firstName, lastName, dob, email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                ) {
                    Text("Sign Up", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Login Text
                Text(
                    text = "Already have an account? Login",
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}

/* ============================
   PREVIEW
   ============================ */

@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    MyHealthMateaAppTheme {
        SignupScreen(
            onSignupClick = { _, _, _, _, _ -> },
            onLoginClick = {}
        )
    }
}
