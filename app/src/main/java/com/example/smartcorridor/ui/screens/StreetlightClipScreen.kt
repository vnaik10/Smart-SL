package com.example.smartcorridor.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcorridor.model.PoleState
import com.example.smartcorridor.model.VehicleType
import com.example.smartcorridor.ui.CorridorViewModel
import com.example.ui.theme.CorridorActiveGreen
import com.example.ui.theme.CorridorPreparingAmber
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.sin

/**
 * Dedicated Full Screen Page: Smart Streetlight Animated Clip & Live Speed Telemetry
 * Displays:
 * 1. Street-Level Animated Clip showing real-time streetlights and the active vehicle
 * 2. Speedometer HUD and vehicle identification embedded directly in the clip
 * 3. 2 KM Advance Wave Lookahead telemetry, Next Pole distance, and LDR 24/7 Day/Night override
 * 4. Interactive Speed Throttle (40 - 110 KM/H) and Day/Night mode toggles
 */
@Composable
fun StreetlightClipScreen(
    viewModel: CorridorViewModel,
    onBack: () -> Unit = {}
) {
    val currentVehicle by viewModel.currentVehicle.collectAsState()
    val currentGps by viewModel.currentGps.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val corridorStatus by viewModel.corridorStatus.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    var isPlaying by remember { mutableStateOf(true) }
    var isNightMode by remember { mutableStateOf(true) }
    var throttleSpeedKmh by remember { mutableFloatStateOf(68f) }

    // Continuous smooth looping transit progress
    val infiniteTransition = rememberInfiniteTransition(label = "clip_progress_transition")
    val autoProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (14000 * (68f / throttleSpeedKmh)).toInt().coerceIn(6000, 24000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "auto_progress"
    )

    val effectiveProgress: Float = if (activeSession != null) {
        val distMeters = corridorStatus.vehicleStationMeters.toFloat()
        val totalMeters = (selectedRoute.distanceKm * 1000.0).toFloat().coerceAtLeast(1000f)
        (distMeters / totalMeters).coerceIn(0f, 1f)
    } else {
        autoProgress
    }

    // Determine pole states based on progress
    val p1State = when {
        effectiveProgress < 0.10f -> PoleState.PREPARING
        effectiveProgress in 0.10f..0.38f -> PoleState.ACTIVE
        else -> PoleState.NORMAL
    }
    val p2State = when {
        effectiveProgress < 0.20f -> PoleState.NORMAL
        effectiveProgress in 0.20f..0.36f -> PoleState.PREPARING
        effectiveProgress in 0.36f..0.64f -> PoleState.ACTIVE
        else -> PoleState.NORMAL
    }
    val p3State = when {
        effectiveProgress < 0.45f -> PoleState.NORMAL
        effectiveProgress in 0.45f..0.62f -> PoleState.PREPARING
        effectiveProgress in 0.62f..0.88f -> PoleState.ACTIVE
        else -> PoleState.NORMAL
    }
    val p4State = when {
        effectiveProgress < 0.70f -> PoleState.NORMAL
        effectiveProgress in 0.70f..0.86f -> PoleState.PREPARING
        effectiveProgress >= 0.86f -> PoleState.ACTIVE
        else -> PoleState.NORMAL
    }

    // Dynamic calculated speed
    val displaySpeedKmh: Float = if (activeSession != null) {
        currentGps.speedKmh
    } else {
        (throttleSpeedKmh + sin(effectiveProgress.toDouble() * 12.5).toFloat() * 4f).coerceIn(35f, 120f)
    }

    val vehicleThemeColor = when (currentVehicle.type) {
        VehicleType.FIRE_TRUCK -> EmergencyRed
        VehicleType.POLICE -> TechCyan
        VehicleType.AMBULANCE -> CorridorActiveGreen
        VehicleType.RESCUE -> Color(0xFFEA580C)
    }

    val vehicleIcon = when (currentVehicle.type) {
        VehicleType.FIRE_TRUCK -> Icons.Default.LocalFireDepartment
        VehicleType.POLICE -> Icons.Default.Security
        VehicleType.AMBULANCE -> Icons.Default.LocalHospital
        VehicleType.RESCUE -> Icons.Default.Emergency
    }

    val nextPoleName = when {
        effectiveProgress < 0.25f -> "P01 - MG Road Entry"
        effectiveProgress < 0.50f -> "P02 - Metro Jn"
        effectiveProgress < 0.75f -> "P03 - Cross Road"
        else -> "P04 - Hospital Gate"
    }

    val distToNextM: Int = when {
        effectiveProgress < 0.25f -> ((0.25f - effectiveProgress) * 2000f).toInt().coerceAtLeast(40)
        effectiveProgress < 0.50f -> ((0.50f - effectiveProgress) * 2000f).toInt().coerceAtLeast(40)
        effectiveProgress < 0.75f -> ((0.75f - effectiveProgress) * 2000f).toInt().coerceAtLeast(40)
        else -> ((1.0f - effectiveProgress) * 1500f).toInt().coerceAtLeast(20)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Navigation & Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TechCyan
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "STREETLIGHT CLIP & LIVE HUD",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = CorridorActiveGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, CorridorActiveGreen)
                                ) {
                                    Text(
                                        text = if (activeSession != null) "LIVE MISSION" else "SIMULATION",
                                        color = CorridorActiveGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "High-speed 3D-perspective corridor animation with speed HUD",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = vehicleThemeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, vehicleThemeColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = vehicleIcon,
                                contentDescription = null,
                                tint = vehicleThemeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentVehicle.type.displayName.uppercase(),
                                color = vehicleThemeColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // 1. THE HERO ANIMATED STREETLIGHT CLIP
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streetlight_clip_card"),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.5.dp, TechCyan.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Clip Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = CorridorActiveGreen,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STREET CORRIDOR VISUALIZER",
                                style = MaterialTheme.typography.labelMedium,
                                color = CorridorActiveGreen,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        // Day / Night Toggle Pill
                        Surface(
                            onClick = { isNightMode = !isNightMode },
                            color = NavyDark,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isNightMode) TechCyan else CorridorPreparingAmber)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isNightMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                                    contentDescription = "Day/Night Mode",
                                    tint = if (isNightMode) TechCyan else CorridorPreparingAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isNightMode) "NIGHT" else "DAY",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // THE ANIMATED CLIP BOX WITH EMBEDDED SPEEDOMETER & TELEMETRY
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isNightMode) Color(0xFF040711) else Color(0xFF1E2E4A))
                    ) {
                        // 1. Street-Level Animated Streetlight Canvas
                        AnimatedStreetCorridorCanvas(
                            progress = effectiveProgress,
                            isNightMode = isNightMode,
                            p1State = p1State,
                            p2State = p2State,
                            p3State = p3State,
                            p4State = p4State,
                            vehicleType = currentVehicle.type
                        )

                        // 2. EMBEDDED SPEEDOMETER HUD (Top-Left of Clip)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            color = Color(0xDD040915),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Speedometer",
                                    tint = TechCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "${displaySpeedKmh.toInt()} KM/H",
                                        color = TechCyan,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = "TRANSIT SPEED",
                                        color = TextMuted,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        // 3. EMBEDDED VEHICLE IDENTIFIER BADGE (Top-Right of Clip)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            color = Color(0xDD040915),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, vehicleThemeColor.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = vehicleIcon,
                                    contentDescription = "Vehicle Type",
                                    tint = vehicleThemeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = currentVehicle.registrationNumber,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                    Text(
                                        text = currentVehicle.type.displayName.uppercase(),
                                        color = vehicleThemeColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 4. EMBEDDED 2.0 KM LOOKAHEAD WAVE STATUS (Bottom-Center of Clip)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            color = Color(0xDD051A10),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CorridorActiveGreen.copy(alpha = 0.8f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CorridorActiveGreen,
                                    modifier = Modifier.size(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2.0 KM LOOKAHEAD ACTIVATED • NEXT: $nextPoleName ($distToNextM m)",
                                    color = CorridorActiveGreen,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Track
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CORRIDOR TRANSIT PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${(effectiveProgress * 100).toInt()}% • ${(effectiveProgress * selectedRoute.distanceKm.toFloat()).format1Dec()} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { effectiveProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CorridorActiveGreen,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }

        // 2. INTERACTIVE CONTROLS: Speed Throttle & Simulation Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SPEED THROTTLE & WAVE CONTROLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Simulated Driving Speed:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${throttleSpeedKmh.toInt()} KM/H",
                            style = MaterialTheme.typography.titleMedium,
                            color = TechCyan,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = throttleSpeedKmh,
                        onValueChange = { throttleSpeedKmh = it },
                        valueRange = 40f..110f,
                        steps = 14,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = TechCyan,
                            activeTrackColor = TechCyan,
                            inactiveTrackColor = Color(0xFF1E293B)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.stepSimulationManual() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TechCyan),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = NavySurface)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = null, tint = TechCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("STEP WAVE", color = TechCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.toggleSimulationPlayPause() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) NavySurface else CorridorActiveGreen),
                            border = BorderStroke(1.dp, if (isPlaying) NavyBorder else CorridorActiveGreen)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isPlaying) TextPrimary else Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "PAUSE CLIP" else "PLAY CLIP",
                                color = if (isPlaying) TextPrimary else Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. DETAILED SPEEDOMETER & TELEMETRY DECK
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Speed & Kinetic Telemetry
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = TechCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SPEED TELEMETRY", style = MaterialTheme.typography.labelSmall, color = TechCyan, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${displaySpeedKmh.toInt()} km/h",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Limit: 50 km/h (Emergency Override)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Acceleration", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("+1.4 m/s²", style = MaterialTheme.typography.labelSmall, color = CorridorActiveGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Card 2: 24/7 Streetlight Strobe Telemetry
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = BorderStroke(1.dp, CorridorActiveGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = CorridorActiveGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("STREETLIGHT WAVE", style = MaterialTheme.typography.labelSmall, color = CorridorActiveGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "100% STROBE",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "LDR 24/7 Day/Night Override",
                            style = MaterialTheme.typography.labelSmall,
                            color = CorridorActiveGreen,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Advance Window", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("2.0 KM Cone", style = MaterialTheme.typography.labelSmall, color = TechCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. PHYSICAL SMART POLE LED DISPLAY BOARD
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
                            Icon(Icons.Default.Emergency, contentDescription = null, tint = vehicleThemeColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STREETLIGHT PHYSICAL MATRIX DISPLAY (ESP32)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TechCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text("P02 MATRIX", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val activeLedMessage = when (currentVehicle.type) {
                        VehicleType.FIRE_TRUCK -> "🚒 FIRE TRUCK APPROACHING 🚒 • CLEAR EMERGENCY LANE"
                        VehicleType.POLICE -> "🚓 POLICE PURSUIT 🚓 • MOVE ASIDE IMMEDIATELY"
                        VehicleType.AMBULANCE -> "🚨 AMBULANCE APPROACHING 🚨 • PLEASE GIVE WAY"
                        VehicleType.RESCUE -> "🚨 RESCUE SQUAD APPROACHING 🚨 • YIELD TO RIGHT"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF030508))
                            .border(1.5.dp, Color(0xFF1E2D4A), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeLedMessage,
                            color = when (currentVehicle.type) {
                                VehicleType.FIRE_TRUCK -> Color(0xFFFF5252)
                                VehicleType.POLICE -> Color(0xFF38BDF8)
                                else -> Color(0xFFFFCC00)
                            },
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Displays real-time instructions to civilian drivers 2.0 KM ahead of ${currentVehicle.type.displayName.lowercase()}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

private fun Float.format1Dec(): String = String.format(Locale.US, "%.1f", this)
