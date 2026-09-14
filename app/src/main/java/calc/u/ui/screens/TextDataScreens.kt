package calc.u.ui.screens

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import calc.u.core.TextData
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.security.MessageDigest
import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun TextDataScreen() {
    var tab by remember { mutableStateOf("hash") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                listOf(
                    "hash" to "Hash",
                    "base64" to "Base64",
                    "stats" to "Stats",
                    "qr" to "QR",
                    "uuid" to "UUID",
                    "case" to "Case",
                    "url" to "URL",
                    "morse" to "Morse",
                    "binary" to "Binary",
                    "json" to "JSON",
                    "regex" to "Regex",
                    "unix" to "Unix",
                    "totp" to "TOTP",
                    "cipher" to "Cipher",
                    "jwt" to "JWT",
                    "textplus" to "Text+",
                    "diff" to "Diff",
                    "csvjson" to "CSV/JSON",
                    "cron" to "Cron"
                )
            ) { (id, label) ->
                FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "base64" -> Base64Screen()
                "stats" -> TextStatsScreen()
                "qr" -> QrScreen()
                "uuid" -> UuidScreen()
                "case" -> CaseConverterCard()
                "url" -> UrlCodecCard()
                "morse" -> MorseCard()
                "binary" -> BinaryHexCard()
                "json" -> JsonFormatterCard()
                "regex" -> RegexTesterCard()
                "unix" -> UnixTimeCard()
                "totp" -> TotpCard()
                "cipher" -> CipherCard()
                "jwt" -> JwtCard()
                "textplus" -> TextPlusScreen()
                "diff" -> DiffScreen()
                "csvjson" -> CsvJsonScreen()
                "cron" -> CronMiscScreen()
                else -> HashScreen()
            }
        }
    }
}

private fun sha512Hex(text: String): String {
    try {
        val digest = MessageDigest.getInstance("SHA-512").digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    } catch (e: java.security.NoSuchAlgorithmException) {
        throw IllegalArgumentException("SHA-512 unavailable", e)
    }
}

private fun rot47(text: String): String {
    return text.map { c ->
        if (c in '!'..'~') (((c.code - 33 + 47) % 94) + 33).toChar() else c
    }.joinToString("")
}

private fun splitWords(text: String): List<String> {
    return text.trim().split(Regex("[^A-Za-z0-9]+")).filter { it.isNotEmpty() }
}

private fun toCamelCase(text: String): String {
    val parts = splitWords(text)
    if (parts.isEmpty()) return ""
    return parts.first().lowercase() + parts.drop(1).joinToString("") { p ->
        val l = p.lowercase()
        l.substring(0, 1).uppercase() + l.substring(1)
    }
}

private fun toSnakeCase(text: String): String {
    return splitWords(text).joinToString("_") { it.lowercase() }
}

private fun toKebabCase(text: String): String {
    return splitWords(text).joinToString("-") { it.lowercase() }
}

private fun hexValLocal(c: Char): Int {
    return when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> throw IllegalArgumentException("Bad hex char: $c")
    }
}

private fun hexToText(hex: String): String {
    val clean = hex.filter { !it.isWhitespace() }
    require(clean.isNotEmpty()) { "Hex input is empty" }
    require(clean.length % 2 == 0) { "Hex length must be even" }
    val bytes = ByteArray(clean.length / 2) { i ->
        ((hexValLocal(clean[i * 2]) shl 4) or hexValLocal(clean[i * 2 + 1])).toByte()
    }
    return bytes.toString(Charsets.UTF_8)
}

private fun jsonMinify(json: String): String {
    val sb = StringBuilder()
    var inStr = false
    var esc = false
    for (c in json) {
        if (inStr) {
            sb.append(c)
            if (esc) esc = false
            else if (c == '\\') esc = true
            else if (c == '"') inStr = false
        } else {
            if (c == '"') {
                inStr = true
                sb.append(c)
            } else if (!c.isWhitespace()) {
                sb.append(c)
            }
        }
    }
    return sb.toString()
}

private fun jsonErrorLine(input: String): Int {
    var depth = 0
    var inStr = false
    var esc = false
    val lines = input.split("\n")
    for ((idx, line) in lines.withIndex()) {
        for (c in line) {
            if (inStr) {
                if (esc) esc = false
                else if (c == '\\') esc = true
                else if (c == '"') inStr = false
            } else {
                when (c) {
                    '"' -> inStr = true
                    '{', '[' -> depth++
                    '}', ']' -> {
                        depth--
                        if (depth < 0) return idx + 1
                    }
                }
            }
        }
    }
    if (inStr || depth != 0) {
        val lastContent = lines.indexOfLast { it.isNotBlank() }
        return if (lastContent >= 0) lastContent + 1 else lines.size
    }
    return lines.size
}

private fun vigenere(text: String, key: String, encrypt: Boolean): String {
    val shifts = key.filter { it.isLetter() }.map { it.uppercaseChar() - 'A' }
    require(shifts.isNotEmpty()) { "Key must contain a letter" }
    var ki = 0
    return text.map { c ->
        when (c) {
            in 'A'..'Z' -> {
                val s = shifts[ki % shifts.size]
                ki++
                'A' + ((c - 'A' + (if (encrypt) s else 26 - s)) % 26)
            }
            in 'a'..'z' -> {
                val s = shifts[ki % shifts.size]
                ki++
                'a' + ((c - 'a' + (if (encrypt) s else 26 - s)) % 26)
            }
            else -> c
        }
    }.joinToString("")
}

// NOTE: this XOR-decrypt helper belongs in core TextData.kt as a tested pure
// function next to xorHex(); it lives here only because TextData.kt is read-only
// for this change set.
private fun xorHexDecryptToText(hex: String, key: String): String {
    val clean = hex.filter { !it.isWhitespace() }
    require(clean.isNotEmpty()) { "Hex input is empty" }
    require(clean.length % 2 == 0) { "Hex length must be even" }
    val kb = key.toByteArray(Charsets.UTF_8)
    require(kb.isNotEmpty()) { "Key must not be empty" }
    val bytes = ByteArray(clean.length / 2) { i ->
        ((hexValLocal(clean[i * 2]) shl 4) or hexValLocal(clean[i * 2 + 1])).toByte()
    }
    return bytes.mapIndexed { i, b -> (b.toInt() xor kb[i % kb.size].toInt()).toByte() }
        .toByteArray().toString(Charsets.UTF_8)
}

private fun splitCsvCells(line: String, delim: Char): List<String> {
    val cells = ArrayList<String>()
    val sb = StringBuilder()
    var inQ = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        if (c == '"') {
            if (inQ && i + 1 < line.length && line[i + 1] == '"') {
                sb.append('"')
                i += 2
            } else {
                inQ = !inQ
                sb.append(c)
                i++
            }
        } else if (c == delim && !inQ) {
            cells.add(sb.toString())
            sb.clear()
            i++
        } else {
            sb.append(c)
            i++
        }
    }
    cells.add(sb.toString())
    return cells
}

