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

    /**
     * Sets up the robot.
     * */
    @Override
    protected void setup() {
        sensor = new UltrasonicSensor(1, 0x12);

        registerAllDevices();

        sensor.connectToI2C(6);
    }

    /**
     * The main loop of the robot.
     * */
    @Override
    protected void loop() {
        System.out.println("Distance: " + sensor.getDistance().getValue(Unit.Type.CENTIMETER));
    }
}
