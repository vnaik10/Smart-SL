package com.example.smartcorridor.data

import com.example.smartcorridor.data.local.EmergencySessionDao
import com.example.smartcorridor.data.local.EmergencySessionEntity
import com.example.smartcorridor.model.AlertSeverity
import com.example.smartcorridor.model.AlertType
import com.example.smartcorridor.model.CorridorStatus
import com.example.smartcorridor.model.CorridorConfiguration
import com.example.smartcorridor.model.CorridorEventLog
import com.example.smartcorridor.model.Driver
import com.example.smartcorridor.model.EmergencyRoute
import com.example.smartcorridor.model.EmergencySession
import com.example.smartcorridor.model.EmergencyStatus
import com.example.smartcorridor.model.GeoPoint
import com.example.smartcorridor.model.GpsLocation
import com.example.smartcorridor.model.PoleState
import com.example.smartcorridor.model.RoadAlert
import com.example.smartcorridor.model.SmartPole
import com.example.smartcorridor.model.UserRole
import com.example.smartcorridor.model.Vehicle
import com.example.smartcorridor.model.VehicleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CorridorRepository(private val sessionDao: EmergencySessionDao) {

    // Pre-seeded Master Drivers
    private val masterDrivers = listOf(
        Driver("DRIVER001", "Officer Sarah Chen", "MED-8842", "+1-555-0192", true, "KA-XX-1234", UserRole.DRIVER),
        Driver("DRIVER002", "Paramedic Marcus Ray", "FIRE-109", "+1-555-0144", true, "KA-XX-9999", UserRole.DRIVER),
        Driver("ADMIN001", "Chief Controller Dave Vance", "ADM-01", "+1-555-0100", true, "KA-XX-1234", UserRole.ADMIN)
    )

    // Pre-seeded Master Vehicles
    private val masterVehicles = listOf(
        Vehicle("KA-XX-1234", VehicleType.AMBULANCE, "KA-01-EQ-1234", "DRIVER001", 1, "AVAILABLE"),
        Vehicle("KA-XX-9999", VehicleType.FIRE_TRUCK, "KA-01-FT-9999", "DRIVER002", 1, "AVAILABLE"),
        Vehicle("KA-XX-5555", VehicleType.POLICE, "KA-01-PD-5555", "ADMIN001", 2, "AVAILABLE"),
        Vehicle("KA-XX-3333", VehicleType.RESCUE, "KA-01-RS-3333", "DRIVER001", 1, "AVAILABLE")
    )

    // Pre-seeded Smart Poles along Metropolitan Highway Corridors
    private val initialPoles = listOf(
        SmartPole("P01", "P01 - Station Road", 12.9702, 77.5925, "SEG_01", listOf("R01", "R02"), 1, batteryLevel = 94, emergencyState = PoleState.NORMAL, vehiclesDetected = 14, averageSpeedKmh = 42.0, maxSpeedKmh = 58.0, overspeedCount = 1, trafficLevel = "MODERATE"),
        SmartPole("P02", "P02 - MG Road Junction", 12.9724, 77.5958, "SEG_02", listOf("R01", "R02", "R03"), 2, batteryLevel = 88, emergencyState = PoleState.NORMAL, vehiclesDetected = 26, averageSpeedKmh = 47.5, maxSpeedKmh = 72.0, overspeedCount = 4, trafficLevel = "CONGESTED"),
        SmartPole("P03", "P03 - Victoria Flyover", 12.9750, 77.5995, "SEG_03", listOf("R01", "R02"), 3, batteryLevel = 92, emergencyState = PoleState.NORMAL, vehiclesDetected = 18, averageSpeedKmh = 49.0, maxSpeedKmh = 64.0, overspeedCount = 2, trafficLevel = "MODERATE"),
        SmartPole("P04", "P04 - Trinity Circle", 12.9785, 77.6030, "SEG_04", listOf("R01", "R03"), 4, batteryLevel = 85, emergencyState = PoleState.NORMAL, vehiclesDetected = 22, averageSpeedKmh = 41.0, maxSpeedKmh = 54.0, overspeedCount = 1, trafficLevel = "MODERATE"),
        SmartPole("P05", "P05 - Hospital Approach", 12.9815, 77.6065, "SEG_05", listOf("R01"), 5, batteryLevel = 90, emergencyState = PoleState.NORMAL, vehiclesDetected = 9, averageSpeedKmh = 38.0, maxSpeedKmh = 46.0, overspeedCount = 0, trafficLevel = "LOW"),
        SmartPole("P06", "P06 - Trauma Center Gate", 12.9845, 77.6095, "SEG_06", listOf("R01"), 6, batteryLevel = 96, emergencyState = PoleState.NORMAL, vehiclesDetected = 5, averageSpeedKmh = 28.0, maxSpeedKmh = 35.0, overspeedCount = 0, trafficLevel = "LOW"),
        SmartPole("P07", "P07 - Ring Road North", 12.9740, 77.6050, "SEG_07", listOf("R02", "R03"), 7, batteryLevel = 82, emergencyState = PoleState.NORMAL, vehiclesDetected = 31, averageSpeedKmh = 52.0, maxSpeedKmh = 78.0, overspeedCount = 6, trafficLevel = "CONGESTED"),
        SmartPole("P08", "P08 - East Corridor Bypass", 12.9790, 77.6110, "SEG_08", listOf("R02", "R03"), 8, batteryLevel = 89, emergencyState = PoleState.NORMAL, vehiclesDetected = 15, averageSpeedKmh = 46.0, maxSpeedKmh = 62.0, overspeedCount = 2, trafficLevel = "MODERATE")
    )


    // Pre-seeded Pre-calculated Routes by Vehicle Type
    val ambulanceRoutes = listOf(
        EmergencyRoute(
            id = "R01",
            name = "R01: City Hospital Trauma Center Express",
            originName = "Central Depot",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "City Hospital Trauma Center",
            destLat = 12.9850,
            destLng = 77.6100,
            distanceKm = 6.2,
            estimatedDurationMin = 8.0,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9691, 77.5912),
                GeoPoint(12.9702, 77.5925), // P1
                GeoPoint(12.9713, 77.5940),
                GeoPoint(12.9724, 77.5958), // P2
                GeoPoint(12.9737, 77.5976),
                GeoPoint(12.9750, 77.5995), // P3
                GeoPoint(12.9768, 77.6012),
                GeoPoint(12.9785, 77.6030), // P4
                GeoPoint(12.9800, 77.6048),
                GeoPoint(12.9815, 77.6065), // P5
                GeoPoint(12.9830, 77.6080),
                GeoPoint(12.9845, 77.6095), // P6
                GeoPoint(12.9850, 77.6100)  // Destination
            ),
            poleIds = listOf("P01", "P02", "P03", "P04", "P05", "P06"),
            isRecommended = true
        ),
        EmergencyRoute(
            id = "R02",
            name = "R02: District General Hospital Via MG Road",
            originName = "Central Depot",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "District General Hospital",
            destLat = 12.9880,
            destLng = 77.6150,
            distanceKm = 7.5,
            estimatedDurationMin = 11.0,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9702, 77.5925),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9740, 77.6050),
                GeoPoint(12.9790, 77.6110),
                GeoPoint(12.9880, 77.6150)
            ),
            poleIds = listOf("P01", "P02", "P07", "P08"),
            isRecommended = false
        ),
        EmergencyRoute(
            id = "R03",
            name = "R03: Apollo Multispecialty Via Ring Road",
            originName = "Central Depot",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Apollo Multispecialty Emergency",
            destLat = 12.9920,
            destLng = 77.6200,
            distanceKm = 9.1,
            estimatedDurationMin = 14.0,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9785, 77.6030),
                GeoPoint(12.9740, 77.6050),
                GeoPoint(12.9920, 77.6200)
            ),
            poleIds = listOf("P02", "P04", "P07", "P08"),
            isRecommended = false
        )
    )

    val fireTruckRoutes = listOf(
        EmergencyRoute(
            id = "F01",
            name = "F01: Sector 4 Chemical Fire Hazmat Express",
            originName = "Central Fire Station 1",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Sector 4 Industrial Chemical Fire",
            destLat = 12.9850,
            destLng = 77.6100,
            distanceKm = 6.2,
            estimatedDurationMin = 7.5,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9691, 77.5912),
                GeoPoint(12.9702, 77.5925),
                GeoPoint(12.9713, 77.5940),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9737, 77.5976),
                GeoPoint(12.9750, 77.5995),
                GeoPoint(12.9768, 77.6012),
                GeoPoint(12.9785, 77.6030),
                GeoPoint(12.9800, 77.6048),
                GeoPoint(12.9815, 77.6065),
                GeoPoint(12.9830, 77.6080),
                GeoPoint(12.9845, 77.6095),
                GeoPoint(12.9850, 77.6100)
            ),
            poleIds = listOf("P01", "P02", "P03", "P04", "P05", "P06"),
            isRecommended = true
        ),
        EmergencyRoute(
            id = "F02",
            name = "F02: Commercial Complex 5-Alarm Structural Fire",
            originName = "Central Fire Station 1",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Metro Commercial Complex Blaze",
            destLat = 12.9880,
            destLng = 77.6150,
            distanceKm = 7.5,
            estimatedDurationMin = 10.0,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9702, 77.5925),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9740, 77.6050),
                GeoPoint(12.9790, 77.6110),
                GeoPoint(12.9880, 77.6150)
            ),
            poleIds = listOf("P01", "P02", "P07", "P08"),
            isRecommended = false
        ),
        EmergencyRoute(
            id = "F03",
            name = "F03: Petroleum Depot Hydrocarbon Flare-up",
            originName = "Central Fire Station 1",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Central Petroleum Depot Incident",
            destLat = 12.9920,
            destLng = 77.6200,
            distanceKm = 9.1,
            estimatedDurationMin = 13.0,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9785, 77.6030),
                GeoPoint(12.9740, 77.6050),
                GeoPoint(12.9920, 77.6200)
            ),
            poleIds = listOf("P02", "P04", "P07", "P08"),
            isRecommended = false
        )
    )

    val policeRoutes = listOf(
        EmergencyRoute(
            id = "P01",
            name = "P01: Central Bank Armed Alarm Response",
            originName = "Police Headquarters",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Downtown Central Bank Incident",
            destLat = 12.9850,
            destLng = 77.6100,
            distanceKm = 6.2,
            estimatedDurationMin = 6.5,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9691, 77.5912),
                GeoPoint(12.9702, 77.5925),
                GeoPoint(12.9713, 77.5940),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9737, 77.5976),
                GeoPoint(12.9750, 77.5995),
                GeoPoint(12.9768, 77.6012),
                GeoPoint(12.9785, 77.6030),
                GeoPoint(12.9800, 77.6048),
                GeoPoint(12.9815, 77.6065),
                GeoPoint(12.9830, 77.6080),
                GeoPoint(12.9845, 77.6095),
                GeoPoint(12.9850, 77.6100)
            ),
            poleIds = listOf("P01", "P02", "P03", "P04", "P05", "P06"),
            isRecommended = true
        ),
        EmergencyRoute(
            id = "P02",
            name = "P02: Metro Transit VIP Convoy Clearance",
            originName = "Police Headquarters",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Metro High-Security Transit Hub",
            destLat = 12.9880,
            destLng = 77.6150,
            distanceKm = 7.5,
            estimatedDurationMin = 9.0,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9702, 77.5925),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9740, 77.6050),
                GeoPoint(12.9790, 77.6110),
                GeoPoint(12.9880, 77.6150)
            ),
            poleIds = listOf("P01", "P02", "P07", "P08"),
            isRecommended = false
        ),
        EmergencyRoute(
            id = "P03",
            name = "P03: Highway 7 Rapid Intercept & Barricade",
            originName = "Police Headquarters",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Highway Checkpost 7 Intercept",
            destLat = 12.9920,
            destLng = 77.6200,
            distanceKm = 9.1,
            estimatedDurationMin = 11.5,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9785, 77.6030),
                GeoPoint(12.9740, 77.6050),
                GeoPoint(12.9920, 77.6200)
            ),
            poleIds = listOf("P02", "P04", "P07", "P08"),
            isRecommended = false
        )
    )

    val rescueRoutes = listOf(
        EmergencyRoute(
            id = "U01",
            name = "U01: Flyover Structural Collapse Heavy Rescue",
            originName = "Disaster Response Base",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Metro Flyover Collapse Site",
            destLat = 12.9850,
            destLng = 77.6100,
            distanceKm = 6.2,
            estimatedDurationMin = 8.5,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9702, 77.5925),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9750, 77.5995),
                GeoPoint(12.9785, 77.6030),
                GeoPoint(12.9850, 77.6100)
            ),
            poleIds = listOf("P01", "P02", "P03", "P04", "P05", "P06"),
            isRecommended = true
        ),
        EmergencyRoute(
            id = "U02",
            name = "U02: Tunnel Rapid Drainage & Evacuation",
            originName = "Disaster Response Base",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Underground Tunnel Flood Breach",
            destLat = 12.9880,
            destLng = 77.6150,
            distanceKm = 7.5,
            estimatedDurationMin = 11.0,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9702, 77.5925),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9740, 77.6050),
                GeoPoint(12.9880, 77.6150)
            ),
            poleIds = listOf("P01", "P02", "P07", "P08"),
            isRecommended = false
        ),
        EmergencyRoute(
            id = "U03",
            name = "U03: Suburban Landslide Disaster Response",
            originName = "Disaster Response Base",
            originLat = 12.9680,
            originLng = 77.5900,
            destinationName = "Suburban Landslide Emergency",
            destLat = 12.9920,
            destLng = 77.6200,
            distanceKm = 9.1,
            estimatedDurationMin = 13.5,
            waypoints = listOf(
                GeoPoint(12.9680, 77.5900),
                GeoPoint(12.9724, 77.5958),
                GeoPoint(12.9785, 77.6030),
                GeoPoint(12.9740, 77.6050),
                GeoPoint(12.9920, 77.6200)
            ),
            poleIds = listOf("P02", "P04", "P07", "P08"),
            isRecommended = false
        )
    )

    fun getRoutesForVehicle(vehicleType: VehicleType): List<EmergencyRoute> {
        return when (vehicleType) {
            VehicleType.AMBULANCE -> ambulanceRoutes
            VehicleType.FIRE_TRUCK -> fireTruckRoutes
            VehicleType.POLICE -> policeRoutes
            VehicleType.RESCUE -> rescueRoutes
        }
    }

    val availableRoutes: List<EmergencyRoute>
        get() = getRoutesForVehicle(_currentVehicle.value.type)

    // Reactive State Holders
    private val _currentDriver = MutableStateFlow<Driver?>(masterDrivers.first())
    val currentDriver: StateFlow<Driver?> = _currentDriver.asStateFlow()

    private val _currentVehicle = MutableStateFlow<Vehicle>(masterVehicles.first())
    val currentVehicle: StateFlow<Vehicle> = _currentVehicle.asStateFlow()

    private val _selectedRoute = MutableStateFlow<EmergencyRoute>(availableRoutes.first())
    val selectedRoute: StateFlow<EmergencyRoute> = _selectedRoute.asStateFlow()

    private val _poles = MutableStateFlow<List<SmartPole>>(initialPoles)
    val poles: StateFlow<List<SmartPole>> = _poles.asStateFlow()

    private val _activeSession = MutableStateFlow<EmergencySession?>(null)
    val activeSession: StateFlow<EmergencySession?> = _activeSession.asStateFlow()

    private val _roadAlerts = MutableStateFlow<List<RoadAlert>>(listOf(
        RoadAlert(
            id = "ALT-INIT-01",
            title = "Stationary Vehicle Reported",
            type = AlertType.OBSTACLE,
            severity = AlertSeverity.HIGH,
            latitude = 12.9724,
            longitude = 77.5958,
            poleId = "P02",
            description = "ESP32-CAM detected stationary car obstructing emergency lane near Pole P02."
        )
    ))
    val roadAlerts: StateFlow<List<RoadAlert>> = _roadAlerts.asStateFlow()

    private val _currentGps = MutableStateFlow(
        GpsLocation(
            latitude = 12.9680,
            longitude = 77.5900,
            accuracyMeters = 3.2f,
            speedKmh = 0.0f,
            heading = 45.0f,
            isMock = true
        )
    )
    val currentGps: StateFlow<GpsLocation> = _currentGps.asStateFlow()

    // Configurable Corridor Parameters (Change 2, 20)
    private val _corridorConfig = MutableStateFlow(CorridorConfiguration())
    val corridorConfig: StateFlow<CorridorConfiguration> = _corridorConfig.asStateFlow()

    // Real-Time Corridor Event Logs (Change 7)
    private val _eventLogs = MutableStateFlow<List<com.example.smartcorridor.model.CorridorEventLog>>(
        listOf(
            com.example.smartcorridor.model.CorridorEventLog(
                id = "INIT-01",
                timestampMs = System.currentTimeMillis() - 120000,
                timeFormatted = "11:20:00",
                poleId = "SYS",
                state = PoleState.NORMAL,
                message = "Corridor telematics gateway initialized • Standby"
            )
        )
    )
    val eventLogs: StateFlow<List<com.example.smartcorridor.model.CorridorEventLog>> = _eventLogs.asStateFlow()

    // Normal Traffic Monitoring Telemetry (Change 9, 10, 11, 14, 15, 16)
    private val initialNormalVehicles = listOf(
        com.example.smartcorridor.model.NormalVehicle(
            id = "V101",
            type = com.example.smartcorridor.model.NormalVehicleType.CAR,
            speedKmh = 42.0,
            speedLimitKmh = 50.0,
            direction = "Northbound",
            poleId = "P01",
            lane = "Lane 1 (Cruising)",
            isOverspeed = false,
            latitude = 12.9705,
            longitude = 77.5928
        ),
        com.example.smartcorridor.model.NormalVehicle(
            id = "V102",
            type = com.example.smartcorridor.model.NormalVehicleType.BIKE,
            speedKmh = 67.5,
            speedLimitKmh = 50.0,
            direction = "Northbound",
            poleId = "P02",
            lane = "Lane 2 (Fast)",
            isOverspeed = true,
            latitude = 12.9726,
            longitude = 77.5960
        ),
        com.example.smartcorridor.model.NormalVehicle(
            id = "V103",
            type = com.example.smartcorridor.model.NormalVehicleType.BUS,
            speedKmh = 36.0,
            speedLimitKmh = 50.0,
            direction = "Northbound",
            poleId = "P02",
            lane = "Lane 1 (Bus Bay)",
            isOverspeed = false,
            latitude = 12.9730,
            longitude = 77.5968
        ),
        com.example.smartcorridor.model.NormalVehicle(
            id = "V104",
            type = com.example.smartcorridor.model.NormalVehicleType.CAR,
            speedKmh = 54.0,
            speedLimitKmh = 50.0,
            direction = "Southbound",
            poleId = "P03",
            lane = "Lane 1",
            isOverspeed = true,
            latitude = 12.9752,
            longitude = 77.5998
        ),
        com.example.smartcorridor.model.NormalVehicle(
            id = "V105",
            type = com.example.smartcorridor.model.NormalVehicleType.AUTO,
            speedKmh = 31.0,
            speedLimitKmh = 50.0,
            direction = "Northbound",
            poleId = "P04",
            lane = "Lane 1",
            isOverspeed = false,
            latitude = 12.9782,
            longitude = 77.6028
        ),
        com.example.smartcorridor.model.NormalVehicle(
            id = "V106",
            type = com.example.smartcorridor.model.NormalVehicleType.TRUCK,
            speedKmh = 38.0,
            speedLimitKmh = 50.0,
            direction = "Southbound",
            poleId = "P07",
            lane = "Lane 2 (Heavy)",
            isOverspeed = false,
            latitude = 12.9742,
            longitude = 77.6053
        )
    )

    private val _normalVehicles = MutableStateFlow<List<com.example.smartcorridor.model.NormalVehicle>>(initialNormalVehicles)
    val normalVehicles: StateFlow<List<com.example.smartcorridor.model.NormalVehicle>> = _normalVehicles.asStateFlow()

    // Mode: Live Hardware vs Demo / Simulation (Change 21, 22)
    private val _isDemoMode = MutableStateFlow(true)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    // Expose Room DB history flow
    val historySessions: Flow<List<EmergencySessionEntity>> = sessionDao.getAllSessions()


    fun authenticateDriver(badgeOrId: String): Driver? {
        val found = masterDrivers.find {
            it.id.equals(badgeOrId, ignoreCase = true) || it.badgeNumber.equals(badgeOrId, ignoreCase = true)
        }
        if (found != null && found.isAuthorized) {
            _currentDriver.value = found
            masterVehicles.find { it.id == found.assignedVehicleId }?.let {
                _currentVehicle.value = it
            }
        }
        return found
    }

    fun selectVehicle(vehicle: Vehicle) {
        _currentVehicle.value = vehicle
        val newRoutes = getRoutesForVehicle(vehicle.type)
        if (newRoutes.isNotEmpty()) {
            _selectedRoute.value = newRoutes.first()
        }
    }

    fun selectRoute(route: EmergencyRoute) {
        _selectedRoute.value = route
    }

    fun updateGpsLocation(loc: GpsLocation) {
        _currentGps.value = loc
    }

    fun updatePoles(newPoles: List<SmartPole>) {
        _poles.value = newPoles
    }

    fun updatePoleState(poleId: String, newState: PoleState) {
        _poles.value = _poles.value.map {
            if (it.poleId == poleId) it.copy(emergencyState = newState) else it
        }
    }

    fun startEmergency(route: EmergencyRoute, vehicle: Vehicle, driver: Driver): EmergencySession {
        val session = EmergencySession(
            id = "EMRG-${System.currentTimeMillis() % 100000}",
            driverId = driver.id,
            driverName = driver.name,
            vehicleId = vehicle.id,
            vehicleType = vehicle.type,
            originName = route.originName,
            destinationName = route.destinationName,
            routeId = route.id,
            priorityLevel = vehicle.priorityLevel,
            startTime = System.currentTimeMillis(),
            status = EmergencyStatus.ACTIVE
        )
        _activeSession.value = session
        return session
    }

    suspend fun endEmergency(reason: String = "COMPLETED") {
        val current = _activeSession.value ?: return
        val endTs = System.currentTimeMillis()
        val durationSec = ((endTs - current.startTime) / 1000).coerceAtLeast(1)
        val finalStatus = if (reason == "COMPLETED") EmergencyStatus.COMPLETED else EmergencyStatus.CANCELLED

        val finishedSession = current.copy(
            endTime = endTs,
            durationSeconds = durationSec,
            totalDistanceKm = _selectedRoute.value.distanceKm,
            avgSpeedKmh = 52.4,
            status = finalStatus,
            cancellationReason = if (reason != "COMPLETED") reason else null
        )

        // Save into local Room database
        sessionDao.insertSession(
            EmergencySessionEntity(
                id = finishedSession.id,
                driverId = finishedSession.driverId,
                driverName = finishedSession.driverName,
                vehicleId = finishedSession.vehicleId,
                vehicleType = finishedSession.vehicleType.displayName,
                originName = finishedSession.originName,
                destinationName = finishedSession.destinationName,
                routeId = finishedSession.routeId,
                priorityLevel = finishedSession.priorityLevel,
                startTime = finishedSession.startTime,
                endTime = finishedSession.endTime,
                durationSeconds = finishedSession.durationSeconds,
                totalDistanceKm = finishedSession.totalDistanceKm,
                avgSpeedKmh = finishedSession.avgSpeedKmh,
                status = finishedSession.status.name,
                activatedPolesCount = _selectedRoute.value.poleIds.size,
                alertsCount = _roadAlerts.value.size,
                cancellationReason = finishedSession.cancellationReason
            )
        )

        // Reset all poles to normal
        _poles.value = _poles.value.map {
            it.copy(
                emergencyState = PoleState.NORMAL,
                lightBrightness = 15,
                oledMessage = _corridorConfig.value.normalOledMessage,
                activeMessagePriority = 5
            )
        }

        addCorridorEvent(
            poleId = "ALL",
            state = PoleState.NORMAL,
            message = "Emergency run ended ($reason) • All poles returned to normal traffic"
        )

        _activeSession.value = null
    }

    fun addRoadAlert(alert: RoadAlert) {
        _roadAlerts.value = listOf(alert) + _roadAlerts.value
    }

    fun dismissRoadAlert(alertId: String) {
        _roadAlerts.value = _roadAlerts.value.filterNot { it.id == alertId }
    }

    // Config Management (Change 20)
    fun updateCorridorConfig(newConfig: CorridorConfiguration) {
        _corridorConfig.value = newConfig
    }

    fun toggleDemoMode() {
        _isDemoMode.value = !_isDemoMode.value
    }

    // Event Logs (Change 7)
    fun addCorridorEvents(events: List<com.example.smartcorridor.model.CorridorEventLog>) {
        if (events.isEmpty()) return
        _eventLogs.value = (events + _eventLogs.value).take(100)
    }

    fun addCorridorEvent(poleId: String, state: PoleState, message: String) {
        val now = System.currentTimeMillis()
        val timeFmt = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(now))
        val evt = com.example.smartcorridor.model.CorridorEventLog(
            id = "EVT-$now-$poleId",
            timestampMs = now,
            timeFormatted = timeFmt,
            poleId = poleId,
            state = state,
            message = message
        )
        _eventLogs.value = (listOf(evt) + _eventLogs.value).take(100)
    }

    fun clearEventLogs() {
        _eventLogs.value = emptyList()
    }

    // Normal Vehicle Detection & Simulation (Changes 10, 11, 14, 15, 16, 21)
    fun addNormalVehicle(vehicle: com.example.smartcorridor.model.NormalVehicle) {
        _normalVehicles.value = listOf(vehicle) + _normalVehicles.value.take(24)

        // Also update the pole's local traffic statistics
        _poles.value = _poles.value.map { pole ->
            if (pole.poleId == vehicle.poleId) {
                val newCount = pole.vehiclesDetected + 1
                val newOverspeed = if (vehicle.isOverspeed) pole.overspeedCount + 1 else pole.overspeedCount
                val newMax = maxOf(pole.maxSpeedKmh, vehicle.speedKmh)
                val newAvg = ((pole.averageSpeedKmh * pole.vehiclesDetected) + vehicle.speedKmh) / newCount
                val newTrafficLevel = if (newCount > 25) "CONGESTED" else if (newCount > 10) "MODERATE" else "LOW"

                // LED priority: if emergency is not active, set overspeed or normal traffic message
                val (msg, prio) = if (pole.emergencyState != PoleState.ACTIVE && pole.emergencyState != PoleState.PREPARING) {
                    if (vehicle.isOverspeed) {
                        Pair("OVERSPEED (${vehicle.speedKmh.toInt()} KM/H) • SLOW DOWN", 3)
                    } else {
                        Pair("SPEED: ${vehicle.speedKmh.toInt()} KM/H • LIMIT 50", 4)
                    }
                } else {
                    Pair(pole.oledMessage, pole.activeMessagePriority)
                }

                pole.copy(
                    vehiclesDetected = newCount,
                    overspeedCount = newOverspeed,
                    maxSpeedKmh = newMax,
                    averageSpeedKmh = ((newAvg * 10).toInt()) / 10.0,
                    trafficLevel = newTrafficLevel,
                    oledMessage = msg,
                    activeMessagePriority = prio
                )
            } else {
                pole
            }
        }
    }

    fun simulateNewVehicleDetection(isOverspeed: Boolean = false) {
        val nextId = "V" + (100 + (_normalVehicles.value.size + 1))
        val polesList = _poles.value
        val randomPole = polesList.randomOrNull() ?: polesList.first()
        val types = com.example.smartcorridor.model.NormalVehicleType.values()
        val randomType = types.random()
        val speed = if (isOverspeed) (58.0 + (Math.random() * 22.0)) else (25.0 + (Math.random() * 23.0))
        val lanes = listOf("Lane 1 (Cruising)", "Lane 2 (Fast)", "Emergency Lane Buffer")

        val newVehicle = com.example.smartcorridor.model.NormalVehicle(
            id = nextId,
            type = randomType,
            speedKmh = ((speed * 10).toInt()) / 10.0,
            speedLimitKmh = _corridorConfig.value.speedLimitKmh,
            direction = if (Math.random() > 0.5) "Northbound" else "Southbound",
            poleId = randomPole.poleId,
            lane = lanes.random(),
            isOverspeed = speed > _corridorConfig.value.speedLimitKmh,
            isSimulated = _isDemoMode.value,
            latitude = randomPole.latitude + (Math.random() - 0.5) * 0.001,
            longitude = randomPole.longitude + (Math.random() - 0.5) * 0.001
        )
        addNormalVehicle(newVehicle)
    }
}

