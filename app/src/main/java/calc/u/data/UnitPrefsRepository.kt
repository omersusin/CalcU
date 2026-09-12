package calc.u.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.unitsDataStore by preferencesDataStore("calcu-units")

@Singleton
class UnitPrefsRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    fun favoritesFlow(cat: String): Flow<Set<String>> =
        ctx.unitsDataStore.data.map {
            runCatching { it[stringSetPreferencesKey("fav_$cat")] ?: emptySet() }.getOrDefault(emptySet())
        }.catch { emit(emptySet()) }

    suspend fun getPair(cat: String): Pair<String?, String?> {
        return runCatching {
            val prefs = ctx.unitsDataStore.data.first()
            prefs[stringPreferencesKey("pair_${cat}_from")] to prefs[stringPreferencesKey("pair_${cat}_to")]
        }.getOrDefault(null to null)
    }

    suspend fun savePair(cat: String, from: String, to: String) {
        runCatching {
            ctx.unitsDataStore.edit {
                it[stringPreferencesKey("pair_${cat}_from")] = from
                it[stringPreferencesKey("pair_${cat}_to")] = to
            }
        }
    }

    suspend fun toggleFavorite(cat: String, unit: String): Set<String> {
        return runCatching {
            val key = stringSetPreferencesKey("fav_$cat")
            var result = emptySet<String>()
            ctx.unitsDataStore.edit {
                val current = runCatching { it[key] ?: emptySet() }.getOrDefault(emptySet())
                result = if (unit in current) current - unit else current + unit
                it[key] = result
            }
            result
        }.getOrDefault(emptySet())
    }
}
