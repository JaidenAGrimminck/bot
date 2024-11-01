#include <EEPROM.h>
#include <Wire.h>
#include <Servo.h>
#include <PWM.h>

//some advanced constants.
#define MAX_MESSAGE_SIZE 25

/**
This is the number of devices that are connected to the Arduino.
**/
#define NUM_DEVICES 5

/**
This is the number of servos connected to the Arduino. This is NOT independent of the above variable.
**/
#define NUM_SERVOS 0

/**
This is the number of ping devices there are. This is NOT independent of the above variable.
**/
#define PING_DEVICES 0

/**
This is whether to use 
*/
#define USE_FREQUENCY_PWM true

/**
Frequency used for the PWM (if enabled above)
*/
#define FREQUENCY 20000 //20kHz

/**
This defines if we're using I2C or via Serial for our messages.
*/
#define USE_I2C false

/**
NOTICE: In order to use this device, before uploading you MUST set the following variable, 
otherwise the code will NOT work. Do not set it less than 0x08, it won't be able to form a connection. Any number from 0x08 to 0xFF is fine.

If any issues are occuring, connect via USB to the device to view the serial log messages.
**/

byte I2C_ADDRESS = 0x12;

/** 
Also, if you want to priortize that address over the one saved in the EEPROM, set the variable
below to 'false'. This is recommended to be set to 'true' if you're fully using the EEPROM, 
as the saved I2C in EEPROM will have no affect if the below variable is set to 'false'.
**/

bool PRIORITIZE_EEPROM_I2C = false;

/** 
EEPROM Formatting

--- GENERAL ---

First 8 bytes are reserved for any information:
0: I2C Address
1: How many devices are attached.
2: RESERVED
3: RESERVED
4: RESERVED
5: RESERVED
6: RESERVED
7: RESERVED

--- DEVICES ---

Then, the next bytes are device information. For each device, they reserve n bytes, following this format,
where the # is the index in the chunk of bytes representing the device.

0: # of bytes that device takes up in EEPROM, represented as "n."
1: Ignore flag or device flag. If 0x00, skip this device (it's disabled). Else, this is the flag that represents the device type.
 - device types:
 - (0x00 ignore)
 - 0x01 -> PIN DEVICE
 - 0x02 -> PWM (SERVO) DEVICE
 - 0x03 -> PING (ULTRASONIC) DEVICE
 - 0x04 -> PWM (pwmWrite) DEVICE
2, ..., n: Specific flags and info for device, see below:

-- PIN DEVICE: --
2: Pin number
3: out / in.
 - 0x01 for out
 - 0x00 for in

-- PWM (SERVO) DEVICE: --
2: Pin number

-- PING (ULTRASONIC) DEVICE: --
2: Pin number for echo (this is also used as reference by I2C)
3: Pin number for trigger

-- PWM (pwmWrite) DEVICE --
2: Pin number

If this is used for testing and likely will need to be constantly changed, enable the following boolean: */

bool IGNORE_EEPROM = true;

/*
Then, you can edit the following EEPROM_SIM to fit the information following the above flags.
You can look at the script "eeprom_gen.py" *TODO* to automatically curate the EEPROM sim registry
if you don't feel comfortable editing it yourself.

Ideally, this can be automatically updated in the control code on the raspi or any device that is connected,
but to not waste EEPROM cycles, you may want to just update it in the EEPROM_SIM.
**/

byte EEPROM_SIM[] = {
    // -- general --
    I2C_ADDRESS, // i2c address
    NUM_DEVICES, // # of devices
    0, 0, 0, 0, 0, 0,
    // -- devices --
    //The motor controllers data eeprom
    0x02, 0x04, 0x09, // top right/left motor
    0x02, 0x04, 0x0A, // back right/left motor
    0x03, 0x01, 0x08, 0x01, // top right/left direction
    0x03, 0x01, 0x0B, 0x01, // back right/left direction
    0x03, 0x01, 0x0D, 0x01, // LED   
};

/**

If you want to look at the I2C formatting for sending/recieving, look at [---- I2C Messages ----]

**/

//variables for the subscription system
//if the following subscription variable is too little, you can increase it 
#define MAX_SUBSCRIPTIONS 1
//TODO: move this into the eeprom

//ordered list of subscriptions, L=2n. n=pin, n+1=subscribed device address
//if subscribed device address<0x08, then it goes back to the control device (rasbpi)
byte subscribedTo[MAX_SUBSCRIPTIONS * 2];
int currentlySubscribed = 0;

