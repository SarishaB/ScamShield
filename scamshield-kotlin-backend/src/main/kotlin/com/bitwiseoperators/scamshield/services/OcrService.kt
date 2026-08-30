package com.bitwiseoperators.scamshield.services

import com.bitwiseoperators.scamshield.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * OCR.Space-backed OCR service.
 *
 * No local OCR binary is launched. The uploaded image is sent directly to
 * OCR.Space's /parse/image endpoint using its base64Image form field.
 */
class OcrService(private val config: AppConfig) {
    private val httpClient = HttpClient(CIO) {
        expectSuccess = false
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    suspend fun extractText(image: File): String {
        println("[ScamShield][OCR] Starting OCR.Space request: ${image.absolutePath} (${image.length()} bytes)")

        if (!config.ocr.enabled) {
            println("[ScamShield][OCR] OCR is disabled (OCR_ENABLED=false).")
            return ""
        }

        if (!image.exists() || image.length() == 0L) {
            println("[ScamShield][OCR] ERROR: image file is missing or empty.")
            return ""
        }

        if (config.ocr.apiKey.isBlank()) {
            println("[ScamShield][OCR] ERROR: OCR.Space API key is not configured. Set OCR_SPACE_API_KEY.")
            return ""
        }

        return try {
            val contentType = detectContentType(image)
            val fileBytes = image.readBytes()

            // OCR.Space also accepts the image as a base64Image form field.
            // Using this avoids Ktor multipart encoding differences and guarantees
            // that OCR.Space receives actual image content.
            val mime = contentType.toString()
            val base64Image = java.util.Base64.getEncoder().encodeToString(fileBytes)
            val dataUri = "data:$mime;base64,$base64Image"

            val response = httpClient.post(config.ocr.baseUrl) {
                header("apikey", config.ocr.apiKey)
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("language", config.ocr.language)
                            append("isOverlayRequired", "false")
                            append("detectOrientation", "true")
                            append("OCREngine", config.ocr.engine.toString())
                            append("base64Image", dataUri)
                        }
                    )
                )
            }

            val body = response.bodyAsText()

            println("[ScamShield][OCR] OCR.Space HTTP ${response.status.value}")

            if (!response.status.value.let { it in 200..299 }) {
                println("[ScamShield][OCR] ERROR: OCR.Space returned HTTP ${response.status.value}.")
                println("[ScamShield][OCR] Response: ${body.take(4000)}")
                return ""
            }

            val root = json.parseToJsonElement(body).jsonObject

            val isErrored = root["IsErroredOnProcessing"]
                ?.jsonPrimitive
                ?.content
                ?.toBooleanStrictOrNull()
                ?: false

            if (isErrored) {
                val details = root["ErrorMessage"]?.toString()
                    ?: root["ErrorDetails"]?.toString()
                    ?: "Unknown OCR.Space processing error"
                println("[ScamShield][OCR] ERROR: OCR.Space processing failed: ${details.take(4000)}")
                return ""
            }

            val extractedText = buildString {
                root["ParsedResults"]
                    ?.jsonArray
                    ?.forEach { result ->
                        val text = result.jsonObject["ParsedText"]
                            ?.jsonPrimitive
                            ?.contentOrNull
                            .orEmpty()
                        if (text.isNotBlank()) {
                            if (isNotEmpty()) append('\n')
                            append(text)
                        }
                    }
            }
                .replace("\u0000", "")
                .trim()
                .take(30000)

            println("\n========== SCAMSHIELD OCR OUTPUT ==========")
            if (extractedText.isBlank()) {
                println("(OCR.Space detected no text)")
            } else {
                println(extractedText)
            }
            println("========== END OCR OUTPUT ==========")
            println("[ScamShield][OCR] Extracted ${extractedText.length} characters via OCR.Space.")

            extractedText
        } catch (e: Exception) {
            println("[ScamShield][OCR] ERROR: ${e::class.simpleName}: ${e.message}")
            ""
        }
    }

    private fun detectContentType(image: File): ContentType {
        val header = image.inputStream().use { input ->
            ByteArray(16).also { input.read(it) }
        }

        return when {
            header.size >= 8 &&
                header[0] == 0x89.toByte() && header[1] == 0x50.toByte() &&
                header[2] == 0x4E.toByte() && header[3] == 0x47.toByte() ->
                ContentType.Image.PNG

            header.size >= 3 &&
                header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() &&
                header[2] == 0xFF.toByte() ->
                ContentType.Image.JPEG

            header.size >= 6 &&
                header.copyOfRange(0, 6).contentEquals(byteArrayOf(
                    'G'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(),
                    '8'.code.toByte(), '9'.code.toByte(), 'a'.code.toByte()
                )) ->
                ContentType.Image.GIF

            header.size >= 2 &&
                header[0] == 'B'.code.toByte() && header[1] == 'M'.code.toByte() ->
                ContentType.parse("image/bmp")

            header.size >= 4 && (
                header.copyOfRange(0, 4).contentEquals(byteArrayOf('I'.code.toByte(), 'I'.code.toByte(), 42, 0)) ||
                header.copyOfRange(0, 4).contentEquals(byteArrayOf('M'.code.toByte(), 'M'.code.toByte(), 0, 42))
            ) ->
                ContentType.parse("image/tiff")

            else -> ContentType.Application.OctetStream
        }
    }

    // Prevent a dangling CIO client when the service is used in tests or shutdown hooks.
    fun close() {
        httpClient.close()
    }
}
