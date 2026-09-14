package calc.u.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import calc.u.packs.CatalogEntry
import calc.u.packs.JsRuntime
import calc.u.packs.PackManager
import calc.u.packs.PackManifest
import calc.u.packs.parseCatalog
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PacksViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context
) : ViewModel() {
    private val manager = PackManager(ctx.filesDir, ctx.assets)
    private val nonce = MutableStateFlow(0)

    val installed: StateFlow<List<PackManifest>> = nonce.map {
        runCatching { manager.installed() }.getOrDefault(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enabled: StateFlow<Set<String>> = nonce.map {
        runCatching { manager.enabledIds() }.getOrDefault(emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val catalog: StateFlow<List<CatalogEntry>> = nonce.map {
        runCatching {
            ctx.assets.open("packs/catalog.json").use {
                parseCatalog(it.readBytes().decodeToString()).packs
            }
        }.getOrDefault(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        nonce.update { it + 1 }
    }

    suspend fun install(entry: CatalogEntry): String? = withContext(Dispatchers.IO) {
        val err = manager.installFromCatalog(entry).exceptionOrNull()?.message
        refresh()
        err
    }

    suspend fun remove(id: String) = withContext(Dispatchers.IO) {
        manager.remove(id)
        refresh()
    }

    suspend fun setEnabled(id: String, on: Boolean) = withContext(Dispatchers.IO) {
        manager.setEnabled(id, on)
        refresh()
    }

    suspend fun runTool(id: String, function: String, input: String): Result<String> =
        withContext(Dispatchers.Default) {
            val script = manager.entrySource(id)
                ?: return@withContext Result.failure(IllegalStateException("pack not installed"))
            JsRuntime.eval(script, function, input)
        }
}

@Composable
fun PacksScreen(vm: PacksViewModel = hiltViewModel()) {
    val installed by vm.installed.collectAsStateWithLifecycle()
    val enabled by vm.enabled.collectAsStateWithLifecycle()
    val catalog by vm.catalog.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var inputs by remember { mutableStateOf(mapOf<String, String>()) }
    var outputs by remember { mutableStateOf(mapOf<String, String>()) }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.refresh() }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(
                "Packs",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                "Download tool packs once, use them offline. Only hash-pinned catalog packs can install.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (status != null) {
            item {
                Text(status ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            Text(
                "Installed (${installed.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
        }
        if (installed.isEmpty()) {
            item { Text("Nothing installed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(installed, key = { it.id }) { pack ->
            val on = enabled.contains(pack.id)
            SectionCard("${pack.name} v${pack.version}") {
                Text(
                    pack.description.ifBlank { pack.id },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (on) "Enabled" else "Disabled",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = on, onCheckedChange = { checked ->
                        scope.launch { vm.setEnabled(pack.id, checked) }
                    })
                    TextButton(onClick = { scope.launch { vm.remove(pack.id) } }) {
                        Text("Remove")
                    }
                }
                if (on) {
                    pack.tools.forEach { tool ->
                        val key = "${pack.id}/${tool.id}"
                        OutlinedTextField(
                            value = inputs[key] ?: "",
                            onValueChange = { inputs = inputs + (key to it) },
                            label = { Text(tool.label) },
                            placeholder = { Text(tool.hint.ifBlank { "input" }) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        val out = outputs[key]
                        if (!out.isNullOrBlank()) {
                            Text(
                                out,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                        Button(onClick = {
                            scope.launch {
                                busy = true
                                status = null
                                val res = vm.runTool(pack.id, tool.function, inputs[key] ?: "")
                                busy = false
                                res.onSuccess { outputs = outputs + (key to it.ifBlank { "—" }) }
                                    .onFailure { status = it.message ?: "run failed" }
                            }
                        }, enabled = !busy) { Text("Run ${tool.label}") }
                    }
                }
            }
        }
        item {
            Text(
                "Catalog (${catalog.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
        }
        items(catalog, key = { it.id }) { entry ->
            val have = installed.firstOrNull { it.id == entry.id }
            SectionCard(entry.name) {
                Text(
                    entry.description.ifBlank { "v${entry.version}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        when {
                            have == null -> "v${entry.version} available"
                            have.version < entry.version -> "installed v${have.version}, update v${entry.version}"
                            else -> "installed v${have.version}"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (have == null || have.version < entry.version) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                busy = true
                                status = null
                                val err = vm.install(entry)
                                busy = false
                                status = err ?: "${entry.name} installed"
                            }
                        }, enabled = !busy) {
                            Text(if (have == null) "Install" else "Update")
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ResultLine("Runtime", "duktape-js-1 (sandboxed, no host bridge)")
                ResultLine("Trust", "hash-pinned catalog only")
            }
        }
    }
}
