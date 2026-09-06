package com.example.smartcorridor.model

enum class NormalVehicleType(val displayName: String, val defaultSpeed: Double) {
    CAR("Car", 45.0),
    BIKE("Motorcycle / Bike", 55.0),
    BUS("Public Bus", 38.0),
    TRUCK("Heavy Commercial Truck", 34.0),
    AUTO("Auto-Rickshaw", 32.0)
}

data class NormalVehicle(
    val id: String,
    val type: NormalVehicleType,
    val speedKmh: Double,
    val speedLimitKmh: Double = 50.0,
    val direction: String = "Northbound",
    val headingDegrees: Float = 45f,
    val poleId: String = "P02",
    val lane: String = "Lane 1 (Fast)",
    val detectionTimeMs: Long = System.currentTimeMillis(),
    val isOverspeed: Boolean = speedKmh > speedLimitKmh,
    val isSimulated: Boolean = true,
    val detectionConfidence: Float = 0.94f,
    val baselineDistanceMeters: Double = 15.0, // Dual detection sensor baseline
    val transitDurationMs: Long = 1080L, // Time delta between sensor 1 & 2
    val latitude: Double = 12.9724,
    val longitude: Double = 77.5958
)

data class TrafficSummary(
    val totalVehiclesDetected: Int,
    val activeVehiclesCount: Int,
    val averageSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val overspeedCount: Int,
    val trafficLevel: String, // LOW, MODERATE, CONGESTED
    val activeRoadAlertsCount: Int,
    val isDemoMode: Boolean = true,
    val lastUpdatedMs: Long = System.currentTimeMillis()
)

data class CorridorConfiguration(
    val advanceTriggerDistanceMeters: Double = 2000.0,  // 2 KM main corridor trigger distance
    val preparingBufferMeters: Double = 600.0,          // Anticipatory preparing window buffer
    val deactivationBehindDistanceMeters: Double = 120.0,// Distance behind ambulance before resetting
    val speedLimitKmh: Double = 50.0,
    val overspeedThresholdKmh: Double = 60.0,
    val failSafeTimeoutSeconds: Int = 45,
    val isDemoMode: Boolean = true,
    val emergencyLedTemplate: String = "AMBULANCE APPROACHING • PLEASE GIVE WAY",
    val preparingLedTemplate: String = "EMERGENCY APPROACHING • PREPARE TO CLEAR",
    val overspeedLedTemplate: String = "OVERSPEED DETECTED • SLOW DOWN",
    val normalTrafficLedTemplate: String = "SPEED LIMIT: 50 KM/H • DRIVE SAFELY"
) {
    // Convenience aliases for backward compatibility across engine and repository
    val corridorWindowMeters: Double get() = advanceTriggerDistanceMeters
    val preActivationDistanceMeters: Double get() = preparingBufferMeters
    val preparingWindowMeters: Double get() = advanceTriggerDistanceMeters + preparingBufferMeters
    val passedHysteresisMeters: Double get() = deactivationBehindDistanceMeters
    val emergencyOledMessage: String get() = emergencyLedTemplate
    val preparingOledMessage: String get() = preparingLedTemplate
    val overspeedOledMessage: String get() = overspeedLedTemplate
    val normalOledMessage: String get() = normalTrafficLedTemplate
}


data class CorridorEventLog(
    val id: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val timeFormatted: String,
    val poleId: String,
    val state: PoleState,
    val message: String
)
