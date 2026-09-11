package calc.u.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import calc.u.core.Currency
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fxDataStore by preferencesDataStore("calcu-fx")

enum class RateSource(val label: String) {
    LIVE("live"),
    CACHED("cached"),
    OFFLINE("offline")
}

@Serializable
private data class FxResponse(val rates: Map<String, Double> = emptyMap())

private interface FxService {
    @GET("latest/USD")
    suspend fun latestUsd(): FxResponse
}

@Singleton
class CurrencyRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }
    private val ratesKey = stringPreferencesKey("rates_json")
    private val tsKey = longPreferencesKey("rates_ts")

    private val api: FxService = Retrofit.Builder()
        .baseUrl("https://open.er-api.com/v6/")
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FxService::class.java)

    private val _source = MutableStateFlow(RateSource.OFFLINE)
    val source: StateFlow<RateSource> = _source.asStateFlow()

    val rates: Flow<Map<String, Double>> = ctx.fxDataStore.data.map { prefs ->
        val raw = prefs[ratesKey]
        if (raw.isNullOrEmpty()) Currency.fallbackUsdRates
        else runCatching { json.decodeFromString<Map<String, Double>>(raw) }.getOrNull()?.takeIf { it.isNotEmpty() } ?: Currency.fallbackUsdRates
    }

    val isStale: Flow<Boolean> = ctx.fxDataStore.data.map { prefs ->
        val ts = prefs[tsKey] ?: 0L
        System.currentTimeMillis() - ts > STALE_MS
    }

    suspend fun refresh(): RateSource {
        return try {
            val res = api.latestUsd()
            if (res.rates.isEmpty()) throw IllegalStateException("empty rates")
            ctx.fxDataStore.edit {
                it[ratesKey] = json.encodeToString(res.rates)
                it[tsKey] = System.currentTimeMillis()
            }
            _source.value = RateSource.LIVE
            RateSource.LIVE
        } catch (e: IOException) {
            val cached = runCatching { ctx.fxDataStore.data.first()[ratesKey] }.getOrNull()
            val hasCache = !cached.isNullOrEmpty()
            _source.value = if (hasCache) RateSource.CACHED else RateSource.OFFLINE
            _source.value
        } catch (e: Exception) {
            _source.value = RateSource.OFFLINE
            RateSource.OFFLINE
        }
    }

    companion object {
        private const val STALE_MS = 24L * 60L * 60L * 1000L
    }
}
