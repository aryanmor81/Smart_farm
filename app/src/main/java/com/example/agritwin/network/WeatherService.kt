package com.example.agritwin.network

import android.util.Log
import com.example.agritwin.config.ApiKeys
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherService {
    private const val OPENWEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val TAG = "WeatherService"

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(OPENWEATHER_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    fun isApiKeyConfigured(): Boolean {
        val key = ApiKeys.OPENWEATHER_API_KEY
        val isConfigured = key.isNotEmpty() && !key.contains("your_")
        Log.d(TAG, "API Key Configured: $isConfigured")
        return isConfigured
    }

    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherResponse? {

        if (!isApiKeyConfigured()) {
            Log.w(TAG, "OpenWeather API key not configured. Using demo mode.")
            return null
        }

        return try {
            Log.d(TAG, "Fetching weather for ($latitude, $longitude)")
            apiService.getWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = ApiKeys.OPENWEATHER_API_KEY
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch weather: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
}