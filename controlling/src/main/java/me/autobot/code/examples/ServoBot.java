package me.autobot.code.examples;

import me.autobot.lib.robot.Robot;
import me.autobot.lib.robot.motors.Servo;

/**
 * A bot used to test the Servo class!
 * */
public class ServoBot extends Robot {
    private Servo servo;

    /**
     * Creates a new SimRobot
     * Not necessary needed, but it's here for the sake of completeness.
     * */
    public ServoBot() {
        super();
    }

    /**
     * Creates a new servo at address 0x12, then lets the i2c device know that the servo we're looking for is the one attached to the gpio pin 8.
     * */
    @Override
    protected void setup() {
        servo = new Servo(0x01, 0x12);

        getDevices().forEach(device -> {
            device.setParent(this);
        });

        servo.connectToI2C(8);
    }

    double t = 0;

    /**
     * Every 100ms, set the speed of the servo to sin(t).
     * */
    @Override
    protected void loop() {
        if (clock().elapsedSince(100)) {
            servo.setSpeed(Math.sin(t));

            t += 0.01;
        }
    }
}
