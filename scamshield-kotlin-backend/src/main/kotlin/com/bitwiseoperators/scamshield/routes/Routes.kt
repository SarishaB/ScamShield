package com.bitwiseoperators.scamshield.routes

import com.bitwiseoperators.scamshield.config.AppConfig
import com.bitwiseoperators.scamshield.model.*
import com.bitwiseoperators.scamshield.services.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import java.io.File
import java.util.UUID

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
        get("/health") {
            call.respond(HealthResponse("ok", "configured"))
        }

        get("/api/v1/analyze/url") {
            requireApiKey(call, config)
            val raw = call.request.queryParameters["url"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("missing_url", "Query parameter 'url' is required.")
                )

            call.respond(urlService.analyze(raw))
        }

        post("/api/v1/reports") {
            requireApiKey(call, config)
            val request = call.receive<ReportRequest>()
            runCatching { communityService.report(request) }
                .onFailure {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_report", it.message ?: "Invalid report.")
                    )
                }

            call.respond(HttpStatusCode.Created, ReportResponse(true, "Report accepted for community intelligence."))
        }

        post("/api/v1/analyze/screenshot") {
            requireApiKey(call, config)

            val tempFile = File.createTempFile("scamshield-upload-", ".png")
            try {
                var receivedBytes = 0L
                var originalFileName: String? = null

                val multipart = call.receiveMultipart(
                    formFieldLimit = config.upload.maxBytes
                )

                multipart.forEachPart { part ->
                    when (part) {
                        is io.ktor.http.content.PartData.FileItem -> {
                            if (part.name == "screenshot" || part.name == "image") {
                                originalFileName = part.originalFileName
                                val channel = part.provider()
                                tempFile.outputStream().use { out ->
                                    val buffer = ByteArray(8192)
                                    while (true) {
                                        val n = channel.readAvailable(buffer)
                                        if (n <= 0) break
                                        receivedBytes += n
                                        if (receivedBytes > config.upload.maxBytes) {
                                            throw IllegalArgumentException("Screenshot exceeds the maximum upload size.")
                                        }
                                        out.write(buffer, 0, n)
                                    }
                                }
                            }
                        }
                        else -> Unit
                    }
                    part.dispose()
                }

                if (receivedBytes == 0L) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_screenshot", "Send a multipart field named 'screenshot'.")
                    )
                }

                val extractedText = ocrService.extractText(tempFile)
                val extractedUrls = extractUrls(extractedText)

                val urlResults = extractedUrls
                    .distinct()
                    .take(10)
                    .map { urlService.analyze(it) }

                val qrRaw = qrService.decode(tempFile)
                val qrResults = if (qrRaw != null) {
                    val qrUrl = extractFirstUrl(qrRaw)
                    listOf(
                        QrResult(
                            decoded = true,
                            rawValue = qrRaw.take(4000),
                            type = when {
                                qrUrl != null -> "URL"
                                qrRaw.startsWith("upi://", true) -> "UPI"
                                else -> "TEXT"
                            },
                            urlAnalysis = qrUrl?.let(urlService::analyze)
                        )
                    )
                } else {
                    emptyList()
                }

                val message = if (extractedText.isNotBlank()) {
                    messageService.analyze(extractedText)
                } else null

                val response = fusionService.fuse(
                    extractedText = extractedText,
                    urls = urlResults,
                    qrResults = qrResults,
                    message = message
                )

                call.respond(response)
            } finally {
                tempFile.delete()
            }
        }

        post("/api/v1/analyze/text") {
            requireApiKey(call, config)
            val request = call.receive<TextRequest>()
            val message = messageService.analyze(request.text)
            val urls = message.extractedUrls.distinct().take(10).map(urlService::analyze)
            val response = fusionService.fuse(request.text, urls, emptyList(), message)
            call.respond(response)
        }
    }
}

@kotlinx.serialization.Serializable
data class TextRequest(val text: String)

private suspend fun requireApiKey(call: ApplicationCall, config: AppConfig) {
    if (config.security.apiKey.isBlank()) return
    if (call.request.headers["X-API-Key"] != config.security.apiKey) {
        call.respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse("unauthorized", "Missing or invalid API key.")
        )
        throw io.ktor.server.plugins.BadRequestException("unauthorized")
    }
}

private fun extractUrls(text: String): List<String> =
    Regex("""(?i)\b(?:(?:https?|www)\:\/\/)?(?:[a-z0-9-]+\.)+[a-z]{2,}(?:[\/?#][^\s<]*)?""")
        .findAll(text)
        .map { it.value.trimEnd('.', ',', '!', '?', ')', ']') }
        .toList()

private fun extractFirstUrl(value: String): String? =
    Regex("""(?i)\b(?:https?|www)\:\/\/[^\s<]+""").find(value)?.value
        ?: Regex("""(?i)\b(?:[a-z0-9-]+\.)+[a-z]{2,}(?:[\/?#][^\s<]*)?""")
            .find(value)?.value
