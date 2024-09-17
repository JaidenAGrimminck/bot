package me.autobot.lib.math.rotation;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.coordinates.Vector3d;

public class Rotation2d {
    private double theta;

    /**
     * Create a new Rotation2d with an angle of 0.
     */
    public Rotation2d() {
        this.theta = 0;
    }

    /**
     * Create a new Rotation2d with the given angle in radians.
     * @param theta The angle in radians.
     */
    public Rotation2d(double theta) {
        this.theta = theta;

        normalize();
    }

    private void normalize() {
        this.theta = Math.IEEEremainder(this.theta, 2 * Math.PI);
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public Rotation2d rotateBy(Rotation2d other) {
        return new Rotation2d(this.theta + other.theta);
    }

    public Rotation2d normal() {
        return new Rotation2d(this.theta + Math.PI / 2);
    }

    public Rotation2d inverse() {
        return new Rotation2d(-this.theta);
    }

    public double cos() {
        return Mathf.cos(theta);
    }

    public double sin() {
        return Mathf.sin(theta);
    }

    public double tan() {
        return Math.tan(theta);
    }

    public double getRadians() {
        return theta;
    }

    public double getDegrees() {
        return Math.toDegrees(theta);
    }

    public Vector2d toVector() {
        return new Vector2d(Math.cos(theta), Math.sin(theta));
    }

    public Vector3d toVector3d() {
        return new Vector3d(Math.cos(theta), Math.sin(theta), 0);
    }

    /**
     * Create a new Rotation2d with the given degrees.
     * @param degrees The angle in degrees.
     * @return A new Rotation2d with the given angle in degrees.
     */
    public static Rotation2d fromDegrees(double degrees) {
        //ignore the 0.00001, it's just to prevent any potential issues....
        return new Rotation2d(Math.toRadians(degrees + 0.00001));
    }

    /**
     * Create a new Rotation2d with the given radians.
     * @param radians The angle in radians.
     * @return A new Rotation2d with the given angle in radians.
     */
    public static Rotation2d fromRadians(double radians) {
        return new Rotation2d(radians);
    }

    /**
     * Create a new Rotation2d with an angle of 0.
     * @return A new Rotation2d with an angle of 0.
     */
    public static Rotation2d zero() {
        return new Rotation2d();
    }
}
