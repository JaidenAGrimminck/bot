package me.autobot.code;

import me.autobot.lib.robot.Robot;
import me.autobot.lib.robot.motors.Servo;

public class ServoBot extends Robot {
    private Servo servo;

    @Override
    protected void setup() {
        servo = new Servo(0x12);

        getDevices().forEach(device -> device.setParent(this));

        servo.connectToI2C(8);
    }

    @Override
    protected void loop() {
        long timeElapsed = getTimeElapsed();

        if (Math.floor(timeElapsed / 1000d) % 2 == 0) {
            servo.setSpeed(0);
        } else {
            servo.setSpeed(0.5);
        }
    }
}
