package calc.u.data

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore("calcu-settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val themeKey = stringPreferencesKey("theme")

    val theme: Flow<String> = ctx.settingsDataStore.data.map { it[themeKey] ?: "system" }

    suspend fun setTheme(value: String) {
        ctx.settingsDataStore.edit { it[themeKey] = value }
    }

    private val tipKey = booleanPreferencesKey("graph_tip_seen")

    val graphTipSeen: Flow<Boolean> = ctx.settingsDataStore.data.map { it[tipKey] ?: false }

    suspend fun setGraphTipSeen() {
        ctx.settingsDataStore.edit { it[tipKey] = true }
    }

    private val vibrationKey = booleanPreferencesKey("vibration")

    val vibration: Flow<Boolean> = ctx.settingsDataStore.data.map { it[vibrationKey] ?: true }

    suspend fun setVibration(value: Boolean) {
        ctx.settingsDataStore.edit { it[vibrationKey] = value }
    }

    private val dynamicKey = booleanPreferencesKey("dynamic_color")

    val dynamicColor: Flow<Boolean> = ctx.settingsDataStore.data.map {
        it[dynamicKey] ?: (Build.VERSION.SDK_INT >= 31)
    }

    suspend fun setDynamicColor(value: Boolean) {
        ctx.settingsDataStore.edit { it[dynamicKey] = value }
    }
}
