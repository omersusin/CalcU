package calc.u.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TapeEntry(
    val id: Long,
    val expression: String,
    val result: String
)

object TapeHolder {
    const val MAX = 200
    private val lock = Any()
    private var nextId = 1L
    private val _entries = MutableStateFlow<List<TapeEntry>>(emptyList())
    val entries: StateFlow<List<TapeEntry>> = _entries.asStateFlow()

    fun add(expression: String, result: String) {
        synchronized(lock) {
            val entry = TapeEntry(nextId++, expression, result)
            _entries.value = (_entries.value + entry).takeLast(MAX)
        }
    }

    fun remove(id: Long) {
        synchronized(lock) {
            _entries.value = _entries.value.filterNot { it.id == id }
        }
    }

    fun clear() {
        synchronized(lock) {
            _entries.value = emptyList()
        }
    }
}
