package calc.u.ui.screens

import android.os.Build
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import calc.u.ui.theme.CalcUThemeSeeds
import calc.u.ui.theme.CustomThemeId
import calc.u.ui.theme.KeypadShapeIds
import calc.u.ui.theme.hslToSeedArgb
import calc.u.ui.theme.seedArgbToHsl
import com.google.android.material.color.utilities.TonalPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {
    val theme: StateFlow<String> =
        repo.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val themeMode: StateFlow<String> =
        repo.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val amoled: StateFlow<Boolean> =
        repo.amoled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val vibration: StateFlow<Boolean> =
        repo.vibration.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val dynamicColor: StateFlow<Boolean> =
        repo.dynamicColor.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Build.VERSION.SDK_INT >= 31
        )
    val historyCap: StateFlow<Int> =
        repo.historyCap.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 200)
    val numberFormat: StateFlow<String> =
        repo.numberFormat.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "locale")
    val decimals: StateFlow<Int> =
        repo.decimals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)
    val fractions: StateFlow<Boolean> =
        repo.fractions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val keepScreenOn: StateFlow<Boolean> =
        repo.keepScreenOn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val memoryRow: StateFlow<Boolean> =
        repo.memoryRow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val engineering: StateFlow<Boolean> =
        repo.engineering.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val precisionSlider: StateFlow<Int> =
        repo.precisionSlider.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)
    val customSeedArgb: StateFlow<Int?> =
        repo.customSeedArgb.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val keypadShape: StateFlow<String> =
        repo.keypadShape.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KeypadShapeIds.Circles)

    fun setTheme(value: String) {
        viewModelScope.launch { repo.setTheme(value) }
    }

    fun setThemeMode(value: String) {
        viewModelScope.launch { repo.setThemeMode(value) }
    }

    fun setAmoled(value: Boolean) {
        viewModelScope.launch { repo.setAmoled(value) }
    }

    fun setVibration(value: Boolean) {
        viewModelScope.launch { repo.setVibration(value) }
    }

    fun setDynamicColor(value: Boolean) {
        viewModelScope.launch { repo.setDynamicColor(value) }
    }

    fun setHistoryCap(value: Int) {
        viewModelScope.launch { repo.setHistoryCap(value) }
    }

    fun setNumberFormat(value: String) {
        viewModelScope.launch { repo.setNumberFormat(value) }
    }

    fun setDecimals(value: Int) {
        viewModelScope.launch { repo.setDecimals(value) }
    }

    fun setFractions(value: Boolean) {
        viewModelScope.launch { repo.setFractions(value) }
    }

    fun setKeepScreenOn(value: Boolean) {
        viewModelScope.launch { repo.setKeepScreenOn(value) }
    }

    fun setMemoryRow(value: Boolean) {
        viewModelScope.launch { repo.setMemoryRow(value) }
    }

    fun setEngineering(value: Boolean) {
        viewModelScope.launch { repo.setEngineering(value) }
    }

    fun setPrecisionSlider(value: Int) {
        viewModelScope.launch { repo.setPrecisionSlider(value) }
    }

    fun setCustomSeedArgb(value: Int) {
        viewModelScope.launch {
            repo.setCustomSeedArgb(value)
            repo.setTheme(CustomThemeId)
        }
    }

    fun setKeypadShape(value: String) {
        viewModelScope.launch { repo.setKeypadShape(value) }
    }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val amoled by vm.amoled.collectAsStateWithLifecycle()
    val vibration by vm.vibration.collectAsStateWithLifecycle()
    val dynamicColor by vm.dynamicColor.collectAsStateWithLifecycle()
    val historyCap by vm.historyCap.collectAsStateWithLifecycle()
    val numberFormat by vm.numberFormat.collectAsStateWithLifecycle()
    val decimals by vm.decimals.collectAsStateWithLifecycle()
    val fractions by vm.fractions.collectAsStateWithLifecycle()
    val keepScreenOn by vm.keepScreenOn.collectAsStateWithLifecycle()
    val memoryRow by vm.memoryRow.collectAsStateWithLifecycle()
    val engineering by vm.engineering.collectAsStateWithLifecycle()
    val precisionSlider by vm.precisionSlider.collectAsStateWithLifecycle()
    val customSeedArgb by vm.customSeedArgb.collectAsStateWithLifecycle()
    val keypadShape by vm.keypadShape.collectAsStateWithLifecycle()
    var showCustomSheet by remember { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Theme") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var showMoreSeeds by remember { mutableStateOf(false) }
                    Text(
                        "Mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() }
                    )
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        val modes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                        modes.forEachIndexed { index, (id, label) ->
                            SegmentedButton(
                                selected = themeMode == id,
                                onClick = { vm.setThemeMode(id) },
                                shape = SegmentedButtonDefaults.itemShape(index, modes.size)
                            ) {
                                Text(label)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "AMOLED background",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Pure-black surfaces",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = amoled,
                            onCheckedChange = { vm.setAmoled(it) }
                        )
                    }
                    Text(
                        "Dynamic M3 Fixed (wallpaper) is the main theme; Obsidian (graphite-mint) is the second.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (theme == "amoled" || theme == "contrast") {
                        Text(
                            "Legacy theme \"" + theme + "\" active — pick a seed below to migrate (mode and background carry over).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (Build.VERSION.SDK_INT >= 31) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DynamicSeedTile(
                                selected = theme == "system",
                                dynamicOn = dynamicColor,
                                onSelect = {
                                    vm.setTheme("system")
                                    vm.setDynamicColor(true)
                                },
                                onDynamicChange = {
                                    vm.setDynamicColor(it)
                                    if (it) vm.setTheme("system")
                                }
                            )
                            val obsidianSeed = CalcUThemeSeeds.classics.first { it.id == "obsidian" }
                            val obsidianSelected = theme == "obsidian"
                            val obsidianBorderColor =
                                if (obsidianSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        if (obsidianSelected) 2.dp else 1.dp,
                                        obsidianBorderColor,
                                        MaterialTheme.shapes.medium
                                    )
                                    .background(MaterialTheme.colorScheme.surface)
                                    .selectable(
                                        selected = obsidianSelected,
                                        onClick = { vm.setTheme("obsidian") },
                                        role = Role.RadioButton
                                    )
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SwatchPreview(
                                    swatches = seedSwatches(obsidianSeed),
                                    selected = obsidianSelected
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "Obsidian",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (obsidianSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            if (obsidianSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Static graphite-mint",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val obsidianSeed = CalcUThemeSeeds.classics.first { it.id == "obsidian" }
                        SwatchPreview(
                            swatches = seedSwatches(obsidianSeed),
                            selected = theme == "obsidian"
                        )
                        Text(
                            "Obsidian",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(
                                selected = showMoreSeeds,
                                onClick = { showMoreSeeds = !showMoreSeeds },
                                role = Role.Button
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "More seed themes",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (showMoreSeeds) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.Filled.KeyboardArrowDown
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (showMoreSeeds) {
                        if (Build.VERSION.SDK_INT >= 31) {
                            SeedGroupHeader("Dynamic")
                            DynamicSeedTile(
                                selected = theme == "system",
                                dynamicOn = dynamicColor,
                                onSelect = {
                                    vm.setTheme("system")
                                    vm.setDynamicColor(true)
                                },
                                onDynamicChange = {
                                    vm.setDynamicColor(it)
                                    if (it) vm.setTheme("system")
                                }
                            )
                        }
                        SeedGroupHeader("Botanical")
                        SeedGrid(
                            seeds = listOf(CalcUThemeSeeds.botanical),
                            selectedId = theme,
                            onSelect = { vm.setTheme(it) }
                        )
                        SeedGroupHeader("Classics")
                        SeedGrid(
                            seeds = CalcUThemeSeeds.classics,
                            selectedId = theme,
                            onSelect = { vm.setTheme(it) }
                        )
                        SeedGroupHeader("Vivid")
                        SeedGrid(
                            seeds = CalcUThemeSeeds.vivid,
                            selectedId = theme,
                            onSelect = { vm.setTheme(it) }
                        )
                        SeedGroupHeader("Custom")
                        CustomSeedTile(
                            selected = theme == CustomThemeId,
                            customArgb = customSeedArgb,
                            onSelect = { vm.setTheme(CustomThemeId) },
                            onCustomize = { showCustomSheet = true }
                        )
                    }
                    SeedGroupHeader("Keypad shape")
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        val shapes = listOf(
                            KeypadShapeIds.Circles to "Circles",
                            KeypadShapeIds.Squircle to "Squircle",
                            KeypadShapeIds.Pill to "Pill"
                        )
                        shapes.forEachIndexed { index, (id, label) ->
                            SegmentedButton(
                                selected = keypadShape == id,
                                onClick = { vm.setKeypadShape(id) },
                                shape = SegmentedButtonDefaults.itemShape(index, shapes.size)
                            ) {
                                Text(label)
                            }
                        }
                    }
                    Text(
                        "Keypad shape is applied live through keyShape() via LocalKeyShape in CalculatorScreens — no follow-up needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            SectionCard("General") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.selectableGroup()) {
                        listOf(
                            "locale" to "System locale",
                            "comma" to "1,234,567.89",
                            "space" to "1 234 567.89",
                            "none" to "1234567.89",
                            "indian" to "12,34,567.89"
                        ).forEach { (id, example) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                    .selectable(
                                        selected = numberFormat == id,
                                        onClick = { vm.setNumberFormat(id) },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = numberFormat == id,
                                    onClick = { vm.setNumberFormat(id) }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                                    Text(
                                        id.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        example,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        "Decimals: $decimals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 4, 6, 10).forEach { scale ->
                            FilterChip(
                                selected = decimals == scale,
                                onClick = { vm.setDecimals(scale) },
                                label = { Text("$scale") }
                            )
                        }
                    }
                    Text(
                        "Precision: $precisionSlider",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = precisionSlider.toFloat(),
                        onValueChange = { vm.setPrecisionSlider(it.toInt().coerceIn(0, 16)) },
                        valueRange = 0f..16f,
                        steps = 15
                    )
                    Text(
                        "Keep last $historyCap entries",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(50, 100, 200, 500, 1000).forEach { cap ->
                            FilterChip(
                                selected = historyCap == cap,
                                onClick = { vm.setHistoryCap(cap) },
                                label = { Text("$cap") }
                            )
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Calculator") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Engineering",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = engineering,
                            onCheckedChange = { vm.setEngineering(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Show fractions",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = fractions,
                            onCheckedChange = { vm.setFractions(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Keep screen on",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = keepScreenOn,
                            onCheckedChange = { vm.setKeepScreenOn(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Memory row",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = memoryRow,
                            onCheckedChange = { vm.setMemoryRow(it) }
                        )
                    }
                }
            }
        }
        item {
            val context = LocalContext.current
            var feedbackError by remember { mutableStateOf<String?>(null) }
            SectionCard("Feedback") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            feedbackError = null
                            try {
                                context.startActivity(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("market://details?id=" + context.packageName)
                                    )
                                )
                            } catch (e: Exception) {
                                try {
                                    context.startActivity(
                                        android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
                                        )
                                    )
                                } catch (e2: Exception) {
                                    feedbackError = "Cannot open store"
                                }
                            }
                        }) {
                            Text("Rate app")
                        }
                        Button(onClick = {
                            feedbackError = null
                            try {
                                val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "message/rfc822"
                                    data = android.net.Uri.parse("mailto:")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "CalcU issue")
                                }
                                context.startActivity(android.content.Intent.createChooser(send, "Report issue"))
                            } catch (e: Exception) {
                                feedbackError = "No email app found"
                            }
                        }) {
                            Text("Report issue")
                        }
                    }
                    feedbackError?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        item {
            val context = LocalContext.current
            var languageError by remember { mutableStateOf<String?>(null) }
            SectionCard("Other") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        languageError = null
                        try {
                            if (Build.VERSION.SDK_INT >= 33) {
                                context.startActivity(
                                    android.content.Intent(
                                        android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
                                    ).apply {
                                        data = android.net.Uri.parse("package:" + context.packageName)
                                    }
                                )
                            } else {
                                context.startActivity(
                                    android.content.Intent(android.provider.Settings.ACTION_LOCALE_SETTINGS)
                                )
                            }
                        } catch (e: Exception) {
                            languageError = "Cannot open language settings"
                        }
                    }) {
                        Text("Language")
                    }
                    languageError?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        "CalcU is a fast offline-first calculator with unit conversion, finance, math, geometry and health tools.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
                    Text(
                        "Custom-seed + button-shape personalization re-implemented from Calc-OS (MIT License, © 2026 HyBox); all code here is original.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Lato typeface by Lukasz Dziedzic (SIL Open Font License 1.1); see assets/licenses/OFL-Lato.txt.",
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
        item {
            val context = LocalContext.current
            var lastCrash by remember { mutableStateOf<String?>(null) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                lastCrash = runCatching { calc.u.system.CrashReporter.readLast(context.applicationContext) }.getOrNull()
            }
            SectionCard("Diagnostics") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        lastCrash?.lineSequence()?.take(3)?.joinToString("\n")?.ifBlank { "No recorded crashes" }
                            ?: "No recorded crashes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                runCatching {
                                    val text = lastCrash ?: return@runCatching
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("crash", text))
                                }
                            },
                            enabled = lastCrash != null
                        ) {
                            Text("Copy")
                        }
                        Button(
                            onClick = {
                                runCatching {
                                    val text = lastCrash ?: return@runCatching
                                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, text)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(send, "Share crash log"))
                                }
                            },
                            enabled = lastCrash != null
                        ) {
                            Text("Share")
                        }
                        Button(
                            onClick = {
                                runCatching {
                                    calc.u.system.CrashReporter.clear(context.applicationContext)
                                    lastCrash = null
                                }
                            },
                            enabled = lastCrash != null
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }
        item {
            val context = LocalContext.current
            var floatOn by remember { mutableStateOf(false) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                floatOn = runCatching { calc.u.system.FloatCalcService.isOverlayGranted(context) }.getOrDefault(false)
            }
            SectionCard("Floating calculator") {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Floating mini-calculator",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = floatOn,
                        onCheckedChange = { on ->
                            if (on) {
                                floatOn = runCatching {
                                    calc.u.system.FloatCalcService.startOrRequestPermission(context.applicationContext)
                                }.getOrDefault(false)
                            } else {
                                runCatching { calc.u.system.FloatCalcService.stop(context.applicationContext) }
                                floatOn = false
                            }
                        }
                    )
                }
            }
        }
    }
    if (showCustomSheet) {
        CustomSeedSheet(
            initialArgb = customSeedArgb,
            onApply = {
                vm.setCustomSeedArgb(it)
                showCustomSheet = false
            },
            onDismiss = { showCustomSheet = false }
        )
    }
}

