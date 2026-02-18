package com.example.agritwin.network


import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.Body


data class WeatherResponse(
    @SerializedName("main")
    val main: MainWeatherData,
    @SerializedName("clouds")
    val clouds: CloudsData,
    @SerializedName("rain")
    val rain: RainData? = null,
    @SerializedName("wind")
    val wind: WindData,
    @SerializedName("sys")
    val sys: SystemData
)

data class MainWeatherData(
    @SerializedName("temp")
    val temperature: Double,
    @SerializedName("humidity")
    val humidity: Int,
    @SerializedName("pressure")
    val pressure: Int,
    @SerializedName("feels_like")
    val feelsLike: Double
)

data class CloudsData(
    @SerializedName("all")
    val cloudiness: Int
)

data class RainData(
    @SerializedName("1h")
    val oneHourVolume: Double? = null
)

data class WindData(
    @SerializedName("speed")
    val speed: Double,
    @SerializedName("deg")
    val degree: Int,
    @SerializedName("gust")
    val gust: Double? = null
)

data class SystemData(
    @SerializedName("sunrise")
    val sunrise: Long,
    @SerializedName("sunset")
    val sunset: Long,
    @SerializedName("country")
    val country: String
)

// ==================== SENTINEL HUB OAUTH ====================

data class SentinelTokenRequest(
    @SerializedName("grant_type")
    val grantType: String = "client_credentials",
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String
)

data class SentinelTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("token_type")
    val tokenType: String
)

data class SentinelEvalscriptRequest(
    @SerializedName("input")
    val input: InputRequest,
    @SerializedName("evalscript")
    val evalscript: String
)

data class InputRequest(
    @SerializedName("bounds")
    val bounds: BoundsRequest,
    @SerializedName("data")
    val data: List<DataSourceRequest>
)

data class BoundsRequest(
    @SerializedName("bbox")
    val bbox: List<Double>,
    @SerializedName("properties")
    val properties: Map<String, String> = mapOf("crs" to "http://www.opengis.net/gml/srs/epsg.xml#4326")
)

data class DataSourceRequest(
    @SerializedName("dataFilter")
    val dataFilter: DataFilterRequest,
    @SerializedName("type")
    val type: String = "sentinel-2-l2a"
)

data class DataFilterRequest(
    @SerializedName("timeRange")
    val timeRange: TimeRangeRequest
)

data class TimeRangeRequest(
    @SerializedName("from")
    val from: String,
    @SerializedName("to")
    val to: String
)

data class NDVIResponse(
    @SerializedName("data")
    val data: List<NDVIData>
)

data class NDVIData(
    @SerializedName("output")
    val output: NDVIOutput
)

data class NDVIOutput(
    @SerializedName("data")
    val ndviValues: List<List<Float>>? = null
)

interface ApiService {

    @GET("weather")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @POST("oauth/token")
    suspend fun getSentinelToken(
        @Body request: SentinelTokenRequest
    ): SentinelTokenResponse

    @POST("api/v1/process")
    suspend fun getProcessedSentinelData(
        @Header("Authorization") authHeader: String,
        @Body request: SentinelEvalscriptRequest
    ): String
}