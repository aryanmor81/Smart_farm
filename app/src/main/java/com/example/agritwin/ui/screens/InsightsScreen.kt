package com.example.agritwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.agritwin.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun InsightsScreen() {
    var selectedDay by remember { mutableStateOf(0) }
    var ndvi by remember { mutableStateOf(0.65f) }
    var soilMoisture by remember { mutableStateOf(0.55f) }
    var temperature by remember { mutableStateOf(28.0) }
    var humidity by remember { mutableStateOf(65) }
    var rainProbability by remember { mutableStateOf(30) }
    var windSpeed by remember { mutableStateOf(3.5) }
    var rainwaterCaptured by remember { mutableStateOf(1250.0) }
    var undergroundContainerCapacity by remember { mutableStateOf(5000.0) }
    var undergroundContainerLevel by remember { mutableStateOf(3200.0) }
    var showRainwaterTransfer by remember { mutableStateOf(false) }

    val metrics = FarmHealthMetrics(
        ndvi = ndvi,
        temperature = temperature,
        humidity = humidity,
        rainProbability = rainProbability,
        soilMoisture = soilMoisture,
        windSpeed = windSpeed
    )

    val schedule = AIEngine.generateIrrigationSchedule(metrics, daysAhead = 7)
    val phenologyStage = AIEngine.getCropPhenologyStage(daysFromPlanting = 45)
    val stressIndex = AIEngine.calculateCropStressIndex(
        ndvi = ndvi,
        soilMoisture = soilMoisture,
        temperature = temperature,
        humidity = humidity
    )

    val et0 = AIEngine.calculateEvapotranspiration(temperature, humidity, windSpeed)
    val cwr = AIEngine.calculateCropWaterRequirement(temperature, humidity, windSpeed)

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
            // ==================== HEADER ====================
            item {
                Column {
                    Text(
                        text = "AI Insights & RainWater Harvesting",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Green700,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Advanced agricultural analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral600
                    )
                }
            }

            // ==================== RAINWATER HARVESTING FEATURE ====================
            item {
                Text(
                    text = "Rainwater Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                RainwaterHarvestingCard(
                    rainwaterCaptured = rainwaterCaptured,
                    undergroundContainerLevel = undergroundContainerLevel,
                    undergroundContainerCapacity = undergroundContainerCapacity,
                    rainProbability = rainProbability,
                    onTransferClick = { showRainwaterTransfer = true }
                )
            }

            // ==================== TRANSFER DIALOG ====================
            if (showRainwaterTransfer) {
                item {
                    SimpleRainwaterTransferDialog(
                        rainwaterCaptured = rainwaterCaptured,
                        undergroundContainerLevel = undergroundContainerLevel,
                        undergroundContainerCapacity = undergroundContainerCapacity,
                        onTransfer = { amount ->
                            if (amount <= rainwaterCaptured) {
                                rainwaterCaptured -= amount
                                undergroundContainerLevel = (undergroundContainerLevel + amount).coerceAtMost(undergroundContainerCapacity)
                            }
                            showRainwaterTransfer = false
                        },
                        onDismiss = { showRainwaterTransfer = false }
                    )
                }
            }

            // ==================== CROP PHENOLOGY ====================
            item {
                Text(
                    text = " Current Growth Stage",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                SimplePhenologyCard(phenologyStage = phenologyStage)
            }

            // ==================== STRESS ANALYSIS ====================
            item {
                Text(
                    text = "Crop Stress Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                StressGaugeCard(stressIndex = stressIndex)
            }

            // ==================== WATER ANALYSIS ====================
            item {
                Text(
                    text = "Water Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsightCard(
                        modifier = Modifier.weight(1f),
                        title = "Evapotranspiration",
                        value = "%.2f".format(et0),
                        unit = "mm/day",
                        icon = Icons.Default.WaterDrop,
                        description = "Water loss from soil & plants",
                        color = WaterBlue
                    )
                    InsightCard(
                        modifier = Modifier.weight(1f),
                        title = "Crop Water Need",
                        value = "%.2f".format(cwr),
                        unit = "mm/day",
                        icon = Icons.Default.Water,
                        description = "Actual crop requirement",
                        color = InfoBlue
                    )
                }
            }

            // ==================== PREDICTION SLIDER ====================
            item {
                Text(
                    text = "Future Predictions",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                PredictionCard(
                    selectedDay = selectedDay,
                    onDayChange = { selectedDay = it },
                    metrics = metrics,
                    temperature = temperature,
                    ndvi = ndvi
                )
            }

            // ==================== RISK ASSESSMENT ====================
            item {
                Text(
                    text = "Risk Assessment",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RiskAlert(
                        icon = Icons.Default.Opacity,
                        title = "Drought Risk",
                        description = "Low soil moisture detected",
                        severity = "Medium",
                        action = "Increase irrigation frequency"
                    )
                    RiskAlert(
                        icon = Icons.Default.Thermostat,
                        title = "Heat Stress",
                        description = "Temperature approaching critical level",
                        severity = "Low",
                        action = "Monitor daily and irrigate in morning"
                    )
                    RiskAlert(
                        icon = Icons.Default.Cloud,
                        title = "Disease Risk",
                        description = "High humidity favors fungal diseases",
                        severity = "Medium",
                        action = "Apply preventive fungicide if needed"
                    )
                }
            }

            // ==================== RECOMMENDATIONS ====================
            item {
                Text(
                    text = "AI Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RecommendationCard(
                        number = 1,
                        title = "Optimize Irrigation Schedule",
                        description = "Based on ET₀ and phenology stage, increase irrigation frequency to 2x daily",
                        priority = "High",
                        estimatedSavings = "15% water savings"
                    )
                    RecommendationCard(
                        number = 2,
                        title = "Pest Management",
                        description = "Scout for aphids and whiteflies. Consider IPM approach.",
                        priority = "Medium",
                        estimatedSavings = "Prevent 20% yield loss"
                    )
                    RecommendationCard(
                        number = 3,
                        title = "Nutrient Application",
                        description = "Apply nitrogen-rich fertilizer during grain filling stage",
                        priority = "Medium",
                        estimatedSavings = "Increase yield by 10%"
                    )
                }
            }

            // ==================== 7-DAY SCHEDULE ====================
            item {
                Text(
                    text = "7 Day Irrigation Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    schedule.forEach { scheduleItem ->
                        ScheduleCard(scheduleItem)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==================== RAINWATER HARVESTING CARD ====================

@Composable
fun RainwaterHarvestingCard(
    rainwaterCaptured: Double,
    undergroundContainerLevel: Double,
    undergroundContainerCapacity: Double,
    rainProbability: Int,
    onTransferClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = SkyBlue.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rainwater Harvesting System",
                        style = MaterialTheme.typography.titleMedium,
                        color = SkyBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Smart underground storage & distribution",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                }
                Icon(
                    imageVector = Icons.Default.Water,
                    contentDescription = "Water",
                    tint = SkyBlue,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarningOrange.copy(alpha = 0.1f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Expected Rainfall",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "Next 7 days",
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Badge(containerColor = WarningOrange) {
                    Text(
                        text = "$rainProbability%",
                        modifier = Modifier.padding(horizontal = 6.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = WaterBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = WaterBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Captured",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "${"%.0f".format(rainwaterCaptured)}L",
                            style = MaterialTheme.typography.titleMedium,
                            color = WaterBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ready to transfer",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral500,
                            fontSize = 9.sp
                        )
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = InfoBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Container",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "${"%.0f".format(undergroundContainerLevel)}L",
                            style = MaterialTheme.typography.titleMedium,
                            color = InfoBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "of ${undergroundContainerCapacity.toInt()}L",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral500,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Storage Level",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(undergroundContainerLevel / undergroundContainerCapacity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = InfoBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = (undergroundContainerLevel / undergroundContainerCapacity).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = InfoBlue,
                    trackColor = Neutral200
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onTransferClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = rainwaterCaptured > 0 && undergroundContainerLevel < undergroundContainerCapacity
            ) {
                Icon(
                    imageVector = Icons.Default.MoveDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 6.dp)
                )
                Text(
                    "Transfer to Underground Storage",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ==================== RAINWATER TRANSFER DIALOG ====================

@Composable
fun SimpleRainwaterTransferDialog(
    rainwaterCaptured: Double,
    undergroundContainerLevel: Double,
    undergroundContainerCapacity: Double,
    onTransfer: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var transferAmount by remember { mutableStateOf(rainwaterCaptured) }

    val maxTransferable = minOf(
        rainwaterCaptured,
        undergroundContainerCapacity - undergroundContainerLevel
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Water,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Transfer Rainwater",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Transfer captured rainwater to your underground storage container",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Neutral100
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Available to Transfer",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${"%.0f".format(rainwaterCaptured)} L",
                                style = MaterialTheme.typography.labelMedium,
                                color = WaterBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider(color = Neutral300, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Container Space Available",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${"%.0f".format(maxTransferable)} L",
                                style = MaterialTheme.typography.labelMedium,
                                color = InfoBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider(color = Neutral300, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Current Level",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${"%.0f".format(undergroundContainerLevel)} / ${"%.0f".format(undergroundContainerCapacity)} L",
                                style = MaterialTheme.typography.labelMedium,
                                color = Green600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Transfer Amount",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral700,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${"%.0f".format(transferAmount)} L",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Slider(
                        value = transferAmount.toFloat(),
                        onValueChange = { transferAmount = it.toDouble() },
                        valueRange = 0f..maxTransferable.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = SkyBlue,
                            activeTrackColor = SkyBlue,
                            inactiveTrackColor = Neutral300
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (transferAmount > 0) {
                        onTransfer(transferAmount)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = transferAmount > 0 && maxTransferable > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    "Confirm",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    "Cancel",
                    color = Green600,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        tonalElevation = 4.dp
    )
}

// ==================== SIMPLE PHENOLOGY CARD ====================

@Composable
fun SimplePhenologyCard(phenologyStage: Any?) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Green100
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vegetative Growth Stage",
                        style = MaterialTheme.typography.titleLarge,
                        color = Green700,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Day 45 of growth cycle",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                }
                Icon(
                    imageVector = Icons.Default.EmojiNature,
                    contentDescription = "Growth",
                    tint = Green600,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Neutral100)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Water Need",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "4.2 mm/day",
                        style = MaterialTheme.typography.titleMedium,
                        color = WaterBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stage Duration",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "~30 days",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green600,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Rapid leaf and stem development. Plant is building biomass. Monitor for nutrient deficiencies and weed competition.",
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600
            )
        }
    }
}

// ==================== STRESS GAUGE CARD ====================

@Composable
fun StressGaugeCard(stressIndex: Float) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when {
                stressIndex > 60 -> Color(0xFFFFEBEE)
                stressIndex > 30 -> Color(0xFFFFF9C4)
                else -> Green100
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crop Stress Index",
                style = MaterialTheme.typography.labelLarge,
                color = Neutral600
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        when {
                            stressIndex > 60 -> ErrorRed.copy(alpha = 0.2f)
                            stressIndex > 30 -> WarningOrange.copy(alpha = 0.2f)
                            else -> SuccessGreen.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${stressIndex.toInt()}",
                        style = MaterialTheme.typography.displayMedium,
                        color = when {
                            stressIndex > 60 -> ErrorRed
                            stressIndex > 30 -> WarningOrange
                            else -> SuccessGreen
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when {
                    stressIndex > 60 -> "High Stress - Immediate action required"
                    stressIndex > 30 -> "Moderate Stress - Monitor closely"
                    else -> "Low Stress - Optimal conditions"
                },
                style = MaterialTheme.typography.labelMedium,
                color = Neutral700,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = stressIndex / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    stressIndex > 60 -> ErrorRed
                    stressIndex > 30 -> WarningOrange
                    else -> SuccessGreen
                },
                trackColor = Neutral200
            )
        }
    }
}

// ==================== INSIGHT CARD ====================

@Composable
fun InsightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    description: String,
    color: Color
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
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Neutral700,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = Neutral500,
                fontSize = 8.sp,
                maxLines = 2
            )
        }
    }
}

// ==================== PREDICTION CARD ====================

@Composable
fun PredictionCard(
    selectedDay: Int,
    onDayChange: (Int) -> Unit,
    metrics: FarmHealthMetrics,
    temperature: Double,
    ndvi: Float
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prediction for Day +$selectedDay",
                    style = MaterialTheme.typography.labelMedium,
                    color = Green700,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Slider(
                value = selectedDay.toFloat(),
                onValueChange = { onDayChange(it.toInt()) },
                valueRange = 0f..5f,
                steps = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Green600,
                    activeTrackColor = Green600,
                    inactiveTrackColor = Green200
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            val predictedMetrics = AIEngine.predictMetricsForDay(metrics, selectedDay, false)
            val predictedRecommendation = AIEngine.calculateIrrigationNeeds(predictedMetrics)

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PredictionRow(
                    label = "Temperature",
                    value = "${predictedMetrics.temperature.roundToInt()}°C",
                    icon = Icons.Default.Thermostat,
                    change = "${if (predictedMetrics.temperature > temperature) "+" else ""}${(predictedMetrics.temperature - temperature).roundToInt()}"
                )
                PredictionRow(
                    label = "Humidity",
                    value = "${predictedMetrics.humidity}%",
                    icon = Icons.Default.AcUnit,
                    change = "↓"
                )
                PredictionRow(
                    label = "NDVI",
                    value = "%.3f".format(predictedMetrics.ndvi),
                    icon = Icons.Default.Grass,
                    change = "${if (predictedMetrics.ndvi > ndvi) "+" else ""}${"%.3f".format(predictedMetrics.ndvi - ndvi)}"
                )
                PredictionRow(
                    label = "Water Need",
                    value = "${predictedRecommendation.waterRequired.roundToInt()}L",
                    icon = Icons.Default.Water,
                    change = "${if (predictedRecommendation.waterRequired > 2000) "High" else "Moderate"}"
                )
            }
        }
    }
}

// ==================== PREDICTION ROW ====================

@Composable
fun PredictionRow(
    label: String,
    value: String,
    icon: ImageVector,
    change: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Neutral100)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Green600,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    color = Green700,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Green600.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = change,
                style = MaterialTheme.typography.labelSmall,
                color = Green700,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

// ==================== RISK ALERT ====================

@Composable
fun RiskAlert(
    icon: ImageVector,
    title: String,
    description: String,
    severity: String,
    action: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when (severity) {
                "High" -> ErrorRed.copy(alpha = 0.1f)
                "Medium" -> WarningOrange.copy(alpha = 0.1f)
                else -> InfoBlue.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = when (severity) {
                            "High" -> ErrorRed
                            "Medium" -> WarningOrange
                            else -> InfoBlue
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = Neutral900,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                    }
                }

                Badge(
                    containerColor = when (severity) {
                        "High" -> ErrorRed
                        "Medium" -> WarningOrange
                        else -> InfoBlue
                    },
                    contentColor = Neutral50
                ) {
                    Text(
                        text = severity,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Neutral100)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Neutral600,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = action,
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral700,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ==================== RECOMMENDATION CARD ====================

@Composable
fun RecommendationCard(
    number: Int,
    title: String,
    description: String,
    priority: String,
    estimatedSavings: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Green600),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$number",
                            style = MaterialTheme.typography.labelMedium,
                            color = Neutral50,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = Neutral900,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                    }
                }

                Badge(
                    containerColor = when (priority) {
                        "High" -> ErrorRed
                        else -> WarningOrange
                    },
                    contentColor = Neutral50
                ) {
                    Text(
                        text = priority,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Green100)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Green600,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = estimatedSavings,
                    style = MaterialTheme.typography.labelSmall,
                    color = Green700,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ScheduleCard(schedule: com.example.agritwin.ai.AIEngine.IrrigationSchedule) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                    text = "${schedule.waterDepth.toInt()} mm | ${schedule.duration} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${(schedule.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Green600,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Confidence",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral500
                )
            }
        }
    }
}