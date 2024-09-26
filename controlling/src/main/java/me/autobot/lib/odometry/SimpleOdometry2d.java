package me.autobot.lib.odometry;

import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;

public class SimpleOdometry2d {
    private Vector2d startPosition;
    private Vector2d position;

    private Rotation2d startRotation;
    private Rotation2d rotation;


    /**
     * Creates an empty odometry object with a position and rotation of 0.
     * */
    public SimpleOdometry2d() {
        this(Vector2d.zero(), Rotation2d.zero());
    }

    /**
     * Creates an odometry object with a given position and rotation.
     * */
    public SimpleOdometry2d(Vector2d position, Rotation2d rotation) {
        this.position = position;
        this.rotation = rotation;

        this.startPosition = Vector2d.zero();
        this.startRotation = Rotation2d.zero();
    }

    /**
     * Sets the starting position of the odometry object.
     * @param startPosition The starting position of the odometry object.
     *                      This is useful for resetting the odometry object.
     * @return The odometry object.
     * @see SimpleOdometry2d#reset()
     * */
    public SimpleOdometry2d setStartPosition(Vector2d startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    /**
     * Sets the starting rotation of the odometry object.
     * @param startRotation The starting rotation of the odometry object.
     *                      This is useful for resetting the odometry object.
     * @return The odometry object.
     * @see SimpleOdometry2d#reset()
     * */
    public SimpleOdometry2d setStartRotation(Rotation2d startRotation) {
        this.startRotation = startRotation;
        return this;
    }

    /**
     * Resets the odometry object to the starting position and rotation.
     * @return The odometry object.
     * */
    public SimpleOdometry2d reset() {
        position = startPosition.clone();
        rotation = startRotation.clone();
        return this;
    }

    /**
     * Gets the current position of the odometry object.
     * */
    public Vector2d getPosition() {
        return position;
    }

    /**
     * Gets the current rotation of the odometry object.
     * */
    public Rotation2d getRotation() {
        return rotation;
    }

    /**
     * Rotates the odometry object by a given rotation.
     * @param rotation The rotation to rotate the odometry object by.
     * */
    public void rotate(Rotation2d rotation) {
        this.rotation = this.rotation.rotateBy(rotation);
    }

    /**
     * Moves the odometry object by a given movement.
     * @param movement The movement to move the odometry object by.
     * */
    public void move(Vector2d movement) {
        position = position.add(movement);
    }
}
