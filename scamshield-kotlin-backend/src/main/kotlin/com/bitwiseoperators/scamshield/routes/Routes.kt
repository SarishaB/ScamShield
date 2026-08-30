package com.bitwiseoperators.scamshield.routes

import com.bitwiseoperators.scamshield.config.AppConfig
import com.bitwiseoperators.scamshield.model.*
import com.bitwiseoperators.scamshield.services.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File

fun Application.configureRoutes(
    config: AppConfig,
    qrService: QrDecoderService,
    ocrService: OcrService,
    urlService: UrlAnalysisService,
    messageService: MessageAnalysisService,
    communityService: CommunityService,
    fusionService: RiskFusionService
) {
    routing {

        // ---------------------------------------------------------
        // Health check
        // ---------------------------------------------------------

        get("/health") {
            call.respond(
                HealthResponse("ok","configured"
                )
            )
        }

        // ---------------------------------------------------------
        // URL analysis
        // ---------------------------------------------------------

        get("/api/v1/analyze/url") {
            requireApiKey(call, config)

            val raw = call.request.queryParameters["url"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        "missing_url",
                        "Query parameter 'url' is required."
                    )
                )

            call.respond(
                urlService.analyze(raw)
            )
        }

        // ---------------------------------------------------------
        // Community reports
        // ---------------------------------------------------------

        post("/api/v1/reports") {
            requireApiKey(call, config)

            val request = call.receive<ReportRequest>()

            runCatching {
                communityService.report(request)
            }.onFailure {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        "invalid_report",
                        it.message ?: "Invalid report."
                    )
                )
            }

            call.respond(
                HttpStatusCode.Created,
                ReportResponse(
                    true,
                    "Report accepted for community intelligence."
                )
            )
        }

        // ---------------------------------------------------------
        // Screenshot analysis
        // ---------------------------------------------------------

        post("/api/v1/analyze/screenshot") {
            requireApiKey(call, config)

            val tempFile = File.createTempFile(
                "scamshield-upload-",
                ".png"
            )

            try {

                var receivedBytes = 0L
                var originalFileName: String? = null

                val multipart = call.receiveMultipart(
                    formFieldLimit = config.upload.maxBytes
                )

                multipart.forEachPart { part ->

                    when (part) {

                        is PartData.FileItem -> {

                            if (
                                part.name == "screenshot" ||
                                part.name == "image"
                            ) {

                                originalFileName =
                                    part.originalFileName

                                // Copy uploaded file directly
                                // to our temporary file.
                                part.provider().copyAndClose(
                                    tempFile.writeChannel()
                                )

                                receivedBytes =
                                    tempFile.length()

                                if (
                                    receivedBytes >
                                    config.upload.maxBytes
                                ) {
                                    throw IllegalArgumentException(
                                        "Screenshot exceeds the maximum upload size."
                                    )
                                }
                            }
                        }

                        else -> Unit
                    }

                    part.dispose()
                }

                // -------------------------------------------------
                // Make sure an image was actually uploaded
                // -------------------------------------------------

                if (receivedBytes == 0L) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            "missing_screenshot",
                            "Send a multipart field named 'screenshot'."
                        )
                    )
                }

                // -------------------------------------------------
                // OCR
                // -------------------------------------------------

                val extractedText =
                    ocrService.extractText(tempFile)

                // -------------------------------------------------
                // Extract URLs from OCR text
                // -------------------------------------------------

                val extractedUrls =
                    extractUrls(extractedText)

                val urlResults =
                    extractedUrls
                        .distinct()
                        .take(10)
                        .map {
                            urlService.analyze(it)
                        }

                // -------------------------------------------------
                // QR code detection
                // -------------------------------------------------

                val qrRaw =
                    qrService.decode(tempFile)

                val qrResults =
                    if (qrRaw != null) {

                        val qrUrl =
                            extractFirstUrl(qrRaw)

                        listOf(
                            QrResult(
                                decoded = true,

                                rawValue =
                                    qrRaw.take(4000),

                                type = when {

                                    qrUrl != null ->
                                        "URL"

                                    qrRaw.startsWith(
                                        "upi://",
                                        ignoreCase = true
                                    ) ->
                                        "UPI"

                                    else ->
                                        "TEXT"
                                },

                                urlAnalysis =
                                    qrUrl?.let {
                                        urlService.analyze(it)
                                    }
                            )
                        )

                    } else {
                        emptyList()
                    }

                // -------------------------------------------------
                // SMS / message scam-intent analysis
                // -------------------------------------------------

                val message =
                    if (extractedText.isNotBlank()) {

                        messageService.analyze(
                            extractedText
                        )

                    } else {
                        null
                    }

                // -------------------------------------------------
                // Combine all detection signals
                // -------------------------------------------------

                val response =
                    fusionService.fuse(
                        extractedText = extractedText,
                        urls = urlResults,
                        qrResults = qrResults,
                        message = message
                    )

                // -------------------------------------------------
                // Return final analysis
                // -------------------------------------------------

                call.respond(response)

            } finally {

                // Always delete temporary screenshot.
                tempFile.delete()
            }
        }

        // ---------------------------------------------------------
        // Direct text analysis
        // ---------------------------------------------------------

        post("/api/v1/analyze/text") {

            requireApiKey(
                call,
                config
            )

            val request =
                call.receive<TextRequest>()

            val message =
                messageService.analyze(
                    request.text
                )

            val urls =
                message.extractedUrls
                    .distinct()
                    .take(10)
                    .map {
                        urlService.analyze(it)
                    }

            val response =
                fusionService.fuse(
                    request.text,
                    urls,
                    emptyList(),
                    message
                )

            call.respond(response)
        }
    }
}


// =============================================================
// REQUEST MODEL
// =============================================================

@kotlinx.serialization.Serializable
data class TextRequest(
    val text: String
)


// =============================================================
// API KEY VALIDATION
// =============================================================

private suspend fun requireApiKey(
    call: ApplicationCall,
    config: AppConfig
) {

    // If no API key is configured,
    // authentication is disabled.
    if (config.security.apiKey.isBlank()) {
        return
    }

    val providedKey =
        call.request.headers["X-API-Key"]

    if (providedKey != config.security.apiKey) {

        call.respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse(
                "unauthorized",
                "Missing or invalid API key."
            )
        )

        throw io.ktor.server.plugins.BadRequestException(
            "unauthorized"
        )
    }
}


// =============================================================
// URL EXTRACTION FROM OCR / TEXT
// =============================================================

private fun extractUrls(
    text: String
): List<String> {

    return Regex(
        """(?i)\b(?:(?:https?|www)\:\/\/)?(?:[a-z0-9-]+\.)+[a-z]{2,}(?:[\/?#][^\s<]*)?"""
    )
        .findAll(text)
        .map {
            it.value.trimEnd(
                '.',
                ',',
                '!',
                '?',
                ')',
                ']'
            )
        }
        .toList()
}


// =============================================================
// EXTRACT FIRST URL FROM QR CONTENT
// =============================================================

private fun extractFirstUrl(
    value: String
): String? {

    return Regex(
        """(?i)\b(?:https?|www)\:\/\/[^\s<]+"""
    )
        .find(value)
        ?.value
        ?: Regex(
            """(?i)\b(?:[a-z0-9-]+\.)+[a-z]{2,}(?:[\/?#][^\s<]*)?"""
        )
            .find(value)
            ?.value
}