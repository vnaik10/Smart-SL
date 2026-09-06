#include "PoleStateManager.h"

PoleStateManager::PoleStateManager(LightController& light, const char* poleId)
    : light(light), currentState(STATE_NORMAL), stateEnteredTime(0) {
    strncpy(this->poleId, poleId, sizeof(this->poleId) - 1);
    for (int i = 0; i < 4; i++) {
        slots[i].active = false;
        slots[i].lastHeartbeat = 0;
    }
}

void PoleStateManager::begin() {
    transitionTo(STATE_NORMAL);
}

const char* PoleStateManager::getStateString() const {
    switch (currentState) {
        case STATE_NORMAL: return "NORMAL";
        case STATE_PREPARING: return "PREPARING";
        case STATE_ACTIVE: return "ACTIVE";
        case STATE_PASSED: return "PASSED";
        case STATE_FAULT: return "FAULT";
        case STATE_OFFLINE: return "OFFLINE";
        default: return "UNKNOWN";
    }
}

void PoleStateManager::transitionTo(PoleState newState) {
    currentState = newState;
    stateEnteredTime = millis();
    Serial.printf("[%s] State -> %s\n", poleId, getStateString());

    switch (currentState) {
        case STATE_ACTIVE:
            light.setMode(LIGHT_EMERGENCY);
            break;
        case STATE_PREPARING:
            light.setMode(LIGHT_DIM); // Standby amber or dim
            break;
        case STATE_PASSED:
        case STATE_NORMAL:
            light.setStrobe(false);
            break;
        default:
            break;
    }
}

void PoleStateManager::handleCommand(const CorridorPacket& pkt) {
    // Check if addressed to us or broadcast
    if (strcmp(pkt.targetPole, poleId) != 0 && strcmp(pkt.targetPole, "ALL") != 0) {
        return;
    }

    if (pkt.command == CMD_ACTIVATE) {
        // Register or update slot
        for (int i = 0; i < 4; i++) {
            if (!slots[i].active || strcmp(slots[i].emergencyId, pkt.emergencyId) == 0) {
                strncpy(slots[i].emergencyId, pkt.emergencyId, sizeof(slots[i].emergencyId) - 1);
                slots[i].priority = (PriorityLevel)pkt.priority;
                slots[i].lastHeartbeat = millis();
                slots[i].active = true;
                break;
            }
        }
        transitionTo(STATE_ACTIVE);
    } else if (pkt.command == CMD_PREPARE) {
        if (currentState != STATE_ACTIVE) {
            transitionTo(STATE_PREPARING);
        }
    } else if (pkt.command == CMD_DEACTIVATE || pkt.command == CMD_EMERGENCY_CANCEL) {
        for (int i = 0; i < 4; i++) {
            if (slots[i].active && strcmp(slots[i].emergencyId, pkt.emergencyId) == 0) {
                slots[i].active = false;
            }
        }
        evaluateDominantState();
    }
}

void PoleStateManager::evaluateDominantState() {
    bool hasActive = false;
    for (int i = 0; i < 4; i++) {
        if (slots[i].active) {
            hasActive = true;
            break;
        }
    }

    if (!hasActive && currentState == STATE_ACTIVE) {
        transitionTo(STATE_PASSED);
    }
}

void PoleStateManager::update(bool isDark, bool motionDetected) {
    unsigned long now = millis();

    // 1. Check fail-safe watchdog (45 seconds without heartbeat clears emergency slot)
    for (int i = 0; i < 4; i++) {
        if (slots[i].active && (now - slots[i].lastHeartbeat > 45000)) {
            Serial.printf("[%s] Emergency %s timed out! Auto-clearing slot.\n", poleId, slots[i].emergencyId);
            slots[i].active = false;
            evaluateDominantState();
        }
    }

    // 2. Handle PASSED -> NORMAL transient state (5s safety delay)
    if (currentState == STATE_PASSED) {
        if (now - stateEnteredTime > 5000) {
            transitionTo(STATE_NORMAL);
        }
        return;
    }

    // 3. Autonomous local lighting when in NORMAL state (no internet or cloud needed!)
    if (currentState == STATE_NORMAL) {
        if (!isDark) {
            light.setMode(LIGHT_OFF);
        } else {
            if (motionDetected) {
                light.setMode(LIGHT_MOTION);
            } else {
                light.setMode(LIGHT_DIM);
            }
        }
    }
}
