package com.bitwiseoperators.scamshield.services

import com.bitwiseoperators.scamshield.model.*

class MessageAnalysisService(
    private val communityService: CommunityService
) {
    private val urlRegex = Regex(
        """(?i)\b(?:(?:https?|www)\:\/\/)?(?:[a-z0-9-]+\.)+[a-z]{2,}(?:[\/?#][^\s<]*)?"""
    )

    private val upiRegex = Regex(
        """(?i)\b[a-z0-9._-]{2,}@[a-z]{2,}\b"""
    )

    private val categories = listOf(
        "otp" to Pair(20, listOf("otp", "one time password", "verification code")),
        "credential_theft" to Pair(18, listOf("password", "passcode", "login", "sign in", "signin")),
        "urgency" to Pair(15, listOf("urgent", "immediately", "act now", "within 24 hours", "last warning")),
        "account_threat" to Pair(18, listOf("account suspended", "account blocked", "kyc expired", "service will stop")),
        "payment_request" to Pair(20, listOf("send money", "pay now", "transfer", "upi", "payment", "deposit")),
        "reward_refund" to Pair(12, listOf("cashback", "reward", "prize", "refund", "lottery", "gift")),
        "investment" to Pair(18, listOf("guaranteed return", "double your money", "investment", "profit")),
        "impersonation" to Pair(16, listOf("bank officer", "police", "income tax", "customs", "customer care", "support team")),
        "remote_access" to Pair(25, listOf("anydesk", "teamviewer", "remote access", "screen sharing", "install this app"))
    )

    fun analyze(text: String): MessageResult {
        val lower = text.lowercase()
        val evidence = mutableListOf<Evidence>()
        var score = 0

        for ((category, rule) in categories) {
            val (points, terms) = rule
            val hits = terms.filter { lower.contains(it) }
            if (hits.isNotEmpty()) {
                val awarded = minOf(points, hits.size * (points / 2 + 1))
                score += awarded
                evidence += Evidence(
                    category,
                    awarded,
                    "Detected ${category.replace('_', ' ')} language: ${hits.joinToString(", ")}."
                )
            }
        }

        if (text.count { it == '!' } >= 3) {
            score += 8
            evidence += Evidence("excessive_exclamation", 8, "The message uses unusually urgent punctuation.")
        }

        val urls = urlRegex.findAll(text).map { it.value }.distinct().toList()
        val upis = upiRegex.findAll(text).map { it.value }.distinct().toList()

        if (urls.isNotEmpty()) {
            score += 10
            evidence += Evidence("contains_url", 10, "The message contains a web link.")
        }

        if (upis.isNotEmpty()) {
            score += 8
            evidence += Evidence("contains_upi", 8, "The message contains a UPI-like identifier.")
        }

        val community = communityService.lookup(text)
        if (community.corroborated) {
            score += 25
            evidence += Evidence("community_reports", 25, "Similar message content has multiple corroborating community reports.")
        } else if (community.reports > 0) {
            score += 10
            evidence += Evidence("community_reports", 10, "Similar message content has community reports.")
        }

        score = score.coerceIn(0, 100)
        return MessageResult(
            component = ComponentResult(score, score.toRisk(), evidence.sortedByDescending { it.score }),
            extractedUrls = urls,
            extractedUpiIds = upis,
            community = community
        )
    }
}

private fun Int.toRisk(): RiskLevel = when {
    this >= 60 -> RiskLevel.HIGH
    this >= 30 -> RiskLevel.MEDIUM
    else -> RiskLevel.LOW
}