private fun unquoteCell(cell: String): String {
    val t = cell.trim()
    if (t.length >= 2 && t.startsWith('"') && t.endsWith('"')) {
        return t.substring(1, t.length - 1).replace("\"\"", "\"")
    }
    return cell
}

private fun csvEscapeLocal(v: String): String {
    return if (v.contains(',') || v.contains('"') || v.contains('\n') || v.contains('\r')) {
        "\"" + v.replace("\"", "\"\"") + "\""
    } else {
        v
    }
}

private fun withDelimiter(csv: String, delim: Char): String {
    if (delim == ',') return csv
    return csv.split("\n").joinToString("\n") { line ->
        if (line.isBlank()) line
        else splitCsvCells(line, delim).joinToString(",") { csvEscapeLocal(unquoteCell(it)) }
    }
}

private fun parseCronField(field: String, min: Int, max: Int): Set<Int> {
    val out = mutableSetOf<Int>()
    for (part in field.split(",")) {
        if (part.isEmpty()) throw IllegalArgumentException("Bad cron field: $field")
        var range = part
        var step = 1
        if (part.contains("/")) {
            val sp = part.split("/")
            if (sp.size != 2) throw IllegalArgumentException("Bad cron field: $field")
            range = sp[0]
            step = sp[1].toIntOrNull() ?: throw IllegalArgumentException("Bad cron field: $field")
            require(step > 0) { "Bad cron step: $field" }
        }
        val lo: Int
        val hi: Int
        if (range == "*") {
            lo = min
            hi = max
        } else if (range.contains("-")) {
            val ends = range.split("-")
            if (ends.size != 2) throw IllegalArgumentException("Bad cron field: $field")
            lo = ends[0].toIntOrNull() ?: throw IllegalArgumentException("Bad cron field: $field")
            hi = ends[1].toIntOrNull() ?: throw IllegalArgumentException("Bad cron field: $field")
        } else {
            lo = range.toIntOrNull() ?: throw IllegalArgumentException("Bad cron field: $field")
            hi = lo
        }
        require(lo in min..max && hi in min..max && lo <= hi) { "Cron value out of range: $field" }
        var v = lo
        while (v <= hi) {
            out.add(v)
            v += step
        }
    }
    if (out.isEmpty()) throw IllegalArgumentException("Bad cron field: $field")
    return out
}

private fun cronNextRuns(expr: String, fromSec: Long, count: Int): List<String> {
    val parts = expr.trim().split("\\s+".toRegex())
    require(parts.size == 5) { "Expected 5 fields: minute hour day month weekday" }
    val mins = parseCronField(parts[0], 0, 59)
    val hours = parseCronField(parts[1], 0, 23)
    val doms = parseCronField(parts[2], 1, 31)
    val mons = parseCronField(parts[3], 1, 12)
    val rawDow = parseCronField(parts[4], 0, 7)
    val dows = rawDow.map { if (it == 7) 0 else it }.toSet()
    val fullDom = (1..31).toSet()
    val fullDow = (0..6).toSet()
    val domRestr = doms != fullDom
    val dowRestr = dows != fullDow
    val zone = java.time.ZoneId.systemDefault()
    var cursor = (fromSec / 60 + 1) * 60
    val out = ArrayList<String>(count)
    var guard = 0
    while (out.size < count && guard < 366 * 24 * 60) {
        guard++
        val dt = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(cursor), zone)
        val domOk = dt.dayOfMonth in doms
        val dowOk = (dt.dayOfWeek.value % 7) in dows
        val dayOk = if (domRestr && dowRestr) domOk || dowOk else domOk && dowOk
        if (dt.minute in mins && dt.hour in hours && dt.monthValue in mons && dayOk) {
            out.add(runCatching { TextData.unixToDate(cursor) }.getOrDefault("$cursor"))
        }
        cursor += 60
    }
    if (out.size < count) throw IllegalArgumentException("No runs in next 366 days")
    return out
}

private fun formatUuidLocal(raw: String, hyphens: Boolean, upper: Boolean): String {
    var v = raw
    if (!hyphens) v = v.replace("-", "")
    if (upper) v = v.uppercase()
    return v
}

@Composable
private fun HashRow(label: String, value: String, clipboard: ClipboardManager) {
    ResultLine(label, value)
    OutlinedButton(
        onClick = { runCatching { clipboard.setText(AnnotatedString(value)) } },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Copy $label") }
}

@Composable
fun HashScreen() {
    var input by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val sha256 = remember(input) { runCatching { TextData.sha256(input) }.getOrDefault("—") }
    val sha512 = remember(input) { runCatching { sha512Hex(input) }.getOrDefault("—") }
    val sha1 = remember(input) { runCatching { TextData.sha1(input) }.getOrDefault("—") }
    val md5 = remember(input) { runCatching { TextData.md5(input) }.getOrDefault("—") }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Hash") {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Input") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (input.isEmpty()) {
                    Text(
                        "Enter text above — hashes appear for every row.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
                HashRow("SHA-256", sha256, clipboard)
                HashRow("SHA-512", sha512, clipboard)
                HashRow("SHA-1", sha1, clipboard)
                HashRow("MD5", md5, clipboard)
            }
        }
    }
}

@Composable
fun Base64Screen() {
    var input by remember { mutableStateOf("") }
    var encode by remember { mutableStateOf(true) }
    var urlSafe by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val res = remember(input, encode, urlSafe) {
        runCatching {
            if (encode) {
                if (urlSafe) Base64.getUrlEncoder().encodeToString(input.toByteArray(Charsets.UTF_8))
                else TextData.base64Encode(input)
            } else {
                if (urlSafe) Base64.getUrlDecoder().decode(input).toString(Charsets.UTF_8)
                else TextData.base64Decode(input)
            }
        }
    }
    val output = res.getOrNull() ?: ""
    val errMsg = res.exceptionOrNull()?.let { "invalid Base64: ${it.message ?: "decode failed"}" }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Base64") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = encode, onClick = { encode = true }, label = { Text("Encode") }) }
                    item { FilterChip(selected = !encode, onClick = { encode = false }, label = { Text("Decode") }) }
                    item { FilterChip(selected = urlSafe, onClick = { urlSafe = !urlSafe }, label = { Text("URL-safe") }) }
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(if (encode) "Plain text" else "Base64") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                if (errMsg != null) {
                    Text(errMsg, color = MaterialTheme.colorScheme.error)
                } else {
                    ResultLine("Result", if (output.isEmpty()) "—" else output)
                }
                Button(onClick = { runCatching { clipboard.setText(AnnotatedString(output)) } }) { Text("Copy") }
            }
        }
    }
}

