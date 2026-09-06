#include <Arduino.h>
#include "LightController.h"
#include "EspNowManager.h"
#include "PoleStateManager.h"
#include "Sensors.h"

// Hardcoded Pole Identity (e.g. Node P02)
#define POLE_ID "P02"

// Pin Assignments
#define PIN_LED_PWM     18
#define PIN_LDR_ADC     34
#define PIN_PIR_MOTION  27
#define PIN_BUZZER      25
#define PIN_BATTERY_ADC 35

LightController light(PIN_LED_PWM, 0);
LdrSensor ldr(PIN_LDR_ADC);
MotionSensor motion(PIN_PIR_MOTION);
BuzzerController buzzer(PIN_BUZZER);
BatteryMonitor battery(PIN_BATTERY_ADC);
PoleStateManager stateManager(light, POLE_ID);
EspNowManager espNow;

void onPacketReceived(const CorridorPacket& pkt) {
    Serial.printf("[PACKET] Cmd: 0x%02X Target: %s From: %s EMRG: %s\n",
                  pkt.command, pkt.targetPole, pkt.sourcePole, pkt.emergencyId);
    stateManager.handleCommand(pkt);
}

void setup() {
    Serial.begin(115200);
    Serial.printf("\n=== SMART CORRIDOR POLE [%s] BOOTING ===\n", POLE_ID);

    light.begin();
    ldr.begin();
    motion.begin();
    buzzer.begin();
    battery.begin();
    stateManager.begin();

    if (!espNow.begin(POLE_ID, onPacketReceived)) {
        Serial.println("ESP-NOW init failed! Operating in standalone sensor mode.");
    }
}

void loop() {
    bool dark = ldr.isDark();
    bool motionActive = motion.isMotionActive();

    stateManager.update(dark, motionActive);
    light.update();
    buzzer.setAlarm(stateManager.getState() == STATE_ACTIVE);
    buzzer.update();

    // Periodic heartbeat / status telemetry every 10 seconds
    static unsigned long lastHeartbeat = 0;
    if (millis() - lastHeartbeat > 10000) {
        lastHeartbeat = millis();
        Serial.printf("[TELEMETRY] Pole: %s | State: %s | Battery: %d%% (%.2fV) | Duty: %d | Dark: %d | Motion: %d\n",
                      POLE_ID, stateManager.getStateString(),
                      battery.getPercentage(), battery.getVoltage(),
                      light.getCurrentDuty(), dark, motionActive);
    }

    delay(10);
}
