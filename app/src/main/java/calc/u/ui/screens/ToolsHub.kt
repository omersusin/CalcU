package calc.u.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import calc.u.R
import calc.u.data.ToolPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class ToolEntry(val name: String, val category: String, val route: String)

private val HubTools = listOf(
    ToolEntry("Calculator", "Everyday", "calc"),
    ToolEntry("Graph", "Math", "graph"),
    ToolEntry("Convert", "Everyday", "convert"),
    ToolEntry("Finance", "Finance", "finance"),
    ToolEntry("Math", "Math", "math"),
    ToolEntry("Steps", "Math", "steps"),
    ToolEntry("Stopwatch", "Time", "time"),
    ToolEntry("Timer", "Time", "time"),
    ToolEntry("Pomodoro", "Time", "time"),
    ToolEntry("Resistor", "Electro", "electro"),
    ToolEntry("Divider / LED / RC", "Electro", "electro"),
    ToolEntry("Subnet", "Network", "electro"),
    ToolEntry("Hash", "Text+Data", "textdata"),
    ToolEntry("Base64", "Text+Data", "textdata"),
    ToolEntry("Text stats", "Text+Data", "textdata"),
    ToolEntry("QR", "Text+Data", "textdata"),
    ToolEntry("UUID", "Text+Data", "textdata"),
    ToolEntry("Case / URL / Morse", "Text+Data", "textdata"),
    ToolEntry("Binary / JSON / Regex", "Text+Data", "textdata"),
    ToolEntry("Unix time", "Text+Data", "textdata"),
    ToolEntry("TOTP codes", "Text+Data", "textdata"),
    ToolEntry("Cipher", "Text+Data", "textdata"),
    ToolEntry("Tally", "Everyday", "everyday"),
    ToolEntry("Dice & coin", "Everyday", "everyday"),
    ToolEntry("Number words", "Everyday", "everyday"),
    ToolEntry("Paint & tiles", "Everyday", "everyday"),
    ToolEntry("Ideal weight", "Everyday", "everyday"),
    ToolEntry("Metronome", "Everyday", "everyday"),
    ToolEntry("Ruler", "Everyday", "ruler"),
    ToolEntry("Compass", "Sensors", "sensors"),
    ToolEntry("Spirit level", "Sensors", "sensors"),
    ToolEntry("Sound meter", "Sensors", "sensors"),
    ToolEntry("Activity analyze", "History", "analyze")
)

private val RouteLabels = mapOf(
    "calc" to "Calculator",
    "graph" to "Graph",
    "convert" to "Convert",
    "finance" to "Finance",
    "math" to "Math",
    "steps" to "Steps",
    "time" to "Time Lab",
    "electro" to "Electro",
    "textdata" to "Text+Data",
    "everyday" to "Everyday",
    "qrscan" to "QR Scan",
    "sensors" to "Sensors",
    "ruler" to "Ruler",
    "tools" to "Tools",
    "settings" to "Settings"
)

@HiltViewModel
class ToolsHubViewModel @Inject constructor(private val prefs: ToolPrefs) : ViewModel() {
    val hubOrder = prefs.hubOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToolPrefs.DefaultHubOrder)
    val recents = prefs.recentTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<String>())

    fun move(route: String, delta: Int) {
        val cur = hubOrder.value.toMutableList()
        val idx = cur.indexOf(route)
        if (idx == -1) return
        val to = (idx + delta).coerceIn(0, cur.size - 1)
        if (to == idx) return
        cur.removeAt(idx)
        cur.add(to, route)
        viewModelScope.launch { prefs.setHubOrder(cur) }
    }

    fun record(route: String) {
        viewModelScope.launch { prefs.record(route) }
    }
}

@Composable
private fun categoryLabel(category: String): String = when (category) {
    "Finance" -> stringResource(R.string.tool_cat_finance)
    "Math" -> stringResource(R.string.tool_cat_math)
    "Health" -> stringResource(R.string.tool_cat_health)
    "Time" -> stringResource(R.string.tool_cat_time)
    "Electro" -> stringResource(R.string.tool_cat_electro)
    "Network" -> stringResource(R.string.tool_cat_network)
    "Text+Data" -> stringResource(R.string.tool_cat_textdata)
    "Everyday" -> stringResource(R.string.tool_cat_everyday)
    "Sensors" -> stringResource(R.string.tool_cat_sensors)
    else -> category
}

@Composable
fun ToolsHub(onOpen: (String) -> Unit, vm: ToolsHubViewModel = hiltViewModel()) {
    val order by vm.hubOrder.collectAsStateWithLifecycle()
    val recents by vm.recents.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }
    fun open(route: String) {
        vm.record(route)
        onOpen(route)
    }
    val orderedAll = remember(order) {
        val rank = order.withIndex().associate { it.value to it.index }
        HubTools.withIndex()
            .sortedWith(compareBy({ rank[it.value.route] ?: Int.MAX_VALUE }, { it.index }))
            .map { it.value }
    }
    val filtered = remember(query, orderedAll) {
        if (query.isBlank()) orderedAll
        else orderedAll.filter { it.name.contains(query, ignoreCase = true) }
    }
    val grouped = remember(filtered) { filtered.groupBy { it.category } }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.hub_search)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { editing = !editing }) {
                    Text(if (editing) "Done" else "Edit")
                }
            }
        }
        if (query.isBlank() && recents.isNotEmpty()) {
            item {
                Text(
                    "Recent",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            items(recents) { route ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { open(route) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(RouteLabels[route] ?: route, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        grouped.forEach { (category, tools) ->
            item {
                Text(
                    categoryLabel(category),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            items(tools) { tool ->
                if (editing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier.weight(1f).clickable { open(tool.route) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(tool.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    categoryLabel(tool.category),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column {
                            IconButton(onClick = { vm.move(tool.route, -1) }) {
                                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                            }
                            IconButton(onClick = { vm.move(tool.route, 1) }) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { open(tool.route) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tool.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                categoryLabel(tool.category),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item { Text(stringResource(R.string.hub_empty), style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
