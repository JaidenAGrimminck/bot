
// this script tests when the arduino connection cuts off for the serial monitor.
// (there's some byte that the serial monitor can't handle, and it cuts the connection)

double d = 0;

bool received = false;
bool aligned = false;

void setup() {
    Serial.begin(38400);
    //Serial.println("0");

    // internal led
    pinMode(LED_BUILTIN, OUTPUT);
    digitalWrite(LED_BUILTIN, LOW);
}

void loop() {
    if (Serial.available() > 0) {
        byte c = Serial.read();

        //Serial.print(c);

        // enable led
        if (c == 0x05) {
            aligned = true;
            digitalWrite(LED_BUILTIN, HIGH);
        }

        if (aligned) {
            byte n[4];
            memcpy(n, &d, 4);

            byte rn[4];

            for (int i = 0; i < 4; i++) {
                rn[i] = n[3 - i];
            }

            byte test[8];

            for (int i = 0; i < 4; i++) {
                test[i + 4] = rn[i];
            }

            test[0] = 0x0A;
            test[1] = 0x02;
            test[2] = 0x00;
            test[3] = 0x01;

            for (int i = 0; i < 8; i++) {
                Serial.print((char) test[i]);
            }

            d += 1;
        } else {
            byte l[8] = {
                0x55, 0x56, 0x57, 0x58, 0x59, 0x60, 0x61, 0x62
            };

            for (int i = 0; i < 8; i++) {
                Serial.print((char) l[i]);
            }
        }

        delay(100);
    }
}