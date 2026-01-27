package com.example.healthmate.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMateTopBar(
        title: String,
        actions: @Composable RowScope.() -> Unit = {},
        scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val context = LocalContext.current
    val themeManager = ThemeManager(context)
    val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
            title = { Text(text = title) },
            scrollBehavior = scrollBehavior,
            colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.primary
                    ),
            actions = {
                // Theme Switcher Logic
                IconButton(onClick = { scope.launch { themeManager.toggleTheme() } }) {
                    Icon(
                            imageVector =
                                    if (isDarkMode) Icons.Default.LightMode
                                    else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary
                    )
                }
                // User provided actions
                actions()
            }
    )
}
