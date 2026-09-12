package calc.u.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calc.u.core.TextData
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard

@Composable
fun TextDataScreen() {
    var tab by remember { mutableStateOf("hash") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    "cipher" to "Cipher"
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
                else -> HashScreen()
            }
        }
    }
}

@Composable
fun HashScreen() {
    var input by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val sha = remember(input) { runCatching { TextData.sha256(input) }.getOrDefault("—") }
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
                HorizontalDivider()
                ResultLine("SHA-256", sha)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { runCatching { clipboard.setText(AnnotatedString(sha)) } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Copy SHA") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { runCatching { clipboard.setText(AnnotatedString(md5)) } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Copy MD5") }
                    }
                }
                ResultLine("MD5", md5)
            }
        }
    }
}

@Composable
fun Base64Screen() {
    var input by remember { mutableStateOf("") }
    var encode by remember { mutableStateOf(true) }
    val clipboard = LocalClipboardManager.current
    val output = remember(input, encode) {
        runCatching {
            if (encode) TextData.base64Encode(input) else TextData.base64Decode(input)
        }.getOrDefault("—")
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Base64") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = encode, onClick = { encode = true }, label = { Text("Encode") }) }
                    item { FilterChip(selected = !encode, onClick = { encode = false }, label = { Text("Decode") }) }
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(if (encode) "Plain text" else "Base64") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                ResultLine("Result", output)
                Button(onClick = { runCatching { clipboard.setText(AnnotatedString(output)) } }) { Text("Copy") }
            }
        }
    }
}

@Composable
fun TextStatsScreen() {
    var input by remember { mutableStateOf("") }
    val counts = remember(input) { runCatching { TextData.counts(input) }.getOrDefault(Triple(0, 0, 0)) }
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
            }
        }
    }
}

@Composable
fun QrScreen() {
    var input by remember { mutableStateOf("") }
    val bitmap = remember(input) {
        if (input.isBlank()) null
        else runCatching {
            val m = TextData.qrMatrix(input, 512)
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
            bmp.asImageBitmap()
        }.getOrNull()
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("QR code") {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                if (bitmap == null) {
                    ResultLine("Preview", "—")
                } else {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(bitmap = bitmap, contentDescription = "QR for $input", modifier = Modifier.size(256.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun UuidScreen() {
    var value by remember { mutableStateOf(runCatching { TextData.uuid() }.getOrDefault("")) }
    val clipboard = LocalClipboardManager.current
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("UUID") {
                ResultLine("Value", value.ifBlank { "—" })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = { runCatching { TextData.uuid() }.onSuccess { value = it } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Regenerate") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { runCatching { clipboard.setText(AnnotatedString(value)) } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Copy") }
                    }
                }
            }
        }
    }
}

@Composable
fun CaseConverterCard() {
    var input by remember { mutableStateOf("") }
    val upper = remember(input) { runCatching { TextData.toUpper(input) }.getOrDefault(input) }
    val lower = remember(input) { runCatching { TextData.toLower(input) }.getOrDefault(input) }
    val title = remember(input) { runCatching { TextData.titleCase(input) }.getOrDefault(input) }
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
    }
}

@Composable
fun UrlCodecCard() {
    var input by remember { mutableStateOf("") }
    var decode by remember { mutableStateOf(false) }
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
    }
}

@Composable
fun MorseCard() {
    var plain by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val encoded = remember(plain) { runCatching { TextData.morseEncode(plain) }.getOrDefault("—") }
    val decoded = remember(code) { runCatching { TextData.morseDecode(code) }.getOrDefault("—") }
    SectionCard("Morse") {
        OutlinedTextField(
            value = plain,
            onValueChange = { plain = it },
            label = { Text("Text to encode") },
            modifier = Modifier.fillMaxWidth()
        )
        ResultLine("Encoded", encoded)
        HorizontalDivider()
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Code to decode") },
            modifier = Modifier.fillMaxWidth()
        )
        ResultLine("Decoded", decoded)
    }
}

@Composable
fun BinaryHexCard() {
    var text by remember { mutableStateOf("") }
    var bin by remember { mutableStateOf("") }
    val binOut = remember(text) { runCatching { TextData.textToBinary(text) }.getOrDefault("—") }
    val textOut = remember(bin) { runCatching { TextData.binaryToText(bin) }.getOrDefault("—") }
    val hexOut = remember(text) { runCatching { TextData.textToHex(text) }.getOrDefault("—") }
    SectionCard("Binary / Hex") {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text") },
            modifier = Modifier.fillMaxWidth()
        )
        ResultLine("Binary", binOut)
        ResultLine("Hex", hexOut)
        HorizontalDivider()
        OutlinedTextField(
            value = bin,
            onValueChange = { bin = it },
            label = { Text("Binary to decode") },
            modifier = Modifier.fillMaxWidth()
        )
        ResultLine("Text", textOut)
    }
}

