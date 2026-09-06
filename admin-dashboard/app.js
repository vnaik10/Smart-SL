// Smart Emergency Corridor — Web Admin Dashboard Controller
document.addEventListener("DOMContentLoaded", () => {
  // Initialize Leaflet Map centered on Bangalore Corridor Alpha
  const map = L.map("map").setView([12.973, 77.598], 14);

  L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
    attribution: "&copy; OpenStreetMap contributors &copy; CARTO",
    subdomains: "abcd",
    maxZoom: 19
  }).addTo(map);

  // Route R01 Coordinates
  const routeCoords = [
    [12.9680, 77.5900], // Origin: Central Depot
    [12.9702, 77.5925], // Pole 1
    [12.9724, 77.5958], // Pole 2
    [12.9750, 77.5995], // Pole 3
    [12.9785, 77.6030], // Pole 4
    [12.9815, 77.6065], // Pole 5
    [12.9850, 77.6100]  // Destination: City Hospital
  ];

  const routePolyline = L.polyline(routeCoords, {
    color: "#00E5FF",
    weight: 5,
    opacity: 0.85,
    dashArray: "8, 6"
  }).addTo(map);

  // Smart Poles along Route
  const poles = [
    { id: "P01", name: "P01 - Station Road", coords: [12.9702, 77.5925], state: "ACTIVE", battery: 94 },
    { id: "P02", name: "P02 - MG Road Junction", coords: [12.9724, 77.5958], state: "ACTIVE", battery: 88 },
    { id: "P03", name: "P03 - Victoria Flyover", coords: [12.9750, 77.5995], state: "PREPARING", battery: 92 },
    { id: "P04", name: "P04 - Trinity Circle", coords: [12.9785, 77.6030], state: "NORMAL", battery: 85 },
    { id: "P05", name: "P05 - Hospital Approach", coords: [12.9815, 77.6065], state: "NORMAL", battery: 90 }
  ];

  const poleMarkers = {};

  function getPoleColor(state) {
    switch (state) {
      case "ACTIVE": return "#00F5D4";
      case "PREPARING": return "#FFB703";
      case "PASSED": return "#38BDF8";
      default: return "#64748B";
    }
  }

  poles.forEach(p => {
    const marker = L.circleMarker(p.coords, {
      radius: 9,
      fillColor: getPoleColor(p.state),
      color: "#FFFFFF",
      weight: 2,
      opacity: 1,
      fillOpacity: 0.9
    }).addTo(map);

    marker.bindPopup(`<b>${p.id}</b>: ${p.name}<br>State: <b>${p.state}</b><br>Battery: ${p.battery}%`);
    poleMarkers[p.id] = marker;
  });

  // Emergency Vehicle Marker (Ambulance)
  let vehiclePosIndex = 0;
  const ambulanceIcon = L.divIcon({
    className: "custom-div-icon",
    html: "<div style='background-color:#E63946;width:26px;height:26px;border-radius:50%;border:2px solid #FFF;display:flex;align-items:center;justify-content:center;font-size:14px;box-shadow:0 0 10px #E63946'>🚑</div>",
    iconSize: [26, 26],
    iconAnchor: [13, 13]
  });

  const ambulanceMarker = L.marker(routeCoords[0], { icon: ambulanceIcon }).addTo(map);

  function updatePoleWaveUI() {
    const list = document.getElementById("pole-wave-list");
    if (!list) return;
    list.innerHTML = "";

    poles.forEach(p => {
      let tagClass = "tag-normal";
      if (p.state === "ACTIVE") tagClass = "tag-active";
      if (p.state === "PREPARING") tagClass = "tag-prep";
      if (p.state === "PASSED") tagClass = "tag-passed";

      const item = document.createElement("div");
      item.className = "pole-wave-item";
      item.innerHTML = `
        <span><b>${p.id}</b> (${p.name})</span>
        <span class="pole-tag ${tagClass}">${p.state}</span>
      `;
      list.appendChild(item);
    });
  }

  updatePoleWaveUI();

  // Simulated Ambulance Movement
  let simTimer = null;
  const btnSim = document.getElementById("btn-sim-ambulance");

  btnSim.addEventListener("click", () => {
    if (simTimer) {
      clearInterval(simTimer);
      simTimer = null;
      btnSim.innerText = "🚑 Start Simulated Run";
      return;
    }

    btnSim.innerText = "⏸️ Pause Simulation";
    simTimer = setInterval(() => {
      vehiclePosIndex = (vehiclePosIndex + 1) % routeCoords.length;
      const newPos = routeCoords[vehiclePosIndex];
      ambulanceMarker.setLatLng(newPos);

      // 2 KM Corridor Wave Calculation
      if (vehiclePosIndex === 0) {
        poles[0].state = "ACTIVE";
        poles[1].state = "ACTIVE";
        poles[2].state = "PREPARING";
        poles[3].state = "NORMAL";
        poles[4].state = "NORMAL";
      } else if (vehiclePosIndex === 1) {
        poles[0].state = "ACTIVE";
        poles[1].state = "ACTIVE";
        poles[2].state = "ACTIVE";
        poles[3].state = "PREPARING";
        poles[4].state = "NORMAL";
      } else if (vehiclePosIndex === 2) {
        poles[0].state = "PASSED";
        poles[1].state = "ACTIVE";
        poles[2].state = "ACTIVE";
        poles[3].state = "PREPARING";
        poles[4].state = "NORMAL";
      } else if (vehiclePosIndex >= 3) {
        poles[0].state = "NORMAL";
        poles[1].state = "PASSED";
        poles[2].state = "ACTIVE";
        poles[3].state = "ACTIVE";
        poles[4].state = "PREPARING";
      }

      // Update markers
      poles.forEach(p => {
        poleMarkers[p.id].setStyle({ fillColor: getPoleColor(p.state) });
      });

      updatePoleWaveUI();
    }, 2500);
  });

  // Road Obstacle Injection
  const btnAlert = document.getElementById("btn-trigger-alert");
  btnAlert.addEventListener("click", () => {
    alert("⚠️ ESP32-CAM INTRUSION ALERT INJECTED:\nStationary vehicle blocking emergency lane near Pole P02!");
    L.circleMarker([12.9724, 77.5958], {
      radius: 12,
      fillColor: "#EF4444",
      color: "#FFFFFF",
      weight: 3,
      fillOpacity: 0.9
    }).addTo(map).bindPopup("<b>⚠️ ROAD BLOCKAGE</b><br>Detected by ESP32-CAM near P02").openPopup();
  });
});
