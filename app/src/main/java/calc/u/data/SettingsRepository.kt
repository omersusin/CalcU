package calc.u.data

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore("calcu-settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val themeKey = stringPreferencesKey("theme")

    val theme: Flow<String> = ctx.settingsDataStore.data.map { it[themeKey] ?: "system" }.catch { emit("system") }

    suspend fun setTheme(value: String) {
        runCatching { ctx.settingsDataStore.edit { it[themeKey] = value } }
    }

    private val tipKey = booleanPreferencesKey("graph_tip_seen")

    val graphTipSeen: Flow<Boolean> = ctx.settingsDataStore.data.map { it[tipKey] ?: false }.catch { emit(false) }

    suspend fun setGraphTipSeen() {
        runCatching { ctx.settingsDataStore.edit { it[tipKey] = true } }
    }

    private val vibrationKey = booleanPreferencesKey("vibration")

    val vibration: Flow<Boolean> = ctx.settingsDataStore.data.map { it[vibrationKey] ?: true }.catch { emit(true) }

    suspend fun setVibration(value: Boolean) {
        runCatching { ctx.settingsDataStore.edit { it[vibrationKey] = value } }
    }

    private val dynamicKey = booleanPreferencesKey("dynamic_color")

    val dynamicColor: Flow<Boolean> = ctx.settingsDataStore.data.map {
        it[dynamicKey] ?: (Build.VERSION.SDK_INT >= 31)
    }.catch { emit(Build.VERSION.SDK_INT >= 31) }

    suspend fun setDynamicColor(value: Boolean) {
        runCatching { ctx.settingsDataStore.edit { it[dynamicKey] = value } }
    }

    private val tallyCountKey = androidx.datastore.preferences.core.intPreferencesKey("tally_count")

    val tallyCount: Flow<Int> = ctx.settingsDataStore.data.map { it[tallyCountKey] ?: 0 }.catch { emit(0) }

    suspend fun setTallyCount(value: Int) {
        runCatching { ctx.settingsDataStore.edit { it[tallyCountKey] = value } }
    }

    private val tourSeenKey = booleanPreferencesKey("tour_seen")

    val tourSeen: Flow<Boolean> = ctx.settingsDataStore.data.map { it[tourSeenKey] ?: false }.catch { emit(false) }

    suspend fun setTourSeen() {
        runCatching { ctx.settingsDataStore.edit { it[tourSeenKey] = true } }
    }

    private val historyCapKey = androidx.datastore.preferences.core.intPreferencesKey("history_cap")

    val historyCap: Flow<Int> = ctx.settingsDataStore.data.map { (it[historyCapKey] ?: 200).coerceIn(10, 2000) }.catch { emit(200) }

    suspend fun setHistoryCap(value: Int) {
        val coerced = value.coerceIn(10, 2000)
        runCatching { ctx.settingsDataStore.edit { it[historyCapKey] = coerced } }
    }
}
