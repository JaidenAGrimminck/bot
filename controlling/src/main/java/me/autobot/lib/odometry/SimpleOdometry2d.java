package me.autobot.lib.odometry;

import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;

public class SimpleOdometry2d {
    private Vector2d startPosition;
    private Vector2d position;

    private Rotation2d startRotation;
    private Rotation2d rotation;

    public SimpleOdometry2d() {
        this(Vector2d.zero(), Rotation2d.zero());
    }

    public SimpleOdometry2d(Vector2d position, Rotation2d rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public SimpleOdometry2d setStartPosition(Vector2d startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    public SimpleOdometry2d setStartRotation(Rotation2d startRotation) {
        this.startRotation = startRotation;
        return this;
    }

    public SimpleOdometry2d reset() {
        position = startPosition.clone();
        rotation = startRotation.clone();
        return this;
    }

    public Vector2d getPosition() {
        return position;
    }

    public Rotation2d getRotation() {
        return rotation;
    }

    public void rotate(Rotation2d rotation) {
        this.rotation = this.rotation.rotateBy(rotation);
    }

    public void move(Vector2d movement) {
        position = position.add(movement);
    }
}
