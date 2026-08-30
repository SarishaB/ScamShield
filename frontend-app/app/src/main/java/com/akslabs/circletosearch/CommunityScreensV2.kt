package com.akslabs.circletosearch

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreenV2(onBack: () -> Unit, onReport: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(title = { Text("Community reports") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text("RECENTLY REPORTED SCAMS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Text("Community-submitted indicators and scam patterns.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            CommunityReportV2("Fake delivery fee SMS", "Phishing")
            CommunityReportV2("UPI refund impersonation", "Payment fraud")
            CommunityReportV2("KYC expiry message", "Credential theft")
            CommunityReportV2("Fake customer-care number", "Impersonation")

            Button(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text("REPORT A SCAM") }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("CHAKSHU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("Official channel for reporting suspected fraudulent communications.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = { expanded = !expanded }) { Text(if (expanded) "COLLAPSE" else "HOW TO REPORT") }
                    if (expanded) {
                        Text("Chakshu is part of the Government of India's Sanchar Saathi service. Use it to report suspected fraudulent communications such as scam calls, SMS or WhatsApp messages.", style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sancharsaathi.gov.in/sfc/"))) }) { Text("OPEN CHAKSHU →") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityReportV2(title: String, category: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WarningAmber, null, tint = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    Scaffold(topBar = {
        TopAppBar(title = { Text("Report a scam") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("REPORT A SCAM", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Record the observed scam. Do not enter passwords, OTPs or PINs.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            ReportField("Suspicious URL / phone / indicator", indicator) { indicator = it; error = null }

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Indicator type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    types.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { type = option; expanded = false })
                    }
                }
            }

            ReportField("Scam category", category) { category = it; error = null }
            ReportField("When / where observed", whenWhere) { whenWhere = it }
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("What happened?") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            Text("The API accepts indicator, type, category and optional description. When/where is appended to description.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Button(
                onClick = {
                    scope.launch {
                        submitting = true
                        message = null
                        error = null
                        try {
                            val combinedDescription = listOf(
                                description.trim(),
                                whenWhere.trim().takeIf { it.isNotBlank() }?.let { "Observed: $it" }
                            ).filterNotNull().filter { it.isNotBlank() }.joinToString("\n")
                            val response = ScamDetectionApi.report(
                                context = context,
                                indicator = indicator,
                                type = type,
                                category = category,
                                description = combinedDescription.ifBlank { null }
                            )
                            message = response.message
                        } catch (e: Exception) {
                            error = e.message ?: "Could not submit report"
                        } finally {
                            submitting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !submitting && indicator.isNotBlank() && type.isNotBlank() && category.isNotBlank()
            ) {
                if (submitting) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("SUBMIT REPORT")
            }

            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ReportField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}
