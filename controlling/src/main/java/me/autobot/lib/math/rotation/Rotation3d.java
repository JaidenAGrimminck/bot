package me.autobot.lib.math.rotation;

public class Rotation3d {
    private double theta;
    private double phi;

    public static Rotation3d fromDegrees(double theta, double phi) {
        return new Rotation3d(Math.toRadians(theta), Math.toRadians(phi));
    }

    public Rotation3d(double theta, double phi) {
        this.theta = theta;
        this.phi = phi;

        normalize();
    }

    /**
     * Normalize the angle for \theta and \phi.
     * \theta is normalized to the range [0, 2\pi).
     * \phi is normalized to the range [0, \pi).
     */
    private void normalize() {
        this.theta = Math.IEEEremainder(this.theta, 2 * Math.PI);
        this.phi = Math.IEEEremainder(this.phi, Math.PI);
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getPhi() {
        return phi;
    }

    public void setPhi(double phi) {
        this.phi = phi;
    }

    public Rotation3d rotateBy(Rotation2d other) {
        return new Rotation3d(this.theta + other.getTheta(), this.phi + other.getTheta());
    }

    public Rotation3d rotateBy(Rotation3d other) {
        return new Rotation3d(this.theta + other.getTheta(), this.phi + other.getPhi());
    }

    public Rotation3d normal() {
        return new Rotation3d(this.theta + Math.PI / 2, this.phi + Math.PI / 2);
    }

    public Rotation3d inverse() {
        return new Rotation3d(-this.theta, -this.phi);
    }

    public double cos() {
        return Math.cos(theta) * Math.cos(phi);
    }

    public double sin() {
        return Math.sin(theta) * Math.sin(phi);
    }

    public double tan() {
        return Math.tan(theta) * Math.tan(phi);
    }

    public double getThetaRadians() {
        return theta;
    }

    public double getPhiRadians() {
        return phi;
    }

    public double getThetaDegrees() {
        return Math.toDegrees(theta);
    }

    public double getPhiDegrees() {
        return Math.toDegrees(phi);
    }
}
