# SMART EMERGENCY CORRIDOR — API & FUNCTION SPECIFICATION

This document defines the backend Cloud Functions, REST endpoints, and WebSocket payload interfaces.

---

## 1. Cloud Function Endpoints

### `POST /api/v1/emergency/start`
Initiates an emergency session.
- **Authorization**: Bearer JWT (Firebase Auth `DRIVER` or `ADMIN` role).
- **Request Body**:
  ```json
  {
    "vehicleId": "KA-XX-1234",
    "routeId": "R01",
    "origin": { "lat": 12.9680, "lng": 77.5900, "name": "Central Depot" },
    "destination": { "lat": 12.9850, "lng": 77.6100, "name": "City Hospital" },
    "priorityLevel": 1
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "emergencyId": "EMRG-2026-0906-001",
    "corridorStatus": "ACTIVATED",
    "initialActivePoles": ["P01", "P02"],
    "initialPreparingPoles": ["P03"]
  }
  ```

---

### `POST /api/v1/emergency/telemetry`
High-frequency breadcrumb update from Driver App.
- **Request Body**:
  ```json
  {
    "emergencyId": "EMRG-2026-0906-001",
    "lat": 12.9721,
    "lng": 77.5952,
    "speedKmh": 58.4,
    "heading": 42.0,
    "accuracy": 3.5,
    "timestamp": 1788693845000
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "nextPoleId": "P02",
    "distanceToNextPoleKm": 1.4,
    "activeCorridorPoles": ["P01", "P02"],
    "upcomingPreparingPoles": ["P03"],
    "isArrived": false
  }
  ```

---

### `POST /api/v1/emergency/end`
Gracefully ends an active emergency session.
- **Request Body**:
  ```json
  {
    "emergencyId": "EMRG-2026-0906-001",
    "reason": "DESTINATION_REACHED" // or "MANUAL_CANCEL"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "durationSeconds": 525,
    "totalDistanceKm": 6.24,
    "status": "COMPLETED"
  }
  ```

---

### `POST /api/v1/alerts/report`
Ingests an obstacle alert from ESP32-CAM or Admin.
- **Request Body**:
  ```json
  {
    "type": "OBSTACLE",
    "severity": "HIGH",
    "poleId": "P02",
    "lat": 12.97244,
    "lng": 77.59582,
    "description": "Stationary vehicle blocking emergency corridor lane"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "alertId": "ALT-042",
    "broadcasted": true
  }
  ```
