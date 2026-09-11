package calc.u.data

import android.content.Context
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
}
