package com.bitwiseoperators.scamshield.config

import io.ktor.server.config.*

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int
)

data class VirusTotalConfig(val apiKey: String)
data class OcrConfig(val enabled: Boolean, val command: String, val language: String)
data class UploadConfig(val maxBytes: Long)
data class SecurityConfig(val apiKey: String)

data class AppConfig(
    val database: DatabaseConfig,
    val virusTotal: VirusTotalConfig,
    val ocr: OcrConfig,
    val upload: UploadConfig,
    val security: SecurityConfig
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
            config.property("scamshield.ocr.enabled").getString().toBoolean(),
            config.property("scamshield.ocr.command").getString(),
            config.property("scamshield.ocr.language").getString()
        ),
        upload = UploadConfig(
            config.property("scamshield.upload.maxBytes").getString().toLong()
        ),
        security = SecurityConfig(
            config.property("scamshield.security.apiKey").getString()
        )
    )
}
