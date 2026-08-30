package com.akslabs.circletosearch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun ScamShieldHomeScreenV2(
    onScan: () -> Unit,
    onCommunity: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val accessibilityEnabled = remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    Scaffold(
        containerColor = ShieldBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("SCAMSHIELD", fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        Text("DIGITAL SECURITY", style = MaterialTheme.typography.labelSmall, color = ShieldMuted, letterSpacing = 1.8.sp)
                    }
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 12.dp).size(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = ShieldViolet.copy(alpha = 0.13f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShieldViolet.copy(alpha = 0.32f))
                    ) {
                        Icon(Icons.Default.Shield, null, tint = ShieldVioletBright, modifier = Modifier.padding(10.dp))
                    }
                },
                actions = { IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings", tint = ShieldMuted) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShieldBackground,
                    titleContentColor = ShieldText
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(2.dp))
            ProtectionCardV2(
                enabled = accessibilityEnabled.value,
                onEnable = { openAccessibilitySettings(context); accessibilityEnabled.value = true }
            )

            SectionLabel("CHECK SOMETHING SUSPICIOUS")
            ElevatedCard(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = ShieldSurface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, ShieldViolet.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Paste anything suspicious", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ShieldText)
                    Text(
                        "URLs, copied messages, screenshots or files can be checked here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShieldMuted
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScanTypeLabel("TEXT")
                        ScanTypeLabel("SCREENSHOT")
                        ScanTypeLabel("FILE")
                    }
                    Button(
                        onClick = onScan,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShieldViolet, contentColor = ColorOnViolet)
                    ) { Text("OPEN ANALYZER", fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp) }
                }
            }

            SectionLabel("COMMUNITY INTELLIGENCE")
            ElevatedCard(
                onClick = onCommunity,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = ShieldSurfaceRaised),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ShieldPurple.copy(alpha = 0.28f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShieldViolet.copy(alpha = 0.22f))
                    ) {
                        Icon(Icons.Default.Groups, null, tint = ShieldVioletBright, modifier = Modifier.padding(11.dp).size(25.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Recent scam reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShieldText)
                        Text("See what the community is reporting", style = MaterialTheme.typography.bodySmall, color = ShieldMuted)
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = ShieldVioletBright)
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

private val ColorOnViolet = ShieldBackground

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ShieldDim, letterSpacing = 1.7.sp)
}

@Composable
private fun ProtectionCardV2(enabled: Boolean, onEnable: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = ShieldSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(ShieldPurple.copy(alpha = 0.26f), ShieldSurface, ShieldSurface)))
                .border(1.dp, ShieldViolet.copy(alpha = 0.22f), RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ShieldViolet.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShieldViolet.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.Default.VerifiedUser, null, tint = ShieldVioletBright, modifier = Modifier.padding(13.dp).size(30.dp))
                    }
                    Spacer(Modifier.width(15.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("PROTECTION STATUS", style = MaterialTheme.typography.labelMedium, color = ShieldMuted, letterSpacing = 1.2.sp)
                        Text(if (enabled) "Protected" else "Setup required", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ShieldText)
                    }
                    if (enabled) {
                        Surface(shape = RoundedCornerShape(50), color = ShieldGreen.copy(alpha = 0.13f)) {
                            Text("ACTIVE", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = ShieldGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        }
                    }
                }
                Text(
                    if (enabled) "Your in-context ScamShield protection is ready." else "Enable ScamShield accessibility access to scan suspicious content from other apps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShieldMuted
                )
                if (!enabled) {
                    Button(onClick = onEnable, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp)) {
                        Text("ENABLE SCAMSHIELD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ScanTypeLabel(label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ShieldSurfaceRaised,
        border = androidx.compose.foundation.BorderStroke(1.dp, ShieldDim.copy(alpha = 0.3f)),
        modifier = Modifier.weight(1f)
    ) {
        Text(label, modifier = Modifier.padding(vertical = 9.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ShieldMuted, letterSpacing = 0.7.sp)
    }
}
