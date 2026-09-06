#include "EspNowManager.h"

EspNowManager* EspNowManager::instance = nullptr;

static uint8_t broadcastAddress[] = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};

EspNowManager::EspNowManager() : currentSeq(0), userCallback(nullptr) {
    instance = this;
    memset(poleId, 0, sizeof(poleId));
}

bool EspNowManager::begin(const char* myPoleId, PacketCallback onReceive) {
    strncpy(poleId, myPoleId, sizeof(poleId) - 1);
    userCallback = onReceive;

    WiFi.mode(WIFI_STA);
    WiFi.disconnect();

    if (esp_now_init() != ESP_OK) {
        Serial.println("[ESP-NOW] Init Failed!");
        return false;
    }

    esp_now_peer_info_t peerInfo = {};
    memcpy(peerInfo.peer_addr, broadcastAddress, 6);
    peerInfo.channel = 0;
    peerInfo.encrypt = false;

    if (esp_now_add_peer(&peerInfo) != ESP_OK) {
        Serial.println("[ESP-NOW] Failed to add broadcast peer!");
        return false;
    }

    esp_now_register_recv_cb(onDataRecv);
    Serial.printf("[ESP-NOW] Ready as node: %s\n", poleId);
    return true;
}

uint32_t EspNowManager::calculateCrc(const uint8_t* data, size_t length) {
    uint32_t crc = 0xFFFFFFFF;
    for (size_t i = 0; i < length; i++) {
        crc ^= data[i];
        for (int j = 0; j < 8; j++) {
            crc = (crc >> 1) ^ (0xEDB88320 & (-(crc & 1)));
        }
    }
    return ~crc;
}

bool EspNowManager::broadcastCommand(EspCommand cmd, const char* targetPole, const char* emergencyId, PriorityLevel priority) {
    CorridorPacket pkt = {};
    pkt.magic = ESP_NOW_MAGIC;
    pkt.version = PROTOCOL_VERSION;
    pkt.seqNumber = ++currentSeq;
    strncpy(pkt.emergencyId, emergencyId, sizeof(pkt.emergencyId) - 1);
    strncpy(pkt.sourcePole, poleId, sizeof(pkt.sourcePole) - 1);
    strncpy(pkt.targetPole, targetPole, sizeof(pkt.targetPole) - 1);
    pkt.command = (uint8_t)cmd;
    pkt.priority = (uint8_t)priority;
    pkt.ttl = 4;
    pkt.timestamp = millis();

    pkt.crc = calculateCrc((uint8_t*)&pkt, sizeof(CorridorPacket) - sizeof(uint32_t));

    esp_err_t res = esp_now_send(broadcastAddress, (uint8_t*)&pkt, sizeof(CorridorPacket));
    return res == ESP_OK;
}

void EspNowManager::onDataRecv(const uint8_t *mac, const uint8_t *incomingData, int len) {
    if (len != sizeof(CorridorPacket)) return;

    CorridorPacket pkt;
    memcpy(&pkt, incomingData, sizeof(CorridorPacket));

    if (pkt.magic != ESP_NOW_MAGIC || pkt.version != PROTOCOL_VERSION) return;

    uint32_t calculated = calculateCrc((uint8_t*)&pkt, sizeof(CorridorPacket) - sizeof(uint32_t));
    if (calculated != pkt.crc) {
        Serial.println("[ESP-NOW] CRC checksum mismatch, packet dropped.");
        return;
    }

    // Forwarding logic: If hop TTL > 1, decrement and re-broadcast with jitter
    if (pkt.ttl > 1 && strcmp(pkt.targetPole, instance->poleId) != 0 && strcmp(pkt.targetPole, "ALL") == 0) {
        pkt.ttl--;
        pkt.crc = calculateCrc((uint8_t*)&pkt, sizeof(CorridorPacket) - sizeof(uint32_t));
        delay(random(4, 15)); // Jitter to prevent channel collision
        esp_now_send(broadcastAddress, (uint8_t*)&pkt, sizeof(CorridorPacket));
    }

    if (instance && instance->userCallback) {
        instance->userCallback(pkt);
    }
}