@Composable
fun JsonFormatterCard() {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val output = remember(input) {
        if (input.isBlank()) {
            error = null
            ""
        } else {
            runCatching { TextData.jsonPretty(input) }.onFailure { error = it.message ?: "Invalid JSON" }
                .onSuccess { error = null }.getOrDefault("")
        }
    }
    SectionCard("JSON formatter") {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("JSON") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        HorizontalDivider()
        if (error != null) {
            Text(error ?: "Invalid JSON")
        } else {
            ResultLine("Pretty", if (output.isEmpty()) "—" else output)
        }
    }
}

@Composable
fun RegexTesterCard() {
    var pattern by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val matches = remember(pattern, input) {
        if (pattern.isEmpty() || input.isEmpty()) {
            error = null
            emptyList()
        } else {
            runCatching { TextData.regexTest(pattern, input) }.onFailure { error = it.message ?: "Invalid pattern" }
                .onSuccess { error = null }.getOrDefault(emptyList())
        }
    }
    SectionCard("Regex tester") {
        OutlinedTextField(
            value = pattern,
            onValueChange = { pattern = it },
            label = { Text("Pattern") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Input") },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        if (error != null) {
            Text(error ?: "Invalid pattern")
        } else {
            ResultLine("Matches", if (matches.isEmpty()) "—" else matches.joinToString(", "))
        }
    }
}

@Composable
fun UnixTimeCard() {
    var now by remember { mutableStateOf(runCatching { TextData.unixNow() }.getOrDefault(0L)) }
    var input by remember { mutableStateOf("") }
    val converted = remember(input) {
        if (input.isBlank()) "—"
        else runCatching { TextData.unixToDate(input.trim().toLong()) }.getOrDefault("—")
    }
    SectionCard("Unix time") {
        ResultLine("Now", "$now")
        Button(onClick = { runCatching { TextData.unixNow() }.onSuccess { now = it } }) { Text("Refresh now") }
        HorizontalDivider()
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Timestamp (seconds)") },
            modifier = Modifier.fillMaxWidth()
        )
        ResultLine("Date", converted)
    }
}

@Composable
fun TotpCard() {
    var secret by remember { mutableStateOf("") }
    var nowSec by remember { mutableStateOf(runCatching { System.currentTimeMillis() / 1000L }.getOrDefault(0L)) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            try {
                kotlinx.coroutines.delay(1000L)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                break
            }
            nowSec = runCatching { System.currentTimeMillis() / 1000L }.getOrDefault(nowSec)
        }
    }
    val period = 30L
    val code = remember(secret, nowSec) {
        if (secret.isBlank()) "—"
        else runCatching { calc.u.core.Totp.totp(secret, nowSec, period) }.getOrDefault("—")
    }
    val remaining = remember(nowSec) { runCatching { calc.u.core.Totp.secondsRemaining(nowSec, period) }.getOrDefault(period) }
    val clipboard = LocalClipboardManager.current
    SectionCard("Authenticator (TOTP)") {
        OutlinedTextField(
            value = secret,
            onValueChange = { secret = it },
            label = { Text("Base32 secret") },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        ResultLine("Code", code)
        ResultLine("Expires in", "$remaining s")
        androidx.compose.material3.LinearProgressIndicator(
            progress = { remaining.coerceIn(0L, period).toFloat() / period.coerceAtLeast(1L).toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { runCatching { clipboard.setText(AnnotatedString(code)) } }) { Text("Copy code") }
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
    val output = remember(input, mode, encrypt, shift, key) {
        runCatching {
            if (mode == "caesar") {
                TextData.caesar(input, shift.trim().toIntOrNull() ?: 0, encrypt)
            } else if (encrypt) {
                TextData.xorHex(input, key)
            } else {
                val clean = input.trim()
                require(clean.length % 2 == 0) { "Hex length must be even" }
                val kb = key.toByteArray(Charsets.UTF_8)
                require(kb.isNotEmpty()) { "Key must not be empty" }
                val bytes = clean.chunked(2).map { runCatching { it.toInt(16).toByte() }.getOrElse { throw IllegalArgumentException("Invalid hex") } }.toByteArray()
                bytes.mapIndexed { i, b -> (b.toInt() xor kb[i % kb.size.coerceAtLeast(1)].toInt()).toByte() }
                    .toByteArray().toString(Charsets.UTF_8)
            }
        }.getOrDefault("—")
    }
    SectionCard("Cipher") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = mode == "caesar", onClick = { mode = "caesar" }, label = { Text("Caesar") }) }
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
        HorizontalDivider()
        ResultLine("Result", output)
        Button(onClick = { runCatching { clipboard.setText(AnnotatedString(output)) } }) { Text("Copy") }
    }
}
