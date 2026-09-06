package com.example.smartcorridor.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcorridor.model.PoleState
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

@Composable
fun HardwareSimulatorScreen(viewModel: CorridorViewModel) {
    val poles by viewModel.poles.collectAsState()
    var selectedPoleId by remember { mutableStateOf("P02") }

    val currentPole = poles.find { it.poleId == selectedPoleId } ?: poles.firstOrNull()

    var isNightMode by remember { mutableStateOf(true) }
    var isMotionDetected by remember { mutableStateOf(false) }
    var isSirenMuted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = TechCyan, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VIRTUAL SMART POLE BENCH",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Interactive hardware peripheral and ESP-NOW mesh simulator",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // Node Selector Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(poles) { pole ->
                val isSelected = pole.poleId == selectedPoleId
                Surface(
                    color = if (isSelected) TechCyan else NavyCard,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) TechCyan else NavyBorder),
                    modifier = Modifier.clickable { selectedPoleId = pole.poleId }
                ) {
                    Text(
                        text = pole.poleId,
                        color = if (isSelected) Color.Black else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (currentPole != null) {
            // OLED Display Simulator (SSD1306 128x64 simulation)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = androidx.compose.foundation.BorderStroke(2.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SSD1306 OLED MATRIX DISPLAY SIMULATOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF04101A))
                            .border(1.dp, TechCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentPole.emergencyState == PoleState.ACTIVE) "🚨 EMERGENCY: CLEAR LANE 🚨"
                            else if (currentPole.emergencyState == PoleState.PREPARING) "⚠️ EMERGENCY APPROACHING ⚠️"
                            else "STANDBY — ECO LIGHT",
                            color = if (currentPole.emergencyState == PoleState.ACTIVE) EmergencyRed else TechCyan,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Actuators & Peripheral Indicators
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERIPHERAL STATE (${currentPole.poleId})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TechCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LED PWM Duty Cycle:", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "${currentPole.lightBrightness}% BRIGHTNESS",
                            color = if (currentPole.lightBrightness == 100) CorridorActiveGreen else TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Acoustic Buzzer Siren:", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = if (currentPole.emergencyState == PoleState.ACTIVE) "ALARM SOUNDING (85dB)" else "SILENT",
                            color = if (currentPole.emergencyState == PoleState.ACTIVE) EmergencyRed else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sensor Injection Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HARDWARE SENSOR CONTROLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isNightMode) Icons.Default.Nightlight else Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = if (isNightMode) TechCyan else CorridorPreparingAmber
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (isNightMode) "Ambient: Darkness (LDR Triggered)" else "Ambient: Daylight", color = TextPrimary)
                        }
                        Switch(
                            checked = isNightMode,
                            onCheckedChange = { isNightMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TechCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = CorridorActiveGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("PIR Motion Detector (Pedestrian)", color = TextPrimary)
                        }
                        Switch(
                            checked = isMotionDetected,
                            onCheckedChange = { isMotionDetected = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CorridorActiveGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "DUAL-BEAM VEHICLE SPEED SENSOR (Change 18)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Baseline: 15.0m • Sensor 1 & Sensor 2 pulse delta calculates velocity",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.simulateNewVehicle(isOverspeed = false)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant)
                        ) {
                            Text("🚗 Beam Pulse (Normal)", color = TextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.simulateNewVehicle(isOverspeed = true)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed.copy(alpha = 0.25f))
                        ) {
                            Text("🚨 Overspeed Pulse", color = EmergencyRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }


            // ESP32-CAM Obstacle Alert Injector Button
            Button(
                onClick = {
                    viewModel.injectObstacleAlert(
                        poleId = currentPole.poleId,
                        description = "ESP32-CAM detected unexpected road blockage near ${currentPole.name}"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("trigger_cam_alert_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CorridorPreparingAmber)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TRIGGER ESP32-CAM OBSTACLE ALERT", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            // ESP-NOW Mesh Packet Frame Monitor
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ESP-NOW ACTION FRAME INSPECTOR", style = MaterialTheme.typography.labelSmall, color = TechCyan)
                        Text("MAC: 24:6F:28:AB:12:88", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MAGIC: 0xEC | VER: 1 | SEQ: #4912 | TTL: 4\nTARGET: ${currentPole.poleId} | CMD: ${if (currentPole.emergencyState == PoleState.ACTIVE) "0x02 (CMD_ACTIVATE)" else "0x01 (CMD_PREPARE)"}\nCRC32: 0x9B42D18F (VERIFIED)",
                        color = CorridorActiveGreen,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
