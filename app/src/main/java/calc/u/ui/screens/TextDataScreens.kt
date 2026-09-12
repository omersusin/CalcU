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
                    "uuid" to "UUID"
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
                            onClick = { clipboard.setText(AnnotatedString(sha)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Copy SHA") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { clipboard.setText(AnnotatedString(md5)) },
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
                Button(onClick = { clipboard.setText(AnnotatedString(output)) }) { Text("Copy") }
            }
        }
    }
}

@Composable
fun TextStatsScreen() {
    var input by remember { mutableStateOf("") }
    val counts = remember(input) { TextData.counts(input) }
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
            val bmp = Bitmap.createBitmap(m.width, m.height, Bitmap.Config.ARGB_8888)
            val px = IntArray(m.width * m.height)
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
    var value by remember { mutableStateOf(TextData.uuid()) }
    val clipboard = LocalClipboardManager.current
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("UUID") {
                ResultLine("Value", value)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = { value = TextData.uuid() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Regenerate") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { clipboard.setText(AnnotatedString(value)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Copy") }
                    }
                }
            }
        }
    }
}
