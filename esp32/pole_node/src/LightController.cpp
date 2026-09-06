#include "LightController.h"

LightController::LightController(uint8_t pin, uint8_t channel)
    : pin(pin), channel(channel), targetDuty(0), currentDuty(0),
      strobeActive(false), lastUpdate(0), lastStrobeToggle(0), strobeState(false) {}

void LightController::begin() {
    // 5 kHz PWM frequency, 8-bit resolution (0-255)
    ledcSetup(channel, 5000, 8);
    ledcAttachPin(pin, channel);
    ledcWrite(channel, 0);
}

void LightController::setMode(LightMode mode) {
    targetDuty = (uint8_t)mode;
    if (mode == LIGHT_EMERGENCY) {
        strobeActive = true;
    } else {
        strobeActive = false;
    }
}

void LightController::setStrobe(bool enabled) {
    strobeActive = enabled;
}

void LightController::update() {
    unsigned long now = millis();

    // High-visibility emergency strobe modulation (flashes between 100% and 60% at 4 Hz)
    if (strobeActive) {
        if (now - lastStrobeToggle > 125) {
            strobeState = !strobeState;
            lastStrobeToggle = now;
            uint8_t duty = strobeState ? 255 : 120;
            ledcWrite(channel, duty);
            currentDuty = duty;
        }
        return;
    }

    // Smooth soft-start / soft-fade transition
    if (now - lastUpdate > 10) {
        lastUpdate = now;
        if (currentDuty < targetDuty) {
            currentDuty++;
            ledcWrite(channel, currentDuty);
        } else if (currentDuty > targetDuty) {
            currentDuty--;
            ledcWrite(channel, currentDuty);
        }
    }
}
