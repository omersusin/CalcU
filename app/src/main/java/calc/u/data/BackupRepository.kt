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
        val hprefs = ctx.backupHistoryStore.data.first()
        val history = try {
            Json.decodeFromString<List<String>>(hprefs[stringPreferencesKey("history")] ?: "[]")
        } catch (e: Exception) {
            emptyList()
        }
        val sprefs = ctx.backupSettingsStore.data.first()
        val theme = sprefs[stringPreferencesKey("theme")] ?: "system"
        val vibration = sprefs[booleanPreferencesKey("vibration")] ?: true
        val uprefs = ctx.backupUnitsStore.data.first()
        val favorites = uprefs.asMap().entries.mapNotNull { (k, v) ->
            if (!k.name.startsWith("fav_")) null
            else k.name.removePrefix("fav_") to ((v as? Set<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet())
        }.toMap()
        return Json.encodeToString(Backup(history, theme, vibration, favorites))
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
        ctx.backupHistoryStore.edit {
            it[stringPreferencesKey("history")] = Json.encodeToString(backup.history)
        }
        ctx.backupSettingsStore.edit {
            it[stringPreferencesKey("theme")] = backup.theme
            it[booleanPreferencesKey("vibration")] = backup.vibration
        }
        ctx.backupUnitsStore.edit { prefs ->
            prefs.asMap().keys.map { it.name }
                .filter { it.startsWith("fav_") }
                .forEach { prefs.remove(stringSetPreferencesKey(it)) }
            backup.favorites.forEach { (cat, set) ->
                prefs[stringSetPreferencesKey("fav_$cat")] = set
            }
        }
        return backup.history.size + backup.favorites.values.sumOf { it.size }
    }
}
