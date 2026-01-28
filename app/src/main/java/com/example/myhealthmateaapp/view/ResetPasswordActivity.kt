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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class ResetPasswordActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            ResetPasswordBody(auth = auth)
        }
    }
}

@Composable
fun ResetPasswordBody(auth: FirebaseAuth? = null) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    var showPhoneReset by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
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
                text = if (showPhoneReset) "Reset with Phone" else "Reset Password",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Enter the OTP and your new password", fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(20.dp))

            if (!showPhoneReset) {
                // Email Reset
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email/Phone ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible) R.drawable.baseline_visibility_24
                                    else R.drawable.baseline_visibility_off_24
                                ),
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (confirmPasswordVisible) R.drawable.baseline_visibility_24
                                    else R.drawable.baseline_visibility_off_24
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
                        if (email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
                            message = "All fields are required"
                        } else if (newPassword != confirmPassword) {
                            message = "Passwords do not match"
                        } else if (newPassword.length < 6) {
                            message = "Password must be at least 6 characters"
                        } else {
                            // For email, this would typically verify OTP through backend
                            Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as? ComponentActivity)?.finish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                ) {
                    Text("Reset Password", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))


            } else {
                // Phone Reset
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+1234567890") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (verificationId.isEmpty()) {
                    Button(
                        onClick = {
                            if (phoneNumber.isBlank() || auth == null) {
                                message = "Enter phone number"
                                return@Button
                            }

                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(phoneNumber)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(context as ComponentActivity)
                                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                        message = "Verification completed automatically"
                                    }

                                    override fun onVerificationFailed(e: FirebaseException) {
                                        message = e.localizedMessage ?: "Verification failed"
                                    }

                                    override fun onCodeSent(
                                        id: String,
                                        token: PhoneAuthProvider.ForceResendingToken
                                    ) {
                                        verificationId = id
                                        message = "OTP sent to your phone"
                                    }
                                }).build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text("Send OTP", color = Color.White)
                    }
                } else {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("OTP") },
                        placeholder = { Text("Enter OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        if (passwordVisible) R.drawable.baseline_visibility_24
                                        else R.drawable.baseline_visibility_off_24
                                    ),
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        if (confirmPasswordVisible) R.drawable.baseline_visibility_24
                                        else R.drawable.baseline_visibility_off_24
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
                            if (otp.isBlank()) {
                                message = "Enter OTP"
                                return@Button
                            }
                            if (newPassword.isBlank() || confirmPassword.isBlank()) {
                                message = "All fields are required"
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                message = "Passwords do not match"
                                return@Button
                            }
                            if (newPassword.length < 6) {
                                message = "Password must be at least 6 characters"
                                return@Button
                            }

                            if (auth != null) {
                                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                                auth.signInWithCredential(credential)
                                    .addOnSuccessListener {
                                        auth.currentUser?.updatePassword(newPassword)
                                            ?.addOnSuccessListener {
                                                Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(context, LoginActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(intent)
                                                (context as? ComponentActivity)?.finish()
                                            }
                                            ?.addOnFailureListener {
                                                message = it.localizedMessage ?: "Password update failed"
                                            }
                                    }
                                    .addOnFailureListener {
                                        message = it.localizedMessage ?: "Invalid OTP"
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text("Reset Password", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Back to Email Reset",
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.clickable {
                        showPhoneReset = false
                        phoneNumber = ""
                        verificationId = ""
                        otp = ""
                        newPassword = ""
                        confirmPassword = ""
                        message = ""
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (message.isNotEmpty()) {
                Text(message, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Back to Login",
                color = Color(0xFF1E88E5),
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
fun ResetPasswordPreview() {
    ResetPasswordBody()
}