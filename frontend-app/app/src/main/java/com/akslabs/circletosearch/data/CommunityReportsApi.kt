package com.akslabs.circletosearch.data

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object CommunityReportsApi {
    data class CommunityPost(
        val id: Long,
        val indicator: String,
        val type: String,
        val category: String,
        val description: String?,
        val createdAt: String
    )

    private data class ReportsResponse(
        val posts: List<CommunityPost>,
        val total: Long
    )

    suspend fun getReports(): List<CommunityPost> = withContext(Dispatchers.IO) {
        val endpoint = ScamDetectionApi.API_URL.substringBeforeLast("/") + "/reports"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { BufferedReader(InputStreamReader(it)).readText() }.orEmpty()
            if (status !in 200..299) {
                throw IllegalStateException("Backend returned HTTP $status")
            }
            Gson().fromJson(body, ReportsResponse::class.java)?.posts ?: emptyList()
        } finally {
            connection.disconnect()
        }
    }
}
