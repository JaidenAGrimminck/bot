#include <EEPROM.h>
#include <Wire.h>

/**
NOTICE: In order to use this device, before uploading you MUST set the following variable, 
otherwise the code will NOT work. Do not set it to 0x01, that is RESERVED. Any number from 0x02 to 0xFF is fine.

If any issues are occuring, connect via USB to the device to view the serial log messages.
**/

byte I2C_ADDRESS = 0x00;

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

bool errorSetup = false;
String errorMsg = "";

void setupError(String msg) {
    errorSetup = true;
    errorMsg = msg;
    err(msg);
}

void setup() {
    Serial.begin(9600);

    print("Begun serial.");
    
    byte address = read(0);

    if (address == 0) {
        if (I2C_ADDRESS == 0) {
            setupError("I2C Address was not set! You must set it for every device BEFORE uploading.");
            return;
        } else {
            if (I2C_ADDRESS == 0x01) {
                setupError("Don't set the address to 0x01, that's reserved.");
                return;
            }

            print("Writing to EEPROM to update the I2C address for the first time.");
            write(0, I2C_ADDRESS);
        }
    } else if (address != I2C_ADDRESS) {
        warn("I2C address in code does not match saved address in EEPROM.");
        print("Updating I2C address in EEPROM to new address...");

        if (I2C_ADDRESS == 0x01) {
            setupError("Don't set the address to 0x01, that's reserved.");
            return;
        }

        update(0, I2C_ADDRESS);

        address = I2C_ADDRESS;
    }

    print("Connecting to I2C...");
    Wire.begin(I2C_ADDRESS);

    Wire.onReceive(recieveEvent);
    Wire.onRequest(requestEvent);

    print("Started, now on the lookout for new messages.");
}

void loop() {
    // put your main code here, to run repeatedly:
    if (errorSetup) {
        delay(2000);
        err(errorMsg);
    }
}

void recieveEvent(int nbytes) {
    while (Wire.available()) {
        byte c = Wire.read();
    }
}

void requestEvent() {

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