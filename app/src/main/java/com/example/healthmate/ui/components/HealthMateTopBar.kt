package com.example.healthmate.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.healthmate.util.ThemeManager
import kotlinx.coroutines.launch

/**
 * HealthMate Top App Bar Components
 *
 * Enhanced top app bar with subtitle support, custom navigation,
 * and theme toggle functionality.
 */

/**
 * HealthMate Top Bar - Enhanced
 *
 * Professional top app bar for medical app with:
 * - Optional subtitle
 * - Custom navigation icon
 * - Theme toggle (light/dark mode)
 * - Scroll behavior support
 *
 * @param title Top bar title
 * @param modifier Modifier for customization
 * @param subtitle Optional subtitle text
 * @param navigationIcon Optional navigation icon (e.g., back arrow)
 * @param onNavigationClick Navigation icon click handler
 * @param showThemeToggle Show theme toggle button (default true)
 * @param actions Additional action buttons
 * @param scrollBehavior Scroll behavior for collapsing top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMateTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    showThemeToggle: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val context = LocalContext.current
    val themeManager = ThemeManager(context)
    val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            if (subtitle != null) {
                // Title with subtitle
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Title only
                Text(text = title)
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            // Theme toggle (if enabled)
            if (showThemeToggle) {
                IconButton(onClick = { scope.launch { themeManager.toggleTheme() } }) {
                    Icon(
                        imageVector = if (isDarkMode) {
                            Icons.Default.LightMode
                        } else {
                            Icons.Default.DarkMode
                        },
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Custom actions
            actions()
        }
    )
}

/**
 * Simple Top Bar
 *
 * Simplified top bar without theme toggle for specific screens.
 *
 * @param title Top bar title
 * @param onBackClick Back button click handler
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HealthMateTopBar(
        title = title,
        modifier = modifier,
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = onBackClick,
        showThemeToggle = false
    )
}
