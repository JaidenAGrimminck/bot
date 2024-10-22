package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.math.rotation.Rotation3d;

/**
 * A class representing a 3D spherical coordinate system.
 * */
public class Spherical {
    private double rho;

    private Rotation3d angle;

    /**
     * Create a new Spherical with a radius of 0 and an angle of 0 (theta and phi).
     * @param rho The radius.
     * @param angle The angles in radians.
     */
    public Spherical(double rho, Rotation3d angle) {
        this.rho = rho;
        this.angle = angle;
    }

    /**
     * Create a new Spherical with the given radius and angles in radians.
     * @param rho The radius.
     * @param theta The angle of theta (the ground plane angle), [0, 2\pi).
     * @param phi The angle of phi (the vertical plane angle), [0, \pi).
     */
    public Spherical(double rho, Rotation2d theta, Rotation2d phi) {
        this.rho = rho;
        this.angle = new Rotation3d(theta.getTheta(), phi.getTheta());
    }

    /**
     * Returns the value of rho.
     * @return The value of rho.
     * */
    public double getRho() {
        return rho;
    }

    /**
     * Sets the value of rho.
     * @param rho The new value of rho.
     * */
    public void setRho(double rho) {
        this.rho = rho;
    }

    /**
     * Gets the angle of the Spherical.
     * @return The angle of the Spherical.
     * */
    public Rotation3d getAngle() {
        return angle;
    }

    /**
     * Sets the angle of the Spherical.
     * @param angle The new angle of the Spherical.
     * */
    public void setAngle(Rotation3d angle) {
        this.angle = angle;
    }

    /**
     * Returns a new Spherical that's this rotated by the given Rotation3d.
     * @param other The Rotation3d to rotate the Spherical by.
     * @return The new Spherical that's this rotated by the given Rotation3d.
     * */
    public Spherical rotateBy(Rotation3d other) {
        return new Spherical(this.rho, this.angle.rotateBy(other));
    }

    /**
     * Returns the normal of the Spherical.
     * @return The normal of the Spherical.
     * */
    public Spherical normal() {
        return new Spherical(this.rho, this.angle.normal());
    }

    /**
     * Returns the inverse of the Spherical.
     * @return The inverse of the Spherical.
     * */
    public Spherical inverse() {
        return new Spherical(this.rho, this.angle.inverse());
    }

    /**
     * Returns the cosine of the theta angle.
     * @return The cosine of the theta angle.
     * */
    public double cosTheta() {
        return Math.cos(angle.getTheta());
    }

    /**
     * Returns the sine of the theta angle.
     * @return The sine of the theta angle.
     * */
    public double sinTheta() {
        return Math.sin(angle.getTheta());
    }

    /**
     * Returns the cosine of the phi angle.
     * @return The cosine of the phi angle.
     * */
    public double cosPhi() {
        return Math.cos(angle.getPhi());
    }

    /**
     * Returns the sine of the phi angle.
     * @return The sine of the phi angle.
     * */
    public double sinPhi() {
        return Math.sin(angle.getPhi());
    }

    /**
     * Returns the theta angle in radians.
     * @return The theta angle in radians.
     * */
    public double getTheta() {
        return angle.getTheta();
    }

    /**
     * Returns the phi angle in radians.
     * @return The phi angle in radians.
     * */
    public double getPhi() {
        return angle.getPhi();
    }

    /**
     * Converts the Spherical to a Vector3d.
     * @return The Vector3d representation of the Spherical.
     * */
    public Vector3d toVector() {
        return new Vector3d(rho * sinPhi() * cosTheta(), rho * sinPhi() * sinTheta(), rho * cosPhi());
    }

    /**
     * Flattens and converts the Spherical to a Vector2d.
     * @return The Vector2d representation of the Spherical.
     * */
    public Vector2d toVector2d() {
        return new Vector2d(rho * sinPhi() * cosTheta(), rho * sinPhi() * sinTheta());
    }
}
