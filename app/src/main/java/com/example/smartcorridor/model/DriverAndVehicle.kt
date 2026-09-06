package com.example.smartcorridor.model

enum class UserRole {
    ADMIN,
    DRIVER,
    DISPATCHER
}

data class Driver(
    val id: String,
    val name: String,
    val badgeNumber: String,
    val phone: String,
    val isAuthorized: Boolean,
    val assignedVehicleId: String,
    val role: UserRole = UserRole.DRIVER
)

enum class VehicleType(val displayName: String) {
    AMBULANCE("Ambulance"),
    FIRE_TRUCK("Fire Truck"),
    POLICE("Police Vehicle"),
    RESCUE("Rescue Vehicle")
}

data class Vehicle(
    val id: String,
    val type: VehicleType,
    val registrationNumber: String,
    val assignedDriverId: String,
    val priorityLevel: Int, // 1 = Critical, 2 = High, 3 = Normal
    val status: String = "AVAILABLE" // AVAILABLE, EN_ROUTE, INACTIVE
)
