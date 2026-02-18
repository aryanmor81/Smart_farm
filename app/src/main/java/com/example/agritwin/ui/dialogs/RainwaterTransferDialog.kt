package com.example.agritwin.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agritwin.ui.theme.*

@Composable
fun RainwaterTransferDialog(
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Transfer captured rainwater to your underground storage container",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
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
                                "Current Container Level",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            maxTransferable * 0.25,
                            maxTransferable * 0.5,
                            maxTransferable * 0.75,
                            maxTransferable
                        )
                        val labels = listOf("25%", "50%", "75%", "Max")

                        presets.zip(labels).forEach { (amount, label) ->
                            OutlinedButton(
                                onClick = { transferAmount = amount },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(4.dp),
                                border = ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Green100
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Transfer Summary",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral700,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "After Transfer:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600
                            )
                            Text(
                                "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "• Remaining Surface Water:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral700,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${"%.0f".format(rainwaterCaptured - transferAmount)} L",
                                style = MaterialTheme.typography.labelSmall,
                                color = WaterBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "• Container Level:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral700,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${"%.0f".format(undergroundContainerLevel + transferAmount)} L",
                                style = MaterialTheme.typography.labelSmall,
                                color = Green600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                    "Confirm Transfer",
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