@Composable
private fun SeedGroupHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() }
    )
}

@SuppressLint("RestrictedApi")
private fun seedSwatches(seed: CalcUThemeSeeds.Seed): List<Color> {
    if (seed.id == "botanical") {
        return listOf(
            Color(0xFF4F6632),
            Color(0xFFDCE7C7),
            Color(0xFF6A5F27),
            Color(0xFFFAFAF0)
        )
    }
    if (seed.id == "obsidian") {
        return listOf(
            Color(0xFF2F5D50),
            Color(0xFFD3E5DD),
            Color(0xFF5A6F68),
            Color(0xFFF2F4F2)
        )
    }
    return runCatching {
        val palette = TonalPalette.fromInt(seed.argb)
        listOf(
            Color(palette.tone(40)),
            Color(palette.tone(90)),
            Color(palette.tone(60)),
            Color(palette.tone(95))
        )
    }.getOrDefault(
        listOf(
            Color(seed.argb),
            Color(seed.argb),
            Color(seed.argb),
            Color(seed.argb)
        )
    )
}

@Composable
private fun SeedGrid(
    seeds: List<CalcUThemeSeeds.Seed>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        seeds.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { seed ->
                    SeedTile(
                        label = seed.label,
                        swatches = seedSwatches(seed),
                        selected = selectedId == seed.id,
                        onClick = { onSelect(seed.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SeedTile(
    label: String,
    swatches: List<Color>,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 2.dp else 1.dp
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SwatchPreview(swatches = swatches, selected = selected)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SwatchPreview(
    swatches: List<Color>,
    selected: Boolean
) {
    val first = swatches.getOrElse(0) { MaterialTheme.colorScheme.primary }
    val second = swatches.getOrElse(1) { MaterialTheme.colorScheme.secondaryContainer }
    val third = swatches.getOrElse(2) { MaterialTheme.colorScheme.tertiary }
    val fourth = swatches.getOrElse(3) { MaterialTheme.colorScheme.surfaceContainerHigh }
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(56.dp)
            .clip(MaterialTheme.shapes.small)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.weight(1f)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(first))
                Box(Modifier.weight(1f).fillMaxHeight().background(second))
            }
            Row(Modifier.weight(1f)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(third))
                Box(Modifier.weight(1f).fillMaxHeight().background(fourth))
            }
        }
        if (selected) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun DynamicSeedTile(
    selected: Boolean,
    dynamicOn: Boolean,
    onSelect: () -> Unit,
    onDynamicChange: (Boolean) -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 2.dp else 1.dp
    val swatches = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.surfaceContainerHigh
    )
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SwatchPreview(swatches = swatches, selected = selected)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Dynamic M3 Fixed",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    "Wallpaper hue, fixed hierarchy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = dynamicOn,
                onCheckedChange = onDynamicChange
            )
        }
    }
}