//length of the output buffer, i would make it divisible by 8 for it to work well.
#define OUTPUT_BUFFER 64

//response buffer for the 
byte outputBuffer[OUTPUT_BUFFER];
int currentBufferByte = 0;
bool allowWriteToBuffer = false;

//variables for the setup error if it occurs.
bool errorSetup = false;
String errorMsg = "";

//variables for i2c
bool i2cOpen = false;

/**
Called in setup to setup the loop() so it'll print error if someone connects to serial.
**/
void setupError(String msg) {
    errorSetup = true;
    errorMsg = msg;
    err(msg);
}

void setup() {
    if (USE_FREQUENCY_PWM) {
        InitTimersSafe();
    }

    //Begin serial at baud 9600
    Serial.begin(9600);

    log("Begun serial.");
    
    //read the set i2c address for the device
    byte address = read(0);

    //if the saved i2c address is not set (is 0)
    if (address == 0) {
        // compare that i2c address with the set one in the code
        if (I2C_ADDRESS == 0) {
            //if the i2c address in the code was not set, then it'll throw an error since we don't want to connect w/o an address.
            setupError("I2C Address was not set! You must set it for every device BEFORE uploading.");
            return;
        } else {
            //if the i2c address less than 0x08 then throw another error.
            if (I2C_ADDRESS < 0x08) {
                setupError("Don't set the address to anything less than 0x08, that's reserved.");
                return;
            }

            //once done, save that i2c address.
            log("Writing to EEPROM to update the I2C address for the first time.");
            write(0, I2C_ADDRESS);
        }
    } else if (address != I2C_ADDRESS) {
        warn("I2C address in code does not match saved address in EEPROM.");

        if (PRIORITIZE_EEPROM_I2C) {
            log("Updating I2C address in code to one saved in EEPROM");
            //if the address in eeprom is = 0x01, that's a major issue.
            //to fix, just need to reload the code as described below to remove the 0x01 from the EEPROM and update it.
            if (address < 0x08) {
                setupError("I2C address in code is less than 0x08. You must reload the code onto the device and set I2C_ADDRESS to >0x01 and PRIORITIZE_EEPROM_I2C to true to fix this.");
                return;
            } 

            //set the global i2c address to the address read.
            I2C_ADDRESS = address;
        } else {
            log("Updating I2C address in EEPROM to new address...");

            //if the i2c address in code is less than 0x08, ignore it.
            if (I2C_ADDRESS < 0x08) {
                setupError("Don't set I2C_ADDRESS to 0x08, that's reserved.");
                return;
            }

            //update the EEPROM
            update(0, I2C_ADDRESS);

            //set the local to method address to the i2c address
            address = I2C_ADDRESS;
        }
    }
    
    //connect to i2c
    log("Connecting to I2C...");
    Wire.begin(address);

    //register events
    Wire.onReceive(recieveEvent);
    Wire.onRequest(requestEvent);

    log("Initializing devices...");
    init_devices();

    //done.
    log("Successfully started I2C and initialized devices, now on the lookout for new messages.");

    if (!USE_I2C) {
        //Serial.println(10101);
    }
}

/**
Every two bytes is a reference to the number of devices.
0: The pin of the device
1: Type. See below.

-- TYPES: --
- (0x00: DOES NOT EXIST) [used for reference]
- 0x01: PIN OUT
- 0x02: PIN IN
- 0x03: PWM (servo) OUT
- 0x04: PING
- 0x05: PWM (for pwmWrite) OUT
**/
byte deviceReference[NUM_DEVICES * 2];
int deviceReferenceIndex = 0;

int servoReference[NUM_SERVOS];
int servoReferenceIndex = 0;
Servo servos[NUM_SERVOS];

struct Ping {
    int echo;
    int trigger;
};

Ping pingReference[PING_DEVICES];
int currentDistance[PING_DEVICES];
int pingReferenceIndex = 0;

int findPingDeviceIndex(int pin) {
    for (int i = 0; i < PING_DEVICES; i++) {
        if (pingReference[i].echo == pin) return i;
    }

    return -1;
}

byte findDeviceType(byte pin) {
    for (int i = 0; i < NUM_DEVICES * 2; i += 2) {
        if (deviceReference[i] == pin) return deviceReference[i + 1];
    }

    return 0x00;
}

