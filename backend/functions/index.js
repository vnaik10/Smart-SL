const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();
const rtdb = admin.database();

/**
 * 2 KM Corridor Engine: Ingests 1-second GPS Breadcrumbs, computes
 * projection along route, and dynamically transitions smart poles.
 */
exports.onVehicleTelemetryUpdate = functions.database
  .ref("/telemetry/{vehicleId}")
  .onWrite(async (change, context) => {
    const telemetry = change.after.val();
    if (!telemetry || !telemetry.emergencyId) return null;

    const { emergencyId, lat, lng, speedKmh } = telemetry;
    const vehicleId = context.params.vehicleId;

    // Fetch active emergency session
    const emergencyDoc = await db.collection("emergency_sessions").doc(emergencyId).get();
    if (!emergencyDoc.exists || emergencyDoc.data().status !== "ACTIVE") return null;

    const session = emergencyDoc.data();
    const routeDoc = await db.collection("routes").doc(session.routeId).get();
    if (!routeDoc.exists) return null;

    const route = routeDoc.data();
    const poleIds = route.poleSequence || [];

    // Query poles in sequence
    const polesSnapshot = await db.collection("smart_poles").where("poleId", "in", poleIds).get();
    const poles = [];
    polesSnapshot.forEach(doc => poles.push(doc.data()));
    poles.sort((a, b) => a.sequenceIndex - b.sequenceIndex);

    // Calculate approximate longitudinal route distance to each pole
    // Using simple spherical Haversine distance
    function haversine(lat1, lon1, lat2, lon2) {
      const R = 6371000; // meters
      const dLat = (lat2 - lat1) * Math.PI / 180;
      const dLon = (lon2 - lon1) * Math.PI / 180;
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
      return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    const activePoles = [];
    const preparingPoles = [];
    const passedPoles = [];
    let nextPoleId = null;
    let distanceToNextPoleM = Infinity;

    for (let i = 0; i < poles.length; i++) {
      const pole = poles[i];
      const dist = haversine(lat, lng, pole.latitude, pole.longitude);

      // Determine forward orientation: If vehicle has already passed the pole sequence
      // In production, station delta along polyline: DeltaS = S(pole) - S(vehicle)
      if (dist < distanceToNextPoleM && dist > 50) {
        distanceToNextPoleM = dist;
        nextPoleId = pole.poleId;
      }

      // 2 KM Activation Logic Rules:
      // Active window: within 2000m ahead
      // Preparing window: between 2000m and 2600m
      // Passed: passed by > 150m
      if (dist <= 2000) {
        activePoles.push(pole.poleId);
      } else if (dist > 2000 && dist <= 2600) {
        preparingPoles.push(pole.poleId);
      } else {
        passedPoles.push(pole.poleId);
      }
    }

    // Update Realtime Database live corridor state
    await rtdb.ref(`/corridors/${emergencyId}`).update({
      vehicleId,
      nextPoleId: nextPoleId || "DESTINATION",
      distanceToNextPoleKm: (distanceToNextPoleM / 1000).toFixed(2),
      activePoles,
      preparingPoles,
      updatedAt: Date.now()
    });

    // Enqueue gateway command for upcoming poles
    const gatewayUpdates = {};
    activePoles.forEach(p => {
      gatewayUpdates[`/pole_live_states/${p}`] = {
        state: "ACTIVE",
        brightness: 100,
        oled: "EMERGENCY: CLEAR LANE"
      };
    });
    preparingPoles.forEach(p => {
      gatewayUpdates[`/pole_live_states/${p}`] = {
        state: "PREPARING",
        brightness: 40,
        oled: "EMERGENCY APPROACHING"
      };
    });
    await rtdb.ref().update(gatewayUpdates);

    return null;
  });

/**
 * Handle Obstacle Alerts and broadcast to Driver and Admin
 */
exports.onRoadAlertReported = functions.firestore
  .document("road_alerts/{alertId}")
  .onCreate(async (snap, context) => {
    const alert = snap.data();
    console.log(`[ALERT] High-priority alert reported: ${alert.type} near ${alert.poleId}`);

    // Push notification to active emergency vehicles on corridor
    const payload = {
      notification: {
        title: "⚠️ ROAD OBSTACLE ALERT",
        body: `${alert.description} near Pole ${alert.poleId}`
      },
      data: {
        alertId: context.params.alertId,
        poleId: alert.poleId || "",
        severity: alert.severity || "HIGH"
      }
    };

    await admin.messaging().sendToTopic("emergency_drivers", payload);
    return null;
  });
