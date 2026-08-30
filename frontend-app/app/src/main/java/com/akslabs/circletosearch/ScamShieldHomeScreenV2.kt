package com.akslabs.circletosearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScamShieldHomeScreenV2(
    onScan: () -> Unit,
    onCommunity: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val accessibilityEnabled = remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScamShield", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 12.dp).size(40.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(9.dp))
                    }
                },
                actions = { IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProtectionCardV2(
                enabled = accessibilityEnabled.value,
                onEnable = { openAccessibilitySettings(context); accessibilityEnabled.value = true }
            )

            ElevatedCard(onClick = onScan, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PASTE ANY SUSPICIOUS URLS, FILES, MESSAGES ETC", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Submit copied text, screenshots, URLs or files for analysis.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScanTypeLabel("TEXT")
                        ScanTypeLabel("SCREENSHOT")
                        ScanTypeLabel("FILE")
                    }
                    Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) { Text("OPEN ANALYZER") }
                }
            }

            ElevatedCard(onClick = onCommunity, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                        Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(11.dp).size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("COMMUNITY REPORTS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("View recently reported scams", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProtectionCardV2(enabled: Boolean, onEnable: () -> Unit) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(14.dp).size(34.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (enabled) "Protection active" else "Protection needs setup", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (enabled) "ScamShield is ready to inspect content from other apps." else "Enable Accessibility access to use the in-context scanner.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!enabled) Button(onClick = onEnable, modifier = Modifier.fillMaxWidth()) { Text("Enable ScamShield") }
            else Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                Text("Protected", modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun RowScope.ScanTypeLabel(label: String) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.weight(1f)) {
        Text(label, modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}
