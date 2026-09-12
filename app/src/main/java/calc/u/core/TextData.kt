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
        try {
            val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        } catch (e: java.security.NoSuchAlgorithmException) {
            throw IllegalArgumentException("SHA-256 unavailable", e)
        }
    }

    fun md5(text: String): String {
        try {
            val digest = MessageDigest.getInstance("MD5").digest(text.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        } catch (e: java.security.NoSuchAlgorithmException) {
            throw IllegalArgumentException("MD5 unavailable", e)
        }
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
        require(size > 0) { "size must be > 0" }
        require(size <= 2000) { "size must be <= 2000" }
        require(text.isNotEmpty()) { "text must not be empty" }
        require(text.length <= 5000) { "text too long for QR" }
        try {
            return QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        } catch (e: WriterException) {
            throw IllegalArgumentException(e.message, e)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
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

    fun urlEncode(text: String): String {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8").replace("+", "%20")
        } catch (e: java.io.UnsupportedEncodingException) {
            throw IllegalArgumentException("UTF-8 unavailable", e)
        }
    }

    fun urlDecode(text: String): String {
        try {
            return java.net.URLDecoder.decode(text, "UTF-8")
        } catch (e: java.io.UnsupportedEncodingException) {
            throw IllegalArgumentException("UTF-8 unavailable", e)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

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
        try {
            val instant = java.time.Instant.ofEpochSecond(ts)
            val zone = java.time.ZoneId.systemDefault()
            val dt = java.time.LocalDateTime.ofInstant(instant, zone)
            return dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } catch (e: java.time.DateTimeException) {
            throw IllegalArgumentException("timestamp out of range: $ts")
        } catch (e: ArithmeticException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun caesar(text: String, shift: Int, encrypt: Boolean): String {
        val s = ((if (encrypt) shift else -shift) % 26 + 26) % 26
        return text.map { c ->
            when (c) {
                in 'A'..'Z' -> 'A' + (c - 'A' + s) % 26
                in 'a'..'z' -> 'a' + (c - 'a' + s) % 26
                else -> c
            }
        }.joinToString("")
    }

    fun xorHex(text: String, key: String): String {
        require(key.isNotEmpty()) { "Key must not be empty" }
        val t = text.toByteArray(Charsets.UTF_8)
        val k = key.toByteArray(Charsets.UTF_8)
        return t.mapIndexed { i, b ->
            "%02x".format((b.toInt() xor k[i % k.size].toInt()) and 0xFF)
        }.joinToString("")
    }

    // Text+Data II (ideas after omni-tools, MIT; own implementation below).
    // Attribution note: README/Settings should credit "text tools after omni-tools (MIT)".

    fun rot13(text: String): String {
        return text.map { c ->
            when (c) {
                in 'A'..'Z' -> 'A' + (c - 'A' + 13) % 26
                in 'a'..'z' -> 'a' + (c - 'a' + 13) % 26
                else -> c
            }
        }.joinToString("")
    }

    fun slugify(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    fun isPalindrome(text: String): Boolean {
        val f = text.lowercase().filter { it.isLetterOrDigit() }
        return f == f.reversed()
    }

    private val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")

    fun extractEmails(text: String): List<String> =
        emailRegex.findAll(text).map { it.value }.toList()

    fun diffLines(a: String, b: String): List<String> {
        val x = if (a.isEmpty()) emptyList() else a.split("\n").map { it.trimEnd('\r') }
        val y = if (b.isEmpty()) emptyList() else b.split("\n").map { it.trimEnd('\r') }
        val n = x.size
        val m = y.size
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                dp[i][j] = if (x[i] == y[j]) 1 + dp[i + 1][j + 1] else maxOf(dp[i + 1][j], dp[i][j + 1])
            }
        }
        val out = ArrayList<String>(n + m)
        var i = 0
        var j = 0
        while (i < n || j < m) {
            if (i < n && j < m && x[i] == y[j]) {
                out.add("  " + x[i])
                i++
                j++
            } else if (j < m && (i == n || dp[i][j + 1] >= dp[i + 1][j])) {
                out.add("+ " + y[j])
                j++
            } else {
                out.add("- " + x[i])
                i++
            }
        }
        return out
    }

    private fun splitCsvLine(line: String): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var k = 0
        while (k < line.length) {
            val c = line[k]
            if (inQuotes) {
                if (c == '"') {
                    if (k + 1 < line.length && line[k + 1] == '"') {
                        sb.append('"')
                        k += 2
                    } else {
                        inQuotes = false
                        k++
                    }
                } else {
                    sb.append(c)
                    k++
                }
            } else {
                when (c) {
                    '"' -> { inQuotes = true; k++ }
                    ',' -> { out.add(sb.toString()); sb.setLength(0); k++ }
                    else -> { sb.append(c); k++ }
                }
            }
        }
        if (inQuotes) throw IllegalArgumentException("Unterminated quote in CSV")
        out.add(sb.toString())
        return out
    }

    private fun csvEscape(field: String): String {
        return if (field.contains(',') || field.contains('"') || field.contains('\n') || field.contains('\r')) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
    }

    private fun jsonEscape(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                else -> if (c < ' ') sb.append("\\u%04x".format(c.code)) else sb.append(c)
            }
        }
        return sb.toString()
    }

    fun csvToJson(csv: String): String {
        val lines = csv.split("\n").map { it.trimEnd('\r') }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return "[]"
        val headers = splitCsvLine(lines[0])
        val rows = lines.drop(1).map { line ->
            val vals = splitCsvLine(line)
            val entries = headers.indices.joinToString(",") { hi ->
                val v = if (hi < vals.size) vals[hi] else ""
                "\"${jsonEscape(headers[hi])}\":\"${jsonEscape(v)}\""
            }
            "{$entries}"
        }
        return "[" + rows.joinToString(",") + "]"
    }

    private data class JsonNum(val raw: String)

    private fun skipJsonWs(s: String, pos: IntArray) {
        while (pos[0] < s.length && s[pos[0]].isWhitespace()) pos[0]++
    }

    private fun parseJsonString(s: String, pos: IntArray): String {
        if (pos[0] >= s.length || s[pos[0]] != '"') throw IllegalArgumentException("Expected string")
        pos[0]++
        val sb = StringBuilder()
        while (true) {
            if (pos[0] >= s.length) throw IllegalArgumentException("Unterminated string")
            val c = s[pos[0]++]
            when (c) {
                '"' -> return sb.toString()
                '\\' -> {
                    if (pos[0] >= s.length) throw IllegalArgumentException("Bad escape")
                    when (val e = s[pos[0]++]) {
                        '"', '\\', '/' -> sb.append(e)
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (pos[0] + 4 > s.length) throw IllegalArgumentException("Bad unicode escape")
                            val hex = s.substring(pos[0], pos[0] + 4)
                            val code = hex.toIntOrNull(16) ?: throw IllegalArgumentException("Bad unicode escape")
                            sb.append(code.toChar())
                            pos[0] += 4
                        }
                        else -> throw IllegalArgumentException("Bad escape: $e")
                    }
                }
                else -> sb.append(c)
            }
        }
    }

    private fun parseJsonValue(s: String, pos: IntArray): Any? {
        skipJsonWs(s, pos)
        if (pos[0] >= s.length) throw IllegalArgumentException("Unexpected end of JSON")
        return when (val c = s[pos[0]]) {
            '{' -> parseJsonObject(s, pos)
            '[' -> parseJsonArray(s, pos)
            '"' -> parseJsonString(s, pos)
            't' -> {
                if (!s.startsWith("true", pos[0])) throw IllegalArgumentException("Bad token")
                pos[0] += 4
                true
            }
            'f' -> {
                if (!s.startsWith("false", pos[0])) throw IllegalArgumentException("Bad token")
                pos[0] += 5
                false
            }
            'n' -> {
                if (!s.startsWith("null", pos[0])) throw IllegalArgumentException("Bad token")
                pos[0] += 4
                null
            }
            else -> {
                val start = pos[0]
                while (pos[0] < s.length && s[pos[0]] in "-+0123456789.eE") pos[0]++
                if (start == pos[0]) throw IllegalArgumentException("Unexpected char: $c")
                JsonNum(s.substring(start, pos[0]))
            }
        }
    }

    private fun parseJsonObject(s: String, pos: IntArray): LinkedHashMap<String, Any?> {
        if (s[pos[0]] != '{') throw IllegalArgumentException("Expected {")
        pos[0]++
        val map = LinkedHashMap<String, Any?>()
        skipJsonWs(s, pos)
        if (pos[0] < s.length && s[pos[0]] == '}') {
            pos[0]++
            return map
        }
        while (true) {
            skipJsonWs(s, pos)
            val key = parseJsonString(s, pos)
            skipJsonWs(s, pos)
            if (pos[0] >= s.length || s[pos[0]] != ':') throw IllegalArgumentException("Expected :")
            pos[0]++
            val v = parseJsonValue(s, pos)
            map[key] = v
            skipJsonWs(s, pos)
            if (pos[0] >= s.length) throw IllegalArgumentException("Unterminated object")
            when (s[pos[0]++]) {
                ',' -> continue
                '}' -> return map
                else -> throw IllegalArgumentException("Expected , or }")
            }
        }
    }

    private fun parseJsonArray(s: String, pos: IntArray): ArrayList<Any?> {
        if (s[pos[0]] != '[') throw IllegalArgumentException("Expected [")
        pos[0]++
        val list = ArrayList<Any?>()
        skipJsonWs(s, pos)
        if (pos[0] < s.length && s[pos[0]] == ']') {
            pos[0]++
            return list
        }
        while (true) {
            list.add(parseJsonValue(s, pos))
            skipJsonWs(s, pos)
            if (pos[0] >= s.length) throw IllegalArgumentException("Unterminated array")
            when (s[pos[0]++]) {
                ',' -> continue
                ']' -> return list
                else -> throw IllegalArgumentException("Expected , or ]")
            }
        }
    }

    private fun parseJson(text: String): Any? {
        val pos = intArrayOf(0)
        val v = parseJsonValue(text, pos)
        skipJsonWs(text, pos)
        if (pos[0] != text.length) throw IllegalArgumentException("Trailing characters in JSON")
        return v
    }

    private fun scalarToCsvCell(v: Any?): String {
        return when (v) {
            null -> ""
            is String -> v
            is Boolean -> v.toString()
            is JsonNum -> v.raw
            else -> throw IllegalArgumentException("Only flat objects supported")
        }
    }

    fun jsonToCsv(json: String): String {
        val t = json.trim()
        if (t.isEmpty()) throw IllegalArgumentException("Empty JSON")
        val v = parseJson(t)
        if (v !is List<*>) throw IllegalArgumentException("Expected JSON array")
        if (v.isEmpty()) return ""
        val headers = LinkedHashSet<String>()
        for (el in v) {
            if (el !is Map<*, *>) throw IllegalArgumentException("Expected array of objects")
            for (k in el.keys) {
                if (k !is String) throw IllegalArgumentException("Bad key")
                headers.add(k)
            }
        }
        val lines = ArrayList<String>(v.size + 1)
        lines.add(headers.joinToString(",") { csvEscape(it) })
        for (el in v) {
            @Suppress("UNCHECKED_CAST")
            val map = el as Map<String, Any?>
            lines.add(headers.joinToString(",") { h -> csvEscape(scalarToCsvCell(map[h])) })
        }
        return lines.joinToString("\n")
    }

    fun transposeCsv(csv: String): String {
        if (csv.isBlank()) return ""
        val lines = csv.split("\n").map { it.trimEnd('\r') }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return ""
        val grid = lines.map { splitCsvLine(it) }
        val cols = grid.maxOf { it.size }
        if (cols == 0) return ""
        val out = ArrayList<String>(cols)
        for (c in 0 until cols) {
            out.add(grid.indices.joinToString(",") { r -> csvEscape(grid[r].getOrElse(c) { "" }) })
        }
        return out.joinToString("\n")
    }

    /**
     * Sorts top-level keys of a single JSON object alphabetically.
     * Only the top level is sorted; nested values are kept verbatim.
     */
    fun jsonSortKeys(json: String): String {
        val t = json.trim()
        if (!t.startsWith("{") || !t.endsWith("}")) throw IllegalArgumentException("Expected JSON object")
        val inner = t.substring(1, t.length - 1)
        if (inner.isBlank()) return "{}"
        val parts = splitTopLevel(inner)
        val pairs = parts.map { part ->
            val idx = topLevelColon(part)
            if (idx < 0) throw IllegalArgumentException("Bad object entry")
            val keyRaw = part.substring(0, idx).trim()
            val valRaw = part.substring(idx + 1).trim()
            if (valRaw.isEmpty()) throw IllegalArgumentException("Bad object entry")
            val pos = intArrayOf(0)
            val key = parseJsonString(keyRaw, pos)
            skipJsonWs(keyRaw, pos)
            if (pos[0] != keyRaw.length) throw IllegalArgumentException("Bad key")
            key to valRaw
        }
        return "{" + pairs.sortedBy { it.first }.joinToString(",") { (k, v) -> "\"${jsonEscape(k)}\":$v" } + "}"
    }

    private fun splitTopLevel(s: String): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inString = false
        var escape = false
        var depthObj = 0
        var depthArr = 0
        for (c in s) {
            if (inString) {
                sb.append(c)
                if (escape) escape = false
                else if (c == '\\') escape = true
                else if (c == '"') inString = false
                continue
            }
            when (c) {
                '"' -> { inString = true; sb.append(c) }
                '{' -> { depthObj++; sb.append(c) }
                '}' -> { depthObj--; sb.append(c) }
                '[' -> { depthArr++; sb.append(c) }
                ']' -> { depthArr--; sb.append(c) }
                ',' -> if (depthObj == 0 && depthArr == 0) {
                    out.add(sb.toString())
                    sb.setLength(0)
                } else {
                    sb.append(c)
                }
                else -> sb.append(c)
            }
        }
        if (inString) throw IllegalArgumentException("Unterminated string")
        out.add(sb.toString())
        return out
    }

    private fun topLevelColon(s: String): Int {
        var inString = false
        var escape = false
        var depthObj = 0
        var depthArr = 0
        for (i in s.indices) {
            val c = s[i]
            if (inString) {
                if (escape) escape = false
                else if (c == '\\') escape = true
                else if (c == '"') inString = false
                continue
            }
            when (c) {
                '"' -> inString = true
                '{' -> depthObj++
                '}' -> depthObj--
                '[' -> depthArr++
                ']' -> depthArr--
                ':' -> if (depthObj == 0 && depthArr == 0) return i
            }
        }
        return -1
    }

    private fun flattenJson(v: Any?, path: String, out: MutableMap<String, String>) {
        when (v) {
            is Map<*, *> -> {
                if (v.isEmpty()) {
                    out[path] = "{}"
                } else {
                    for ((k, vv) in v) {
                        val np = if (path.isEmpty()) k.toString() else "$path.${k}"
                        flattenJson(vv, np, out)
                    }
                }
            }
            is List<*> -> {
                if (v.isEmpty()) {
                    out[path] = "[]"
                } else {
                    for (i in v.indices) flattenJson(v[i], "$path[$i]", out)
                }
            }
            null -> out[path] = "null"
            is String -> out[path] = "\"${jsonEscape(v)}\""
            is Boolean -> out[path] = v.toString()
            is JsonNum -> out[path] = v.raw
            else -> out[path] = v.toString()
        }
    }

    fun jsonCompare(a: String, b: String): List<String> {
        val va = parseJson(a.trim())
        val vb = parseJson(b.trim())
        val ma = LinkedHashMap<String, String>()
        val mb = LinkedHashMap<String, String>()
        flattenJson(va, "", ma)
        flattenJson(vb, "", mb)
        val keys = LinkedHashSet<String>()
        keys.addAll(ma.keys)
        keys.addAll(mb.keys)
        val diff = ArrayList<String>()
        for (k in keys) {
            val hasA = ma.containsKey(k)
            val hasB = mb.containsKey(k)
            if (!hasA || !hasB || ma[k] != mb[k]) {
                diff.add(if (k.isEmpty()) "(root)" else k)
            }
        }
        return diff.sorted()
    }

    private fun cronFieldExplain(field: String, singular: String, min: Int, max: Int): String {
        val plural = singular + "s"
        if (field == "*") return "every $singular"
        if (field.startsWith("*/")) {
            val step = field.removePrefix("*/")
            val n = step.toIntOrNull() ?: throw IllegalArgumentException("Bad cron field: $field")
            require(n > 0) { "Bad cron step: $field" }
            return "every $n $plural"
        }
        for (item in field.split(",")) {
            if (item.isEmpty()) throw IllegalArgumentException("Bad cron field: $field")
            val slash = item.split("/")
            val base = slash[0]
            if (slash.size == 2) {
                slash[1].toIntOrNull()?.let { require(it > 0) { "Bad cron step: $field" } }
                    ?: throw IllegalArgumentException("Bad cron field: $field")
            } else if (slash.size > 2) {
                throw IllegalArgumentException("Bad cron field: $field")
            }
            if (base == "*") continue
            for (tok in base.split("-")) {
                if (tok.isEmpty()) throw IllegalArgumentException("Bad cron field: $field")
                val num = tok.toIntOrNull() ?: throw IllegalArgumentException("Bad cron field: $field")
                require(num in min..max) { "Cron $singular out of range: $field" }
            }
            if (base.contains("-")) {
                val ends = base.split("-")
                if (ends.size != 2) throw IllegalArgumentException("Bad cron field: $field")
            }
        }
        return "at $field $plural"
    }

    fun crontabExplain(expr: String): String {
        val parts = expr.trim().split("\\s+".toRegex())
        require(parts.size == 5) { "Expected 5 fields: minute hour day month weekday" }
        val min = cronFieldExplain(parts[0], "minute", 0, 59)
        val hour = cronFieldExplain(parts[1], "hour", 0, 23)
        val dom = cronFieldExplain(parts[2], "day", 1, 31)
        val mon = cronFieldExplain(parts[3], "month", 1, 12)
        val dow = cronFieldExplain(parts[4], "day of week", 0, 7)
        return "Minutes: $min; Hours: $hour; Day of month: $dom; Month: $mon; Day of week: $dow"
    }

    fun isLeapYear(y: Int): Boolean {
        return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
    }

    fun discordTimestamp(unixSec: Long, style: String): String {
        require(style in setOf("t", "T", "d", "D", "f", "F", "R")) { "Bad discord style: $style" }
        return "<t:$unixSec:$style>"
    }
}
