package calc.u.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

// Fixed-hue intent (do NOT hardcode — dynamic-color safe mapping below):
// Finance green, Math violet, Health red/pink, Time blue, Electro amber,
// Network teal, Text+Data slate, Everyday orange, Sensors brown, History purple.
// Implemented as tonal containers cycling by category so light/dark + dynamic color stay safe.
@Composable
private fun categoryContainer(category: String): Color = when (category) {
    "Finance" -> MaterialTheme.colorScheme.primaryContainer
    "Math" -> MaterialTheme.colorScheme.secondaryContainer
    "Health" -> MaterialTheme.colorScheme.errorContainer
    "Time" -> MaterialTheme.colorScheme.tertiaryContainer
    "Electro" -> MaterialTheme.colorScheme.primaryContainer
    "Network" -> MaterialTheme.colorScheme.secondaryContainer
    "Text+Data" -> MaterialTheme.colorScheme.tertiaryContainer
    "Everyday" -> MaterialTheme.colorScheme.secondaryContainer
    "Sensors" -> MaterialTheme.colorScheme.tertiaryContainer
    "History" -> MaterialTheme.colorScheme.primaryContainer
    else -> MaterialTheme.colorScheme.secondaryContainer
}

@Composable
private fun categoryOnContainer(category: String): Color = when (category) {
    "Finance" -> MaterialTheme.colorScheme.onPrimaryContainer
    "Math" -> MaterialTheme.colorScheme.onSecondaryContainer
    "Health" -> MaterialTheme.colorScheme.onErrorContainer
    "Time" -> MaterialTheme.colorScheme.onTertiaryContainer
    "Electro" -> MaterialTheme.colorScheme.onPrimaryContainer
    "Network" -> MaterialTheme.colorScheme.onSecondaryContainer
    "Text+Data" -> MaterialTheme.colorScheme.onTertiaryContainer
    "Everyday" -> MaterialTheme.colorScheme.onSecondaryContainer
    "Sensors" -> MaterialTheme.colorScheme.onTertiaryContainer
    "History" -> MaterialTheme.colorScheme.onPrimaryContainer
    else -> MaterialTheme.colorScheme.onSecondaryContainer
}

private fun toolIcon(entry: ToolEntry): ImageVector = when (entry.route) {
    "calc" -> Icons.Filled.Calculate
    "graph" -> Icons.Filled.ShowChart
    "convert" -> Icons.Filled.SwapHoriz
    "finance" -> Icons.Filled.AttachMoney
    "math" -> Icons.Filled.GridOn
    "steps" -> Icons.Filled.Timeline
    "time" -> Icons.Filled.Timer
    "electro" -> Icons.Filled.Build
    "textdata" -> if (entry.name == "QR") Icons.Filled.QrCode else Icons.Filled.ShortText
    "everyday" -> Icons.Filled.Apps
    "ruler" -> Icons.Filled.Straighten
    "sensors" -> Icons.Filled.Explore
    "analyze" -> Icons.Filled.BarChart
    else -> Icons.Filled.Apps
}

@HiltViewModel
class ToolsHubViewModel @Inject constructor(private val prefs: ToolPrefs) : ViewModel() {
    val hubOrder = prefs.hubOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToolPrefs.DefaultHubOrder)
    val recents = prefs.recentTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<String>())
    val favTools = prefs.favTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet<String>())

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

    fun toggleFav(route: String) {
        viewModelScope.launch { prefs.toggleFavTool(route) }
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
    val favs by vm.favTools.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    fun open(route: String) {
        vm.record(route)
        onOpen(route)
    }
    val filtered = remember(query) {
        if (query.isBlank()) HubTools
        else HubTools.filter { it.name.contains(query, ignoreCase = true) }
    }
    val grouped = remember(filtered) { filtered.groupBy { it.category } }
    val favEntries = remember(favs) { HubTools.filter { favs.contains(it.route) }.distinctBy { it.name } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Welcome",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "What do you need today?",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() }
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.hub_search)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isBlank() && favEntries.isNotEmpty()) {
            item {
                Text(
                    "Favourites",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(favEntries, key = { it.name }) { tool ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .widthIn(max = 72.dp)
                                .clickable { open(tool.route) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(categoryContainer(tool.category))
                            ) {
                                Icon(
                                    toolIcon(tool),
                                    contentDescription = null,
                                    tint = categoryOnContainer(tool.category),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                tool.name,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        grouped.forEach { (category, tools) ->
            item(key = "h-$category") {
                Text(
                    categoryLabel(category),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            items(tools, key = { it.name }) { tool ->
                val starred = favs.contains(tool.route)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { open(tool.route) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(categoryContainer(tool.category))
                        ) {
                            Icon(
                                toolIcon(tool),
                                contentDescription = null,
                                tint = categoryOnContainer(tool.category),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tool.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                categoryLabel(tool.category),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { vm.toggleFav(tool.route) }) {
                            Icon(
                                if (starred) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = if (starred) "Unstar ${tool.name}" else "Star ${tool.name}",
                                tint = if (starred) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
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
