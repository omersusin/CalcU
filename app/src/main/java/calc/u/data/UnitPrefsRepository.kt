package calc.u.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.unitsDataStore by preferencesDataStore("calcu-units")

@Singleton
class UnitPrefsRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    fun favoritesFlow(cat: String): Flow<Set<String>> =
        ctx.unitsDataStore.data.map { it[stringSetPreferencesKey("fav_$cat")] ?: emptySet() }

    suspend fun getPair(cat: String): Pair<String?, String?> {
        val prefs = ctx.unitsDataStore.data.first()
        return prefs[stringPreferencesKey("pair_${cat}_from")] to prefs[stringPreferencesKey("pair_${cat}_to")]
    }

    suspend fun savePair(cat: String, from: String, to: String) {
        ctx.unitsDataStore.edit {
            it[stringPreferencesKey("pair_${cat}_from")] = from
            it[stringPreferencesKey("pair_${cat}_to")] = to
        }
    }

    suspend fun toggleFavorite(cat: String, unit: String): Set<String> {
        val key = stringSetPreferencesKey("fav_$cat")
        var result = emptySet<String>()
        ctx.unitsDataStore.edit {
            val current = it[key] ?: emptySet()
            result = if (unit in current) current - unit else current + unit
            it[key] = result
        }
        return result
    }
}