@Composable
fun TextStatsScreen() {
    var input by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val counts = remember(input) { runCatching { TextData.counts(input) }.getOrDefault(Triple(0, 0, 0)) }
    val extra = remember(input) {
        runCatching {
            val sentences = input.split(Regex("[.!?…]+")).count { it.isNotBlank() }
            val paragraphs = input.split(Regex("\n\\s*\n")).count { it.isNotBlank() }
            val charsNoSpaces = input.count { !it.isWhitespace() }
            val words = if (input.isBlank()) emptyList() else input.trim().split("\\s+".toRegex())
            val unique = words.map { it.lowercase() }.toSet().size
            val totalSecs = ceil(words.size * 60.0 / 200.0).toLong()
            Triple(sentences to paragraphs, charsNoSpaces to unique, totalSecs)
        }.getOrDefault(Triple(0 to 0, 0 to 0, 0L))
    }
    val sentences = extra.first.first
    val paragraphs = extra.first.second
    val charsNoSpaces = extra.second.first
    val uniqueWords = extra.second.second
    val readSecs = extra.third
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Text stats") {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                HorizontalDivider()
                ResultLine("Words", "${counts.first}")
                ResultLine("Chars", "${counts.second}")
                ResultLine("Lines", "${counts.third}")
                ResultLine("Chars (no spaces)", "$charsNoSpaces")
                ResultLine("Sentences", "$sentences")
                ResultLine("Paragraphs", "$paragraphs")
                ResultLine("Unique words", "$uniqueWords")
                ResultLine("Reading time", "${readSecs / 60} min ${readSecs % 60} s @200wpm")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            runCatching {
                                clipboard.setText(
                                    AnnotatedString(
                                        "Words: ${counts.first}\nChars: ${counts.second}\nLines: ${counts.third}\n" +
                                            "Chars (no spaces): $charsNoSpaces\nSentences: $sentences\n" +
                                            "Paragraphs: $paragraphs\nUnique words: $uniqueWords"
                                    )
                                )
                            }
                        }
                    ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy stats") }
                }
            }
        }
    }
}

@Composable
fun QrScreen() {
    var input by remember { mutableStateOf("") }
    var qrSize by remember { mutableStateOf(512) }
    var ecName by remember { mutableStateOf("M") }
    var status by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val ecLevel = remember(ecName) {
        when (ecName) {
            "L" -> ErrorCorrectionLevel.L
            "Q" -> ErrorCorrectionLevel.Q
            "H" -> ErrorCorrectionLevel.H
            else -> ErrorCorrectionLevel.M
        }
    }
    val bmpRes = remember(input, qrSize, ecLevel) {
        if (input.isBlank()) Result.success<Bitmap?>(null)
        else runCatching {
            val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ecLevel, EncodeHintType.MARGIN to 2)
            val m = QRCodeWriter().encode(input, BarcodeFormat.QR_CODE, qrSize, qrSize, hints)
            require(m.width > 0 && m.height > 0 && m.width <= 2048 && m.height <= 2048) { "Invalid QR size" }
            val size = m.width * m.height
            require(size > 0 && size <= 2048 * 2048) { "Invalid QR size" }
            val bmp = Bitmap.createBitmap(m.width, m.height, Bitmap.Config.ARGB_8888)
            val px = IntArray(size)
            for (y in 0 until m.height) {
                for (x in 0 until m.width) {
                    px[y * m.width + x] = if (m.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                }
            }
            bmp.setPixels(px, 0, m.width, 0, 0, m.width, m.height)
            bmp as Bitmap?
        }
    }
    val bmp = bmpRes.getOrNull()
    val errMsg = bmpRes.exceptionOrNull()?.let { it.message ?: "QR encode failed" }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("QR code") {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = qrSize == 256, onClick = { qrSize = 256 }, label = { Text("256") }) }
                    item { FilterChip(selected = qrSize == 512, onClick = { qrSize = 512 }, label = { Text("512") }) }
                    item { FilterChip(selected = qrSize == 1024, onClick = { qrSize = 1024 }, label = { Text("1024") }) }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("L", "M", "Q", "H")) { ec ->
                        FilterChip(selected = ecName == ec, onClick = { ecName = ec }, label = { Text(ec) })
                    }
                }
                HorizontalDivider()
                if (errMsg != null) {
                    Text("Content over capacity: $errMsg", color = MaterialTheme.colorScheme.error)
                } else if (bmp == null) {
                    ResultLine("Preview", "—")
                } else {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "QR for $input",
                            modifier = Modifier.size(256.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            Button(
                                onClick = {
                                    runCatching {
                                        val values = ContentValues().apply {
                                            put(
                                                MediaStore.Images.Media.DISPLAY_NAME,
                                                "qrcode_${System.currentTimeMillis()}.png"
                                            )
                                            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                                        }
                                        val uri = context.contentResolver.insert(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values
                                        ) ?: throw IllegalArgumentException("Gallery unavailable")
                                        context.contentResolver.openOutputStream(uri)?.use { out ->
                                            if (!bmp.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                                                throw IllegalArgumentException("Compress failed")
                                            }
                                        } ?: throw IllegalArgumentException("Gallery unavailable")
                                    }.onSuccess { status = "Saved to gallery" }
                                        .onFailure { status = "Save failed: ${it.message ?: "unknown"}" }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Save") }
                        }
                        Box(Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        val values = ContentValues().apply {
                                            put(
                                                MediaStore.Images.Media.DISPLAY_NAME,
                                                "qrcode_${System.currentTimeMillis()}.png"
                                            )
                                            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                                        }
                                        val uri = context.contentResolver.insert(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values
                                        ) ?: throw IllegalArgumentException("Share unavailable")
                                        context.contentResolver.openOutputStream(uri)?.use { out ->
                                            if (!bmp.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                                                throw IllegalArgumentException("Compress failed")
                                            }
                                        } ?: throw IllegalArgumentException("Share unavailable")
                                        val send = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(send, "Share QR"))
                                    }.onSuccess { status = "Share sheet opened" }
                                        .onFailure { status = "Share failed: ${it.message ?: "unknown"}" }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Share") }
                        }
                    }
                    if (status != null) {
                        Text(
                            status ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UuidScreen() {
    var bulk by remember { mutableStateOf(1) }
    var version by remember { mutableStateOf("v4") }
    var hyphens by remember { mutableStateOf(true) }
    var upper by remember { mutableStateOf(false) }
    var nonce by remember { mutableStateOf(0) }
    val clipboard = LocalClipboardManager.current
    val values = remember(bulk, version, hyphens, upper, nonce) {
        runCatching {
            List(bulk) {
                val raw = when (version) {
                    "v1" -> TextData.uuidV1()
                    "v7" -> TextData.uuidV7()
                    else -> TextData.uuid()
                }
                formatUuidLocal(raw, hyphens, upper)
            }
        }.getOrDefault(emptyList())
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("UUID") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = bulk == 1, onClick = { bulk = 1 }, label = { Text("1") }) }
                    item { FilterChip(selected = bulk == 5, onClick = { bulk = 5 }, label = { Text("5") }) }
                    item { FilterChip(selected = bulk == 10, onClick = { bulk = 10 }, label = { Text("10") }) }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = version == "v4", onClick = { version = "v4" }, label = { Text("v4") }) }
                    item { FilterChip(selected = version == "v1", onClick = { version = "v1" }, label = { Text("v1") }) }
                    item { FilterChip(selected = version == "v7", onClick = { version = "v7" }, label = { Text("v7") }) }
                    item { FilterChip(selected = hyphens, onClick = { hyphens = !hyphens }, label = { Text("Hyphens") }) }
                    item { FilterChip(selected = upper, onClick = { upper = !upper }, label = { Text("Uppercase") }) }
                }
                HorizontalDivider()
                if (values.isEmpty()) {
                    ResultLine("Value", "—")
                } else {
                    values.forEachIndexed { i, v ->
                        Text(
                            "${i + 1}. $v",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = { nonce++ },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Regenerate") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    clipboard.setText(AnnotatedString(values.joinToString("\n")))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Copy all") }
                    }
                }
            }
        }
    }
}

