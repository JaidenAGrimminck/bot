package me.autobot.lib.serial;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class I2CConnection {
    public static int i2cVerboseLevel = 0; //0=none, 1=notify there's an error, 2=print stack trace
    protected static final int THIS_DEVICE_ADDRESS = 0x01;
    public static final int default_bus = 1;

    private Context context;
    private I2CProvider provider;

    private I2CConfig config;
    private I2C device;

    private int deviceAddress;

    private int bus;

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

    public byte[] read(int length) {
        byte[] data = new byte[length];

        for (int i = 0; i < length; i++) {
            try {
                data[i] = device.readByte();
            } catch (Exception e) {
                data[i] = 0x00;
            }
        }

        return data;
    }

    public int getDeviceAddress() {
        return deviceAddress;
    }
    public int getBus() {
        return bus;
    }
}
