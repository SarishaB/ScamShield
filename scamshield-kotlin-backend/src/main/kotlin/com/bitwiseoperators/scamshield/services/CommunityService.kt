package com.bitwiseoperators.scamshield.services

import com.bitwiseoperators.scamshield.db.Database
import com.bitwiseoperators.scamshield.model.*

class CommunityService(private val database: Database) {
    fun lookup(indicator: String): CommunityResult {
        if (indicator.isBlank()) {
            return CommunityResult(0, false)
        }

        // Exact-indicator lookup for URLs/UPIs. For messages we use a normalized
        // text lookup; production deployments should add a privacy-preserving
        // similarity index instead of storing raw message bodies.
        return database.communityResult(indicator)
    }

    fun listPosts(): List<CommunityPost> =
        database.listCommunityPosts()

    fun report(request: ReportRequest) {
        require(request.indicator.isNotBlank()) { "indicator must not be blank" }
        require(request.category.isNotBlank()) { "category must not be blank" }

        database.addReport(
            indicator = request.indicator,
            type = request.type.name,
            category = request.category,
            description = request.description
        )
    }
}