@Composable
fun CaseConverterCard() {
    var input by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val upper = remember(input) { runCatching { TextData.toUpper(input) }.getOrDefault(input) }
    val lower = remember(input) { runCatching { TextData.toLower(input) }.getOrDefault(input) }
    val title = remember(input) { runCatching { TextData.titleCase(input) }.getOrDefault(input) }
    val camel = remember(input) { runCatching { toCamelCase(input) }.getOrDefault(input) }
    val snake = remember(input) { runCatching { toSnakeCase(input) }.getOrDefault(input) }
    val kebab = remember(input) { runCatching { toKebabCase(input) }.getOrDefault(input) }
    SectionCard("Case converter") {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Text") },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        ResultLine("UPPER", upper)
        ResultLine("lower", lower)
        ResultLine("Title", title)
        ResultLine("camelCase", camel)
        ResultLine("snake_case", snake)
        ResultLine("kebab-case", kebab)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = {
                    runCatching {
                        clipboard.setText(
                            AnnotatedString("$upper\n$lower\n$title\n$camel\n$snake\n$kebab")
                        )
                    }
                }
            ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy case results") }
        }
        Text(
            "Turkish-i note: UPPER/lower use the default locale — dotted İ / dotless ı need a tr locale pass (out of scope here).",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UrlCodecCard() {
    var input by remember { mutableStateOf("") }
    var decode by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val output = remember(input, decode) {
        runCatching {
            if (decode) TextData.urlDecode(input) else TextData.urlEncode(input)
        }.getOrDefault("—")
    }
    SectionCard("URL codec") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = !decode, onClick = { decode = false }, label = { Text("Encode") }) }
            item { FilterChip(selected = decode, onClick = { decode = true }, label = { Text("Decode") }) }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(if (decode) "Encoded" else "Plain text") },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        ResultLine("Result", output)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = { runCatching { clipboard.setText(AnnotatedString(output)) } }
            ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy URL result") }
        }
    }
}

@Composable
fun MorseCard() {
    var plain by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var auto by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val encRes = remember(plain) { runCatching { TextData.morseEncode(plain) } }
    val decRes = remember(code) { runCatching { TextData.morseDecode(code) } }
    val autoRes = remember(auto) {
        if (auto.isBlank()) Result.success("—")
        else runCatching {
            val t = auto.trim()
            val looksMorse = t.any { it == '.' || it == '-' } &&
                t.all { it == '.' || it == '-' || it == '/' || it.isWhitespace() }
            if (looksMorse) TextData.morseDecode(t) else TextData.morseEncode(t)
        }
    }
    SectionCard("Morse") {
        OutlinedTextField(
            value = plain,
            onValueChange = { plain = it },
            label = { Text("Text to encode") },
            modifier = Modifier.fillMaxWidth()
        )
        val encErr = encRes.exceptionOrNull()?.message
        if (encErr != null) {
            Text("Cannot encode: $encErr", color = MaterialTheme.colorScheme.error)
        } else {
            ResultLine("Encoded", encRes.getOrNull()?.ifEmpty { "—" } ?: "—")
        }
        HorizontalDivider()
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Code to decode") },
            modifier = Modifier.fillMaxWidth()
        )
        val decErr = decRes.exceptionOrNull()?.message
        if (decErr != null) {
            Text("Cannot decode: $decErr", color = MaterialTheme.colorScheme.error)
        } else {
            ResultLine("Decoded", decRes.getOrNull()?.ifEmpty { "—" } ?: "—")
        }
        HorizontalDivider()
        OutlinedTextField(
            value = auto,
            onValueChange = { auto = it },
            label = { Text("Auto-detect (text or .-/)") },
            modifier = Modifier.fillMaxWidth()
        )
        val autoErr = autoRes.exceptionOrNull()?.message
        if (autoErr != null) {
            Text("Auto failed: $autoErr", color = MaterialTheme.colorScheme.error)
        } else {
            ResultLine("Auto", autoRes.getOrNull()?.ifEmpty { "—" } ?: "—")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = {
                    runCatching {
                        clipboard.setText(
                            AnnotatedString(
                                (encRes.getOrNull()?.ifEmpty { "—" } ?: "—") + "\n" +
                                    (decRes.getOrNull()?.ifEmpty { "—" } ?: "—") + "\n" +
                                    (autoRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                            )
                        )
                    }
                }
            ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Morse results") }
        }
    }
}

