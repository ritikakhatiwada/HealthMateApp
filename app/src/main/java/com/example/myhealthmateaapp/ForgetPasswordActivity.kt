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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class ForgetPasswordActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            ForgotPasswordBody(auth = auth)
        }
    }
}

@Composable
fun ForgotPasswordBody(auth: FirebaseAuth? = null) {
    val context = LocalContext.current

    var emailOrPhone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showPhoneOption by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background Image
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Card container
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                .padding(24.dp)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Forgot Password?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your email/phone to receive a reset code",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = { Text("Email/Phone") },
                placeholder = { Text("abc@gmail.com or +1234567890") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (emailOrPhone.startsWith("+")) KeyboardType.Phone else KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (emailOrPhone.isBlank()) {
                        message = "Please enter email or phone"
                        return@Button
                    }

                    if (auth != null) {
                        // Check if it's a phone number
                        if (emailOrPhone.startsWith("+")) {
                            // Send OTP via Phone
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(emailOrPhone)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(context as ComponentActivity)
                                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                        Toast.makeText(context, "OTP sent!", Toast.LENGTH_SHORT).show()
                                        // Navigate to Reset Password screen
                                        val intent = Intent(context, ResetPasswordActivity::class.java)
                                        intent.putExtra("phoneNumber", emailOrPhone)
                                        intent.putExtra("isPhoneReset", true)
                                        context.startActivity(intent)
                                    }

                                    override fun onVerificationFailed(e: FirebaseException) {
                                        message = e.localizedMessage ?: "Verification failed"
                                    }

                                    override fun onCodeSent(
                                        id: String,
                                        token: PhoneAuthProvider.ForceResendingToken
                                    ) {
                                        verificationId = id
                                        Toast.makeText(context, "OTP sent to your phone!", Toast.LENGTH_SHORT).show()

                                        // Navigate to Reset Password screen with phone data
                                        val intent = Intent(context, ResetPasswordActivity::class.java)
                                        intent.putExtra("phoneNumber", emailOrPhone)
                                        intent.putExtra("verificationId", id)
                                        intent.putExtra("isPhoneReset", true)
                                        context.startActivity(intent)
                                        (context as ComponentActivity).finish()
                                    }
                                }).build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        } else {
                            // Send reset email
                            auth.sendPasswordResetEmail(emailOrPhone)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Reset code sent to email!", Toast.LENGTH_SHORT).show()

                                    // Navigate to Reset Password screen with email data
                                    val intent = Intent(context, ResetPasswordActivity::class.java)
                                    intent.putExtra("email", emailOrPhone)
                                    intent.putExtra("isPhoneReset", false)
                                    context.startActivity(intent)
                                    (context as ComponentActivity).finish()
                                }
                                .addOnFailureListener { e ->
                                    message = e.localizedMessage ?: "Error sending reset code"
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
            ) {
                Text("Send Reset Code", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (message.isNotEmpty()) {
                Text(message, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Remembered your password? Login",
                color = Color(0xFF1E88E5),
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    ForgotPasswordBody()
}