package com.example.agritwin.ai

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

data class CropHealthStatus(
    val level: String,
    val color: String
)

data class IrrigationRecommendation(
    val waterRequired: Double,
    val bestTime: String,
    val urgency: String,
    val riskAlert: String? = null
)

data class FarmHealthMetrics(
    val ndvi: Float,
    val temperature: Double,
    val humidity: Int,
    val rainProbability: Int,
    val soilMoisture: Float,
    val windSpeed: Double
)

object AIEngine {

    private fun deterministicValue(seed: Long, min: Float, max: Float): Float {
        val normalized = sin(seed.toDouble() * PI / 1000.0).toFloat()
        val scaled = (normalized + 1f) / 2f
        return min + scaled * (max - min)
    }

    private fun deterministicValueDouble(seed: Long, min: Double, max: Double): Double {
        val normalized = sin(seed.toDouble() * PI / 1000.0)
        val scaled = (normalized + 1.0) / 2.0
        return min + scaled * (max - min)
    }

    private fun deterministicValueInt(seed: Long, min: Int, max: Int): Int {
        val normalized = sin(seed.toDouble() * PI / 1000.0).toFloat()
        val scaled = (normalized + 1f) / 2f
        return (min + scaled * (max - min)).toInt()
    }

    fun classifyNDVIHealth(ndvi: Float): CropHealthStatus {
        return when {
            ndvi >= 0.6f -> CropHealthStatus("Healthy", "#66BB6A")
            ndvi >= 0.3f -> CropHealthStatus("Moderate", "#FFA726")
            else -> CropHealthStatus("Stressed", "#EF5350")
        }
    }

    fun calculateIrrigationNeeds(metrics: FarmHealthMetrics): IrrigationRecommendation {
        var waterRequired = 0.0

        val tempFactor = when {
            metrics.temperature > 35 -> 1.4
            metrics.temperature > 30 -> 1.2
            metrics.temperature > 25 -> 1.0
            metrics.temperature > 20 -> 0.8
            else -> 0.6
        }

        waterRequired += 1000 * tempFactor
        val humidityFactor = (100 - metrics.humidity) / 100.0
        waterRequired += 500 * humidityFactor
        waterRequired += (1 - metrics.soilMoisture) * 800

        val ndviFactor = when {
            metrics.ndvi < 0.3f -> 1.3
            metrics.ndvi < 0.6f -> 1.0
            else -> 0.7
        }

        waterRequired *= ndviFactor
        waterRequired *= (1 - metrics.rainProbability / 100.0)

        val bestTime = determineBestIrrigationTime(
            metrics.temperature,
            metrics.humidity,
            metrics.windSpeed
        )

        val urgency = when {
            metrics.ndvi < 0.3f && metrics.soilMoisture < 0.3f -> "High"
            metrics.ndvi < 0.5f || metrics.soilMoisture < 0.4f -> "Medium"
            else -> "Low"
        }

        val riskAlert = generateRiskAlert(metrics)

        return IrrigationRecommendation(
            waterRequired = max(0.0, min(10000.0, waterRequired)),
            bestTime = bestTime,
            urgency = urgency,
            riskAlert = riskAlert
        )
    }

    private fun determineBestIrrigationTime(
        temperature: Double,
        humidity: Int,
        windSpeed: Double
    ): String {
        return when {
            temperature > 35 -> "Early Morning (5-7 AM)"
            windSpeed > 5 -> "Evening (5-7 PM)"
            humidity > 70 -> "Morning (7-9 AM)"
            else -> "Early Morning (5-7 AM)"
        }
    }

    private fun generateRiskAlert(metrics: FarmHealthMetrics): String? {
        val alerts = mutableListOf<String>()

        if (metrics.temperature > 40) {
            alerts.add("Heat Stress: Extreme temperature detected")
        } else if (metrics.temperature < 5) {
            alerts.add("Frost Risk: Very low temperature")
        }

        if (metrics.soilMoisture < 0.2f && metrics.ndvi < 0.4f) {
            alerts.add("Drought Risk: Low soil moisture + stressed crops")
        }

        if (metrics.soilMoisture > 0.85f && metrics.humidity > 90) {
            alerts.add("Waterlogging Risk: High moisture + high humidity")
        }

        if (metrics.humidity > 85 && metrics.temperature > 25) {
            alerts.add("Disease Risk: Conditions favor fungal growth")
        }

        return if (alerts.isNotEmpty()) alerts.first() else null
    }

