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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fxDataStore by preferencesDataStore("calcu-fx")

enum class RateSource(val label: String) {
    LIVE("live"),
    CACHED("cached"),
    OFFLINE("offline")
}

@Serializable
private data class FxResponse(
    val rates: Map<String, Double> = emptyMap(),
    val base: String? = null,
    val date: String? = null,
    val amount: Double? = null
)

private interface FxService {
    // Frankfurter (ECB reference rates): https://api.frankfurter.app/v1
    @GET("latest")
    suspend fun latestUsd(@Query("from") from: String = "USD"): FxResponse
}

@Singleton
class CurrencyRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }
    private val ratesKey = stringPreferencesKey("rates_json")
    private val tsKey = longPreferencesKey("rates_ts")

    private val api: FxService = Retrofit.Builder()
        .baseUrl("https://api.frankfurter.app/v1/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .callTimeout(20, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FxService::class.java)

    private val _source = MutableStateFlow(RateSource.OFFLINE)
    val source: StateFlow<RateSource> = _source.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)

    /** Last refresh failure message, null after a successful refresh. Additive; screens may ignore it. */
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    /**
     * Bundled Frankfurter/ECB snapshot covering [Currency.codes] gaps.
     * Currency.fallbackUsdRates only carries ~9 codes, so offline mode merges
     * this table underneath the cached/live rates. Units per 1 USD.
     */
    private val bundledFallback: Map<String, Double> = mapOf(
        "USD" to 1.0, "EUR" to 0.92, "GBP" to 0.79, "JPY" to 149.5, "ISK" to 138.5,
        "INR" to 83.2, "CNY" to 7.24, "TRY" to 32.8, "AED" to 3.6725,
        "CHF" to 0.89, "CAD" to 1.37, "AUD" to 1.52, "NZD" to 1.68, "SGD" to 1.34,
        "HKD" to 7.78, "TWD" to 32.4, "KRW" to 1380.0, "MXN" to 20.5, "BRL" to 5.7,
        "ARS" to 1000.0, "CLP" to 950.0, "COP" to 4400.0, "PEN" to 3.75,
        "ZAR" to 18.3, "EGP" to 49.0, "NGN" to 1550.0, "KES" to 129.0,
        "MAD" to 10.05, "DZD" to 134.0, "TND" to 3.18,
        "SAR" to 3.75, "QAR" to 3.64, "KWD" to 0.31, "BHD" to 0.376, "OMR" to 0.385,
        "JOD" to 0.71, "IQD" to 1310.0, "YER" to 250.0, "LBP" to 89500.0,
        "ILS" to 3.65, "PKR" to 278.0, "BDT" to 119.5, "LKR" to 300.0,
        "NPR" to 133.0, "IDR" to 15800.0, "MYR" to 4.47, "PHP" to 58.5,
        "THB" to 34.5, "VND" to 25400.0, "MMK" to 2100.0, "KHR" to 4100.0,
        "LAK" to 22000.0, "BND" to 1.34, "FJD" to 2.28, "MVR" to 15.42,
        "SEK" to 10.5, "NOK" to 10.8, "DKK" to 7.16, "PLN" to 4.02,
        "CZK" to 23.3, "HUF" to 385.0, "RON" to 4.58, "BGN" to 1.80,
        "RSD" to 108.0, "ALL" to 92.0, "BAM" to 1.80, "MKD" to 56.6,
        "UAH" to 41.5, "RUB" to 90.0, "KZT" to 480.0, "AZN" to 1.70,
        "GEL" to 2.72, "AMD" to 387.0, "BYN" to 3.28, "MDL" to 18.3,
        "AFN" to 71.0, "IRR" to 42000.0, "SYP" to 13000.0
    )

    /** Offline-usable table: cached rates win, then bundled snapshot. */
    fun fallbackRates(): Map<String, Double> = bundledFallback + Currency.fallbackUsdRates

    private suspend fun cachedRates(): Map<String, Double> {
        val raw = runCatching { ctx.fxDataStore.data.first()[ratesKey] }.getOrNull()
        if (raw.isNullOrEmpty()) return emptyMap()
        return runCatching { json.decodeFromString<Map<String, Double>>(raw) }.getOrNull()
            ?.filterValues { it.isFinite() && it > 0.0 }
            ?: emptyMap()
    }

    val rates: Flow<Map<String, Double>> = ctx.fxDataStore.data.map { prefs ->
        val raw = runCatching { prefs[ratesKey] }.getOrNull()
        if (raw.isNullOrEmpty()) fallbackRates()
        else runCatching { json.decodeFromString<Map<String, Double>>(raw) }.getOrNull()
            ?.filterValues { it.isFinite() && it > 0.0 }
            ?.takeIf { it.isNotEmpty() }
            ?.let { fallbackRates() + it + mapOf("USD" to 1.0) }
            ?: fallbackRates()
    }.catch { emit(fallbackRates()) }

    val isStale: Flow<Boolean> = ctx.fxDataStore.data.map { prefs ->
        val ts = runCatching { prefs[tsKey] }.getOrNull() ?: 0L
        runCatching { System.currentTimeMillis() - ts > STALE_MS }.getOrDefault(true)
    }.catch { emit(true) }

    /**
     * Refresh from Frankfurter/ECB. Stale policy: ECB publishes reference
     * rates once per banking day, so [isStale] (older than [STALE_MS] = 24h)
     * marks data due for revalidation; a fresh fetch always replaces the cache.
     *
     * Offline fallback order (uniform for network AND parse/server errors):
     * live -> valid cache (CACHED) -> bundled fallback (OFFLINE).
     * Previous code returned OFFLINE on non-IO errors even with a good cache.
     */
    suspend fun refresh(): RateSource {
        return try {
            val res = api.latestUsd()
            val clean = res.rates.filterValues { it.isFinite() && it > 0.0 }
            if (clean.isEmpty()) throw IllegalStateException(
                "Frankfurter returned no usable rates (empty or non-finite table)"
            )
            val withUsd = clean + ("USD" to 1.0)
            runCatching {
                ctx.fxDataStore.edit {
                    it[ratesKey] = runCatching { json.encodeToString(withUsd) }.getOrDefault("{}")
                    it[tsKey] = System.currentTimeMillis()
                }
            }
            _lastError.value = null
            _source.value = RateSource.LIVE
            RateSource.LIVE
        } catch (e: IOException) {
            offlineWithMessage("Currency refresh failed: no network (${e.message ?: "IOException"}). Showing last saved rates.")
        } catch (e: Exception) {
            offlineWithMessage("Currency refresh failed: ${e.message ?: "unexpected error"}. Showing last saved rates.")
        }
    }

    private suspend fun offlineWithMessage(msg: String): RateSource {
        val hasCache = cachedRates().isNotEmpty()
        _lastError.value = msg
        _source.value = if (hasCache) RateSource.CACHED else RateSource.OFFLINE
        return _source.value
    }

    companion object {
        private const val STALE_MS = 24L * 60L * 60L * 1000L
    }
}
