package com.akslabs.circletosearch

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.akslabs.circletosearch.data.CommunityReportsApi
import com.akslabs.circletosearch.data.ScamDetectionApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreenV2(onReport: () -> Unit) {
    val context = LocalContext.current
    var reports by remember { mutableStateOf<List<CommunityReportsApi.CommunityPost>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") }
    var filterExpanded by remember { mutableStateOf(false) }
    var infoExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val filterOptions = listOf("ALL", "URL", "MESSAGE", "QR", "UPI")

    suspend fun loadReports() {
        loading = true
        try {
            reports = CommunityReportsApi.getReports(context)
            loadError = null
        } catch (e: Exception) {
            if (reports.isEmpty()) loadError = e.message ?: "Unable to load community reports."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loadReports()
        while (true) {
            delay(30_000L)
            loadReports()
        }
    }

    val filteredReports = remember(reports, searchQuery, selectedType) {
        val query = searchQuery.trim()
        reports.filter { report ->
            val matchesType = selectedType == "ALL" || report.type.equals(selectedType, ignoreCase = true)
            val matchesSearch = query.isBlank() || listOfNotNull(
                report.indicator, report.category, report.description, report.type
            ).any { it.contains(query, ignoreCase = true) }
            matchesType && matchesSearch
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Community", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text("Shared scam reports", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { loadReports() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text("Report a scam") }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search reports") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${filteredReports.size} ${if (filteredReports.size == 1) "report" else "reports"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    androidx.compose.material3.ExposedDropdownMenuBox(
                        expanded = filterExpanded,
                        onExpandedChange = { filterExpanded = !filterExpanded }
                    ) {
                        OutlinedButton(onClick = { filterExpanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(selectedType.lowercase().replaceFirstChar { it.uppercase() })
                        }
                        DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                            filterOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = { selectedType = option; filterExpanded = false }
                                )
                            }
                        }
                    }
                }
                if (loadError != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(loadError.orEmpty(), Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (!loading && filteredReports.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Icon(Icons.Default.Group, null, Modifier.padding(16.dp).size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("No reports found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Try another search or be the first to report a scam.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (loading) {
                        item { Text("Loading reports…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    items(filteredReports, key = { it.id }) { report -> CommunityReportCard(report) }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Official reporting", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    "Chakshu, part of Sanchar Saathi, is an official Government of India facility for reporting suspected fraudulent communications.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = { infoExpanded = !infoExpanded }) {
                                    Text(if (infoExpanded) "Hide details" else "Learn more")
                                }
                                if (infoExpanded) {
                                    Text(
                                        "Use the official portal to report suspected fraud calls, SMS and WhatsApp messages.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sancharsaathi.gov.in/sfc/"))) }) {
                                        Text("Open Chakshu")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityReportCard(report: CommunityReportsApi.CommunityPost) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.errorContainer) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(9.dp).size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(report.indicator, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${report.category} • ${report.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            report.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = { }, label = { Text(formatReportTime(report.createdAt)) })
            }
        }
    }
}

private fun formatReportTime(value: String): String = value.replace("T", " ").removeSuffix("Z")

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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Report a scam", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Help protect the community", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Never enter passwords, OTPs or PINs.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            ReportField("Suspicious content", indicator) { indicator = it; error = null }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(value = type, onValueChange = {}, readOnly = true, label = { Text("Indicator type") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    types.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { type = option; expanded = false }) }
                }
            }
            ReportField("Scam category", category) { category = it; error = null }
            ReportField("When / where observed", whenWhere) { whenWhere = it }
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("What happened?") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            Button(
                onClick = {
                    scope.launch {
                        submitting = true; message = null; error = null
                        try {
                            val combinedDescription = listOf(
                                description.trim(),
                                whenWhere.trim().takeIf { it.isNotBlank() }?.let { "Observed: $it" }
                            ).filterNotNull().filter { it.isNotBlank() }.joinToString("\n")
                            val response = ScamDetectionApi.report(context, indicator, type, category, combinedDescription.ifBlank { null })
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
                Text(if (submitting) "Submitting…" else "Submit report")
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReportField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}
