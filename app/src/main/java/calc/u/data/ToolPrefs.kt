package calc.u.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.toolsDataStore by preferencesDataStore("calcu-tools")

@Singleton
class ToolPrefs @Inject constructor(@ApplicationContext private val ctx: Context) {
    companion object {
        val DefaultHubOrder = listOf(
            "calc", "graph", "convert", "finance", "math", "steps",
            "time", "electro", "textdata", "everyday", "qrscan", "sensors", "ruler"
        )
        private const val MAX_RECENTS = 3
    }

    private val orderKey = stringPreferencesKey("hub_order")
    private val recentsKey = stringPreferencesKey("recent_tools")
    private val favKey = stringPreferencesKey("fav_tools")

    val hubOrder: Flow<List<String>> = ctx.toolsDataStore.data.map { prefs ->
        prefs[orderKey]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.takeIf { it.isNotEmpty() } ?: DefaultHubOrder
    }.catch { emit(DefaultHubOrder) }

    suspend fun setHubOrder(order: List<String>) {
        runCatching { ctx.toolsDataStore.edit { it[orderKey] = order.joinToString(",") } }
    }

    val recentTools: Flow<List<String>> = ctx.toolsDataStore.data.map { prefs ->
        prefs[recentsKey]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.take(MAX_RECENTS) ?: emptyList()
    }.catch { emit(emptyList()) }

    suspend fun record(route: String) {
        runCatching {
            ctx.toolsDataStore.edit { prefs ->
                val current = prefs[recentsKey]?.split(",")?.map { it.trim() }
                    ?.filter { it.isNotEmpty() } ?: emptyList()
                prefs[recentsKey] = ((listOf(route) + current).distinct().take(MAX_RECENTS))
                    .joinToString(",")
            }
        }
    }

    val favTools: Flow<Set<String>> = ctx.toolsDataStore.data.map { prefs ->
        prefs[favKey]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.toSet() ?: emptySet()
    }.catch { emit(emptySet()) }

    suspend fun toggleFavTool(route: String) {
        runCatching {
            ctx.toolsDataStore.edit { prefs ->
                val current = prefs[favKey]?.split(",")?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
                val updated = if (current.contains(route)) current - route else current + route
                prefs[favKey] = updated.joinToString(",")
            }
        }
    }
}
