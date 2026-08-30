package com.bitwiseoperators.scamshield.config

import io.ktor.server.config.*

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int
)

data class VirusTotalConfig(val apiKey: String)
data class OcrConfig(
    val enabled: Boolean,
    val apiKey: String,
    val baseUrl: String,
    val language: String,
    val engine: Int
)
data class UploadConfig(val maxBytes: Long)
data class SecurityConfig(val apiKey: String)

data class GeminiConfig(
    val apiKey: String,
    val model: String,
    val baseUrl: String
)

data class AppConfig(
    val database: DatabaseConfig,
    val virusTotal: VirusTotalConfig,
    val ocr: OcrConfig,
    val upload: UploadConfig,
    val security: SecurityConfig,
    val gemini: GeminiConfig
) {
    constructor(config: ApplicationConfig) : this(
        database = DatabaseConfig(
            jdbcUrl = config.property("scamshield.database.jdbcUrl").getString(),
            username = config.property("scamshield.database.username").getString(),
            password = config.property("scamshield.database.password").getString(),
            maximumPoolSize = config.property("scamshield.database.maximumPoolSize").getString().toInt()
        ),
        virusTotal = VirusTotalConfig(
            config.property("scamshield.virustotal.apiKey").getString()
        ),
        ocr = OcrConfig(
            enabled = config.property("scamshield.ocr.enabled").getString().toBoolean(),
            apiKey = config.property("scamshield.ocr.apiKey").getString(),
            baseUrl = config.property("scamshield.ocr.baseUrl").getString(),
            language = config.property("scamshield.ocr.language").getString(),
            engine = config.property("scamshield.ocr.engine").getString().toInt()
        ),
        upload = UploadConfig(
            config.property("scamshield.upload.maxBytes").getString().toLong()
        ),
        security = SecurityConfig(
            config.property("scamshield.security.apiKey").getString()
        ),
        gemini = GeminiConfig(
            apiKey = config.property("scamshield.gemini.apiKey").getString(),
            model = config.property("scamshield.gemini.model").getString(),
            baseUrl = config.property("scamshield.gemini.baseUrl").getString()
        )
    )
}
