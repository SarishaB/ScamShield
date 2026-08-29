package com.akslabs.circletosearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.akslabs.circletosearch.data.ScamDetectionApi
import com.akslabs.circletosearch.utils.UIPreferences
import androidx.compose.ui.platform.LocalContext

@Composable
fun ScamDetectionPanel(
    result: ScamDetectionApi.Result?,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val context = LocalContext.current
    val showAnalysisDetails = remember(context) { UIPreferences(context).isShowAnalysisDetails() }
    val dismissThresholdPx = with(density) { 180.dp.toPx() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .offset { IntOffset(0, dragOffsetPx.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // This sheet is intentionally only draggable downward.
                        dragOffsetPx = (dragOffsetPx + dragAmount.y).coerceAtLeast(0f)
                    },
                    onDragEnd = {
                        if (dragOffsetPx >= dismissThresholdPx) {
                            dragOffsetPx = 0f
                            onClose()
                        } else {
                            dragOffsetPx = 0f
                        }
                    },
                    onDragCancel = { dragOffsetPx = 0f }
                )
            },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search-style drag handle. Pull down to dismiss the scam check.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            RoundedCornerShape(50)
                        )
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Scam check", style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (loading) "Analyzing the selected content…" else "Analysis complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close") }
            }

            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(44.dp).align(Alignment.CenterHorizontally))
                    Text("Checking the selected image with your backend…", modifier = Modifier.align(Alignment.CenterHorizontally))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                error != null -> {
                    Icon(Icons.Default.Error, null, modifier = Modifier.size(42.dp).align(Alignment.CenterHorizontally))
                    Text("Could not complete the scam check", style = MaterialTheme.typography.titleMedium)
                    Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.size(8.dp))
                        Text("Try again")
                    }
                }
                result != null -> {
                    val normalized = result.verdict.trim().uppercase()
                    val isScam = normalized.contains("SCAM")
                    val isSuspicious = normalized.contains("SUSPICIOUS") || normalized.contains("RISK") || normalized.contains("WARNING")
                    val isSafe = normalized == "SAFE" || normalized == "LEGITIMATE"
                    val icon = when { isScam || isSuspicious -> Icons.Default.Warning; isSafe -> Icons.Default.CheckCircle; else -> Icons.Default.Error }
                    val tint = when { isScam -> MaterialTheme.colorScheme.error; isSuspicious -> MaterialTheme.colorScheme.tertiary; isSafe -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                    val title = when { isScam -> "Likely scam"; isSuspicious -> "Suspicious"; isSafe -> "Looks safe"; else -> result.verdict.ifBlank { "Unknown result" } }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(icon, null, modifier = Modifier.size(48.dp), tint = tint)
                        Column {
                            Text(title, style = MaterialTheme.typography.headlineSmall)
                            result.score?.let { Text("Confidence: ${(it.coerceIn(0f, 1f) * 100f).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                    if (showAnalysisDetails) {
                        result.explanation?.takeIf { it.isNotBlank() }?.let { Text(it) }
                    }
                    if (showAnalysisDetails && result.indicators.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text("Why it was flagged", style = MaterialTheme.typography.titleSmall)
                            result.indicators.take(5).forEach { Text("• $it") }
                        }
                    }
                }
            }
        }
    }
}
