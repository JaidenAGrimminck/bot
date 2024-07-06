package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.math.rotation.Rotation3d;

public class Spherical {
    private double rho;

    private Rotation3d angle;

    /**
     * Create a new Spherical with a radius of 0 and an angle of 0 (theta and phi).
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

    public double getRho() {
        return rho;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public Rotation3d getAngle() {
        return angle;
    }

    public void setAngle(Rotation3d angle) {
        this.angle = angle;
    }

    public Spherical rotateBy(Rotation3d other) {
        return new Spherical(this.rho, this.angle.rotateBy(other));
    }

    public Spherical normal() {
        return new Spherical(this.rho, this.angle.normal());
    }

    public Spherical inverse() {
        return new Spherical(this.rho, this.angle.inverse());
    }

    public double cosTheta() {
        return Math.cos(angle.getTheta());
    }

    public double sinTheta() {
        return Math.sin(angle.getTheta());
    }

    public double cosPhi() {
        return Math.cos(angle.getPhi());
    }

    public double sinPhi() {
        return Math.sin(angle.getPhi());
    }

    public double getTheta() {
        return angle.getTheta();
    }

    public double getPhi() {
        return angle.getPhi();
    }

    public Vector3d toVector() {
        return new Vector3d(rho * sinPhi() * cosTheta(), rho * sinPhi() * sinTheta(), rho * cosPhi());
    }

    public Vector2d toVector2d() {
        return new Vector2d(rho * sinPhi() * cosTheta(), rho * sinPhi() * sinTheta());
    }
}
