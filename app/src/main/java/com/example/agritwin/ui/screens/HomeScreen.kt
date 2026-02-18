package com.example.agritwin.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agritwin.ai.AIEngine
import com.example.agritwin.ai.FarmHealthMetrics
import com.example.agritwin.config.ApiKeys
import com.example.agritwin.network.WeatherResponse
import com.example.agritwin.network.WeatherService
import com.example.agritwin.network.SentinelAuthService
import com.example.agritwin.ui.theme.*
import com.example.agritwin.ui.dialogs.ChatbotDialog
import com.example.agritwin.ui.dialogs.IrrigationControlDialog
import com.example.agritwin.ui.dialogs.DiseaseDetectionDialog
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

var currentLanguage = "English"


//We make app in both languages Hindi and English

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen() {
    var farmName by remember { mutableStateOf(ApiKeys.DEFAULT_FARM_NAME) }
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var ndvi by remember { mutableStateOf(0.65f) }
    var soilMoisture by remember { mutableStateOf(0.55f) }
    var stressMode by remember { mutableStateOf(false) }
    var showRefreshing by remember { mutableStateOf(false) }
    var apiStatus by remember { mutableStateOf(if(currentLanguage == "Hindi") "जांच जारी है..." else "Checking...") }
    var selectedForecastDay by remember { mutableStateOf(0) }
    var showIrrigationDetails by remember { mutableStateOf(false) }
    var showChatbot by remember { mutableStateOf(false) }
    var showIrrigationControl by remember { mutableStateOf(false) }
    var showDiseaseDetection by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val latitude = ApiKeys.DEFAULT_LATITUDE
    val longitude = ApiKeys.DEFAULT_LONGITUDE

    val fetchWeatherData = {
        coroutineScope.launch {
            showRefreshing = true
            try {
                weather = WeatherService.fetchWeather(latitude, longitude)
                if (weather != null) {
                    apiStatus = if(currentLanguage == "Hindi") "मौसम: जुड़ा हुआ" else "Weather: Connected"
                } else {
                    apiStatus = if(currentLanguage == "Hindi") "⚠️ मौसम: डेमो मोड" else "Weather: Using Demo Mode"
                }
            } catch (e: Exception) {
                apiStatus = if(currentLanguage == "Hindi") "मौसम: विफल" else "Weather: Failed"
                e.printStackTrace()
            } finally {
                showRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        SentinelAuthService.printCredentialsStatus()
        fetchWeatherData()
        isLoading = false
    }

    LaunchedEffect(stressMode) {
        if (stressMode) {
            ndvi = AIEngine.simulateNDVI(stressMode = true)
            soilMoisture = AIEngine.simulateSoilMoisture(stressMode = true)
        } else {
            ndvi = AIEngine.simulateNDVI(stressMode = false)
            soilMoisture = AIEngine.simulateSoilMoisture(stressMode = false)
        }
    }

    val metrics = FarmHealthMetrics(
        ndvi = ndvi,
        temperature = weather?.main?.temperature ?: 28.0,
        humidity = weather?.main?.humidity ?: 65,
        rainProbability = (weather?.clouds?.cloudiness ?: 30),
        soilMoisture = soilMoisture,
        windSpeed = weather?.wind?.speed ?: 3.5
    )

    val irrigationRecommendation = AIEngine.calculateIrrigationNeeds(metrics)
    val healthStatus = AIEngine.classifyNDVIHealth(ndvi)
    val sustainabilityScore = AIEngine.calculateSustainabilityScore(
        ndvi = ndvi,
        waterUsage = irrigationRecommendation.waterRequired,
        temperature = metrics.temperature,
        rainfall = metrics.rainProbability,
        soilMoisture = soilMoisture
    )

    val evapotranspiration = AIEngine.calculateEvapotranspiration(
        temperature = metrics.temperature,
        humidity = metrics.humidity,
        windSpeed = metrics.windSpeed
    )

    val cropWaterRequirement = AIEngine.calculateCropWaterRequirement(
        temperature = metrics.temperature,
        humidity = metrics.humidity,
        windSpeed = metrics.windSpeed
    )

    val stressIndex = AIEngine.calculateCropStressIndex(
        ndvi = ndvi,
        soilMoisture = soilMoisture,
        temperature = metrics.temperature,
        humidity = metrics.humidity
    )

    val irrigationSchedule = AIEngine.generateIrrigationSchedule(metrics, daysAhead = 7)

    val currentTime = try {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        LocalDateTime.now().format(formatter)
    } catch (e: Exception) {
        "12:00"
    }

    val healthColor = when {
        healthStatus.level == "Healthy" -> Green600
        healthStatus.level == "Moderate" -> WarningOrange
        else -> ErrorRed
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Neutral50
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if(currentLanguage == "Hindi") "शर्मा फार्म" else "Sharma Farm",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Green700,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = apiStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    apiStatus.contains("") -> SuccessGreen
                                    apiStatus.contains("") -> WarningOrange
                                    else -> ErrorRed
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if(currentLanguage == "Hindi") "अपडेट किया गया: $currentTime" else "Updated: $currentTime",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral500
                            )
                        }

                        if (showRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Green600,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Button(
                                onClick = { fetchWeatherData() },
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green100,
                                    contentColor = Green700
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("↻", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    QuickStatsBanner(
                        healthLevel = healthStatus.level,
                        stressIndex = stressIndex,
                        urgency = irrigationRecommendation.urgency,
                        sustainabilityScore = sustainabilityScore
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            modifier = Modifier.weight(2.5f),
                            icon = Icons.Default.AcUnit,
                            label = if(currentLanguage == "Hindi") "सिंचाई" else "Irrigation",
                            onClick = { showIrrigationControl = true },
                            backgroundColor = WaterBlue
                        )
                        ActionButton(
                            modifier = Modifier.weight(3f),
                            icon = Icons.Default.LocalFlorist,
                            label = if(currentLanguage == "Hindi") "रोग जांच" else "Disease Check",
                            onClick = { showDiseaseDetection = true },
                            backgroundColor = ErrorRed
                        )
                    }
                }

                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = if (stressMode) 4.dp else 2.dp,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (stressMode) ErrorRed.copy(alpha = 0.1f) else Green100
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (stressMode) Icons.Default.Warning else Icons.Default.CloudOff,
                                    contentDescription = "Climate Mode",
                                    tint = if (stressMode) ErrorRed else Green700,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if(currentLanguage == "Hindi") "जलवायु तनाव सिमुलेशन" else "Climate Stress Simulation",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Neutral900,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (stressMode) {
                                            if(currentLanguage == "Hindi") "प्रतिकूल परिस्थितियों का अनुकरण" else "Simulating adverse conditions"
                                        } else {
                                            if(currentLanguage == "Hindi") "सामान्य खेत की स्थिति" else "Normal farm conditions"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Neutral600
                                    )
                                }
                            }

                            Switch(
                                checked = stressMode,
                                onCheckedChange = { stressMode = it },
                                modifier = Modifier,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ErrorRed,
                                    checkedTrackColor = ErrorRed.copy(alpha = 0.5f),
                                    uncheckedThumbColor = Green600,
                                    uncheckedTrackColor = Green600.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = if(currentLanguage == "Hindi") "वर्तमान मौसम" else "Current Weather",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green700,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeatherCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Thermostat,
                            title = if(currentLanguage == "Hindi") "तापमान" else "Temperature",
                            value = "${weather?.main?.temperature?.toInt() ?: 28}°C",
                            subtitle = if(currentLanguage == "Hindi") "महसूस होता है ${weather?.main?.feelsLike?.toInt() ?: 27}°C" else "Feels like ${weather?.main?.feelsLike?.toInt() ?: 27}°C",
                            backgroundColor = Color(0xFFFFE0B2)
                        )
                        WeatherCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AcUnit,
                            title = if(currentLanguage == "Hindi") "आर्द्रता" else "Humidity",
                            value = "${weather?.main?.humidity ?: 65}%",
                            subtitle = if(currentLanguage == "Hindi") "दबाव: ${weather?.main?.pressure ?: 1013} hPa" else "Pressure: ${weather?.main?.pressure ?: 1013} hPa",
                            backgroundColor = SkyBlue.copy(alpha = 0.2f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeatherCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Cloud,
                            title = if(currentLanguage == "Hindi") "वर्षा की संभावना" else "Rain Probability",
                            value = "${metrics.rainProbability}%",
                            subtitle = if(currentLanguage == "Hindi") "बादलों का सूचकांक" else "Cloudiness Index",
                            backgroundColor = Color(0xFFB3E5FC)
                        )
                        WeatherCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Grass,
                            title = if(currentLanguage == "Hindi") "हवा की गति" else "Wind Speed",
                            value = "${metrics.windSpeed.toInt()} m/s",
                            subtitle = if(currentLanguage == "Hindi") "दिशा: ${weather?.wind?.degree ?: 0}°" else "Direction: ${weather?.wind?.degree ?: 0}°",
                            backgroundColor = Color(0xFFC8E6C9)
                        )
                    }
                }

                item {
                    Text(
                        text = if(currentLanguage == "Hindi") "उन्नत विश्लेषण" else "Advanced Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green700,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdvancedMetricCard(
                            modifier = Modifier.weight(1f),
                            label = if(currentLanguage == "Hindi") "वाष्पोत्सर्जन" else "Evapotranspiration",
                            value = String.format("%.2f", evapotranspiration),
                            unit = "mm/day",
                            icon = Icons.Default.Water,
                            color = WaterBlue,
                            description = if(currentLanguage == "Hindi") "मिट्टी से पानी की हानि" else "Water loss from soil"
                        )
                        AdvancedMetricCard(
                            modifier = Modifier.weight(1f),
                            label = if(currentLanguage == "Hindi") "फसल जल आवश्यकता" else "Crop Water Need",
                            value = String.format("%.2f", cropWaterRequirement),
                            unit = "mm/day",
                            icon = Icons.Default.AcUnit,
                            color = InfoBlue,
                            description = if(currentLanguage == "Hindi") "फसल की आवश्यकता" else "Actual need"
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdvancedMetricCard(
                            modifier = Modifier.weight(1f),
                            label = if(currentLanguage == "Hindi") "तनाव सूचकांक" else "Stress Index",
                            value = stressIndex.roundToInt().toString(),
                            unit = "/100",
                            icon = Icons.Default.Warning,
                            color = if (stressIndex > 60) ErrorRed else if (stressIndex > 30) WarningOrange else SuccessGreen,
                            description = if(currentLanguage == "Hindi") "तनाव स्तर" else "Stress level"
                        )
                        AdvancedMetricCard(
                            modifier = Modifier.weight(1f),
                            label = if(currentLanguage == "Hindi") "दबाव" else "Pressure",
                            value = "${weather?.main?.pressure ?: 1013}",
                            unit = "hPa",
                            icon = Icons.Default.Cloud,
                            color = SkyBlue,
                            description = if(currentLanguage == "Hindi") "वायुमंडलीय दबाव" else "Atmospheric"
                        )
                    }
                }

                item {
                    Text(
                        text = if(currentLanguage == "Hindi") "फसल स्वास्थ्य" else "Crop Health",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green700,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = healthColor.copy(alpha = 0.12f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if(currentLanguage == "Hindi") "NDVI स्कोर" else "NDVI Score",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Neutral600,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if(currentLanguage == "Hindi") "वनस्पति सूचकांक" else "Vegetation Index",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral500
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Grass,
                                    contentDescription = "NDVI",
                                    tint = healthColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = String.format("%.3f", ndvi),
                                style = MaterialTheme.typography.displaySmall,
                                color = healthColor,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(healthColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = healthStatus.level,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = healthColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if(currentLanguage == "Hindi") "स्व���स्थ्य" else "Health",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Neutral600
                                    )
                                    Text(
                                        text = "${(ndvi * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = healthColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = ndvi,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = healthColor,
                                    trackColor = Neutral200
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = when (healthStatus.level) {
                                    "Healthy" -> if(currentLanguage == "Hindi") "फसल उत्कृष्ट स्थिति में है।" else "Crops are thriving excellently."
                                    "Moderate" -> if(currentLanguage == "Hindi") "फसल ध्यान की जरूरत है।" else "Crops need attention."
                                    else -> if(currentLanguage == "Hindi") "फसल तनावग्रस्त है।" else "Crops are stressed."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral600,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Water,
                                    contentDescription = "Moisture",
                                    tint = WaterBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if(currentLanguage == "Hindi") "नमी" else "Moisture",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Neutral600,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(soilMoisture * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = WaterBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                LinearProgressIndicator(
                                    progress = soilMoisture,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = WaterBlue,
                                    trackColor = Neutral200
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = getMoistureStatus(soilMoisture),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Neutral500
                                )
                            }
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (sustainabilityScore > 75) Green100
                                else if (sustainabilityScore > 50) Color(0xFFFFF9C4)
                                else Color(0xFFFFEBEE)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Grass,
                                    contentDescription = "Sustainability",
                                    tint = if (sustainabilityScore > 75) Green600
                                    else if (sustainabilityScore > 50) WarningOrange
                                    else ErrorRed,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if(currentLanguage == "Hindi") "स्थिरता" else "Sustainability",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Neutral600,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${sustainabilityScore.toInt()}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = if (sustainabilityScore > 75) Green600
                                    else if (sustainabilityScore > 50) WarningOrange
                                    else ErrorRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if(currentLanguage == "Hindi") "अंक" else "Score",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Neutral500
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if(currentLanguage == "Hindi") "AI सिफारिशें" else "AI Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green700,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = when (irrigationRecommendation.urgency) {
                                "High" -> ErrorRed.copy(alpha = 0.08f)
                                "Medium" -> WarningOrange.copy(alpha = 0.08f)
                                else -> Green100
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if(currentLanguage == "Hindi") "सिंचाई योजना" else "Irrigation Plan",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Green700,
                                    fontWeight = FontWeight.Bold
                                )
                                Badge(
                                    containerColor = when (irrigationRecommendation.urgency) {
                                        "High" -> ErrorRed
                                        "Medium" -> WarningOrange
                                        else -> SuccessGreen
                                    },
                                    contentColor = Neutral50,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = if(currentLanguage == "Hindi") {
                                            when (irrigationRecommendation.urgency) {
                                                "High" -> "अधिक"
                                                "Medium" -> "मध्यम"
                                                else -> "कम"
                                            }
                                        } else {
                                            irrigationRecommendation.urgency
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if(currentLanguage == "Hindi") "पानी आवश्यक" else "Water Needed",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Neutral700,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${irrigationRecommendation.waterRequired.toInt()} L",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = WaterBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Neutral100)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Time",
                                    tint = Green600,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if(currentLanguage == "Hindi") "सर्वोत्तम समय" else "Best Time",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Neutral600
                                    )
                                    Text(
                                        text = irrigationRecommendation.bestTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Green700,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (!irrigationRecommendation.riskAlert.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)),
                                    color = ErrorRed.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Alert",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = irrigationRecommendation.riskAlert!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ErrorRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showIrrigationDetails = !showIrrigationDetails },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green600,
                                    contentColor = Neutral50
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    if (showIrrigationDetails) {
                                        if(currentLanguage == "Hindi") "छुपाएं" else "Hide"
                                    } else {
                                        if(currentLanguage == "Hindi") "विवरण देखें" else "Details"
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (showIrrigationDetails && irrigationSchedule.isNotEmpty()) {
                    item {
                        Text(
                            text = if(currentLanguage == "Hindi") "7-दिन की योजना" else "7-Day Plan",
                            style = MaterialTheme.typography.titleMedium,
                            color = Green700,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            irrigationSchedule.forEach { schedule ->
                                IrrigationScheduleCard(schedule)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if(currentLanguage == "Hindi") "7-दिन पूर्वानुमान" else "7-Day Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green700,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(7) { day ->
                            val predictedMetrics = AIEngine.predictMetricsForDay(
                                metrics,
                                day,
                                stressMode
                            )
                            ForecastDayCard(
                                day = day,
                                temperature = predictedMetrics.temperature,
                                humidity = predictedMetrics.humidity,
                                rainProbability = predictedMetrics.rainProbability,
                                ndvi = predictedMetrics.ndvi,
                                isSelected = selectedForecastDay == day,
                                onClick = { selectedForecastDay = day }
                            )
                        }
                    }
                }

                item {
                    val selectedDayMetrics = AIEngine.predictMetricsForDay(metrics, selectedForecastDay, stressMode)
                    val selectedDayRecommendation = AIEngine.calculateIrrigationNeeds(selectedDayMetrics)

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if(currentLanguage == "Hindi") "दिन +$selectedForecastDay" else "Day +$selectedForecastDay",
                                style = MaterialTheme.typography.titleMedium,
                                color = Green700,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = if(currentLanguage == "Hindi") "तापमान" else "Temp",
                                    value = "${selectedDayMetrics.temperature.toInt()}°C",
                                    icon = Icons.Default.Thermostat
                                )
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = if(currentLanguage == "Hindi") "आर्द्रता" else "Humidity",
                                    value = "${selectedDayMetrics.humidity}%",
                                    icon = Icons.Default.AcUnit
                                )
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = if(currentLanguage == "Hindi") "वर्षा" else "Rain",
                                    value = "${selectedDayMetrics.rainProbability}%",
                                    icon = Icons.Default.Cloud
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = "NDVI",
                                    value = String.format("%.2f", selectedDayMetrics.ndvi),
                                    icon = Icons.Default.Grass
                                )
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = if(currentLanguage == "Hindi") "जल" else "Water",
                                    value = "${selectedDayRecommendation.waterRequired.toInt()}L",
                                    icon = Icons.Default.Water
                                )
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = if(currentLanguage == "Hindi") "नमी" else "Moist",
                                    value = "${(selectedDayMetrics.soilMoisture * 100).toInt()}%",
                                    icon = Icons.Default.Opacity
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if(currentLanguage == "Hindi") "मेट्रिक्स सारांश" else "Metrics Summary",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green700,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricRow(
                            label = if(currentLanguage == "Hindi") "तापमान" else "Temperature",
                            value = "${metrics.temperature.toInt()}°C",
                            subtitle = if(currentLanguage == "Hindi") "20-30°C" else "20-30°C",
                            isOptimal = metrics.temperature in 20.0..30.0
                        )
                        MetricRow(
                            label = if(currentLanguage == "Hindi") "आर्द्रता" else "Humidity",
                            value = "${metrics.humidity}%",
                            subtitle = if(currentLanguage == "Hindi") "50-70%" else "50-70%",
                            isOptimal = metrics.humidity in 50..70
                        )
                        MetricRow(
                            label = if(currentLanguage == "Hindi") "हवा" else "Wind",
                            value = "${metrics.windSpeed.toInt()} m/s",
                            subtitle = if(currentLanguage == "Hindi") "< 5 m/s" else "< 5 m/s",
                            isOptimal = metrics.windSpeed < 5
                        )
                        MetricRow(
                            label = if(currentLanguage == "Hindi") "नमी" else "Moisture",
                            value = "${(soilMoisture * 100).toInt()}%",
                            subtitle = if(currentLanguage == "Hindi") "40-70%" else "40-70%",
                            isOptimal = soilMoisture in 0.4f..0.7f
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* Navigate */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green600,
                                contentColor = Neutral50
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                if(currentLanguage == "Hindi") "मैप देखें" else "View Map",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { /* Export */ },
                            modifier = Modifier.fillMaxWidth(),
                            border = ButtonDefaults.outlinedButtonBorder,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                tint = Green600
                            )
                            Text(
                                if(currentLanguage == "Hindi") "रिपोर्ट निर्यात करें" else "Export Report",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Green600
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { showChatbot = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 85.dp),
            containerColor = Green600,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = if(currentLanguage == "Hindi") "चैट" else "Chat",
                modifier = Modifier.size(24.dp)
            )
        }

        if (showChatbot) {
            ChatbotDialog(
                onDismiss = { showChatbot = false },
                farmMetrics = metrics
            )
        }

        if (showIrrigationControl) {
            IrrigationControlDialog(
                onDismiss = { showIrrigationControl = false },
                currentWaterRequired = irrigationRecommendation.waterRequired
            )
        }

        if (showDiseaseDetection) {
            DiseaseDetectionDialog(
                onDismiss = { showDiseaseDetection = false }
            )
        }
    }
}

// ==================== HELPER COMPOSABLES ====================

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = backgroundColor.copy(alpha = 0.15f),
            contentColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(20.dp)
                .padding(end = 8.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun QuickStatsBanner(
    healthLevel: String,
    stressIndex: Float,
    urgency: String,
    sustainabilityScore: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Green100)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatItem(
            modifier = Modifier.weight(1f),
            label = if(currentLanguage == "Hindi") "स्वास्थ्य" else "Health",
            value = healthLevel,
            color = Green600
        )
        QuickStatItem(
            modifier = Modifier.weight(1f),
            label = if(currentLanguage == "Hindi") "तनाव" else "Stress",
            value = "${stressIndex.toInt()}/100",
            color = if (stressIndex > 60) ErrorRed else if (stressIndex > 30) WarningOrange else SuccessGreen
        )
        QuickStatItem(
            modifier = Modifier.weight(1f),
            label = if(currentLanguage == "Hindi") "सिंचाई" else "Irrigation",
            value = if(currentLanguage == "Hindi") {
                when (urgency) {
                    "High" -> "अधिक"
                    "Medium" -> "मध्यम"
                    else -> "कम"
                }
            } else {
                urgency
            },
            color = when (urgency) {
                "High" -> ErrorRed
                "Medium" -> WarningOrange
                else -> SuccessGreen
            }
        )
        QuickStatItem(
            modifier = Modifier.weight(1f),
            label = if(currentLanguage == "Hindi") "स्थिरता" else "Sustainability",
            value = "${sustainabilityScore.toInt()}/100",
            color = if (sustainabilityScore > 75) SuccessGreen else if (sustainabilityScore > 50) WarningOrange else ErrorRed
        )
    }
}

