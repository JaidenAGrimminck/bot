package me.autobot.code;

import me.autobot.lib.robot.Robot;
import me.autobot.lib.robot.motors.Servo;

public class ServoBot extends Robot {
    private Servo servo;

    @Override
    protected void setup() {
        servo = new Servo(0x12);

        getDevices().forEach(device -> {
            device.setParent(this);
        });

        servo.connectToI2C(8);
    }

    double t = 0;

    @Override
    protected void loop() {
        if (clock().elapsedSince(100)) {
            servo.setSpeed(Math.sin(t));

            t += 0.01;
        }
    }
}
