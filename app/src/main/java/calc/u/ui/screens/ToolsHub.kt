package calc.u.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    ToolEntry("Finance Lab", "Finance", "financelab"),
    ToolEntry("Math", "Math", "math"),
    ToolEntry("Steps", "Math", "steps"),
    ToolEntry("Geometry", "Math", "geometry"),
    ToolEntry("Programmer", "Math", "programmer"),
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
    ToolEntry("Text calc", "Text+Data", "textcalc"),
    ToolEntry("Tally", "Everyday", "everyday"),
    ToolEntry("Dice & coin", "Everyday", "everyday"),
    ToolEntry("Number words", "Everyday", "everyday"),
    ToolEntry("Paint & tiles", "Everyday", "everyday"),
    ToolEntry("Ideal weight", "Everyday", "everyday"),
    ToolEntry("Health", "Health", "health"),
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
    "geometry" to "Geometry",
    "programmer" to "Programmer",
    "time" to "Time Lab",
    "electro" to "Electro",
    "textdata" to "Text+Data",
    "everyday" to "Everyday",
    "health" to "Health",
    "qrscan" to "QR Scan",
    "sensors" to "Sensors",
    "ruler" to "Ruler",
    "analyze" to "Analyze",
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
    "financelab" -> Icons.Filled.Insights
    "math" -> Icons.Filled.GridOn
    "steps" -> Icons.Filled.Timeline
    "geometry" -> Icons.Filled.Category
    "programmer" -> Icons.Filled.Code
    "time" -> Icons.Filled.Timer
    "electro" -> Icons.Filled.Build
    "textdata" -> if (entry.name == "QR") Icons.Filled.QrCode else Icons.Filled.ShortText
    "everyday" -> Icons.Filled.Widgets
    "health" -> Icons.Filled.Favorite
    "ruler" -> Icons.Filled.Straighten
    "sensors" -> Icons.Filled.Explore
    "analyze" -> Icons.Filled.BarChart
    else -> Icons.Filled.Apps
}

// Tool identity is "route#name" so favorites/recents/reorder act on one tool,
// not on every tool sharing a route (e.g. the 10 Text+Data entries).
// ToolPrefs only stores opaque strings, so composite keys persist without
// touching ToolPrefs; legacy route-only keys still rank via rankOf fallback.
private fun toolKey(entry: ToolEntry): String = "${entry.route}#${entry.name}"

private fun keyRoute(key: String): String = key.substringBefore("#")

private fun rankOf(order: List<String>, entry: ToolEntry): Int {
    val exact = order.indexOf(toolKey(entry))
    if (exact >= 0) return exact
    val legacy = order.indexOf(entry.route)
    if (legacy >= 0) return legacy
    return Int.MAX_VALUE
}

