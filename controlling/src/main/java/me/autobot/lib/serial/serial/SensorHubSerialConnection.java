package me.autobot.lib.serial.serial;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.serial.i2c.SensorHubI2CConnection;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Connects to the serial port of the sensor hub.
 * */
public class SensorHubSerialConnection extends SerialConnection {

    /** The address of this device, what the signature will be so I2C devices will know to reply to the pi! (I mean they will, but depending on expansion etc it'll be good to have for now...)*/
    public static final int THIS_DEVICE_ADDRESS = 0x01;

    // A list of the connections that have been made, indexed by the device address
    private static HashMap<String, SensorHubSerialConnection> connections = new HashMap<>();

    /**
     * Gets or creates a connection to the serial device with the given ID, bus, and device.
     * @param baudRate The baud rate of the serial connection.
     * @param commPort The serial port of the device.
     * @return The serial connection to the device.
     * */
    public static SensorHubSerialConnection getOrCreateConnection(int baudRate, String commPort) {
        if (connections.containsKey(commPort)) {
            return connections.get(commPort);
        } else {
            SensorHubSerialConnection connection = new SensorHubSerialConnection(baudRate, commPort);
            connections.put(commPort, connection);
            return connection;
        }
    }

    private HashMap<int[], Sensor> subscribedSensors = new HashMap<>();

    /**
     * Creates a new serial connection with the given baud rate and comm port.
     *
     * @param baudRate The baud rate of the serial connection.
     * @param commPort The comm port of the serial connection.
     */
    public SensorHubSerialConnection(int baudRate, String commPort) {
        super(baudRate, commPort);
    }

    /**
     * The value of "HIGH" in the Arduino.
     * */
    public static final byte[] HIGH = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

    /**
     * The value of "LOW" in the Arduino.
     * */
    public static final byte[] LOW = new byte[] {0x00, 0x00, 0x00, 0x00};


    /**
     * Writes the given data to the serial port.
     * @param data The data to write to the serial port.
     * */
    public void write(byte[] data) {
        super.write(data);
    }

    /**
     * Pings the serial device. Won't return anything, but will let the device know that the Pi is still connected.
     * */
    public void ping() {
        write(new byte[] {(byte) 0xFF, 0x00});
    }

    /**
     * Sends a request to subscribe to a specific pin.
     * @param pin The pin to subscribe to.
     * */
    public void subscribeToPin(int pin) {
        write(new byte[] {(byte) 0xA3, (byte) pin, (byte) THIS_DEVICE_ADDRESS});
    }

    /**
     * Subscribes the given sensor to the given sensor pins.
     * @param sensor The sensor to subscribe.
     * @param pins The pins to subscribe the sensor to.
     * */
    public void subscribeSensor(Sensor sensor, int... pins) {
        subscribedSensors.put(Mathf.allPos(pins), sensor);
        for (int pin : pins) {
            subscribeToPin(pin);
        }
    }

    private boolean adjForDistance = false;

    /**
     * Temporary fix for the distance sensor. Will be removed later.
     * @return This object.
     * */
    public SensorHubSerialConnection adj() {
        adjForDistance = true;
        return this;
    }


    String recieved = "";

    /**
     * Called when serial data is received.
     * @param data The data that was received.
     * */
    @Override
    protected void onSerialData(byte[] data) {
        if (!adjForDistance) return;

        //temp fix, make more reliable later. seems like if an error is called, it doesn't report to main thread.

        //convert the data to a string
        String rawstr = new String(data);

        recieved += rawstr;

        String str = "";
        if (recieved.contains(".")) {
            str = recieved.substring(0, recieved.indexOf("."));
            recieved = recieved.substring(recieved.indexOf(".") + 1);
        } else return;


        //split the string by the delimiter
        String[] parts = str.split(" ");


        if (parts.length % 2 != 0) {
            return;
        }

        for (int i = 0; i < parts.length; i+=2) {
            int pin = Integer.parseInt(parts[i]);
            float value = Float.parseFloat(parts[i + 1]);

            for (int[] pins : subscribedSensors.keySet()) {
                int p_index = 0;
                for (int p : pins) {
                    if (p == pin) {
                        subscribedSensors.get(pins).setSensorValue(p_index, value);
                    }

                    p_index++;
                }
            }
        }


//        // Override this method to handle serial data
//        if (data[0] == (byte) 0xA1 || data[0] == (byte) 0xA3) {
//            byte rpin = data[1];
//
//            float value = ByteBuffer.wrap(data, 2, Float.BYTES).getFloat();
//            byte deviceSignature = data[2 + Float.BYTES]; //ignore this
//
//            int pin = Mathf.allPos(rpin);
//
//            for (int[] pins : subscribedSensors.keySet()) {
//                int p_index = 0;
//                for (int p : pins) {
//                    if (p == pin) {
//                        subscribedSensors.get(pins).setSensorValue(p_index, value);
//                    }
//
//                    p_index++;
//                }
//            }
//
//            System.out.println("recieved update " + pin + ": " + value);
//        }
//
//        System.out.println(data);
    }

    /**
     * Writes a value to a specific pin. The value is a float.
     * @param index The index of the pin to write to.
     * @param value The value to write to the pin.
     * @see SensorHubI2CConnection#write(byte[])
     * */
    public void writeToPin(int index, float value) {
        byte[] payload = new byte[3 + Float.BYTES];
        payload[0] = (byte) 0xA2;
        payload[1] = (byte) index;

        ByteBuffer bbuf = ByteBuffer.allocate(Float.BYTES);
        bbuf.putFloat(value);

        System.arraycopy(bbuf.array(), 0, payload, 2, Float.BYTES);
        payload[2 + Float.BYTES] = THIS_DEVICE_ADDRESS;
        write(payload);
    }

    /**
     * Writes a byte list to a specific pin. Should be 4 bytes long (a float!)
     * @param index The index of the pin to write to.
     * @param value The byte list (length=4) to write to the pin.
     * */
    public void writeToPin(int index, byte[] value) {
        if (value.length != Float.BYTES) {
            return;
        }

        byte[] payload = new byte[3 + value.length];
        payload[0] = (byte) 0xA2;
        payload[1] = (byte) index;

        System.arraycopy(value, 0, payload, 2, value.length);
        payload[2 + value.length] = THIS_DEVICE_ADDRESS;
        write(payload);
    }
}
