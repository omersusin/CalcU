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

    fun toUpper(text: String): String = text.uppercase()

    fun toLower(text: String): String = text.lowercase()

    fun titleCase(text: String): String {
        if (text.isBlank()) return text
        return text.trim().split("\\s+".toRegex()).joinToString(" ") { word ->
            if (word.isEmpty()) word
            else word.substring(0, 1).uppercase() + word.substring(1).lowercase()
        }
    }

    fun urlEncode(text: String): String =
        java.net.URLEncoder.encode(text, "UTF-8").replace("+", "%20")

    fun urlDecode(text: String): String =
        java.net.URLDecoder.decode(text, "UTF-8")

    private val morseMap: Map<Char, String> = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..",
        'E' to ".", 'F' to "..-.", 'G' to "--.", 'H' to "....",
        'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.",
        'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--",
        '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
        '8' to "---..", '9' to "----."
    )

    private val morseReverse: Map<String, Char> = morseMap.entries.associate { (k, v) -> v to k }

    fun morseEncode(text: String): String {
        if (text.isBlank()) return ""
        return text.trim().uppercase().split("\\s+".toRegex()).joinToString(" / ") { word ->
            word.map { ch ->
                morseMap[ch] ?: throw IllegalArgumentException("Cannot encode char: $ch")
            }.joinToString(" ")
        }
    }

    fun morseDecode(code: String): String {
        if (code.isBlank()) return ""
        return code.trim().split(" / ").joinToString(" ") { word ->
            word.trim().split("\\s+".toRegex()).map { token ->
                morseReverse[token] ?: throw IllegalArgumentException("Bad morse token: $token")
            }.joinToString("")
        }
    }

    fun textToBinary(text: String): String {
        if (text.isEmpty()) return ""
        return text.map { it.code.toString(2).padStart(8, '0') }.joinToString(" ")
    }

    fun binaryToText(bin: String): String {
        if (bin.isBlank()) return ""
        return bin.trim().split("\\s+".toRegex()).map { token ->
            try {
                token.toInt(2).toChar().toString()
            } catch (e: Exception) {
                throw IllegalArgumentException("Bad binary token: $token", e)
            }
        }.joinToString("")
    }

    fun textToHex(text: String): String {
        if (text.isEmpty()) return ""
        return text.map { "%02x".format(it.code) }.joinToString("")
    }

    fun jsonPretty(json: String): String {
        val sb = StringBuilder()
        var indent = 0
        var inString = false
        var escape = false
        val stack = ArrayDeque<Char>()
        fun appendIndent() {
            repeat(indent * 2) { sb.append(' ') }
        }
        fun trimTrailingWs() {
            while (sb.isNotEmpty() && (sb.last() == ' ' || sb.last() == '\n')) {
                sb.deleteCharAt(sb.length - 1)
            }
        }
        for (c in json) {
            if (inString) {
                sb.append(c)
                if (escape) escape = false
                else if (c == '\\') escape = true
                else if (c == '"') inString = false
                continue
            }
            when (c) {
                '"' -> { inString = true; sb.append(c) }
                '{', '[' -> {
                    stack.addLast(c); sb.append(c); indent++
                    sb.append('\n'); appendIndent()
                }
                '}', ']' -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Unbalanced JSON: extra '$c'")
                    val open = stack.removeLast()
                    if ((c == '}' && open != '{') || (c == ']' && open != '[')) {
                        throw IllegalArgumentException("Unbalanced JSON: mismatched '$open' and '$c'")
                    }
                    indent--
                    if (indent < 0) throw IllegalArgumentException("Unbalanced JSON")
                    trimTrailingWs()
                    sb.append('\n'); appendIndent(); sb.append(c)
                }
                ':' -> sb.append(": ")
                ',' -> { sb.append(','); sb.append('\n'); appendIndent() }
                ' ', '\n', '\r', '\t' -> Unit
                else -> sb.append(c)
            }
        }
        if (inString) throw IllegalArgumentException("Unbalanced JSON: unterminated string")
        if (stack.isNotEmpty()) throw IllegalArgumentException("Unbalanced JSON: unclosed bracket")
        return sb.toString()
    }

    fun regexTest(pattern: String, input: String): List<String> {
        try {
            return Regex(pattern).findAll(input).map { it.value }.toList()
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun unixNow(): Long = System.currentTimeMillis() / 1000

    fun unixToDate(ts: Long): String {
        val instant = java.time.Instant.ofEpochSecond(ts)
        val zone = java.time.ZoneId.systemDefault()
        val dt = java.time.LocalDateTime.ofInstant(instant, zone)
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}