@Composable
fun BinaryHexCard() {
    var text by remember { mutableStateOf("") }
    var bin by remember { mutableStateOf("") }
    var hexIn by remember { mutableStateOf("") }
    var spaced by remember { mutableStateOf(true) }
    val clipboard = LocalClipboardManager.current
    val binRes = remember(text, spaced) {
        runCatching {
            val raw = TextData.textToBinary(text)
            if (spaced) raw else raw.replace(" ", "")
        }
    }
    val textRes = remember(bin, spaced) {
        runCatching {
            if (spaced) TextData.binaryToText(bin)
            else {
                val clean = bin.filter { !it.isWhitespace() }
                require(clean.isNotEmpty()) { "Binary input is empty" }
                require(clean.length % 8 == 0) { "Length must be a multiple of 8 when unspaced" }
                require(clean.all { it == '0' || it == '1' }) { "Only 0/1 allowed" }
                clean.chunked(8).map { tok ->
                    try {
                        tok.toInt(2).toChar().toString()
                    } catch (e: Exception) {
                        throw IllegalArgumentException("Bad binary token: $tok", e)
                    }
                }.joinToString("")
            }
        }
    }
    val hexOutRes = remember(text) { runCatching { TextData.textToHex(text) } }
    val hexTextRes = remember(hexIn) {
        if (hexIn.isBlank()) Result.success("—")
        else runCatching { hexToText(hexIn) }
    }
    SectionCard("Binary / Hex") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = spaced, onClick = { spaced = true }, label = { Text("Space") }) }
            item { FilterChip(selected = !spaced, onClick = { spaced = false }, label = { Text("None") }) }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text") },
            modifier = Modifier.fillMaxWidth()
        )
        val binErr = binRes.exceptionOrNull()?.message
        if (binErr != null) Text(binErr, color = MaterialTheme.colorScheme.error)
        else ResultLine("Binary", binRes.getOrNull()?.ifEmpty { "—" } ?: "—")
        val hexOutErr = hexOutRes.exceptionOrNull()?.message
        if (hexOutErr != null) Text(hexOutErr, color = MaterialTheme.colorScheme.error)
        else ResultLine("Hex", hexOutRes.getOrNull()?.ifEmpty { "—" } ?: "—")
        HorizontalDivider()
        OutlinedTextField(
            value = bin,
            onValueChange = { bin = it },
            label = { Text("Binary to decode") },
            modifier = Modifier.fillMaxWidth()
        )
        val textErr = textRes.exceptionOrNull()?.message
        if (textErr != null) Text(textErr, color = MaterialTheme.colorScheme.error)
        else ResultLine("Text", textRes.getOrNull()?.ifEmpty { "—" } ?: "—")
        HorizontalDivider()
        OutlinedTextField(
            value = hexIn,
            onValueChange = { hexIn = it },
            label = { Text("Hex to decode") },
            modifier = Modifier.fillMaxWidth()
        )
        val hexTextErr = hexTextRes.exceptionOrNull()?.message
        if (hexTextErr != null) Text(hexTextErr, color = MaterialTheme.colorScheme.error)
        else ResultLine("Hex→Text", hexTextRes.getOrNull()?.ifEmpty { "—" } ?: "—")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = {
                    runCatching {
                        clipboard.setText(
                            AnnotatedString(
                                (binRes.getOrNull()?.ifEmpty { "—" } ?: "—") + "\n" +
                                    (hexOutRes.getOrNull()?.ifEmpty { "—" } ?: "—") + "\n" +
                                    (textRes.getOrNull()?.ifEmpty { "—" } ?: "—") + "\n" +
                                    (hexTextRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                            )
                        )
                    }
                }
            ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy binary/hex results") }
        }
    }
}

@Composable
fun JsonFormatterCard() {
    var input by remember { mutableStateOf("") }
    var minify by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val res = remember(input, minify) {
        if (input.isBlank()) Result.success("")
        else runCatching {
            TextData.jsonPretty(input)
            if (minify) jsonMinify(input) else TextData.jsonPretty(input)
        }
    }
    val output = res.getOrNull() ?: ""
    val errMsg = res.exceptionOrNull()?.let {
        val line = jsonErrorLine(input)
        val total = input.split("\n").size
        "Invalid JSON (line $line of $total): ${it.message ?: "parse failed"}"
    }
    SectionCard("JSON formatter") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = !minify, onClick = { minify = false }, label = { Text("Pretty") }) }
            item { FilterChip(selected = minify, onClick = { minify = true }, label = { Text("Minify") }) }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("JSON") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        HorizontalDivider()
        if (errMsg != null) {
            Text(errMsg, color = MaterialTheme.colorScheme.error)
        } else {
            ResultLine(if (minify) "Minified" else "Pretty", if (output.isEmpty()) "—" else output)
            Button(onClick = { runCatching { clipboard.setText(AnnotatedString(output)) } }) { Text("Copy") }
        }
    }
}

@Composable
fun RegexTesterCard() {
    var pattern by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var flagI by remember { mutableStateOf(false) }
    var flagM by remember { mutableStateOf(false) }
    var flagS by remember { mutableStateOf(false) }
    val res = remember(pattern, input, flagI, flagM, flagS) {
        if (pattern.isEmpty() || input.isEmpty()) Result.success(emptyList<MatchResult>())
        else runCatching {
            val opts = buildSet {
                if (flagI) add(RegexOption.IGNORE_CASE)
                if (flagM) add(RegexOption.MULTILINE)
                if (flagS) add(RegexOption.DOT_MATCHES_ALL)
            }
            Regex(pattern, opts).findAll(input).toList()
        }
    }
    val matches = res.getOrNull() ?: emptyList()
    val errMsg = res.exceptionOrNull()?.message
    SectionCard("Regex tester") {
        OutlinedTextField(
            value = pattern,
            onValueChange = { pattern = it },
            label = { Text("Pattern") },
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = flagI, onClick = { flagI = !flagI }, label = { Text("i") }) }
            item { FilterChip(selected = flagM, onClick = { flagM = !flagM }, label = { Text("m") }) }
            item { FilterChip(selected = flagS, onClick = { flagS = !flagS }, label = { Text("s") }) }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Input") },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        if (errMsg != null) {
            Text(errMsg, color = MaterialTheme.colorScheme.error)
        } else {
            ResultLine("Match count", "${matches.size}")
            if (matches.isEmpty()) {
                ResultLine("Matches", "—")
            } else {
                matches.forEachIndexed { i, m ->
                    val groups = m.groupValues.drop(1)
                    Text(
                        "${i + 1}: '${m.value}' groups=[${groups.joinToString(", ") { "'$it'" }}]",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
    }
}

@Composable
fun UnixTimeCard() {
    var now by remember { mutableStateOf(runCatching { TextData.unixNow() }.getOrDefault(0L)) }
    var millis by remember { mutableStateOf(false) }
    var auto by remember { mutableStateOf(true) }
    var input by remember { mutableStateOf("") }
    var revInput by remember { mutableStateOf("") }
    LaunchedEffect(auto) {
        while (auto) {
            delay(1000L)
            now = runCatching { TextData.unixNow() }.getOrDefault(now)
        }
    }
    val zoneId = remember { runCatching { java.time.ZoneId.systemDefault().id }.getOrDefault("system") }
    val shownNow = if (millis) now * 1000L else now
    val convRes = remember(input, millis) {
        if (input.isBlank()) Result.success("—")
        else runCatching {
            val raw = input.trim().toLongOrNull() ?: throw IllegalArgumentException("Not a number: ${input.trim()}")
            TextData.unixToDate(if (millis) raw / 1000L else raw)
        }
    }
    val revRes = remember(revInput, millis) {
        if (revInput.isBlank()) Result.success("—")
        else runCatching {
            val ldt = try {
                java.time.LocalDateTime.parse(
                    revInput.trim(),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
            } catch (e: java.time.format.DateTimeParseException) {
                throw IllegalArgumentException("Use yyyy-MM-dd HH:mm:ss", e)
            }
            val sec = ldt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
            "${if (millis) sec * 1000L else sec}"
        }
    }
    SectionCard("Unix time") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = !millis, onClick = { millis = false }, label = { Text("Seconds") }) }
            item { FilterChip(selected = millis, onClick = { millis = true }, label = { Text("Millis") }) }
            item { FilterChip(selected = auto, onClick = { auto = !auto }, label = { Text("Auto Now") }) }
        }
        Text(
            "Timezone: $zoneId",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ResultLine("Now", "$shownNow")
        Button(onClick = { runCatching { TextData.unixNow() }.onSuccess { now = it } }) { Text("Refresh now") }
        HorizontalDivider()
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(if (millis) "Timestamp (millis)" else "Timestamp (seconds)") },
            modifier = Modifier.fillMaxWidth()
        )
        val convErr = convRes.exceptionOrNull()?.message
        if (convErr != null) Text(convErr, color = MaterialTheme.colorScheme.error)
        else ResultLine("Date", convRes.getOrNull() ?: "—")
        HorizontalDivider()
        OutlinedTextField(
            value = revInput,
            onValueChange = { revInput = it },
            label = { Text("Date (yyyy-MM-dd HH:mm:ss)") },
            modifier = Modifier.fillMaxWidth()
        )
        val revErr = revRes.exceptionOrNull()?.message
        if (revErr != null) Text(revErr, color = MaterialTheme.colorScheme.error)
        else ResultLine("Timestamp", revRes.getOrNull() ?: "—")
    }
}

