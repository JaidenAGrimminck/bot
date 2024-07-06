package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.math.rotation.Rotation3d;

public class Vector3d {
    private double x;
    private double y;
    private double z;

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Vector3d add(Vector3d other) {
        return new Vector3d(this.x + other.getX(), this.y + other.getY(), this.z + other.getZ());
    }

    public Vector3d subtract(Vector3d other) {
        return new Vector3d(this.x - other.getX(), this.y - other.getY(), this.z - other.getZ());
    }

    public Vector3d scale(double scalar) {
        return new Vector3d(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public double dot(Vector3d other) {
        return this.x * other.getX() + this.y * other.getY() + this.z * other.getZ();
    }

    public Vector3d cross(Vector3d other) {
        return new Vector3d(this.y * other.getZ() - this.z * other.getY(),
                            this.z * other.getX() - this.x * other.getZ(),
                            this.x * other.getY() - this.y * other.getX());
    }

    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public Vector3d normalize() {
        double magnitude = magnitude();
        return new Vector3d(this.x / magnitude, this.y / magnitude, this.z / magnitude);
    }

    public double angle(Vector3d other) {
        return Math.acos(this.dot(other) / (this.magnitude() * other.magnitude()));
    }

    public Vector3d rotateBy(Rotation3d rotation) {
        return new Vector3d(this.x * rotation.cos() - this.y * rotation.sin(),
                            this.x * rotation.sin() + this.y * rotation.cos(),
                            this.z);
    }

    public Polar toPolar() {
        return new Polar(magnitude(), new Rotation2d(Math.atan2(y, x)));
    }

    public Spherical toSpherical() {
        return new Spherical(magnitude(), new Rotation3d(Math.atan2(y, x), Math.atan2(z, Math.hypot(x, y))));
    }
}
