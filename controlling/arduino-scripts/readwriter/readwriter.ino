#include <EEPROM.h>
#include <Wire.h>
#include <Servo.h>

#define MAX_MESSAGE_SIZE 25

/**
NOTICE: In order to use this device, before uploading you MUST set the following variable, 
otherwise the code will NOT work. Do not set it to 0x01, that is RESERVED. Any number from 0x02 to 0xFF is fine.

If any issues are occuring, connect via USB to the device to view the serial log messages.
**/

byte I2C_ADDRESS = 0x02;

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
 - 0x02 -> PWM DEVICE
2, ..., n: Specific flags and info for device.

-- PIN DEVICE --
2: Pin number
3: out / in.
 - 0x01 for out
 - 0x00 for in

-- PWM DEVICE --
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
    //general
    I2C_ADDRESS, // i2c address
    0, // # of devices
    0, 0, 0, 0, 0, 0, 0, 0, 0,
    //devices
};

/**

If you want to look at the I2C formatting for sending/recieving, look at [---- I2C Messages ----]

**/

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
    //Begin serial at baud 9600
    Serial.begin(9600);

    print("Begun serial.");
    
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
            //if the i2c address is 0x01 (the raspi i2c address) then throw another error.
            if (I2C_ADDRESS == 0x01) {
                setupError("Don't set the address to 0x01, that's reserved.");
                return;
            }

            //once done, save that i2c address.
            print("Writing to EEPROM to update the I2C address for the first time.");
            write(0, I2C_ADDRESS);
        }
    } else if (address != I2C_ADDRESS) {
        warn("I2C address in code does not match saved address in EEPROM.");

        if (PRIORITIZE_EEPROM_I2C) {
            print("Updating I2C address in code to one saved in EEPROM");
            //if the address in eeprom is = 0x01, that's a major issue.
            //to fix, just need to reload the code as described below to remove the 0x01 from the EEPROM and update it.
            if (address == 0x01) {
                setupError("I2C address in code is equal to 0x01. You must reload the code onto the device and set I2C_ADDRESS to >0x01 and PRIORITIZE_EEPROM_I2C to true to fix this.");
                return;
            } 

            //set the global i2c address to the address read.
            I2C_ADDRESS = address;
        } else {
            print("Updating I2C address in EEPROM to new address...");

            //if the i2c address in code is equal to 0x01, ignore it.
            if (I2C_ADDRESS == 0x01) {
                setupError("Don't set I2C_ADDRESS to 0x01, that's reserved.");
                return;
            }

            //update the EEPROM
            update(0, I2C_ADDRESS);

            //set the local to method address to the i2c address
            address = I2C_ADDRESS;
        }
    }
    
    //connect to i2c
    print("Connecting to I2C...");
    Wire.begin(address);

    //register events
    Wire.onReceive(recieveEvent);
    Wire.onRequest(requestEvent);

    //done.
    print("Started, now on the lookout for new messages.");
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

        print("device type is " + ((int) deviceType));

        on_byte += next_n_bytes;
    }
}

void loop() {
    // throw a message every 2 seconds if an error occured during setup.
    if (errorSetup) {
        delay(2000);
        err(errorMsg);
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

/** -- I2C Events -- **/
int currentMessageIndex = 0;
byte currentMessage[MAX_MESSAGE_SIZE];

void recieveEvent(int nbytes) {
    while (Wire.available()) {
        byte c = Wire.read();
        currentMessage[currentMessageIndex++] = c;

        if (processEvent()) clearCurrentMessage();
    }
}

bool processEvent() {
    if (lessThan(1)) {
        return false;
    }

    if (currentMessage[0] == 0xFF) {
        if (lessThan(2)) return false;

        if (currentMessage[1] == 0x00) {
            print("Pinged!");
        } else {
            incorrectArgs();
        }

        return true;
    }
}

bool lessThan(int n) {
    return currentMessageIndex < n;
}

void clearCurrentMessage() {
    for (int i = 0; i < MAX_MESSAGE_SIZE; i++) {
        currentMessage[i] = 0x00;
    }
    currentMessageIndex = 0;
}

void incorrectArgs() {
    err("Message recieved wasn't correct!");
}

void requestEvent() {
    
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
        if (index < len(EEPROM_SIM)) {
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
    Serial.println(totMsg);
}

/**
Prints a warning to the serial.
**/
void warn(String warn) {
    String totMsg = "[WARNING] " + warn;
    Serial.println(totMsg);
}

/**
Prints a message to the serial.
**/
void print(String msg) {
    String totMsg = "[LOG] " + msg;
    Serial.println(totMsg);
}

/** Helper Methods **/

int len(byte arr[]) {
    return sizeof(arr) / sizeof(byte);
}