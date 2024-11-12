package me.autobot.lib.robot.drivebase;

import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.robot.Motor;

/**
 * A drivebase that uses tank drive
 * */
public class TankDrive {
    private Motor topLeft;
    private Motor topRight;
    private Motor bottomLeft;
    private Motor bottomRight;

    /**
     * Creates a new TankDrive object.
     * @param topLeft The top left motor.
     *                This motor is on the top left of the robot.
     * @param topRight The top right motor.
     *                 This motor is on the top right of the robot.
     * @param bottomLeft The bottom left motor.
     *                  This motor is on the bottom left of the robot.
     * @param bottomRight The bottom right motor.
     *                  This motor is on the bottom right of the robot.
     * @see Motor
     * */
    public TankDrive(Motor topLeft, Motor topRight, Motor bottomLeft, Motor bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    /**
     * Drives the robot with the given speed and rotation.
     * @param leftSide The speed of the left side of the robot.
     *                 Positive is forward, negative is backward.
     * @param rightSide The speed of the right side of the robot.
     *                  Positive is forward, negative is backward.
     * */
    public void drive(double leftSide, double rightSide) {
        topLeft.setSpeed(leftSide);
        topRight.setSpeed(rightSide);
        bottomLeft.setSpeed(leftSide);
        bottomRight.setSpeed(rightSide);
    }
}
