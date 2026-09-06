package com.example.smartcorridor.model

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class EmergencyRoute(
    val id: String,
    val name: String,
    val originName: String,
    val originLat: Double,
    val originLng: Double,
    val destinationName: String,
    val destLat: Double,
    val destLng: Double,
    val distanceKm: Double,
    val estimatedDurationMin: Double,
    val waypoints: List<GeoPoint>,
    val poleIds: List<String>,
    val isRecommended: Boolean = false
)

enum class EmergencyStatus {
    PENDING,
    ACTIVE,
    ARRIVING,
    COMPLETED,
    CANCELLED
}

data class EmergencySession(
    val id: String,
    val driverId: String,
    val driverName: String,
    val vehicleId: String,
    val vehicleType: VehicleType,
    val originName: String,
    val destinationName: String,
    val routeId: String,
    val priorityLevel: Int = 1,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationSeconds: Long = 0,
    val totalDistanceKm: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val status: EmergencyStatus = EmergencyStatus.PENDING,
    val activatedPoles: List<String> = emptyList(),
    val alertsEncountered: List<String> = emptyList(),
    val cancellationReason: String? = null
)

enum class AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class AlertType {
    OBSTACLE,
    BLOCKAGE,
    ACCIDENT,
    SENSOR_FAULT
}

data class RoadAlert(
    val id: String,
    val title: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val latitude: Double,
    val longitude: Double,
    val poleId: String,
    val emergencyId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "ESP32_CAM", // ESP32_CAM, SENSOR, ADMIN
    val status: String = "ACTIVE", // ACTIVE, RESOLVED, DISMISSED
    val description: String
)

data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 3.2f,
    val speedKmh: Float = 55.0f,
    val heading: Float = 45.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isStale: Boolean = false,
    val isMock: Boolean = true
)

data class CorridorStatus(
    val activePoleCount: Int,
    val preparingPoleCount: Int,
    val nextPoleId: String?,
    val nextPoleName: String?,
    val distanceToNextPoleKm: Double,
    val activeCorridorPoles: List<SmartPole>,
    val upcomingPoles: List<SmartPole>,
    val passedPoles: List<SmartPole>,
    val statusMessage: String,
    val vehicleStationMeters: Double = 0.0,
    val vehicleStationText: String = "Source + 0 m",
    val sourceName: String = "",
    val destinationName: String = "",
    val corridorLookaheadMeters: Double = 2000.0,
    val poleDistancesAhead: Map<String, Double> = emptyMap(),
    val poleRouteDistances: Map<String, Double> = emptyMap()
)
