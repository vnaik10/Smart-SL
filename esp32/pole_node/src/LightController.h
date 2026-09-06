#ifndef LIGHT_CONTROLLER_H
#define LIGHT_CONTROLLER_H

#include <Arduino.h>

enum LightMode {
    LIGHT_OFF = 0,
    LIGHT_DIM = 40,      // ~15% brightness for night ambient
    LIGHT_MOTION = 150,  // ~60% brightness for pedestrian/vehicle detection
    LIGHT_EMERGENCY = 255 // 100% full strobe brightness for emergency corridor
};

class LightController {
public:
    LightController(uint8_t pin, uint8_t channel = 0);
    void begin();
    void setMode(LightMode mode);
    void update();
    void setStrobe(bool enabled);
    uint8_t getCurrentDuty() const { return currentDuty; }

private:
    uint8_t pin;
    uint8_t channel;
    uint8_t targetDuty;
    uint8_t currentDuty;
    bool strobeActive;
    unsigned long lastUpdate;
    unsigned long lastStrobeToggle;
    bool strobeState;
};

#endif
