package com.akslabs.circletosearch.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** API models for the community intelligence feed. */
data class CommunityReportsResponse(
    val posts: List<CommunityReport> = emptyList(),
    val total: Int = 0
)

data class CommunityReport(
    val id: Int = 0,
    val indicator: String = "",
    val type: String = "",
    val category: String = "",
    val description: String? = null,
    val createdAt: String = ""
)

suspend fun ScamDetectionApi.getCommunityReports(context: Context): CommunityReportsResponse =
    withContext(Dispatchers.IO) {
        val body = requestForCommunityReports(context)
        com.google.gson.Gson().fromJson(body, CommunityReportsResponse::class.java)
    }

private fun ScamDetectionApi.requestForCommunityReports(context: Context): String {
    val method = javaClass.getDeclaredMethod("request", Context::class.java, String::class.java, String::class.java, String::class.java, String::class.java, Set::class.java)
    method.isAccessible = true
    return method.invoke(this, context, "GET", "/api/v1/reports", null, null, (200..299).toSet()) as String
}