    fun calculateSustainabilityScore(
        ndvi: Float,
        waterUsage: Double,
        temperature: Double,
        rainfall: Int,
        soilMoisture: Float
    ): Float {
        var score = 50f

        score += (ndvi * 30)

        val waterEfficiency = when {
            waterUsage < 1000 -> 25f
            waterUsage < 3000 -> 20f
            waterUsage < 6000 -> 12f
            else -> 5f
        }
        score += waterEfficiency

        if (rainfall > 30) score += 20f
        else if (rainfall > 10) score += 15f
        else score += 5f

        if (soilMoisture in 0.4f..0.7f) score += 15f
        else if (soilMoisture in 0.3f..0.8f) score += 10f
        else score += 3f

        return max(0f, min(100f, score))
    }

    fun simulateNDVI(stressMode: Boolean = false): Float {
        val baseSeed = System.currentTimeMillis() / 1000
        return if (stressMode) {
            deterministicValue(baseSeed, 0.25f, 0.45f)
        } else {
            deterministicValue(baseSeed, 0.55f, 0.75f)
        }
    }

    fun simulateSoilMoisture(stressMode: Boolean = false): Float {
        val baseSeed = System.currentTimeMillis() / 1000 + 1000
        return if (stressMode) {
            deterministicValue(baseSeed, 0.15f, 0.35f)
        } else {
            deterministicValue(baseSeed, 0.45f, 0.65f)
        }
    }

    fun predictMetricsForDay(
        currentMetrics: FarmHealthMetrics,
        daysAhead: Int,
        stressMode: Boolean = false
    ): FarmHealthMetrics {

        val tempSeed = System.currentTimeMillis() / 1000 + daysAhead * 100
        val humiditySeed = System.currentTimeMillis() / 1000 + daysAhead * 100 + 1000
        val ndviSeed = System.currentTimeMillis() / 1000 + daysAhead * 100 + 2000
        val moistureSeed = System.currentTimeMillis() / 1000 + daysAhead * 100 + 3000
        val windSeed = System.currentTimeMillis() / 1000 + daysAhead * 100 + 4000

        val temperatureChange = if (stressMode) {
            deterministicValueDouble(tempSeed, 1.0, 4.0)
        } else {
            deterministicValueDouble(tempSeed, -1.0, 1.0)
        }

        val humidityChange = if (stressMode) {
            deterministicValueInt(humiditySeed, -15, -5)
        } else {
            deterministicValueInt(humiditySeed, -5, 5)
        }

        val ndviChange = if (stressMode) {
            deterministicValue(ndviSeed, -0.08f, -0.02f)
        } else {
            deterministicValue(ndviSeed, -0.02f, 0.05f)
        }

        val soilMoistureChange = if (stressMode) {
            deterministicValue(moistureSeed, -0.1f, -0.02f)
        } else {
            deterministicValue(moistureSeed, -0.05f, 0.08f)
        }

        val windChange = deterministicValueDouble(windSeed, -1.0, 1.0)

        return FarmHealthMetrics(
            ndvi = (currentMetrics.ndvi + (ndviChange * daysAhead)).coerceIn(0f, 1f),
            temperature = currentMetrics.temperature + (temperatureChange * daysAhead),
            humidity = (currentMetrics.humidity + (humidityChange * daysAhead)).coerceIn(0, 100),
            rainProbability = (currentMetrics.rainProbability + deterministicValueInt(
                System.currentTimeMillis() / 1000 + daysAhead,
                -5,
                5
            ) * daysAhead).coerceIn(0, 100),
            soilMoisture = (currentMetrics.soilMoisture + (soilMoistureChange * daysAhead)).coerceIn(0f, 1f),
            windSpeed = (currentMetrics.windSpeed + windChange).coerceAtLeast(0.0)
        )
    }

    fun getHourlyTemperatureVariation(baseTemp: Double, hour: Int): Double {
        val hourAngle = (hour - 12) * (PI / 12.0)
        val variation = 5.0 * sin(hourAngle)
        return baseTemp + variation
    }

