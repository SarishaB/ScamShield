package com.bitwiseoperators.scamshield.services

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class QrDecoderService {
    fun decode(file: File): String? {
        val image: BufferedImage = ImageIO.read(file) ?: return null
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val hints = mapOf(
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
        )

        return runCatching {
            MultiFormatReader().decode(bitmap, hints).text
        }.getOrNull()
    }
}
