package com.akslabs.circletosearch.data

import android.graphics.Bitmap
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** Client for the scam-detection backend. Replace API_URL with your HTTPS endpoint. */
object ScamDetectionApi {
    const val API_URL = "https://YOUR-BACKEND.example.com/api/v1/scam-detection"

    data class Result(
        val verdict: String,
        val score: Float?,
        val explanation: String?,
        val indicators: List<String>
    )

    suspend fun analyze(bitmap: Bitmap): Result = withContext(Dispatchers.IO) {
        val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val imageBytes = java.io.ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
                output.toByteArray()
            }
            val payload = JsonObject().apply {
                addProperty("image_base64", Base64.encodeToString(imageBytes, Base64.NO_WRAP))
            }.toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { BufferedReader(InputStreamReader(it)).readText() }.orEmpty()
            if (status !in 200..299) {
                throw IllegalStateException(if (body.isBlank()) "Backend returned HTTP $status" else "Backend returned HTTP $status: $body")
            }
            parseResult(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseResult(body: String): Result {
        val json = Gson().fromJson(body, JsonObject::class.java)
            ?: throw IllegalStateException("Backend returned an empty response")
        val verdict = json.get("verdict")?.asString ?: json.get("result")?.asString ?: "UNKNOWN"
        val score = json.get("score")?.let { if (it.isJsonNull) null else it.asFloat }
        val explanation = json.get("explanation")?.let { if (it.isJsonNull) null else it.asString }
            ?: json.get("reason")?.let { if (it.isJsonNull) null else it.asString }
        val indicators = json.getAsJsonArray("indicators")?.mapNotNull {
            if (it.isJsonNull) null else it.asString
        } ?: emptyList()
        return Result(verdict, score, explanation, indicators)
    }
}
