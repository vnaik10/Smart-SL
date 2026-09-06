package com.example.smartcorridor.model

enum class PoleState(val displayName: String) {
    NORMAL("Normal"),
    PREPARING("Preparing"),
    ACTIVE("Active"),
    PASSED("Passed"),
    FAULT("Fault"),
    OFFLINE("Offline"),
    OVERRIDDEN("Manual Override")
}

data class SmartPole(
    val poleId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val roadSegmentId: String,
    val routeIds: List<String> = emptyList(),
    val sequenceOrder: Int = 0,
    val hardwareVersion: String = "ESP32-V2.4",
    val firmwareVersion: String = "2.1.0-corridor",
    val status: String = "ONLINE", // ONLINE, OFFLINE, DEGRADED
    val batteryLevel: Int = 90, // %
    val solarCharging: Boolean = true,
    val lightBrightness: Int = 15, // 0 - 100%
    val emergencyState: PoleState = PoleState.NORMAL,
    val lastHeartbeatMs: Long = System.currentTimeMillis(),
    val activeEmergencyId: String? = null,
    val activePriority: Int = 3,
    val oledMessage: String = "STANDBY",
    // Extended Hardware & Traffic Telemetry (Changes 4, 8, 13, 16, 17)
    val communicationStatus: String = "CONNECTED", // CONNECTED, ACKNOWLEDGED, TIMEOUT, DISCONNECTED
    val distanceToVehicleMeters: Double = 0.0,
    val trafficLevel: String = "MODERATE", // LOW, MODERATE, CONGESTED
    val vehiclesDetected: Int = 12,
    val averageSpeedKmh: Double = 44.5,
    val maxSpeedKmh: Double = 68.0,
    val overspeedCount: Int = 2,
    val cameraStatus: String = "ONLINE", // ONLINE, BUSY, ERROR
    val espNowStatus: String = "MESH_ACTIVE", // MESH_ACTIVE, STANDBY, FAILSAFE
    val activeMessagePriority: Int = 5, // 1=Emergency, 2=Hazard, 3=Overspeed, 4=Traffic, 5=Normal
    val routeDistanceMeters: Double = 0.0 // Distance along route from source in meters
)

