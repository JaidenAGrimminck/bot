package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.rotation.Rotation2d;

public class Vector2d {
    private double x;
    private double y;

    public static Vector2d zero() {
        return new Vector2d(0, 0);
    }

    public static Vector2d fromPolar(double magnitude, Rotation2d rotation) {
        return new Vector2d(magnitude * rotation.cos(), magnitude * rotation.sin());
    }
    
    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
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

    public Vector2d multiply(double scalar) {
        return scale(scalar);
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d(this.x + other.getX(), this.y + other.getY());
    }

    public Vector2d subtract(Vector2d other) {
        return new Vector2d(this.x - other.getX(), this.y - other.getY());
    }

    public Vector2d scale(double scalar) {
        return new Vector2d(this.x * scalar, this.y * scalar);
    }

    public double dot(Vector2d other) {
        return this.x * other.getX() + this.y * other.getY();
    }

    public double cross(Vector2d other) {
        return this.x * other.getY() - this.y * other.getX();
    }

    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public Vector2d rotate(Rotation2d rotation) {
        return new Vector2d(this.x * rotation.cos() - this.y * rotation.sin(), this.x * rotation.sin() + this.y * rotation.cos());
    }

    public Vector2d normalize() {
        double magnitude = magnitude();
        return new Vector2d(this.x / magnitude, this.y / magnitude);
    }

    public double angle(Vector2d other) {
        return Math.acos(this.dot(other) / (this.magnitude() * other.magnitude()));
    }

    public Rotation2d angle() {
        return Rotation2d.fromRadians(Math.atan2(this.y, this.x));
    }

    public double distance(Vector2d other) {
        return this.subtract(other).magnitude();
    }

    public Int2 toInt2() {
        return new Int2((int) x, (int) y);
    }

    public Vector2d perpendicular() {
        return new Vector2d(-y, x);
    }

    @Override
    public String toString() {
        return "Vector2d{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
