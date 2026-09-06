package com.example.smartcorridor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcorridor.ui.screens.AdminDashboardScreen
import com.example.smartcorridor.ui.screens.EmergencyActiveCockpitScreen
import com.example.smartcorridor.ui.screens.EmergencyHistoryScreen
import com.example.smartcorridor.ui.screens.GpsStatusScreen
import com.example.smartcorridor.ui.screens.HardwareSimulatorScreen
import com.example.smartcorridor.ui.screens.LoginScreen
import com.example.smartcorridor.ui.screens.NormalTrafficScreen
import com.example.smartcorridor.ui.screens.RouteConfirmationScreen
import com.example.smartcorridor.ui.screens.SelectDestinationScreen
import com.example.smartcorridor.ui.screens.SelectRouteScreen
import com.example.smartcorridor.ui.screens.SmartCorridorPreviewScreen
import com.example.smartcorridor.ui.screens.StartEmergencyScreen
import com.example.smartcorridor.ui.screens.StreetlightClipScreen
import com.example.smartcorridor.ui.screens.VehicleDetailsScreen
import com.example.ui.theme.CorridorActiveGreen
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class DriverScreenStep {
    LOGIN,
    VEHICLE_DETAILS,
    GPS_STATUS,
    SELECT_DESTINATION,
    CORRIDOR_PREVIEW,
    SELECT_ROUTE,
    ROUTE_CONFIRMATION,
    START_EMERGENCY,
    EMERGENCY_ACTIVE,
    STREETLIGHT_CLIP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: CorridorViewModel) {
    var selectedTopNav by remember { mutableIntStateOf(0) } // 0: Driver, 1: Admin, 2: Simulator, 3: History
    var currentDriverStep by remember { mutableStateOf(DriverScreenStep.LOGIN) }

    val activeSession by viewModel.activeSession.collectAsState()

    // If emergency is active, keep driver flow on Cockpit
    val effectiveDriverStep = if (activeSession != null) DriverScreenStep.EMERGENCY_ACTIVE else currentDriverStep

    Scaffold(
        topBar = {
            Surface(
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EmergencyRed.copy(alpha = 0.2f))
                                    .border(1.5.dp, EmergencyRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Emergency,
                                    contentDescription = "Smart Corridor",
                                    tint = EmergencyRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "SMART STREETLIGHT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "IoT Streetlight Emergency Wave",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TechCyan
                                )
                            }
                        }

                        if (activeSession != null) {
                            Surface(
                                color = EmergencyRed,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "● LIVE WAVE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Mode Switcher Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTopNav,
                        containerColor = NavyDark,
                        contentColor = TechCyan,
                        edgePadding = 12.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTopNav]),
                                color = if (selectedTopNav == 0 && activeSession != null) EmergencyRed else TechCyan
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTopNav == 0,
                            onClick = { selectedTopNav = 0 },
                            text = { Text("🚑 Driver Cockpit") }
                        )
                        Tab(
                            selected = selectedTopNav == 1,
                            onClick = { selectedTopNav = 1 },
                            text = { Text("🎬 Streetlight Clip") }
                        )
                        Tab(
                            selected = selectedTopNav == 2,
                            onClick = { selectedTopNav = 2 },
                            text = { Text("🚦 Traffic Monitor") }
                        )
                        Tab(
                            selected = selectedTopNav == 3,
                            onClick = { selectedTopNav = 3 },
                            text = { Text("📊 Admin Center") }
                        )
                        Tab(
                            selected = selectedTopNav == 4,
                            onClick = { selectedTopNav = 4 },
                            text = { Text("🎮 Pole Simulator") }
                        )
                        Tab(
                            selected = selectedTopNav == 5,
                            onClick = { selectedTopNav = 5 },
                            text = { Text("📜 History") }
                        )
                    }
                }
            }
        },
        containerColor = NavyDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTopNav) {
                0 -> {
                    // Full Driver Workflow
                    when (effectiveDriverStep) {
                        DriverScreenStep.LOGIN -> {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { currentDriverStep = DriverScreenStep.VEHICLE_DETAILS }
                            )
                        }
                        DriverScreenStep.VEHICLE_DETAILS -> {
                            VehicleDetailsScreen(
                                viewModel = viewModel,
                                onProceedToGps = { currentDriverStep = DriverScreenStep.GPS_STATUS }
                            )
                        }
                        DriverScreenStep.GPS_STATUS -> {
                            GpsStatusScreen(
                                viewModel = viewModel,
                                onProceedToDestination = { currentDriverStep = DriverScreenStep.SELECT_DESTINATION }
                            )
                        }
                        DriverScreenStep.SELECT_DESTINATION -> {
                            SelectDestinationScreen(
                                viewModel = viewModel,
                                onProceedToRoutes = { currentDriverStep = DriverScreenStep.CORRIDOR_PREVIEW }
                            )
                        }
                        DriverScreenStep.CORRIDOR_PREVIEW -> {
                            SmartCorridorPreviewScreen(
                                viewModel = viewModel,
                                onContinueToRoutes = { currentDriverStep = DriverScreenStep.SELECT_ROUTE },
                                onBackToDestination = { currentDriverStep = DriverScreenStep.SELECT_DESTINATION }
                            )
                        }
                        DriverScreenStep.SELECT_ROUTE -> {
                            SelectRouteScreen(
                                viewModel = viewModel,
                                onProceedToConfirmation = { currentDriverStep = DriverScreenStep.ROUTE_CONFIRMATION }
                            )
                        }
                        DriverScreenStep.ROUTE_CONFIRMATION -> {
                            RouteConfirmationScreen(
                                viewModel = viewModel,
                                onProceedToStartEmergency = { currentDriverStep = DriverScreenStep.START_EMERGENCY }
                            )
                        }
                        DriverScreenStep.START_EMERGENCY -> {
                            StartEmergencyScreen(
                                viewModel = viewModel,
                                onEmergencyEngaged = { currentDriverStep = DriverScreenStep.EMERGENCY_ACTIVE }
                            )
                        }
                        DriverScreenStep.EMERGENCY_ACTIVE -> {
                            EmergencyActiveCockpitScreen(
                                viewModel = viewModel,
                                onEmergencyEnded = {
                                    currentDriverStep = DriverScreenStep.VEHICLE_DETAILS
                                    selectedTopNav = 5 // Auto-switch to history tab to show archived session!
                                }
                            )
                        }
                        DriverScreenStep.STREETLIGHT_CLIP -> {
                            StreetlightClipScreen(
                                viewModel = viewModel,
                                onBack = { currentDriverStep = DriverScreenStep.EMERGENCY_ACTIVE }
                            )
                        }
                    }
                }
                1 -> StreetlightClipScreen(viewModel = viewModel, onBack = { selectedTopNav = 0 })
                2 -> NormalTrafficScreen(viewModel = viewModel)
                3 -> AdminDashboardScreen(viewModel = viewModel)
                4 -> HardwareSimulatorScreen(viewModel = viewModel)
                5 -> EmergencyHistoryScreen(viewModel = viewModel)
            }
        }
    }

}
