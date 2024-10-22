#include <PWM.h> //Used to set pwm frequency to 20khz

int Direction = 10; // pin connected to the "Dir"
int Motor = 9; // pin connected to "P" PWM Signal input"
int32_t frequency = 20000; //frequency (in Hz) 20khz

void setup() {
    //initialize all timers except for 0, to save time keeping functions
    InitTimersSafe();

    //sets the frequency for the specified pin
    bool success = SetPinFrequencySafe(Motor, frequency);

    //if the pin frequency was set successfully, turn pin 13 on (Built in LED)
    if (success) {
        pinMode(13, OUTPUT);
        digitalWrite(13, HIGH);
    }

    pinMode(Direction, OUTPUT);
}

void loop() {
    digitalWrite(Direction, HIGH); //Set direction clockwise
    pwmWrite(Motor, 200); //Spin motor between 0-255, in this case 200
    delay(5000); // for 5 seconds
    pwmWrite(Motor, 0); //Spind motor down
    delay(3000); //for 3 seconds

    digitalWrite(Direction, LOW); //Set direction counter clockwise
    pwmWrite(Motor, 200); //Spin motor
    delay(5000); // for 5 seconds
    pwmWrite(Motor, 0); //Spin motor down
    delay(3000); //for 3 seconds
}

