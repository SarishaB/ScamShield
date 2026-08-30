package com.akslabs.circletosearch

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

private val Navy = Color(0xFF080B14)
private val Panel = Color(0xFF111624)
private val Panel2 = Color(0xFF171D2E)
private val Violet = Color(0xFF7C5CFF)
private val Blue = Color(0xFF3C9DFF)
private val Green = Color(0xFF35D49A)
private val Amber = Color(0xFFFFB84D)

@Composable
fun ScamShieldHomeScreen(
    onManualScan: () -> Unit,
    onCommunity: () -> Unit,
    onSettings: () -> Unit,
    onOcrSettings: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val accessibilityEnabled = remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Violet, Blue))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("SCAMSHIELD", color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                    Text("Your digital shield", color = Color(0xFF9BA6BD), fontSize = 12.sp)
                }
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, "Settings", tint = Color(0xFFB9C1D3))
            }
        }

        Spacer(Modifier.height(26.dp))

        ShieldStatusCard(accessibilityEnabled.value) {
            openAccessibilitySettings(context)
        }

        Spacer(Modifier.height(26.dp))
        Text("WHAT DO YOU WANT TO CHECK?", color = Color(0xFF8D98AF), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("Message", "Paste text", Icons.Default.Message, Violet, Modifier.weight(1f), onManualScan)
            ActionTile("Link", "Check a URL", Icons.Default.Link, Blue, Modifier.weight(1f), onManualScan)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("Clipboard", "Analyze copied text", Icons.Default.ContentPaste, Green, Modifier.weight(1f), onManualScan)
            ActionTile("Community", "Recent scam intel", Icons.Default.Groups, Amber, Modifier.weight(1f), onCommunity)
        }

        Spacer(Modifier.height(26.dp))
        Text("RECENT THREAT SIGNALS", color = Color(0xFF8D98AF), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(10.dp))
        SignalCard("Fake delivery messages", "Community reports", Amber, "Trending")
        Spacer(Modifier.height(9.dp))
        SignalCard("Payment / OTP requests", "High-risk pattern", Color(0xFFFF647C), "Watch")

        Spacer(Modifier.height(24.dp))
        Text(
            "ScamShield analyzes messages, links and screenshots for risk signals. When something looks dangerous, pause and verify through the organisation's official channel.",
            color = Color(0xFF7F899D), fontSize = 12.sp, lineHeight = 18.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ShieldStatusCard(enabled: Boolean, onEnable: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF15152A), Color(0xFF101A2A))))
            .border(1.dp, Violet.copy(alpha = .35f), RoundedCornerShape(28.dp))
            .padding(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(92.dp).clip(CircleShape).background(Violet.copy(alpha = .13f)).border(1.dp, Violet.copy(alpha = .5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, null, tint = Violet, modifier = Modifier.size(46.dp))
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(if (enabled) "PROTECTION ACTIVE" else "PROTECTION NEEDS SETUP", color = if (enabled) Green else Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(5.dp))
                Text(if (enabled) "Your shield is ready." else "Enable the shield to scan content anywhere.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(if (enabled) "Use Circle to Search from other apps." else "Accessibility access is required for the in-context scanner.", color = Color(0xFF9BA6BD), fontSize = 12.sp, lineHeight = 17.sp)
                if (!enabled) {
                    Spacer(Modifier.height(9.dp))
                    Text("ENABLE NOW →", color = Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onEnable() })
                }
            }
        }
    }
}

@Composable
private fun ActionTile(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Panel)
            .border(1.dp, Color.White.copy(alpha = .07f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(23.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, color = Color(0xFF7F899D), fontSize = 11.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun SignalCard(title: String, subtitle: String, accent: Color, tag: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Panel).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Warning, null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF7F899D), fontSize = 11.sp)
        }
        Text(tag, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ManualScanScreen(onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var text by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().background(Navy).padding(20.dp).verticalScroll(rememberScrollState())) {
        TopBar("CHECK SOMETHING", onBack)
        Spacer(Modifier.height(24.dp))
        Text("Paste a suspicious message or link", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("", color = Color(0xFF929DB2), fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Panel).border(1.dp, Color.White.copy(alpha=.08f), RoundedCornerShape(20.dp)).padding(16.dp)) {
            androidx.compose.foundation.text.BasicTextField(value = text, onValueChange = { text = it }, textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp), minLines = 7, modifier = Modifier.fillMaxWidth())
            if (text.isEmpty()) Text("Paste the message here…", color = Color(0xFF687287), fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("PASTE", color = Violet, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Panel2).clickable { clipboard.getText()?.let { text = it.text } }.padding(horizontal = 16.dp, vertical = 12.dp))
            Text("CLEAR", color = Color(0xFF9BA6BD), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Panel2).clickable { text = ""; result = null }.padding(horizontal = 16.dp, vertical = 12.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text("ANALYZE", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Brush.horizontalGradient(listOf(Violet, Blue))).clickable { result = if (text.isBlank()) "Paste something first." else "Ready to connect to ScamShield analysis." }.padding(16.dp), textAlign = TextAlign.Center)
        result?.let { Spacer(Modifier.height(18.dp)); Text(it, color = Amber, fontSize = 13.sp) }
    }
}

@Composable
fun CommunityScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Navy).padding(20.dp).verticalScroll(rememberScrollState())) {
        TopBar("COMMUNITY INTELLIGENCE", onBack)
        Spacer(Modifier.height(22.dp))
        Text("Know what scammers are trying now.", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text("These are UI placeholders until the community-feed endpoint is implemented. They are deliberately not presented as live data.", color = Color(0xFF929DB2), fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(18.dp))
        CommunityItem("Fake delivery fee SMS", "PHISHING · 342 reports", Amber)
        CommunityItem("UPI refund impersonation", "UPI FRAUD · 218 reports", Color(0xFFFF647C))
        CommunityItem("KYC expiry message", "CREDENTIAL THEFT · 154 reports", Violet)
        CommunityItem("Fake customer-care number", "IMPERSONATION · 97 reports", Blue)
    }
}

@Composable
private fun CommunityItem(title: String, subtitle: String, accent: Color) {
    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(18.dp)).background(Panel).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text(subtitle, color = Color(0xFF7F899D), fontSize = 11.sp) }
        Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF657086), modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Back", tint = Color(0xFFB9C1D3)) }
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