@Composable
fun QuickStatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color = Neutral100
) {
    ElevatedCard(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Green600,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Green700,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Neutral500
            )
        }
    }
}

@Composable
fun AdvancedMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    description: String
) {
    ElevatedCard(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Neutral700,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral500,
                    fontSize = 8.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = Neutral500,
                fontSize = 8.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ForecastDayCard(
    day: Int,
    temperature: Double,
    humidity: Int,
    rainProbability: Int,
    ndvi: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .width(90.dp)
            .shadow(if (isSelected) 4.dp else 2.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) Green600 else Neutral100
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (day == 0) if(currentLanguage == "Hindi") "आज" else "Today" else if(currentLanguage == "Hindi") "दिन +$day" else "Day +$day",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Neutral50 else Neutral700,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${temperature.toInt()}°",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Neutral50 else Green700,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "NDVI",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Neutral50 else Neutral600,
                    fontSize = 8.sp
                )
                Text(
                    text = String.format("%.2f", ndvi),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Neutral50 else Green600,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = "Humidity",
                    tint = if (isSelected) Neutral50 else WaterBlue,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "$humidity%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Neutral50 else Neutral600,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun MetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Green100)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Green600,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600,
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = Green700,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    subtitle: String,
    isOptimal: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOptimal) Green100 else Color(0xFFFFEBEE))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Neutral700,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = if (isOptimal) Green700 else ErrorRed,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (isOptimal) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOptimal) Green600 else ErrorRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun IrrigationScheduleCard(schedule: com.example.agritwin.ai.AIEngine.IrrigationSchedule) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.scheduledDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = Green700,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if(currentLanguage == "Hindi")
                        "पानी: ${schedule.waterDepth.toInt()} मिमी | अवधि: ${schedule.duration} मिनट"
                    else
                        "Water: ${schedule.waterDepth.toInt()} mm | Duration: ${schedule.duration} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${(schedule.confidence * 100).toInt()} %",
                            style = MaterialTheme.typography.labelMedium,
                    color = Green600,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if(currentLanguage == "Hindi") "विश्वास" else "Confidence",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral500
                )
            }
        }
    }
}

fun getMoistureStatus(moisture: Float): String {
    return when {
        moisture < 0.2f -> if(currentLanguage == "Hindi") "बहुत सूखा" else "Very Dry"
        moisture < 0.4f -> if(currentLanguage == "Hindi") "सूखा" else "Dry"
        moisture < 0.6f -> if(currentLanguage == "Hindi") "इष्टतम" else "Optimal"
        moisture < 0.8f -> if(currentLanguage == "Hindi") "नम" else "Moist"
        else -> if(currentLanguage == "Hindi") "बहुत गीला" else "Very Wet"
    }
}