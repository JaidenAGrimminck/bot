
#define N_PINGS 3

struct Ping {
    int trigger;
    int echo;
};

Ping pingReference[N_PINGS];
int currentDistance[N_PINGS];

int duration;
int distance;

void setup() {
    //ping 1
    pingReference[0].echo = 6;
    pingReference[0].trigger = 7;
    
    pingReference[1].echo = 8;
    pingReference[1].trigger = 9;

    pingReference[2].echo = 10;
    pingReference[2].trigger = 11;
    

    for (int i = 0; i < N_PINGS; i++) {
        Ping pd = pingReference[i];

        pinMode(pd.echo, INPUT);
        pinMode(pd.trigger, OUTPUT);
    }

    Serial.begin(9600);
    //Serial.println("started!");
}

void loop() {
    String msg = "";

    // put your main code here, to run repeatedly:
    for (int i = 0; i < N_PINGS; i++) {
        Ping pd = pingReference[i];

        // Clears the trigger pin
        digitalWrite(pd.trigger, LOW);
        digitalWrite(pd.trigger, LOW);
        delayMicroseconds(2);

        // Sets the trigger pin on HIGH state for 10 microseconds
        digitalWrite(pd.trigger, HIGH);
        delayMicroseconds(10);
        digitalWrite(pd.trigger, LOW);

        // Reads the echo pin, returns the sound wave travel time in microseconds
        duration = pulseIn(pd.echo, HIGH);
        
        // Calculates the distance the distance
        distance = duration * 0.034 / 2;

        currentDistance[i] = distance;

        delayMicroseconds(5); //quick delay
        
        msg += pd.echo;
        msg += " ";
        msg += currentDistance[i];
        if (i < N_PINGS - 1) {
            msg += " ";
        }
    }

    msg += ".";

    // msg += " ";
    // msg += "\n"

    Serial.print(msg);


    // //0xA3, pin, distance, distance, distance, distance, device_addr
    // byte l[N_PINGS * 7];

    // //loop above has slight delay, so we'll just do it at once.
    // for (int i = 0; i < N_PINGS * 5; i += 5) {
    //     double d = 1.0 * currentDistance[i];
        
    //     byte n[4];

    //     //copy to pre-array
    //     memcpy(n, &d, 4);

    //     reverseArray(n, 4);

    //     l[i] = 0xA3;
    //     l[i + 1] = pingReference[i].echo;
    //     for (int j = 0; j < 4; j++) {
    //         l[i + 2 + j] = n[j];
    //     }
    //     l[i + 6] = 0x00;
    // }

    // //convert over to char[]/string
    // char t[N_PINGS*7];

    // for (int i = 0; i < N_PINGS * 7; i++) {
    //     Serial.write(l[i]);
    // }

    delay(100);
}

void reverseArray(byte* arr, int length) {
    for (int i = 0; i < length / 2; i++) {
        byte temp = arr[i];
        arr[i] = arr[length - i - 1];
        arr[length - i - 1] = temp;
    }
}
