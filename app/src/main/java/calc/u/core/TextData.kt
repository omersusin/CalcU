package calc.u.core

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID

object TextData {
    fun sha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun md5(text: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun base64Encode(text: String): String =
        Base64.getEncoder().encodeToString(text.toByteArray(Charsets.UTF_8))

    fun base64Decode(text: String): String =
        Base64.getDecoder().decode(text).toString(Charsets.UTF_8)

    fun wordCount(text: String): Int =
        if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size

    fun charCount(text: String): Int = text.length

    fun lineCount(text: String): Int =
        if (text.isEmpty()) 0 else text.split("\n").size

    fun counts(text: String): Triple<Int, Int, Int> =
        Triple(wordCount(text), charCount(text), lineCount(text))

    fun uuid(): String = UUID.randomUUID().toString()

    fun qrMatrix(text: String, size: Int): BitMatrix {
        try {
            return QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        } catch (e: WriterException) {
            throw IllegalArgumentException(e.message, e)
        }
    }
}
