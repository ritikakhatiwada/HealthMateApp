package com.example.healthmate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define Color Schemes based on Color.kt
private val DarkColorScheme =
        darkColorScheme(
                primary = DarkPrimary,
                onPrimary = DarkOnPrimary,
                secondary = DarkSecondary,
                onSecondary = DarkOnSecondary,
                background = DarkBackground,
                onBackground = DarkOnBackground,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                error = Error
        )

private val LightColorScheme =
        lightColorScheme(
                primary = LightPrimary,
                onPrimary = LightOnPrimary,
                secondary = LightSecondary,
                onSecondary = LightOnSecondary,
                background = LightBackground,
                onBackground = LightOnBackground,
                surface = LightSurface,
                onSurface = LightOnSurface,
                error = Error
        )

@Composable
fun HealthMateTheme(
        // We remove the default isSystemInDarkTheme() because we want to control it via DataStore
        // But for preview/default we can keep it if needed.
        // However, the standard implementation usually hoists this state.
        darkTheme: Boolean = isSystemInDarkTheme(),
        // Dynamic color is OFF by default to enforce our custom branding
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
