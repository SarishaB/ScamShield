package com.akslabs.circletosearch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FmdGood
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akslabs.circletosearch.ui.theme.ShieldBackground
import com.akslabs.circletosearch.ui.theme.ShieldDim
import com.akslabs.circletosearch.ui.theme.ShieldGreen
import com.akslabs.circletosearch.ui.theme.ShieldMuted
import com.akslabs.circletosearch.ui.theme.ShieldPurple
import com.akslabs.circletosearch.ui.theme.ShieldSurface
import com.akslabs.circletosearch.ui.theme.ShieldSurfaceRaised
import com.akslabs.circletosearch.ui.theme.ShieldText
import com.akslabs.circletosearch.ui.theme.ShieldViolet
import com.akslabs.circletosearch.ui.theme.ShieldVioletBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScamShieldHomeScreenV2(onScan: () -> Unit, onCommunity: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val accessibilityEnabled = remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    val bubbleEnabled = remember { mutableStateOf(prefs.getBoolean("bubble_enabled", false)) }

    Scaffold(containerColor = ShieldBackground, topBar = {
        TopAppBar(
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Text("SCAMSHIELD", fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, fontSize = 18.sp)
                    Text("DIGITAL SECURITY", style = MaterialTheme.typography.labelSmall, color = ShieldDim, letterSpacing = 2.1.sp)
                }
            },
            navigationIcon = {
                Surface(
                    Modifier.padding(start = 12.dp).size(40.dp),
                    RoundedCornerShape(12.dp),
                    ShieldViolet.copy(alpha = 0.10f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShieldViolet.copy(alpha = 0.28f))
                ) { Icon(Icons.Default.Shield, null, tint = ShieldVioletBright, modifier = Modifier.padding(9.dp)) }
            },
            actions = { IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings", tint = ShieldMuted) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldBackground, titleContentColor = ShieldText)
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Spacer(Modifier.height(1.dp))
            ProtectionCardV2(accessibilityEnabled.value) { openAccessibilitySettings(context); accessibilityEnabled.value = true }
            SectionLabel("CHECK SOMETHING SUSPICIOUS")
            ElevatedCard(onClick = onScan, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.elevatedCardColors(containerColor = ShieldSurface), elevation = CardDefaults.elevatedCardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().border(1.dp, ShieldViolet.copy(alpha = 0.20f), RoundedCornerShape(22.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Paste Anything Suspicious", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ShieldText)
                            Text("URLs, messages, screenshots or files.", style = MaterialTheme.typography.bodyMedium, color = ShieldMuted)
                        }
                        Surface(shape = RoundedCornerShape(13.dp), color = ShieldViolet.copy(alpha = 0.11f)) { Icon(Icons.Default.Shield, null, tint = ShieldVioletBright, modifier = Modifier.padding(9.dp).size(21.dp)) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { ScanTypeLabel("TEXT"); ScanTypeLabel("IMAGE"); ScanTypeLabel("FILE") }
                    Button(onClick = onScan, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = ShieldViolet, contentColor = ShieldBackground)) { Text("OPEN ANALYSER", fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp); Spacer(Modifier.width(7.dp)); Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(17.dp)) }
                }
            }
            SectionLabel("COMMUNITY INTELLIGENCE")
            ElevatedCard(onClick = onCommunity, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.elevatedCardColors(containerColor = ShieldSurfaceRaised), elevation = CardDefaults.elevatedCardElevation(0.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = ShieldPurple.copy(alpha = 0.20f), border = androidx.compose.foundation.BorderStroke(1.dp, ShieldViolet.copy(alpha = 0.18f))) { Icon(Icons.Default.Groups, null, tint = ShieldVioletBright, modifier = Modifier.padding(9.dp).size(22.dp)) }
                    Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text("Recent scam reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShieldText); Text("See what the community is reporting", style = MaterialTheme.typography.bodySmall, color = ShieldMuted) }; Icon(Icons.Default.ArrowForward, null, tint = ShieldVioletBright, modifier = Modifier.size(19.dp))
                }
            }
            FloatingAccessCardV2(bubbleEnabled.value, accessibilityEnabled.value) { enabled -> if (!accessibilityEnabled.value) openAccessibilitySettings(context) else { bubbleEnabled.value = enabled; prefs.edit().putBoolean("bubble_enabled", enabled).apply() } }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable private fun SectionLabel(text: String) = Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ShieldDim, letterSpacing = 1.8.sp)

@Composable private fun ProtectionCardV2(enabled: Boolean, onEnable: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), RoundedCornerShape(23.dp), colors = CardDefaults.elevatedCardColors(containerColor = ShieldSurface), elevation = CardDefaults.elevatedCardElevation(0.dp)) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(ShieldPurple.copy(alpha = 0.25f), ShieldSurface, ShieldSurface))).border(1.dp, ShieldViolet.copy(alpha = 0.24f), RoundedCornerShape(23.dp)).padding(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(15.dp), color = ShieldViolet.copy(alpha = 0.13f), border = androidx.compose.foundation.BorderStroke(1.dp, ShieldViolet.copy(alpha = 0.28f))) { Icon(Icons.Default.VerifiedUser, null, tint = ShieldVioletBright, modifier = Modifier.padding(11.dp).size(27.dp)) }
                    Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text("PROTECTION STATUS", style = MaterialTheme.typography.labelMedium, color = ShieldMuted, letterSpacing = 1.25.sp); Text(if (enabled) "Protected" else "Setup required", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ShieldText) }
                    Surface(shape = RoundedCornerShape(50), color = if (enabled) ShieldGreen.copy(alpha = 0.12f) else ShieldViolet.copy(alpha = 0.10f)) { Text(if (enabled) "ACTIVE" else "OFFLINE", Modifier.padding(horizontal = 9.dp, vertical = 6.dp), color = if (enabled) ShieldGreen else ShieldMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp) }
                }
                Text(if (enabled) "Your in-context ScamShield protection is ready." else "Enable ScamShield accessibility access to scan suspicious content from other apps.", style = MaterialTheme.typography.bodyMedium, color = ShieldMuted)
                if (!enabled) Button(onClick = onEnable, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) { Text("ENABLE SCAMSHIELD", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) }
            }
        }
    }
}

