package com.example.smartcorridor.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_sessions")
data class EmergencySessionEntity(
    @PrimaryKey val id: String,
    val driverId: String,
    val driverName: String,
    val vehicleId: String,
    val vehicleType: String,
    val originName: String,
    val destinationName: String,
    val routeId: String,
    val priorityLevel: Int,
    val startTime: Long,
    val endTime: Long?,
    val durationSeconds: Long,
    val totalDistanceKm: Double,
    val avgSpeedKmh: Double,
    val status: String,
    val activatedPolesCount: Int,
    val alertsCount: Int,
    val cancellationReason: String? = null
)
