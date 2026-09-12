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

    /** Max lines per input accepted by [diffLines]. */
    const val MAX_DIFF_LINES = 2000

    /** Max combined chars of both inputs accepted by [diffLines]. */
    const val MAX_DIFF_CHARS = 200_000

    /**
     * Line diff via LCS dynamic programming, O(n*m) time and memory.
     *
     * Limits: each input is capped at [MAX_DIFF_LINES] lines and both inputs
     * combined at [MAX_DIFF_CHARS] chars; larger inputs throw
     * IllegalArgumentException naming the limit. The DP loop checks thread
     * interruption on every row and aborts with CancellationException so
     * large-but-allowed inputs stay cancellable.
     */
    fun diffLines(a: String, b: String): List<String> {
        require(a.length + b.length <= MAX_DIFF_CHARS) {
            "diff too large: max $MAX_DIFF_CHARS chars total"
        }
        val x = if (a.isEmpty()) emptyList() else a.split("\n").map { it.trimEnd('\r') }
        val y = if (b.isEmpty()) emptyList() else b.split("\n").map { it.trimEnd('\r') }
        require(x.size <= MAX_DIFF_LINES && y.size <= MAX_DIFF_LINES) {
            "diff too large: max $MAX_DIFF_LINES lines per input"
        }
        val n = x.size
        val m = y.size
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            if (Thread.currentThread().isInterrupted) {
                throw java.util.concurrent.CancellationException("diffLines cancelled")
            }
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

    // Crypto pack (own implementation; standard JDK/JCE algorithms only).
    // Notes:
    // - Hex is manual lowercase (no HexFormat) for minSdk 24 safety.
    // - Keccak (pre-standard padding) is NOT provided: MessageDigest offers
    //   SHA3-* but not Keccak; implementing Keccak from scratch is out of scope.
    // - BLAKE2 / RIPEMD160 are NOT provided: no JDK provider; out of scope.
    // - Every function below is total or throws IllegalArgumentException.

    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    private fun bytesToHex(bytes: ByteArray): String {
        val out = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            out[i * 2] = HEX_CHARS[v ushr 4]
            out[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(out)
    }

    private fun hexVal(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> throw IllegalArgumentException("Bad hex char: $c")
    }

    private fun hexToBytes(hex: String): ByteArray {
        val t = hex.trim()
        require(t.length % 2 == 0) { "Hex must have even length" }
        val out = ByteArray(t.length / 2)
        for (i in out.indices) {
            out[i] = ((hexVal(t[i * 2]) shl 4) or hexVal(t[i * 2 + 1])).toByte()
        }
        return out
    }

    private fun hmacRaw(alg: String, keyBytes: ByteArray, msgBytes: ByteArray): ByteArray {
        try {
            val mac = javax.crypto.Mac.getInstance(alg)
            mac.init(javax.crypto.spec.SecretKeySpec(keyBytes, alg))
            return mac.doFinal(msgBytes)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: java.security.NoSuchAlgorithmException) {
            throw IllegalArgumentException("$alg unavailable", e)
        } catch (e: java.security.InvalidKeyException) {
            throw IllegalArgumentException("Bad HMAC key", e)
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    private fun hmacHex(alg: String, key: String, msg: String, encoding: String): String {
        try {
            val cs = java.nio.charset.Charset.forName(encoding)
            return bytesToHex(hmacRaw(alg, key.toByteArray(cs), msg.toByteArray(cs)))
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun hmacSha256(key: String, msg: String, encoding: String = "UTF-8"): String =
        hmacHex("HmacSHA256", key, msg, encoding)

    fun hmacSha384(key: String, msg: String, encoding: String = "UTF-8"): String =
        hmacHex("HmacSHA384", key, msg, encoding)

    fun hmacSha512(key: String, msg: String, encoding: String = "UTF-8"): String =
        hmacHex("HmacSHA512", key, msg, encoding)

    private fun jwtPartDecode(part: String): String {
        try {
            var s = part.trim()
            require(s.isNotEmpty()) { "Empty JWT part" }
            val rem = s.length % 4
            if (rem == 1) throw IllegalArgumentException("Bad base64url length")
            if (rem != 0) s += "=".repeat(4 - rem)
            return Base64.getUrlDecoder().decode(s).toString(Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Bad base64url", e)
        }
    }

    fun jwtDecode(token: String): Triple<String, String, String> {
        try {
            var t = token.trim()
            require(t.isNotEmpty()) { "Empty token" }
            if (t.regionMatches(0, "Bearer ", 0, 7, ignoreCase = true)) {
                t = t.substring(7).trim()
            }
            val parts = t.split(".")
            require(parts.size == 3) { "JWT must have 3 parts" }
            require(parts[0].isNotEmpty() && parts[1].isNotEmpty() && parts[2].isNotEmpty()) {
                "JWT parts must not be empty"
            }
            val header = jwtPartDecode(parts[0])
            val payload = jwtPartDecode(parts[1])
            return Triple(header, payload, "signature: " + parts[2].trim())
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    private const val B32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun base32Encode(text: String): String {
        val data = text.toByteArray(Charsets.UTF_8)
        if (data.isEmpty()) return ""
        val sb = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (b in data) {
            buffer = ((buffer and ((1 shl bitsLeft) - 1)) shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                bitsLeft -= 5
                sb.append(B32_ALPHABET[(buffer shr bitsLeft) and 0x1F])
                buffer = buffer and ((1 shl bitsLeft) - 1)
            }
        }
        if (bitsLeft > 0) {
            sb.append(B32_ALPHABET[(buffer shl (5 - bitsLeft)) and 0x1F])
        }
        while (sb.length % 8 != 0) sb.append('=')
        return sb.toString()
    }

    fun base32Decode(text: String): String {
        try {
            val t = text.trim().uppercase()
            if (t.isEmpty()) return ""
            require(t.length % 8 == 0) { "Bad base32 length" }
            var pad = 0
            while (pad < t.length && t[t.length - 1 - pad] == '=') pad++
            require(pad == 0 || pad == 1 || pad == 3 || pad == 4 || pad == 6) {
                "Bad base32 padding"
            }
            val core = t.substring(0, t.length - pad)
            require(!core.contains('=')) { "Bad base32 padding" }
            val out = ArrayList<Byte>(core.length * 5 / 8 + 1)
            var buffer = 0
            var bitsLeft = 0
            for (c in core) {
                val v = B32_ALPHABET.indexOf(c)
                if (v < 0) throw IllegalArgumentException("Bad base32 char: $c")
                buffer = (buffer shl 5) or v
                bitsLeft += 5
                if (bitsLeft >= 8) {
                    bitsLeft -= 8
                    out.add(((buffer shr bitsLeft) and 0xFF).toByte())
                }
            }
            if (bitsLeft > 0) {
                require((buffer and ((1 shl bitsLeft) - 1)) == 0) { "Bad base32 trailing bits" }
            }
            return out.toByteArray().toString(Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    private const val B58_ALPHABET =
        "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

    private fun base58EncodeBytes(input: ByteArray): String {
        if (input.isEmpty()) return ""
        var zeros = 0
        while (zeros < input.size && input[zeros].toInt() == 0) zeros++
        val temp = IntArray(input.size) { input[it].toInt() and 0xFF }
        var start = zeros
        val sb = StringBuilder()
        while (true) {
            while (start < temp.size && temp[start] == 0) start++
            if (start >= temp.size) break
            var carry = 0
            for (i in start until temp.size) {
                val cur = carry * 256 + temp[i]
                temp[i] = cur / 58
                carry = cur % 58
            }
            sb.append(B58_ALPHABET[carry])
        }
        repeat(zeros) { sb.append('1') }
        return sb.reverse().toString()
    }

    private fun base58DecodeBytes(s: String): ByteArray {
        val t = s.trim()
        if (t.isEmpty()) return ByteArray(0)
        val vals = IntArray(t.length)
        for (i in t.indices) {
            val v = B58_ALPHABET.indexOf(t[i])
            if (v < 0) throw IllegalArgumentException("Bad base58 char: ${t[i]}")
            vals[i] = v
        }
        var zeros = 0
        while (zeros < vals.size && vals[zeros] == 0) zeros++
        val temp = vals.copyOf()
        var start = zeros
        val decoded = ArrayList<Byte>()
        while (true) {
            while (start < temp.size && temp[start] == 0) start++
            if (start >= temp.size) break
            var carry = 0
            for (i in start until temp.size) {
                val cur = carry * 58 + temp[i]
                temp[i] = cur / 256
                carry = cur % 256
            }
            decoded.add(carry.toByte())
        }
        val out = ByteArray(zeros + decoded.size)
        for (i in decoded.indices) out[out.size - 1 - i] = decoded[i]
        return out
    }

    fun base58Encode(text: String): String =
        base58EncodeBytes(text.toByteArray(Charsets.UTF_8))

    fun base58Decode(text: String): String {
        try {
            return base58DecodeBytes(text).toString(Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun htmlEscape(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) {
            when (c) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&#39;")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun htmlUnescape(text: String): String {
        try {
            val sb = StringBuilder(text.length)
            var i = 0
            while (i < text.length) {
                if (text[i] != '&') {
                    sb.append(text[i])
                    i++
                    continue
                }
                val semi = text.indexOf(';', i + 1)
                if (semi < 0) {
                    sb.append('&')
                    i++
                    continue
                }
                val entity = text.substring(i + 1, semi)
                val decoded: String? = when (entity) {
                    "amp" -> "&"
                    "lt" -> "<"
                    "gt" -> ">"
                    "quot" -> "\""
                    "apos" -> "'"
                    else -> if (entity.startsWith("#") && entity.length > 1) {
                        val code = if (entity[1] == 'x' || entity[1] == 'X') {
                            require(entity.length > 2) { "Bad entity: &$entity;" }
                            entity.substring(2).toInt(16)
                        } else {
                            entity.substring(1).toInt(10)
                        }
                        String(Character.toChars(code))
                    } else {
                        null
                    }
                }
                if (decoded != null) {
                    sb.append(decoded)
                    i = semi + 1
                } else {
                    sb.append('&')
                    i++
                }
            }
            return sb.toString()
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun crc32(text: String): Long {
        val crc = java.util.zip.CRC32()
        crc.update(text.toByteArray(Charsets.UTF_8))
        return crc.value
    }

    fun adler32(text: String): Long {
        val a = java.util.zip.Adler32()
        a.update(text.toByteArray(Charsets.UTF_8))
        return a.value
    }

    fun crc16(text: String): Int {
        var crc = 0x0000
        for (b in text.toByteArray(Charsets.UTF_8)) {
            crc = crc xor (b.toInt() and 0xFF)
            repeat(8) {
                crc = if (crc and 1 != 0) (crc ushr 1) xor 0xA001 else crc ushr 1
            }
        }
        return crc and 0xFFFF
    }

    private fun digestHex(alg: String, text: String): String {
        try {
            return bytesToHex(MessageDigest.getInstance(alg).digest(text.toByteArray(Charsets.UTF_8)))
        } catch (e: java.security.NoSuchAlgorithmException) {
            throw IllegalArgumentException("$alg unavailable", e)
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun sha1(text: String): String = digestHex("SHA-1", text)

    fun sha224(text: String): String = digestHex("SHA-224", text)

    fun sha3_256(text: String): String = digestHex("SHA3-256", text)

    fun sha3_384(text: String): String = digestHex("SHA3-384", text)

    fun sha3_512(text: String): String = digestHex("SHA3-512", text)

    fun pbkdf2Sha256(password: String, saltHex: String, iterations: Int, bits: Int): String {
        require(iterations > 0) { "iterations must be > 0" }
        require(bits >= 8) { "bits must be >= 8" }
        require(bits % 8 == 0) { "bits must be a multiple of 8" }
        try {
            val salt = hexToBytes(saltHex)
            val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, iterations, bits)
            val skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            return bytesToHex(skf.generateSecret(spec).encoded)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun hkdfSha256(ikm: ByteArray, salt: ByteArray, info: ByteArray, length: Int): ByteArray {
        require(length in 1..(255 * 32)) { "length must be 1..8160" }
        try {
            val realSalt = if (salt.isEmpty()) ByteArray(32) else salt
            val prk = hmacRaw("HmacSHA256", realSalt, ikm)
            val out = ByteArray(length)
            var prev = ByteArray(0)
            var pos = 0
            var counter = 1
            while (pos < length) {
                val msg = prev + info + byteArrayOf(counter.toByte())
                prev = hmacRaw("HmacSHA256", prk, msg)
                val n = minOf(prev.size, length - pos)
                prev.copyInto(out, pos, 0, n)
                pos += n
                counter++
            }
            return out
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun aesCbcEncrypt(keyHex: String, ivHex: String, plaintext: String): String {
        try {
            val key = hexToBytes(keyHex)
            require(key.size == 16 || key.size == 24 || key.size == 32) {
                "Key must be 128/192/256-bit hex"
            }
            val iv = hexToBytes(ivHex)
            require(iv.size == 16) { "IV must be 16 bytes hex" }
            val cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(
                javax.crypto.Cipher.ENCRYPT_MODE,
                javax.crypto.spec.SecretKeySpec(key, "AES"),
                javax.crypto.spec.IvParameterSpec(iv)
            )
            return bytesToHex(cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8)))
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun aesCbcDecrypt(keyHex: String, ivHex: String, ciphertextHex: String): String {
        try {
            val key = hexToBytes(keyHex)
            require(key.size == 16 || key.size == 24 || key.size == 32) {
                "Key must be 128/192/256-bit hex"
            }
            val iv = hexToBytes(ivHex)
            require(iv.size == 16) { "IV must be 16 bytes hex" }
            val cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(
                javax.crypto.Cipher.DECRYPT_MODE,
                javax.crypto.spec.SecretKeySpec(key, "AES"),
                javax.crypto.spec.IvParameterSpec(iv)
            )
            return cipher.doFinal(hexToBytes(ciphertextHex)).toString(Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    private fun chachaBlock(key: ByteArray, nonce: ByteArray, counter: Int): ByteArray {
        fun le(b: ByteArray, o: Int): Int =
            (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or
                ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)
        fun rotl(v: Int, n: Int): Int = (v shl n) or (v ushr (32 - n))
        val s = IntArray(16)
        s[0] = 0x61707865; s[1] = 0x3320646e; s[2] = 0x79622d32; s[3] = 0x6b206574
        for (i in 0..7) s[4 + i] = le(key, i * 4)
        s[12] = counter
        s[13] = le(nonce, 0); s[14] = le(nonce, 4); s[15] = le(nonce, 8)
        val w = s.copyOf()
        fun qr(a: Int, b: Int, c: Int, d: Int) {
            w[a] += w[b]; w[d] = rotl(w[d] xor w[a], 16)
            w[c] += w[d]; w[b] = rotl(w[b] xor w[c], 12)
            w[a] += w[b]; w[d] = rotl(w[d] xor w[a], 8)
            w[c] += w[d]; w[b] = rotl(w[b] xor w[c], 7)
        }
        repeat(10) {
            qr(0, 4, 8, 12); qr(1, 5, 9, 13); qr(2, 6, 10, 14); qr(3, 7, 11, 15)
            qr(0, 5, 10, 15); qr(1, 6, 11, 12); qr(2, 7, 8, 13); qr(3, 4, 9, 14)
        }
        val out = ByteArray(64)
        for (i in 0..15) {
            val v = w[i] + s[i]
            out[i * 4] = (v and 0xFF).toByte()
            out[i * 4 + 1] = ((v ushr 8) and 0xFF).toByte()
            out[i * 4 + 2] = ((v ushr 16) and 0xFF).toByte()
            out[i * 4 + 3] = ((v ushr 24) and 0xFF).toByte()
        }
        return out
    }

    private fun chachaCrypt(key: ByteArray, nonce: ByteArray, counter: Int, data: ByteArray): ByteArray {
        val out = ByteArray(data.size)
        var block = 0
        var pos = 0
        while (pos < data.size) {
            val ks = chachaBlock(key, nonce, counter + block)
            val n = minOf(64, data.size - pos)
            for (i in 0 until n) out[pos + i] = (data[pos + i].toInt() xor ks[i].toInt()).toByte()
            pos += n
            block++
        }
        return out
    }

    fun chacha20Encrypt(keyHex: String, nonceHex: String, counter: Int, plaintext: String): String {
        try {
            val key = hexToBytes(keyHex)
            require(key.size == 32) { "Key must be 32 bytes hex" }
            val nonce = hexToBytes(nonceHex)
            require(nonce.size == 12) { "Nonce must be 12 bytes hex" }
            require(counter >= 0) { "counter must be >= 0" }
            return bytesToHex(chachaCrypt(key, nonce, counter, plaintext.toByteArray(Charsets.UTF_8)))
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun chacha20Decrypt(keyHex: String, nonceHex: String, counter: Int, ciphertextHex: String): String {
        try {
            val key = hexToBytes(keyHex)
            require(key.size == 32) { "Key must be 32 bytes hex" }
            val nonce = hexToBytes(nonceHex)
            require(nonce.size == 12) { "Nonce must be 12 bytes hex" }
            require(counter >= 0) { "counter must be >= 0" }
            return chachaCrypt(key, nonce, counter, hexToBytes(ciphertextHex)).toString(Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun rc4(text: String, key: String): String {
        require(key.isNotEmpty()) { "Key must not be empty" }
        try {
            val cipher = try {
                javax.crypto.Cipher.getInstance("ARCFOUR")
            } catch (e: java.security.NoSuchAlgorithmException) {
                throw IllegalArgumentException("RC4 unavailable on this device", e)
            }
            cipher.init(
                javax.crypto.Cipher.ENCRYPT_MODE,
                javax.crypto.spec.SecretKeySpec(key.toByteArray(Charsets.UTF_8), "ARCFOUR")
            )
            return bytesToHex(cipher.doFinal(text.toByteArray(Charsets.UTF_8)))
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun rc4Decrypt(ciphertextHex: String, key: String): String {
        require(key.isNotEmpty()) { "Key must not be empty" }
        try {
            val cipher = try {
                javax.crypto.Cipher.getInstance("ARCFOUR")
            } catch (e: java.security.NoSuchAlgorithmException) {
                throw IllegalArgumentException("RC4 unavailable on this device", e)
            }
            cipher.init(
                javax.crypto.Cipher.DECRYPT_MODE,
                javax.crypto.spec.SecretKeySpec(key.toByteArray(Charsets.UTF_8), "ARCFOUR")
            )
            return cipher.doFinal(hexToBytes(ciphertextHex)).toString(Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    private fun cborEncodeHead(major: Int, value: Long, out: ArrayList<Byte>) {
        require(value >= 0) { "CBOR length must be >= 0" }
        val mt = major shl 5
        when {
            value <= 23 -> out.add((mt or value.toInt()).toByte())
            value <= 0xFF -> {
                out.add((mt or 24).toByte())
                out.add(value.toByte())
            }
            value <= 0xFFFF -> {
                out.add((mt or 25).toByte())
                out.add((value ushr 8).toByte())
                out.add(value.toByte())
            }
            value <= 0xFFFFFFFFL -> {
                out.add((mt or 26).toByte())
                for (s in 24 downTo 0 step 8) out.add((value ushr s).toByte())
            }
            else -> {
                out.add((mt or 27).toByte())
                for (s in 56 downTo 0 step 8) out.add((value ushr s).toByte())
            }
        }
    }

    private fun cborEncodeInt(raw: String, out: ArrayList<Byte>) {
        val n = raw.toLongOrNull() ?: throw IllegalArgumentException("CBOR floats unsupported: $raw")
        if (n >= 0) cborEncodeHead(0, n, out) else cborEncodeHead(1, -(n + 1), out)
    }

    private fun cborEncodeValue(v: Any?, out: ArrayList<Byte>) {
        when (v) {
            null -> out.add(0xF6.toByte())
            is Boolean -> out.add(if (v) 0xF5.toByte() else 0xF4.toByte())
            is String -> {
                val bytes = v.toByteArray(Charsets.UTF_8)
                cborEncodeHead(3, bytes.size.toLong(), out)
                for (b in bytes) out.add(b)
            }
            is JsonNum -> cborEncodeInt(v.raw, out)
            is Map<*, *> -> {
                cborEncodeHead(5, v.size.toLong(), out)
                for ((k, vv) in v) {
                    require(k is String) { "CBOR map keys must be text" }
                    cborEncodeValue(k, out)
                    cborEncodeValue(vv, out)
                }
            }
            is List<*> -> {
                cborEncodeHead(4, v.size.toLong(), out)
                for (e in v) cborEncodeValue(e, out)
            }
            else -> throw IllegalArgumentException("CBOR unsupported value")
        }
    }

    fun cborEncodeJson(json: String): String {
        try {
            val v = parseJson(json.trim())
            val out = ArrayList<Byte>()
            cborEncodeValue(v, out)
            return bytesToHex(out.toByteArray())
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    private fun cborReadArg(bytes: ByteArray, pos: IntArray, ai: Int): Long {
        return when {
            ai < 24 -> ai.toLong()
            ai == 24 -> {
                require(pos[0] < bytes.size) { "Truncated CBOR" }
                (bytes[pos[0]++].toInt() and 0xFF).toLong()
            }
            ai == 25 -> {
                require(pos[0] + 2 <= bytes.size) { "Truncated CBOR" }
                val hi = (bytes[pos[0]++].toInt() and 0xFF).toLong()
                val lo = (bytes[pos[0]++].toInt() and 0xFF).toLong()
                (hi shl 8) or lo
            }
            ai == 26 -> {
                require(pos[0] + 4 <= bytes.size) { "Truncated CBOR" }
                var acc = 0L
                repeat(4) { acc = (acc shl 8) or (bytes[pos[0]++].toInt() and 0xFF).toLong() }
                acc
            }
            ai == 27 -> {
                require(pos[0] + 8 <= bytes.size) { "Truncated CBOR" }
                var acc = 0L
                repeat(8) { acc = (acc shl 8) or (bytes[pos[0]++].toInt() and 0xFF).toLong() }
                if (acc < 0) throw IllegalArgumentException("CBOR length too large")
                acc
            }
            else -> throw IllegalArgumentException("Indefinite CBOR unsupported")
        }
    }

    private fun cborDecodeValue(bytes: ByteArray, pos: IntArray): Any? {
        require(pos[0] < bytes.size) { "Truncated CBOR" }
        val ib = bytes[pos[0]++].toInt() and 0xFF
        val major = ib shr 5
        val ai = ib and 0x1F
        return when (major) {
            0 -> JsonNum(cborReadArg(bytes, pos, ai).toString())
            1 -> JsonNum((-1L - cborReadArg(bytes, pos, ai)).toString())
            3 -> {
                val n = cborReadArg(bytes, pos, ai)
                require(n <= Int.MAX_VALUE) { "CBOR text too large" }
                val len = n.toInt()
                require(pos[0] + len <= bytes.size) { "Truncated CBOR text" }
                val s = bytes.copyOfRange(pos[0], pos[0] + len).toString(Charsets.UTF_8)
                pos[0] += len
                s
            }
            4 -> {
                val n = cborReadArg(bytes, pos, ai)
                require(n <= Int.MAX_VALUE) { "CBOR array too large" }
                ArrayList<Any?>().also { list ->
                    repeat(n.toInt()) { list.add(cborDecodeValue(bytes, pos)) }
                }
            }
            5 -> {
                val n = cborReadArg(bytes, pos, ai)
                require(n <= Int.MAX_VALUE) { "CBOR map too large" }
                LinkedHashMap<String, Any?>().also { map ->
                    repeat(n.toInt()) {
                        val k = cborDecodeValue(bytes, pos)
                        require(k is String) { "CBOR map keys must be text" }
                        map[k] = cborDecodeValue(bytes, pos)
                    }
                }
            }
            7 -> when (ai) {
                20 -> false
                21 -> true
                22 -> null
                else -> throw IllegalArgumentException("Unsupported CBOR simple/float: $ai")
            }
            else -> throw IllegalArgumentException("Unsupported CBOR major: $major")
        }
    }

    private fun cborToJson(v: Any?): String = when (v) {
        null -> "null"
        is Boolean -> v.toString()
        is String -> "\"" + jsonEscape(v) + "\""
        is JsonNum -> v.raw
        is List<*> -> v.joinToString(",", "[", "]") { cborToJson(it) }
        is Map<*, *> -> v.entries.joinToString(",", "{", "}") { (k, vv) ->
            "\"" + jsonEscape(k.toString()) + "\":" + cborToJson(vv)
        }
        else -> throw IllegalArgumentException("Bad CBOR value")
    }

    fun cborDecodeHex(hex: String): String {
        try {
            val bytes = hexToBytes(hex)
            require(bytes.isNotEmpty()) { "Empty CBOR" }
            val pos = intArrayOf(0)
            val v = cborDecodeValue(bytes, pos)
            require(pos[0] == bytes.size) { "Trailing CBOR bytes" }
            return cborToJson(v)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    private fun formatUuid(msb: Long, lsb: Long): String {
        val a = (msb ushr 32) and 0xFFFFFFFFL
        val b = (msb ushr 16) and 0xFFFFL
        val c = msb and 0xFFFFL
        val d = (lsb ushr 48) and 0xFFFFL
        val e = lsb and 0xFFFFFFFFFFFFL
        return "%08x-%04x-%04x-%04x-%012x".format(a, b, c, d, e)
    }

    fun uuidV1(): String {
        val rand = java.security.SecureRandom()
        val ts = System.currentTimeMillis() * 10000L + 0x01B21DD213814000L
        val timeLow = ts and 0xFFFFFFFFL
        val timeMid = (ts ushr 32) and 0xFFFFL
        val timeHi = (ts ushr 48) and 0x0FFFL
        val msb = (timeLow shl 32) or (timeMid shl 16) or 0x1000L or timeHi
        val clockSeq = (rand.nextInt(1 shl 14) or 0x8000).toLong()
        val node = (rand.nextLong() and 0xFFFFFFFFFFFFL) or 0x010000000000L
        return formatUuid(msb, (clockSeq shl 48) or node)
    }

    fun uuidV6(): String {
        val rand = java.security.SecureRandom()
        val ts = System.currentTimeMillis() * 10000L + 0x01B21DD213814000L
        val timeHigh = (ts ushr 28) and 0xFFFFFFFFL
        val timeMid = (ts ushr 12) and 0xFFFFL
        val timeLow = ts and 0xFFFL
        val msb = (timeHigh shl 32) or (timeMid shl 16) or 0x6000L or timeLow
        val clockSeq = (rand.nextInt(1 shl 14) or 0x8000).toLong()
        val node = (rand.nextLong() and 0xFFFFFFFFFFFFL) or 0x010000000000L
        return formatUuid(msb, (clockSeq shl 48) or node)
    }

    fun uuidV7(): String {
        val rand = java.security.SecureRandom()
        val tsMs = System.currentTimeMillis() and 0xFFFFFFFFFFFFL
        val randA = rand.nextInt(1 shl 12).toLong()
        val randB = rand.nextLong() and 0x3FFFFFFFFFFFFFFFL
        val msb = (tsMs shl 16) or 0x7000L or randA
        val lsb = Long.MIN_VALUE or randB
        return formatUuid(msb, lsb)
    }

    fun bcryptHash(text: String, rounds: Int = 10): String {
        require(rounds in 4..31) { "rounds must be 4..31" }
        try {
            return org.mindrot.jbcrypt.BCrypt.hashpw(
                text,
                org.mindrot.jbcrypt.BCrypt.gensalt(rounds)
            )
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    fun bcryptVerify(text: String, hash: String): Boolean {
        return try {
            org.mindrot.jbcrypt.BCrypt.checkpw(text, hash)
        } catch (e: Exception) {
            false
        }
    }
}
