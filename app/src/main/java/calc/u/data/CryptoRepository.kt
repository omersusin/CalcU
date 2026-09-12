package calc.u.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import retrofit2.http.Query
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cryptoDataStore by preferencesDataStore("calcu-crypto")

@Serializable
data class CoinPrice(
    val usd: Double = 0.0,
    val usd_24h_change: Double? = null
)

private interface CoinGeckoService {
    @GET("simple/price")
    suspend fun coinPrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String,
        @Query("include_24hr_change") include24hChange: Boolean
    ): Map<String, CoinPrice>
}

@Singleton
class CryptoRepository @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }
    private val pricesKey = stringPreferencesKey("prices_json")
    private val tsKey = longPreferencesKey("prices_ts")

    private val api: CoinGeckoService = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/api/v3/")
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(CoinGeckoService::class.java)

    private val _source = MutableStateFlow(RateSource.OFFLINE)
    val source: StateFlow<RateSource> = _source.asStateFlow()

    val prices: Flow<Map<String, CoinPrice>> = ctx.cryptoDataStore.data.map { prefs ->
        val raw = prefs[pricesKey]
        if (raw.isNullOrEmpty()) emptyMap()
        else runCatching { json.decodeFromString<Map<String, CoinPrice>>(raw) }.getOrNull()?.takeIf { it.isNotEmpty() } ?: emptyMap()
    }

    val isStale: Flow<Boolean> = ctx.cryptoDataStore.data.map { prefs ->
        val ts = prefs[tsKey] ?: 0L
        System.currentTimeMillis() - ts > STALE_MS
    }

    suspend fun refresh(): RateSource {
        return try {
            val res = api.coinPrices(
                ids = DEFAULT_IDS,
                vsCurrencies = "usd",
                include24hChange = true
            )
            if (res.isEmpty()) throw IllegalStateException("empty prices")
            ctx.cryptoDataStore.edit {
                it[pricesKey] = json.encodeToString(res)
                it[tsKey] = System.currentTimeMillis()
            }
            _source.value = RateSource.LIVE
            RateSource.LIVE
        } catch (e: IOException) {
            val cached = runCatching { ctx.cryptoDataStore.data.first()[pricesKey] }.getOrNull()
            val hasCache = !cached.isNullOrEmpty()
            _source.value = if (hasCache) RateSource.CACHED else RateSource.OFFLINE
            _source.value
        } catch (e: Exception) {
            _source.value = RateSource.OFFLINE
            RateSource.OFFLINE
        }
    }

    companion object {
        const val DEFAULT_IDS = "bitcoin,ethereum,solana,bnb,xrp,cardano,dogecoin"
        private const val STALE_MS = 60L * 60L * 1000L
    }
}
