package com.bitwiseoperators.scamshield.services

import com.bitwiseoperators.scamshield.config.AppConfig
import java.io.File
import java.util.concurrent.TimeUnit

class OcrService(private val config: AppConfig) {
    fun extractText(image: File): String {
        if (!config.ocr.enabled) return ""

        val outputBase = File.createTempFile("scamshield-ocr-", "")
        outputBase.delete()

        return try {
            val process = ProcessBuilder(
                config.ocr.command,
                image.absolutePath,
                outputBase.absolutePath,
                "-l",
                config.ocr.language
            )
                .redirectErrorStream(true)
                .start()

            val finished = process.waitFor(30, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return ""
            }

            val textFile = File("${outputBase.absolutePath}.txt")
            if (!textFile.exists()) return ""

            textFile.readText(Charsets.UTF_8).take(30000)
        } catch (_: Exception) {
            ""
        } finally {
            File("${outputBase.absolutePath}.txt").delete()
            outputBase.delete()
        }
    }
}
