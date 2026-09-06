package com.example.smartcorridor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewCarousel
import com.example.smartcorridor.model.VehicleType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcorridor.model.EmergencyRoute
import com.example.smartcorridor.model.PoleState
import com.example.smartcorridor.ui.CorridorViewModel
import kotlin.math.sin
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
import kotlinx.coroutines.launch

/**
 * Smart Streetlight Emergency Corridor Trigger Preview Screen
 *
 * Appears immediately after Destination selection, before Route Confirmation.
 * Visually communicates how poles activate 2.0 KM ahead of the emergency vehicle
 * across both Day and Night operating conditions.
 */
@Composable
fun SmartCorridorPreviewScreen(
    viewModel: CorridorViewModel,
    onContinueToRoutes: () -> Unit,
    onBackToDestination: () -> Unit
) {
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val activeVehicle by viewModel.currentVehicle.collectAsState()

    // Day vs Night Mode toggle
    var isNightMode by remember { mutableStateOf(true) }

    // Visual Presentation Mode: 0 = Architectural Street View, 1 = Route Map Corridor View
    var selectedViewTab by remember { mutableIntStateOf(0) }

    // Animation progress (0.0f = start approaching P1, 1.0f = passed P4 reaching destination)
    val currentVehicle by viewModel.currentVehicle.collectAsState()
    val progressAnim = remember { Animatable(0f) }
    var isPlaying by remember { mutableStateOf(true) }
    var animSpeed by remember { mutableFloatStateOf(1.0f) } // 1x or 1.5x
    val coroutineScope = rememberCoroutineScope()

    // Control animation loop
    LaunchedEffect(isPlaying, animSpeed) {
        while (isPlaying) {
            val remaining = (1f - progressAnim.value) * (8000f / animSpeed)
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = remaining.toInt().coerceAtLeast(100),
                    easing = LinearEasing
                )
            )
            // Pause momentarily at destination, then loop
            delay(1200)
            progressAnim.snapTo(0f)
        }
    }

    val progress = progressAnim.value

    // SOURCE-BASED 2 KM CORRIDOR DEMONSTRATION LOGIC:
    // Route section: 0 to 2800 meters
    // Vehicle starts at SOURCE (0m)
    // Demonstration Smart Poles:
    // P1 = 500m (MG Road Junction)
    // P2 = 1000m (Victoria Flyover)
    // P3 = 1600m (Central Plaza)
    // P4 = 2300m (Hospital Gate)
    val vehicleDistM = progress * 2800.0 // Vehicle position along route (0 to 2800m)
    val p1Delta = 500.0 - vehicleDistM
    val p2Delta = 1000.0 - vehicleDistM
    val p3Delta = 1600.0 - vehicleDistM
    val p4Delta = 2300.0 - vehicleDistM

    // Source-based lookahead logic:
    // Ahead <= 1200m -> ACTIVE (100% brightness green wave)
    // Ahead <= 2000m -> PREPARING (anticipatory 2.0 KM lookahead wave)
    // Behind by <= 120m -> PASSED (hold green until passed)
    // Otherwise -> NORMAL / STANDBY
    fun getDemoPoleState(delta: Double): PoleState = when {
        delta < -350.0 -> PoleState.NORMAL
        delta < -120.0 -> PoleState.PASSED
        delta <= 1200.0 -> PoleState.ACTIVE
        delta <= 2000.0 -> PoleState.PREPARING
        else -> PoleState.NORMAL
    }

    val p1State = getDemoPoleState(p1Delta)
    val p2State = getDemoPoleState(p2Delta)
    val p3State = getDemoPoleState(p3Delta)
    val p4State = getDemoPoleState(p4Delta)

    // Current Next Upcoming Pole & Telemetry Countdown
    val (nextPoleName, nextPoleDistanceKm, nextPoleState) = when {
        p1Delta >= -120.0 -> Triple("P1 (MG Junction - 500m)", (p1Delta.coerceAtLeast(0.0) / 1000.0), p1State)
        p2Delta >= -120.0 -> Triple("P2 (Victoria Flyover - 1.0km)", (p2Delta.coerceAtLeast(0.0) / 1000.0), p2State)
        p3Delta >= -120.0 -> Triple("P3 (Central Plaza - 1.6km)", (p3Delta.coerceAtLeast(0.0) / 1000.0), p3State)
        p4Delta >= -120.0 -> Triple("P4 (Hospital Gate - 2.3km)", (p4Delta.coerceAtLeast(0.0) / 1000.0), p4State)
        else -> Triple("Destination (Trauma Center)", 0.0, PoleState.ACTIVE)
    }

    // Dynamic Frame Stage Description
    val (frameStageTitle, frameStageDetail) = when {
        vehicleDistM < 150.0 -> Pair(
            "FRAME 1 & 2: SOURCE TRIGGER (0m)",
            "Ambulance at SOURCE • Advance 2.0 KM wave triggered immediately: P1 (500m) & P2 (1.0km) ACTIVE • P3 (1.6km) PREPARING • P4 (2.3km) STANDBY"
        )
        vehicleDistM < 800.0 -> Pair(
            "FRAME 3: 2 KM WAVE ROLLING FORWARD (+${vehicleDistM.toInt()}m)",
            "Ambulance at Source + ${vehicleDistM.toInt()}m • P1 passed • P2 & P3 ACTIVE ahead • P4 enters 2.0 KM lookahead window (PREPARING)"
        )
        vehicleDistM < 1600.0 -> Pair(
            "FRAME 4: WAVE CONTINUATION (+${"%.1f".format(vehicleDistM / 1000.0)}km)",
            "Ambulance at Source + ${"%.1f".format(vehicleDistM / 1000.0)}km • P1-P2 passed • P3 & P4 ACTIVE • Continuous 2.0 KM clearance ahead"
        )
        else -> Pair(
            "APPROACHING DESTINATION (+${"%.1f".format(vehicleDistM / 1000.0)}km)",
            "Hospital Trauma Bay entrance in sight • Final intersection secured • All corridor streetlights reverting to baseline"
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Header with Back Navigation & Subtitle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackToDestination,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NavySurface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TechCyan
                    )
                }

                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SMART EMERGENCY CORRIDOR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Smart poles will activate ahead of your vehicle",
                        style = MaterialTheme.typography.bodySmall,
                        color = TechCyan,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    color = NavySurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "PREVIEW",
                        color = TechCyan,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. Selected Destination & Route Intelligence Info Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmergencyRed.copy(alpha = 0.2f))
                                    .border(1.dp, EmergencyRed, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = EmergencyRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SELECTED DESTINATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedRoute.destinationName.ifBlank { "City Hospital Trauma Center" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EST. CORRIDOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${"%.1f".format(selectedRoute.distanceKm.coerceAtLeast(6.2))} KM",
                                style = MaterialTheme.typography.titleMedium,
                                color = TechCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyDark.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = CorridorActiveGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Route intelligence is preparing the emergency corridor ahead",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 3. View Switcher (Streetview Animation vs Route Map) & Day/Night Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Tabs
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurface)
                        .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Surface(
                        modifier = Modifier.clickable { selectedViewTab = 0 },
                        color = if (selectedViewTab == 0) TechCyan.copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewCarousel,
                                contentDescription = null,
                                tint = if (selectedViewTab == 0) TechCyan else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Street Animation",
                                color = if (selectedViewTab == 0) TechCyan else TextMuted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.clickable { selectedViewTab = 1 },
                        color = if (selectedViewTab == 1) TechCyan.copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = if (selectedViewTab == 1) TechCyan else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Route Map",
                                color = if (selectedViewTab == 1) TechCyan else TextMuted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Day / Night Mode Toggle
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NavySurface)
                        .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
                        .clickable { isNightMode = !isNightMode }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color.Transparent
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                            contentDescription = "Day/Night Mode",
                            tint = if (isNightMode) TechCyan else CorridorPreparingAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isNightMode) "NIGHT MODE" else "DAY MODE",
                            color = TextPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. MAIN ANIMATION STAGE: Street-Level Smart Pole Clip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("animated_pole_trigger_preview"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF070C1A) else Color(0xFF1B2A4A)
                ),
                border = BorderStroke(1.5.dp, TechCyan.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Stage Header with Corridor Status Callout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CorridorActiveGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2.0 KM ADVANCE ACTIVATION CONE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CorridorActiveGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = "${(progress * 100).toInt()}% TRANSIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedViewTab == 0) {
                        // View A: Highway Side-Elevation Animated Canvas with embedded Speedometer HUD and Vehicle Telemetry
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNightMode) Color(0xFF050813) else Color(0xFF1E2E4A))
                        ) {
                            AnimatedStreetCorridorCanvas(
                                progress = progress,
                                isNightMode = isNightMode,
                                p1State = p1State,
                                p2State = p2State,
                                p3State = p3State,
                                p4State = p4State,
                                vehicleType = currentVehicle.type
                            )

                            // Live Speedometer HUD embedded inside the clip (Top-Left)
                            val simulatedSpeedKmh = (64.0 + (progress * 12.0) - (sin(progress * 6.28) * 4.0)).coerceIn(48.0, 78.0)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                                color = Color(0xDD040915),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Speedometer",
                                        tint = TechCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "${simulatedSpeedKmh.toInt()} KM/H",
                                            color = TechCyan,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                        Text(
                                            text = "TRANSIT SPEED",
                                            color = TextMuted,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.4.sp
                                        )
                                    }
                                }
                            }

                            // Active Vehicle Identification Badge embedded inside clip (Top-Right)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                color = Color(0xDD040915),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when (currentVehicle.type) {
                                        VehicleType.FIRE_TRUCK -> EmergencyRed
                                        VehicleType.POLICE -> TechCyan
                                        VehicleType.AMBULANCE -> CorridorActiveGreen
                                        else -> TechCyan
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (currentVehicle.type) {
                                            VehicleType.FIRE_TRUCK -> "🚒 ${currentVehicle.registrationNumber}"
                                            VehicleType.POLICE -> "🚓 ${currentVehicle.registrationNumber}"
                                            VehicleType.AMBULANCE -> "🚑 ${currentVehicle.registrationNumber}"
                                            VehicleType.RESCUE -> "🚨 ${currentVehicle.registrationNumber}"
                                        },
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // 2 KM Lookahead & Override Status embedded inside clip (Bottom-Left)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp),
                                color = Color(0xDD040915),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CorridorActiveGreen.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(CorridorActiveGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "WAVE: 2.0 KM LOOKAHEAD",
                                        color = CorridorActiveGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // View B: Route Map Corridor Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF080D1D))
                        ) {
                            AnimatedRouteMapCanvas(
                                progress = progress,
                                p1State = p1State,
                                p2State = p2State,
                                p3State = p3State,
                                p4State = p4State
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2 KM Corridor visual ruler & direction indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "◄ 2.0 KM CORRIDOR WAVE ROLLS AHEAD OF VEHICLE FROM SOURCE ►",
                            style = MaterialTheme.typography.labelSmall,
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 4.5. Source-Based 2 KM Rolling Window Frame Stage Indicator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1F16)),
                border = BorderStroke(1.5.dp, CorridorActiveGreen.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(CorridorActiveGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = frameStageTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = CorridorActiveGreen,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Surface(
                            color = TechCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Source + ${vehicleDistM.toInt()}m",
                                color = TechCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = frameStageDetail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 5. LED Screen Display Simulation Box (Close-Up Hardware Look)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                tint = EmergencyRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SMART POLE PHYSICAL LED DISPLAY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TechCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "ESP32 MATRIX",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Physical LED display board styling
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF030508))
                            .border(1.5.dp, Color(0xFF1E2D4A), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val vehicleApproachText = when (currentVehicle.type) {
                            VehicleType.FIRE_TRUCK -> "🚒 FIRE TRUCK APPROACHING 🚒"
                            VehicleType.POLICE -> "🚓 POLICE PURSUIT 🚓"
                            VehicleType.AMBULANCE -> "🚨 AMBULANCE APPROACHING 🚨"
                            VehicleType.RESCUE -> "🚨 RESCUE SQUAD APPROACHING 🚨"
                        }
                        val activeLedMessage = when {
                            p1State == PoleState.ACTIVE || p2State == PoleState.ACTIVE || p3State == PoleState.ACTIVE || p4State == PoleState.ACTIVE -> {
                                if ((System.currentTimeMillis() / 900) % 2 == 0L) {
                                    vehicleApproachText
                                } else {
                                    "⚠️ PLEASE GIVE WAY • MOVE LEFT ⚠️"
                                }
                            }
                            p1State == PoleState.PREPARING || p2State == PoleState.PREPARING || p3State == PoleState.PREPARING || p4State == PoleState.PREPARING -> {
                                "${currentVehicle.type.displayName.uppercase()} APPROACHING (2.0 KM)"
                            }
                            else -> {
                                "SPEED LIMIT: 50 KM/H • DRIVE SAFELY"
                            }
                        }

                        val ledColor = when {
                            p1State == PoleState.ACTIVE || p2State == PoleState.ACTIVE || p3State == PoleState.ACTIVE || p4State == PoleState.ACTIVE -> EmergencyRed
                            p1State == PoleState.PREPARING || p2State == PoleState.PREPARING || p3State == PoleState.PREPARING || p4State == PoleState.PREPARING -> CorridorPreparingAmber
                            else -> CorridorActiveGreen
                        }

                        Text(
                            text = activeLedMessage,
                            color = ledColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Real-time message broadcast via ESP-NOW wireless mesh network.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 6. Pole Status Progression Badges (P1, P2, P3, P4)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE POLE STATE SYNCHRONIZATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PoleStatusCard(
                            poleName = "P1",
                            location = "MG Junc",
                            routeDist = "500m",
                            deltaDist = if (p1Delta >= 0) "+${p1Delta.toInt()}m" else "${(-p1Delta).toInt()}m past",
                            state = p1State,
                            modifier = Modifier.weight(1f)
                        )
                        PoleStatusCard(
                            poleName = "P2",
                            location = "Victoria",
                            routeDist = "1.0km",
                            deltaDist = if (p2Delta >= 0) "+${p2Delta.toInt()}m" else "${(-p2Delta).toInt()}m past",
                            state = p2State,
                            modifier = Modifier.weight(1f)
                        )
                        PoleStatusCard(
                            poleName = "P3",
                            location = "Plaza",
                            routeDist = "1.6km",
                            deltaDist = if (p3Delta >= 0) "+${p3Delta.toInt()}m" else "${(-p3Delta).toInt()}m past",
                            state = p3State,
                            modifier = Modifier.weight(1f)
                        )
                        PoleStatusCard(
                            poleName = "P4",
                            location = "Hospital",
                            routeDist = "2.3km",
                            deltaDist = if (p4Delta >= 0) "+${p4Delta.toInt()}m" else "${(-p4Delta).toInt()}m past",
                            state = p4State,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 7. Live Next Pole Telemetry Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NEXT POLE IN CORRIDOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = nextPoleName,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Activation point: 2.0 km prior to arrival",
                            style = MaterialTheme.typography.bodySmall,
                            color = TechCyan,
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "${"%.1f".format(nextPoleDistanceKm)} KM",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (nextPoleDistanceKm <= 2.0) CorridorActiveGreen else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            color = when (nextPoleState) {
                                PoleState.ACTIVE -> CorridorActiveGreen.copy(alpha = 0.2f)
                                PoleState.PREPARING -> CorridorPreparingAmber.copy(alpha = 0.2f)
                                else -> NavySurfaceVariant
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = nextPoleState.displayName.uppercase(),
                                color = when (nextPoleState) {
                                    PoleState.ACTIVE -> CorridorActiveGreen
                                    PoleState.PREPARING -> CorridorPreparingAmber
                                    else -> TextSecondary
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 8. Day & Night Architecture Proof Note
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                            contentDescription = null,
                            tint = if (isNightMode) TechCyan else CorridorPreparingAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isNightMode) "NIGHT OPERATION BEHAVIOR" else "DAY OPERATION BEHAVIOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNightMode) TechCyan else CorridorPreparingAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isNightMode) {
                            "Streetlights normally operate in 15% dimmed mode at night. Emergency activation overrides ambient LDR sensor and fires 100% emergency strobe brightness."
                        } else {
                            "During the day, streetlights are normally completely OFF. Emergency activation immediately turns ON the streetlight luminaire and OLED warning screen ahead of the ambulance."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 9. Animation Controls (Replay, Play/Pause, Speed)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            progressAnim.snapTo(0f)
                            isPlaying = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f))
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Replay", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPlaying) "Pause" else "Play", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        animSpeed = if (animSpeed == 1.0f) 1.5f else 1.0f
                    },
                    modifier = Modifier.weight(0.8f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${animSpeed}x", fontSize = 12.sp)
                }
            }
        }

        // 10. Primary Proceed CTA Button: "CONTINUE TO ROUTE SELECTION"
        item {
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onContinueToRoutes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("continue_to_routes_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CorridorActiveGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CONTINUE TO ROUTE SELECTION",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Pole Status Card displaying Pole ID, State, and Color Badge
 */
@Composable
fun PoleStatusCard(
    poleName: String,
    location: String,
    routeDist: String = "",
    deltaDist: String = "",
    state: PoleState,
    modifier: Modifier = Modifier
) {
    val stateColor = when (state) {
        PoleState.ACTIVE -> CorridorActiveGreen
        PoleState.PREPARING -> CorridorPreparingAmber
        PoleState.PASSED -> PolePassed
        else -> TextMuted
    }

    val containerBg = when (state) {
        PoleState.ACTIVE -> CorridorActiveGreen.copy(alpha = 0.12f)
        PoleState.PREPARING -> CorridorPreparingAmber.copy(alpha = 0.12f)
        else -> NavySurfaceVariant.copy(alpha = 0.6f)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, stateColor.copy(alpha = if (state == PoleState.NORMAL) 0.3f else 0.8f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = poleName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = location,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp
            )
            if (routeDist.isNotEmpty()) {
                Text(
                    text = routeDist,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechCyan,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = stateColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = state.displayName.uppercase(),
                    color = stateColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            if (deltaDist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = deltaDist,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (deltaDist.contains("past")) TextMuted else TechCyan,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Architectural Street-Level Animation Canvas
 * Shows actual physical smart streetlights (mast, ESP32 box, curved arm, LED display, luminaire cone)
 * and the emergency vehicle driving forward with its 2.0 KM forward corridor wave.
 */
@Composable
fun AnimatedStreetCorridorCanvas(
    progress: Float,
    isNightMode: Boolean,
    p1State: PoleState,
    p2State: PoleState,
    p3State: PoleState,
    p4State: PoleState,
    vehicleType: VehicleType = VehicleType.AMBULANCE
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Ground & Road geometry
        val skyHeight = h * 0.45f
        val roadTop = skyHeight
        val roadBottom = h * 0.90f
        val roadHeight = roadBottom - roadTop

        // 1. Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isNightMode) {
                    listOf(Color(0xFF030712), Color(0xFF0A1128))
                } else {
                    listOf(Color(0xFF162544), Color(0xFF243B68))
                },
                startY = 0f,
                endY = skyHeight
            ),
            size = Size(w, skyHeight)
        )

        // 2. Distant city skyline silhouette
        drawCitySkyline(w, skyHeight, isNightMode)

        // 3. Road Surface (Asphalt)
        drawRect(
            color = Color(0xFF121620),
            topLeft = Offset(0f, roadTop),
            size = Size(w, roadHeight)
        )

        // Road Curb / Sidewalk
        drawRect(
            color = Color(0xFF26334D),
            topLeft = Offset(0f, roadTop - 6f),
            size = Size(w, 6f)
        )
        drawRect(
            color = Color(0xFF1E283C),
            topLeft = Offset(0f, roadBottom),
            size = Size(w, h - roadBottom)
        )

        // Dashed Center Road Line
        val dashWidth = 24f
        val dashGap = 16f
        var currentX = 0f
        val laneY = roadTop + roadHeight * 0.5f
        while (currentX < w) {
            drawLine(
                color = Color(0xFFE2E8F0).copy(alpha = 0.4f),
                start = Offset(currentX, laneY),
                end = Offset(currentX + dashWidth, laneY),
                strokeWidth = 3f
            )
            currentX += dashWidth + dashGap
        }

        // 4. Pole Positions along canvas X axis (Route section 0 to 2800m)
        // P1 at 500m (~0.21w), P2 at 1000m (~0.42w), P3 at 1600m (~0.65w), P4 at 2300m (~0.88w)
        val poleX1 = w * 0.21f
        val poleX2 = w * 0.42f
        val poleX3 = w * 0.65f
        val poleX4 = w * 0.88f

        val poleList = listOf(
            Triple(poleX1, p1State, "P1"),
            Triple(poleX2, p2State, "P2"),
            Triple(poleX3, p3State, "P3"),
            Triple(poleX4, p4State, "P4")
        )

        // 4.5. Render SOURCE Starting Point Marker at left edge
        val sourceX = w * 0.05f
        drawLine(
            color = TechCyan.copy(alpha = 0.8f),
            start = Offset(sourceX, roadTop - 20f),
            end = Offset(sourceX, roadBottom + 5f),
            strokeWidth = 2.5f
        )
        drawCircle(
            color = TechCyan,
            radius = 5f,
            center = Offset(sourceX, roadTop - 20f)
        )
        // Source Starting Gate Banner
        drawRoundRect(
            color = Color(0xFF0F2B1D),
            topLeft = Offset(sourceX - 16f, roadTop - 34f),
            size = Size(32f, 13f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = CorridorActiveGreen,
            topLeft = Offset(sourceX - 16f, roadTop - 34f),
            size = Size(32f, 13f),
            cornerRadius = CornerRadius(2f, 2f),
            style = Stroke(width = 1f)
        )

        // 5. Emergency Vehicle Position
        // Vehicle starts directly at SOURCE (sourceX) and travels along the route
        val vehicleX = sourceX + progress * (w * 0.90f)
        val vehicleY = laneY + roadHeight * 0.18f

        // 6. 2.0 KM FORWARD EMERGENCY CORRIDOR BEAM
        // Reaches 2.0 KM ahead along the route (~0.58 of the route length)
        val corridorReach = w * 0.58f
        val corridorStart = vehicleX + 35f
        val corridorEnd = (corridorStart + corridorReach).coerceAtMost(w)

        if (corridorStart < w) {
            val corridorWidth = (corridorEnd - corridorStart).coerceAtLeast(0f)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CorridorActiveGreen.copy(alpha = 0.38f),
                        CorridorActiveGreen.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startX = corridorStart,
                    endX = corridorEnd
                ),
                topLeft = Offset(corridorStart, roadTop),
                size = Size(corridorWidth, roadHeight)
            )

            // Dynamic corridor boundary line
            drawLine(
                color = CorridorActiveGreen.copy(alpha = 0.85f),
                start = Offset(corridorStart, roadTop),
                end = Offset(corridorEnd, roadTop),
                strokeWidth = 2.5f
            )
            drawLine(
                color = CorridorActiveGreen.copy(alpha = 0.85f),
                start = Offset(corridorStart, roadBottom),
                end = Offset(corridorEnd, roadBottom),
                strokeWidth = 2.5f
            )
        }

        // 7. Render Smart Streetlight Poles (Physical Structure)
        poleList.forEach { (px, pState, pLabel) ->
            drawPhysicalSmartPole(
                scope = this,
                poleX = px,
                groundY = roadTop,
                state = pState,
                label = pLabel,
                isNightMode = isNightMode
            )
        }

        // 8. Render Emergency Vehicle (Ambulance, Fire Truck, Police, Rescue)
        drawEmergencyVehicle(
            scope = this,
            x = vehicleX,
            y = vehicleY,
            progress = progress,
            vehicleType = vehicleType
        )
    }
}

/**
 * Renders the physical smart streetlight pole hardware:
 * - Steel pole mast
 * - Hardware enclosure box with ESP32 & sensors
 * - Electronic LED display board
 * - Curved cantilever streetlight arm
 * - High-output luminaire fixture
 * - Luminaire light cone casting onto the road
 */
private fun drawPhysicalSmartPole(
    scope: DrawScope,
    poleX: Float,
    groundY: Float,
    state: PoleState,
    label: String,
    isNightMode: Boolean
) {
    scope.apply {
        val poleHeight = groundY * 0.82f
        val poleTopY = groundY - poleHeight

        // Determine light & glow color based on state
        val lightColor = when (state) {
            PoleState.ACTIVE -> CorridorActiveGreen
            PoleState.PREPARING -> CorridorPreparingAmber
            PoleState.PASSED -> PolePassed
            else -> if (isNightMode) Color(0xFFCBD5E1).copy(alpha = 0.3f) else Color.Transparent
        }

        val luminaireX = poleX + 22f
        val luminaireY = poleTopY - 6f

        // A. Streetlight Luminaire Light Cone down to road
        if (state == PoleState.ACTIVE || state == PoleState.PREPARING || (isNightMode && state == PoleState.NORMAL)) {
            val conePath = Path().apply {
                moveTo(luminaireX, luminaireY)
                lineTo(luminaireX - 35f, groundY + 40f)
                lineTo(luminaireX + 35f, groundY + 40f)
                close()
            }

            val coneAlpha = when (state) {
                PoleState.ACTIVE -> 0.45f
                PoleState.PREPARING -> 0.28f
                else -> 0.10f
            }

            drawPath(
                path = conePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lightColor.copy(alpha = coneAlpha),
                        lightColor.copy(alpha = coneAlpha * 0.5f),
                        Color.Transparent
                    ),
                    startY = luminaireY,
                    endY = groundY + 40f
                )
            )

            // Radial hotspot on the road surface
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lightColor.copy(alpha = coneAlpha * 0.8f),
                        Color.Transparent
                    ),
                    center = Offset(luminaireX, groundY + 10f),
                    radius = 35f
                ),
                topLeft = Offset(luminaireX - 35f, groundY - 5f),
                size = Size(70f, 30f)
            )
        }

        // B. Steel Pole Mast (Vertical Column)
        drawLine(
            color = Color(0xFF64748B),
            start = Offset(poleX, groundY),
            end = Offset(poleX, poleTopY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Pole base anchor
        drawRect(
            color = Color(0xFF334155),
            topLeft = Offset(poleX - 5f, groundY - 5f),
            size = Size(10f, 6f)
        )

        // C. Cantilever Luminaire Arm (Curved over road)
        val armPath = Path().apply {
            moveTo(poleX, poleTopY + 10f)
            quadraticTo(poleX, poleTopY - 8f, luminaireX, luminaireY)
        }
        drawPath(
            path = armPath,
            color = Color(0xFF94A3B8),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // D. Streetlight Luminaire Head (Light Fixture)
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(luminaireX - 6f, luminaireY - 3f),
            size = Size(14f, 7f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Light emitter bulb
        drawCircle(
            color = if (lightColor == Color.Transparent) Color(0xFF475569) else lightColor,
            radius = if (state == PoleState.ACTIVE) 4.5f else 3f,
            center = Offset(luminaireX + 1f, luminaireY + 2f)
        )

        // Luminaire glow aura when active
        if (state == PoleState.ACTIVE) {
            drawCircle(
                color = CorridorActiveGreen.copy(alpha = 0.4f),
                radius = 11f,
                center = Offset(luminaireX + 1f, luminaireY + 2f)
            )
        }

        // E. Hardware Enclosure Box (ESP32 + Sensors) mounted on pole
        val boxY = groundY - poleHeight * 0.45f
        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(poleX - 7f, boxY),
            size = Size(9f, 16f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = TechCyan.copy(alpha = 0.7f),
            topLeft = Offset(poleX - 7f, boxY),
            size = Size(9f, 16f),
            cornerRadius = CornerRadius(2f, 2f),
            style = Stroke(width = 1f)
        )
        // Blinking status sensor LED on the ESP32 box
        drawCircle(
            color = if (state == PoleState.ACTIVE) EmergencyRed else CorridorActiveGreen,
            radius = 1.5f,
            center = Offset(poleX - 2.5f, boxY + 4f)
        )

        // F. Electronic LED Message Display Board
        val ledBoardY = groundY - poleHeight * 0.75f
        drawRoundRect(
            color = Color(0xFF020408),
            topLeft = Offset(poleX - 16f, ledBoardY),
            size = Size(24f, 11f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = if (state == PoleState.ACTIVE) EmergencyRed else Color(0xFF334155),
            topLeft = Offset(poleX - 16f, ledBoardY),
            size = Size(24f, 11f),
            cornerRadius = CornerRadius(2f, 2f),
            style = Stroke(width = 1f)
        )

        // Animated LED text strip (represented by luminous matrix blocks)
        val ledTextColor = when (state) {
            PoleState.ACTIVE -> EmergencyRed
            PoleState.PREPARING -> CorridorPreparingAmber
            else -> CorridorActiveGreen.copy(alpha = 0.8f)
        }
        drawLine(
            color = ledTextColor,
            start = Offset(poleX - 13f, ledBoardY + 5.5f),
            end = Offset(poleX + 5f, ledBoardY + 5.5f),
            strokeWidth = 2.5f
        )

        // G. Pole State Label Tag Badge
        val tagBg = when (state) {
            PoleState.ACTIVE -> CorridorActiveGreen
            PoleState.PREPARING -> CorridorPreparingAmber
            else -> Color(0xFF334155)
        }
        drawRoundRect(
            color = tagBg,
            topLeft = Offset(poleX - 12f, groundY + 8f),
            size = Size(24f, 10f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

/**
 * Draws the Emergency Vehicle (Ambulance, Fire Truck, Police, Rescue) with headlights, siren strobes, and distinctive livery
 */
private fun drawEmergencyVehicle(
    scope: DrawScope,
    x: Float,
    y: Float,
    progress: Float,
    vehicleType: VehicleType = VehicleType.AMBULANCE
) {
    scope.apply {
        // Vehicle Shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.6f),
            topLeft = Offset(x - 5f, y + 16f),
            size = Size(48f, 8f)
        )

        // Headlight beam projecting forward onto road
        val beamPath = Path().apply {
            moveTo(x + 38f, y + 8f)
            lineTo(x + 95f, y - 4f)
            lineTo(x + 95f, y + 22f)
            close()
        }
        drawPath(
            path = beamPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFFFBEB).copy(alpha = 0.45f),
                    Color(0xFFFEF08A).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                startX = x + 38f,
                endX = x + 95f
            )
        )

        val mainBodyColor = when (vehicleType) {
            VehicleType.FIRE_TRUCK -> Color(0xFFDC2626) // Fire Engine Red
            VehicleType.POLICE -> Color(0xFF0F172A)     // Dark Tactical Blue/Black
            VehicleType.RESCUE -> Color(0xFFEA580C)     // Safety Orange
            VehicleType.AMBULANCE -> Color(0xFFF8FAFC)  // Clean Ambulance White
        }

        val cabColor = when (vehicleType) {
            VehicleType.FIRE_TRUCK -> Color(0xFFB91C1C)
            VehicleType.POLICE -> Color(0xFF0F172A)
            VehicleType.RESCUE -> Color(0xFFC2410C)
            VehicleType.AMBULANCE -> Color(0xFFF8FAFC)
        }

        // Vehicle Main Body
        drawRoundRect(
            color = mainBodyColor,
            topLeft = Offset(x, y),
            size = Size(38f, 18f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Vehicle Front Hood / Cab
        drawRoundRect(
            color = cabColor,
            topLeft = Offset(x + 24f, y + 4f),
            size = Size(14f, 14f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Police Special Door Panel (White center doors)
        if (vehicleType == VehicleType.POLICE) {
            drawRect(
                color = Color(0xFFF8FAFC),
                topLeft = Offset(x + 10f, y + 2f),
                size = Size(14f, 14f)
            )
        }

        // Windshield (Cyan tinted glass)
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(x + 25f, y + 5f),
            size = Size(8f, 7f),
            cornerRadius = CornerRadius(1f, 1f)
        )

        // Vehicle-Specific Livery & Stripes
        when (vehicleType) {
            VehicleType.AMBULANCE -> {
                // Red Emergency Stripe along side
                drawRect(
                    color = EmergencyRed,
                    topLeft = Offset(x, y + 8f),
                    size = Size(36f, 3f)
                )
                // Red Cross on body
                drawRect(color = EmergencyRed, topLeft = Offset(x + 12f, y + 3f), size = Size(2f, 6f))
                drawRect(color = EmergencyRed, topLeft = Offset(x + 10f, y + 5f), size = Size(6f, 2f))
            }
            VehicleType.FIRE_TRUCK -> {
                // High-visibility yellow hazard chevron stripe
                drawRect(
                    color = Color(0xFFFBBF24),
                    topLeft = Offset(x, y + 8f),
                    size = Size(36f, 3f)
                )
                // Roof ladder (Silver/Chrome)
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(x + 4f, y - 6f),
                    size = Size(26f, 3f),
                    cornerRadius = CornerRadius(1f, 1f)
                )
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(x + 10f, y - 6f),
                    end = Offset(x + 10f, y - 3f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(x + 18f, y - 6f),
                    end = Offset(x + 18f, y - 3f),
                    strokeWidth = 1f
                )
            }
            VehicleType.POLICE -> {
                // Tech Cyan Tactical line
                drawRect(
                    color = TechCyan,
                    topLeft = Offset(x, y + 9f),
                    size = Size(36f, 2.5f)
                )
            }
            VehicleType.RESCUE -> {
                // White Reflective Safety Stripe
                drawRect(
                    color = Color(0xFFF8FAFC),
                    topLeft = Offset(x, y + 8f),
                    size = Size(36f, 3f)
                )
            }
        }

        // Wheels
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 4.5f,
            center = Offset(x + 8f, y + 18f)
        )
        drawCircle(
            color = Color(0xFF94A3B8),
            radius = 2f,
            center = Offset(x + 8f, y + 18f)
        )

        drawCircle(
            color = Color(0xFF0F172A),
            radius = 4.5f,
            center = Offset(x + 30f, y + 18f)
        )
        drawCircle(
            color = Color(0xFF94A3B8),
            radius = 2f,
            center = Offset(x + 30f, y + 18f)
        )

        // Roof Siren Flasher (Alternating Strobes)
        val flashRed = ((progress * 80).toInt() % 2 == 0)
        val sirenColor = when (vehicleType) {
            VehicleType.FIRE_TRUCK -> if (flashRed) EmergencyRed else Color(0xFFF59E0B)
            VehicleType.POLICE -> if (flashRed) Color(0xFF2563EB) else EmergencyRed
            VehicleType.RESCUE -> if (flashRed) Color(0xFFF97316) else Color(0xFFFACC15)
            VehicleType.AMBULANCE -> if (flashRed) EmergencyRed else Color(0xFF2563EB)
        }

        drawRoundRect(
            color = sirenColor,
            topLeft = Offset(x + 12f, y - 4f),
            size = Size(8f, 4f),
            cornerRadius = CornerRadius(1f, 1f)
        )
        // Siren Aura Strobe
        drawCircle(
            color = sirenColor.copy(alpha = 0.55f),
            radius = 9f,
            center = Offset(x + 16f, y - 2f)
        )
    }
}

/**
 * Draws background city skyline silhouette
 */
private fun drawCitySkyline(w: Float, skyHeight: Float, isNightMode: Boolean) {
    // Subtle architectural backdrop
}

/**
 * Route Map Corridor View Canvas (Top-Down Route Overview)
 */
@Composable
fun AnimatedRouteMapCanvas(
    progress: Float,
    p1State: PoleState,
    p2State: PoleState,
    p3State: PoleState,
    p4State: PoleState
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Dark Route Map Grid
        val gridStep = 30f
        var gx = 0f
        while (gx < w) {
            drawLine(
                color = Color(0xFF1E2D4D).copy(alpha = 0.25f),
                start = Offset(gx, 0f),
                end = Offset(gx, h),
                strokeWidth = 1f
            )
            gx += gridStep
        }
        var gy = 0f
        while (gy < h) {
            drawLine(
                color = Color(0xFF1E2D4D).copy(alpha = 0.25f),
                start = Offset(0f, gy),
                end = Offset(w, gy),
                strokeWidth = 1f
            )
            gy += gridStep
        }

        // Curved Route Polyline Path
        val startPoint = Offset(w * 0.08f, h * 0.65f)
        val ctrl1 = Offset(w * 0.35f, h * 0.30f)
        val ctrl2 = Offset(w * 0.65f, h * 0.75f)
        val endPoint = Offset(w * 0.92f, h * 0.35f)

        val routePath = Path().apply {
            moveTo(startPoint.x, startPoint.y)
            cubicTo(ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, endPoint.x, endPoint.y)
        }

        // Broad road corridor base
        drawPath(
            path = routePath,
            color = Color(0xFF1E293B),
            style = Stroke(width = 24f, cap = StrokeCap.Round)
        )

        // Route center line
        drawPath(
            path = routePath,
            color = TechCyan.copy(alpha = 0.6f),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // 4 Pole Node Points sampled along cubic curve
        val poles = listOf(
            Pair(Offset(w * 0.22f, h * 0.48f), p1State),
            Pair(Offset(w * 0.45f, h * 0.44f), p2State),
            Pair(Offset(w * 0.68f, h * 0.62f), p3State),
            Pair(Offset(w * 0.88f, h * 0.38f), p4State)
        )

        // Emergency Vehicle Position along route
        val t = progress
        // Approximate cubic Bezier coordinate
        val vx = (1 - t) * (1 - t) * (1 - t) * startPoint.x +
                3 * (1 - t) * (1 - t) * t * ctrl1.x +
                3 * (1 - t) * t * t * ctrl2.x +
                t * t * t * endPoint.x
        val vy = (1 - t) * (1 - t) * (1 - t) * startPoint.y +
                3 * (1 - t) * (1 - t) * t * ctrl1.y +
                3 * (1 - t) * t * t * ctrl2.y +
                t * t * t * endPoint.y

        // 2 KM Corridor forward aura ahead of vehicle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    CorridorActiveGreen.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = Offset(vx, vy),
                radius = 80f
            ),
            radius = 80f,
            center = Offset(vx, vy)
        )

        // Draw Pole Nodes
        poles.forEachIndexed { index, (pos, state) ->
            val nodeColor = when (state) {
                PoleState.ACTIVE -> CorridorActiveGreen
                PoleState.PREPARING -> CorridorPreparingAmber
                PoleState.PASSED -> PolePassed
                else -> Color(0xFF64748B)
            }

            // Pole glow ring when active
            if (state == PoleState.ACTIVE) {
                drawCircle(
                    color = CorridorActiveGreen.copy(alpha = 0.3f),
                    radius = 16f,
                    center = pos
                )
            }

            // Pole Node Core
            drawCircle(
                color = nodeColor,
                radius = 7f,
                center = pos
            )
            drawCircle(
                color = Color(0xFF0B132B),
                radius = 3.5f,
                center = pos
            )
        }

        // Vehicle Marker Icon
        drawCircle(
            color = EmergencyRed,
            radius = 9f,
            center = Offset(vx, vy)
        )
        drawCircle(
            color = Color.White,
            radius = 4f,
            center = Offset(vx, vy)
        )

        // Destination Marker Pin
        drawCircle(
            color = CorridorActiveGreen,
            radius = 11f,
            center = endPoint
        )
        drawCircle(
            color = Color.Black,
            radius = 5f,
            center = endPoint
        )
    }
}
