package com.akslabs.circletosearch

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akslabs.circletosearch.data.ScamDetectionApi
import com.akslabs.circletosearch.ui.theme.ShieldAmber
import com.akslabs.circletosearch.ui.theme.ShieldBackground
import com.akslabs.circletosearch.ui.theme.ShieldDim
import com.akslabs.circletosearch.ui.theme.ShieldMuted
import com.akslabs.circletosearch.ui.theme.ShieldPurple
import com.akslabs.circletosearch.ui.theme.ShieldRed
import com.akslabs.circletosearch.ui.theme.ShieldSurface
import com.akslabs.circletosearch.ui.theme.ShieldSurfaceRaised
import com.akslabs.circletosearch.ui.theme.ShieldText
import com.akslabs.circletosearch.ui.theme.ShieldViolet
import com.akslabs.circletosearch.ui.theme.ShieldVioletBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreenV2(onBack: () -> Unit, onReport: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(containerColor = ShieldBackground, topBar = {
        TopAppBar(
            title = { Text("COMMUNITY", fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = ShieldMuted) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldBackground, titleContentColor = ShieldText)
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(4.dp))
            Text("Recent scam reports", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ShieldText)
            Text("See what people are reporting and help others stay ahead of active scams.", style = MaterialTheme.typography.bodyMedium, color = ShieldMuted)
            CommunityReportV2("Fake delivery fee SMS", "Phishing", ShieldAmber)
            CommunityReportV2("UPI refund impersonation", "Payment fraud", ShieldRed)
            CommunityReportV2("KYC expiry message", "Credential theft", ShieldViolet)
            CommunityReportV2("Fake customer-care number", "Impersonation", ShieldAmber)

            Button(onClick = onReport, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp)) { Text("REPORT A SCAM", fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp) }

            Card(colors = CardDefaults.cardColors(containerColor = ShieldSurface), shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, ShieldViolet.copy(alpha = 0.16f))) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = ShieldPurple.copy(alpha = 0.22f)) { Icon(Icons.Default.Info, null, tint = ShieldVioletBright, modifier = Modifier.padding(9.dp).size(20.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Text("WHAT IS CHAKSHU?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ShieldText, letterSpacing = 0.8.sp)
                    }
                    Text("An official Government of India facility for reporting suspected fraudulent communications.", style = MaterialTheme.typography.bodySmall, color = ShieldMuted)
                    OutlinedButton(onClick = { expanded = !expanded }, shape = RoundedCornerShape(13.dp)) { Text(if (expanded) "HIDE DETAILS" else "LEARN MORE") }
                    if (expanded) {
                        Text("Chakshu is part of Sanchar Saathi. It can be used to report suspected fraud communications such as scam calls, SMS and WhatsApp messages.", style = MaterialTheme.typography.bodySmall, color = ShieldText)
                        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sancharsaathi.gov.in/sfc/"))) }) { Text("OPEN CHAKSHU →", color = ShieldVioletBright) }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CommunityReportV2(title: String, category: String, accent: androidx.compose.ui.graphics.Color) {
    Card(colors = CardDefaults.cardColors(containerColor = ShieldSurfaceRaised), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(11.dp), color = accent.copy(alpha = 0.12f)) { Icon(Icons.Default.WarningAmber, null, tint = accent, modifier = Modifier.padding(8.dp).size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = ShieldText)
                Text(category, style = MaterialTheme.typography.bodySmall, color = ShieldMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScamScreenV2(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var indicator by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var whenWhere by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("URL", "MESSAGE", "QR", "UPI")

    Scaffold(containerColor = ShieldBackground, topBar = {
        TopAppBar(title = { Text("REPORT", fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = ShieldMuted) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldBackground, titleContentColor = ShieldText))
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(4.dp))
            Text("Report a scam", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ShieldText)
            Text("Record what you observed. Never enter passwords, OTPs or PINs.", color = ShieldMuted)
            ReportField("Suspicious URL / phone / indicator", indicator) { indicator = it; error = null }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(value = type, onValueChange = {}, readOnly = true, label = { Text("Indicator type") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(15.dp))
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { types.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { type = option; expanded = false }) } }
            }
            ReportField("Scam category", category) { category = it; error = null }
            ReportField("When / where observed", whenWhere) { whenWhere = it }
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("What happened?") }, modifier = Modifier.fillMaxWidth(), minLines = 5, shape = RoundedCornerShape(15.dp))
            Button(onClick = {
                scope.launch {
                    submitting = true; message = null; error = null
                    try {
                        val combinedDescription = listOf(description.trim(), whenWhere.trim().takeIf { it.isNotBlank() }?.let { "Observed: $it" }).filterNotNull().filter { it.isNotBlank() }.joinToString("\n")
                        val response = ScamDetectionApi.report(context, indicator, type, category, combinedDescription.ifBlank { null })
                        message = response.message
                    } catch (e: Exception) { error = e.message ?: "Could not submit report" } finally { submitting = false }
                }
            }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp), enabled = !submitting && indicator.isNotBlank() && type.isNotBlank() && category.isNotBlank()) {
                if (submitting) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("SUBMIT REPORT", fontWeight = FontWeight.Bold)
            }
            message?.let { Text(it, color = ShieldVioletBright) }
            error?.let { Text(it, color = ShieldRed) }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ReportField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp))
}
