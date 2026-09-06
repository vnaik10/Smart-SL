#ifndef SENSORS_H
#define SENSORS_H

#include <Arduino.h>

class LdrSensor {
public:
    LdrSensor(uint8_t pin) : pin(pin), movingAvg(0) {}
    void begin() { pinMode(pin, INPUT); }
    bool isDark(uint16_t threshold = 1800) {
        uint16_t raw = analogRead(pin);
        movingAvg = (movingAvg * 3 + raw) / 4;
        return movingAvg > threshold; // High resistance in dark -> high ADC
    }
    uint16_t getRaw() const { return movingAvg; }
private:
    uint8_t pin;
    uint16_t movingAvg;
};

class MotionSensor {
public:
    MotionSensor(uint8_t pin) : pin(pin), lastDetected(0) {}
    void begin() { pinMode(pin, INPUT_PULLDOWN); }
    bool isMotionActive(unsigned long holdTimeMs = 30000) {
        if (digitalRead(pin) == HIGH) {
            lastDetected = millis();
            return true;
        }
        return (millis() - lastDetected) < holdTimeMs;
    }
private:
    uint8_t pin;
    unsigned long lastDetected;
};

class BuzzerController {
public:
    BuzzerController(uint8_t pin) : pin(pin), enabled(false), lastToggle(0), state(false) {}
    void begin() { pinMode(pin, OUTPUT); digitalWrite(pin, LOW); }
    void setAlarm(bool on) { enabled = on; if (!on) digitalWrite(pin, LOW); }
    void update() {
        if (!enabled) return;
        if (millis() - lastToggle > 250) {
            lastToggle = millis();
            state = !state;
            digitalWrite(pin, state ? HIGH : LOW);
        }
    }
private:
    uint8_t pin;
    bool enabled;
    unsigned long lastToggle;
    bool state;
};

class BatteryMonitor {
public:
    BatteryMonitor(uint8_t pin) : pin(pin) {}
    void begin() { pinMode(pin, INPUT); }
    float getVoltage() {
        uint16_t raw = analogRead(pin);
        // 3.3V reference, 12-bit ADC, 1:4 divider
        return (raw / 4095.0f) * 3.3f * 4.0f;
    }
    uint8_t getPercentage() {
        float v = getVoltage();
        if (v >= 12.8f) return 100;
        if (v <= 11.5f) return 5;
        return (uint8_t)(((v - 11.5f) / 1.3f) * 95.0f + 5.0f);
    }
private:
    uint8_t pin;
};

#endif
