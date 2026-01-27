package com.example.myhealthmateaapp

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class SignUpActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
        firestore = FirebaseFirestore.getInstance()

        setContent {
            SignupScreen(auth = auth, prefs = prefs, firestore = firestore)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    auth: FirebaseAuth? = null,
    prefs: SharedPreferences? = null,
    firestore: FirebaseFirestore? = null
) {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isOtpMode by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Helper function to save user profile to Firestore
    fun saveUserProfile(userId: String, email: String, phone: String) {
        val userProfile = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phoneNumber" to phone,
            "createdAt" to System.currentTimeMillis()
        )

        firestore?.collection("users")?.document(userId)?.set(userProfile)
            ?.addOnSuccessListener {
                // Profile saved successfully
            }
            ?.addOnFailureListener {
                // Handle error silently or log
            }
    }

    // Helper function to detect if input is phone number
    fun isPhoneNumber(input: String): Boolean {
        return input.startsWith("+") || (input.all { it.isDigit() } && input.length >= 10)
    }

    // Helper function to format phone number
    fun formatPhoneNumber(input: String): String {
        return if (!input.startsWith("+")) {
            "+977$input" // Default country code
        } else {
            input
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background Image
            Image(
                painter = painterResource(R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                    .padding(24.dp)
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "← Back",
                        color = Color(0xFF1E88E5),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                            (context as ComponentActivity).finish()
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Title
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    placeholder = { Text("Enter first name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF34C759),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    enabled = !isOtpMode
                )

                Spacer(Modifier.height(12.dp))

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    placeholder = { Text("Enter last name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF34C759),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    enabled = !isOtpMode
                )

                Spacer(Modifier.height(12.dp))

                // Email/Phone Input Field
                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("Email/Phone") },
                    placeholder = { Text("Enter email or phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF34C759),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    enabled = !isOtpMode
                )

                Spacer(Modifier.height(12.dp))

                // Password or OTP Field based on mode
                if (!isOtpMode) {
                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter password (min 6 characters)") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        if (passwordVisible) R.drawable.baseline_visibility_off_24
                                        else R.drawable.baseline_visibility_24
                                    ),
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF34C759),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Remember Me Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF34C759))
                        )
                        Text("Remember me", fontSize = 14.sp)
                    }

                } else {
                    // OTP Field
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it },
                        label = { Text("Enter OTP") },
                        placeholder = { Text("6-digit code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF34C759),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Back to password signup
                    Text(
                        "Use Password Instead",
                        color = Color(0xFF1E88E5),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                isOtpMode = false
                                verificationId = ""
                                otp = ""
                                message = ""
                            }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Sign Up Button
                Button(
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank() || emailOrPhone.isBlank()) {
                            message = "Please fill all fields"
                            return@Button
                        }

                        isProcessing = true
                        val isPhone = isPhoneNumber(emailOrPhone)

                        if (isPhone) {
                            // Phone Authentication Flow
                            val phoneNum = formatPhoneNumber(emailOrPhone)

                            if (!isOtpMode) {
                                // Send OTP
                                if (auth != null) {
                                    val options = PhoneAuthOptions.newBuilder(auth)
                                        .setPhoneNumber(phoneNum)
                                        .setTimeout(60L, TimeUnit.SECONDS)
                                        .setActivity(context as ComponentActivity)
                                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                isProcessing = false
                                                auth.signInWithCredential(credential)
                                                    .addOnSuccessListener { authResult ->
                                                        // Save user profile
                                                        authResult.user?.let { user ->
                                                            saveUserProfile(user.uid, "", phoneNum)
                                                        }

                                                        prefs?.edit()?.putBoolean("remember", rememberMe)?.apply()
                                                        Toast.makeText(context, "Account created successfully!", Toast.LENGTH_LONG).show()

                                                        // Navigate to HomeActivity
                                                        val intent = Intent(context, HomeActivity::class.java)
                                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        context.startActivity(intent)
                                                        (context as ComponentActivity).finish()
                                                    }
                                                    .addOnFailureListener {
                                                        isProcessing = false
                                                        message = it.localizedMessage ?: "Auto-verification failed"
                                                    }
                                            }

                                            override fun onVerificationFailed(e: FirebaseException) {
                                                isProcessing = false
                                                message = e.localizedMessage ?: "Verification failed"
                                            }

                                            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                                isProcessing = false
                                                verificationId = id
                                                isOtpMode = true
                                                message = "OTP sent to your phone"
                                            }
                                        }).build()
                                    PhoneAuthProvider.verifyPhoneNumber(options)
                                }
                            } else {
                                // Verify OTP
                                if (otp.isBlank()) {
                                    message = "Please enter OTP"
                                    isProcessing = false
                                    return@Button
                                }

                                if (verificationId.isNotEmpty() && auth != null) {
                                    val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                                    auth.signInWithCredential(credential)
                                        .addOnSuccessListener { authResult ->
                                            // Save user profile
                                            authResult.user?.let { user ->
                                                saveUserProfile(user.uid, "", phoneNum)
                                            }

                                            isProcessing = false
                                            prefs?.edit()?.putBoolean("remember", rememberMe)?.apply()
                                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_LONG).show()

                                            // Navigate to HomeActivity
                                            val intent = Intent(context, HomeActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(intent)
                                            (context as ComponentActivity).finish()
                                        }
                                        .addOnFailureListener {
                                            isProcessing = false
                                            message = "Invalid OTP. Please try again"
                                        }
                                } else {
                                    isProcessing = false
                                    message = "Please request OTP first"
                                }
                            }

                        } else {
                            // Email Authentication Flow
                            if (password.isBlank()) {
                                message = "Please enter password"
                                isProcessing = false
                                return@Button
                            }

                            if (password.length < 6) {
                                message = "Password must be at least 6 characters"
                                isProcessing = false
                                return@Button
                            }

                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
                                message = "Please enter a valid email"
                                isProcessing = false
                                return@Button
                            }

                            if (auth != null && prefs != null) {
                                auth.createUserWithEmailAndPassword(emailOrPhone, password)
                                    .addOnSuccessListener { authResult ->
                                        // Save user profile
                                        authResult.user?.let { user ->
                                            saveUserProfile(user.uid, emailOrPhone, "")
                                        }

                                        // Send email verification
                                        auth.currentUser?.sendEmailVerification()
                                            ?.addOnSuccessListener {
                                                isProcessing = false
                                                prefs.edit().putBoolean("remember", rememberMe).apply()
                                                Toast.makeText(
                                                    context,
                                                    "Account created! Please verify your email.",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                // Navigate to LoginActivity
                                                val intent = Intent(context, LoginActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(intent)
                                                (context as ComponentActivity).finish()
                                            }
                                            ?.addOnFailureListener {
                                                isProcessing = false
                                                message = "Account created but failed to send verification email"
                                            }
                                    }
                                    .addOnFailureListener { exception ->
                                        isProcessing = false
                                        message = when {
                                            exception.message?.contains("email address is already in use") == true ->
                                                "Email already registered"
                                            exception.message?.contains("password") == true ->
                                                "Password should be at least 6 characters"
                                            exception.message?.contains("badly formatted") == true ->
                                                "Invalid email format"
                                            else -> exception.localizedMessage ?: "Sign up failed"
                                        }
                                    }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34C759),
                        disabledContainerColor = Color(0xFF34C759).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (isOtpMode && verificationId.isNotEmpty()) "Verify OTP"
                            else if (isOtpMode) "Send OTP"
                            else "Sign Up",
                            color = Color.White,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Error/Success Message
                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = if (message.contains("sent") || message.contains("success"))
                            Color(0xFF34C759) else Color.Red,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Login Link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Already have an account? ",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Login",
                        fontSize = 14.sp,
                        color = Color(0xFF1E88E5),
                        modifier = Modifier.clickable {
                            // Navigate to LoginActivity
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as ComponentActivity).finish()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpPreview() {
    SignupScreen()
}