package calc.u.core

import kotlin.random.Random

// Text++ core: text/list/csv/json/xml/case utilities.
// Ideas after omni-tools (MIT); all code below is an original implementation.
object TextPlus {

    // ---------- hidden chars ----------

    private fun hiddenName(code: Int): String? {
        when (code) {
            0x202A -> return "LEFT-TO-RIGHT EMBEDDING"
            0x202B -> return "RIGHT-TO-LEFT EMBEDDING"
            0x202C -> return "POP DIRECTIONAL FORMATTING"
            0x202D -> return "LEFT-TO-RIGHT OVERRIDE"
            0x202E -> return "RIGHT-TO-LEFT OVERRIDE"
            0x200E -> return "LEFT-TO-RIGHT MARK"
            0x200F -> return "RIGHT-TO-LEFT MARK"
            0x200B -> return "ZERO WIDTH SPACE"
            0x200C -> return "ZERO WIDTH NON-JOINER"
            0x200D -> return "ZERO WIDTH JOINER"
            0x2060 -> return "WORD JOINER"
            0xFEFF -> return "ZERO WIDTH NO-BREAK SPACE"
            0x00A0 -> return "NO-BREAK SPACE"
            0x2000 -> return "EN QUAD"
            0x2001 -> return "EM QUAD"
            0x2002 -> return "EN SPACE"
            0x2003 -> return "EM SPACE"
            0x2004 -> return "THREE-PER-EM SPACE"
            0x2005 -> return "FOUR-PER-EM SPACE"
            0x2006 -> return "SIX-PER-EM SPACE"
            0x2007 -> return "FIGURE SPACE"
            0x2008 -> return "PUNCTUATION SPACE"
            0x2009 -> return "THIN SPACE"
            0x200A -> return "HAIR SPACE"
        }
        if (code == 9) return "TAB"
        if (code == 10) return "LINE FEED"
        if (code == 13) return "CARRIAGE RETURN"
        if (code < 32 || code == 127) return "CONTROL"
        return null
    }

    fun hiddenChars(text: String): List<Triple<Int, String, String>> {
        val out = ArrayList<Triple<Int, String, String>>()
        for (i in text.indices) {
            val code = text[i].code
            val name = hiddenName(code) ?: continue
            out.add(Triple(i, name, "U+%04X".format(code)))
        }
        return out
    }

    // ---------- unicode ----------

    fun unicodeEncode(text: String): String {
        val sb = StringBuilder(text.length * 6)
        for (c in text) {
            sb.append("\\u%04X".format(c.code))
        }
        return sb.toString()
    }

