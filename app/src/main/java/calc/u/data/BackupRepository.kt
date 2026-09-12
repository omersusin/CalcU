package calc.u.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.backupHistoryStore by preferencesDataStore("calcu")
private val Context.backupSettingsStore by preferencesDataStore("calcu-settings")
private val Context.backupUnitsStore by preferencesDataStore("calcu-units")

@Serializable
data class Backup(
    val history: List<String> = emptyList(),
    val theme: String = "system",
    val vibration: Boolean = true,
    val favorites: Map<String, Set<String>> = emptyMap()
)

@Singleton
class BackupRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    suspend fun export(): String {
        return runCatching {
            val hprefs = runCatching { ctx.backupHistoryStore.data.first() }.getOrNull()
            val history = try {
                Json.decodeFromString<List<String>>(hprefs?.get(stringPreferencesKey("history")) ?: "[]")
            } catch (e: Exception) {
                emptyList()
            }
            val sprefs = runCatching { ctx.backupSettingsStore.data.first() }.getOrNull()
            val theme = sprefs?.get(stringPreferencesKey("theme")) ?: "system"
            val vibration = sprefs?.get(booleanPreferencesKey("vibration")) ?: true
            val uprefs = runCatching { ctx.backupUnitsStore.data.first() }.getOrNull()
            val favorites: Map<String, Set<String>> = runCatching {
                uprefs?.asMap()?.entries?.mapNotNull { e ->
                    if (!e.key.name.startsWith("fav_")) null
                    else e.key.name.removePrefix("fav_") to (
                        (e.value as? Set<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet<String>()
                    )
                }?.toMap() ?: emptyMap()
            }.getOrDefault(emptyMap())
            Json.encodeToString(Backup(history, theme, vibration, favorites))
        }.getOrDefault("{\"history\":[],\"theme\":\"system\",\"vibration\":true,\"favorites\":{}}")
    }

    suspend fun import(json: String): Int {
        val backup = try {
            Json.decodeFromString<Backup>(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid file")
        }
        require(backup.history.size < 10000) { "Invalid file" }
        require(backup.favorites.size < 10000) { "Invalid file" }
        require(backup.favorites.values.all { it.size < 10000 }) { "Invalid file" }
        require(backup.theme.length < 100) { "Invalid file" }
        val safeHistory = backup.history.take(10000)
        val safeFavorites = backup.favorites.toList().take(10000).toMap()
            .mapValues { (_, v) -> v.take(10000).toSet() }
        runCatching {
            ctx.backupHistoryStore.edit {
                it[stringPreferencesKey("history")] = runCatching { Json.encodeToString(safeHistory) }.getOrDefault("[]")
            }
        }
        runCatching {
            ctx.backupSettingsStore.edit {
                it[stringPreferencesKey("theme")] = backup.theme
                it[booleanPreferencesKey("vibration")] = backup.vibration
            }
        }
        runCatching {
            ctx.backupUnitsStore.edit { prefs ->
                prefs.asMap().keys.map { it.name }
                    .filter { it.startsWith("fav_") }
                    .forEach { prefs.remove(stringSetPreferencesKey(it)) }
                safeFavorites.forEach { (cat, set) ->
                    val safeCat = cat.take(100)
                    prefs[stringSetPreferencesKey("fav_$safeCat")] = set.map { it.take(100) }.toSet()
                }
            }
        }
        return safeHistory.size + safeFavorites.values.sumOf { it.size }
    }
}
