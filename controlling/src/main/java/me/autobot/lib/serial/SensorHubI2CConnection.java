package me.autobot.lib.serial;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.robot.Sensor;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * I2C connection to the <a href="https://github.com/JaidenAGrimminck/bot/blob/main/controlling/arduino-scripts/readwriter/readwriter.ino">configurable Arduino code</a> that's prebuilt for this library.
 * */
public class SensorHubI2CConnection extends I2CConnection {
    /**
     * Default bus to use for the I2C connection.
     * */
    public static final int DEFAULT_BUS = 1;

    /**
     * The value of "HIGH" in the Arduino.
     * */
    public static final byte[] HIGH = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

    /**
     * The value of "LOW" in the Arduino.
     * */
    public static final byte[] LOW = new byte[] {0x00, 0x00, 0x00, 0x00};

    /**
     * Generates an ID for the I2C connection for the Pi4J context.
     * @param bus The bus of the I2C connection.
     * @param device The device of the I2C connection.
     * @return The ID of the I2C connection.
     * */
    public static String generateId(int bus, int device) {
        return "i2c-connection-" + bus + "-" + device;
    }

    // A list of the connections that have been made, indexed by the device address
    private static HashMap<String, SensorHubI2CConnection> connections = new HashMap<>();

    /**
     * Gets or creates a connection to the I2C device with the given ID, bus, and device.
     * @param id The ID of the I2C device. Uses this as reference for the Pi4J context.
     * @param bus The bus of the I2C device.
     * @param device The device of the I2C device.
     * @return The I2C connection to the device.
     * */
    public static SensorHubI2CConnection getOrCreateConnection(String id, int bus, int device) {
        if (connections.containsKey(id)) {
            return connections.get(id);
        } else {
            SensorHubI2CConnection connection = new SensorHubI2CConnection(id, bus, device);
            connections.put(id, connection);
            return connection;
        }
    }

    private HashMap<int[], Sensor> subscribedSensors = new HashMap<>();

    /**
     * Creates a new I2C connection to the default Arduino code with the given bus and device address.
     * @param id The ID of the I2C connection.
     * @param bus The bus of the I2C connection.
     * @param device The device address of the I2C connection.
     * */
    public SensorHubI2CConnection(String id, int bus, int device) {
        super(id, bus, device);
    }

    /**
     * Subscribes the given sensor to the given sensor pins.
     * @param sensor The sensor to subscribe.
     * @param pins The pins to subscribe the sensor to.
     * */
    public void subscribeSensor(Sensor sensor, int... pins) {
        subscribedSensors.put(Mathf.allPos(pins), sensor);
        for (int pin : pins) {
            System.out.println("Subscribing to pin " + pin);
            subscribeToPin(pin);
        }

        setupReadThread();
    }

    /**
     * Creates a new reading thread that reads data from the I2C device.
     * This is a separate thread that runs in the background.
     * @see Thread
     * @see Runnable
     * @see SensorHubI2CConnection#read(int)
     * */
    protected void setupReadThread() {
        Thread thread = new Thread(
                () -> {
                    while (true) {
                        byte[] data = read(16);
                        if (data != null) {
                            if (data[0] == (byte) 0xA1 || data[0] == (byte) 0xA3) {
                                byte rpin = data[1];

                                float value = ByteBuffer.wrap(data, 2, Float.BYTES).getFloat();
                                byte deviceSignature = data[2 + Float.BYTES];

                                //run a check to see if the device signature is the same as the device address, if not, ignore the data
                                assert deviceSignature == getDeviceAddress() : "Device signature does not match device address.";

                                int pin = Mathf.allPos(rpin);

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
                        }

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        thread.start();
    }

    /**
     * Pings the I2C device. Won't return anything, but will let the device know that the Pi is still connected.
     * */
    public void ping() {
        write(new byte[] {(byte) 0xFF, 0x00});
    }

    /**
     * Sends a request to read the value of a specific pin.
     * @param index The index of the pin to read.
     * */
    public void readPin(int index) {
        write(new byte[] {(byte) 0xA1, (byte) index, THIS_DEVICE_ADDRESS});
    }

    /**
     * Sends a request to subscribe to a specific pin.
     * @param pin The pin to subscribe to.
     * */
    public void subscribeToPin(int pin) {
        write(new byte[] {(byte) 0xA3, (byte) pin, (byte) THIS_DEVICE_ADDRESS});
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
