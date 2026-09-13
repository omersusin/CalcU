package calc.u.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("calcu")

@Singleton
class HistoryRepository @Inject constructor(@ApplicationContext private val ctx: Context, private val settingsRepo: SettingsRepository) {
    private val key = stringPreferencesKey("history")
    val history: Flow<List<String>> = ctx.dataStore.data.map {
        try { Json.decodeFromString<List<String>>(it[key] ?: "[]") } catch (e: Exception) { emptyList() }
    }.catch { emit(emptyList()) }
    suspend fun push(expr: String, result: String) {
        runCatching {
            val cap = currentCap()
            val entry = "${System.currentTimeMillis()}|$expr=$result|"
            ctx.dataStore.edit { p ->
                val cur: MutableList<String> = try { Json.decodeFromString<MutableList<String>>(p[key] ?: "[]") } catch (e: Exception) { mutableListOf() }
                cur.add(0, entry)
                p[key] = runCatching { Json.encodeToString(cur.take(cap)) }.getOrDefault("[]")
            }
        }
    }

    private suspend fun currentCap(): Int =
        runCatching { settingsRepo.historyCap.first() }.getOrDefault(200)
    fun search(q: String): Flow<List<String>> = history.map { list ->
        if (q.isBlank()) list else list.filter { it.contains(q, ignoreCase = true) }
    }
    suspend fun deleteAt(index: Int) {
        runCatching {
            ctx.dataStore.edit { p ->
                val cur: MutableList<String> = try { Json.decodeFromString<MutableList<String>>(p[key] ?: "[]") } catch (e: Exception) { mutableListOf() }
                if (index in cur.indices) cur.removeAt(index)
                p[key] = runCatching { Json.encodeToString(cur.take(currentCap())) }.getOrDefault("[]")
            }
        }
    }
    suspend fun setNote(index: Int, note: String) {
        runCatching {
            val clean = runCatching { note.replace("|", "/").replace("\n", " ").take(140) }.getOrDefault("")
            ctx.dataStore.edit { p ->
                val cur: MutableList<String> = try { Json.decodeFromString<MutableList<String>>(p[key] ?: "[]") } catch (e: Exception) { mutableListOf() }
                if (index in cur.indices) {
                    val parts = runCatching { cur.getOrNull(index)?.split("|", limit = 3) }.getOrNull()
                    val ts = parts?.getOrNull(0) ?: System.currentTimeMillis().toString()
                    val body = parts?.getOrNull(1) ?: ""
                    cur[index] = "$ts|$body|$clean"
                }
                p[key] = runCatching { Json.encodeToString(cur.take(currentCap())) }.getOrDefault("[]")
            }
        }
    }
    suspend fun clear() { runCatching { ctx.dataStore.edit { it.remove(key) } } }
    fun activityLast14Days(): Flow<Map<Long, Int>> = history.map { list ->
        runCatching {
            val today = System.currentTimeMillis() / 86400000L
            val counts = mutableMapOf<Long, Int>()
            for (e in list) {
                val ts = e.substringBefore("|").toLongOrNull() ?: continue
                val day = ts / 86400000L
                if (day in (today - 13)..today) {
                    counts[day] = (counts[day] ?: 0) + 1
                }
            }
            counts.toMap()
        }.getOrDefault(emptyMap())
    }.catch { emit(emptyMap()) }
}
