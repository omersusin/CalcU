package calc.u.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import calc.u.core.Engine
import calc.u.data.HistoryRepository
import calc.u.data.SettingsRepository
import calc.u.widget.CalcUWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class CalcUiState(
    val input: String = "",
    val result: String = "",
    val angleDeg: Boolean = true,
    val memory: Double = 0.0,
    val history: List<String> = emptyList(),
    val query: String = "",
    val showGraphTip: Boolean = false
) {
    val canEvaluate: Boolean get() = input.isNotBlank()
}

sealed interface CalcEffect {
    data class Copy(val text: String) : CalcEffect
}

@HiltViewModel
class CalcViewModel @Inject constructor(
    private val historyRepo: HistoryRepository,
    private val settingsRepo: SettingsRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalcUiState())
    val uiState: StateFlow<CalcUiState> = _uiState.asStateFlow()
    private val _effects = Channel<CalcEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()
    val vibration: StateFlow<Boolean> =
        settingsRepo.vibration.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val activity: StateFlow<Map<Long, Int>> =
        historyRepo.activityLast14Days().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            historyRepo.history.collect { h -> _uiState.update { it.copy(history = h) } }
        }
        viewModelScope.launch {
            settingsRepo.graphTipSeen.collect { seen ->
                _uiState.update { it.copy(showGraphTip = !seen) }
            }
        }
    }

    fun onInput(s: String) { _uiState.update { it.copy(input = it.input + s) }; evaluate() }
    fun onClear() { _uiState.update { it.copy(input = "", result = "") } }
    fun onBackspace() { _uiState.update { it.copy(input = it.input.dropLast(1)) }; evaluate() }
    fun onToggleAngle() { _uiState.update { it.copy(angleDeg = !it.angleDeg) }; evaluate() }
    fun onQueryChange(q: String) { _uiState.update { it.copy(query = q) } }

    fun onCopyResult() {
        val r = _uiState.value.result
        if (r.isNotBlank()) _effects.trySend(CalcEffect.Copy(r))
    }

    fun onEquals() {
        val st = _uiState.value
        Engine.eval(st.input, st.angleDeg).onSuccess {
            val r = Engine.format(it)
            _uiState.update { s -> s.copy(result = r) }
            viewModelScope.launch {
                historyRepo.push(st.input, r)
                refreshWidget()
            }
        }.onFailure {
            _uiState.update { s -> s.copy(result = "Error") }
        }
    }

    private fun refreshWidget() {
        try {
            val mgr = AppWidgetManager.getInstance(appContext)
            val ids = mgr.getAppWidgetIds(ComponentName(appContext, CalcUWidget::class.java))
            if (ids.isNotEmpty()) {
                appContext.sendBroadcast(
                    Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun evaluate() {
        val st = _uiState.value
        if (st.input.isBlank()) { _uiState.update { it.copy(result = "") }; return }
        Engine.eval(st.input, st.angleDeg).onSuccess {
            _uiState.update { s -> s.copy(result = Engine.format(it)) }
        }.onFailure { _uiState.update { s -> s.copy(result = "") } }
    }

    fun onMemPlus() {
        _uiState.value.result.toDoubleOrNull()?.let { v ->
            _uiState.update { it.copy(memory = it.memory + v) }
        }
    }
    fun onMemMinus() {
        _uiState.value.result.toDoubleOrNull()?.let { v ->
            _uiState.update { it.copy(memory = it.memory - v) }
        }
    }
    fun onMemRecall() { _uiState.update { it.copy(input = it.input + Engine.format(BigDecimal.valueOf(it.memory))) }; evaluate() }
    fun onMemClear() { _uiState.update { it.copy(memory = 0.0) } }
    fun onClearHistory() { viewModelScope.launch { historyRepo.clear() } }
    fun onHistoryTap(entry: String) {
        val body = entry.substringAfter("|", entry).let {
            if (entry.count { c -> c == '|' } >= 2) it.substringBeforeLast("|") else it
        }
        val expr = if ("=" in body) body.substringBeforeLast("=") else body
        if (expr.isBlank()) return
        _uiState.update { it.copy(input = expr) }
        evaluate()
    }
    fun onDeleteHistoryAt(index: Int) { viewModelScope.launch { historyRepo.deleteAt(index) } }
    fun onSetHistoryNote(index: Int, note: String) { viewModelScope.launch { historyRepo.setNote(index, note) } }
    fun onDismissGraphTip() {
        _uiState.update { it.copy(showGraphTip = false) }
        viewModelScope.launch { settingsRepo.setGraphTipSeen() }
    }
}
