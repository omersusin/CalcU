package calc.u.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import calc.u.data.SettingsRepository
import calc.u.ui.FluentExpander
import calc.u.ui.SectionCard
import com.microsoft.fluentui.tokenized.controls.RadioButton
import com.microsoft.fluentui.tokenized.controls.ToggleSwitch
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

    fun setTheme(value: String) {
        viewModelScope.launch { repo.setTheme(value) }
    }

    fun setVibration(value: Boolean) {
        viewModelScope.launch { repo.setVibration(value) }
    }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    val vibration by vm.vibration.collectAsStateWithLifecycle()
    val options = listOf(
        "system" to "System",
        "light" to "Light",
        "dark" to "Dark",
        "amoled" to "AMOLED",
        "contrast" to "High contrast"
    )
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Appearance") {
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
                                onClick = { vm.setTheme(id) },
                                selected = theme == id
                            )
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
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
                    ToggleSwitch(
                        onValueChange = { vm.setVibration(it) },
                        checkedState = vibration
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
    }
}
