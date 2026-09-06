#ifndef POLE_STATE_MANAGER_H
#define POLE_STATE_MANAGER_H

#include <Arduino.h>
#include "LightController.h"
#include "EspNowManager.h"

enum PoleState {
    STATE_NORMAL,
    STATE_PREPARING,
    STATE_ACTIVE,
    STATE_PASSED,
    STATE_FAULT,
    STATE_OFFLINE
};

struct EmergencySlot {
    char emergencyId[16];
    PriorityLevel priority;
    unsigned long lastHeartbeat;
    bool active;
};

class PoleStateManager {
public:
    PoleStateManager(LightController& light, const char* poleId);
    void begin();
    void update(bool isDark, bool motionDetected);
    void handleCommand(const CorridorPacket& pkt);

    PoleState getState() const { return currentState; }
    const char* getStateString() const;

private:
    LightController& light;
    char poleId[6];
    PoleState currentState;
    EmergencySlot slots[4]; // Max 4 concurrent overlapping emergencies

    unsigned long stateEnteredTime;
    void transitionTo(PoleState newState);
    void evaluateDominantState();
};

#endif
