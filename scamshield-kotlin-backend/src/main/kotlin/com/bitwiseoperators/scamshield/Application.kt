package com.bitwiseoperators.scamshield

import com.bitwiseoperators.scamshield.config.AppConfig
import com.bitwiseoperators.scamshield.db.Database
import com.bitwiseoperators.scamshield.routes.configureRoutes
import com.bitwiseoperators.scamshield.services.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import io.ktor.server.response.respond

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = AppConfig(environment.config)
    val database = Database(config.database)
    database.migrate()

    val communityService = CommunityService(database)
    val urlService = UrlAnalysisService(config, communityService)
    val messageService = MessageAnalysisService(communityService)
    val qrService = QrDecoderService()
    val ocrService = OcrService(config)
    val fusionService = RiskFusionService()

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }

    install(CORS) {
        anyHost()
        allowHeader("X-API-Key")
        allowHeader("Content-Type")
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled request error", cause)
            call.respond(
                io.ktor.http.HttpStatusCode.InternalServerError,
                com.bitwiseoperators.scamshield.model.ErrorResponse(
                    error = "internal_error",
                    message = "The server could not complete the request."
                )
            )
        }
    }

    configureRoutes(
        config = config,
        qrService = qrService,
        ocrService = ocrService,
        urlService = urlService,
        messageService = messageService,
        communityService = communityService,
        fusionService = fusionService
    )
}