@HiltViewModel
class ToolsHubViewModel @Inject constructor(private val prefs: ToolPrefs) : ViewModel() {
    val hubOrder = prefs.hubOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToolPrefs.DefaultHubOrder)
    val recents = prefs.recentTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<String>())
    val favTools = prefs.favTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet<String>())

    fun move(id: String, delta: Int) {
        val stored = hubOrder.value
        val seq = if (stored.any { "#" in it }) {
            stored.toMutableList()
        } else {
            HubTools.sortedWith(
                compareBy(
                    { e: ToolEntry -> stored.indexOf(e.route).takeIf { it >= 0 } ?: Int.MAX_VALUE },
                    { e: ToolEntry -> HubTools.indexOf(e) }
                )
            ).map { toolKey(it) }.toMutableList()
        }
        HubTools.map { toolKey(it) }.forEach { if (!seq.contains(it)) seq.add(it) }
        seq.removeAll { k -> HubTools.none { toolKey(it) == k } }
        val idx = seq.indexOf(id)
        if (idx == -1) return
        val to = (idx + delta).coerceIn(0, seq.size - 1)
        if (to == idx) return
        seq.removeAt(idx)
        seq.add(to, id)
        viewModelScope.launch { prefs.setHubOrder(seq) }
    }

    fun record(key: String) {
        viewModelScope.launch { prefs.record(key) }
    }

    fun toggleFav(key: String) {
        viewModelScope.launch { prefs.toggleFavTool(key) }
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
    val recents by vm.recents.collectAsStateWithLifecycle()
    val order by vm.hubOrder.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var editMode by rememberSaveable { mutableStateOf(false) }
    var collapsedList by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val collapsed = remember(collapsedList) { collapsedList.toSet() }
    val searchFocus = remember { FocusRequester() }
    val calcEntry = remember {
        HubTools.firstOrNull { it.route == "calc" } ?: ToolEntry("Calculator", "Everyday", "calc")
    }
    fun open(entry: ToolEntry, key: String = toolKey(entry)) {
        vm.record(key)
        onOpen(entry.route)
    }
    val searching = query.isNotBlank()
    val filtered = remember(query) {
        if (query.isBlank()) HubTools
        else HubTools.filter { it.name.contains(query, ignoreCase = true) }
    }
    val grouped = remember(filtered) { filtered.groupBy { it.category } }
    val orderedGroups = remember(grouped, order) {
        val cats = grouped.keys.sortedWith(
            compareBy(
                { cat -> grouped.getValue(cat).minOf { rankOf(order, it) } },
                { cat -> HubTools.indexOfFirst { t -> t.category == cat } }
            )
        )
        cats.associateWith { cat ->
            grouped.getValue(cat).sortedWith(
                compareBy({ rankOf(order, it) }, { HubTools.indexOf(it) })
            )
        }
    }
    val favEntries = remember(favs) { HubTools.filter { favs.contains(toolKey(it)) } }
    val recentKeys = remember(recents) { recents.distinct() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { open(calcEntry) },
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface
            ) {
                Icon(Icons.Filled.Calculate, contentDescription = "Open calculator")
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Welcome",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            "What do you need today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { searchFocus.requestFocus() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search tools")
                    }
                    IconButton(onClick = { editMode = !editMode }) {
                        Icon(
                            if (editMode) Icons.Filled.Done else Icons.Filled.Edit,
                            contentDescription = if (editMode) "Done editing" else "Edit order"
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.hub_search)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth().focusRequester(searchFocus)
                )
            }
            if (query.isBlank() && favEntries.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Favourites",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f).semantics { heading() }
                        )
                        TextButton(onClick = { editMode = !editMode }) {
                            Text(if (editMode) "Done" else "Edit")
                        }
                    }
                }
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(favEntries, key = { toolKey(it) }) { tool ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .widthIn(max = 76.dp)
                                    .clickable { open(tool) }
                            ) {
                                Box(
                                    modifier = Modifier.size(72.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(categoryContainer(tool.category))
                                    ) {
                                        Icon(
                                            toolIcon(tool),
                                            contentDescription = null,
                                            tint = categoryOnContainer(tool.category),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                    if (editMode) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.inverseSurface)
                                                .clickable { vm.toggleFav(toolKey(tool)) }
                                        ) {
                                            Icon(
                                                Icons.Filled.Star,
                                                contentDescription = "Unstar ${tool.name}",
                                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
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
            if (query.isBlank() && recentKeys.isNotEmpty()) {
                item {
                    Text(
                        "Recent",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                items(recentKeys, key = { "recent-$it" }) { key ->
                    val entry = HubTools.firstOrNull { toolKey(it) == key }
                    val route = entry?.route ?: keyRoute(key)
                    val label = entry?.name ?: (RouteLabels[route] ?: route)
                    val category = entry?.category ?: "Everyday"
                    val iconEntry = entry ?: ToolEntry(label, category, route)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { open(iconEntry, key) },
                        shape = MaterialTheme.shapes.large,
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
                                    .size(48.dp)
                                    .clip(MaterialTheme.shapes.large)
                                    .background(categoryContainer(category))
                            ) {
                                Icon(
                                    toolIcon(iconEntry),
                                    contentDescription = null,
                                    tint = categoryOnContainer(category),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(label, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    categoryLabel(category),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            orderedGroups.forEach { (category, tools) ->
                val expanded = searching || !collapsed.contains(category)
                item(key = "h-$category") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                collapsedList =
                                    if (collapsed.contains(category)) collapsedList - category
                                    else collapsedList + category
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            categoryLabel(category),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { heading() }
                        )
                        Text(
                            "${tools.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Collapse $category" else "Expand $category"
                        )
                    }
                }
                if (expanded) {
                    items(tools, key = { toolKey(it) }) { tool ->
                        val starred = favs.contains(toolKey(tool))
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { open(tool) },
                            shape = MaterialTheme.shapes.large,
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
                                        .size(48.dp)
                                        .clip(MaterialTheme.shapes.large)
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
                                if (editMode) {
                                    IconButton(onClick = { vm.move(toolKey(tool), -1) }) {
                                        Icon(
                                            Icons.Filled.ArrowUpward,
                                            contentDescription = "Move ${tool.name} up"
                                        )
                                    }
                                    IconButton(onClick = { vm.move(toolKey(tool), 1) }) {
                                        Icon(
                                            Icons.Filled.ArrowDownward,
                                            contentDescription = "Move ${tool.name} down"
                                        )
                                    }
                                }
                                IconButton(onClick = { vm.toggleFav(toolKey(tool)) }) {
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
            }
            if (filtered.isEmpty()) {
                item { Text(stringResource(R.string.hub_empty), style = MaterialTheme.typography.bodyMedium) }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}
