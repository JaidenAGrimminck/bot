package me.autobot.lib.serial;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.robot.Sensor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class SensorHubI2CConnection extends I2CConnection {
    private HashMap<int[], Sensor> subscribedSensors = new HashMap<>();

    public SensorHubI2CConnection(String id, int bus, int device) {
        super(id, bus, device);

        setupReadThread();
    }

    public void subscribeSensor(Sensor sensor, int... pins) {
        subscribedSensors.put(Mathf.allPos(pins), sensor);
        for (int pin : pins) {
            subscribeToPin(pin);
        }
    }

    protected void setupReadThread() {
        Thread thread = new Thread(
                () -> {
                    while (true) {
                        byte[] data = read(16);
                        if (data != null) {
                            if (data[0] == (byte) 0xA1 || data[0] == (byte) 0xA3) {
                                byte rpin = data[1];
                                double value = ByteBuffer.wrap(data, 2, Double.BYTES).getDouble();
                                byte device = data[2 + Double.BYTES];

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

    public void ping() {
        write(new byte[] {(byte) 0xFF, 0x00});
    }

    public void readPin(int index) {
        write(new byte[] {(byte) 0xA1, (byte) index, THIS_DEVICE_ADDRESS});
    }

    public void subscribeToPin(int index) {
        write(new byte[] {(byte) 0xA3, (byte) index, THIS_DEVICE_ADDRESS});
    }

    public void writeToPin(int index, double value) {
        byte[] payload = new byte[3 + Double.BYTES];
        payload[0] = (byte) 0xA2;
        payload[1] = (byte) index;

        ByteBuffer bbuf = ByteBuffer.allocate(Double.BYTES);
        bbuf.putDouble(value);

        System.arraycopy(bbuf.array(), 0, payload, 2, Double.BYTES);
        payload[2 + Double.BYTES] = THIS_DEVICE_ADDRESS;
        write(payload);
    }
}