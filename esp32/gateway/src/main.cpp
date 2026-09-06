#include <Arduino.h>
#include <WiFi.h>
#include <esp_now.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// Wi-Fi Credentials
const char* WIFI_SSID = "SmartCorridor_AP";
const char* WIFI_PASS = "emergencyCorridor2026";

// Firebase Realtime Database URL
const char* FIREBASE_HOST = "https://smart-corridor-default-rtdb.firebaseio.com";
const char* FIREBASE_AUTH = "FIREBASE_SECRET_OR_ID_TOKEN";

#define ESP_NOW_MAGIC 0xEC
#define PROTOCOL_VERSION 1

#pragma pack(push, 1)
struct CorridorPacket {
    uint8_t magic;
    uint8_t version;
    uint16_t seqNumber;
    char emergencyId[16];
    char vehicleId[12];
    char sourcePole[6];
    char targetPole[6];
    uint8_t command;
    uint8_t priority;
    uint8_t ttl;
    uint32_t timestamp;
    uint32_t crc;
};
#pragma pack(pop)

static uint8_t broadcastAddress[] = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};

uint32_t calculateCrc(const uint8_t* data, size_t length) {
    uint32_t crc = 0xFFFFFFFF;
    for (size_t i = 0; i < length; i++) {
        crc ^= data[i];
        for (int j = 0; j < 8; j++) {
            crc = (crc >> 1) ^ (0xEDB88320 & (-(crc & 1)));
        }
    }
    return ~crc;
}

void onDataRecv(const uint8_t *mac, const uint8_t *incomingData, int len) {
    if (len != sizeof(CorridorPacket)) return;
    CorridorPacket pkt;
    memcpy(&pkt, incomingData, sizeof(CorridorPacket));

    if (pkt.magic != ESP_NOW_MAGIC) return;

    Serial.printf("[GATEWAY] Received telemetry frame from pole: %s | Cmd: 0x%02X\n",
                  pkt.sourcePole, pkt.command);

    // Relay pole status back to Firebase Realtime Database
    if (WiFi.status() == WL_CONNECTED) {
        HTTPClient http;
        String path = String(FIREBASE_HOST) + "/pole_live_states/" + String(pkt.sourcePole) + ".json?auth=" + FIREBASE_AUTH;
        http.begin(path);
        http.addHeader("Content-Type", "application/json");

        StaticJsonDocument<200> doc;
        doc["state"] = (pkt.command == 0x02) ? "ACTIVE" : "PREPARING";
        doc["lastSeen"] = millis();
        String json;
        serializeJson(doc, json);

        http.PUT(json);
        http.end();
    }
}

void setup() {
    Serial.begin(115200);
    Serial.println("\n=== SMART CORRIDOR IOT GATEWAY BOOTING ===");

    WiFi.mode(WIFI_AP_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASS);

    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 20) {
        delay(500);
        Serial.print(".");
        attempts++;
    }
    Serial.println(WiFi.status() == WL_CONNECTED ? "\nWi-Fi Connected!" : "\nWi-Fi Offline (Running local mesh)");

    if (esp_now_init() == ESP_OK) {
        esp_now_peer_info_t peerInfo = {};
        memcpy(peerInfo.peer_addr, broadcastAddress, 6);
        peerInfo.channel = 0;
        peerInfo.encrypt = false;
        esp_now_add_peer(&peerInfo);
        esp_now_register_recv_cb(onDataRecv);
        Serial.println("ESP-NOW Transceiver Active.");
    }
}

void loop() {
    // Poll Firebase Realtime Database command queue
    static unsigned long lastPoll = 0;
    if (WiFi.status() == WL_CONNECTED && millis() - lastPoll > 1000) {
        lastPoll = millis();
        HTTPClient http;
        String url = String(FIREBASE_HOST) + "/gateway_queue/commands.json?auth=" + FIREBASE_AUTH;
        http.begin(url);
        int httpCode = http.GET();
        if (httpCode == 200) {
            String payload = http.getString();
            if (payload != "null" && payload.length() > 5) {
                DynamicJsonDocument doc(1024);
                deserializeJson(doc, payload);
                JsonObject root = doc.as<JsonObject>();
                for (JsonPair kv : root) {
                    JsonObject cmd = kv.value().as<JsonObject>();
                    if (!cmd["processed"].as<bool>()) {
                        const char* target = cmd["target"];
                        const char* action = cmd["action"];
                        const char* emrgId = cmd["emergencyId"];

                        CorridorPacket pkt = {};
                        pkt.magic = ESP_NOW_MAGIC;
                        pkt.version = PROTOCOL_VERSION;
                        pkt.seqNumber = random(1000, 9999);
                        strncpy(pkt.emergencyId, emrgId, sizeof(pkt.emergencyId) - 1);
                        strncpy(pkt.sourcePole, "GTWY", 5);
                        strncpy(pkt.targetPole, target, 5);
                        pkt.command = (strcmp(action, "ACTIVATE") == 0) ? 0x02 : 0x01;
                        pkt.priority = 1;
                        pkt.ttl = 5;
                        pkt.timestamp = millis();
                        pkt.crc = calculateCrc((uint8_t*)&pkt, sizeof(CorridorPacket) - sizeof(uint32_t));

                        esp_now_send(broadcastAddress, (uint8_t*)&pkt, sizeof(CorridorPacket));
                        Serial.printf("[GATEWAY] Broadcasted cloud command to ESP-NOW: %s -> %s\n", action, target);

                        // Mark processed
                        HTTPClient ackHttp;
                        String ackUrl = String(FIREBASE_HOST) + "/gateway_queue/commands/" + kv.key().c_str() + "/processed.json?auth=" + FIREBASE_AUTH;
                        ackHttp.begin(ackUrl);
                        ackHttp.PUT("true");
                        ackHttp.end();
                    }
                }
            }
        }
        http.end();
    }

    delay(20);
}
