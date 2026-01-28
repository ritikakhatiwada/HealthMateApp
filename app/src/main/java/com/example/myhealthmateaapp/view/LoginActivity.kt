package com.example.myhealthmateaapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import android.widget.Toast
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

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)

        setContent {
            LoginScreen(auth = auth, prefs = prefs)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(auth: FirebaseAuth? = null, prefs: SharedPreferences? = null) {

    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isOtpMode by remember { mutableStateOf(false) } // Switch between password and OTP
    var isProcessing by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Helper function to detect if input is phone number
    fun isPhoneNumber(input: String): Boolean {
        // Check if it starts with + and contains only digits after that
        // or if it's all digits with length > 9
        return input.startsWith("+") || (input.all { it.isDigit() } && input.length >= 10)
    }

    // Helper function to format phone number
    fun formatPhoneNumber(input: String): String {
        return if (!input.startsWith("+")) {
            "+977$input" // Default country code, change as needed
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

                // Logo or Title (Optional)
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

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
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Password or OTP Field based on mode
                if (!isOtpMode) {
                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
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

                    // Remember Me and Forgot Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF34C759))
                            )
                            Text("Remember me", fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            "Forgot Password?",
                            color = Color(0xFF1E88E5),
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                // Navigate to ForgotPasswordActivity
                                val intent = Intent(context, ForgetPasswordActivity::class.java)
                                // Optionally pass the email/phone if already entered
                                if (emailOrPhone.isNotEmpty()) {
                                    intent.putExtra("email_or_phone", emailOrPhone)
                                }
                                context.startActivity(intent)
                            }
                        )
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

                    // Back to password login
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

                // Login Button
                Button(
                    onClick = {
                        if (emailOrPhone.isBlank()) {
                            message = "Please enter email or phone"
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
                                                    .addOnSuccessListener {
                                                        prefs?.edit()?.putBoolean("remember", rememberMe)?.apply()
                                                        // Navigate to HomeActivity
                                                        val intent = Intent(context, HomeActivity::class.java)
                                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        context.startActivity(intent)
                                                        (context as ComponentActivity).finish()
                                                    }
                                                    .addOnFailureListener {
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
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            prefs?.edit()?.putBoolean("remember", rememberMe)?.apply()
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

                            if (auth != null && prefs != null) {
                                auth.signInWithEmailAndPassword(emailOrPhone, password)
                                    .addOnSuccessListener {
                                        if (!auth.currentUser!!.isEmailVerified) {
                                            message = "Please verify your email first"
                                            auth.signOut()
                                            isProcessing = false
                                        } else {
                                            prefs.edit().putBoolean("remember", rememberMe).apply()
                                            // Navigate to HomeActivity
                                            val intent = Intent(context, HomeActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(intent)
                                            (context as ComponentActivity).finish()
                                        }
                                    }
                                    .addOnFailureListener {
                                        isProcessing = false
                                        message = "Invalid email or password"
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
                            else "Login",
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

                // Sign Up Link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Don't have an account? ",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Sign Up",
                        fontSize = 14.sp,
                        color = Color(0xFF1E88E5),
                        modifier = Modifier.clickable {
                            // Navigate to SignUpActivity
                            val intent = Intent(context, SignUpActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}