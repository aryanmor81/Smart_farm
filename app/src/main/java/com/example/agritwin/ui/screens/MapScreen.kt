package com.example.agritwin.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.example.agritwin.config.ApiKeys
import com.example.agritwin.ui.theme.*

@Composable
fun MapScreen() {
    var selectedLocation by remember { mutableStateOf(LatLng(ApiKeys.DEFAULT_LATITUDE, ApiKeys.DEFAULT_LONGITUDE)) }
    var isSatelliteView by remember { mutableStateOf(true) }
    var showHealthOverlay by remember { mutableStateOf(true) }
    var farmRadius by remember { mutableStateOf(1.5f) } // km
    var ndviValue by remember { mutableStateOf(0.72f) }
    var showControlPanel by remember { mutableStateOf(true) }
    var expandedSection by remember { mutableStateOf<String?>("overview") }

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            selectedLocation, 18f // Higher zoom for single farm focus
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = if (isSatelliteView) MapType.SATELLITE else MapType.NORMAL,
                isMyLocationEnabled = false
            ),
            onMapClick = { newLocation ->
                selectedLocation = newLocation
                cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                    newLocation, 18f
                )
            }
        ) {
            Marker(
                state = MarkerState(position = selectedLocation),
                title = ApiKeys.DEFAULT_FARM_NAME,
                snippet = "Tap to recenter"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Neutral50
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = ApiKeys.DEFAULT_FARM_NAME,
                            style = MaterialTheme.typography.titleLarge,
                            color = Green700,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Digital Twin Monitor",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                    }
                    IconButton(
                        onClick = { showControlPanel = !showControlPanel },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Green100)
                    ) {
                        Icon(
                            imageVector = if (showControlPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Panel",
                            tint = Green700,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        if (showControlPanel) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Neutral50)
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                ControlPanelSection(
                    title = "Overview",
                    icon = Icons.Default.Info,
                    isExpanded = expandedSection == "overview",
                    onToggle = { expandedSection = if (expandedSection == "overview") null else "overview" }
                ) {
                    // Farm Location Info
                    InfoRow(label = "Latitude", value = "%.4f".format(selectedLocation.latitude))
                    InfoRow(label = "Longitude", value = "%.4f".format(selectedLocation.longitude))
                    InfoRow(label = "Farm Radius", value = "%.1f km".format(farmRadius))
                }

                ControlPanelSection(
                    title = "Crop Health",
                    icon = Icons.Default.Grass,
                    isExpanded = expandedSection == "health",
                    onToggle = { expandedSection = if (expandedSection == "health") null else "health" }
                ) {
                    NdviHealthCard(ndviValue)
                }

                ControlPanelSection(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    isExpanded = expandedSection == "settings",
                    onToggle = { expandedSection = if (expandedSection == "settings") null else "settings" }
                ) {
                    SettingsRow(
                        icon = if (isSatelliteView) Icons.Default.Satellite else Icons.Default.Terrain,
                        label = if (isSatelliteView) "Satellite" else "Terrain",
                        isActive = isSatelliteView
                    ) {
                        isSatelliteView = !isSatelliteView
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    SettingsRow(
                        icon = Icons.Default.Visibility,
                        label = "Health Overlay",
                        isActive = showHealthOverlay
                    ) {
                        showHealthOverlay = !showHealthOverlay
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonChecked,
                                    contentDescription = "Radius",
                                    tint = Green700,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Farm Boundary",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Neutral700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "%.1f km".format(farmRadius),
                                style = MaterialTheme.typography.labelMedium,
                                color = Green600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = farmRadius,
                            onValueChange = { farmRadius = it },
                            valueRange = 0.5f..5f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Green600,
                                activeTrackColor = Green600,
                                inactiveTrackColor = Green200
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                                selectedLocation, 18f
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green600,
                            contentColor = Neutral50
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 6.dp)
                        )
                        Text("Focus Farm", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {  },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 6.dp)
                        )
                        Text("Copy", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ControlPanelSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isExpanded) Green else Color.Transparent)
                .padding(12.dp)
                .clickable(enabled = true, onClick = onToggle),
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
                    contentDescription = title,
                    tint = Green700,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Green600,
                modifier = Modifier.size(22.dp)
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = Green600,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
                contentDescription = label,
                tint = Green700,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Neutral700,
                fontWeight = FontWeight.SemiBold
            )
        }
        Switch(
            checked = isActive,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.85f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Green600,
                checkedTrackColor = Green300,
                uncheckedThumbColor = Neutral400,
                uncheckedTrackColor = Neutral200
            )
        )
    }
}

@Composable
fun NdviHealthCard(ndviValue: Float) {
    val healthStatus = when {
        ndviValue >= 0.6f -> Pair("Excellent Health", HealthyGreen)
        ndviValue >= 0.4f -> Pair("Good Health", ModerateOrange)
        else -> Pair("Needs Attention", StressedRed)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        ndviValue >= 0.6f -> Color(0xFFE8F5E9)
                        ndviValue >= 0.4f -> Color(0xFFFFF9C4)
                        else -> Color(0xFFFFEBEE)
                    }
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NDVI Value",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
                Text(
                    text = "%.3f".format(ndviValue),
                    style = MaterialTheme.typography.headlineSmall,
                    color = healthStatus.second,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = healthStatus.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = healthStatus.second,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = ndviValue,
                    modifier = Modifier.size(80.dp),
                    color = healthStatus.second,
                    strokeWidth = 6.dp,
                    trackColor = healthStatus.second.copy(alpha = 0.2f)
                )
                Icon(
                    imageVector = Icons.Default.Grass,
                    contentDescription = "Health",
                    tint = healthStatus.second,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HealthIndicator(
                label = "Moisture",
                value = "75%",
                color = Color(0xFF64B5F6),
                modifier = Modifier.weight(1f)
            )
            HealthIndicator(
                label = "Temperature",
                value = "28°C",
                color = Color(0xFFFF8A65),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HealthIndicator(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600,
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}