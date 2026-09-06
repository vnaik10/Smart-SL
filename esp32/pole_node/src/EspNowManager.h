#ifndef ESP_NOW_MANAGER_H
#define ESP_NOW_MANAGER_H

#include <Arduino.h>
#include <esp_now.h>
#include <WiFi.h>

#define ESP_NOW_MAGIC 0xEC
#define PROTOCOL_VERSION 1

enum EspCommand : uint8_t {
    CMD_PREPARE = 0x01,
    CMD_ACTIVATE = 0x02,
    CMD_DEACTIVATE = 0x03,
    CMD_EMERGENCY_CANCEL = 0x04,
    CMD_HEARTBEAT = 0x05,
    CMD_STATUS_TELEMETRY = 0x06,
    CMD_OBSTACLE_ALERT = 0x07,
    CMD_FAILSAFE_RESET = 0x08
};

enum PriorityLevel : uint8_t {
    PRIORITY_CRITICAL = 1,
    PRIORITY_HIGH = 2,
    PRIORITY_NORMAL = 3
};

#pragma pack(push, 1)
struct CorridorPacket {
    uint8_t magic;           // 0xEC
    uint8_t version;         // 1
    uint16_t seqNumber;      // Sequence number
    char emergencyId[16];    // e.g. "EMRG-001"
    char vehicleId[12];      // e.g. "KA-XX-1234"
    char sourcePole[6];      // e.g. "P01"
    char targetPole[6];      // e.g. "P02" or "ALL"
    uint8_t command;         // EspCommand
    uint8_t priority;        // PriorityLevel
    uint8_t ttl;             // Max hop TTL (e.g. 5)
    uint32_t timestamp;      // Epoch/uptime ms
    uint32_t crc;            // CRC32 checksum
};
#pragma pack(pop)

typedef void (*PacketCallback)(const CorridorPacket& packet);

class EspNowManager {
public:
    EspNowManager();
    bool begin(const char* myPoleId, PacketCallback onReceive);
    bool broadcastCommand(EspCommand cmd, const char* targetPole, const char* emergencyId, PriorityLevel priority);
    bool sendObstacleAlert(const char* description, uint8_t severity);
    void update();

    static uint32_t calculateCrc(const uint8_t* data, size_t length);

private:
    char poleId[6];
    uint16_t currentSeq;
    PacketCallback userCallback;
    static EspNowManager* instance;

    static void onDataRecv(const uint8_t *mac, const uint8_t *incomingData, int len);
};

#endif
