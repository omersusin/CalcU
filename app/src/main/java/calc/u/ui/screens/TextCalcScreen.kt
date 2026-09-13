package calc.u.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calc.u.core.TextCalc
import calc.u.core.TextSession
import calc.u.core.TextTokenKind
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.textCalcStore by preferencesDataStore("calcu-textcalc")

private val SessionKey = stringPreferencesKey("textcalc_sessions")

private const val SampleText = "rent = 1200\nfood = 350\nrent + food\ntotal"

@Composable
fun TextCalcScreen() {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf(SampleText) }
    var saveOpen by rememberSaveable { mutableStateOf(false) }
    var saveName by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf<String?>(null) }
    val results = remember(text) { runCatching { TextCalc.evaluateAll(text) }.getOrDefault(emptyList()) }
    val sessions by remember(app) {
        app.textCalcStore.data.map { prefs ->
            TextCalc.decodeSessions(prefs[SessionKey] ?: "[]").sortedByDescending { it.updatedAt }
        }.catch { emit(emptyList()) }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    fun persist(next: List<TextSession>) {
        scope.launch {
            runCatching { app.textCalcStore.edit { it[SessionKey] = TextCalc.encodeSessions(next) } }
        }
    }
    fun bodyWithResults(): String = runCatching { TextCalc.renderWithResults(text) }.getOrDefault(text)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Text calculator",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    "One calculation per line — variables carry downward. Use # for notes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(bodyWithResults()))
                        status = "Copied with results"
                    }
                ) { Text("Copy with results") }
                TextButton(
                    onClick = {
                        val body = bodyWithResults()
                        runCatching {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, body)
                            }
                            val chooser = Intent.createChooser(send, "Share").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            app.startActivity(chooser)
                        }
                    }
                ) { Text("Share") }
            }
        }
        item {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(TextCalc.MAX_CHARS); status = null },
                label = { Text("Scratchpad") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                minLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
        }
        status?.let { s ->
            item {
                Text(s, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            Text(
                "Results",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
        }
        if (results.isEmpty()) {
            item {
                Text(
                    "Type a calculation above to see live results.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(results, key = { it.index }) { r ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${r.index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(28.dp).padding(top = 2.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            highlight(r.input),
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                        )
                        when {
                            r.error != null -> Text(
                                r.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            r.display.isNotEmpty() -> Text(
                                r.display,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).semantics { heading() }
                )
                TextButton(onClick = { saveOpen = true }) { Text("Save current") }
            }
        }
        if (sessions.isEmpty()) {
            item {
                Text(
                    "No saved sessions yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(sessions, key = { it.name + it.updatedAt }) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                sessionDate(s.updatedAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { text = s.body.take(TextCalc.MAX_CHARS); status = null }) {
                            Text("Open")
                        }
                        IconButton(
                            onClick = {
                                val copy = s.copy(
                                    name = TextCalc.cleanSessionName(s.name + " copy").ifEmpty { "copy" },
                                    updatedAt = System.currentTimeMillis()
                                )
                                persist(listOf(copy) + sessions)
                            }
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate ${s.name}")
                        }
                        IconButton(onClick = { persist(sessions.filter { it != s }) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete ${s.name}")
                        }
                    }
                }
            }
        }
    }

    if (saveOpen) {
        AlertDialog(
            onDismissRequest = { saveOpen = false },
            title = { Text("Save session") },
            text = {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it.take(TextCalc.MAX_NAME_CHARS) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = TextCalc.cleanSessionName(saveName)
                        if (clean.isNotEmpty()) {
                            persist(
                                listOf(TextSession(clean, text, System.currentTimeMillis())) +
                                    sessions.filter { it.name != clean }
                            )
                            saveName = ""
                            saveOpen = false
                            status = "Saved \"$clean\""
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { saveOpen = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun highlight(input: String): AnnotatedString {
    val number = MaterialTheme.colorScheme.primary
    val variable = MaterialTheme.colorScheme.secondary
    val function = MaterialTheme.colorScheme.tertiary
    val plain = MaterialTheme.colorScheme.onSurface
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    val tokens = runCatching { TextCalc.tokenize(input) }.getOrDefault(emptyList())
    return buildAnnotatedString {
        if (tokens.isEmpty()) {
            append(input)
            return@buildAnnotatedString
        }
        var cursor = 0
        for (t in tokens) {
            val s = t.start.coerceIn(0, input.length)
            val e = t.end.coerceIn(s, input.length)
            if (s > cursor) append(input.substring(cursor, s))
            val style = when (t.kind) {
                TextTokenKind.Number -> SpanStyle(color = number)
                TextTokenKind.Variable -> SpanStyle(color = variable)
                TextTokenKind.Function -> SpanStyle(color = function)
                TextTokenKind.Operator -> SpanStyle(color = dim)
                TextTokenKind.Comment -> SpanStyle(color = dim, fontStyle = FontStyle.Italic)
                TextTokenKind.Text -> SpanStyle(color = plain)
            }
            pushStyle(style)
            append(input.substring(s, e))
            pop()
            cursor = e
        }
        if (cursor < input.length) append(input.substring(cursor))
    }
}

private fun sessionDate(ts: Long): String {
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ts))
    }.getOrDefault("")
}
