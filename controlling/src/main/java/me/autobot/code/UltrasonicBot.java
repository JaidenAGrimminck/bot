package me.autobot.code;

import me.autobot.lib.math.Unit;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.robot.sensors.UltrasonicSensor;

/**
 * A robot that uses an ultrasonic sensor.
 * */
public class UltrasonicBot extends Robot {

    /**
     * Creates a new UltrasonicBot
     * Not necessary needed, but it's here for the sake of completeness.
     * */
    public UltrasonicBot() {}

    private UltrasonicSensor sensor;
    private UltrasonicSensor sensor2;
    private UltrasonicSensor sensor3;

    /**
     * Sets up the robot.
     * */
    @Override
    protected void setup() {
        sensor = new UltrasonicSensor(1, 0x12);
        sensor2 = new UltrasonicSensor(2, 0x12);
        sensor3 = new UltrasonicSensor(3, 0x12);

        registerAllDevices();

        sensor.connectToSerial(6, "/dev/cu.usbmodem1101");
        sensor2.connectToSerial(8, "/dev/cu.usbmodem1101");
        sensor3.connectToSerial(10, "/dev/cu.usbmodem1101");
    }

    /**
     * The main loop of the robot.
     * */
    @Override
    protected void loop() {
        if (!clock().elapsed(2000)) return;
        System.out.println("Distance 1: " + sensor.getDistance().getValue(Unit.Type.CENTIMETER));
        System.out.println("Distance 2: " + sensor2.getDistance().getValue(Unit.Type.CENTIMETER));
        System.out.println("Distance 3: " + sensor3.getDistance().getValue(Unit.Type.CENTIMETER));

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