@Composable
fun TotpCard() {
    var secret by remember { mutableStateOf("") }
    var digits by remember { mutableStateOf(6) }
    var periodSel by remember { mutableStateOf(30L) }
    var nowSec by remember { mutableStateOf(runCatching { System.currentTimeMillis() / 1000L }.getOrDefault(0L)) }
    LaunchedEffect(Unit) {
        while (true) {
            try {
                delay(1000L)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                break
            }
            nowSec = runCatching { System.currentTimeMillis() / 1000L }.getOrDefault(nowSec)
        }
    }
    val codeRes = remember(secret, nowSec, digits, periodSel) {
        if (secret.isBlank()) Result.success("—")
        else runCatching { calc.u.core.Totp.totp(secret, nowSec, periodSel, digits) }
    }
    val code = codeRes.getOrNull() ?: "—"
    val secretErr = codeRes.exceptionOrNull()?.message
    val remaining = remember(nowSec, periodSel) {
        runCatching { calc.u.core.Totp.secondsRemaining(nowSec, periodSel) }.getOrDefault(periodSel)
    }
    val uri = remember(secret, digits, periodSel) {
        val clean = secret.trim().replace(" ", "").replace("-", "").uppercase()
        "otpauth://totp/CalcU?secret=$clean&issuer=CalcU&digits=$digits&period=$periodSel"
    }
    val clipboard = LocalClipboardManager.current
    SectionCard("Authenticator (TOTP)") {
        OutlinedTextField(
            value = secret,
            onValueChange = { secret = it },
            label = { Text("Base32 secret") },
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = digits == 6, onClick = { digits = 6 }, label = { Text("6 digits") }) }
            item { FilterChip(selected = digits == 8, onClick = { digits = 8 }, label = { Text("8 digits") }) }
            item { FilterChip(selected = periodSel == 30L, onClick = { periodSel = 30L }, label = { Text("30s") }) }
            item { FilterChip(selected = periodSel == 60L, onClick = { periodSel = 60L }, label = { Text("60s") }) }
        }
        HorizontalDivider()
        if (secretErr != null) {
            Text("Invalid secret: $secretErr", color = MaterialTheme.colorScheme.error)
        } else {
            ResultLine("Code", code)
        }
        ResultLine("Expires in", "$remaining s")
        androidx.compose.material3.LinearProgressIndicator(
            progress = { remaining.coerceIn(0L, periodSel).toFloat() / periodSel.coerceAtLeast(1L).toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        ResultLine("Provisioning URI", if (secret.isBlank()) "—" else uri)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                Button(
                    onClick = { runCatching { clipboard.setText(AnnotatedString(code)) } },
                    enabled = code != "—" && secretErr == null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Copy code") }
            }
            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { runCatching { clipboard.setText(AnnotatedString(uri)) } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Copy URI") }
            }
        }
    }
}

