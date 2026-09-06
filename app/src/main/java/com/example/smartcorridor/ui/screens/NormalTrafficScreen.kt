package com.example.smartcorridor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcorridor.model.NormalVehicle
import com.example.smartcorridor.model.PoleState
import com.example.smartcorridor.model.SmartPole
import com.example.smartcorridor.ui.CorridorViewModel
import com.example.ui.theme.CorridorActiveGreen
import com.example.ui.theme.CorridorPreparingAmber
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.PoleActive
import com.example.ui.theme.PoleNormal
import com.example.ui.theme.PolePassed
import com.example.ui.theme.PolePreparing
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Normal Traffic & Vehicle Monitoring Screen (Changes 9-19, 21, 22)
 * Operates independently of Emergency Corridor mode.
 */
@Composable
fun NormalTrafficScreen(viewModel: CorridorViewModel) {
    val poles by viewModel.poles.collectAsState()
    val vehicles by viewModel.normalVehicles.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val roadAlerts by viewModel.roadAlerts.collectAsState()
    val config by viewModel.corridorConfig.collectAsState()

    var trafficTab by remember { mutableStateOf(0) }
    var selectedVehicleForDialog by remember { mutableStateOf<NormalVehicle?>(null) }

    // KPI Metrics calculation
    val totalDetected = vehicles.size + poles.sumOf { it.vehiclesDetected }
    val activeVehiclesCount = vehicles.size
    val avgSpeed = if (vehicles.isNotEmpty()) vehicles.map { it.speedKmh }.average() else 43.5
    val maxSpeed = if (vehicles.isNotEmpty()) vehicles.maxOf { it.speedKmh } else 68.0
    val overspeedCount = vehicles.count { it.isOverspeed } + poles.sumOf { it.overspeedCount }
    val trafficLevel = if (vehicles.size > 15) "CONGESTED" else if (vehicles.size > 6) "MODERATE" else "LOW"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp)
    ) {
        // Header with Real vs Demo Mode Badge (Change 21, 22)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRAFFIC & VEHICLE MONITORING",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Real-Time Sensor Ingestion & Optical Speed Estimation",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Live vs Demo Mode Toggle Badge (Change 22)
            Surface(
                color = if (isDemoMode) Color(0xFF2A1C05) else Color(0xFF0F2B1D),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDemoMode) CorridorPreparingAmber else CorridorActiveGreen
                ),
                modifier = Modifier.clickable { viewModel.toggleDemoMode() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isDemoMode) CorridorPreparingAmber else CorridorActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDemoMode) "DEMO SIMULATION" else "LIVE SENSORS",
                        color = if (isDemoMode) CorridorPreparingAmber else CorridorActiveGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Emergency Override Alert Banner (Change 19)
        if (activeSession != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color(0xFF4A1010),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY OVERRIDE ENGAGED: Corridor poles display Priority 1 Ambulance alerts.",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Traffic Summary KPI Cards (Change 14)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total Vehicles
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOTAL VEHICLES", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalDetected",
                        style = MaterialTheme.typography.titleLarge,
                        color = TechCyan,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("$activeVehiclesCount Active", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
                }
            }

            // Average Speed
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("AVG SPEED", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${avgSpeed.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = CorridorActiveGreen,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("KM/H (Limit ${config.speedLimitKmh.toInt()})", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
                }
            }

            // Overspeed Alert
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (overspeedCount > 0) Color(0xFF331414) else NavyCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (overspeedCount > 0) EmergencyRed else NavyBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("OVERSPEED", style = MaterialTheme.typography.labelSmall, color = if (overspeedCount > 0) EmergencyRed else TextMuted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$overspeedCount",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (overspeedCount > 0) EmergencyRed else TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("Max ${maxSpeed.toInt()} km/h", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
                }
            }

            // Traffic Level
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TRAFFIC LEVEL", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = trafficLevel,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (trafficLevel == "CONGESTED") EmergencyRed else if (trafficLevel == "MODERATE") CorridorPreparingAmber else CorridorActiveGreen,
                        fontWeight = FontWeight.Black
                    )
                    Text("${poles.size} Poles Active", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Traffic Sub-Tabs
        TabRow(
            selectedTabIndex = trafficTab,
            containerColor = NavySurface,
            contentColor = TechCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[trafficTab]),
                    color = TechCyan
                )
            }
        ) {
            Tab(
                selected = trafficTab == 0,
                onClick = { trafficTab = 0 },
                text = { Text("Live Map", fontSize = 12.sp) }
            )
            Tab(
                selected = trafficTab == 1,
                onClick = { trafficTab = 1 },
                text = { Text("Vehicle Detections", fontSize = 12.sp) }
            )
            Tab(
                selected = trafficTab == 2,
                onClick = { trafficTab = 2 },
                text = { Text("Pole Telemetry", fontSize = 12.sp) }
            )
            Tab(
                selected = trafficTab == 3,
                onClick = { trafficTab = 3 },
                text = { Text("Speed Arch", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (trafficTab) {
                0 -> LiveTrafficMapView(poles, vehicles, activeSession != null)
                1 -> VehicleDetectionTableView(vehicles, isDemoMode) { selectedVehicleForDialog = it }
                2 -> PoleTrafficTelemetryView(poles, config.speedLimitKmh)
                3 -> SpeedArchitectureView(isDemoMode)
            }
        }

        // Demo Mode Control Bar (Change 21)
        Surface(
            color = NavySurface,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.simulateNewVehicle(isOverspeed = false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("+ Normal Vehicle", color = TechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.simulateNewVehicle(isOverspeed = true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("+ Overspeed (72km/h)", color = EmergencyRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.simulateTrafficCongestion() },
                    modifier = Modifier.weight(0.9f),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CorridorPreparingAmber),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("+ Congestion", color = CorridorPreparingAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Vehicle Telemetry Dialog Details
    selectedVehicleForDialog?.let { veh ->
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(veh.detectionTimeMs))
        AlertDialog(
            onDismissRequest = { selectedVehicleForDialog = null },
            containerColor = NavyCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = if (veh.isOverspeed) EmergencyRed else TechCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vehicle Telemetry: ${veh.id}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vehicle Type:", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text(veh.type.displayName, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Measured Speed:", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "${veh.speedKmh} km/h",
                            color = if (veh.isOverspeed) EmergencyRed else CorridorActiveGreen,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speed Limit:", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text("${veh.speedLimitKmh} km/h", color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Associated Pole:", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text(veh.poleId, color = TechCyan, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lane & Direction:", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text("${veh.direction} • ${veh.lane}", color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Detection Time:", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text(timeStr, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = NavyDark,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "DUAL SENSOR SPEED MODEL",
                                color = TechCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Baseline Distance: ${veh.baselineDistanceMeters} m\nTransit Time: ${veh.transitDurationMs} ms (v = d/t)\nOptical Model Confidence: ${(veh.detectionConfidence * 100).toInt()}%",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedVehicleForDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan)
                ) {
                    Text("CLOSE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Live Traffic Map with normal vehicles, smart poles, and alerts (Change 15)
 */
@Composable
fun LiveTrafficMapView(poles: List<SmartPole>, vehicles: List<NormalVehicle>, isEmergencyActive: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Road grid network
            drawLine(
                color = NavyBorder.copy(alpha = 0.6f),
                start = Offset(w * 0.15f, h * 0.85f),
                end = Offset(w * 0.85f, h * 0.15f),
                strokeWidth = 14f
            )
            drawLine(
                color = NavyDark,
                start = Offset(w * 0.15f, h * 0.85f),
                end = Offset(w * 0.85f, h * 0.15f),
                strokeWidth = 10f
            )

            // Cross street
            drawLine(
                color = NavyBorder.copy(alpha = 0.5f),
                start = Offset(w * 0.2f, h * 0.3f),
                end = Offset(w * 0.8f, h * 0.7f),
                strokeWidth = 8f
            )

            // Draw Smart Poles
            poles.forEachIndexed { idx, pole ->
                val ratio = (idx + 1).toFloat() / (poles.size + 1)
                val px = (w * 0.15f) + (w * 0.7f) * ratio
                val py = (h * 0.85f) - (h * 0.7f) * ratio

                // Baseline sensor detection zone
                drawCircle(
                    color = TechCyan.copy(alpha = 0.15f),
                    radius = 24f,
                    center = Offset(px, py)
                )

                // Pole marker
                drawCircle(
                    color = if (pole.emergencyState == PoleState.ACTIVE) PoleActive else TechCyan,
                    radius = 8f,
                    center = Offset(px, py)
                )
            }

            // Draw Normal Detected Vehicles
            vehicles.forEachIndexed { index, veh ->
                val ratio = ((index * 0.16f) + 0.1f).coerceIn(0.1f, 0.9f)
                val vx = (w * 0.15f) + (w * 0.7f) * ratio
                val vy = (h * 0.85f) - (h * 0.7f) * ratio

                val vehColor = if (veh.isOverspeed) EmergencyRed else TechCyan

                // Vehicle dot
                drawCircle(
                    color = vehColor,
                    radius = if (veh.isOverspeed) 9f else 7f,
                    center = Offset(vx + (index * 4 - 8), vy + (index * 2 - 4))
                )
            }
        }

        // Overlay Map Legend
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(NavyDark.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Text("MAP LEGEND", style = MaterialTheme.typography.labelSmall, color = TechCyan, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TechCyan))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Normal Vehicle (< 50 km/h)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmergencyRed))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Overspeed Vehicle (> 50 km/h)", style = MaterialTheme.typography.labelSmall, color = EmergencyRed)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PoleActive))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Smart Pole Sensor Node", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

/**
 * Vehicle Detection Table / List (Change 10, 14)
 */
@Composable
fun VehicleDetectionTableView(
    vehicles: List<NormalVehicle>,
    isDemoMode: Boolean,
    onVehicleClicked: (NormalVehicle) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(vehicles) { veh ->
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(veh.detectionTimeMs))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVehicleClicked(veh) },
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (veh.isOverspeed) EmergencyRed.copy(alpha = 0.6f) else NavyBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (veh.isOverspeed) EmergencyRed else CorridorActiveGreen)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${veh.id} • ${veh.type.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NavyDark,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                                ) {
                                    Text(
                                        text = if (veh.isSimulated) "SIMULATED" else "LIVE SENSOR",
                                        color = TextMuted,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Pole: ${veh.poleId} • ${veh.direction} • ${veh.lane}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${veh.speedKmh} KM/H",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (veh.isOverspeed) EmergencyRed else CorridorActiveGreen,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            color = if (veh.isOverspeed) EmergencyRed.copy(alpha = 0.2f) else CorridorActiveGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (veh.isOverspeed) "OVERSPEED" else "NORMAL",
                                color = if (veh.isOverspeed) EmergencyRed else CorridorActiveGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Per-Pole Traffic Telemetry Breakdown (Change 16)
 */
@Composable
fun PoleTrafficTelemetryView(poles: List<SmartPole>, speedLimit: Double) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(poles) { pole ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pole.poleId} - ${pole.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            color = when (pole.trafficLevel) {
                                "CONGESTED" -> EmergencyRed.copy(alpha = 0.2f)
                                "MODERATE" -> CorridorPreparingAmber.copy(alpha = 0.2f)
                                else -> CorridorActiveGreen.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (pole.trafficLevel) {
                                    "CONGESTED" -> EmergencyRed
                                    "MODERATE" -> CorridorPreparingAmber
                                    else -> CorridorActiveGreen
                                }
                            )
                        ) {
                            Text(
                                text = "${pole.trafficLevel} TRAFFIC",
                                color = when (pole.trafficLevel) {
                                    "CONGESTED" -> EmergencyRed
                                    "MODERATE" -> CorridorPreparingAmber
                                    else -> CorridorActiveGreen
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vehicles Detected: ${pole.vehiclesDetected}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text("Overspeed Violations: ${pole.overspeedCount}", color = if (pole.overspeedCount > 0) EmergencyRed else TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Average Speed: ${pole.averageSpeedKmh} km/h", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text("Max Recorded: ${pole.maxSpeedKmh} km/h", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = NavyDark,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LED DISPLAY (Prio ${pole.activeMessagePriority}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "\"${pole.oledMessage}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Yellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Speed Estimation & Sensor Architecture Explanation (Change 11, 18, 22)
 */
@Composable
fun SpeedArchitectureView(isDemoMode: Boolean) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DUAL SENSOR SPEED ESTIMATION METHODOLOGY",
                        style = MaterialTheme.typography.labelMedium,
                        color = TechCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The ESP32 Smart Pole hardware implements speed estimation using two coordinated approaches:\n\n" +
                                "1. Dual Baseline Timing Sensors (PIR / Ultrasonic / Beam Break):\n" +
                                "Two sensors are placed along the roadway at a fixed physical baseline distance (d = 15.0 meters). When a vehicle passes Sensor 1 (timestamp t1) and Sensor 2 (timestamp t2):\n" +
                                "   Speed v = (d / (t2 - t1)) × 3.6 km/h\n\n" +
                                "2. ESP32-CAM Optical Frame Tracking:\n" +
                                "ESP32-CAM runs lightweight optical centroid tracking across 10fps downsampled frames. The pixel displacement between frames calibrated against roadway fiducials provides real-time vehicle velocity.\n\n" +
                                "3. ESP-NOW Mesh Packet Protocol:\n" +
                                "Detected speed packets are transmitted through local ESP-NOW broadcast to adjacent poles within 10ms, triggering local LED overspeed warnings without cloud latency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "HARDWARE INTEGRATION VS DEMO SIMULATION (Change 22)",
                        style = MaterialTheme.typography.labelMedium,
                        color = CorridorActiveGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Current Mode: ${if (isDemoMode) "SIMULATION / DEMO MODE" else "LIVE HARDWARE MODE"}\n" +
                                "• In Demo Mode, realistic vehicle transits with randomized speeds, types (Cars, Bikes, Trucks, Buses), and occasional overspeed violations are generated for presentation demonstration.\n" +
                                "• In Live Hardware Mode, the gateway ingests real ESP-NOW telemetry packets over serial/WiFi WebSocket directly into the Room database and repository flows.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
