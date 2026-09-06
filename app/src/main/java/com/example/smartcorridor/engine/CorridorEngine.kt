package com.example.smartcorridor.engine

import com.example.smartcorridor.model.CorridorConfiguration
import com.example.smartcorridor.model.CorridorEventLog
import com.example.smartcorridor.model.EmergencyRoute
import com.example.smartcorridor.model.GeoPoint
import com.example.smartcorridor.model.GpsLocation
import com.example.smartcorridor.model.PoleState
import com.example.smartcorridor.model.SmartPole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CorridorEngine {

    // Default 2 KM activation parameters
    const val DEFAULT_ACTIVE_AHEAD_METERS = 1200.0       // Immediate 1.2 KM 100% active zone
    const val DEFAULT_CORRIDOR_WINDOW_METERS = 2000.0    // 2.0 KM total corridor lookahead
    const val DEFAULT_PASSED_HYSTERESIS_METERS = 120.0   // Distance behind ambulance before marking PASSED
    const val DEFAULT_PASSED_TO_NORMAL_METERS = 350.0    // Distance behind ambulance before resetting to NORMAL

    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    /**
     * Accurate Great Circle Haversine Distance in meters
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Calculates cumulative distance in meters for each waypoint along the route polyline.
     * Index 0 is always 0.0 (SOURCE / START LOCATION).
     */
    fun calculatePolylineCumulativeDistances(waypoints: List<GeoPoint>): DoubleArray {
        if (waypoints.isEmpty()) return DoubleArray(0)
        val cum = DoubleArray(waypoints.size)
        cum[0] = 0.0
        for (i in 1 until waypoints.size) {
            val prev = waypoints[i - 1]
            val curr = waypoints[i]
            cum[i] = cum[i - 1] + calculateDistanceMeters(prev.latitude, prev.longitude, curr.latitude, curr.longitude)
        }
        return cum
    }

    /**
     * Projects any geographic point (vehicle or smart pole) onto the route polyline.
     * Computes the exact longitudinal distance along the route from the SOURCE (waypoint 0).
     * Works correctly even when the road curves, turns corners, or has multiple bends.
     */
    fun projectPointOntoRouteDistance(
        lat: Double,
        lng: Double,
        waypoints: List<GeoPoint>,
        cumDistances: DoubleArray
    ): Double {
        if (waypoints.isEmpty()) return 0.0
        if (waypoints.size == 1) return 0.0

        var minPerpDist = Double.MAX_VALUE
        var bestRouteDist = 0.0

        for (i in 0 until waypoints.size - 1) {
            val a = waypoints[i]
            val b = waypoints[i + 1]

            val meanLat = Math.toRadians((a.latitude + b.latitude) / 2.0)
            val metersPerDegLat = 111132.954
            val metersPerDegLng = 111412.84 * cos(meanLat)

            val ax = a.longitude * metersPerDegLng
            val ay = a.latitude * metersPerDegLat
            val bx = b.longitude * metersPerDegLng
            val by = b.latitude * metersPerDegLat
            val px = lng * metersPerDegLng
            val py = lat * metersPerDegLat

            val segDx = bx - ax
            val segDy = by - ay
            val segLenSq = segDx * segDx + segDy * segDy

            val t = if (segLenSq > 0.0001) {
                (((px - ax) * segDx + (py - ay) * segDy) / segLenSq).coerceIn(0.0, 1.0)
            } else {
                0.0
            }

            val projX = ax + t * segDx
            val projY = ay + t * segDy
            val perpDist = sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY))

            if (perpDist < minPerpDist) {
                minPerpDist = perpDist
                val segDist = cumDistances[i + 1] - cumDistances[i]
                bestRouteDist = cumDistances[i] + t * segDist
            }
        }

        return bestRouteDist
    }

    /**
     * Evaluates the 2 KM Smart Corridor Wave along the selected route.
     *
     * Key Principles (Strictly adhering to 2 KM Emergency Triggering):
     * 1. ROUTE DISTANCE, NOT STRAIGHT-LINE RADIUS:
     *    Vehicle position and all smart pole positions are projected onto the route polyline.
     *    Distances are measured longitudinally along the road.
     * 2. SOURCE-BASED INITIALIZATION:
     *    When emergency starts at SOURCE (vehicle at 0m), the next 2 KM is calculated immediately:
     *    - Poles within 0 - 1.2 KM ahead -> ACTIVE (100% luminaire brightness + strobes + LED warning)
     *    - Poles within 1.2 - 2.0 KM ahead -> PREPARING (50% brightness + advance LED warning)
     *    - Poles beyond 2.0 KM -> STANDBY / NORMAL
     *    Poles are prepared/activated in advance BEFORE the vehicle starts moving or arrives.
     * 3. ROLLING 2 KM WINDOW:
     *    As vehicle moves forward, the 2 KM window rolls forward along the route:
     *    - Passed poles (distance < -120m) -> PASSED -> NORMAL
     *    - New upcoming poles entering the 2 KM window -> PREPARING -> ACTIVE
     * 4. 24/7 DAY & NIGHT OVERRIDE:
     *    Emergency corridor triggers regardless of ambient LDR sensor (Day or Night).
     */
    fun evaluateCorridor(
        vehicleLocation: GpsLocation,
        route: EmergencyRoute,
        allPoles: List<SmartPole>,
        activePriority: Int = 1,
        config: CorridorConfiguration = CorridorConfiguration()
    ): CorridorEvaluationResult {
        val routePoles = route.poleIds.mapNotNull { id -> allPoles.find { it.poleId == id } }
            .sortedBy { it.sequenceOrder }

        if (routePoles.isEmpty() || route.waypoints.isEmpty()) {
            return CorridorEvaluationResult(
                updatedPoles = allPoles,
                nextPole = null,
                distanceToNextPoleKm = 0.0,
                activePoles = emptyList(),
                preparingPoles = emptyList(),
                passedPoles = emptyList(),
                generatedEvents = emptyList()
            )
        }

        // 1. Calculate cumulative polyline chainage from Source
        val cumDistances = calculatePolylineCumulativeDistances(route.waypoints)
        val routeTotalLengthM = cumDistances.lastOrNull() ?: (route.distanceKm * 1000.0)

        // 2. Determine Vehicle Station along Route from Source
        val vehicleRouteDistM = projectPointOntoRouteDistance(
            lat = vehicleLocation.latitude,
            lng = vehicleLocation.longitude,
            waypoints = route.waypoints,
            cumDistances = cumDistances
        )

        val vehicleStationText = if (vehicleRouteDistM < 1000.0) {
            "Source + ${vehicleRouteDistM.toInt()} m"
        } else {
            "Source + ${"%.2f".format(vehicleRouteDistM / 1000.0)} km"
        }

        // 3. Precompute Route Distances for each Pole along Route
        val poleRouteDistances = mutableMapOf<String, Double>()
        val poleDistancesAhead = mutableMapOf<String, Double>()

        routePoles.forEach { pole ->
            val pRouteDist = projectPointOntoRouteDistance(
                lat = pole.latitude,
                lng = pole.longitude,
                waypoints = route.waypoints,
                cumDistances = cumDistances
            )
            poleRouteDistances[pole.poleId] = pRouteDist
            poleDistancesAhead[pole.poleId] = pRouteDist - vehicleRouteDistM
        }

        val updatedPolesMap = allPoles.associateBy { it.poleId }.toMutableMap()
        val activeList = mutableListOf<SmartPole>()
        val preparingList = mutableListOf<SmartPole>()
        val passedList = mutableListOf<SmartPole>()
        val newEvents = mutableListOf<CorridorEventLog>()

        var nextPole: SmartPole? = null
        var distanceToNextPoleM = Double.MAX_VALUE

        val nowTs = System.currentTimeMillis()
        val timeStr = timeFormatter.format(Date(nowTs))

        // Threshold parameters
        val activeAheadMeters = DEFAULT_ACTIVE_AHEAD_METERS
        val corridorLookaheadMeters = config.corridorWindowMeters.coerceAtLeast(2000.0) // 2.0 KM
        val hysteresisBehindMeters = config.passedHysteresisMeters.coerceAtLeast(100.0)
        val passedToNormalMeters = DEFAULT_PASSED_TO_NORMAL_METERS

        // 4. Assign Deterministic Corridor States Based on Route Distance
        routePoles.forEach { pole ->
            val deltaM = poleDistancesAhead[pole.poleId] ?: 0.0
            val poleRouteDistM = poleRouteDistances[pole.poleId] ?: 0.0

            val newState: PoleState
            val brightness: Int
            val oledText: String
            val msgPriority: Int

            when {
                // Pole is BEHIND the vehicle
                deltaM < -passedToNormalMeters -> {
                    // Vehicle is far past pole -> Revert to Normal traffic baseline
                    newState = PoleState.NORMAL
                    brightness = 15
                    oledText = config.normalTrafficLedTemplate
                    msgPriority = 5
                }
                deltaM < -hysteresisBehindMeters -> {
                    // Vehicle has just passed pole -> Cleared / Resetting
                    newState = PoleState.PASSED
                    brightness = 15
                    oledText = "EMERGENCY PASSED • RESUME NORMAL SPEED"
                    msgPriority = 4
                    passedList.add(pole)
                }
                // Pole is AHEAD of vehicle within immediate 1.2 KM active wave
                deltaM <= activeAheadMeters -> {
                    newState = PoleState.ACTIVE
                    brightness = 100 // Emergency 100% Brightness + Strobes (Works 24/7 Day & Night)
                    oledText = config.emergencyLedTemplate
                    msgPriority = 1
                    activeList.add(pole)

                    // Track immediate next upcoming pole
                    if (deltaM >= -hysteresisBehindMeters && deltaM < distanceToNextPoleM) {
                        distanceToNextPoleM = deltaM.coerceAtLeast(0.0)
                        nextPole = pole
                    }
                }
                // Pole is AHEAD within the rolling 2.0 KM Corridor preparation window
                deltaM <= corridorLookaheadMeters -> {
                    newState = PoleState.PREPARING
                    brightness = 50 // Advance caution lighting
                    oledText = "EMERGENCY VEHICLE APPROACHING (2.0 KM) • PREPARE TO CLEAR"
                    msgPriority = 1
                    preparingList.add(pole)

                    // If no active pole is ahead, next pole is the preparing pole
                    if (nextPole == null && deltaM < distanceToNextPoleM) {
                        distanceToNextPoleM = deltaM
                        nextPole = pole
                    }
                }
                // Pole is OUTSIDE the 2.0 KM window (Standby / Normal traffic)
                else -> {
                    newState = PoleState.NORMAL
                    brightness = 15
                    oledText = config.normalTrafficLedTemplate
                    msgPriority = 5
                }
            }

            // Priority resolution: if pole already had a higher priority emergency, retain it
            val resolvedState = if (pole.activePriority < activePriority && pole.emergencyState == PoleState.ACTIVE) {
                pole.emergencyState
            } else {
                newState
            }

            // Detect state change and emit chronological event log
            if (pole.emergencyState != resolvedState) {
                val distFromSrcFmt = if (poleRouteDistM < 1000.0) "${poleRouteDistM.toInt()}m" else "${"%.1f".format(poleRouteDistM / 1000.0)}km"
                val deltaFmt = if (deltaM >= 0) "+${deltaM.toInt()}m ahead" else "${deltaM.toInt()}m behind"
                val eventDesc = when (resolvedState) {
                    PoleState.ACTIVE -> "${pole.poleId} ACTIVATED • 100% Strobe ($deltaFmt, $distFromSrcFmt from Source)"
                    PoleState.PREPARING -> "${pole.poleId} PREPARING • 2 KM Wave Advancing ($deltaFmt)"
                    PoleState.PASSED -> "${pole.poleId} PASSED • Corridor Clear"
                    PoleState.NORMAL -> "${pole.poleId} NORMAL • Baseline Lighting"
                    else -> "${pole.poleId} -> ${resolvedState.displayName}"
                }
                newEvents.add(
                    CorridorEventLog(
                        id = "EVT-${nowTs}-${pole.poleId}",
                        timestampMs = nowTs,
                        timeFormatted = timeStr,
                        poleId = pole.poleId,
                        state = resolvedState,
                        message = eventDesc
                    )
                )
            }

            updatedPolesMap[pole.poleId] = pole.copy(
                emergencyState = resolvedState,
                lightBrightness = if (resolvedState == PoleState.ACTIVE) 100 else if (resolvedState == PoleState.PREPARING) 50 else 15,
                oledMessage = oledText,
                activePriority = if (resolvedState == PoleState.ACTIVE || resolvedState == PoleState.PREPARING) 1 else 5,
                activeMessagePriority = msgPriority,
                distanceToVehicleMeters = deltaM,
                routeDistanceMeters = poleRouteDistM,
                communicationStatus = if (pole.status == "ONLINE") "CONNECTED" else "OFFLINE"
            )
        }

        // If all poles were passed or nextPole was not picked, select the last pole or nearest
        if (nextPole == null) {
            nextPole = routePoles.firstOrNull { (poleDistancesAhead[it.poleId] ?: -1.0) >= 0.0 }
                ?: routePoles.lastOrNull()
            distanceToNextPoleM = (poleDistancesAhead[nextPole?.poleId] ?: 0.0).coerceAtLeast(0.0)
        }

        val distanceToNextKm = if (nextPole != null) (distanceToNextPoleM / 1000.0) else 0.0

        return CorridorEvaluationResult(
            updatedPoles = updatedPolesMap.values.toList(),
            nextPole = nextPole,
            distanceToNextPoleKm = distanceToNextKm,
            activePoles = activeList,
            preparingPoles = preparingList,
            passedPoles = passedList,
            generatedEvents = newEvents,
            vehicleStationMeters = vehicleRouteDistM,
            vehicleStationText = vehicleStationText,
            poleDistancesAhead = poleDistancesAhead,
            poleRouteDistances = poleRouteDistances
        )
    }

    /**
     * Checks if destination geofence has been reached (< 100 meters)
     */
    fun isDestinationReached(vehicleLocation: GpsLocation, route: EmergencyRoute): Boolean {
        val distToDest = calculateDistanceMeters(
            vehicleLocation.latitude,
            vehicleLocation.longitude,
            route.destLat,
            route.destLng
        )
        return distToDest <= 100.0
    }
}

data class CorridorEvaluationResult(
    val updatedPoles: List<SmartPole>,
    val nextPole: SmartPole?,
    val distanceToNextPoleKm: Double,
    val activePoles: List<SmartPole>,
    val preparingPoles: List<SmartPole>,
    val passedPoles: List<SmartPole>,
    val generatedEvents: List<CorridorEventLog> = emptyList(),
    val vehicleStationMeters: Double = 0.0,
    val vehicleStationText: String = "Source + 0 m",
    val poleDistancesAhead: Map<String, Double> = emptyMap(),
    val poleRouteDistances: Map<String, Double> = emptyMap()
)