@Composable
fun CipherCard() {
    var input by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("caesar") }
    var encrypt by remember { mutableStateOf(true) }
    var shift by remember { mutableStateOf("3") }
    var key by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val res = remember(input, mode, encrypt, shift, key) {
        runCatching {
            when (mode) {
                "caesar" -> {
                    val s = shift.trim().toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid shift: '${shift.trim()}' is not an integer")
                    TextData.caesar(input, s, encrypt)
                }
                "vigenere" -> vigenere(input, key, encrypt)
                else -> if (encrypt) {
                    TextData.xorHex(input, key)
                } else {
                    xorHexDecryptToText(input, key)
                }
            }
        }
    }
    val output = res.getOrNull() ?: ""
    val errMsg = res.exceptionOrNull()?.message
    SectionCard("Cipher") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = mode == "caesar", onClick = { mode = "caesar" }, label = { Text("Caesar") }) }
            item { FilterChip(selected = mode == "vigenere", onClick = { mode = "vigenere" }, label = { Text("Vigenère") }) }
            item { FilterChip(selected = mode == "xor", onClick = { mode = "xor" }, label = { Text("XOR") }) }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = encrypt, onClick = { encrypt = true }, label = { Text("Encrypt") }) }
            item { FilterChip(selected = !encrypt, onClick = { encrypt = false }, label = { Text("Decrypt") }) }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(if (mode == "xor" && !encrypt) "Hex input" else "Text") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        if (mode == "caesar") {
            OutlinedTextField(
                value = shift,
                onValueChange = { shift = it },
                label = { Text("Shift") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Key") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (mode == "xor") {
            Text(
                "Warning: raw XOR with a repeated password is not a KDF — use PBKDF2/HKDF from the Crypto set for real keys.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider()
        if (errMsg != null) {
            Text(errMsg, color = MaterialTheme.colorScheme.error)
        } else {
            ResultLine("Result", if (output.isEmpty()) "—" else output)
        }
        Button(onClick = { runCatching { clipboard.setText(AnnotatedString(output)) } }) { Text("Copy") }
    }
}

@Composable
fun JwtCard() {
    var input by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val res = remember(input) {
        runCatching {
            val parts = input.trim().split(".")
            require(parts.size == 3) { "need header.payload.signature" }
            Triple(
                prettyJsonLocal(jwtPartLocal(parts[0])),
                prettyJsonLocal(jwtPartLocal(parts[1])),
                parts[2]
            )
        }
    }
    val errMsg = res.exceptionOrNull()?.let { "invalid token: ${it.message ?: "decode failed"}" }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("JWT") {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("header.payload.signature") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                val decoded = res.getOrNull()
                if (input.isBlank()) {
                    ResultLine("Result", "—")
                } else if (errMsg != null || decoded == null) {
                    Text(errMsg ?: "decode failed", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Header", style = MaterialTheme.typography.titleSmall)
                    Text(
                        decoded.first,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    Text("Payload", style = MaterialTheme.typography.titleSmall)
                    Text(
                        decoded.second,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    ResultLine("Signature", decoded.third.ifBlank { "—" })
                    Text(
                        "Decode only — signature is not verified.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            runCatching {
                                clipboard.setText(AnnotatedString(decoded.first + "\n" + decoded.second))
                            }
                        }
                    ) { Text("Copy JSON") }
                }
            }
        }
    }
}

private fun jwtPartLocal(part: String): String {
    val padded = part + "=".repeat((4 - part.length % 4) % 4)
    return Base64.getUrlDecoder().decode(padded).toString(Charsets.UTF_8)
}

private fun prettyJsonLocal(raw: String): String = runCatching {
    val el = Json.parseToJsonElement(raw)
    Json { prettyPrint = true }.encodeToString(JsonElement.serializer(), el)
}.getOrDefault(raw)

@Composable
fun TextPlusScreen() {
    var rot by remember { mutableStateOf("") }
    var rot47Sel by remember { mutableStateOf(false) }
    var slug by remember { mutableStateOf("") }
    var palin by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val rotOut = remember(rot, rot47Sel) {
        runCatching { if (rot47Sel) rot47(rot) else TextData.rot13(rot) }.getOrDefault("—")
    }
    val slugOut = remember(slug) { runCatching { TextData.slugify(slug) }.getOrDefault("—") }
    val palinOut = remember(palin) {
        runCatching { if (TextData.isPalindrome(palin)) "Palindrome" else "Not a palindrome" }.getOrDefault("—")
    }
    val emailOut = remember(email) { runCatching { TextData.extractEmails(email) }.getOrDefault(emptyList()) }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard(if (rot47Sel) "ROT47" else "ROT13") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = !rot47Sel, onClick = { rot47Sel = false }, label = { Text("ROT13") }) }
                    item { FilterChip(selected = rot47Sel, onClick = { rot47Sel = true }, label = { Text("ROT47") }) }
                }
                OutlinedTextField(
                    value = rot,
                    onValueChange = { rot = it },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                HorizontalDivider()
                ResultLine("Result", rotOut.ifEmpty { "—" })
            }
        }
        item {
            SectionCard("Slug") {
                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Rules: lowercase, every [^a-z0-9]+ run becomes '-', leading/trailing '-' trimmed.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                ResultLine("Slug", slugOut.ifEmpty { "—" })
            }
        }
        item {
            SectionCard("Palindrome") {
                OutlinedTextField(
                    value = palin,
                    onValueChange = { palin = it },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Rules: case-insensitive, only letters and digits count — spaces and punctuation ignored.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                ResultLine("Check", palinOut)
            }
        }
        item {
            SectionCard("Emails") {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                HorizontalDivider()
                ResultLine("Count", "${emailOut.size}")
                ResultLine("Found", if (emailOut.isEmpty()) "—" else emailOut.joinToString(", "))
                Button(
                    onClick = { runCatching { clipboard.setText(AnnotatedString(emailOut.joinToString("\n"))) } }
                ) { Text("Copy emails") }
            }
        }
    }
}

