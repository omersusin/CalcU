package calc.u.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import calc.u.data.BackupRepository
import calc.u.data.SettingsRepository
import calc.u.ui.FluentExpander
import calc.u.ui.SectionCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {
    val theme: StateFlow<String> =
        repo.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val vibration: StateFlow<Boolean> =
        repo.vibration.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val dynamicColor: StateFlow<Boolean> =
        repo.dynamicColor.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Build.VERSION.SDK_INT >= 31
        )

    fun setTheme(value: String) {
        viewModelScope.launch { repo.setTheme(value) }
    }

    fun setVibration(value: Boolean) {
        viewModelScope.launch { repo.setVibration(value) }
    }

    fun setDynamicColor(value: Boolean) {
        viewModelScope.launch { repo.setDynamicColor(value) }
    }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    val vibration by vm.vibration.collectAsStateWithLifecycle()
    val dynamicColor by vm.dynamicColor.collectAsStateWithLifecycle()
    val options = listOf(
        "system" to "System",
        "light" to "Light",
        "dark" to "Dark",
        "amoled" to "AMOLED",
        "contrast" to "High contrast",
        "ocean" to "Ocean",
        "forest" to "Forest",
        "sunset" to "Sunset",
        "grape" to "Grape"
    )
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Appearance") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        themeShowcases.forEach { showcase ->
                            ThemePreviewCard(
                                showcase = showcase,
                                selected = theme == showcase.id,
                                onClick = { vm.setTheme(showcase.id) }
                            )
                        }
                    }
                    Column(Modifier.selectableGroup()) {
                        options.forEach { (id, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .selectable(
                                    selected = theme == id,
                                    onClick = { vm.setTheme(id) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = theme == id,
                                onClick = { vm.setTheme(id) }
                            )
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 31) {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Dynamic color",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = dynamicColor,
                                onCheckedChange = { vm.setDynamicColor(it) }
                            )
                        }
                    }
                    }
                }
            }
        }
        item {
            SectionCard("Haptics") {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Vibration",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = vibration,
                        onCheckedChange = { vm.setVibration(it) }
                    )
                }
            }
        }
        item {
            SectionCard("About") {
                Text(
                    "CalcU is a fast offline-first calculator with unit conversion, finance, math, geometry and health tools.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item {
            FluentExpander(header = "Attributions") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Expression evaluation powered by EvalEx (Apache License 2.0).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Converter coverage inspired by UnitConverterUltimate (Apache License 2.0).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Fluent UI for Android forked at omersusin/fluentui-android (MIT License); visual language ported from WinUI (MIT License).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Calculator interaction ideas re-implemented from OpenCalc / Fossify (GPL-family); all code here is original.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Investment formulas after CalcHub (MIT); matrix/constants/solver interaction ideas re-implemented from Stagnant09/Android-Calculator (unlicensed, ideas only).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var backupStatus by remember { mutableStateOf<String?>(null) }
            var pendingJson by remember { mutableStateOf("") }
            val backupRepo = remember(context) { BackupRepository(context.applicationContext) }
            val exportLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                    if (uri == null) return@rememberLauncherForActivityResult
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(pendingJson.toByteArray()) }
                        backupStatus = "Backup saved"
                    } catch (e: Exception) {
                        backupStatus = "Invalid file"
                    }
                }
            val importLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri == null) return@rememberLauncherForActivityResult
                    scope.launch {
                        try {
                            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: throw IllegalArgumentException("Invalid file")
                            val count = backupRepo.import(bytes.toString(Charsets.UTF_8))
                            backupStatus = "Restored $count entries"
                        } catch (e: Exception) {
                            backupStatus = "Invalid file"
                        }
                    }
                }
            SectionCard("Backup") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            scope.launch {
                                try {
                                    pendingJson = backupRepo.export()
                                    exportLauncher.launch("calcu-backup.json")
                                } catch (e: Exception) {
                                    backupStatus = "Invalid file"
                                }
                            }
                        }) {
                            Text("Export")
                        }
                        Button(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                            Text("Import")
                        }
                    }
                    backupStatus?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private data class ThemeShowcase(
    val id: String,
    val label: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val previewSurface: Color,
    val previewOnSurface: Color
)

private val themeShowcases = listOf(
    ThemeShowcase(
        id = "system",
        label = "System",
        primary = Color(0xFF4C662B),
        secondary = Color(0xFF586249),
        tertiary = Color(0xFF38656A),
        previewSurface = Color(0xFFF9FAEF),
        previewOnSurface = Color(0xFF1A1C16)
    ),
    ThemeShowcase(
        id = "light",
        label = "Light",
        primary = Color(0xFF30588F),
        secondary = Color(0xFF5A6B85),
        tertiary = Color(0xFF6B5E8A),
        previewSurface = Color(0xFFFDFBFF),
        previewOnSurface = Color(0xFF1A1C1E)
    ),
    ThemeShowcase(
        id = "dark",
        label = "Dark",
        primary = Color(0xFFAAC7FF),
        secondary = Color(0xFFBEC6DC),
        tertiary = Color(0xFFDDBCE0),
        previewSurface = Color(0xFF131316),
        previewOnSurface = Color(0xFFE3E2E9)
    ),
    ThemeShowcase(
        id = "amoled",
        label = "AMOLED",
        primary = Color(0xFFBBDEFB),
        secondary = Color(0xFF90A4AE),
        tertiary = Color(0xFF80CBC4),
        previewSurface = Color(0xFF000000),
        previewOnSurface = Color(0xFFFFFFFF)
    ),
    ThemeShowcase(
        id = "contrast",
        label = "High contrast",
        primary = Color(0xFF000000),
        secondary = Color(0xFF1A1A1A),
        tertiary = Color(0xFF424242),
        previewSurface = Color(0xFFFFFFFF),
        previewOnSurface = Color(0xFF000000)
    )
)

@Composable
private fun ThemePreviewCard(
    showcase: ThemeShowcase,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 2.dp else 1.dp
    Column(
        modifier = modifier.width(104.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .size(width = 80.dp, height = 64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(showcase.previewSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = showcase.previewOnSurface,
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(showcase.primary)
            )
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(showcase.secondary)
            )
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(showcase.tertiary)
            )
        }
        Text(
            text = showcase.label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
