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

        if (timeElapsed < 5000) {
            servo.setSpeed(0.5);
        } else {
            servo.setSpeed(1);
        }
    }
}
