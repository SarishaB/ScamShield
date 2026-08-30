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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedScanScreen(onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var text by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedFile = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Content analysis") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("PASTE ANY SUSPICIOUS URLS, FILES, MESSAGES ETC", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Submit copied text, screenshots, URLs or files for analysis.", color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 7,
                label = { Text("TEXT / URL") },
                placeholder = { Text("Paste copied message, URL or text…") }
            )

            OutlinedButton(
                onClick = { clipboard.getText()?.let { text = it.text } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentPaste, null)
                Spacer(Modifier.width(8.dp))
                Text("PASTE FROM CLIPBOARD")
            }

            ElevatedCard(onClick = { picker.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("SCREENSHOT / FILE", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (selectedFile == null) "Attach an image or file" else "File selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = { /* Backend analysis hook */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank() || selectedFile != null
            ) { Text("ANALYZE CONTENT") }
        }
    }
}
