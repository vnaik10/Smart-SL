# SMART EMERGENCY CORRIDOR — DEMO & EVALUATION GUIDE
**Complete Step-by-Step Presentation Script for College Final-Year Project Review**

---

## 1. Demonstration Setup

This project features a **unified multi-mode architecture**:
- **Driver Cockpit**: Realistic 13-screen emergency navigation workflow used by ambulance drivers.
- **Admin Command Center**: Complete fleet, pole, route, alert, and analytics oversight with interactive real-time map.
- **Virtual Pole & Hardware Simulator**: Live interactive testing panel that lets you test the 2 KM corridor, trigger ESP32-CAM obstacle alerts, simulate low-battery conditions, and demonstrate fail-safe transitions without requiring physical hardware.

---

## 2. Walkthrough Steps (1 to 36)

### Phase A: Driver Onboarding & Vehicle Assignment
1. Launch the app. The **Driver Login** screen appears with emergency credential authorization.
2. Log in using Driver ID `DRIVER001` and password `emergency2026`.
3. The app authenticates and displays the **Vehicle Details** screen: Ambulance `KA-XX-1234`, assigned to Officer Sarah Chen (`DRIVER001`).
4. Tap **Verify GPS Status**. The **GPS Status** screen verifies satellite lock, accuracy (3.2m), and coordinates. (Toggle between Live Hardware GPS and Test Simulator Mode as needed).

### Phase B: Mission Planning & Route Calculation
5. Tap **Select Destination**. The destination selector displays critical hospitals (`City Hospital`, `District Hospital`, `General Hospital`).
6. Select **City Hospital** (Distance: 6.2 km).
7. The **Select Route** screen calculates multiple possible routes:
   - `Route R01`: City Hospital Express (6.2 km, 8 min, 6 smart poles) — *Recommended*
   - `Route R02`: Via MG Road Alternate (7.5 km, 11 min, 8 smart poles)
   - `Route R03`: Outer Ring Road (9.1 km, 14 min, 10 smart poles)
8. Choose **Route R01**.
9. The **Route Confirmation** screen displays the complete mission manifest. Tap **CONFIRM ROUTE**.

### Phase C: Emergency Activation & 2 KM Corridor Wave
10. The **Start Emergency** screen displays a prominent red warning badge: *"You are about to activate Emergency Mode"*.
11. Tap **START EMERGENCY**.
12. The **Emergency Active** HUD engages! The live status displays:
    - Vehicle: `AMBULANCE KA-XX-1234`
    - Next Pole: `P01 (0.4 KM)`
    - Corridor Status: `ACTIVATED`
    - Running mission timer starts ticking.
13. Switch to **Live Tracking Map** to watch the vehicle proceed along Route R01.
14. Observe the **2 KM Activation Logic**:
    - Streetlight `P01` and `P02` switch to **ACTIVE** (100% Brightness, strobe warning, OLED: "EMERGENCY: CLEAR LANE").
    - Streetlight `P03` switches to **PREPARING** (Display: "EMERGENCY APPROACHING").
    - Distant poles `P04`, `P05` remain **NORMAL** (energy-saving dim light).

### Phase D: Vehicle Progression & Hysteresis
15. As the ambulance moves past `P01` (exceeding the 150m hysteresis threshold):
    - `P01` switches to **PASSED** and seamlessly reverts to **NORMAL** sensor-based lighting.
    - `P03` is promoted from `PREPARING` to **ACTIVE**.
    - `P04` transitions to **PREPARING**.
    - The moving 2 KM corridor window advances in real time.

### Phase E: ESP32-CAM Obstacle Event
16. In the **Simulator** or **Road Alerts** view, trigger an ESP32-CAM road blockage near Pole `P02`.
17. A high-priority audible and visual **Road Alert** badge instantly pops up on both the Driver HUD and Admin Dashboard:
    *"POSSIBLE BLOCKAGE DETECTED NEAR POLE P02"*.
18. The driver acknowledges the hazard or takes a bypass lane.

### Phase F: Destination Arrival & Archival
19. As the vehicle enters within 100 meters of City Hospital, the **Destination Geofence** triggers!
20. The dialog displays: *"EMERGENCY COMPLETE — You have reached City Hospital"*.
21. Tap **END EMERGENCY**.
22. The 2 KM corridor safely shuts down; all remaining active streetlights return to energy-saving automatic lighting.
23. The complete emergency session (GPS track, duration, average speed, activated poles, alerts encountered) is permanently archived in **Emergency History**.

### Phase G: Admin Command & Fleet Verification
24. Tap the **Admin Dashboard** tab in the app top bar.
25. Inspect the **Live Metrics**: Active Emergencies, Active Poles, Fleet Availability, System Health.
26. Open **Pole Management** to inspect battery levels, solar charging, and firmware versions.
27. Open **Emergency History** to inspect the completed mission record and route statistics.
