package com.bitwiseoperators.scamshield.services

import com.bitwiseoperators.scamshield.config.GeminiConfig
import com.bitwiseoperators.scamshield.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.CIO
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.net.URI

/**
 * Message analysis backed by Google AI Studio's Gemini API.
 *
 * URL/UPI extraction remains deterministic because those values are needed
 * by the rest of ScamShield's pipeline. The scam-intent judgement itself
 * comes from Gemini rather than the old local keyword list.
 */
class MessageAnalysisService(
    private val communityService: CommunityService,
    private val geminiConfig: GeminiConfig
) {
    private val httpClient = HttpClient(CIO)

    private val urlRegex = Regex(
        """(?i)\b(?:(?:https?|www)\:\/\/)?(?:[a-z0-9-]+\.)+[a-z]{2,}(?:[\/?#][^\s<]*)?"""
    )

    private val upiRegex = Regex(
        """(?i)\b[a-z0-9._-]{2,}@[a-z]{2,}\b"""
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Serializable
    private data class GeminiRequest(
        @SerialName("systemInstruction") val systemInstruction: GeminiContent? = null,
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig
    )

    @Serializable
    private data class GeminiContent(
        val role: String? = null,
        val parts: List<GeminiPart>
    )

    @Serializable
    private data class GeminiPart(
        val text: String
    )

    @Serializable
    private data class GeminiGenerationConfig(
        @SerialName("responseMimeType") val responseMimeType: String = "application/json",
        @SerialName("responseSchema") val responseSchema: JsonObject
    )

    @Serializable
    private data class GeminiResponse(
        val candidates: List<GeminiCandidate> = emptyList()
    )

    @Serializable
    private data class GeminiCandidate(
        val content: GeminiContent? = null
    )

    @Serializable
    private data class AiAnalysis(
        val score: Int,
        val verdict: String,
        val evidence: List<AiEvidence> = emptyList()
    )

    @Serializable
    private data class AiEvidence(
        val category: String,
        val score: Int,
        val reason: String
    )

    suspend fun analyze(text: String): MessageResult {
        require(text.isNotBlank()) { "Message text must not be blank." }

        val urls = extractUrls(text)
        val upis = extractUpis(text)
        val community = communityService.lookup(text)

        val ai = analyzeWithGemini(text, urls, upis)

        val evidence = ai.evidence
            .map {
                Evidence(
                    feature = it.category,
                    score = it.score.coerceAtLeast(0),
                    reason = it.reason
                )
            }
            .toMutableList()

        var score = ai.score.coerceIn(0, 100)

        if (urls.isNotEmpty()) {
            evidence += Evidence(
                "contains_url",
                0,
                "The message contains a web link; the URL is analyzed separately."
            )
        }

        if (upis.isNotEmpty()) {
            evidence += Evidence(
                "contains_upi",
                0,
                "The message contains a UPI-like payment identifier."
            )
        }

        if (community.corroborated) {
            score = (score + 25).coerceAtMost(100)
            evidence += Evidence(
                "community_reports",
                25,
                "Similar message content has multiple corroborating community reports."
            )
        } else if (community.reports > 0) {
            score = (score + 10).coerceAtMost(100)
            evidence += Evidence(
                "community_reports",
                10,
                "Similar message content has community reports."
            )
        }

        return MessageResult(
            component = ComponentResult(
                score = score,
                verdict = score.toRisk(),
                evidence = evidence.sortedByDescending { it.score }
            ),
            extractedUrls = urls,
            extractedUpiIds = upis,
            community = community
        )
    }

    private suspend fun analyzeWithGemini(
        text: String,
        urls: List<String>,
        upis: List<String>
    ): AiAnalysis {
        if (geminiConfig.apiKey.isBlank()) {
            throw IllegalStateException(
                "Gemini API key is not configured. Set GEMINI_API_KEY in the backend environment."
            )
        }

        val schema = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("score") { put("type", "INTEGER") }
                putJsonObject("verdict") {
                    put("type", "STRING")
                    putJsonArray("enum") {
                        add("LOW")
                        add("MEDIUM")
                        add("HIGH")
                    }
                }
                putJsonObject("evidence") {
                    put("type", "ARRAY")
                    putJsonObject("items") {
                        put("type", "OBJECT")
                        putJsonObject("properties") {
                            putJsonObject("category") { put("type", "STRING") }
                            putJsonObject("score") { put("type", "INTEGER") }
                            putJsonObject("reason") { put("type", "STRING") }
                        }
                        putJsonArray("required") {
                            add("category")
                            add("score")
                            add("reason")
                        }
                    }
                }
            }
            putJsonArray("required") {
                add("score")
                add("verdict")
                add("evidence")
            }
        }

        val systemPrompt = """
            You are ScamShield, a scam-message risk analyst.
            Analyze the supplied message for scam/fraud intent.

            Important rules:
            - Do not decide based only on isolated words. Consider context, intent,
              impersonation, social engineering, urgency, credential theft, payment requests,
              suspicious instructions, fake rewards/refunds, delivery scams, remote-access requests,
              investment promises, and requests for personal information.
            - Legitimate messages can contain links, OTPs, payments, or account language.
              Do not label them scams just because one such indicator appears.
            - Score 0-100 where 0 means clearly benign and 100 means highly likely to be a scam.
            - LOW = 0-29, MEDIUM = 30-59, HIGH = 60-100.
            - Provide concise evidence tied to the actual message.
            - Return ONLY the requested JSON structure.
        """.trimIndent()

        val userPrompt = buildString {
            append("Message to analyze:\n---\n")
            append(text.take(12000))
            append("\n---\n")
            if (urls.isNotEmpty()) {
                append("Extracted URLs (analyzed separately): ")
                append(urls.joinToString(", "))
                append('\n')
            }
            if (upis.isNotEmpty()) {
                append("Extracted UPI-like identifiers: ")
                append(upis.joinToString(", "))
                append('\n')
            }
        }

        val request = GeminiRequest(
            systemInstruction = GeminiContent(
                role = "system",
                parts = listOf(GeminiPart(systemPrompt))
            ),
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(userPrompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseSchema = schema
            )
        )

        val endpoint = geminiConfig.baseUrl.trimEnd('/') +
                "/models/${geminiConfig.model}:generateContent"

        val response = httpClient.post(endpoint) {
            header("x-goog-api-key", geminiConfig.apiKey)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(GeminiRequest.serializer(), request))
        }

        if (!response.status.isSuccess()) {
            val body = response.bodyAsText().take(2000)
            throw IllegalStateException(
                "Gemini API returned HTTP ${response.status.value}: $body"
            )
        }

        val responseBody = json.decodeFromString<GeminiResponse>(response.bodyAsText())
        val modelText = responseBody.candidates
            .asSequence()
            .flatMap { it.content?.parts.orEmpty().asSequence() }
            .map { it.text }
            .firstOrNull { it.isNotBlank() }
            ?: throw IllegalStateException("Gemini returned no analysis content.")

        return json.decodeFromString<AiAnalysis>(stripJsonFences(modelText))
    }

    private fun extractUrls(text: String): List<String> =
        urlRegex.findAll(text)
            .map { it.value.trimEnd('.', ',', '!', '?', ')', ']', '"', '\'') }
            .distinct()
            .toList()

    private fun extractUpis(text: String): List<String> =
        upiRegex.findAll(text)
            .map { it.value }
            .distinct()
            .toList()

    private fun stripJsonFences(value: String): String {
        return value
            .trim()
            .removePrefix("```")
            .removePrefix("json")
            .removeSuffix("```")
            .trim()
    }

    private fun isSuspiciousUrl(url: String): Boolean {
        val normalized = when {
            url.startsWith("www.", ignoreCase = true) -> "https://$url"
            !url.startsWith("http://", ignoreCase = true) &&
                    !url.startsWith("https://", ignoreCase = true) -> "https://$url"
            else -> url
        }

        val uri = runCatching { URI(normalized) }.getOrNull() ?: return false
        val host = uri.host?.lowercase() ?: return false

        val suspiciousTlds = setOf("vip", "top", "click", "shop", "icu", "xyz", "tk", "ml", "ga", "cf")
        if (host.substringAfterLast('.') in suspiciousTlds) return true

        val knownBrands = listOf(
            "usps", "amazon", "paypal", "apple", "microsoft", "google", "netflix",
            "fedex", "dhl", "ups", "bank"
        )

        for (brand in knownBrands) {
            if (host.contains("$brand.") &&
                !host.endsWith("$brand.com") &&
                !host.endsWith("$brand.org") &&
                !host.endsWith("$brand.gov")
            ) return true

            if (host.contains("$brand-") || host.contains("-$brand")) return true
        }

        return false
    }
}

private fun Int.toRisk(): RiskLevel =
    when {
        this >= 60 -> RiskLevel.HIGH
        this >= 30 -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }
