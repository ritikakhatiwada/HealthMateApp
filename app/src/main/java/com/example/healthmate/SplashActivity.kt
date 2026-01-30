package com.example.healthmate

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.healthmate.auth.FirebaseAuthHelper
import com.example.healthmate.ui.theme.HealthMateTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

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

    LaunchedEffect(Unit) {
        // Minimum splash display time for branding
        val minDisplayTime = 1500L
        val startTime = System.currentTimeMillis()

        val destination: Intent = if (FirebaseAuthHelper.isLoggedIn()) {
            // Fetch role with timeout to prevent hanging on slow network
            val role = withTimeoutOrNull(5000L) {
                FirebaseAuthHelper.getUserRole()
            } ?: "USER"

            when (role.uppercase()) {
                "ADMIN" -> Intent(context, AdminDashBoardActivity::class.java)
                else -> Intent(context, UserDashBoardActivity::class.java)
            }
        } else {
            Intent(context, WelcomeActivity::class.java)
        }

        // Ensure minimum display time has elapsed
        val elapsedTime = System.currentTimeMillis() - startTime
        if (elapsedTime < minDisplayTime) {
            delay(minDisplayTime - elapsedTime)
        }

        destination.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(destination)

        // Use appropriate transition method based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            activity.overridePendingTransition(0, 0)
        }

        activity.finish()
    }

    // Splash screen - fills height, no top/bottom white bars, no stretching
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash6),
            contentDescription = "Splash Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillHeight // Fills screen height, maintains aspect ratio, centers horizontally
        )
    }
}