@Composable
private fun CustomSeedTile(
    selected: Boolean,
    customArgb: Int?,
    onSelect: () -> Unit,
    onCustomize: () -> Unit
) {
    val swatches = if (customArgb != null) {
        seedSwatches(CalcUThemeSeeds.Seed(CustomThemeId, "Custom", customArgb))
    } else {
        val placeholder = MaterialTheme.colorScheme.outlineVariant
        listOf(placeholder, placeholder, placeholder, placeholder)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SeedTile(
            label = if (customArgb != null) "Custom" else "Custom — not set",
            swatches = swatches,
            selected = selected,
            onClick = { if (customArgb != null) onSelect() else onCustomize() },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "HSL sliders + live preview; Apply saves the seed and switches to it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onCustomize) {
                Text("Customize")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomSeedSheet(
    initialArgb: Int?,
    onApply: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val (initH, initS, initL) = remember(initialArgb) {
        seedArgbToHsl(initialArgb ?: hslToSeedArgb(265f, 0.45f, 0.55f))
    }
    var hue by remember(initialArgb) { mutableStateOf(initH) }
    var sat by remember(initialArgb) { mutableStateOf(initS) }
    var light by remember(initialArgb) { mutableStateOf(initL) }
    val previewArgb = hslToSeedArgb(hue, sat, light)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Custom seed",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() }
            )
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(64.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(Color(previewArgb))
            )
            SwatchPreview(
                swatches = seedSwatches(CalcUThemeSeeds.Seed(CustomThemeId, "Custom", previewArgb)),
                selected = false
            )
            HslSliderRow(
                label = "Hue",
                valueText = "${hue.roundToInt()}°",
                value = hue,
                range = 0f..360f,
                onChange = { hue = it.coerceIn(0f, 360f) }
            )
            HslSliderRow(
                label = "Saturation",
                valueText = "${(sat * 100f).roundToInt()}%",
                value = sat,
                range = 0f..1f,
                onChange = { sat = it.coerceIn(0f, 1f) }
            )
            HslSliderRow(
                label = "Lightness",
                valueText = "${(light * 100f).roundToInt()}%",
                value = light,
                range = 0f..1f,
                onChange = { light = it.coerceIn(0f, 1f) }
            )
            Button(
                onClick = { onApply(previewArgb) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("Apply")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HslSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range
        )
    }
}
