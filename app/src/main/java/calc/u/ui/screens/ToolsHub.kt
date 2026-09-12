package calc.u.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

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
    ToolEntry("Compass", "Sensors", "sensors"),
    ToolEntry("Spirit level", "Sensors", "sensors")
)

@Composable
fun ToolsHub(onOpen: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) HubTools
        else HubTools.filter { it.name.contains(query, ignoreCase = true) }
    }
    val grouped = remember(filtered) { filtered.groupBy { it.category } }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search tools") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        grouped.forEach { (category, tools) ->
            item {
                Text(
                    category,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            items(tools) { tool ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpen(tool.route) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tool.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            tool.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item { Text("No tools match.", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
