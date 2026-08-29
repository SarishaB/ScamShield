package com.bitwiseoperators.scamshield.services

import com.bitwiseoperators.scamshield.model.*

class RiskFusionService {
    fun fuse(
        extractedText: String,
        urls: List<UrlResult>,
        qrResults: List<QrResult>,
        message: MessageResult?
    ): AnalyzeResponse {
        val components = mutableListOf<Pair<Int, String>>()
        val reasons = mutableListOf<String>()

        urls.maxByOrNull { it.component.score }?.let {
            components += it.component.score to "url"
            reasons += it.component.evidence.take(3).map { e -> e.reason }
        }

        message?.let {
            components += it.component.score to "message"
            reasons += it.component.evidence.take(4).map { e -> e.reason }
        }

        qrResults.mapNotNull { it.urlAnalysis }
            .maxByOrNull { it.component.score }?.let {
                components += it.component.score to "qr"
                reasons += it.component.evidence.take(3).map { e -> e.reason }
            }

        val community = urls.mapNotNull { it.community }.maxByOrNull { it.reports }
        community?.let {
            if (it.corroborated) reasons += "The community database has multiple corroborating reports for an extracted indicator."
        }

        val maxScore = components.maxOfOrNull { it.first } ?: 0
        val average = if (components.isEmpty()) 0 else components.map { it.first }.average().toInt()

        val finalScore = when {
            maxScore >= 80 -> maxScore
            maxScore >= 60 -> maxOf(maxScore, average + 15)
            else -> maxOf(average, maxScore)
        }.coerceIn(0, 100)

        val risk = when {
            finalScore >= 60 -> RiskLevel.HIGH
            finalScore >= 30 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val uniqueReasons = reasons.distinct().take(8)
        val safeAction = when (risk) {
            RiskLevel.HIGH ->
                "Do not click, pay, reply, or install anything. Open the organisation's official app/site manually and verify. Report suspected fraud through your approved reporting channel."
            RiskLevel.MEDIUM ->
                "Pause before interacting. Verify the sender and open the organisation's official app/site manually instead of using the provided link."
            RiskLevel.LOW ->
                "No strong scam indicators were detected. Still avoid sharing OTPs, passwords, PINs, or payment credentials."
        }

        return AnalyzeResponse(
            riskLevel = risk,
            riskScore = finalScore,
            reasons = uniqueReasons.ifEmpty { listOf("No strong scam indicators were detected.") },
            safeAction = safeAction,
            extractedText = extractedText.takeIf { it.isNotBlank() },
            urls = urls,
            qr = qrResults,
            message = message,
            community = community
        )
    }
}
