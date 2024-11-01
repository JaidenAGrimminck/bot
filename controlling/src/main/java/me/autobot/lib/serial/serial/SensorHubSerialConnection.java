package me.autobot.lib.serial.serial;

import me.autobot.lib.serial.i2c.SensorHubI2CConnection;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Connects to the serial port of the sensor hub.
 * */
public class SensorHubSerialConnection extends SerialConnection {

    /** The address of this device, what the signature will be so I2C devices will know to reply to the pi! (I mean they will, but depending on expansion etc it'll be good to have for now...)*/
    protected static final int THIS_DEVICE_ADDRESS = 0x01;

    // A list of the connections that have been made, indexed by the device address
    private static HashMap<String, SensorHubSerialConnection> connections = new HashMap<>();

    /**
     * Gets or creates a connection to the serial device with the given ID, bus, and device.
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
