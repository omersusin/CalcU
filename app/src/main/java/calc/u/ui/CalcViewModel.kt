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
            runCatching {
                historyRepo.history.collect { h -> _uiState.update { it.copy(history = h) } }
            }
        }
        viewModelScope.launch {
            runCatching {
                settingsRepo.graphTipSeen.collect { seen ->
                    _uiState.update { it.copy(showGraphTip = !seen) }
                }
            }
        }
    }

    fun onInput(s: String) {
        _uiState.update {
            val cur = it.input
            val needMul = cur.isNotEmpty() && cur.last() in ")!%0123456789πe" &&
                s.isNotEmpty() && (s.first() == '(' || s.first() == '√' || s.first().isLetter())
            it.copy(input = if (needMul) cur + "×" + s else cur + s)
        }
        evaluate()
    }
    private var lastResult: String = ""
    fun onAns() {
        val ans = lastResult.ifBlank { "0" }
        _uiState.update {
            val cur = it.input
            val needMul = cur.isNotEmpty() && cur.last() in ")!%0123456789πe"
            it.copy(input = if (needMul) cur + "×" + ans else cur + ans)
        }
        evaluate()
    }
    fun onClear() { _uiState.update { it.copy(input = "", result = "") } }
    fun onBackspace() { _uiState.update { it.copy(input = atomicBackspace(it.input)) }; evaluate() }
    private fun atomicBackspace(input: String): String {
        val tokens = listOf("asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "log(", "ln(", "√(", "10^(", "e^(")
        for (tok in tokens.sortedByDescending { it.length }) {
            if (input.endsWith(tok)) return input.dropLast(tok.length)
        }
        return input.dropLast(1)
    }
    fun onToggleAngle() { _uiState.update { it.copy(angleDeg = !it.angleDeg) }; evaluate() }
    fun onQueryChange(q: String) { _uiState.update { it.copy(query = q) } }

    fun onCopyResult() {
        val r = _uiState.value.result
        if (r.isNotBlank()) _effects.trySend(CalcEffect.Copy(r))
    }

    fun onEquals() {
        val st = _uiState.value
        Engine.validateExpr(st.input)?.let { msg ->
            _uiState.update { s -> s.copy(result = msg) }
            return
        }
        runCatching { Engine.eval(st.input, st.angleDeg) }.getOrNull()?.onSuccess {
            val r = runCatching { Engine.format(it) }.getOrDefault("Error")
            if (r != "Error") lastResult = r
            _uiState.update { s -> s.copy(result = r) }
            viewModelScope.launch {
                runCatching { historyRepo.push(st.input, r) }
                refreshWidget()
            }
        }?.onFailure {
            _uiState.update { s -> s.copy(result = "Error") }
        } ?: _uiState.update { s -> s.copy(result = "Error") }
    }

    private fun refreshWidget() {
        try {
            val mgr = AppWidgetManager.getInstance(appContext)
            val ids = mgr.getAppWidgetIds(ComponentName(appContext, CalcUWidget::class.java))
            if (ids.isNotEmpty()) {
                appContext.sendBroadcast(
                    Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .setClass(appContext, CalcUWidget::class.java)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun evaluate() {
        val st = _uiState.value
        if (st.input.isBlank()) { _uiState.update { it.copy(result = "") }; return }
        if (Engine.validateExpr(st.input) != null) { _uiState.update { it.copy(result = "") }; return }
        runCatching { Engine.eval(st.input, st.angleDeg) }.getOrNull()?.onSuccess {
            val formatted = runCatching { Engine.format(it) }.getOrNull()
            if (formatted != null) _uiState.update { s -> s.copy(result = formatted) }
            else _uiState.update { s -> s.copy(result = "") }
        }?.onFailure { _uiState.update { s -> s.copy(result = "") } }
            ?: _uiState.update { s -> s.copy(result = "") }
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
    fun onMemRecall() {
        val formatted = runCatching { Engine.format(BigDecimal.valueOf(safeMemory())) }.getOrNull()
        if (formatted != null) {
            _uiState.update { it.copy(input = it.input + formatted) }
            evaluate()
        }
    }
    private fun safeMemory(): Double {
        val m = _uiState.value.memory
        return if (m.isFinite()) m else 0.0
    }
    fun onMemClear() { _uiState.update { it.copy(memory = 0.0) } }
    fun onClearHistory() { viewModelScope.launch { runCatching { historyRepo.clear() } } }
    fun onHistoryTap(entry: String) {
        val body = runCatching {
            entry.substringAfter("|", entry).let {
                if (entry.count { c -> c == '|' } >= 2) it.substringBeforeLast("|") else it
            }
        }.getOrDefault(entry)
        val expr = runCatching {
            if ("=" in body) body.substringBeforeLast("=") else body
        }.getOrDefault(body)
        if (expr.isBlank()) return
        _uiState.update { it.copy(input = expr) }
        evaluate()
    }
    fun onDeleteHistoryAt(index: Int) { viewModelScope.launch { runCatching { historyRepo.deleteAt(index) } } }
    fun onSetHistoryNote(index: Int, note: String) { viewModelScope.launch { runCatching { historyRepo.setNote(index, note) } } }
    fun onDismissGraphTip() {
        _uiState.update { it.copy(showGraphTip = false) }
        viewModelScope.launch { runCatching { settingsRepo.setGraphTipSeen() } }
    }
}
