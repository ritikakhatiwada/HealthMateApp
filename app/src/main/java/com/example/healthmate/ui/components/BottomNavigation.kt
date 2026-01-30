package com.example.healthmate.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.healthmate.ui.theme.Elevation

/**
 * Bottom Navigation Components for HealthMate
 *
 * Redesigned bottom navigation with better visual hierarchy
 * and badge support for notifications.
 */

/**
 * HealthMate Bottom Navigation
 *
 * Material 3 bottom navigation bar with medical app styling.
 *
 * @param items List of navigation items
 * @param selectedIndex Currently selected tab index
 * @param onItemSelected Tab selection callback
 */
@Composable
fun HealthMateBottomNav(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = Elevation.medium
    ) {
        items.forEachIndexed { index, item ->
            HealthMateNavigationBarItem(
                item = item,
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

/**
 * HealthMate Navigation Bar Item
 *
 * Individual navigation item with badge support.
 */
@Composable
private fun RowScope.HealthMateNavigationBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            if (item.badgeCount != null && item.badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge {
                            Text(
                                text = if (item.badgeCount > 9) "9+" else item.badgeCount.toString()
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label
                    )
                }
            } else {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.icon,
                    contentDescription = item.label
                )
            }
        },
        label = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

/**
 * Bottom Navigation Item Data Class
 *
 * @param label Navigation item label
 * @param icon Default icon
 * @param selectedIcon Icon when selected (default same as icon)
 * @param badgeCount Optional badge count for notifications
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val badgeCount: Int? = null
)
