package me.autobot.lib.robot.drivebase;

import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.robot.Motor;

/**
 * A drivebase that uses arcade drive.
 * */
public class ArcadeDrive {
    private Motor topLeft;
    private Motor topRight;
    private Motor bottomLeft;
    private Motor bottomRight;

    /**
     * Creates a new ArcadeDrive object.
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
    public ArcadeDrive(Motor topLeft, Motor topRight, Motor bottomLeft, Motor bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    /**
     * Drives the robot with the given speed and rotation.
     * @param speed The speed of the robot.
     *              Positive is forward, negative is backward.
     *              Range: -1 to 1.
     * @param rotation The rotation of the robot.
     *                 Positive is right, negative is left.
     * */
    public void drive(double speed, double rotation) {
        drive(speed, rotation, 1);
    }

    /**
     * Drives the robot with the given speed and rotation.
     * @param speed The speed of the robot.
     *              Positive is forward, negative is backward.
     *              Range: -1 to 1.
     * @param rotation The rotation of the robot.
     *                 Positive is right, negative is left.
     * @param multiplier The multiplier to apply to the motor speeds.
     * */
    public void drive(double speed, double rotation, double multiplier) {
        Vector2d motorSpeeds = arcadeDrive(speed, rotation);

        topLeft.setSpeed(motorSpeeds.getX() * multiplier);
        topRight.setSpeed(motorSpeeds.getY() * multiplier);
        bottomLeft.setSpeed(motorSpeeds.getX() * multiplier);
        bottomRight.setSpeed(motorSpeeds.getY() * multiplier);
    }

    /**
     * Translates arcade drive to motor speeds.
     * @param speed The speed of the robot.
     * @param rot The rotation of the robot.
     * @return The motor speeds. (x=left, y=right)
     * */
    protected Vector2d arcadeDrive(double speed, double rot) {
        double max = Math.max(Math.abs(speed), Math.abs(rot));

        double leftMotorSpeed;
        double rightMotorSpeed;

        double total = speed + rot;
        double diff = speed - rot;

        if (speed > 0.0) {
            if (rot > 0.0) {
                leftMotorSpeed = max;
                rightMotorSpeed = diff;
            } else {
                leftMotorSpeed = total;
                rightMotorSpeed = max;
            }
        } else {
            if (rot > 0.0) {
                leftMotorSpeed = total;
                rightMotorSpeed = -max;
            } else {
                leftMotorSpeed = -max;
                rightMotorSpeed = diff;
            }
        }

        return new Vector2d(leftMotorSpeed, rightMotorSpeed);
    }
}
