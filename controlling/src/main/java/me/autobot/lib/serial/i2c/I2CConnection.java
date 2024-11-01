package me.autobot.lib.serial.i2c;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import me.autobot.lib.serial.Connection;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Creates a connection to an I2C device.
 * */
public class I2CConnection extends Connection {
    /**
     * Verboseness level for I2C errors.
     * 0=none, 1=notify there's an error, 2=print stack trace
     * */
    public static int i2cVerboseLevel = 2;

    /** The address of this device, what the signature will be so I2C devices will know to reply to the pi! (I mean they will, but depending on expansion etc it'll be good to have for now...)*/
    protected static final int THIS_DEVICE_ADDRESS = 0x01;

    /**
     * The default I2C bus to use.
     * This is the bus that the I2C device is connected to by default.
     * */
    public static final int default_bus = 1;

    private Context context;
    private I2CProvider provider;

    private I2CConfig config;
    private I2C device;

    private int deviceAddress;

    private int bus;

    /**
     * Creates a new I2C connection with the given bus and device address.
     * @param id The ID of the I2C device.
     *           Can be any string.
     *           This is used to identify the device in the Pi4J context.
     * @param bus The bus of the I2C device.
     * @param device The device of the I2C device.
     * */
    public I2CConnection(String id, int bus, int device) {
        context = Pi4J.newAutoContext();
        provider = context.provider("linuxfs-i2c");
        config = I2C.newConfigBuilder(context)
                .id(id)
                .bus(bus)
                .device(device)
                .build();
        this.device = provider.create(config);   

        this.deviceAddress = device;
        this.bus = bus;
    }

    /**
     * Writes data to the I2C device.
     * @param data The data to write to the I2C device.
     * */
    public void write(byte[] data) {
        try {
            device.write(data);
        } catch (Exception e) {
            if (i2cVerboseLevel == 2) {
                e.printStackTrace();
            } else if (i2cVerboseLevel == 1) {
                System.out.println("Error writing to I2C device");
            }
        }
    }

    /**
     * Reads data from the I2C device.
     * @param length The length of the data to read.
     * @return The data read from the I2C device.
     * */
    public byte[] read(int length) {
        ByteBuffer buff = device.readByteBuffer(length);

        return buff.array();
    }

    /**
     * Gets the device address of the I2C device.
     * @return The device address of the I2C device.
     * */
    public int getDeviceAddress() {
        return deviceAddress;
    }

    /**
     * Gets the bus of the I2C device.
     * @return The bus of the I2C device.
     * */
    public int getBus() {
        return bus;
    }
}
