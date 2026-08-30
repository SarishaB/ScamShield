package com.bitwiseoperators.scamshield.model

import kotlinx.serialization.Serializable

@Serializable
enum class RiskLevel { LOW, MEDIUM, HIGH }

@Serializable
enum class IndicatorType { URL, MESSAGE, QR, UPI }

@Serializable
data class Evidence(
    val feature: String,
    val score: Int,
    val reason: String
)

@Serializable
data class ComponentResult(
    val score: Int,
    val verdict: RiskLevel,
    val evidence: List<Evidence> = emptyList()
)

@Serializable
data class UrlResult(
    val url: String,
    val normalizedUrl: String,
    val component: ComponentResult,
    val domain: String? = null,
    val virusTotal: VirusTotalResult? = null,
    val community: CommunityResult? = null
)

@Serializable
data class VirusTotalResult(
    val found: Boolean,
    val malicious: Int = 0,
    val suspicious: Int = 0,
    val harmless: Int = 0,
    val undetected: Int = 0
)

@Serializable
data class MessageResult(
    val component: ComponentResult,
    val extractedUrls: List<String>,
    val extractedUpiIds: List<String>,
    val community: CommunityResult? = null
)

@Serializable
data class QrResult(
    val decoded: Boolean,
    val rawValue: String? = null,
    val type: String? = null,
    val urlAnalysis: UrlResult? = null
)

@Serializable
data class CommunityResult(
    val reports: Int,
    val corroborated: Boolean,
    val categories: List<String> = emptyList()
)

@Serializable
data class AnalyzeResponse(
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val reasons: List<String>,
    val safeAction: String,
    val extractedText: String? = null,
    val urls: List<UrlResult> = emptyList(),
    val qr: List<QrResult> = emptyList(),
    val message: MessageResult? = null,
    val community: CommunityResult? = null
)

@Serializable
data class ReportRequest(
    val indicator: String,
    val type: IndicatorType,
    val category: String,
    val description: String? = null
)

@Serializable
data class CommunityPost(
    val id: Long,
    val indicator: String,
    val type: IndicatorType,
    val category: String,
    val description: String? = null,
    val createdAt: String
)

@Serializable
data class CommunityPostsResponse(
    val posts: List<CommunityPost>,
    val total: Long
)

@Serializable
data class ReportResponse(
    val accepted: Boolean,
    val message: String
)

@Serializable
data class HealthResponse(
    val status: String,
    val database: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)
