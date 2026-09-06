package com.example.smartcorridor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcorridor.data.CorridorRepository
import com.example.smartcorridor.data.local.AppDatabase
import com.example.smartcorridor.data.local.EmergencySessionEntity
import com.example.smartcorridor.engine.CorridorEngine
import com.example.smartcorridor.engine.SimulationEngine
import com.example.smartcorridor.model.AlertSeverity
import com.example.smartcorridor.model.AlertType
import com.example.smartcorridor.model.CorridorStatus
import com.example.smartcorridor.model.Driver
import com.example.smartcorridor.model.EmergencyRoute
import com.example.smartcorridor.model.EmergencySession
import com.example.smartcorridor.model.GpsLocation
import com.example.smartcorridor.model.PoleState
import com.example.smartcorridor.model.RoadAlert
import com.example.smartcorridor.model.SmartPole
import com.example.smartcorridor.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppMode {
    DRIVER_FLOW,
    TRAFFIC_MONITOR,
    ADMIN_DASHBOARD,
    HARDWARE_SIMULATOR
}

class CorridorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = CorridorRepository(database.emergencySessionDao())
    val simulationEngine = SimulationEngine()

    // Mode Switcher
    private val _currentMode = MutableStateFlow(AppMode.DRIVER_FLOW)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    // Exposed Flows from Repository
    val currentDriver: StateFlow<Driver?> = repository.currentDriver
    val currentVehicle: StateFlow<Vehicle> = repository.currentVehicle
    val selectedRoute: StateFlow<EmergencyRoute> = repository.selectedRoute
    val poles: StateFlow<List<SmartPole>> = repository.poles
    val activeSession: StateFlow<EmergencySession?> = repository.activeSession
    val roadAlerts: StateFlow<List<RoadAlert>> = repository.roadAlerts
    val currentGps: StateFlow<GpsLocation> = repository.currentGps
    val corridorConfig: StateFlow<com.example.smartcorridor.model.CorridorConfiguration> = repository.corridorConfig
    val eventLogs: StateFlow<List<com.example.smartcorridor.model.CorridorEventLog>> = repository.eventLogs
    val normalVehicles: StateFlow<List<com.example.smartcorridor.model.NormalVehicle>> = repository.normalVehicles
    val isDemoMode: StateFlow<Boolean> = repository.isDemoMode

    val historySessions: StateFlow<List<EmergencySessionEntity>> = repository.historySessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Corridor Status
    private val _corridorStatus = MutableStateFlow(
        CorridorStatus(
            activePoleCount = 0,
            preparingPoleCount = 0,
            nextPoleId = null,
            nextPoleName = null,
            distanceToNextPoleKm = 0.0,
            activeCorridorPoles = emptyList(),
            upcomingPoles = emptyList(),
            passedPoles = emptyList(),
            statusMessage = "IDLE - READY"
        )
    )
    val corridorStatus: StateFlow<CorridorStatus> = _corridorStatus.asStateFlow()

    // Destination Arrival Geofence Flag
    private val _isDestinationReached = MutableStateFlow(false)
    val isDestinationReached: StateFlow<Boolean> = _isDestinationReached.asStateFlow()

    init {
        // Prepare default route simulation
        simulationEngine.setRoute(repository.selectedRoute.value)
    }

    fun setAppMode(mode: AppMode) {
        _currentMode.value = mode
    }

    fun loginDriver(badgeOrId: String): Boolean {
        val driver = repository.authenticateDriver(badgeOrId)
        return driver != null
    }

    fun selectVehicle(vehicle: Vehicle) {
        repository.selectVehicle(vehicle)
        simulationEngine.setRoute(repository.selectedRoute.value)
    }

    fun getAvailableRoutes(): List<EmergencyRoute> {
        return repository.getRoutesForVehicle(currentVehicle.value.type)
    }

    fun selectRoute(route: EmergencyRoute) {
        repository.selectRoute(route)
        simulationEngine.setRoute(route)
        _isDestinationReached.value = false

        // Position vehicle at Source initially
        if (route.waypoints.isNotEmpty()) {
            val origin = route.waypoints.first()
            val originGps = GpsLocation(
                latitude = origin.latitude,
                longitude = origin.longitude,
                speedKmh = 0.0f,
                heading = 45.0f,
                accuracyMeters = 2.0f,
                isMock = true
            )
            repository.updateGpsLocation(originGps)
        }
    }

    fun startEmergency(): EmergencySession {
        val driver = currentDriver.value ?: repository.authenticateDriver("DRIVER001")!!
        val vehicle = currentVehicle.value
        val route = selectedRoute.value

        val session = repository.startEmergency(route, vehicle, driver)
        _isDestinationReached.value = false

        // Initialize vehicle position at route origin / SOURCE
        val sourceGps = if (route.waypoints.isNotEmpty()) {
            val origin = route.waypoints.first()
            GpsLocation(
                latitude = origin.latitude,
                longitude = origin.longitude,
                speedKmh = 0.0f,
                heading = 45.0f,
                accuracyMeters = 2.0f,
                isMock = true
            )
        } else {
            currentGps.value
        }
        repository.updateGpsLocation(sourceGps)

        // Reset simulation to start at SOURCE
        simulationEngine.setRoute(route)

        // Log Source-Based 2 KM corridor activation
        repository.addCorridorEvent(
            poleId = "SOURCE",
            state = PoleState.ACTIVE,
            message = "Emergency Corridor ACTIVATED from ${route.originName} • 2.0 KM advance wave calculated immediately"
        )

        // Run initial corridor evaluation at SOURCE (before vehicle starts rolling)
        evaluateCorridor(sourceGps)

        // Automatically start simulation engine from SOURCE
        simulationEngine.startSimulation(viewModelScope) { loc ->
            repository.updateGpsLocation(loc)
            evaluateCorridor(loc)
        }

        return session
    }

    private fun evaluateCorridor(loc: GpsLocation) {
        val route = selectedRoute.value
        val currentPoles = poles.value
        val currentCfg = repository.corridorConfig.value

        val result = CorridorEngine.evaluateCorridor(
            vehicleLocation = loc,
            route = route,
            allPoles = currentPoles,
            activePriority = currentVehicle.value.priorityLevel,
            config = currentCfg
        )

        repository.updatePoles(result.updatedPoles)
        repository.addCorridorEvents(result.generatedEvents)

        _corridorStatus.value = CorridorStatus(
            activePoleCount = result.activePoles.size,
            preparingPoleCount = result.preparingPoles.size,
            nextPoleId = result.nextPole?.poleId,
            nextPoleName = result.nextPole?.name,
            distanceToNextPoleKm = result.distanceToNextPoleKm,
            activeCorridorPoles = result.activePoles,
            upcomingPoles = result.preparingPoles,
            passedPoles = result.passedPoles,
            statusMessage = if (result.activePoles.isNotEmpty()) "2.0 KM CORRIDOR ACTIVE" else "CRUISING",
            vehicleStationMeters = result.vehicleStationMeters,
            vehicleStationText = result.vehicleStationText,
            sourceName = route.originName,
            destinationName = route.destinationName,
            corridorLookaheadMeters = 2000.0,
            poleDistancesAhead = result.poleDistancesAhead,
            poleRouteDistances = result.poleRouteDistances
        )

        // Check geofence
        if (activeSession.value != null && CorridorEngine.isDestinationReached(loc, route)) {
            _isDestinationReached.value = true
            simulationEngine.pauseSimulation()
            repository.addCorridorEvent(
                poleId = "DEST",
                state = PoleState.PASSED,
                message = "Vehicle arrived at destination (${route.destinationName})"
            )
        }
    }

    fun endEmergency(reason: String = "COMPLETED") {
        viewModelScope.launch {
            simulationEngine.resetSimulation()
            _isDestinationReached.value = false
            repository.endEmergency(reason)
            _corridorStatus.value = _corridorStatus.value.copy(
                activePoleCount = 0,
                preparingPoleCount = 0,
                statusMessage = "COMPLETED"
            )
        }
    }

    fun stepSimulationManual() {
        simulationEngine.stepForward { loc ->
            repository.updateGpsLocation(loc)
            evaluateCorridor(loc)
        }
    }

    fun toggleSimulationPlayPause() {
        if (simulationEngine.isSimulating.value) {
            simulationEngine.pauseSimulation()
        } else {
            simulationEngine.startSimulation(viewModelScope) { loc ->
                repository.updateGpsLocation(loc)
                evaluateCorridor(loc)
            }
        }
    }

    // Config & Admin Controls (Change 20)
    fun updateCorridorConfig(newConfig: com.example.smartcorridor.model.CorridorConfiguration) {
        repository.updateCorridorConfig(newConfig)
    }

    fun toggleDemoMode() {
        repository.toggleDemoMode()
    }

    fun clearEventLogs() {
        repository.clearEventLogs()
    }

    // Traffic Monitoring & Simulation Controls (Change 10, 11, 21)
    fun simulateNewVehicle(isOverspeed: Boolean = false) {
        repository.simulateNewVehicleDetection(isOverspeed)
    }

    fun simulateOverspeedBurst() {
        repeat(2) {
            repository.simulateNewVehicleDetection(isOverspeed = true)
        }
    }

    fun simulateTrafficCongestion() {
        repeat(5) {
            repository.simulateNewVehicleDetection(isOverspeed = false)
        }
    }

    fun injectObstacleAlert(poleId: String = "P02", description: String = "Stationary vehicle blocking emergency lane near Pole P02") {
        val targetPole = poles.value.find { it.poleId == poleId }
        val alert = RoadAlert(
            id = "ALT-${System.currentTimeMillis() % 10000}",
            title = "⚠️ Lane Blockage Detected",
            type = AlertType.OBSTACLE,
            severity = AlertSeverity.HIGH,
            latitude = targetPole?.latitude ?: 12.9724,
            longitude = targetPole?.longitude ?: 77.5958,
            poleId = poleId,
            description = description
        )
        repository.addRoadAlert(alert)
        repository.addCorridorEvent(
            poleId = poleId,
            state = PoleState.FAULT,
            message = "Obstacle detected at $poleId • Lane blocked"
        )
    }

    fun dismissAlert(alertId: String) {
        repository.dismissRoadAlert(alertId)
    }

    fun setPoleStateManual(poleId: String, state: PoleState) {
        repository.updatePoleState(poleId, state)
    }

    fun resetCorridorConfigToDefaults() {
        repository.updateCorridorConfig(com.example.smartcorridor.model.CorridorConfiguration())
    }
}


