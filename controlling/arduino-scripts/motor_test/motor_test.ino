#include <PWM.h> //Used to set pwm frequency to 20khz

//PINS: 3, 9, 10

#define RIGHT_SIDE false

int TopRightDirection = 8; // pin connected to the "Dir"
int TopRightMotor = 9; // pin connected to "P" PWM Signal input"

int BackRightDirection = 11;
int BackRightMotor = 10;

int TopLeftDirection = 8;
int TopLeftMotor = 9;

int BackLeftDirection = 11;
int BackLeftMotor = 10;

int32_t frequency = 20000; //frequency (in Hz) 20khz

bool successTop = false;
bool successBack = false;

void setup() {
    //initialize all timers except for 0, to save time keeping functions
    InitTimersSafe();

    Serial.begin(9600);

    Serial.println("starting.");

    //sets the frequency for the specified pin

    if (RIGHT_SIDE) {
        successTop = SetPinFrequencySafe(TopRightMotor, frequency);
        successBack = SetPinFrequencySafe(BackRightMotor, frequency);
    } else {
        successTop = SetPinFrequencySafe(TopLeftMotor, frequency);
        successBack = SetPinFrequencySafe(BackLeftMotor, frequency);
    }

    //if the pin frequency was set successfully, turn pin 13 on (Built in LED)
    if (successTop && successBack) {
        pinMode(13, OUTPUT);
        digitalWrite(13, HIGH);
    } else {
        pinMode(13, OUTPUT);
        digitalWrite(13, LOW);
    }

    if (RIGHT_SIDE) {
        pinMode(TopRightDirection, OUTPUT);
        pinMode(BackRightDirection, OUTPUT);
    } else {
        pinMode(TopLeftDirection, OUTPUT);
        pinMode(BackLeftDirection, OUTPUT);
    }
}

void loop() {
    if (!(successBack && successTop)) {
        Serial.print(successBack);
        Serial.print(" ");
        Serial.print(successTop);
        Serial.println();

        delay(3000);
        return;
    }

    int topDir = TopLeftDirection;
    int backDir = BackLeftDirection;

    int topMotor = TopLeftMotor;
    int backMotor = BackLeftMotor;

    if (RIGHT_SIDE) {
        topDir = TopRightDirection;
        backDir = BackRightDirection;

        topMotor = TopRightMotor;
        backMotor = BackRightMotor;
    }

    digitalWrite(topDir, HIGH);
    digitalWrite(backDir, HIGH);

    pwmWrite(topMotor, 50);
    pwmWrite(backMotor, 50);

    delay(3000);

    pwmWrite(topMotor, 0);
    pwmWrite(backMotor, 0);

    delay(5000);
}