/**
Initializes the devices on the device according to saved EEPROM memory.
**/
void init_devices() {
    //the byte that the program is on.
    int on_byte = 8;

    for (int i = 0; i < read(1); i++) {
        int next_n_bytes = read(on_byte);

        byte deviceType = read(on_byte + 1);

        if (deviceType == 0x01) {
            byte pin = read(on_byte + 2);
            byte inoutR = read(on_byte + 3);

            logp("Registering a PIN device at pin ");
            print((int) pin);
            print(" with type ");
            bool in = inoutR == 0x00;

            deviceReference[deviceReferenceIndex++] = pin;

            if (!in) {
                print("OUT");

                deviceReference[deviceReferenceIndex++] = 0x01;
            } else {
                print("IN");

                deviceReference[deviceReferenceIndex++] = 0x02;
            }

            pinMode(pin, inoutR); //INPUT = 0x00 and OUTPUT = 0x01, so we can just pass it in since that's what we have already

            println("!");
        } else if (deviceType == 0x02) {
            byte pin = read(on_byte + 2);

            deviceReference[deviceReferenceIndex++] = pin;
            deviceReference[deviceReferenceIndex++] = 0x03;

            servos[servoReferenceIndex].attach(pin);
            servoReference[servoReferenceIndex++] = pin;
            
            logp("Added servo to port ");
            println((int) pin);
        } else if (deviceType == 0x03) {
            byte echo = read(on_byte + 2);
            byte trig = read(on_byte + 3);

            Ping ping;

            ping.echo = echo;
            ping.trigger = trig;

            deviceReference[deviceReferenceIndex++] = echo;
            deviceReference[deviceReferenceIndex++] = 0x04;

            pingReference[pingReferenceIndex++] = ping;

            pinMode(trig, OUTPUT);
            pinMode(echo, INPUT);

            logp("Added 4 pin ultrasonic sensor to port ");
            println((int) echo);
        } else if (deviceType == 0x04) {
            byte motor = read(on_byte + 2);

            bool success = SetPinFrequencySafe(motor, FREQUENCY);

            if (success) {
                logp("Successfully added motor to port ");
                println((int) motor);
            } else {
                errp("Issue setting frequency on pin ");
                println((int) motor);

                errorSetup = true;
                errorMsg = "Issue setting pin frequency safe.";

                return;
            }

            deviceReference[deviceReferenceIndex++] = motor;
            deviceReference[deviceReferenceIndex++] = 0x05;
        }

        on_byte += next_n_bytes + 1;
    }
}

/** -- I2C Events -- **/
int currentMessageIndex = 0;
byte currentMessage[MAX_MESSAGE_SIZE];

//used in calculating the ultrasonic sensor.
int distance;
long duration;

void loop() {
    // throw a message every 2 seconds if an error occured during setup.
    if (errorSetup) {
        delay(2000);
        err(errorMsg);
        return;
    }

    //ultrasonic sensor
    for (int i = 0; i < PING_DEVICES; i++) {
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
    }

    //Serial.println(currentDistance[0]);

    for (int i = 0; i < MAX_SUBSCRIPTIONS * 2; i += 2) {
        byte pin = subscribedTo[i];
        byte device_addr = subscribedTo[i + 1];

        if (pin == 0x00) { //empty, not in use
            break;
        }

        byte rbuff[7];

        rbuff[0] = 0xA3;
        rbuff[1] = pin;
        rbuff[6] = read(0);

        byte dtype = findDeviceType(pin);

        if (dtype == 0x04) {
            int in = findPingDeviceIndex(pin);
            if (in == -1) continue;

            float d = 1.0 * currentDistance[in];
            
            byte n[4];

            //copy to pre-array
            memcpy(n, &d, 4);

            // little-endian to big-endian conversion of pre-array
            reverseArray(n, 4);

            //copy then to buffer chunk
            for (int j = 0; j < 4; j++) {
                rbuff[j + 2] = n[j];
            }
        }
        
        //then copy buffer chunk over to the output buffer
        //keep track of how much was added (3 info bytes, 4 bytes for an int)
        if (allowWriteToBuffer) {
            for (int j = 0; j < 7; j++) {
                outputBuffer[currentBufferByte++] = rbuff[j];
            }
        }
    }

    if (allowWriteToBuffer) {
        allowWriteToBuffer = false;
    }

    if (!USE_I2C) {
        if (Serial.available() > 0) {
            //digitalWrite(13, LOW);

            byte c = Serial.read();
            currentMessage[currentMessageIndex++] = c;

            if (currentMessageIndex == MAX_MESSAGE_SIZE) {
                currentMessageIndex--;
            }

            if (processEvent()) clearCurrentMessage();
        }
    }
}

