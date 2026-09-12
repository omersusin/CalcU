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

    private val numberFormatKey = stringPreferencesKey("grouping")

    val numberFormat: Flow<String> = ctx.settingsDataStore.data.map { it[numberFormatKey] ?: "locale" }.catch { emit("locale") }

    suspend fun setNumberFormat(value: String) {
        val coerced = if (value in setOf("comma", "space", "none", "indian")) value else "comma"
        runCatching { ctx.settingsDataStore.edit { it[numberFormatKey] = coerced } }
    }

    private val decimalsKey = androidx.datastore.preferences.core.intPreferencesKey("decimals")

    val decimals: Flow<Int> = ctx.settingsDataStore.data.map { (it[decimalsKey] ?: 10).coerceIn(0, 12) }.catch { emit(10) }

    suspend fun setDecimals(value: Int) {
        val coerced = value.coerceIn(0, 12)
        runCatching { ctx.settingsDataStore.edit { it[decimalsKey] = coerced } }
    }

    private val fractionsKey = booleanPreferencesKey("fractions")

    val fractions: Flow<Boolean> = ctx.settingsDataStore.data.map { it[fractionsKey] ?: true }.catch { emit(true) }

    suspend fun setFractions(value: Boolean) {
        runCatching { ctx.settingsDataStore.edit { it[fractionsKey] = value } }
    }

    private val keepScreenOnKey = booleanPreferencesKey("keep_screen_on")

    val keepScreenOn: Flow<Boolean> = ctx.settingsDataStore.data.map { it[keepScreenOnKey] ?: false }.catch { emit(false) }

    suspend fun setKeepScreenOn(value: Boolean) {
        runCatching { ctx.settingsDataStore.edit { it[keepScreenOnKey] = value } }
    }

    private val memoryRowKey = booleanPreferencesKey("memory_row")

    val memoryRow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[memoryRowKey] ?: true }.catch { emit(true) }

    suspend fun setMemoryRow(value: Boolean) {
        runCatching { ctx.settingsDataStore.edit { it[memoryRowKey] = value } }
    }

    private val engineeringKey = booleanPreferencesKey("engineering")

    val engineering: Flow<Boolean> = ctx.settingsDataStore.data.map { it[engineeringKey] ?: false }.catch { emit(false) }

    suspend fun setEngineering(value: Boolean) {
        runCatching { ctx.settingsDataStore.edit { it[engineeringKey] = value } }
    }

    private val precisionSliderKey = androidx.datastore.preferences.core.intPreferencesKey("precision_slider")

    val precisionSlider: Flow<Int> = ctx.settingsDataStore.data.map { (it[precisionSliderKey] ?: 10).coerceIn(0, 16) }.catch { emit(10) }

    suspend fun setPrecisionSlider(value: Int) {
        val coerced = value.coerceIn(0, 16)
        runCatching { ctx.settingsDataStore.edit { it[precisionSliderKey] = coerced } }
    }

    private val keypadLayoutKey = stringPreferencesKey("keypad_layout")

    val keypadLayout: Flow<String> = ctx.settingsDataStore.data.map {
        val raw = it[keypadLayoutKey] ?: "simple"
        if (raw in setOf("simple", "classic", "modern")) raw else "simple"
    }.catch { emit("simple") }

    suspend fun setKeypadLayout(value: String) {
        val coerced = if (value in setOf("simple", "classic", "modern")) value else "simple"
        runCatching { ctx.settingsDataStore.edit { it[keypadLayoutKey] = coerced } }
    }
}
