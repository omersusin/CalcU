package calc.u.core

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Totp {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun base32Decode(secret: String): ByteArray {
        val cleaned = secret.filter { it != ' ' && it != '\t' && it != '\n' && it != '\r' && it != '-' }
            .uppercase()
        if (cleaned.isEmpty()) throw IllegalArgumentException("Empty secret")
        var s = cleaned
        while (s.endsWith("=")) s = s.dropLast(1)
        if (s.isEmpty()) throw IllegalArgumentException("Empty secret")
        val out = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in s) {
            val v = ALPHABET.indexOf(c)
            if (v < 0) throw IllegalArgumentException("Bad base32 char: $c")
            buffer = (buffer shl 5) or v
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                out.add(((buffer shr bitsLeft) and 0xFF).toByte())
            }
        }
        return out.toByteArray()
    }

    fun hotp(key: ByteArray, counter: Long, digits: Int = 6): String {
        val msg = ByteArray(8)
        var c = counter
        for (i in 7 downTo 0) {
            msg[i] = (c and 0xFF).toByte()
            c = c ushr 8
        }
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hmac = mac.doFinal(msg)
        val offset = hmac[hmac.size - 1].toInt() and 0x0F
        val binary = ((hmac[offset].toInt() and 0x7F) shl 24) or
            ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
            ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
            (hmac[offset + 3].toInt() and 0xFF)
        var mod = 1
        repeat(digits) { mod *= 10 }
        return (binary % mod).toString().padStart(digits, '0')
    }

    fun totp(secret: String, timeSec: Long, period: Long = 30, digits: Int = 6): String {
        val key = base32Decode(secret)
        val counter = Math.floorDiv(timeSec, period)
        return hotp(key, counter, digits)
    }

    fun secondsRemaining(timeSec: Long, period: Long = 30): Long =
        period - Math.floorMod(timeSec, period)
}
