package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.rotation.Rotation2d;

public class Polar {
    private double r;
    
    private Rotation2d theta;

    public Polar(double r, Rotation2d theta) {
        this.r = r;
        this.theta = theta;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public Rotation2d getTheta() {
        return theta;
    }

    public void setTheta(Rotation2d theta) {
        this.theta = theta;
    }

    public Polar rotateBy(Rotation2d other) {
        return new Polar(this.r, this.theta.rotateBy(other));
    }

    public Polar normal() {
        return new Polar(this.r, this.theta.normal());
    }

    public Polar inverse() {
        return new Polar(this.r, this.theta.inverse());
    }

    public double cos() {
        return Math.cos(theta.getTheta()) * r;
    }

    public double sin() {
        return Math.sin(theta.getTheta()) * r;
    }

    public double tan() {
        return Math.tan(theta.getTheta()) * r;
    }

    public double getRadians() {
        return theta.getTheta();
    }

    public double getDegrees() {
        return Math.toDegrees(theta.getTheta());
    }

    //to vector
    public Vector2d toVector() {
        return new Vector2d(r * Math.cos(theta.getTheta()), r * Math.sin(theta.getTheta()));
    }
}
