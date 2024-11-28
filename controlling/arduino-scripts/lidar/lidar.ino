#include <SoftwareSerial.h>

#define RX_PIN 0
#define TX_PIN 1
#define BAUD_RATE 128000

#define BUFFER_SIZE 256

byte buffer[BUFFER_SIZE]; // just hope that the data is not longer than 256 bytes
int at = 0;

bool recievedStart = false;

bool ignoreData = false;

SoftwareSerial lidarSerial(RX_PIN, TX_PIN);

void setup() {
  Serial.begin(9600);
  lidarSerial.begin(BAUD_RATE);
  Serial.println("Initializing Lidar...");
}

void loop() {
    if (ignoreData) {
        Serial.println("Ignoring data! You may need to power cycle the lidar.");
        delay(1000);
        return;
    }

    if (lidarSerial.available()) {
        Serial.println("Lidar data:");
        while (lidarSerial.available()) {
            uint8_t byte = lidarSerial.read();

            buffer[at++] = byte;

            if (at == BUFFER_SIZE) {
                Serial.println("Buffer full, clearing...");
                at = 0;
            }

            processBuffer();
            
            Serial.print(byte, HEX);
            Serial.print(" ");
        }
        Serial.println();
    } else {
        Serial.println("No data available.");
        delay(500);
    }
}

void processStart();
void processData();

void processBuffer() {
    if (!recievedStart) {
        processStart();
    } else {
        processData();
    }
}

void processStart() {
    if (at < 7) {
        return;
    }

    //start sequence:
    // A5 5A (start of data)
    // 14 00 00 00 (length of initialization)
    // 04 (type code)

    if (buffer[0] == 0xA5 && buffer[1] == 0x5A) {
        Serial.println("Confirmed start of data.");
    } else {
        Serial.println("Invalid start of data, ignoring data.");
        ignoreData = true;
    }

    if (buffer[2] == 0x14 && buffer[3] == 0x00) {
        Serial.println("Confirmed correct length of initialization.");
    } else {
        Serial.println("Invalid start of data, ignoring data.");
        ignoreData = true;
    }

    if (buffer[6] == 0x04) {
        Serial.println("Confirmed correct type code.");
    } else {
        Serial.println("Invalid start of data, ignoring data.");
        ignoreData = true;
    }

    // 4 for the type, 14 for the length, 2 for the start of data
    // then 7 bytes for the device info at the start
    if (at < 7 + 4 + 14 + 2) return;

    byte deviceInfo[20];

    // copy bytes 
    for (int i = 0; i < 20; i++) {
        deviceInfo[i] = buffer[i + 7];
    }

    byte model = deviceInfo[0];
    byte firmware = deviceInfo[1] << 8 | deviceInfo[2];

    byte hardwareVersion = deviceInfo[3];

    // serial number is bytes offset 4 to 19
    byte serialNumber[16];

    for (int i = 0; i < 16; i++) {
        serialNumber[i] = deviceInfo[i + 4];
    }

    Serial.print("Model: ");
    Serial.println(model);

    Serial.print("Firmware: ");
    Serial.println(firmware);

    Serial.print("Hardware version: ");
    Serial.println(hardwareVersion);

    Serial.print("Serial number: ");
    for (int i = 0; i < 16; i++) {
        Serial.print(serialNumber[i], HEX);
    }
    Serial.println();

    Serial.println("Initialization complete.");

    recievedStart = true;
    clearAndMoveBuffer(7 + 4 + 14 + 2);
}

void processData() {
    if (at < 7) return;

    if (buffer[0] != 0xA5 || buffer[1] != 0x5A) {
        //shift buffer by 1, to find the next start of data
        for (int i = 0; i < at - 1; i++) {
            buffer[i] = buffer[i + 1];
        }

        at--;

        return;
    }

    // The data communication of X4PRO adopts the little-endian mode, with the low order first

    // LSB MSB (start sign)
    // LSB MSB LSB (2 bits for mode) MSB (length of data)
    // LSB (type code)
    // ... content

    //get the 7th and 8th bit of the 6th byte
    byte mode = (buffer[5] & 0b11000000) >> 6;

    byte lsbByte = buffer[2];
    byte msbByte = buffer[3];
    byte lsbByte2 = buffer[4];
    byte msbByte2 = buffer[5] & 0b00111111;

    // length is characterized as:
    // 0x[msbByte2][lsbByte2][msbByte][lsbByte]
    int length = (msbByte2 << 24) | (lsbByte2 << 16) | (msbByte << 8) | lsbByte;

    bool isContinous = mode == 0x01;

    byte type = buffer[6];

    Serial.print("Mode: ");
    Serial.println(mode);
    
    at--; // todo: add processData
}


void clearAndMoveBuffer(int offset) {
    for (int i = 0; i < at - offset; i++) {
        buffer[i] = buffer[i + offset];
    }

    at -= offset;
}