/**
---- I2C Messages ----

--- RECEIVE: ---
0: message type.
- 0xFF: PING
- 0xA0: EDIT EEPROM
- 0xA1: READ
- 0xA2: WRITE
- 0xA3: SUBSCRIBE

-- PING: --
1: 0x00

-- READ: --
1: Pin
2: Signature (I2C address of request)

-- SUBSCRIBE: --
1: Pin
2: Signature (I2C address of request)

-- WRITE: --
1: Pin
2...9: Double Value
- LOW => 0 => [0x00...0x00]
- HIGH => MAX => [0xFF...0xFF]
10: Signature (I2C address of request)

-- EDIT: (EEPROM) --
1: action
- 0x01: add
- 0x02: update
- ...etc. TODO: implement

--- SEND: ---

0:
- 0xA1: RESPONSE TO READ
- 0xA3: RESPONSE TO SUBSCRIBE

-- RESPONSE TO READ: --
1: Pin of device
2...9: Double Value
10: This I2C Signature (I2C_ADDRESS)

-- SUBSCRIBE UPDATE: --
1: Pin of device
2...9: Double Value
10: This I2C Signature (I2C_ADDRESS)

**/

/**
Called whenever bytes are recieved from the I2C bus.
**/
void recieveEvent(int nbytes) {
    while (Wire.available()) {
        byte c = Wire.read();
        currentMessage[currentMessageIndex++] = c;

        if (processEvent()) clearCurrentMessage();
    }
}

/**
Processes any events using the currentMessage buffer.
**/
bool processEvent() {
    if (lessThan(1)) {
        return false;
    }

    if (currentMessage[0] == 0xFF) {
        if (lessThan(2)) return false;

        if (currentMessage[1] == 0x00) {
            log("Pinged!");
        } else {
            incorrectArgs();
        }

        return true;
    } else if (currentMessage[0] == 0xA2) {
        if (lessThan(7)) return false;
        
        byte pin = currentMessage[1];
        byte i2cSignature = currentMessage[7];

        byte rawNumber[4];
        for (int i = 2; i < 6; i++) {
            rawNumber[i - 2] = currentMessage[i];
        }

        // big-endian to little-endian conversion
        reverseArray(rawNumber, 4);

        float d;

        memcpy(&d, rawNumber, sizeof(d));

        byte dtype = findDeviceType(pin);

        if (dtype != 0x00) {
            if (dtype == 0x01) {
                if (currentMessage[2] == 0xFF) {
                    digitalWrite(pin, HIGH);
                    log("Writing HIGH to a device!");
                } else {
                    digitalWrite(pin, LOW);
                    log("Writing LOW to a device!");
                }
            } else if (dtype == 0x03) {
                for (int i = 0; i < NUM_SERVOS; i++) {
                    if (servoReference[i] == pin) {
                        servos[i].write((int) d);

                        logp("Writing ");
                        print(d);
                        print(" to servo on port ");
                        println((int) pin);
                    }
                }
            } else if (dtype == 0x05) {
                int speed = (int) d;

                pwmWrite(pin, speed);

                logp("Writing ");
                print(d);
                print(" to PWM on port ");
                println((int) pin);
            }
        } else {
            errp("Requested device ");
            print((int) pin);
            println(" does not exist.");
        }

        return true;
    } else if (currentMessage[0] == 0xA3) {
        if (lessThan(3)) return false;

        byte pin = currentMessage[1];
        byte signature = currentMessage[2];

        if (currentlySubscribed >= MAX_SUBSCRIPTIONS) {
            err("Cannot subscribe something new! Already at the maximum number of subscriptions!");
            return true;
        }
        
        //n=pin, n+1=device address
        subscribedTo[currentlySubscribed++] = pin;
        subscribedTo[currentlySubscribed++] = signature;
        
        print("[LOG] Subscribed sensor at pin ");
        print(pin);
        println("!");
    }

    return true;
}

/**
If the number of bytes in the currentMessage buffer is less than n, then this returns true.
**/
bool lessThan(int n) {
    return currentMessageIndex < n;
}