    fun unicodeDecode(text: String): String {
        val sb = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\\' && i + 1 < text.length && text[i + 1] == 'u') {
                if (i + 6 > text.length) throw IllegalArgumentException("Bad unicode escape at $i")
                val hex = text.substring(i + 2, i + 6)
                val code = hex.toIntOrNull(16) ?: throw IllegalArgumentException("Bad unicode escape at $i")
                sb.append(code.toChar())
                i += 6
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    // ---------- url ----------

    fun urlParse(text: String): Map<String, String> {
        var s = text
        var fragment = ""
        var query = ""
        val hash = s.indexOf('#')
        if (hash >= 0) {
            fragment = s.substring(hash + 1)
            s = s.substring(0, hash)
        }
        val q = s.indexOf('?')
        if (q >= 0) {
            query = s.substring(q + 1)
            s = s.substring(0, q)
        }
        var protocol = ""
        var host = ""
        var path = ""
        val sep = s.indexOf("://")
        if (sep >= 0) {
            protocol = s.substring(0, sep)
            val rest = s.substring(sep + 3)
            val slash = rest.indexOf('/')
            if (slash >= 0) {
                host = rest.substring(0, slash)
                path = rest.substring(slash)
            } else {
                host = rest
                path = ""
            }
        } else {
            path = s
        }
        val out = LinkedHashMap<String, String>(5)
        out["protocol"] = protocol
        out["host"] = host
        out["path"] = path
        out["query"] = query
        out["fragment"] = fragment
        return out
    }

    fun urlBuild(parts: Map<String, String>): String {
        val protocol = parts["protocol"] ?: ""
        val host = parts["host"] ?: ""
        val path = parts["path"] ?: ""
        val query = parts["query"] ?: ""
        val fragment = parts["fragment"] ?: ""
        val sb = StringBuilder()
        if (protocol.isNotEmpty()) sb.append(protocol).append("://")
        sb.append(host)
        sb.append(path)
        if (query.isNotEmpty()) sb.append('?').append(query)
        if (fragment.isNotEmpty()) sb.append('#').append(fragment)
        return sb.toString()
    }

    // ---------- censor / substring / dedupe ----------

    fun censor(text: String, words: List<String>, mask: Char = '█'): String {
        val keys = words.filter { it.isNotEmpty() }
        if (keys.isEmpty() || text.isEmpty()) return text
        val alt = keys.map { Regex.escape(it) }.joinToString("|")
        val re = Regex("\\b(?:$alt)\\b", RegexOption.IGNORE_CASE)
        return re.replace(text) { m -> mask.toString().repeat(m.value.length) }
    }

    private fun substringSingle(text: String, start1Based: Int, length: Int): String {
        if (text.isEmpty() || length == 0) return ""
        val from = start1Based - 1
        if (from >= text.length) return ""
        val end = minOf(text.length, from + length)
        return text.substring(from, end)
    }

    fun substring1(text: String, start1Based: Int, length: Int, perLine: Boolean = false): String {
        require(start1Based >= 1) { "start must be >= 1" }
        require(length >= 0) { "length must be >= 0" }
        if (!perLine) return substringSingle(text, start1Based, length)
        if (text.isEmpty()) return ""
        return text.split("\n").map { line ->
            substringSingle(line.trimEnd('\r'), start1Based, length)
        }.joinToString("\n")
    }

    fun dedupeLines(text: String, mode: String = "all"): String {
        val m = mode.lowercase()
        require(m == "all" || m == "consecutive" || m == "unique") { "Bad mode: $mode" }
        if (text.isEmpty()) return ""
        val lines = text.split("\n").map { it.trimEnd('\r') }
        return when (m) {
            "all" -> lines.distinct().joinToString("\n")
            "consecutive" -> {
                val out = ArrayList<String>(lines.size)
                for (line in lines) {
                    if (out.isEmpty() || out.last() != line) out.add(line)
                }
                out.joinToString("\n")
            }
            else -> {
                val counts = LinkedHashMap<String, Int>()
                for (line in lines) counts[line] = (counts[line] ?: 0) + 1
                lines.filter { counts[it] == 1 }.joinToString("\n")
            }
        }
    }

    // ---------- basic text ops ----------

    fun reverseText(text: String): String = text.reversed()

    fun rotateText(text: String, n: Int): String {
        if (text.isEmpty()) return ""
        val k = ((n % text.length) + text.length) % text.length
        if (k == 0) return text
        return text.drop(k) + text.take(k)
    }

    fun quoteLines(text: String, prefix: String = "> "): String {
        if (text.isEmpty()) return ""
        return text.split("\n").map { raw ->
            val line = raw.trimEnd('\r')
            if (line.trimStart().startsWith(">")) line else prefix + line
        }.joinToString("\n")
    }

    fun splitText(text: String, delimiter: String = "\n"): List<String> {
        if (delimiter.isEmpty()) {
            if (text.isEmpty()) return emptyList()
            return text.map { it.toString() }
        }
        return text.split(delimiter)
    }

    fun joinLines(lines: List<String>, separator: String = "\n"): String =
        lines.joinToString(separator)

    fun repeatText(text: String, count: Int): String {
        if (count < 0) return ""
        if (count == 0 || text.isEmpty()) return ""
        return text.repeat(count)
    }

    fun truncateText(text: String, maxLength: Int, side: String = "end", indicator: String = "…"): String {
        require(maxLength >= 0) { "maxLength must be >= 0" }
        val s = side.lowercase()
        require(s == "end" || s == "start" || s == "middle") { "Bad side: $side" }
        if (text.length <= maxLength) return text
        if (maxLength <= indicator.length) return indicator.take(maxLength)
        val keep = maxLength - indicator.length
        return when (s) {
            "end" -> text.take(keep) + indicator
            "start" -> indicator + text.takeLast(keep)
            else -> {
                val left = (keep + 1) / 2
                val right = keep - left
                text.take(left) + indicator + text.takeLast(right)
            }
        }
    }

    // ---------- list ops ----------

    fun listSort(items: List<String>, ascending: Boolean = true, caseSensitive: Boolean = false): List<String> {
        val sorted = if (caseSensitive) {
            items.sortedWith(compareBy { it })
        } else {
            items.sortedWith(compareBy({ it.lowercase() }, { it }))
        }
        return if (ascending) sorted else sorted.asReversed()
    }

    fun listShuffle(items: List<String>, seed: Long? = null): List<String> {
        if (items.size <= 1) return items.toList()
        val rng = if (seed == null) Random.Default else Random(seed)
        val out = items.toMutableList()
        for (i in out.size - 1 downTo 1) {
            val j = rng.nextInt(i + 1)
            val tmp = out[i]
            out[i] = out[j]
            out[j] = tmp
        }
        return out
    }

    fun listUnique(items: List<String>): List<String> = items.distinct()

    fun listFrequency(items: List<String>): Map<String, Int> {
        val out = LinkedHashMap<String, Int>()
        for (item in items) out[item] = (out[item] ?: 0) + 1
        return out
    }

    fun listRotate(items: List<String>, n: Int): List<String> {
        if (items.isEmpty()) return emptyList()
        val k = ((n % items.size) + items.size) % items.size
        if (k == 0) return items.toList()
        return items.drop(k) + items.take(k)
    }

    fun listChunk(items: List<String>, size: Int): List<List<String>> {
        require(size > 0) { "size must be > 0" }
        return items.chunked(size)
    }

    fun listDuplicate(items: List<String>, times: Int = 2): List<String> {
        require(times >= 0) { "times must be >= 0" }
        if (times == 0 || items.isEmpty()) return emptyList()
        val out = ArrayList<String>(items.size * times)
        repeat(times) { out.addAll(items) }
        return out
    }

    // ---------- csv ----------

    private fun splitCsvRow(line: String, delim: Char): List<String> {
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
                when {
                    c == '"' -> { inQuotes = true; k++ }
                    c == delim -> { out.add(sb.toString()); sb.setLength(0); k++ }
                    else -> { sb.append(c); k++ }
                }
            }
        }
        if (inQuotes) throw IllegalArgumentException("Unterminated quote in CSV")
        out.add(sb.toString())
        return out
    }

    private fun csvJoinRow(fields: List<String>, delim: Char): String {
        return fields.joinToString(delim.toString()) { f ->
            if (f.contains(delim) || f.contains('"') || f.contains('\n') || f.contains('\r')) {
                "\"" + f.replace("\"", "\"\"") + "\""
            } else {
                f
            }
        }
    }

    fun csvSwapColumns(csv: String, a: Int, b: Int): String {
        require(a >= 0 && b >= 0) { "column indexes must be >= 0" }
        if (csv.isBlank()) return ""
        val lines = csv.split("\n").map { it.trimEnd('\r') }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return ""
        return lines.map { line ->
            val row = splitCsvRow(line, ',').toMutableList()
            val need = maxOf(a, b)
            while (row.size <= need) row.add("")
            val tmp = row[a]
            row[a] = row[b]
            row[b] = tmp
            csvJoinRow(row, ',')
        }.joinToString("\n")
    }

    fun csvTranspose(csv: String): String {
        if (csv.isBlank()) return ""
        val lines = csv.split("\n").map { it.trimEnd('\r') }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return ""
        val grid = lines.map { splitCsvRow(it, ',') }
        val cols = grid.maxOf { it.size }
        if (cols == 0) return ""
        val out = ArrayList<String>(cols)
        for (c in 0 until cols) {
            out.add(csvJoinRow(grid.indices.map { r -> grid[r].getOrElse(c) { "" } }, ','))
        }
        return out.joinToString("\n")
    }

    fun csvChangeDelimiter(csv: String, oldDelim: Char = ',', newDelim: Char = ';'): String {
        if (csv.isEmpty()) return ""
        return csv.split("\n").map { raw ->
            val line = raw.trimEnd('\r')
            if (line.isEmpty()) ""
            else csvJoinRow(splitCsvRow(line, oldDelim), newDelim)
        }.joinToString("\n")
    }

    fun csvFindIncomplete(csv: String): List<Int> {
        if (csv.isEmpty()) return emptyList()
        val raw = csv.split("\n")
        val bad = ArrayList<Int>()
        var headerSize: Int? = null
        for (idx in raw.indices) {
            val line = raw[idx].trimEnd('\r')
            if (line.isBlank()) continue
            val size = try {
                splitCsvRow(line, ',').size
            } catch (e: IllegalArgumentException) {
                bad.add(idx)
                continue
            }
            if (headerSize == null) headerSize = size
            else if (size != headerSize) bad.add(idx)
        }
        return bad
    }

    // ---------- json (only fns missing from TextData) ----------

    private fun skipWs(s: String, pos: IntArray) {
        while (pos[0] < s.length && (s[pos[0]] == ' ' || s[pos[0]] == '\n' || s[pos[0]] == '\r' || s[pos[0]] == '\t')) pos[0]++
    }

    private fun parseJsonStringAt(s: String, pos: IntArray) {
        if (pos[0] >= s.length || s[pos[0]] != '"') throw IllegalArgumentException("Expected string")
        pos[0]++
        while (true) {
            if (pos[0] >= s.length) throw IllegalArgumentException("Unterminated string")
            val c = s[pos[0]++]
            if (c == '"') return
            if (c == '\\') {
                if (pos[0] >= s.length) throw IllegalArgumentException("Bad escape")
                when (val e = s[pos[0]++]) {
                    '"', '\\', '/' -> Unit
                    'b', 'f', 'n', 'r', 't' -> Unit
                    'u' -> {
                        if (pos[0] + 4 > s.length) throw IllegalArgumentException("Bad unicode escape")
                        hex4(s.substring(pos[0], pos[0] + 4))
                        pos[0] += 4
                    }
                    else -> throw IllegalArgumentException("Bad escape: $e")
                }
            } else if (c < ' ') {
                throw IllegalArgumentException("Unescaped control in string")
            }
        }
    }

    private fun hex4(h: String) {
        if (h.length != 4 || h.toIntOrNull(16) == null) throw IllegalArgumentException("Bad unicode escape")
    }

    private fun parseJsonNumberAt(s: String, pos: IntArray) {
        val start = pos[0]
        if (pos[0] < s.length && s[pos[0]] == '-') pos[0]++
        if (pos[0] >= s.length) throw IllegalArgumentException("Bad number")
        if (s[pos[0]] == '0') {
            pos[0]++
        } else if (s[pos[0]] in '1'..'9') {
            while (pos[0] < s.length && s[pos[0]] in '0'..'9') pos[0]++
        } else {
            throw IllegalArgumentException("Bad number")
        }
        if (pos[0] < s.length && s[pos[0]] == '.') {
            pos[0]++
            if (pos[0] >= s.length || s[pos[0]] !in '0'..'9') throw IllegalArgumentException("Bad number")
            while (pos[0] < s.length && s[pos[0]] in '0'..'9') pos[0]++
        }
        if (pos[0] < s.length && (s[pos[0]] == 'e' || s[pos[0]] == 'E')) {
            pos[0]++
            if (pos[0] < s.length && (s[pos[0]] == '+' || s[pos[0]] == '-')) pos[0]++
            if (pos[0] >= s.length || s[pos[0]] !in '0'..'9') throw IllegalArgumentException("Bad number")
            while (pos[0] < s.length && s[pos[0]] in '0'..'9') pos[0]++
        }
        if (pos[0] == start) throw IllegalArgumentException("Bad number")
    }

    private fun parseJsonValueAt(s: String, pos: IntArray) {
        skipWs(s, pos)
        if (pos[0] >= s.length) throw IllegalArgumentException("Unexpected end of JSON")
        when (val c = s[pos[0]]) {
            '{' -> {
                pos[0]++
                skipWs(s, pos)
                if (pos[0] < s.length && s[pos[0]] == '}') {
                    pos[0]++
                    return
                }
                while (true) {
                    skipWs(s, pos)
                    parseJsonStringAt(s, pos)
                    skipWs(s, pos)
                    if (pos[0] >= s.length || s[pos[0]] != ':') throw IllegalArgumentException("Expected :")
                    pos[0]++
                    parseJsonValueAt(s, pos)
                    skipWs(s, pos)
                    if (pos[0] >= s.length) throw IllegalArgumentException("Unterminated object")
                    val d = s[pos[0]++]
                    if (d == '}') return
                    if (d != ',') throw IllegalArgumentException("Expected , or }")
                }
            }
            '[' -> {
                pos[0]++
                skipWs(s, pos)
                if (pos[0] < s.length && s[pos[0]] == ']') {
                    pos[0]++
                    return
                }
                while (true) {
                    parseJsonValueAt(s, pos)
                    skipWs(s, pos)
                    if (pos[0] >= s.length) throw IllegalArgumentException("Unterminated array")
                    val d = s[pos[0]++]
                    if (d == ']') return
                    if (d != ',') throw IllegalArgumentException("Expected , or ]")
                }
            }
            '"' -> parseJsonStringAt(s, pos)
            't' -> {
                if (!s.startsWith("true", pos[0])) throw IllegalArgumentException("Bad token")
                pos[0] += 4
            }
            'f' -> {
                if (!s.startsWith("false", pos[0])) throw IllegalArgumentException("Bad token")
                pos[0] += 5
            }
            'n' -> {
                if (!s.startsWith("null", pos[0])) throw IllegalArgumentException("Bad token")
                pos[0] += 4
            }
            else -> {
                if (c == '-' || c in '0'..'9') parseJsonNumberAt(s, pos)
                else throw IllegalArgumentException("Unexpected char: $c")
            }
        }
    }

    private fun checkJsonValid(json: String) {
        if (json.isBlank()) throw IllegalArgumentException("Empty JSON")
        val pos = intArrayOf(0)
        parseJsonValueAt(json, pos)
        skipWs(json, pos)
        if (pos[0] != json.length) throw IllegalArgumentException("Trailing characters in JSON")
    }

    fun jsonMinify(json: String): String {
        checkJsonValid(json)
        val sb = StringBuilder(json.length)
        var inString = false
        var escape = false
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
                ' ', '\n', '\r', '\t' -> Unit
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun jsonValidate(json: String): Boolean {
        return try {
            checkJsonValid(json)
            true
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: IndexOutOfBoundsException) {
            false
        }
    }

    fun jsonEscape(text: String): String {
        val sb = StringBuilder(text.length + 8)
        for (c in text) {
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

    // ---------- xml ----------

    private val xmlName = Regex("[A-Za-z_][A-Za-z0-9._:-]*")

    private fun xmlTagName(content: String): String {
        val t = content.trim().trimEnd('/')
        val end = t.indexOfFirst { it == ' ' || it == '\t' || it == '\n' || it == '\r' || it == '/' }
        return if (end < 0) t else t.substring(0, end)
    }

    fun xmlValidate(xml: String): Boolean {
        try {
            if (xml.isBlank()) return false
            val stack = ArrayDeque<String>()
            var found = false
            var i = 0
            while (i < xml.length) {
                if (xml[i] != '<') {
                    i++
                    continue
                }
                if (xml.startsWith("<!--", i)) {
                    val end = xml.indexOf("-->", i + 4)
                    if (end < 0) return false
                    i = end + 3
                    continue
                }
                if (xml.startsWith("<![CDATA[", i)) {
                    val end = xml.indexOf("]]>", i + 9)
                    if (end < 0) return false
                    i = end + 3
                    continue
                }
                if (xml.startsWith("<?", i)) {
                    val end = xml.indexOf("?>", i + 2)
                    if (end < 0) return false
                    i = end + 2
                    continue
                }
                if (xml.startsWith("<!DOCTYPE", i) || xml.startsWith("<!doctype", i)) {
                    val end = xml.indexOf('>', i + 2)
                    if (end < 0) return false
                    i = end + 1
                    continue
                }
                if (xml.startsWith("</", i)) {
                    val end = xml.indexOf('>', i + 2)
                    if (end < 0) return false
                    val name = xml.substring(i + 2, end).trim()
                    if (!xmlName.matches(name)) return false
                    if (stack.isEmpty() || stack.removeLast() != name) return false
                    i = end + 1
                    continue
                }
                val end = xml.indexOf('>', i + 1)
                if (end < 0) return false
                val content = xml.substring(i + 1, end)
                if (content.isBlank()) return false
                val selfClose = content.trimEnd().endsWith("/")
                val name = xmlTagName(content)
                if (!xmlName.matches(name)) return false
                if (!selfClose) stack.addLast(name)
                found = true
                i = end + 1
            }
            return found && stack.isEmpty()
        } catch (e: Exception) {
            return false
        }
    }

    fun xmlPretty(xml: String): String {
        if (!xmlValidate(xml)) throw IllegalArgumentException("Invalid XML")
        val lines = ArrayList<String>()
        var indent = 0
        fun push(tag: String) {
            lines.add("  ".repeat(indent) + tag.trim())
        }
        fun pushText(t: String) {
            val v = t.trim()
            if (v.isNotEmpty()) lines.add("  ".repeat(indent) + v)
        }
        var i = 0
        var textStart = 0
        while (i < xml.length) {
            if (xml[i] != '<') {
                i++
                continue
            }
            if (textStart < i) pushText(xml.substring(textStart, i))
            if (xml.startsWith("<!--", i)) {
                val end = xml.indexOf("-->", i + 4) + 3
                push(xml.substring(i, end))
                i = end
            } else if (xml.startsWith("<![CDATA[", i)) {
                val end = xml.indexOf("]]>", i + 9) + 3
                push(xml.substring(i, end))
                i = end
            } else if (xml.startsWith("<?", i)) {
                val end = xml.indexOf("?>", i + 2) + 2
                push(xml.substring(i, end))
                i = end
            } else if (xml.startsWith("</", i)) {
                val end = xml.indexOf('>', i + 2) + 1
                indent--
                push(xml.substring(i, end))
                i = end
            } else {
                val end = xml.indexOf('>', i + 1) + 1
                val content = xml.substring(i + 1, end - 1)
                val selfClose = content.trimEnd().endsWith("/")
                push(xml.substring(i, end))
                if (!selfClose) indent++
                i = end
            }
            textStart = i
        }
        if (textStart < xml.length) pushText(xml.substring(textStart))
        return lines.joinToString("\n")
    }

    // ---------- case ----------

    private fun splitCaseWords(text: String): List<String> {
        var s = text.trim()
        if (s.isEmpty()) return emptyList()
        s = s.replace(Regex("[_\\-]+"), " ")
        s = s.replace(Regex("[^A-Za-z0-9 ]+"), " ")
        s = Regex("(?<=[A-Z])(?=[A-Z][a-z])").replace(s, " ")
        s = Regex("(?<=[a-z0-9])(?=[A-Z])").replace(s, " ")
        return s.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    }

    fun caseSnake(text: String): String =
        splitCaseWords(text).joinToString("_") { it.lowercase() }

    fun caseKebab(text: String): String =
        splitCaseWords(text).joinToString("-") { it.lowercase() }

    fun caseCamel(text: String): String {
        val words = splitCaseWords(text)
        if (words.isEmpty()) return ""
        val first = words.first().lowercase()
        val rest = words.drop(1).map { w ->
            val low = w.lowercase()
            low.replaceFirstChar { it.uppercaseChar() }
        }
        return first + rest.joinToString("")
    }

    fun caseConstant(text: String): String =
        splitCaseWords(text).joinToString("_") { it.uppercase() }
}
