package com.bitwiseoperators.scamshield.services

import com.bitwiseoperators.scamshield.config.AppConfig
import com.bitwiseoperators.scamshield.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.isSuccess
import java.net.IDN
import java.net.URI
import java.util.Base64

class UrlAnalysisService(
    private val config: AppConfig,
    private val communityService: CommunityService
) {

    private val client = HttpClient(CIO)

    private val shorteners = setOf(
        "bit.ly",
        "tinyurl.com",
        "t.co",
        "goo.gl",
        "is.gd",
        "cutt.ly",
        "ow.ly",
        "rebrand.ly",
        "shorturl.at"
    )

    private val suspiciousTlds = setOf(
        "zip",
        "top",
        "click",
        "work",
        "xyz",
        "buzz",
        "gq",
        "tk",
        "ml"
    )

    // =========================================================
    // URL ANALYSIS
    // =========================================================

    suspend fun analyze(url: String): UrlResult {

        val normalized = normalizeUrl(url)

        val uri =
            runCatching {
                URI(normalized)
            }.getOrNull()

        if (uri == null || uri.host.isNullOrBlank()) {

            return UrlResult(
                url = url,
                normalizedUrl = normalized,

                component = ComponentResult(
                    90,
                    RiskLevel.HIGH,

                    listOf(
                        Evidence(
                            "invalid_url",
                            90,
                            "The extracted value is not a valid web URL."
                        )
                    )
                )
            )
        }

        val host =
            runCatching {
                IDN.toASCII(
                    uri.host.lowercase()
                )
            }.getOrDefault(
                uri.host.lowercase()
            )

        val evidence =
            mutableListOf<Evidence>()

        var score = 0

        fun add(
            points: Int,
            feature: String,
            reason: String
        ) {

            score += points

            evidence += Evidence(
                feature,
                points,
                reason
            )
        }

        // =====================================================
        // HTTPS CHECK
        // =====================================================

        if (uri.scheme.lowercase() != "https") {

            add(
                12,
                "no_https",
                "The link does not use HTTPS."
            )
        }

        // =====================================================
        // IP ADDRESS CHECK
        // =====================================================

        if (
            host.matches(
                Regex(
                    """\d{1,3}(\.\d{1,3}){3}"""
                )
            )
        ) {

            add(
                25,
                "ip_host",
                "The link uses an IP address instead of a normal domain."
            )
        }

        // =====================================================
        // PUNYCODE CHECK
        // =====================================================

        if (host.contains("xn--")) {

            add(
                25,
                "punycode",
                "The domain uses Punycode, which can hide look-alike characters."
            )
        }

        // =====================================================
        // URL SHORTENER CHECK
        // =====================================================

        if (shorteners.contains(host)) {

            add(
                20,
                "url_shortener",
                "The link uses a URL-shortening service, hiding the final destination."
            )
        }

        // =====================================================
        // TLD CHECK
        // =====================================================

        val tld =
            host.substringAfterLast(
                '.',
                ""
            )

        if (tld in suspiciousTlds) {

            add(
                15,
                "suspicious_tld",
                "The domain uses a TLD frequently seen in abusive links."
            )
        }

        // =====================================================
        // USER INFO CHECK
        // =====================================================

        if (uri.userInfo != null) {

            add(
                25,
                "userinfo",
                "The URL contains user-information syntax that can disguise the actual host."
            )
        }

        // =====================================================
        // URL LENGTH CHECK
        // =====================================================

        if (normalized.length > 180) {

            add(
                10,
                "long_url",
                "The URL is unusually long and contains more room for obfuscation."
            )
        }

        // =====================================================
        // SCAM KEYWORD CHECK
        // =====================================================

        val lower =
            normalized.lowercase()

        val riskyTerms =
            mapOf(
                "verify" to 8,
                "verification" to 8,
                "login" to 6,
                "signin" to 6,
                "password" to 10,
                "otp" to 12,
                "kyc" to 10,
                "wallet" to 7,
                "claim" to 6,
                "reward" to 7,
                "refund" to 7,
                "urgent" to 8,
                "suspended" to 10,
                "bank" to 5
            )

        riskyTerms.forEach { (term, points) ->

            if (lower.contains(term)) {

                add(
                    points,
                    "keyword_$term",
                    "The URL contains the scam-related term '$term'."
                )
            }
        }

        // =====================================================
        // @ CHARACTER CHECK
        // =====================================================

        if (
            Regex("@")
                .containsMatchIn(normalized)
        ) {

            add(
                8,
                "at_character",
                "The URL contains '@', which can be used in deceptive URLs."
            )
        }

        score =
            score.coerceIn(
                0,
                100
            )

        // =====================================================
        // COMMUNITY INTELLIGENCE
        // =====================================================

        val community =
            communityService.lookup(
                normalized
            )

        if (community.corroborated) {

            add(
                30,
                "community_reports",
                "This exact indicator has multiple corroborating community reports."
            )

        } else if (community.reports > 0) {

            add(
                12,
                "community_reports",
                "This indicator has community reports, but they are not yet corroborated."
            )
        }

        // =====================================================
        // VIRUSTOTAL
        // =====================================================

        val vt =
            lookupVirusTotal(
                normalized
            )

        if (vt?.found == true) {

            val vtPoints =
                when {

                    vt.malicious >= 3 ->
                        35

                    vt.malicious > 0 ||
                            vt.suspicious >= 3 ->
                        25

                    vt.suspicious > 0 ->
                        12

                    else ->
                        0
                }

            if (vtPoints > 0) {

                add(
                    vtPoints,
                    "virustotal",
                    "VirusTotal has malicious/suspicious detections for this URL."
                )
            }
        }

        // =====================================================
        // FINAL SCORE
        // =====================================================

        score =
            score.coerceIn(
                0,
                100
            )

        return UrlResult(

            url = url,

            normalizedUrl =
                normalized,

            component =
                ComponentResult(
                    score,
                    score.toRisk(),
                    evidence.sortedByDescending {
                        it.score
                    }
                ),

            domain =
                host,

            virusTotal =
                vt,

            community =
                community
        )
    }


    // =========================================================
    // VIRUSTOTAL LOOKUP
    // =========================================================

    private suspend fun lookupVirusTotal(
        url: String
    ): VirusTotalResult? {

        val key =
            config.virusTotal.apiKey

        if (key.isBlank()) {
            return null
        }

        return try {

            val id =
                Base64
                    .getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                        url.toByteArray()
                    )

            val response =
                client.get(
                    "https://www.virustotal.com/api/v3/urls/$id"
                ) {

                    header(
                        "x-apikey",
                        key
                    )
                }

            if (!response.status.isSuccess()) {

                return VirusTotalResult(
                    found = false
                )
            }

            val body =
                response.body<VtUrlResponse>()

            val stats =
                body.data.attributes.lastAnalysisStats

            VirusTotalResult(

                found = true,

                malicious =
                    stats.malicious,

                suspicious =
                    stats.suspicious,

                harmless =
                    stats.harmless,

                undetected =
                    stats.undetected
            )

        } catch (_: Exception) {

            null
        }
    }


    // =========================================================
    // URL NORMALIZATION
    // =========================================================

    private fun normalizeUrl(
        raw: String
    ): String {

        var value =
            raw.trim()

        if (
            !value.startsWith(
                "http://",
                true
            ) &&
            !value.startsWith(
                "https://",
                true
            )
        ) {

            value =
                "https://$value"
        }

        return value
    }
}


// =============================================================
// VIRUSTOTAL RESPONSE MODELS
// =============================================================

@kotlinx.serialization.Serializable
private data class VtUrlResponse(
    val data: VtData
)

@kotlinx.serialization.Serializable
private data class VtData(
    val attributes: VtAttributes
)

@kotlinx.serialization.Serializable
private data class VtAttributes(
    val lastAnalysisStats: VtStats
)

@kotlinx.serialization.Serializable
private data class VtStats(

    val malicious: Int = 0,

    val suspicious: Int = 0,

    val harmless: Int = 0,

    val undetected: Int = 0
)


// =============================================================
// RISK LEVEL CONVERSION
// =============================================================

private fun Int.toRisk(): RiskLevel =

    when {

        this >= 60 ->
            RiskLevel.HIGH

        this >= 30 ->
            RiskLevel.MEDIUM

        else ->
            RiskLevel.LOW
    }