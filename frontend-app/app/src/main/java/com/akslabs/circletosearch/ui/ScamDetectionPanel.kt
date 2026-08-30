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

private val ShieldViolet = Color(0xFF9B6CFF)
private val ShieldBright = Color(0xFFC09BFF)
private val ShieldPurple = Color(0xFF7138D4)
private val ShieldSurface = Color(0xFF110D20)
private val ShieldRaised = Color(0xFF18112B)

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
                        dragOffsetPx = (dragOffsetPx + dragAmount.y).coerceAtLeast(0f)
                    },
                    onDragEnd = {
                        if (dragOffsetPx >= dismissThresholdPx) {
                            dragOffsetPx = 0f
                            onClose()
                        } else dragOffsetPx = 0f
                    },
                    onDragCancel = { dragOffsetPx = 0f }
                )
            },
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        tonalElevation = 10.dp,
        shadowElevation = 18.dp,
        color = ShieldSurface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(42.dp).height(4.dp).background(ShieldViolet.copy(alpha = .55f), RoundedCornerShape(50)))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(ShieldPurple.copy(alpha = .25f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null, tint = ShieldBright, modifier = Modifier.size(23.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("SCAMSHIELD ANALYSIS", color = Color(0xFFF4F0FF), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(if (loading) "Analyzing selected content…" else "Threat assessment", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA49BB8))
                }
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close", tint = Color(0xFFA49BB8)) }
            }

            when {
                loading -> {
                    CircularProgressIndicator(color = ShieldViolet, modifier = Modifier.size(44.dp).align(Alignment.CenterHorizontally))
                    Text("Checking selected content…", color = Color(0xFFA49BB8), modifier = Modifier.align(Alignment.CenterHorizontally))
                    LinearProgressIndicator(color = ShieldViolet, trackColor = ShieldRaised, modifier = Modifier.fillMaxWidth())
                }
                error != null -> {
                    Icon(Icons.Default.Error, null, tint = Color(0xFFFF5F7A), modifier = Modifier.size(42.dp).align(Alignment.CenterHorizontally))
                    Text("Could not complete the scam check", color = Color(0xFFF4F0FF), style = MaterialTheme.typography.titleMedium)
                    Text(error, color = Color(0xFFA49BB8))
                    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.size(8.dp)); Text("Try again") }
                }
                result != null -> {
                    val normalized = result.verdict.trim().uppercase()
                    val isScam = normalized.contains("SCAM")
                    val isSuspicious = normalized.contains("SUSPICIOUS") || normalized.contains("RISK") || normalized.contains("WARNING")
                    val isSafe = normalized == "SAFE" || normalized == "LEGITIMATE"
                    val icon = when { isScam || isSuspicious -> Icons.Default.Warning; isSafe -> Icons.Default.CheckCircle; else -> Icons.Default.Error }
                    val tint = when { isScam -> Color(0xFFFF5F7A); isSuspicious -> Color(0xFFFFC45C); isSafe -> Color(0xFF43E0AE); else -> Color(0xFFA49BB8) }
                    val title = when { isScam -> "LIKELY SCAM"; isSuspicious -> "SUSPICIOUS"; isSafe -> "LOOKS SAFE"; else -> result.verdict.ifBlank { "UNKNOWN RESULT" } }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(icon, null, modifier = Modifier.size(48.dp), tint = tint)
                        Column {
                            Text(title, color = tint, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                            result.score?.let { Text("Confidence: ${(it.coerceIn(0f, 1f) * 100f).toInt()}%", color = Color(0xFFA49BB8)) }
                        }
                    }
                    if (showAnalysisDetails) {
                        result.explanation?.takeIf { it.isNotBlank() }?.let { Text(it, color = Color(0xFFF4F0FF)) }
                    }
                    if (showAnalysisDetails && result.indicators.isNotEmpty()) {
                        Column(Modifier.fillMaxWidth().background(ShieldRaised, RoundedCornerShape(18.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("WHY IT WAS FLAGGED", color = ShieldBright, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            result.indicators.take(5).forEach { Text("• $it", color = Color(0xFFA49BB8)) }
                        }
                    }
                }
            }
        }
    }
}
