package com.example.smartcorridor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcorridor.model.Driver
import com.example.smartcorridor.model.EmergencyRoute
import com.example.smartcorridor.model.Vehicle
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

/**
 * Screen 1: Driver Login
 */
@Composable
fun LoginScreen(
    viewModel: CorridorViewModel,
    onLoginSuccess: () -> Unit
) {
    var driverIdInput by remember { mutableStateOf("DRIVER001") }
    var passwordInput by remember { mutableStateOf("emergency2026") }
    var loginError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo & Shield Badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(EmergencyRed.copy(alpha = 0.15f))
                .border(2.dp, EmergencyRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Emergency,
                contentDescription = "Emergency Corridor Logo",
                tint = EmergencyRed,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SMART EMERGENCY CORRIDOR",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            ),
            color = TextPrimary
        )

        Text(
            text = "Driver Authentication & Mission Dispatch",
            style = MaterialTheme.typography.bodySmall,
            color = TechCyan,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "OFFICIAL DRIVER CREDENTIALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = driverIdInput,
                    onValueChange = { driverIdInput = it },
                    label = { Text("Driver ID or Badge Number") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TechCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_id_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Security Passkey") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TechCyan) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                if (loginError != null) {
                    Text(
                        text = loginError!!,
                        color = EmergencyRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val ok = viewModel.loginDriver(driverIdInput)
                        if (ok) {
                            loginError = null
                            onLoginSuccess()
                        } else {
                            loginError = "Authorization denied: Driver ID not authorized."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "AUTHORIZE & PROCEED",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Demo Credentials: DRIVER001 (Officer Chen) / DRIVER002 (Paramedic Ray)",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

/**
 * Screen 2: Vehicle Details
 */
@Composable
fun VehicleDetailsScreen(
    viewModel: CorridorViewModel,
    onProceedToGps: () -> Unit
) {
    val currentDriver by viewModel.currentDriver.collectAsState()
    val currentVehicle by viewModel.currentVehicle.collectAsState()

    val vehicleOptions = listOf(
        Vehicle("KA-XX-1234", VehicleType.AMBULANCE, "KA-01-EQ-1234", "DRIVER001", 1, "AVAILABLE"),
        Vehicle("KA-XX-9999", VehicleType.FIRE_TRUCK, "KA-01-FT-9999", "DRIVER002", 1, "AVAILABLE"),
        Vehicle("KA-XX-5555", VehicleType.POLICE, "KA-01-PD-5555", "ADMIN001", 2, "AVAILABLE")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(20.dp)
    ) {
        Text(
            text = "ASSIGNED VEHICLE & DRIVER",
            style = MaterialTheme.typography.titleMedium,
            color = TechCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Confirm vehicle telematics unit prior to emergency dispatch",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Driver Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TechCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TechCyan)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = currentDriver?.name ?: "Officer Sarah Chen",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Badge: ${currentDriver?.badgeNumber ?: "MED-8842"} • Authorized Operator",
                        style = MaterialTheme.typography.bodySmall,
                        color = CorridorActiveGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SELECT ACTIVE FLEET VEHICLE",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        vehicleOptions.forEach { veh ->
            val isSelected = veh.id == currentVehicle.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.selectVehicle(veh) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NavyCard.copy(alpha = 0.9f) else NavySurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) TechCyan else NavyBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmergencyRed else NavyBorder.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "${veh.type.displayName} (${veh.id})",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reg: ${veh.registrationNumber} • Priority: Level ${veh.priorityLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = CorridorActiveGreen)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onProceedToGps,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("verify_gps_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "VERIFY GPS STATUS",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Color.Black)
        }
    }
}

/**
 * Screen 3: GPS Status Screen
 */
@Composable
fun GpsStatusScreen(
    viewModel: CorridorViewModel,
    onProceedToDestination: () -> Unit
) {
    val gps by viewModel.currentGps.collectAsState()
    var isSimulationMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(20.dp)
    ) {
        Text(
            text = "GPS LOCK & TELEMETRY VERIFICATION",
            style = MaterialTheme.typography.titleMedium,
            color = TechCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ensuring high-precision satellite accuracy for 2 KM corridor tracking",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Satellite Lock Indicator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CorridorActiveGreen.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "HIGH-PRECISION LOCK READY",
                            style = MaterialTheme.typography.labelMedium,
                            color = CorridorActiveGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "8 SATELLITES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "ACCURACY", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "±${gps.accuracyMeters} m",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Text(text = "CURRENT SPEED", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "${gps.speedKmh.toInt()} km/h",
                            style = MaterialTheme.typography.titleLarge,
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Text(text = "HEADING", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "${gps.heading.toInt()}° NE",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LAT: ${"%.5f".format(gps.latitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LNG: ${"%.5f".format(gps.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Simulation vs Hardware Switcher Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GPS SOURCE SELECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { isSimulationMode = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSimulationMode) NavyCard else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSimulationMode) 2.dp else 1.dp,
                            color = if (isSimulationMode) TechCyan else NavyBorder
                        )
                    ) {
                        Text(
                            text = "Test Simulator",
                            color = if (isSimulationMode) TechCyan else TextMuted
                        )
                    }

                    OutlinedButton(
                        onClick = { isSimulationMode = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isSimulationMode) NavyCard else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (!isSimulationMode) 2.dp else 1.dp,
                            color = if (!isSimulationMode) TechCyan else NavyBorder
                        )
                    ) {
                        Text(
                            text = "Hardware GPS",
                            color = if (!isSimulationMode) TechCyan else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onProceedToDestination,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("proceed_destination_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "SELECT DESTINATION",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

/**
 * Screen 4: Select Destination (Dynamic destinations for Ambulance, Fire Truck, Police, Rescue)
 */
data class EmergencyDestinationItem(
    val name: String,
    val description: String,
    val distanceKm: Double,
    val categoryBadge: String,
    val routeId: String
)

@Composable
fun SelectDestinationScreen(
    viewModel: CorridorViewModel,
    onProceedToRoutes: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val currentVehicle by viewModel.currentVehicle.collectAsState()

    val (categoryTitle, categorySubtitle, destinationList) = when (currentVehicle.type) {
        com.example.smartcorridor.model.VehicleType.AMBULANCE -> Triple(
            "DESIGNATED EMERGENCY HOSPITALS",
            "Emergency medical facility destination determines corridor path",
            listOf(
                EmergencyDestinationItem(
                    name = "City Hospital Trauma Center",
                    description = "Level 1 Trauma Center & Critical Care Bay • MG Road",
                    distanceKm = 6.2,
                    categoryBadge = "TRAUMA L1",
                    routeId = "R01"
                ),
                EmergencyDestinationItem(
                    name = "District General Hospital",
                    description = "General Emergency, ICU & Surgical Wing • Victoria Bypass",
                    distanceKm = 7.5,
                    categoryBadge = "ICU WING",
                    routeId = "R02"
                ),
                EmergencyDestinationItem(
                    name = "Apollo Multispecialty Emergency",
                    description = "Tertiary Emergency & Cardiac Cath Lab • Ring Road",
                    distanceKm = 9.1,
                    categoryBadge = "CARDIAC",
                    routeId = "R03"
                )
            )
        )
        com.example.smartcorridor.model.VehicleType.FIRE_TRUCK -> Triple(
            "ACTIVE FIRE INCIDENT & HAZMAT TARGET SITES",
            "Active fire emergency incident site determines corridor path",
            listOf(
                EmergencyDestinationItem(
                    name = "Sector 4 Industrial Chemical Fire",
                    description = "Major Hazmat Chemical Fire & Evacuation Zone • MG Road",
                    distanceKm = 6.2,
                    categoryBadge = "HAZMAT RED",
                    routeId = "F01"
                ),
                EmergencyDestinationItem(
                    name = "Metro Commercial Complex Blaze",
                    description = "High-Rise 5-Alarm Structural Fire • Victoria Bypass",
                    distanceKm = 7.5,
                    categoryBadge = "5-ALARM",
                    routeId = "F02"
                ),
                EmergencyDestinationItem(
                    name = "Central Petroleum Depot Incident",
                    description = "Hydrocarbon Tank Flare-up & Foam Grid Grid • Ring Road",
                    distanceKm = 9.1,
                    categoryBadge = "TANK CRISIS",
                    routeId = "F03"
                )
            )
        )
        com.example.smartcorridor.model.VehicleType.POLICE -> Triple(
            "HIGH-PRIORITY POLICE INCIDENT & TACTICAL SITES",
            "Tactical scene & rapid pursuit intercept determines corridor path",
            listOf(
                EmergencyDestinationItem(
                    name = "Downtown Central Bank Incident",
                    description = "Code Red Armed Robbery & Perimeter Lockdown • MG Road",
                    distanceKm = 6.2,
                    categoryBadge = "CODE RED",
                    routeId = "P01"
                ),
                EmergencyDestinationItem(
                    name = "Metro High-Security Transit Hub",
                    description = "VIP Convoy Rapid Clearance & Tactical Response • Victoria Bypass",
                    distanceKm = 7.5,
                    categoryBadge = "TACTICAL",
                    routeId = "P02"
                ),
                EmergencyDestinationItem(
                    name = "Highway Checkpost 7 Intercept",
                    description = "High-Speed Intercept & Fast-Track Barricade • Ring Road",
                    distanceKm = 9.1,
                    categoryBadge = "PURSUIT",
                    routeId = "P03"
                )
            )
        )
        com.example.smartcorridor.model.VehicleType.RESCUE -> Triple(
            "DISASTER RESCUE & RAPID INTERVENTION SITES",
            "Search and rescue disaster incident determines corridor path",
            listOf(
                EmergencyDestinationItem(
                    name = "Metro Flyover Collapse Site",
                    description = "Search & Heavy Rescue Urban Disaster Zone • MG Road",
                    distanceKm = 6.2,
                    categoryBadge = "SEARCH/RESCUE",
                    routeId = "U01"
                ),
                EmergencyDestinationItem(
                    name = "Underground Tunnel Flood Breach",
                    description = "Subterranean Drainage & Rapid Extraction Site • Victoria Bypass",
                    distanceKm = 7.5,
                    categoryBadge = "FLOOD BREACH",
                    routeId = "U02"
                ),
                EmergencyDestinationItem(
                    name = "Suburban Landslide Emergency",
                    description = "Disaster Response & Slope Evacuation Grid • Ring Road",
                    distanceKm = 9.1,
                    categoryBadge = "DISASTER",
                    routeId = "U03"
                )
            )
        )
    }

    val filteredDestinations = if (searchQuery.isBlank()) {
        destinationList
    } else {
        destinationList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(20.dp)
    ) {
        // Vehicle Dispatch Badge
        Surface(
            color = when (currentVehicle.type) {
                com.example.smartcorridor.model.VehicleType.FIRE_TRUCK -> EmergencyRed.copy(alpha = 0.18f)
                com.example.smartcorridor.model.VehicleType.POLICE -> Color(0xFF1E3A8A).copy(alpha = 0.35f)
                com.example.smartcorridor.model.VehicleType.AMBULANCE -> CorridorActiveGreen.copy(alpha = 0.18f)
                else -> TechCyan.copy(alpha = 0.18f)
            },
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                when (currentVehicle.type) {
                    com.example.smartcorridor.model.VehicleType.FIRE_TRUCK -> EmergencyRed
                    com.example.smartcorridor.model.VehicleType.POLICE -> TechCyan
                    com.example.smartcorridor.model.VehicleType.AMBULANCE -> CorridorActiveGreen
                    else -> TechCyan
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (currentVehicle.type) {
                        com.example.smartcorridor.model.VehicleType.FIRE_TRUCK -> Icons.Default.LocalFireDepartment
                        com.example.smartcorridor.model.VehicleType.POLICE -> Icons.Default.Security
                        com.example.smartcorridor.model.VehicleType.AMBULANCE -> Icons.Default.LocalHospital
                        else -> Icons.Default.Emergency
                    },
                    contentDescription = null,
                    tint = when (currentVehicle.type) {
                        com.example.smartcorridor.model.VehicleType.FIRE_TRUCK -> EmergencyRed
                        com.example.smartcorridor.model.VehicleType.POLICE -> TechCyan
                        com.example.smartcorridor.model.VehicleType.AMBULANCE -> CorridorActiveGreen
                        else -> TechCyan
                    },
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DISPATCHING AS: ${currentVehicle.type.displayName.uppercase()} [${currentVehicle.registrationNumber}]",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "SELECT EMERGENCY DESTINATION",
            style = MaterialTheme.typography.titleMedium,
            color = TechCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = categorySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Destination or Site...") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TechCyan) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("destination_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TechCyan,
                unfocusedBorderColor = NavyBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = categoryTitle,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        filteredDestinations.forEach { destItem ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        val routes = viewModel.getAvailableRoutes()
                        val route = routes.find { it.id == destItem.routeId }
                            ?: routes.find { it.destinationName.contains(destItem.name.split(" ")[0]) }
                            ?: routes.firstOrNull()
                            ?: viewModel.selectedRoute.value
                        viewModel.selectRoute(route)
                        onProceedToRoutes()
                    },
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = destItem.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = destItem.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${destItem.distanceKm} km",
                            style = MaterialTheme.typography.titleMedium,
                            color = TechCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = CorridorActiveGreen.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = destItem.categoryBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = CorridorActiveGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 5: Select Route Screen
 */
@Composable
fun SelectRouteScreen(
    viewModel: CorridorViewModel,
    onProceedToConfirmation: () -> Unit
) {
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val availableRoutes = viewModel.repository.availableRoutes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(20.dp)
    ) {
        Text(
            text = "SELECT CORRIDOR ROUTE",
            style = MaterialTheme.typography.titleMedium,
            color = TechCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose optimal smart streetlight corridor with active wave",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        availableRoutes.forEach { route ->
            val isSelected = route.id == selectedRoute.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { viewModel.selectRoute(route) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NavyCard else NavySurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) CorridorActiveGreen else NavyBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = route.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        if (route.isRecommended) {
                            Surface(
                                color = CorridorActiveGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "FASTEST CORRIDOR",
                                    color = CorridorActiveGreen,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Distance: ${route.distanceKm} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "ETA: ${route.estimatedDurationMin} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = TechCyan
                        )
                        Text(
                            text = "${route.poleIds.size} Smart Poles",
                            style = MaterialTheme.typography.bodySmall,
                            color = CorridorPreparingAmber
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onProceedToConfirmation,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_route_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "CONFIRM ROUTE SUMMARY",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
        }
    }
}

/**
 * Screen 6: Route Confirmation Screen
 */
@Composable
fun RouteConfirmationScreen(
    viewModel: CorridorViewModel,
    onProceedToStartEmergency: () -> Unit
) {
    val currentDriver by viewModel.currentDriver.collectAsState()
    val currentVehicle by viewModel.currentVehicle.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(20.dp)
    ) {
        Text(
            text = "MISSION MANIFEST CONFIRMATION",
            style = MaterialTheme.typography.titleMedium,
            color = TechCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review emergency parameters prior to 2 KM corridor activation",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ManifestRow(label = "Vehicle", value = "${currentVehicle.type.displayName} (${currentVehicle.id})")
                ManifestRow(label = "Registration", value = currentVehicle.registrationNumber)
                ManifestRow(label = "Driver", value = currentDriver?.name ?: "Sarah Chen")
                ManifestRow(label = "Destination", value = selectedRoute.destinationName)
                ManifestRow(label = "Route", value = selectedRoute.name)
                ManifestRow(label = "Distance", value = "${selectedRoute.distanceKm} km")
                ManifestRow(label = "Estimated Time", value = "${selectedRoute.estimatedDurationMin} min")
                ManifestRow(label = "Smart Poles to Lock", value = "${selectedRoute.poleIds.size} Poles (${selectedRoute.poleIds.joinToString()})")
                ManifestRow(label = "Priority Level", value = "Priority 1 (Critical Emergency)")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onProceedToStartEmergency,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("proceed_start_emergency_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "CONTINUE TO EMERGENCY ACTIVATION",
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Emergency, contentDescription = null)
        }
    }
}

@Composable
fun ManifestRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

/**
 * Screen 7: Start Emergency Screen
 */
@Composable
fun StartEmergencyScreen(
    viewModel: CorridorViewModel,
    onEmergencyEngaged: () -> Unit
) {
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val currentVehicle by viewModel.currentVehicle.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-Vis Emergency Warning Badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(EmergencyRed.copy(alpha = 0.2f))
                .border(3.dp, EmergencyRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = EmergencyRed,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ACTIVATE 2 KM CORRIDOR WAVE?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = TextPrimary
        )

        Text(
            text = "Engaging emergency will immediately force 100% LED brightness and visual alert sirens along the next 2 KM of streetlights.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.startEmergency()
                onEmergencyEngaged()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("start_emergency_action_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.FlashOn,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "START EMERGENCY CORRIDOR",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}