@Composable
fun DiffScreen() {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var ignoreWs by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val norm = remember(a, b, ignoreWs) {
        if (!ignoreWs) a to b
        else {
            fun sq(s: String) = s.split("\n").joinToString("\n") { it.trim().replace("\\s+".toRegex(), " ") }
            sq(a) to sq(b)
        }
    }
    val diffRes = remember(norm) {
        runCatching { TextData.diffLines(norm.first, norm.second).joinToString("\n") }
    }
    val diff = diffRes.getOrNull() ?: ""
    val errMsg = diffRes.exceptionOrNull()?.message
    val numbered = remember(diff) {
        if (diff.isEmpty()) ""
        else diff.split("\n").mapIndexed { i, l -> "%4d %s".format(i + 1, l) }.joinToString("\n")
    }
    val scrollV = rememberScrollState()
    val scrollH = rememberScrollState()
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Line diff") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = ignoreWs,
                            onClick = { ignoreWs = !ignoreWs },
                            label = { Text("Ignore whitespace") }
                        )
                    }
                }
                OutlinedTextField(
                    value = a,
                    onValueChange = { a = it },
                    label = { Text("Original") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = b,
                    onValueChange = { b = it },
                    label = { Text("Modified") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                HorizontalDivider()
                if (errMsg != null) {
                    Text(errMsg, color = MaterialTheme.colorScheme.error)
                } else if (numbered.isEmpty()) {
                    ResultLine("Diff", "—")
                } else {
                    Box(Modifier.fillMaxWidth().horizontalScroll(scrollH)) {
                        Text(
                            numbered,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.verticalScroll(scrollV)
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(
                            onClick = { runCatching { clipboard.setText(AnnotatedString(diff)) } }
                        ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy diff") }
                    }
                }
            }
        }
    }
}

@Composable
fun CsvJsonScreen() {
    var csv by remember { mutableStateOf("") }
    var jsonIn by remember { mutableStateOf("") }
    var csvT by remember { mutableStateOf("") }
    var sortIn by remember { mutableStateOf("") }
    var cmpA by remember { mutableStateOf("") }
    var cmpB by remember { mutableStateOf("") }
    var delimName by remember { mutableStateOf(",") }
    val clipboard = LocalClipboardManager.current
    val delim = remember(delimName) {
        when (delimName) {
            ";" -> ';'
            "tab" -> '\t'
            "|" -> '|'
            else -> ','
        }
    }
    val csvJsonRes = remember(csv, delim) { runCatching { TextData.csvToJson(withDelimiter(csv, delim)) } }
    val jsonCsvRes = remember(jsonIn) { runCatching { TextData.jsonToCsv(jsonIn) } }
    val transposedRes = remember(csvT, delim) { runCatching { TextData.transposeCsv(withDelimiter(csvT, delim)) } }
    val sortedRes = remember(sortIn) { runCatching { TextData.jsonSortKeys(sortIn) } }
    val comparedRes = remember(cmpA, cmpB) { runCatching { TextData.jsonCompare(cmpA, cmpB) } }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("CSV to JSON") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = delimName == ",", onClick = { delimName = "," }, label = { Text("Comma") }) }
                    item { FilterChip(selected = delimName == ";", onClick = { delimName = ";" }, label = { Text("Semicolon") }) }
                    item { FilterChip(selected = delimName == "tab", onClick = { delimName = "tab" }, label = { Text("Tab") }) }
                    item { FilterChip(selected = delimName == "|", onClick = { delimName = "|" }, label = { Text("Pipe") }) }
                }
                OutlinedTextField(
                    value = csv,
                    onValueChange = { csv = it },
                    label = { Text("CSV") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
                HorizontalDivider()
                val e = csvJsonRes.exceptionOrNull()?.message
                if (e != null) Text(e, color = MaterialTheme.colorScheme.error)
                else ResultLine("JSON", csvJsonRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            runCatching {
                                clipboard.setText(
                                    AnnotatedString(csvJsonRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                                )
                            }
                        }
                    ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy CSV to JSON result") }
                }
            }
        }
        item {
            SectionCard("JSON to CSV") {
                OutlinedTextField(
                    value = jsonIn,
                    onValueChange = { jsonIn = it },
                    label = { Text("JSON array") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
                HorizontalDivider()
                val e = jsonCsvRes.exceptionOrNull()?.message
                if (e != null) Text(e, color = MaterialTheme.colorScheme.error)
                else ResultLine("CSV", jsonCsvRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            runCatching {
                                clipboard.setText(
                                    AnnotatedString(jsonCsvRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                                )
                            }
                        }
                    ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy JSON to CSV result") }
                }
            }
        }
        item {
            SectionCard("Transpose CSV") {
                OutlinedTextField(
                    value = csvT,
                    onValueChange = { csvT = it },
                    label = { Text("CSV") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                HorizontalDivider()
                val e = transposedRes.exceptionOrNull()?.message
                if (e != null) Text(e, color = MaterialTheme.colorScheme.error)
                else ResultLine("Transposed", transposedRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            runCatching {
                                clipboard.setText(
                                    AnnotatedString(transposedRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                                )
                            }
                        }
                    ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy transposed CSV") }
                }
            }
        }
        item {
            SectionCard("Sort keys / Compare") {
                OutlinedTextField(
                    value = sortIn,
                    onValueChange = { sortIn = it },
                    label = { Text("JSON object") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                val se = sortedRes.exceptionOrNull()?.message
                if (se != null) Text(se, color = MaterialTheme.colorScheme.error)
                else ResultLine("Sorted", sortedRes.getOrNull()?.ifEmpty { "—" } ?: "—")
                HorizontalDivider()
                OutlinedTextField(
                    value = cmpA,
                    onValueChange = { cmpA = it },
                    label = { Text("JSON A") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = cmpB,
                    onValueChange = { cmpB = it },
                    label = { Text("JSON B") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                val ce = comparedRes.exceptionOrNull()?.message
                if (ce != null) Text(ce, color = MaterialTheme.colorScheme.error)
                else {
                    val compared = comparedRes.getOrDefault(listOf("—"))
                    ResultLine("Differing paths", if (compared.isEmpty()) "identical" else compared.joinToString(", "))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            runCatching {
                                clipboard.setText(
                                    AnnotatedString(
                                        (sortedRes.getOrNull()?.ifEmpty { "—" } ?: "—") + "\n" +
                                            comparedRes.getOrDefault(listOf("—")).joinToString(", ")
                                    )
                                )
                            }
                        }
                    ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy sort/compare result") }
                }
            }
        }
    }
}

@Composable
fun CronMiscScreen() {
    var expr by remember { mutableStateOf("*/15 * * * *") }
    var year by remember { mutableStateOf("") }
    var ts by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("R") }
    val clipboard = LocalClipboardManager.current
    val explainedRes = remember(expr) { runCatching { TextData.crontabExplain(expr) } }
    val nowSec = remember { runCatching { System.currentTimeMillis() / 1000L }.getOrDefault(0L) }
    val nextRes = remember(expr, nowSec) { runCatching { cronNextRuns(expr, nowSec, 3) } }
    val leap = remember(year) {
        val y = year.trim().toIntOrNull()
        if (y == null) "—"
        else runCatching { if (TextData.isLeapYear(y)) "Leap year" else "Common year" }.getOrDefault("—")
    }
    val discordRes = remember(ts, style) {
        val t = ts.trim().toLongOrNull()
        if (t == null) Result.success("—")
        else runCatching { TextData.discordTimestamp(t, style) }
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Cron") {
                OutlinedTextField(
                    value = expr,
                    onValueChange = { expr = it },
                    label = { Text("Crontab (5 fields)") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                val ee = explainedRes.exceptionOrNull()?.message
                if (ee != null) Text(ee, color = MaterialTheme.colorScheme.error)
                else ResultLine("Explains", explainedRes.getOrNull() ?: "—")
                val ne = nextRes.exceptionOrNull()?.message
                if (ne != null) {
                    Text("Next runs: $ne", color = MaterialTheme.colorScheme.error)
                } else {
                    nextRes.getOrNull()?.forEachIndexed { i, run ->
                        ResultLine("Next ${i + 1}", run)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            runCatching {
                                clipboard.setText(
                                    AnnotatedString(
                                        (explainedRes.getOrNull() ?: "—") + "\n" +
                                            (nextRes.getOrNull()?.joinToString("\n") ?: "—")
                                    )
                                )
                            }
                        }
                    ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy cron result") }
                }
            }
        }
        item {
            SectionCard("Leap year") {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                ResultLine("Result", leap)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = { runCatching { clipboard.setText(AnnotatedString(leap)) } }
                    ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy leap year result") }
                }
            }
        }
        item {
            SectionCard("Discord timestamp") {
                OutlinedTextField(
                    value = ts,
                    onValueChange = { ts = it },
                    label = { Text("Unix seconds") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("t", "T", "d", "D", "f", "F", "R")) { s ->
                        FilterChip(selected = style == s, onClick = { style = s }, label = { Text(s) })
                    }
                }
                HorizontalDivider()
                val de = discordRes.exceptionOrNull()?.message
                if (de != null) Text(de, color = MaterialTheme.colorScheme.error)
                else ResultLine("Tag", discordRes.getOrNull() ?: "—")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            runCatching {
                                clipboard.setText(AnnotatedString(discordRes.getOrNull() ?: "—"))
                            }
                        }
                    ) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Discord timestamp") }
                }
            }
        }
    }
}
