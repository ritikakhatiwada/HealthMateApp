package com.example.healthmate

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.ui.theme.HealthMateTheme
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HealthMateTheme { SplashBody() } }
    }
}

@Composable
fun SplashBody() {
    val context = LocalContext.current
    val activity = context as Activity

    // Navigation logic with delay and auth check
    LaunchedEffect(Unit) {
        delay(1000) // 1 second delay for splash

        if (FirebaseAuthHelper.isLoggedIn()) {
            // User is logged in, check role and redirect
            val role = FirebaseAuthHelper.getUserRole()
            val intent =
                    when (role.uppercase()) {
                        "ADMIN" -> Intent(context, AdminDashBoardActivity::class.java)
                        else -> Intent(context, UserDashBoardActivity::class.java)
                    }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        } else {
            // Not logged in, go to welcome screen
            val intent = Intent(context, WelcomeActivity::class.java)
            context.startActivity(intent)
        }
        activity.finish()
    }

    Scaffold { padding ->
        Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(70.dp))

            // Loading indicator
            CircularProgressIndicator(color = Color(0xFF2196F3))
        }
    }
}
