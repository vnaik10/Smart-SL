package com.example.smartcorridor.engine

import com.example.smartcorridor.model.EmergencyRoute
import com.example.smartcorridor.model.GeoPoint
import com.example.smartcorridor.model.GpsLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SimulationEngine {

    private val _simulatedLocation = MutableStateFlow<GpsLocation?>(null)
    val simulatedLocation: StateFlow<GpsLocation?> = _simulatedLocation.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _progressRatio = MutableStateFlow(0f)
    val progressRatio: StateFlow<Float> = _progressRatio.asStateFlow()

    private var currentRoute: EmergencyRoute? = null
    private var currentIndex = 0
    private var simulationJob: Job? = null
    private var speedMultiplier = 1.0f

    fun setRoute(route: EmergencyRoute) {
        currentRoute = route
        currentIndex = 0
        if (route.waypoints.isNotEmpty()) {
            val origin = route.waypoints.first()
            _simulatedLocation.value = GpsLocation(
                latitude = origin.latitude,
                longitude = origin.longitude,
                speedKmh = 60.0f,
                heading = 45.0f,
                isMock = true
            )
            _progressRatio.value = 0f
        }
    }

    fun startSimulation(scope: CoroutineScope, onStep: (GpsLocation) -> Unit) {
        val route = currentRoute ?: return
        if (route.waypoints.isEmpty()) return

        _isSimulating.value = true
        simulationJob?.cancel()

        simulationJob = scope.launch(Dispatchers.Default) {
            while (_isSimulating.value && currentIndex < route.waypoints.size) {
                val pt = route.waypoints[currentIndex]
                val nextPt = if (currentIndex + 1 < route.waypoints.size) route.waypoints[currentIndex + 1] else pt
                val heading = calculateBearing(pt.latitude, pt.longitude, nextPt.latitude, nextPt.longitude)

                val loc = GpsLocation(
                    latitude = pt.latitude,
                    longitude = pt.longitude,
                    speedKmh = (55.0f + (currentIndex % 3) * 5.0f) * speedMultiplier,
                    heading = heading.toFloat(),
                    accuracyMeters = 2.8f,
                    timestamp = System.currentTimeMillis(),
                    isMock = true
                )

                _simulatedLocation.value = loc
                _progressRatio.value = currentIndex.toFloat() / (route.waypoints.size - 1).coerceAtLeast(1)
                onStep(loc)

                currentIndex++
                // Update every 1.5 seconds divided by speed multiplier
                delay((1500L / speedMultiplier).toLong().coerceAtLeast(200L))
            }

            if (currentIndex >= route.waypoints.size) {
                _isSimulating.value = false
            }
        }
    }

    fun stepForward(onStep: (GpsLocation) -> Unit) {
        val route = currentRoute ?: return
        if (currentIndex < route.waypoints.size - 1) {
            currentIndex++
            val pt = route.waypoints[currentIndex]
            val loc = GpsLocation(
                latitude = pt.latitude,
                longitude = pt.longitude,
                speedKmh = 60.0f,
                heading = 45.0f,
                accuracyMeters = 2.5f,
                timestamp = System.currentTimeMillis(),
                isMock = true
            )
            _simulatedLocation.value = loc
            _progressRatio.value = currentIndex.toFloat() / (route.waypoints.size - 1).coerceAtLeast(1)
            onStep(loc)
        }
    }

    fun pauseSimulation() {
        _isSimulating.value = false
        simulationJob?.cancel()
    }

    fun resetSimulation() {
        pauseSimulation()
        currentIndex = 0
        _progressRatio.value = 0f
        currentRoute?.let { setRoute(it) }
    }

    fun setSpeed(multiplier: Float) {
        speedMultiplier = multiplier
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2))
        val x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon)
        val brng = Math.toDegrees(Math.atan2(y, x))
        return (brng + 360) % 360
    }
}
