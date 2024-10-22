package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.rotation.Rotation2d;

/**
 * Polar coordinates represent a vector by its magnitude and the angle it makes with the x-axis.
 * */
public class Polar {
    private double r;
    
    private Rotation2d theta;

    /**
     * Constructs a Polar object with the given magnitude and angle.
     * @param r The magnitude of the vector.
     *          This is the distance from the origin to the point.
     * @param theta The angle of the vector.
     *              This is the angle the vector makes with the x-axis.
     * */
    public Polar(double r, Rotation2d theta) {
        this.r = r;
        this.theta = theta;
    }

    /**
     * Returns the magnitude of the vector (the radius value).
     * @return The magnitude of the vector.
     * */
    public double getR() {
        return r;
    }

    /**
     * Sets the magnitude of the vector (the radius value).
     * @param r The new magnitude of the vector.
     * */
    public void setR(double r) {
        this.r = r;
    }

    /**
     * Returns the angle of the vector.
     * @return The angle of the vector.
     * */
    public Rotation2d getTheta() {
        return theta;
    }

    /**
     * Sets the angle of the vector.
     * @param theta The new angle of the vector.
     * */
    public void setTheta(Rotation2d theta) {
        this.theta = theta;
    }

    /**
     * Rotates the vector by the given angle.
     * @param other The angle by which to rotate the vector.
     * @return The vector rotated by the given angle.
     * */
    public Polar rotateBy(Rotation2d other) {
        return new Polar(this.r, this.theta.rotateBy(other));
    }

    /**
     * Gets the normal of the vector, with the same magnitude (this implies a 90 degree rotation).
     * @return The normal of the vector.
     * */
    public Polar normal() {
        return new Polar(this.r, this.theta.normal());
    }

    /**
     * Returns the inverse of the vector, with the same magnitude and the opposite angle.
     * @return The inverse of the vector.
     * */
    public Polar inverse() {
        return new Polar(this.r, this.theta.inverse());
    }

    /**
     * Returns the x-component of the vector, calculated as r * cos(theta).
     * @return The x-component of the vector (cos(theta) * r).
     * */
    public double cos() {
        return Math.cos(theta.getTheta()) * r;
    }

    /**
     * Returns the y-component of the vector, calculated as r * sin(theta).
     * @return The y-component of the vector (sin(theta) * r).
     * */
    public double sin() {
        return Math.sin(theta.getTheta()) * r;
    }

    /**
     * Returns the tangent of the vector, calculated as sin(theta) / cos(theta).
     * @return The tangent of the vector (sin(theta) / cos(theta)).
     * */
    public double tan() {
        return Math.tan(theta.getTheta());
    }

    /**
     * Returns the angle of the vector in radians.
     * @return The angle of the vector in radians.
     */
    public double getRadians() {
        return theta.getTheta();
    }

    /**
     * Returns the angle of the vector in degrees.
     * @return The angle of the vector in degrees.
     */
    public double getDegrees() {
        return Math.toDegrees(theta.getTheta());
    }

    /**
     * Converts the Polar object to a Vector2d object.
     * @return The Vector2d object equivalent to the Polar object.
     * */
    public Vector2d toVector() {
        return new Vector2d(r * Math.cos(theta.getTheta()), r * Math.sin(theta.getTheta()));
    }
}
