package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.math.rotation.Rotation3d;

/**
 * A class representing a 3D vector.
 * */
public class Vector3d {
    private double x;
    private double y;
    private double z;

    /**
     * Creates a 3D zero vector
     * @return The zero vector
     * */
    public static Vector3d zero() {
        return new Vector3d(0, 0, 0);
    }

    /**
     * Creates a new 3D vector object.
     * @param x The x component of the vector.
     * @param y The y component of the vector.
     * @param z The z component of the vector.
     * */
    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Gets the x component of the vector.
     * @return The x component of the vector.
     * */
    public double getX() {
        return x;
    }

    /**
     * Sets the x component of the vector.
     * @param x The new x component of the vector.
     * */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets the y component of the vector.
     * @return The y component of the vector.
     * */
    public double getY() {
        return y;
    }

    /**
     * Sets the y component of the vector.
     * @param y The new y component of the vector.
     * */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Gets the z component of the vector.
     * @return The z component of the vector.
     * */
    public double getZ() {
        return z;
    }

    /**
     * Sets the z component of the vector.
     * @param z The new z component of the vector.
     * */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Adds another vector to this vector (and returns a new vector).
     * @param other The other vector to add to this vector.
     * @return The new vector that is the sum of this vector and the other vector.
     * */
    public Vector3d add(Vector3d other) {
        return new Vector3d(this.x + other.getX(), this.y + other.getY(), this.z + other.getZ());
    }

    /**
     * Subtracts another vector from this vector (and returns a new vector).
     * @param other The other vector to subtract from this vector.
     * @return The new vector that is the difference of this vector and the other vector.
     * */
    public Vector3d subtract(Vector3d other) {
        return new Vector3d(this.x - other.getX(), this.y - other.getY(), this.z - other.getZ());
    }

    /**
     * Scales the vector by a scalar (and returns a new vector).
     * @param scalar The scalar to scale the vector by.
     * @return The new vector that is the scaled version of this vector.
     * */
    public Vector3d scale(double scalar) {
        return new Vector3d(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    /**
     * Dot product of this vector and another vector.
     * @param other The other vector to dot this vector with.
     * @return The dot product of this vector and the other vector.
     * */
    public double dot(Vector3d other) {
        return this.x * other.getX() + this.y * other.getY() + this.z * other.getZ();
    }

    /**
     * Cross product of this vector and another vector (and returns a new vector).
     * @param other The other vector to cross this vector with.
     * @return The new vector that is the cross product of this vector and the other vector.
     * */
    public Vector3d cross(Vector3d other) {
        return new Vector3d(this.y * other.getZ() - this.z * other.getY(),
                            this.z * other.getX() - this.x * other.getZ(),
                            this.x * other.getY() - this.y * other.getX());
    }

    /**
     * Returns the magnitude of the vector.
     * @return The magnitude of the vector.
     * */
    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    /**
     * Returns a new vector that is the normalized version of this vector.
     * @return The normalized version of this vector.
     * */
    public Vector3d normalize() {
        double magnitude = magnitude();
        return new Vector3d(this.x / magnitude, this.y / magnitude, this.z / magnitude);
    }

    /**
     * Gets the angle between this vector and another vector.
     * @param other The other vector to get the angle between.
     * @return The angle between this vector and the other vector.
     * */
    public double angle(Vector3d other) {
        return Math.acos(this.dot(other) / (this.magnitude() * other.magnitude()));
    }

    /**
     * Returns a new vector that's this rotated by the given Rotation3d.
     * @param rotation The Rotation3d to rotate the vector by.
     * @return The new vector that's this rotated by the given Rotation3d.
     * */
    public Vector3d rotateBy(Rotation3d rotation) {
        return new Vector3d(this.x * rotation.cos() - this.y * rotation.sin(),
                            this.x * rotation.sin() + this.y * rotation.cos(),
                            this.z);
    }

    /**
     * Converts the vector to a polar vector.
     * @return The polar vector representation of this vector.
     * */
    public Polar toPolar() {
        return new Polar(magnitude(), new Rotation2d(Math.atan2(y, x)));
    }

    /**
     * Converts the vector to a spherical vector.
     * @return The spherical vector representation of this vector.
     * */
    public Spherical toSpherical() {
        return new Spherical(magnitude(), new Rotation3d(Math.atan2(y, x), Math.atan2(z, Math.hypot(x, y))));
    }

    /**
     * Converts the vector to a 2D vector (x, y).
     * @return The 2D vector representation of this vector.
     * */
    public Vector2d toXY() {
        return new Vector2d(x, y);
    }

    /**
     * Converts the vector to a 2D vector (x, z).
     * @return The 2D vector representation of this vector.
     * */
    public Vector2d toXZ() {
        return new Vector2d(x, z);
    }

    /**
     * Converts the vector to a 2D vector (y, z).
     * @return The 2D vector representation of this vector.
     * */
    public Vector2d toYZ() {
        return new Vector2d(y, z);
    }

    /**
     * Returns a string representation of the vector.
     * @return The string representation of the vector.
     * */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