    fun getHourlyHumidityVariation(baseHumidity: Int, hour: Int): Int {
        val hourAngle = (hour - 12) * (PI / 12.0)
        val variation = 20 * cos(hourAngle)
        return (baseHumidity + variation).toInt().coerceIn(0, 100)
    }

    fun calculateEvapotranspiration(
        temperature: Double,
        humidity: Int,
        windSpeed: Double,
        solarRadiation: Double = 20.0
    ): Double {
        val tempFactor = 0.0023 * (temperature + 17.8) * (solarRadiation - 0.5)
        val humidityFactor = (100 - humidity) / 100.0
        val windFactor = 1 + 0.04 * windSpeed
        return max(0.0, tempFactor * humidityFactor * windFactor)
    }

    fun calculateCropWaterRequirement(
        temperature: Double,
        humidity: Int,
        windSpeed: Double,
        cropCoefficient: Float = 0.85f,
        leafAreaIndex: Float = 3.0f
    ): Double {
        val et0 = calculateEvapotranspiration(temperature, humidity, windSpeed)
        return et0 * cropCoefficient * (leafAreaIndex / 5.0)
    }

    fun calculateCropStressIndex(
        ndvi: Float,
        soilMoisture: Float,
        temperature: Double,
        humidity: Int,
        vpd: Double = 1.5
    ): Float {
        var stressIndex = 0f

        stressIndex += (1 - ndvi) * 30f

        val optimalMoisture = 0.55f
        val moistureDelta = Math.abs((soilMoisture - optimalMoisture).toDouble())
        stressIndex += (moistureDelta * 35f).toFloat()

        val optimalTemp = 25.0
        val tempDeviation = Math.abs(temperature - optimalTemp)
        stressIndex += when {
            tempDeviation > 15 -> 20f
            tempDeviation > 10 -> 15f
            tempDeviation > 5 -> 10f
            else -> 5f
        }

        stressIndex += (vpd.toFloat() / 5.0f) * 15.0f

        return max(0f, min(100f, stressIndex))
    }

    data class CropPhenologyStage(
        val stage: String,
        val daysFromPlanting: Int,
        val waterRequirement: Double
    )

    fun getCropPhenologyStage(daysFromPlanting: Int): CropPhenologyStage {
        return when {
            daysFromPlanting < 20 -> CropPhenologyStage("Germination & Establishment", daysFromPlanting, 2.0)
            daysFromPlanting < 50 -> CropPhenologyStage("Vegetative Growth", daysFromPlanting, 4.5)
            daysFromPlanting < 80 -> CropPhenologyStage("Flowering & Pod Formation", daysFromPlanting, 6.5)
            daysFromPlanting < 110 -> CropPhenologyStage("Grain Filling", daysFromPlanting, 5.5)
            else -> CropPhenologyStage("Maturity & Senescence", daysFromPlanting, 2.0)
        }
    }

    data class IrrigationSchedule(
        val scheduledDate: String,
        val waterDepth: Double,
        val duration: Int,
        val confidence: Float
    )

    fun generateIrrigationSchedule(
        currentMetrics: FarmHealthMetrics,
        daysAhead: Int = 7
    ): List<IrrigationSchedule> {

        val schedule = mutableListOf<IrrigationSchedule>()
        var predictedMetrics = currentMetrics

        for (dayOffset in 0 until daysAhead) {

            predictedMetrics = predictMetricsForDay(
                predictedMetrics,
                dayOffset,
                stressMode = false
            )

            val waterRequirement = calculateCropWaterRequirement(
                predictedMetrics.temperature,
                predictedMetrics.humidity,
                predictedMetrics.windSpeed
            )

            if (waterRequirement > 2.0 && predictedMetrics.soilMoisture < 0.5f) {
                schedule.add(
                    IrrigationSchedule(
                        scheduledDate = "Day +$dayOffset",
                        waterDepth = waterRequirement * 25,
                        duration = (waterRequirement * 3).toInt(),
                        confidence = 0.7f + (0.1f * sin((dayOffset * PI / daysAhead).toFloat()))
                    )
                )
            }
        }

        return schedule
    }
}
