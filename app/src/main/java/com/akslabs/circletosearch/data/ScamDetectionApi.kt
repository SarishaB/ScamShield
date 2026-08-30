package com.akslabs.circletosearch.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ScamDetectionApi {
    private const val PREFS = "scamshield_api"
    private const val BASE_URL_KEY = "base_url"
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"
    @Volatile private var cachedBaseUrl: String? = null

    data class AnalyzeResponse(val riskLevel: String, val riskScore: Int, val reasons: List<String>, val safeAction: String, val extractedText: String? = null, val urls: List<UrlResult> = emptyList(), val qr: List<QrResult> = emptyList(), val message: MessageResult? = null, val community: CommunityResult? = null)
    data class UrlResult(val url: String, val normalizedUrl: String, val component: ComponentResult, val domain: String? = null, val virusTotal: VirusTotalResult? = null, val community: CommunityResult? = null)
    data class ComponentResult(val score: Int, val verdict: String, val evidence: List<Evidence> = emptyList())
    data class Evidence(val feature: String, val score: Int, val reason: String)
    data class VirusTotalResult(val found: Boolean, val malicious: Int = 0, val suspicious: Int = 0, val harmless: Int = 0, val undetected: Int = 0)
    data class MessageResult(val component: ComponentResult, val extractedUrls: List<String> = emptyList(), val extractedUpiIds: List<String> = emptyList(), val community: CommunityResult? = null)
    data class QrResult(val decoded: Boolean, val rawValue: String? = null, val type: String? = null, val urlAnalysis: UrlResult? = null)
    data class CommunityResult(val reports: Int, val corroborated: Boolean, val categories: List<String> = emptyList())
    data class ReportResponse(val accepted: Boolean, val message: String)
    data class HealthResponse(val status: String, val database: String)
    data class CommunityReportsResponse(val posts: List<CommunityReport> = emptyList(), val total: Int = 0)
    data class CommunityReport(val id: Int = 0, val indicator: String = "", val type: String = "", val category: String = "", val description: String? = null, val createdAt: String = "")
    data class ApiException(val statusCode: Int, val errorCode: String?, override val message: String) : IllegalStateException(message)

    fun getBaseUrl(context: Context): String { cachedBaseUrl?.let { return it }; val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(BASE_URL_KEY, null)?.trim()?.trimEnd('/').orEmpty(); return (saved.ifBlank { DEFAULT_BASE_URL }).also { cachedBaseUrl = it } }
    fun setBaseUrl(context: Context, value: String) { val normalized = value.trim().trimEnd('/'); require(normalized.startsWith("http://") || normalized.startsWith("https://")); context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(BASE_URL_KEY, normalized).apply(); cachedBaseUrl = normalized }
    suspend fun health(context: Context): HealthResponse = withContext(Dispatchers.IO) { Gson().fromJson(request(context, "GET", "/health"), HealthResponse::class.java) }
    suspend fun getCommunityReports(context: Context): CommunityReportsResponse = withContext(Dispatchers.IO) { Gson().fromJson(request(context, "GET", "/api/v1/reports"), CommunityReportsResponse::class.java) }
    suspend fun analyzeUrl(context: Context, url: String): AnalyzeResponse = withContext(Dispatchers.IO) { val encoded = URLEncoder.encode(url, StandardCharsets.UTF_8.toString()); val result = Gson().fromJson(request(context, "GET", "/api/v1/analyze/url?url=$encoded"), UrlResult::class.java); AnalyzeResponse(result.component.verdict, result.component.score, result.component.evidence.map { it.reason }.ifEmpty { result.community?.categories ?: emptyList() }, if (result.component.verdict.equals("HIGH", true)) "Do not open or submit information on this URL." else "Verify the domain through an official channel before continuing.", urls = listOf(result), community = result.community) }
    suspend fun analyzeText(context: Context, text: String): AnalyzeResponse = withContext(Dispatchers.IO) { Gson().fromJson(request(context, "POST", "/api/v1/analyze/text", Gson().toJson(mapOf("text" to text)), "application/json"), AnalyzeResponse::class.java) }
    suspend fun analyze(bitmap: Bitmap, context: Context): AnalyzeResponse = withContext(Dispatchers.IO) { val bytes = java.io.ByteArrayOutputStream().use { output -> bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output); output.toByteArray() }; analyzeScreenshotBytes(context, bytes, "screenshot.jpg", "image/jpeg") }
    suspend fun analyzeScreenshot(context: Context, uri: Uri): AnalyzeResponse = withContext(Dispatchers.IO) { val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IllegalStateException("Could not read the selected image"); analyzeScreenshotBytes(context, bytes, "screenshot.jpg", "image/jpeg") }
    suspend fun report(context: Context, indicator: String, type: String, category: String, description: String?): ReportResponse = withContext(Dispatchers.IO) { val normalizedType = type.trim().uppercase(); require(normalizedType in setOf("URL", "MESSAGE", "QR", "UPI")); val requestJson = buildString { append('{'); append("\"indicator\":").append(Gson().toJson(indicator.trim())).append(','); append("\"type\":").append(Gson().toJson(normalizedType)).append(','); append("\"category\":").append(Gson().toJson(category.trim())); if (!description.isNullOrBlank()) append(',').append("\"description\":").append(Gson().toJson(description.trim())); append('}') }; Gson().fromJson(request(context, "POST", "/api/v1/reports", requestJson, "application/json", setOf(201)), ReportResponse::class.java) }
    private fun analyzeScreenshotBytes(context: Context, bytes: ByteArray, filename: String, mimeType: String): AnalyzeResponse { val boundary = "----ScamShieldBoundary${System.currentTimeMillis()}"; val connection = openConnection(context, "POST", "/api/v1/analyze/screenshot").apply { doOutput = true; setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary"); setRequestProperty("Accept", "application/json") }; try { connection.outputStream.use { out -> out.write("--$boundary\r\nContent-Disposition: form-data; name=\"screenshot\"; filename=\"$filename\"\r\nContent-Type: $mimeType\r\n\r\n".toByteArray(Charsets.UTF_8)); out.write(bytes); out.write("\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8)) }; return Gson().fromJson(readResponse(connection), AnalyzeResponse::class.java) } finally { connection.disconnect() } }
    private fun request(context: Context, method: String, path: String, body: String? = null, contentType: String? = null, expected: Set<Int> = (200..299).toSet()): String { val connection = openConnection(context, method, path).apply { if (body != null) { doOutput = true; if (contentType != null) setRequestProperty("Content-Type", contentType); setRequestProperty("Accept", "application/json") } }; try { if (body != null) connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }; val bodyText = readResponse(connection); if (connection.responseCode !in expected) throw parseApiException(connection.responseCode, bodyText); return bodyText } finally { connection.disconnect() } }
    private fun openConnection(context: Context, method: String, path: String): HttpURLConnection { return (java.net.URL(getBaseUrl(context) + path).openConnection() as HttpURLConnection).apply { requestMethod = method; connectTimeout = 15_000; readTimeout = 45_000; useCaches = false } }
    private fun readResponse(connection: HttpURLConnection): String { val status = connection.responseCode; val stream = if (status in 200..299) connection.inputStream else connection.errorStream; return stream?.use { BufferedReader(InputStreamReader(it)).readText() }.orEmpty() }
    private fun parseApiException(status: Int, body: String): ApiException { return runCatching { val json = Gson().fromJson(body, JsonObject::class.java); ApiException(status, json?.get("error")?.takeUnless { it.isJsonNull }?.asString, json?.get("message")?.takeUnless { it.isJsonNull }?.asString ?: "Backend returned HTTP $status") }.getOrElse { ApiException(status, null, if (body.isBlank()) "Backend returned HTTP $status" else body) } }
}
