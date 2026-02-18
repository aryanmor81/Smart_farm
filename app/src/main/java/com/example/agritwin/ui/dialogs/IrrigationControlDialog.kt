package com.example.agritwin.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.agritwin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun IrrigationControlDialog(
    onDismiss: () -> Unit,
    currentWaterRequired: Double
) {
    var waterLevel by remember { mutableStateOf(currentWaterRequired.toFloat()) }
    var irrigationDuration by remember { mutableStateOf(30f) }
    var selectedZone by remember { mutableStateOf("All Zones") }
    var irrigationMode by remember { mutableStateOf("Auto") }
    var isIrrigating by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val zones = listOf("All Zones", "Zone A", "Zone B", "Zone C", "Zone D")
    val modes = listOf("Auto", "Manual", "Scheduled")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp)),
            color = Neutral50
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaterBlue)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = "Irrigation",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "Irrigation Control",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isIrrigating) ErrorRed.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "System Status",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Neutral600
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    if (isIrrigating) "Irrigating..." else "Standby",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isIrrigating) ErrorRed else SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (isIrrigating) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = if (isIrrigating) ErrorRed else SuccessGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Select Irrigation Zone",
                            style = MaterialTheme.typography.labelLarge,
                            color = Neutral900,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            zones.forEach { zone ->
                                FilterChip(
                                    selected = selectedZone == zone,
                                    onClick = { selectedZone = zone },
                                    label = { Text(zone) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WaterBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Water Quantity",
                                style = MaterialTheme.typography.labelLarge,
                                color = Neutral900,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${waterLevel.toInt()} L",
                                style = MaterialTheme.typography.titleMedium,
                                color = WaterBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = waterLevel,
                            onValueChange = { waterLevel = it },
                            valueRange = 0f..1000f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = WaterBlue,
                                activeTrackColor = WaterBlue,
                                inactiveTrackColor = WaterBlue.copy(alpha = 0.3f)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "50L" to 50f,
                                "100L" to 100f,
                                "200L" to 200f,
                                "500L" to 500f
                            ).forEach { (label, value) ->
                                OutlinedButton(
                                    onClick = { waterLevel = value },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Duration",
                                style = MaterialTheme.typography.labelLarge,
                                color = Neutral900,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${irrigationDuration.toInt()} minutes",
                                style = MaterialTheme.typography.titleMedium,
                                color = Green600,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = irrigationDuration,
                            onValueChange = { irrigationDuration = it },
                            valueRange = 5f..120f,
                            steps = 23,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Green600,
                                activeTrackColor = Green600,
                                inactiveTrackColor = Green600.copy(alpha = 0.3f)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "5m" to 5f,
                                "15m" to 15f,
                                "30m" to 30f,
                                "60m" to 60f
                            ).forEach { (label, value) ->
                                OutlinedButton(
                                    onClick = { irrigationDuration = value },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Irrigation Mode",
                            style = MaterialTheme.typography.labelLarge,
                            color = Neutral900,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            modes.forEach { mode ->
                                FilterChip(
                                    selected = irrigationMode == mode,
                                    onClick = { irrigationMode = mode },
                                    label = { Text(mode) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Green600,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = InfoBlue.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = InfoBlue,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Text(
                                "Based on current soil moisture and weather conditions, the AI recommends ${currentWaterRequired.toInt()}L of water.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral700
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                showConfirmation = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIrrigating) ErrorRed else Green600,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isIrrigating
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                "Start Irrigation",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (isIrrigating) {
                            Button(
                                onClick = { isIrrigating = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarningOrange,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    "Stop Irrigation",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Close",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Green600
                            )
                        }
                    }
                }
            }
        }
    }
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = {
                Text(
                    "Confirm Irrigation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Zone: $selectedZone")
                    Text("Water: ${waterLevel.toInt()}L")
                    Text("Duration: ${irrigationDuration.toInt()} minutes")
                    Text("Mode: $irrigationMode")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isIrrigating = true
                        showConfirmation = false
                        coroutineScope.launch {
                            // Simulate irrigation
                            kotlinx.coroutines.delay((irrigationDuration * 60 * 1000).toLong())
                            isIrrigating = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}