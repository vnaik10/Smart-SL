#include <Arduino.h>
#include <esp_now.h>
#include <WiFi.h>
#include "esp_camera.h"

// ESP32-CAM (AI-Thinker Pinout)
#define PWDN_GPIO_NUM     32
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM      0
#define SIOD_GPIO_NUM     26
#define SIOC_GPIO_NUM     27
#define Y9_GPIO_NUM       35
#define Y8_GPIO_NUM       34
#define Y7_GPIO_NUM       39
#define Y6_GPIO_NUM       36
#define Y5_GPIO_NUM       21
#define Y4_GPIO_NUM       19
#define Y3_GPIO_NUM       18
#define Y2_GPIO_NUM        5
#define VSYNC_GPIO_NUM    25
#define HREF_GPIO_NUM     23
#define PCLK_GPIO_NUM     22

#define FLASH_LED_PIN      4

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

// Low-overhead frame differencing baseline buffer
#define DOWNSAMPLED_W 16
#define DOWNSAMPLED_H 12
uint8_t prevFrame[DOWNSAMPLED_W * DOWNSAMPLED_H] = {0};

void initCamera() {
    camera_config_t config;
    config.ledc_channel = LEDC_CHANNEL_0;
    config.ledc_timer = LEDC_TIMER_0;
    config.pin_d0 = Y2_GPIO_NUM;
    config.pin_d1 = Y3_GPIO_NUM;
    config.pin_d2 = Y4_GPIO_NUM;
    config.pin_d3 = Y5_GPIO_NUM;
    config.pin_d4 = Y6_GPIO_NUM;
    config.pin_d5 = Y7_GPIO_NUM;
    config.pin_d6 = Y8_GPIO_NUM;
    config.pin_d7 = Y9_GPIO_NUM;
    config.pin_xclk = XCLK_GPIO_NUM;
    config.pin_pclk = PCLK_GPIO_NUM;
    config.pin_vsync = VSYNC_GPIO_NUM;
    config.pin_href = HREF_GPIO_NUM;
    config.pin_sscb_sda = SIOD_GPIO_NUM;
    config.pin_sscb_scl = SIOC_GPIO_NUM;
    config.pin_pwdn = PWDN_GPIO_NUM;
    config.pin_reset = RESET_GPIO_NUM;
    config.xclk_freq_hz = 20000000;
    config.pixel_format = PIXFORMAT_GRAYSCALE;
    config.frame_size = FRAMESIZE_QQVGA; // 160x120
    config.jpeg_quality = 12;
    config.fb_count = 1;

    esp_err_t err = esp_camera_init(&config);
    if (err != ESP_OK) {
        Serial.printf("Camera init failed with error 0x%x\n", err);
    }
}

void setup() {
    Serial.begin(115200);
    Serial.println("\n=== SMART CORRIDOR ESP32-CAM OBSTACLE DETECTOR ===");

    pinMode(FLASH_LED_PIN, OUTPUT);
    digitalWrite(FLASH_LED_PIN, LOW);

    initCamera();

    WiFi.mode(WIFI_STA);
    WiFi.disconnect();

    if (esp_now_init() == ESP_OK) {
        esp_now_peer_info_t peerInfo = {};
        memcpy(peerInfo.peer_addr, broadcastAddress, 6);
        peerInfo.channel = 0;
        peerInfo.encrypt = false;
        esp_now_add_peer(&peerInfo);
        Serial.println("ESP-NOW Alert Channel Ready.");
    }
}

void loop() {
    camera_frame_buffer_t *fb = esp_camera_fb_get();
    if (!fb) {
        delay(1000);
        return;
    }

    // Downsample and compute temporal luminance difference to detect lane blockage
    uint32_t deltaSum = 0;
    int stepX = fb->width / DOWNSAMPLED_W;
    int stepY = fb->height / DOWNSAMPLED_H;

    for (int y = 0; y < DOWNSAMPLED_H; y++) {
        for (int x = 0; x < DOWNSAMPLED_W; x++) {
            int idx = y * DOWNSAMPLED_W + x;
            uint8_t pixel = fb->buf[(y * stepY) * fb->width + (x * stepX)];
            deltaSum += abs((int)pixel - (int)prevFrame[idx]);
            prevFrame[idx] = pixel;
        }
    }

    esp_camera_fb_return(fb);

    // If sudden blockage or abnormal activity detected in lane ROI
    if (deltaSum > 3500) {
        Serial.printf("[VISION] Obstacle/Abnormal event detected! Delta: %u\n", deltaSum);

        CorridorPacket alertPkt = {};
        alertPkt.magic = ESP_NOW_MAGIC;
        alertPkt.version = PROTOCOL_VERSION;
        alertPkt.seqNumber = random(1000, 9999);
        strncpy(alertPkt.emergencyId, "ALL", 4);
        strncpy(alertPkt.sourcePole, "CAM2", 5);
        strncpy(alertPkt.targetPole, "ALL", 4);
        alertPkt.command = 0x07; // CMD_OBSTACLE_ALERT
        alertPkt.priority = 1;   // CRITICAL
        alertPkt.ttl = 4;
        alertPkt.timestamp = millis();
        alertPkt.crc = calculateCrc((uint8_t*)&alertPkt, sizeof(CorridorPacket) - sizeof(uint32_t));

        esp_now_send(broadcastAddress, (uint8_t*)&alertPkt, sizeof(CorridorPacket));

        // Flash indicator briefly
        digitalWrite(FLASH_LED_PIN, HIGH);
        delay(80);
        digitalWrite(FLASH_LED_PIN, LOW);

        // Inhibit rapid re-triggering for 8 seconds
        delay(8000);
    }

    delay(200);
}
