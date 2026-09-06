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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay

/**
 * Screen 8, 9, 10: Emergency Active Cockpit, Radar Map & 2KM Wave Inspector
 */
@Composable
fun EmergencyActiveCockpitScreen(
    viewModel: CorridorViewModel,
    onEmergencyEnded: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val currentVehicle by viewModel.currentVehicle.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val corridorStatus by viewModel.corridorStatus.collectAsState()
    val currentGps by viewModel.currentGps.collectAsState()
    val poles by viewModel.poles.collectAsState()
    val roadAlerts by viewModel.roadAlerts.collectAsState()
    val isArrived by viewModel.isDestinationReached.collectAsState()
    val isSimulating by viewModel.simulationEngine.isSimulating.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    var runningSeconds by remember { mutableStateOf(0) }
    var showEndConfirmation by remember { mutableStateOf(false) }

    // Running duration timer
    LaunchedEffect(activeSession) {
        while (activeSession != null) {
            delay(1000L)
            runningSeconds++
        }
    }

    val minutes = runningSeconds / 60
    val seconds = runningSeconds % 60
    val timerStr = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
    ) {
        // Top Emergency Banner HUD
        Surface(
            color = EmergencyRed,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2 KM EMERGENCY CORRIDOR ACTIVE",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = timerStr,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Secondary Telemetry Bar
        Surface(
            color = NavyCard,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${currentVehicle.type.displayName} ${currentVehicle.id}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Route: ${selectedRoute.name}",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Next Pole & Distance Callout
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NEXT POLE: ${corridorStatus.nextPoleId ?: "P01"}",
                        color = TechCyan,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${"%.1f".format(corridorStatus.distanceToNextPoleKm)} KM AHEAD",
                        color = CorridorActiveGreen,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Road Alert Warning Bar (if alerts exist)
        if (roadAlerts.isNotEmpty()) {
            val alert = roadAlerts.first()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A1010)),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.title,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = alert.description,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.dismissAlert(alert.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("DISMISS", fontSize = 11.sp)
                    }
                }
            }
        }

        // Cockpit Sub-Tabs
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = NavySurface,
            contentColor = TechCyan,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = TechCyan
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("HUD Cockpit") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("🎬 Streetlight Clip") }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Live Radar Map") }
            )
            Tab(
                selected = activeTab == 3,
                onClick = { activeTab = 3 },
                text = { Text("2 KM Wave Status") }
            )
            Tab(
                selected = activeTab == 4,
                onClick = { activeTab = 4 },
                text = { Text("📋 Event Log") }
            )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> HudCockpitView(viewModel, corridorStatus, onOpenClip = { activeTab = 1 })
                1 -> StreetlightClipScreen(viewModel = viewModel, onBack = { activeTab = 0 })
                2 -> LiveRadarMapView(viewModel)
                3 -> PoleWaveStatusView(viewModel)
                4 -> CorridorEventLogView(viewModel)
            }
        }


        // Bottom Controls: Simulation Stepper & End Emergency Button
        Surface(
            color = NavySurface,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulation step button
                OutlinedButton(
                    onClick = { viewModel.stepSimulationManual() },
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = NavyCard)
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = "Step", tint = TechCyan)
                }

                // Simulation play/pause
                OutlinedButton(
                    onClick = { viewModel.toggleSimulationPlayPause() },
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = NavyCard)
                ) {
                    Icon(
                        if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = TechCyan
                    )
                }

                // End Emergency Button
                Button(
                    onClick = { showEndConfirmation = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("end_emergency_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "END EMERGENCY",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }

    // Confirmation dialog for ending emergency manually
    if (showEndConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndConfirmation = false },
            containerColor = NavyCard,
            title = {
                Text(
                    text = "Confirm Emergency Termination",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to shut down the 2 KM emergency corridor? All streetlights will safely revert to normal automated lighting.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndConfirmation = false
                        viewModel.endEmergency("MANUAL_TERMINATION")
                        onEmergencyEnded()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("YES, END MISSION")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEndConfirmation = false }) {
                    Text("RESUME", color = TextPrimary)
                }
            }
        )
    }

    // Destination Arrival Dialog (Geofence < 100m)
    if (isArrived) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = NavyCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = CorridorActiveGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DESTINATION REACHED",
                        color = CorridorActiveGreen,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "You have safely arrived at ${selectedRoute.destinationName}.",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Elapsed Mission Time: $timerStr\n• Route: ${selectedRoute.name}\n• Total Corridor Distance: ${selectedRoute.distanceKm} km\n• Smart Poles Traversed: ${selectedRoute.poleIds.size}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endEmergency("COMPLETED")
                        onEmergencyEnded()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CorridorActiveGreen)
                ) {
                    Text("FINISH MISSION & ARCHIVE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Sub-View 0: HUD Cockpit View (Changes 1, 2, 3, 4)
 */
@Composable
fun HudCockpitView(
    viewModel: CorridorViewModel,
    corridorStatus: com.example.smartcorridor.model.CorridorStatus,
    onOpenClip: () -> Unit = {}
) {
    val currentVehicle by viewModel.currentVehicle.collectAsState()
    val currentGps by viewModel.currentGps.collectAsState()
    val progress by viewModel.simulationEngine.progressRatio.collectAsState()
    val poles by viewModel.poles.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()

    val nextPole = poles.find { it.poleId == corridorStatus.nextPoleId } ?: poles.firstOrNull()
    val routePoles = selectedRoute.poleIds.mapNotNull { id -> poles.find { it.poleId == id } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Source-Based 2 KM Rolling Window Telemetry Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF071B12)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CorridorActiveGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = CorridorActiveGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SOURCE-BASED 2 KM CORRIDOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = CorridorActiveGreen,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Surface(
                            color = TechCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LOOKAHEAD: 2.0 KM",
                                color = TechCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "VEHICLE POSITION",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Source + ${corridorStatus.vehicleStationText}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Origin: ${corridorStatus.sourceName ?: selectedRoute.originName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CORRIDOR WAVE STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${corridorStatus.activePoleCount} ACTIVE • ${corridorStatus.preparingPoleCount} PREPARING",
                                style = MaterialTheme.typography.titleSmall,
                                color = CorridorActiveGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Poles triggered in advance of vehicle",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Prominent Next Pole Triggering Banner (Change 1, 4)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B1D)),
                border = androidx.compose.foundation.BorderStroke(2.dp, CorridorActiveGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(CorridorActiveGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NEXT CORRIDOR POLE AHEAD",
                                style = MaterialTheme.typography.labelSmall,
                                color = CorridorActiveGreen,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        // Communication Status Badge (Change 4)
                        Surface(
                            color = CorridorActiveGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CorridorActiveGreen)
                        ) {
                            Text(
                                text = "COMMS: CONNECTED (ACK: 14ms)",
                                color = CorridorActiveGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = nextPole?.name ?: "P02 - MG Road Junction",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pole ID: ${nextPole?.poleId ?: "P02"} • Hardware: ${nextPole?.hardwareVersion ?: "ESP32-V2.4"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val distM = ((corridorStatus.distanceToNextPoleKm * 1000).toInt()).coerceAtLeast(0)
                            Text(
                                text = if (distM >= 1000) "${"%.2f".format(corridorStatus.distanceToNextPoleKm)} km" else "$distM m",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TechCyan,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "TRIGGER DISTANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Pole State & LED Message preview
                    Surface(
                        color = NavyDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "POLE STATUS: ${nextPole?.emergencyState?.displayName?.uppercase() ?: "ACTIVE"}",
                                color = if (nextPole?.emergencyState == PoleState.ACTIVE) CorridorActiveGreen else CorridorPreparingAmber,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "LED: \"${nextPole?.oledMessage ?: "AMBULANCE APPROACHING"}\"",
                                color = Color.Yellow,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 24/7 Day/Night Override & Vehicle Telemetry Row (Change 3)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("VEHICLE ID", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentVehicle.id,
                            style = MaterialTheme.typography.titleMedium,
                            color = TechCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(currentVehicle.registrationNumber, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CURRENT SPEED", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${currentGps.speedKmh.toInt()} KM/H",
                            style = MaterialTheme.typography.titleMedium,
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text("GPS Active", style = MaterialTheme.typography.labelSmall, color = CorridorActiveGreen)
                    }
                }

                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CorridorActiveGreen.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("24/7 OVERRIDE", style = MaterialTheme.typography.labelSmall, color = CorridorActiveGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "100% STROBE",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Text("Day & Night Active", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }

        // Live Clip Page Shortcut Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenClip() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C192E)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, TechCyan.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TechCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Clip",
                                    tint = TechCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🎬 STREETLIGHT CLIP & LIVE SPEED HUD",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = CorridorActiveGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = CorridorActiveGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Dedicated screen with animated corridor wave, digital speedometer, and pole telemetry",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = onOpenClip,
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("VIEW CLIP", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Corridor Sequence Ladder (Change 1, 4)
        item {
            Text(
                text = "CORRIDOR POLE SEQUENCE ALONG ROUTE",
                style = MaterialTheme.typography.labelMedium,
                color = TechCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        items(routePoles) { pole ->
            val isNext = pole.poleId == corridorStatus.nextPoleId
            val deltaAhead = corridorStatus.poleDistancesAhead[pole.poleId] ?: (pole.routeDistanceMeters - corridorStatus.vehicleStationMeters)
            val deltaAheadText = if (deltaAhead >= 0) "+${deltaAhead.toInt()}m ahead" else "${(-deltaAhead).toInt()}m past"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNext) Color(0xFF142921) else NavyCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isNext) 2.dp else 1.dp,
                    color = when (pole.emergencyState) {
                        PoleState.ACTIVE -> PoleActive
                        PoleState.PREPARING -> PolePreparing
                        PoleState.PASSED -> PolePassed
                        else -> NavyBorder
                    }
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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (pole.emergencyState) {
                                        PoleState.ACTIVE -> PoleActive
                                        PoleState.PREPARING -> PolePreparing
                                        PoleState.PASSED -> PolePassed
                                        else -> PoleNormal
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${pole.poleId} - ${pole.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isNext) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = TechCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "NEXT",
                                            color = TechCyan,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Route: ${pole.routeDistanceMeters.toInt()}m • $deltaAheadText • ${pole.lightBrightness}% brightness",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "LED: \"${pole.oledMessage}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (pole.emergencyState == PoleState.ACTIVE) EmergencyRed else TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = when (pole.emergencyState) {
                                PoleState.ACTIVE -> PoleActive
                                PoleState.PREPARING -> PolePreparing
                                PoleState.PASSED -> PolePassed
                                else -> PoleNormal
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = pole.emergencyState.displayName.uppercase(),
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (deltaAhead >= 0 && deltaAhead <= 2000) "In 2KM Wave" else "Comms: OK",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (deltaAhead >= 0 && deltaAhead <= 2000) CorridorActiveGreen else TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sub-View 1: Live Radar Map View with Pole Labels & Advance Corridor Projection (Change 6)
 */
@Composable
fun LiveRadarMapView(viewModel: CorridorViewModel) {
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val poles by viewModel.poles.collectAsState()
    val gps by viewModel.currentGps.collectAsState()
    val progress by viewModel.simulationEngine.progressRatio.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Radar concentric rings
            for (i in 1..4) {
                drawCircle(
                    color = NavyBorder.copy(alpha = 0.4f),
                    radius = (w / 8) * i,
                    center = Offset(w / 2, h / 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }

            // Draw Route Track Path
            val startY = h * 0.82f
            val endY = h * 0.16f
            val startX = w * 0.18f
            val endX = w * 0.82f

            drawLine(
                color = TechCyan.copy(alpha = 0.5f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )

            // Vehicle coordinates along track
            val vehX = startX + (endX - startX) * progress
            val vehY = startY + (endY - startY) * progress

            // 2 KM Green Wave Projection cone ahead of vehicle
            val waveAheadX = (vehX + (endX - vehX) * 0.45f).coerceAtMost(endX)
            val waveAheadY = (vehY + (endY - vehY) * 0.45f).coerceAtLeast(endY)

            drawLine(
                color = CorridorActiveGreen.copy(alpha = 0.7f),
                start = Offset(vehX, vehY),
                end = Offset(waveAheadX, waveAheadY),
                strokeWidth = 10f
            )

            // Draw Poles along route
            val routePoles = selectedRoute.poleIds.mapNotNull { id -> poles.find { it.poleId == id } }
            routePoles.forEachIndexed { idx, pole ->
                val ratio = (idx + 1).toFloat() / (routePoles.size + 1)
                val poleX = startX + (endX - startX) * ratio
                val poleY = startY + (endY - startY) * ratio

                val poleColor = when (pole.emergencyState) {
                    PoleState.ACTIVE -> PoleActive
                    PoleState.PREPARING -> PolePreparing
                    PoleState.PASSED -> PolePassed
                    else -> PoleNormal
                }

                // Draw pole glow halo
                drawCircle(
                    color = poleColor.copy(alpha = 0.35f),
                    radius = if (pole.emergencyState == PoleState.ACTIVE) 30f else 20f,
                    center = Offset(poleX, poleY)
                )

                // Draw pole marker
                drawCircle(
                    color = poleColor,
                    radius = 12f,
                    center = Offset(poleX, poleY)
                )
            }

            // Draw Emergency Vehicle strobe pulse
            drawCircle(
                color = EmergencyRed.copy(alpha = 0.35f),
                radius = 34f,
                center = Offset(vehX, vehY)
            )
            drawCircle(
                color = EmergencyRed,
                radius = 14f,
                center = Offset(vehX, vehY)
            )
        }

        // Overlay Legend & Real-Time Wave Status
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(NavyDark.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Text("CORRIDOR RADAR STATUS", style = MaterialTheme.typography.labelSmall, color = TechCyan, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PoleActive))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🟢 Active (Corridor Ahead)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PolePreparing))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🟡 Preparing (Anticipatory 2KM)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PolePassed))
                Spacer(modifier = Modifier.width(6.dp))
                Text("✓ Passed (Normal Traffic Resumed)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
            }
        }
    }
}

/**
 * Sub-View 2: Pole Wave Status View (Detailed Telemetry)
 */
@Composable
fun PoleWaveStatusView(viewModel: CorridorViewModel) {
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val poles by viewModel.poles.collectAsState()
    val corridorStatus by viewModel.corridorStatus.collectAsState()
    val routePoles = selectedRoute.poleIds.mapNotNull { id -> poles.find { it.poleId == id } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1F16)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CorridorActiveGreen)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SOURCE-BASED ROLLING CORRIDOR TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        color = CorridorActiveGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vehicle Station: Source + ${corridorStatus.vehicleStationText} • Lookahead: 2.0 KM",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Origin: ${corridorStatus.sourceName ?: selectedRoute.originName} • Destination: ${selectedRoute.destinationName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "CORRIDOR SMART POLES (ROUTE ${selectedRoute.id})",
                style = MaterialTheme.typography.labelMedium,
                color = TechCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(routePoles) { pole ->
            val deltaAhead = corridorStatus.poleDistancesAhead[pole.poleId] ?: (pole.routeDistanceMeters - corridorStatus.vehicleStationMeters)
            val deltaAheadText = if (deltaAhead >= 0) "+${deltaAhead.toInt()}m ahead" else "${(-deltaAhead).toInt()}m behind"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (pole.emergencyState == PoleState.ACTIVE) 2.dp else 1.dp,
                    color = when (pole.emergencyState) {
                        PoleState.ACTIVE -> PoleActive
                        PoleState.PREPARING -> PolePreparing
                        PoleState.PASSED -> PolePassed
                        else -> NavyBorder
                    }
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${pole.poleId}: ${pole.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status Pill
                        Surface(
                            color = when (pole.emergencyState) {
                                PoleState.ACTIVE -> PoleActive
                                PoleState.PREPARING -> PolePreparing
                                PoleState.PASSED -> PolePassed
                                else -> PoleNormal
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = pole.emergencyState.displayName.uppercase(),
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Route Station: ${pole.routeDistanceMeters.toInt()}m from Source ($deltaAheadText)\n• Streetlight Brightness: ${pole.lightBrightness}%\n• LED Priority Message: \"${pole.oledMessage}\"\n• Advance Trigger Status: ${if (deltaAhead in 0.0..2000.0) "INSIDE 2.0 KM CORRIDOR" else if (deltaAhead < 0.0) "PASSED" else "AHEAD OF WAVE"}\n• Hardware Comms: ${pole.communicationStatus} (ESP-NOW MESH)\n• Traffic: ${pole.vehiclesDetected} vehicles (Avg ${pole.averageSpeedKmh} km/h)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Sub-View 3: Real-Time Corridor Event Log (Change 7)
 * Chronological timestamped event feed:
 * 11:23:10 P2 preparing
 * 11:23:14 P2 activated
 * 11:23:18 P3 preparing
 * 11:24:02 P1 passed
 * 11:24:03 P1 returned to normal
 */
@Composable
fun CorridorEventLogView(viewModel: CorridorViewModel) {
    val eventLogs by viewModel.eventLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CORRIDOR ACTIVATION EVENT LOG",
                    style = MaterialTheme.typography.labelMedium,
                    color = TechCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live chronological corridor state transitions",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            OutlinedButton(
                onClick = { viewModel.clearEventLogs() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Text("CLEAR", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (eventLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No corridor transition events recorded yet.\nStart or step the simulation to view pole-by-pole triggers.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(eventLogs) { evt ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Timestamp pill
                            Surface(
                                color = NavyDark,
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                            ) {
                                Text(
                                    text = evt.timeFormatted,
                                    color = TechCyan,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // State indicator circle
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (evt.state) {
                                            PoleState.ACTIVE -> PoleActive
                                            PoleState.PREPARING -> PolePreparing
                                            PoleState.PASSED -> PolePassed
                                            PoleState.FAULT -> EmergencyRed
                                            else -> PoleNormal
                                        }
                                    )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = evt.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

