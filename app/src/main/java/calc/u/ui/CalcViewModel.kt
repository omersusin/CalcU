package calc.u.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import calc.u.core.Engine
import calc.u.data.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalcUiState(
    val input: String = "",
    val result: String = "",
    val angleDeg: Boolean = true,
    val memory: Double = 0.0,
    val history: List<String> = emptyList()
) {
    val canEvaluate: Boolean get() = input.isNotBlank()
}

sealed interface CalcEffect {
    data class Copy(val text: String) : CalcEffect
}

@HiltViewModel
class CalcViewModel @Inject constructor(private val historyRepo: HistoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CalcUiState())
    val uiState: StateFlow<CalcUiState> = _uiState.asStateFlow()
    private val _effects = Channel<CalcEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            historyRepo.history.collect { h -> _uiState.update { it.copy(history = h) } }
        }
    }

    fun onInput(s: String) { _uiState.update { it.copy(input = it.input + s) }; evaluate() }
    fun onClear() { _uiState.update { it.copy(input = "", result = "") } }
    fun onBackspace() { _uiState.update { it.copy(input = it.input.dropLast(1)) }; evaluate() }
    fun onToggleAngle() { _uiState.update { it.copy(angleDeg = !it.angleDeg) }; evaluate() }

    fun onEquals() {
        val st = _uiState.value
        Engine.eval(st.input, st.angleDeg).onSuccess {
            val r = Engine.format(it)
            _uiState.update { s -> s.copy(result = r) }
            viewModelScope.launch { historyRepo.push("${st.input} = $r") }
        }.onFailure {
            _uiState.update { s -> s.copy(result = "Error") }
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
    fun onMemRecall() { _uiState.update { it.copy(input = it.input + it.memory.toString()) }; evaluate() }
    fun onMemClear() { _uiState.update { it.copy(memory = 0.0) } }
    fun onClearHistory() { viewModelScope.launch { historyRepo.clear() } }
}
