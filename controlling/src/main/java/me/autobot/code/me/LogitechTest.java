package me.autobot.code.me;

import me.autobot.lib.controls.LogitechF310;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;

/**
 * Robot used for testing the LogitechF310 gamepad.
 * */
@PlayableRobot(name = "Logitech Robot")
public class LogitechTest extends Robot {
    private LogitechF310 gamepad;

    /**
     * Sets up the robot.
     * */
    @Override
    protected void setup() {
        gamepad = new LogitechF310("/gamepad1");
    }

    /**
     * Loops the robot.
     * */
    @Override
    protected void loop() {
        if (!clock().elapsedSince(1000)) {
            return;
        }

        double leftx = gamepad.getLeftX();
        double lefty = gamepad.getLeftY();
        double rightx = gamepad.getRightX();
        double righty = gamepad.getRightY();

        System.out.println("LEFT: (" + leftx + ", " + lefty + "), RIGHT: (" + rightx + ", " + righty + "), Last Updated: " + gamepad.getLastUpdated());
    }

    /**
     * On the robot stop.
     * */
    @Override
    protected void stop() {

    }
}
