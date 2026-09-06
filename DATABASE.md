# SMART EMERGENCY CORRIDOR — DATABASE SCHEMA & ACCESS MODEL

This document specifies the database schemas, access patterns, and rationale for partitioning between **Cloud Firestore** and **Firebase Realtime Database (RTDB)**.

---

## 1. Storage Technology Separation Rationale

- **Cloud Firestore (Persistent Relational-Like Storage)**:
  - Used for stable metadata, master entities, security roles, route geometry, pole device registries, and historical audit logs.
  - Benefits: Complex queries (`where`, `orderBy`), subcollections, document-level security rules, and long-term archiving.
  - Billing Optimization: Low read/write frequency (sessions start once, routes are loaded once, history is read on demand).

- **Firebase Realtime Database (High-Frequency Streaming)**:
  - Used for 1-second GPS breadcrumbs, active pole state bitmasks, live vehicle speeds, and gateway ESP-NOW queues.
  - Benefits: Socket-level JSON tree synchronization, minimal overhead per update, sub-80ms transmission latency.
  - Billing Optimization: Priced per gigabyte transferred rather than per document write, reducing costs for 1 Hz vehicle telemetry.

---

## 2. Cloud Firestore Collections

### Collection: `/users`
```json
{
  "uid": "usr_drv_001",
  "email": "driver001@emergencycorridor.org",
  "role": "DRIVER", // "ADMIN" | "DRIVER" | "DISPATCHER"
  "name": "Sarah Chen",
  "badgeNumber": "MED-8842",
  "assignedVehicleId": "KA-XX-1234",
  "status": "ACTIVE", // "ACTIVE" | "SUSPENDED" | "OFF_DUTY"
  "createdAt": "2026-01-15T08:00:00Z"
}
```

### Collection: `/vehicles`
```json
{
  "vehicleId": "KA-XX-1234",
  "type": "Ambulance", // "Ambulance" | "Fire Truck" | "Police Vehicle" | "Rescue Vehicle"
  "registrationNumber": "KA-01-EQ-1234",
  "organization": "Metropolitan EMS",
  "priorityLevel": 1, // 1 = Critical, 2 = High, 3 = Normal
  "currentDriverId": "usr_drv_001",
  "status": "AVAILABLE", // "AVAILABLE" | "EN_ROUTE" | "MAINTENANCE"
  "equipmentClass": "ALS_ADVANCED_LIFE_SUPPORT"
}
```

### Collection: `/smart_poles`
```json
{
  "poleId": "P02",
  "name": "Pole 02 - MG Road Junction",
  "latitude": 12.97244,
  "longitude": 77.59582,
  "roadSegmentId": "SEG_MG_NORTH",
  "routeIds": ["R01", "R02"],
  "sequenceIndex": 2,
  "hardwareVersion": "ESP32-V2.4",
  "firmwareVersion": "2.1.0-corridor",
  "status": "ONLINE", // "ONLINE" | "OFFLINE" | "DEGRADED"
  "batteryLevel": 88,
  "solarCharging": true,
  "lastHeartbeat": "2026-09-06T11:24:00Z",
  "ambientLightLux": 45.2,
  "ledDutyCyclePercent": 15
}
```

### Collection: `/routes`
```json
{
  "routeId": "R01",
  "name": "Corridor Alpha - City Hospital Express",
  "originName": "Central Depot",
  "destinationName": "City Hospital",
  "originLat": 12.96800,
  "originLng": 77.59000,
  "destLat": 12.98500,
  "destLng": 77.61000,
  "distanceKm": 6.2,
  "estimatedDurationMin": 8.5,
  "poleSequence": ["P01", "P02", "P03", "P04", "P05", "P06"],
  "polyline": "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
  "enabled": true
}
```

### Collection: `/emergency_sessions`
```json
{
  "emergencyId": "EMRG-2026-0906-001",
  "driverId": "usr_drv_001",
  "vehicleId": "KA-XX-1234",
  "vehicleType": "Ambulance",
  "routeId": "R01",
  "originName": "Central Station",
  "destinationName": "City Hospital",
  "priorityLevel": 1,
  "startTime": "2026-09-06T11:20:00Z",
  "endTime": "2026-09-06T11:28:45Z",
  "durationSeconds": 525,
  "totalDistanceKm": 6.24,
  "avgSpeedKmh": 42.8,
  "status": "COMPLETED", // "PENDING" | "ACTIVE" | "COMPLETED" | "CANCELLED"
  "activatedPoles": ["P01", "P02", "P03", "P04", "P05", "P06"],
  "alertsEncountered": ["ALT-042"],
  "cancellationReason": null
}
```

### Collection: `/road_alerts`
```json
{
  "alertId": "ALT-042",
  "type": "OBSTACLE", // "OBSTACLE" | "BLOCKAGE" | "ACCIDENT" | "SENSOR_FAULT"
  "severity": "HIGH", // "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"
  "poleId": "P02",
  "latitude": 12.97244,
  "longitude": 77.59582,
  "emergencyId": "EMRG-2026-0906-001",
  "source": "ESP32_CAM", // "ESP32_CAM" | "SMART_POLE" | "ADMIN"
  "status": "ACTIVE", // "ACTIVE" | "RESOLVED" | "DISMISSED"
  "description": "Possible stationary vehicle obstructing emergency lane near Pole P2.",
  "timestamp": "2026-09-06T11:24:15Z"
}
```

---

## 3. Firebase Realtime Database (RTDB) Schema

```json
{
  "telemetry": {
    "KA-XX-1234": {
      "emergencyId": "EMRG-2026-0906-001",
      "lat": 12.97210,
      "lng": 77.59520,
      "speedKmh": 58.4,
      "heading": 42.0,
      "accuracyMeters": 3.8,
      "timestamp": 1788693845000,
      "isGpsStale": false
    }
  },
  "corridors": {
    "EMRG-2026-0906-001": {
      "status": "ACTIVE",
      "vehicleId": "KA-XX-1234",
      "routeId": "R01",
      "nextPoleId": "P02",
      "distanceToNextPoleKm": 1.4,
      "activePoles": ["P01", "P02"],
      "preparingPoles": ["P03"],
      "passedPoles": []
    }
  },
  "pole_live_states": {
    "P01": { "state": "ACTIVE", "brightness": 100, "oled": "EMERGENCY: CLEAR LANE" },
    "P02": { "state": "ACTIVE", "brightness": 100, "oled": "EMERGENCY: CLEAR LANE" },
    "P03": { "state": "PREPARING", "brightness": 15, "oled": "EMERGENCY APPROACHING" },
    "P04": { "state": "NORMAL", "brightness": 15, "oled": "WELCOME" }
  },
  "gateway_queue": {
    "commands": {
      "cmd_9912": {
        "target": "P02",
        "action": "ACTIVATE",
        "emergencyId": "EMRG-2026-0906-001",
        "timestamp": 1788693845000,
        "processed": false
      }
    }
  }
}
```
