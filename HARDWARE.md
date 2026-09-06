# SMART EMERGENCY CORRIDOR — HARDWARE & IOT SPECIFICATION

This document details the embedded electronics, pinouts, firmware module architecture, and ESP-NOW mesh relaying for the ESP32 smart streetlights and ESP32-CAM obstacle detector.

---

## 1. Smart Pole Node Architecture

Each smart streetlight pole is equipped with:
1. **Microcontroller**: ESP32-WROOM-32 (240MHz Dual-Core Xtensa LX6, 520KB SRAM, 4MB Flash).
2. **Streetlight Illumination**: High-power COB LED array driven via IRLZ44N Logic-Level MOSFET with PWM brightness control (0–255 duty cycle, 5 kHz frequency).
3. **Ambient Light Sensing**: LDR (Light Dependent Resistor) in voltage divider circuit with 10k resistor connected to ADC1 (GPIO 34).
4. **Pedestrian/Vehicle Motion**: HC-SR501 PIR sensor or RCWL-0516 microwave radar sensor on digital interrupt GPIO 27.
5. **Visual Driver Alerting**: SSD1306 0.96" I2C OLED or MAX7219 8x32 LED Matrix (displaying "EMERGENCY: CLEAR LANE" / "EMERGENCY APPROACHING").
6. **Acoustic Warning**: Active Piezo Siren / Buzzer on GPIO 25.
7. **Power System**: 12V LiFePO4 battery pack with MPPT Solar Charge Controller, monitored via resistive voltage divider on ADC1 (GPIO 35).

### Pin Allocation Map (ESP32 Smart Pole)

| Component | Pin | Mode / Function |
|---|---|---|
| LED PWM MOSFET | GPIO 18 | `LEDC_CHANNEL_0`, 5 kHz, 8-bit |
| LDR Light Sensor | GPIO 34 | `ADC1_CH6` (Analog In, 0–4095) |
| PIR Motion Sensor | GPIO 27 | `INPUT_PULLDOWN` (Interrupt Edge) |
| Active Buzzer Siren | GPIO 25 | `OUTPUT` (High = 85 dB Alarm) |
| OLED / Matrix Display | GPIO 21 (SDA), GPIO 22 (SCL) | I2C Bus, 400 kHz |
| Battery Voltage Monitor | GPIO 35 | `ADC1_CH7` (Divider Ratio 1:4) |
| ESP-NOW Radio | Internal 2.4 GHz Antenna | IEEE 802.11 b/g/n Action Frames |

---

## 2. Firmware Modular Architecture

The firmware is structured into isolated, testable modules:
```
/esp32
  ├── gateway/
  │   └── src/
  │       └── main.cpp           # Wi-Fi <-> ESP-NOW Bridge
  ├── pole_node/
  │   └── src/
  │       ├── LightController.h/cpp   # PWM LED logic, soft fade
  │       ├── MotionSensor.h/cpp      # PIR debounce and ISR
  │       ├── LdrSensor.h/cpp         # Moving-average lux filtering
  │       ├── EmergencyDisplay.h/cpp  # OLED/LED Matrix scrolling text
  │       ├── BuzzerController.h/cpp  # Pulsed tone generator
  │       ├── BatteryMonitor.h/cpp    # Fuel gauge and low-voltage cutoff
  │       ├── EspNowManager.h/cpp     # Packet parser, CRC, mesh forwarding
  │       ├── PoleStateManager.h/cpp  # Deterministic FSM
  │       └── main.cpp
  └── cam_node/
      └── src/
          └── main.cpp           # ESP32-CAM frame differencing & alert frame
```

---

## 3. ESP-NOW Mesh & Fail-Safe Logic

1. **Broadcast & Targeted Addressing**:
   - Packets specify `targetPole` or `0xFF:0xFF:0xFF:0xFF:0xFF:0xFF` (Broadcast).
   - Each pole checks if `targetPole == MY_POLE_ID || targetPole == ALL_POLES`.
2. **Multi-Hop Relaying**:
   - Each pole decrements `ttl` by 1. If `ttl > 0`, it re-broadcasts the packet after a randomized jitter delay (5–15 ms) to prevent RF collision.
3. **Fail-Safe Timeout**:
   - If an emergency state (`ACTIVE` or `PREPARING`) does not receive a renewal heartbeat within **45 seconds**, the pole autonomously drops back to `NORMAL` mode.
   - Streetlights will **never** stay permanently locked at 100% brightness due to network drops.
