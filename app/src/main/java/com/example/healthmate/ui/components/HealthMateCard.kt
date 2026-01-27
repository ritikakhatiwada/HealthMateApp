package com.example.healthmate.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard HealthMate Card
 * - White Background (Surface)
 * - Rounded Corners (16dp)
 * - Soft Elevation (2-4dp)
 * - Clean spacing
 */
@Composable
fun HealthMateCard(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        padding: Dp = 16.dp,
        elevation: Dp = 2.dp,
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
                onClick = onClick,
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) { Column(modifier = Modifier.padding(padding)) { content() } }
    } else {
        Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) { Column(modifier = Modifier.padding(padding)) { content() } }
    }
}