@Composable private fun FloatingAccessCardV2(enabled: Boolean, accessibilityEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), RoundedCornerShape(19.dp), colors = CardDefaults.elevatedCardColors(containerColor = ShieldSurfaceRaised), elevation = CardDefaults.elevatedCardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(11.dp), color = ShieldViolet.copy(alpha = 0.10f)) { Icon(Icons.Default.FmdGood, null, tint = ShieldVioletBright, modifier = Modifier.padding(9.dp).size(21.dp)) }
            Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text("FLOATING SCAMSHIELD", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = ShieldText); Text(if (!accessibilityEnabled) "Enable access to use the floating control" else if (enabled) "Available above other apps" else "Add the shield for quick access", style = MaterialTheme.typography.bodySmall, color = ShieldMuted) }; Switch(checked = enabled, onCheckedChange = onToggle, enabled = accessibilityEnabled, colors = SwitchDefaults.colors(checkedThumbColor = ShieldBackground, checkedTrackColor = ShieldViolet, uncheckedThumbColor = ShieldMuted, uncheckedTrackColor = ShieldBackground, uncheckedBorderColor = ShieldDim))
        }
    }
}

@Composable private fun RowScope.ScanTypeLabel(label: String) { Surface(shape = RoundedCornerShape(10.dp), color = Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.dp, ShieldDim.copy(alpha = 0.34f)), modifier = Modifier.weight(1f)) { Text(label, Modifier.padding(vertical = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ShieldMuted, letterSpacing = 0.8.sp) } }
