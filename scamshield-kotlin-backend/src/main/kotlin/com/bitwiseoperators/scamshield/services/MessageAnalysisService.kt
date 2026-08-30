package com.bitwiseoperators.scamshield.services

import com.bitwiseoperators.scamshield.model.*
import java.net.URI

class MessageAnalysisService(
    private val communityService: CommunityService
) {
    private val urlRegex = Regex(
        """(?i)\b(?:(?:https?|www)\:\/\/)?(?:[a-z0-9-]+\.)+[a-z]{2,}(?:[\/?#][^\s<]*)?"""
    )

    private val upiRegex = Regex(
        """(?i)\b[a-z0-9._-]{2,}@[a-z]{2,}\b"""
    )

    /*
     * Each category represents a scam-related behavioural signal.
     *
     * We deliberately use multiple related phrases instead of relying
     * on one exact sentence. This makes the detector more robust to
     * variations in scam messages.
     */
    private val categories = listOf(

        "otp" to Pair(
            20,
            listOf(
                "otp",
                "one time password",
                "one-time password",
                "verification code",
                "security code",
                "authentication code"
            )
        ),

        "credential_theft" to Pair(
            20,
            listOf(
                "password",
                "passcode",
                "login",
                "sign in",
                "signin",
                "username",
                "account credentials",
                "verify your account"
            )
        ),

        "urgency" to Pair(
            15,
            listOf(
                "urgent",
                "immediately",
                "act now",
                "within 24 hours",
                "within 48 hours",
                "last warning",
                "final warning",
                "immediate action",
                "as soon as possible",
                "expires today",
                "expires soon"
            )
        ),

        "account_threat" to Pair(
            18,
            listOf(
                "account suspended",
                "account blocked",
                "account will be suspended",
                "account will be blocked",
                "account will be closed",
                "account disabled",
                "kyc expired",
                "kyc has expired",
                "service will stop",
                "access will be revoked"
            )
        ),

        "payment_request" to Pair(
            20,
            listOf(
                "send money",
                "pay now",
                "make a payment",
                "transfer money",
                "bank transfer",
                "upi",
                "payment",
                "deposit",
                "pay",
                "processing fee",
                "delivery fee",
                "verification fee"
            )
        ),

        "reward_refund" to Pair(
            12,
            listOf(
                "cashback",
                "reward",
                "prize",
                "refund",
                "lottery",
                "gift",
                "winner",
                "you have won",
                "claim your"
            )
        ),

        "investment" to Pair(
            18,
            listOf(
                "guaranteed return",
                "guaranteed profit",
                "double your money",
                "investment opportunity",
                "investment",
                "profit",
                "risk free",
                "risk-free",
                "guaranteed income"
            )
        ),

        "impersonation" to Pair(
            18,
            listOf(
                "bank officer",
                "bank representative",
                "police",
                "income tax",
                "customs",
                "customer care",
                "customer support",
                "support team",
                "usps",
                "post office",
                "fedex",
                "dhl",
                "amazon",
                "government department"
            )
        ),

        "remote_access" to Pair(
            25,
            listOf(
                "anydesk",
                "teamviewer",
                "remote access",
                "screen sharing",
                "screen share",
                "install this app",
                "download this app",
                "remote control"
            )
        ),

        "delivery_scam" to Pair(
            18,
            listOf(
                "parcel",
                "package",
                "shipment",
                "delivery",
                "delivery attempt",
                "zip code",
                "postal code",
                "address information",
                "delivery address",
                "parcel detained",
                "package detained",
                "shipment detained",
                "customs clearance"
            )
        ),

        "verification_request" to Pair(
            15,
            listOf(
                "confirm your",
                "confirm the",
                "verify your",
                "verify the",
                "verification required",
                "update your information",
                "update your details",
                "confirm your details",
                "confirm your information"
            )
        ),

        "reply_activation" to Pair(
            20,
            listOf(
                "reply with a y",
                "reply y",
                "reply with yes",
                "reply yes",
                "reply to activate",
                "open it again",
                "activate the link",
                "copy the link",
                "paste the link",
                "open the link"
            )
        )
    )

    fun analyze(text: String): MessageResult {
        val lower = text.lowercase()
        val evidence = mutableListOf<Evidence>()
        var score = 0

        // ---------------------------------------------------------
        // Keyword/category analysis
        // ---------------------------------------------------------

        for ((category, rule) in categories) {
            val (points, terms) = rule

            val hits = terms.filter { term ->
                lower.contains(term)
            }

            if (hits.isNotEmpty()) {

                /*
                 * Award the category's base score.
                 *
                 * Additional matching terms increase confidence,
                 * but each category is capped at its configured
                 * maximum.
                 */
                val awarded = minOf(
                    points,
                    points / 2 + hits.size * 3
                )

                score += awarded

                evidence += Evidence(
                    category,
                    awarded,
                    "Detected ${category.replace('_', ' ')} language: " +
                            hits.take(5).joinToString(", ") +
                            "."
                )
            }
        }

        // ---------------------------------------------------------
        // URL detection
        // ---------------------------------------------------------

        val urls = urlRegex
            .findAll(text)
            .map { it.value.trimEnd('.', ',', '!', '?', ')', ']', '"', '\'') }
            .distinct()
            .toList()

        if (urls.isNotEmpty()) {
            score += 10

            evidence += Evidence(
                "contains_url",
                10,
                "The message contains a web link."
            )
        }

        // ---------------------------------------------------------
        // Suspicious URL structure
        // ---------------------------------------------------------

        val suspiciousUrls = urls.filter { isSuspiciousUrl(it) }

        if (suspiciousUrls.isNotEmpty()) {
            score += 20

            evidence += Evidence(
                "suspicious_url",
                20,
                "The message contains a link with suspicious domain or URL characteristics."
            )
        }

        // ---------------------------------------------------------
        // UPI detection
        // ---------------------------------------------------------

        val upis = upiRegex
            .findAll(text)
            .map { it.value }
            .distinct()
            .toList()

        if (upis.isNotEmpty()) {
            score += 8

            evidence += Evidence(
                "contains_upi",
                8,
                "The message contains a UPI-like payment identifier."
            )
        }

        // ---------------------------------------------------------
        // Excessive punctuation
        // ---------------------------------------------------------

        if (text.count { it == '!' } >= 3) {
            score += 8

            evidence += Evidence(
                "excessive_exclamation",
                8,
                "The message uses unusually urgent punctuation."
            )
        }

        // ---------------------------------------------------------
        // Direct action + link combination
        // ---------------------------------------------------------

        val actionWords = listOf(
            "click",
            "open",
            "visit",
            "tap",
            "confirm",
            "verify",
            "activate",
            "reply",
            "download"
        )

        val hasActionLanguage =
            actionWords.any { lower.contains(it) }

        if (urls.isNotEmpty() && hasActionLanguage) {
            score += 12

            evidence += Evidence(
                "action_link_combination",
                12,
                "The message combines a web link with instructions to take immediate action."
            )
        }

        // ---------------------------------------------------------
        // Personal information request
        // ---------------------------------------------------------

        val personalInfoTerms = listOf(
            "zip code",
            "postal code",
            "address",
            "date of birth",
            "dob",
            "phone number",
            "mobile number",
            "bank details",
            "card details",
            "account number",
            "personal information",
            "personal details"
        )

        val personalInfoHits =
            personalInfoTerms.filter { lower.contains(it) }

        if (personalInfoHits.isNotEmpty()) {
            score += 12

            evidence += Evidence(
                "personal_information_request",
                12,
                "The message references personal or account information: " +
                        personalInfoHits.take(5).joinToString(", ") +
                        "."
            )
        }

        // ---------------------------------------------------------
        // Community intelligence
        // ---------------------------------------------------------

        val community = communityService.lookup(text)

        if (community.corroborated) {
            score += 25

            evidence += Evidence(
                "community_reports",
                25,
                "Similar message content has multiple corroborating community reports."
            )
        } else if (community.reports > 0) {
            score += 10

            evidence += Evidence(
                "community_reports",
                10,
                "Similar message content has community reports."
            )
        }

        // ---------------------------------------------------------
        // Final score
        // ---------------------------------------------------------

        score = score.coerceIn(0, 100)

        return MessageResult(
            component = ComponentResult(
                score,
                score.toRisk(),
                evidence.sortedByDescending { it.score }
            ),

            extractedUrls = urls,
            extractedUpiIds = upis,
            community = community
        )
    }

    /*
     * Detect suspicious URL characteristics.
     *
     * This is intentionally heuristic. A suspicious TLD alone does
     * NOT mean that a URL is malicious. Stronger signals such as
     * deceptive brand-like hostnames are also considered.
     */
    private fun isSuspiciousUrl(url: String): Boolean {
        val normalized =
            if (url.startsWith("www.", ignoreCase = true)) {
                "https://$url"
            } else if (
                !url.startsWith("http://", ignoreCase = true) &&
                !url.startsWith("https://", ignoreCase = true)
            ) {
                "https://$url"
            } else {
                url
            }

        val uri = runCatching {
            URI(normalized)
        }.getOrNull() ?: return false

        val host = uri.host?.lowercase() ?: return false

        val suspiciousTlds = setOf(
            "vip",
            "top",
            "click",
            "shop",
            "icu",
            "xyz",
            "tk",
            "ml",
            "ga",
            "cf"
        )

        val tld = host.substringAfterLast('.', "")

        if (tld in suspiciousTlds) {
            return true
        }

        /*
         * Detect hostnames that contain a brand followed by
         * another domain component, e.g.
         *
         * usps.com-bcamkozq.vip
         *
         * The real registrable domain here is com-bcamkozq.vip,
         * not usps.com.
         */
        val knownBrands = listOf(
            "usps",
            "amazon",
            "paypal",
            "apple",
            "microsoft",
            "google",
            "netflix",
            "fedex",
            "dhl",
            "ups",
            "bank"
        )

        for (brand in knownBrands) {
            if (
                host.contains("$brand.") &&
                !host.endsWith("$brand.com") &&
                !host.endsWith("$brand.org") &&
                !host.endsWith("$brand.gov")
            ) {
                return true
            }

            if (
                host.contains("$brand-") ||
                host.contains("-$brand")
            ) {
                return true
            }
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