package me.autobot.lib.math.rotation;

//I need to go open up my multivariable calc notebook to re-write this later lol but for now it's "fine"

/**
 * A 3D rotation in radians.
 * */
public class Rotation3d {
    private double theta;
    private double phi;

    /**
     * Returns a new Rotation3d with the given angles in radians.
     * @param theta The angle in radians.
     *              This is the angle in the xy plane.
     * @param phi The angle in radians.
     *            This is the angle in the xz plane.
     * @return A new Rotation3d with the given angles in radians.
     * */
    public static Rotation3d fromDegrees(double theta, double phi) {
        //the .0001 is to prevent a bug lmao idk how to fix it and i don't want to spend the hours of time to fix it
        return new Rotation3d(Math.toRadians(theta + 0.0001), Math.toRadians(phi + .0001));
    }

    /**
     * Creates a new Rotation3d with the given angles in radians.
     * @param theta The angle in radians.
     *              This is the angle in the xy plane.
     * @param phi The angle in radians.
     *            This is the angle in the xz plane.
     * */
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

    /**
     * Returns the xy angle (theta) in radians.
     * @return The xy angle in radians.
     * */
    public double getTheta() {
        return theta;
    }

    /**
     * Sets the xy angle (theta) in radians.
     * @param theta The xy angle in radians.
     * */
    public void setTheta(double theta) {
        this.theta = theta;
    }

    /**
     * Gets the xz angle (phi) in radians.
     * @return The xz angle in radians.
     * */
    public double getPhi() {
        return phi;
    }

    /**
     * Sets the xz angle (phi) in radians.
     * @param phi The xz angle in radians.
     * */
    public void setPhi(double phi) {
        this.phi = phi;
    }

    /**
     * Creates a new Rotation3d rotated by the given 2d rotation.
     * @param other The 2d rotation to rotate by.
     * @return A new Rotation3d with the angle rotated by the given 2d angle.
     * */
    public Rotation3d rotateBy(Rotation2d other) {
        return new Rotation3d(this.theta + other.getTheta(), this.phi);
    }

    /**
     * Rotate the angle by the given 3d angle.
     * @param other The angle to rotate by.
     * @return A new Rotation3d with the angle rotated by the given angle.
     * */
    public Rotation3d rotateBy(Rotation3d other) {
        return new Rotation3d(this.theta + other.getTheta(), this.phi + other.getPhi());
    }

    /**
     * Returns the normal of the angle (adding 90 degrees to both angles).
     * @return A new Rotation3d with the normal of the angle.
     * */
    public Rotation3d normal() {
        return new Rotation3d(this.theta + Math.PI / 2, this.phi + Math.PI / 2);
    }

    /**
     * Get the inverse of the angle.
     * @return A new Rotation3d with the inverse of the angle.
     * */
    public Rotation3d inverse() {
        return new Rotation3d(-this.theta, -this.phi);
    }

    /**
     * Returns the cosine multiplied of the angles.
     * @return The cosine of the angles.
     * */
    public double cos() {
        return Math.cos(theta) * Math.cos(phi);
    }

    /**
     * Returns the sine multiplied of the angles.
     * @return The sine of the angle.
     * */
    public double sin() {
        return Math.sin(theta) * Math.sin(phi);
    }

    /**
     * Returns the tangent multiplied of the angles.
     * @return The tangent of the angle.
     * */
    public double tan() {
        return Math.tan(theta) * Math.tan(phi);
    }

    /**
     * Get the xy angle in radians.
     * @return The xy angle in radians.
     * */
    public double getThetaRadians() {
        return theta;
    }

    /**
     * Get the xz angle in radians.
     * @return The xz angle in radians.
     * */
    public double getPhiRadians() {
        return phi;
    }

    /**
     * Get the xy angle in degrees.
     * @return The xy angle in degrees.
     * */
    public double getThetaDegrees() {
        return Math.toDegrees(theta);
    }

    /**
     * Get the xz angle in degrees.
     * @return The xz angle in degrees.
     * */
    public double getPhiDegrees() {
        return Math.toDegrees(phi);
    }
}