/**
Clears and resets the currentMessage buffer.
**/
void clearCurrentMessage() {
    for (int i = 0; i < MAX_MESSAGE_SIZE; i++) {
        currentMessage[i] = 0x00;
    }
    currentMessageIndex = 0;
}

/**
An quick error message if the message recieved via the buffer wasn't correct.
**/
void incorrectArgs() {
    err("Message recieved wasn't correct!");
}

/**
Called whenever bytes are requested 
**/
void requestEvent() {
    if (OUTPUT_BUFFER < 16) {
        err("Hey, that's weird -- the output buffer is less than 16 bytes? It must be >=16 bytes.");
        return;
    }

    //Lol, work-around to this method interupting the loop().... we just run it :)
    loop();

    for (int i = 0; i < 16; i++) {
        Wire.write(outputBuffer[i]);
    }

    //shift the output buffer back 16 bytes
    shiftOutputBuffer(16);

    if (currentBufferByte == 0) {
        allowWriteToBuffer = true;
    }
}


/**
Shifts the output buffer by n bytes (removes the first n bytes and moves the rest of the array from index n to 0)
*/
void shiftOutputBuffer(int nbytes) {
    for (int i = 0; i < OUTPUT_BUFFER; i++) {
        if (i + nbytes >= OUTPUT_BUFFER) {
            outputBuffer[i] = 0;
        } else {
            outputBuffer[i] = outputBuffer[i + nbytes];
        }
    }

    //shift it back that many bytes.
    currentBufferByte -= nbytes;

    if (currentBufferByte < 0) {
        currentBufferByte = 0;
    }
}

/** -- I2C Methods **/
void send(byte address, byte bytes[]) {
    if (i2cOpen) return;

    Wire.beginTransmission(address);
    i2cOpen = true;

    for (int i = 0; i < len(bytes); i++) {
        Wire.write(bytes[i]);
    }

    i2cOpen = false;
    Wire.endTransmission();
}


/** -- EEPROM Methods -- **/

/**
Updates the specified index in the EEPROM
**/
void update(int index, byte value) {
    if (IGNORE_EEPROM) {
        if (index < len(EEPROM_SIM)) {
            EEPROM_SIM[index] = value;
        } else {
            return;
        }
    } else {
        //TODO: implement
    }
}

/**
Writes to the index in the EEPROM
**/
void write(int index, byte value) {
    if (IGNORE_EEPROM) {
        if (index < len(EEPROM_SIM)) {
            EEPROM_SIM[index] = value;
        } else {
            return;
        }
    } else {
        //TODO: implement
    }
}

/**
Reads the index in the EEPROM
**/
byte read(int index) {
    if (IGNORE_EEPROM) {
        if (index < sizeof(EEPROM_SIM)) {
            return EEPROM_SIM[index];
        } else {
            return 0x00;
        }
    } else {
        //TODO: implement
        return 0x00;
    }
}

/** Logging **/

/**
Prints an error to the serial.
**/
void err(String err) {
    String totMsg = "[ERROR] " + err;
    println(totMsg);
}

/**
Prints a partial error to the serial.
**/
void errp(String err) {
    String totMsg = "[ERROR] " + err;
    print(totMsg);
}

/**
Prints a warning to the serial.
**/
void warn(String warn) {
    String totMsg = "[WARNING] " + warn;
    println(totMsg);
}

/**
Logs a message to the serial.
**/
void log(String msg) {
    String totMsg = "[LOG] " + msg;
    println(totMsg);
}

/**
Logs a partial message to the serial.
**/
void logp(String msg) {
    String totMsg = "[LOG]" + msg;
    print(totMsg);
}

void print(String msg) {
    if (!USE_I2C) return;
    Serial.print(msg);
}

void print(int n) {
    if (!USE_I2C) return;
    Serial.print(n);
}

void println(String msg) {
    if (!USE_I2C) return;
    Serial.println(msg);
}

void println(int n) {
    if (!USE_I2C) return;
    Serial.println(n);
}


/** Helper Methods **/

int len(byte arr[]) {
    return sizeof(arr) / sizeof(byte);
}

void reverseArray(byte* arr, int length) {
    for (int i = 0; i < length / 2; i++) {
        byte temp = arr[i];
        arr[i] = arr[length - i - 1];
        arr[length - i - 1] = temp;
    }
}