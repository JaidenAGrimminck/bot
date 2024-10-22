package me.autobot.lib.math.rotation;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.coordinates.Vector3d;

/**
 * A 2D rotation in radians.
 * */
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

    /**
     * Normalize the angle to be between 0 and 2pi.
     * */
    private void normalize() {
        this.theta = Math.IEEEremainder(this.theta, 2 * Math.PI);
    }

    /**
     * Returns the angle in radians.
     * @return The angle in radians.
     * */
    public double getTheta() {
        return theta;
    }

    /**
     * Sets the angle in radians.
     * @param theta The angle in radians.
     * */
    public void setTheta(double theta) {
        this.theta = theta;
    }

    /**
     * Rotate the angle by the given angle.
     * @param other The angle to rotate by.
     * @return A new Rotation2d with the angle rotated by the given angle.
     * */
    public Rotation2d rotateBy(Rotation2d other) {
        return new Rotation2d(this.theta + other.theta);
    }

    /**
     * Returns the normal of the angle (adding 90 degrees).
     * @return A new Rotation2d with the angle normal to the current angle.
     * */
    public Rotation2d normal() {
        return new Rotation2d(this.theta + Math.PI / 2);
    }

    /**
     * Returns the inverse of the angle.
     * @return A new Rotation2d with the angle inverted.
     * */
    public Rotation2d inverse() {
        return new Rotation2d(-this.theta);
    }

    /**
     * Returns the cosine of the angle.
     * @return The cosine of the angle.
     * */
    public double cos() {
        return Mathf.cos(theta);
    }

    /**
     * Returns the sine of the angle.
     * @return The sine of the angle.
     * */
    public double sin() {
        return Mathf.sin(theta);
    }

    /**
     * Returns the tangent of the angle.
     * @return The tangent of the angle.
     * */
    public double tan() {
        return Math.tan(theta);
    }

    /**
     * Returns the angle in radians.
     * @return The angle in radians.
     * */
    public double getRadians() {
        return theta;
    }

    /**
     * Returns the angle in degrees.
     * @return The angle in degrees.
     * */
    public double getDegrees() {
        return Math.toDegrees(theta);
    }

    /**
     * Converts the angle to a vector (unit vector).
     * @return A new Vector2d equivalent to the angle / in the direction of the angle.
     */
    public Vector2d toVector() {
        return new Vector2d(Math.cos(theta), Math.sin(theta));
    }

    /**
     * Converts the angle to a 3D vector (unit vector).
     * @return A new Vector3d equivalent to the angle / in the direction of the angle, but z=0.
     * */
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

    /**
     * Clone the Rotation2d.
     * @return A new Rotation2d with the same angle as the current one.
     * */
    public Rotation2d clone() {
        return new Rotation2d(this.theta);
    }
}
