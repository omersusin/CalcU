package calc.u.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("calcu")

@Singleton
class HistoryRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val key = stringPreferencesKey("history")
    val history: Flow<List<String>> = ctx.dataStore.data.map {
        try { Json.decodeFromString<List<String>>(it[key] ?: "[]") } catch (e: Exception) { emptyList() }
    }
    suspend fun push(expr: String, result: String) {
        val entry = "${System.currentTimeMillis()}|$expr=$result"
        ctx.dataStore.edit { p ->
            val cur: MutableList<String> = try { Json.decodeFromString<MutableList<String>>(p[key] ?: "[]") } catch (e: Exception) { mutableListOf() }
            cur.add(0, entry)
            p[key] = Json.encodeToString(cur.take(200))
        }
    }
    fun search(q: String): Flow<List<String>> = history.map { list ->
        if (q.isBlank()) list else list.filter { it.contains(q, ignoreCase = true) }
    }
    suspend fun deleteAt(index: Int) {
        ctx.dataStore.edit { p ->
            val cur: MutableList<String> = try { Json.decodeFromString<MutableList<String>>(p[key] ?: "[]") } catch (e: Exception) { mutableListOf() }
            if (index in cur.indices) cur.removeAt(index)
            p[key] = Json.encodeToString(cur.take(200))
        }
    }
    suspend fun clear() { ctx.dataStore.edit { it.remove(key) } }
}
