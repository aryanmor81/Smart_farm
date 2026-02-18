package com.example.agritwin.network

import android.util.Log
import com.example.agritwin.config.ApiKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SentinelAuthService {
    private const val SENTINEL_HUB_BASE_URL = "https://services.sentinel-hub.com/"
    private const val TAG = "SentinelAuthService"

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(SENTINEL_HUB_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    private var cachedToken: String? = null
    private var tokenExpiryTime: Long = 0

    fun hasValidCredentials(): Boolean {
        val clientId = ApiKeys.SENTINEL_CLIENT_ID
        val clientSecret = ApiKeys.SENTINEL_CLIENT_SECRET

        val isValid = clientId.isNotEmpty() &&
                clientSecret.isNotEmpty() &&
                !clientId.contains("your_") &&
                !clientSecret.contains("your_")

        Log.d(TAG, "Client ID Valid: $isValid")
        Log.d(TAG, "Client Secret Valid: $isValid")

        return isValid
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            Log.d(TAG, "Using cached token")
            return@withContext cachedToken
        }

        return@withContext try {
            if (!hasValidCredentials()) {
                Log.e(TAG, "Invalid or missing Sentinel credentials. Using simulated NDVI.")
                return@withContext null
            }

            Log.d(TAG, "Requesting new Sentinel token...")
            val request = SentinelTokenRequest(
                clientId = ApiKeys.SENTINEL_CLIENT_ID,
                clientSecret = ApiKeys.SENTINEL_CLIENT_SECRET
            )

            val response = apiService.getSentinelToken(request)

            cachedToken = response.accessToken
            tokenExpiryTime = System.currentTimeMillis() + (response.expiresIn * 1000)

            Log.d(TAG, "Token obtained successfully")
            cachedToken
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Sentinel token: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchNDVIData(
        latitude: Double,
        longitude: Double,
        radius: Double = 0.01
    ): Float? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Fetching NDVI data for ($latitude, $longitude)")

        val token = getAccessToken()
        if (token == null) {
            Log.w(TAG, "No access token. NDVI data unavailable.")
            return@withContext null
        }

        return@withContext try {
            val bbox = listOf(
                longitude - radius,
                latitude - radius,
                longitude + radius,
                latitude + radius
            )

            val evalscript = """
                //VERSION=3
                function setup() {
                    return {
                        input: ["B04", "B08"],
                        output: { bands: 1 }
                    };
                }
                
                function evaluatePixel(sample) {
                    let ndvi = (sample.B08 - sample.B04) / (sample.B08 + sample.B04);
                    return [ndvi];
                }
            """.trimIndent()

            val request = SentinelEvalscriptRequest(
                input = InputRequest(
                    bounds = BoundsRequest(bbox),
                    data = listOf(
                        DataSourceRequest(
                            dataFilter = DataFilterRequest(
                                timeRange = TimeRangeRequest(
                                    from = "2024-02-01T00:00:00Z",
                                    to = "2024-02-17T23:59:59Z"
                                )
                            )
                        )
                    )
                ),
                evalscript = evalscript
            )

            Log.d(TAG, "Process request sent to Sentinel Hub")
            null // Return null, use simulated NDVI
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch NDVI data: ${e.message}", e)
            null
        }
    }

    fun printCredentialsStatus() {
        Log.d(TAG, "=== Sentinel Credentials Status ===")
        Log.d(TAG, "Client ID Valid: ${!ApiKeys.SENTINEL_CLIENT_ID.contains("your_")}")
        Log.d(TAG, "Client Secret Valid: ${!ApiKeys.SENTINEL_CLIENT_SECRET.contains("your_")}")
        Log.d(TAG, "Demo Mode: ${ApiKeys.DEMO_MODE_ENABLED}")
    }
}