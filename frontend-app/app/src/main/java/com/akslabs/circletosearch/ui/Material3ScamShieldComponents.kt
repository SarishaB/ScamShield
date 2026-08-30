package com.akslabs.circletosearch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Material 3 building blocks for the ScamShield home/access surface.
 * The actual accessibility service state should be supplied by the caller.
 */
@Composable
fun ScamShieldHomeAccessCard(
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "ScamShield",
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("ScamShield", style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (isActive) "Floating access is active" else "Floating access is ready",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isActive) "SCAMSHIELD ACTIVE · REMOVE" else "ADD SCAMSHIELD TO YOUR SCREEN")
            }
        }
    }
}

/**
 * Reusable top content with proper system-bar spacing.
 */
@Composable
fun ScamShieldTopContent(
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.statusBarsPadding()) {
        content()
    }
}
