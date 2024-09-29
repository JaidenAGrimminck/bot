package me.autobot.lib.serial;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class I2CConnection {
    protected static final int THIS_DEVICE_ADDRESS = 0x01;
    public static final int default_bus = 1;

    private Context context;
    private I2CProvider provider;

    private I2CConfig config;
    private I2C device;

    private int deviceAddress;

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
    }
    
    public void write(byte[] data) {
        device.write(data);
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
}
