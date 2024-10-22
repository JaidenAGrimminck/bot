package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.rotation.Rotation2d;

/**
 * A class representing a 2D vector.
 * */
public class Vector2d {
    private double x;
    private double y;

    /**
     * The zero vector. Equivalent to (0, 0).
     * @return The zero vector.
     * */
    public static Vector2d zero() {
        return new Vector2d(0, 0);
    }

    /**
     * Creates a new vector from polar coordinates.
     * @param magnitude The magnitude of the vector.
     * @param rotation The rotation of the vector.
     * @return The vector from polar coordinates.
     * */
    public static Vector2d fromPolar(double magnitude, Rotation2d rotation) {
        return new Vector2d(magnitude * rotation.cos(), magnitude * rotation.sin());
    }

    /**
     * Creates a new vector from cartesian coordinates.
     * @param x The x coordinate
     * @param y The y coordinate
     * */
    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x coordinate of the vector.
     * @return The x coordinate of the vector.
     * */
    public double getX() {
        return x;
    }

    /**
     * Sets the x coordinate of the vector.
     * @param x The new x coordinate of the vector.
     * */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets the y coordinate of the vector.
     * @return The y coordinate of the vector.
     * */
    public double getY() {
        return y;
    }

    /**
     * Sets the y coordinate of the vector.
     * @param y The new y coordinate of the vector.
     * */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Multiplies the vector by a scalar.
     * @param scalar The scalar to multiply the vector by.
     * @return The vector multiplied by the scalar.
     * */
    public Vector2d multiply(double scalar) {
        return scale(scalar);
    }

    /**
     * Adds two vectors together.
     * @param other The other vector to add to this vector.
     * @return The sum of the two vectors.
     */
    public Vector2d add(Vector2d other) {
        return new Vector2d(this.x + other.getX(), this.y + other.getY());
    }

    /**
     * Subtracts two vectors.
     * @param other The other vector to subtract from this vector.
     * @return The difference of the two vectors.
     * */
    public Vector2d subtract(Vector2d other) {
        return new Vector2d(this.x - other.getX(), this.y - other.getY());
    }

    /**
     * Scales the vector by a scalar. Equivalent to multiplying the vector by a scalar.
     * @param scalar The scalar to scale the vector by.
     * @return The scaled vector.
     * */
    public Vector2d scale(double scalar) {
        return new Vector2d(this.x * scalar, this.y * scalar);
    }

    /**
     * Gets the dot product of two vectors.
     * @param other The other vector to dot this vector with.
     * @return The dot product of the two vectors.
     * */
    public double dot(Vector2d other) {
        return this.x * other.getX() + this.y * other.getY();
    }

    /**
     * Gets the cross product of two vectors.
     * @param other The other vector to cross this vector with.
     * @return The cross product of the two vectors.
     * */
    public double cross(Vector2d other) {
        return this.x * other.getY() - this.y * other.getX();
    }

    /**
     * Gets the magnitude of the vector.
     * @return The magnitude of the vector.
     * */
    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    /**
     * Rotates the vector by a rotation.
     * @param rotation The rotation to rotate the vector by.
     * @return The rotated vector.
     * */
    public Vector2d rotate(Rotation2d rotation) {
        return new Vector2d(this.x * rotation.cos() - this.y * rotation.sin(), this.x * rotation.sin() + this.y * rotation.cos());
    }

    /**
     * Normalizes the vector.
     * @return The normalized vector.
     * */
    public Vector2d normalize() {
        double magnitude = magnitude();
        return new Vector2d(this.x / magnitude, this.y / magnitude);
    }

    /**
     * Gets the angle between two vectors.
     * @param other The other vector to get the angle between.
     * @return The angle between the two vectors.
     * */
    public double angle(Vector2d other) {
        return Math.acos(this.dot(other) / (this.magnitude() * other.magnitude()));
    }

    /**
     * Gets the angle of the vector.
     * @return The angle of the vector.
     * */
    public Rotation2d angle() {
        return Rotation2d.fromRadians(Math.atan2(this.y, this.x));
    }

    /**
     * Gets the distance between two vectors.
     * @param other The other vector to get the distance between.
     * @return The distance between the two vectors.
     * */
    public double distance(Vector2d other) {
        return this.subtract(other).magnitude();
    }

    /**
     * Converts the vector to a 2D integer vector.
     * @return The vector as a 2D integer vector.
     * */
    public Int2 toInt2() {
        return new Int2((int) x, (int) y);
    }

    /**
     * Gets the perpendicular vector of the vector.
     * @return The perpendicular vector of the vector.
     * */
    public Vector2d perpendicular() {
        return new Vector2d(-y, x);
    }

    /**
     * Gets the string representation of the vector.
     * */
    @Override
    public String toString() {
        return "Vector2d{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    /**
     * Clones the vector.
     * @return The cloned vector.
     * */
    public Vector2d clone() {
        return new Vector2d(x, y);
    }
}
