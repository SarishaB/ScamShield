package com.akslabs.circletosearch

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akslabs.circletosearch.data.ScamDetectionApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedScanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var result by remember { mutableStateOf<ScamDetectionApi.AnalyzeResponse?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFile = uri
        if (uri != null) text = ""
        result = null
        error = null
    }

    fun analyze() {
        scope.launch {
            loading = true
            error = null
            result = null
            try {
                result = when {
                    selectedFile != null -> ScamDetectionApi.analyzeScreenshot(context, selectedFile!!)
                    text.trim().matches(Regex("(?i)^https?://\\S+$")) -> ScamDetectionApi.analyzeUrl(context, text.trim())
                    text.isNotBlank() -> ScamDetectionApi.analyzeText(context, text)
                    else -> throw IllegalArgumentException("Enter text or select a screenshot first.")
                }
            } catch (e: Exception) {
                error = e.message ?: "Unable to contact ScamShield backend"
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScamShield analysis") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("CHECK A MESSAGE, URL OR SCREENSHOT", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "ScamShield looks for suspicious language, risky links, impersonation signals, and other patterns that may indicate fraud before you act on a message or image.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it; selectedFile = null; result = null; error = null },
                modifier = Modifier.fillMaxWidth(),
                minLines = 7,
                label = { Text("TEXT / URL") },
                placeholder = { Text("Paste a suspicious message or URL…") }
            )

            OutlinedButton(
                onClick = { clipboard.getText()?.let { text = it.text; selectedFile = null; result = null; error = null } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentPaste, null)
                Spacer(Modifier.width(8.dp))
                Text("PASTE FROM CLIPBOARD")
            }

            ElevatedCard(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("SCREENSHOT", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (selectedFile == null) "Choose an image to scan with OCR + QR detection" else "Screenshot selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = ::analyze,
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && (text.isNotBlank() || selectedFile != null)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("ANALYZE WITH SCAMSHIELD")
            }

            error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(10.dp))
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            result?.let { analysis ->
                val high = analysis.riskLevel.equals("HIGH", true)
                val medium = analysis.riskLevel.equals("MEDIUM", true)
                val tint = when { high -> MaterialTheme.colorScheme.error; medium -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.primary }
                val icon = when { high || medium -> Icons.Default.Warning; else -> Icons.Default.CheckCircle }
                Card {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, tint = tint, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${analysis.riskLevel} RISK", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = tint)
                                Text("Risk score: ${analysis.riskScore}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (analysis.reasons.isNotEmpty()) {
                            Text("Reasons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            analysis.reasons.take(8).forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                        }
                        if (analysis.safeAction.isNotBlank()) {
                            HorizontalDivider()
                            Text(analysis.safeAction, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
