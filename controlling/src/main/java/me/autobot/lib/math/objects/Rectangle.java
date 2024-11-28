package me.autobot.lib.math.objects;

import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;

/**
 * Rectangle object with position, size, and rotation.
 * */
public class Rectangle {
    private Vector2d position;
    private Vector2d size;

    private Rotation2d rotation = new Rotation2d(0);

    /**
     * Creates a new rectangle with the given position and size.
     * @param position The position of the rectangle, from the center.
     * @param size The size of the rectangle.
     * */
    public Rectangle(Vector2d position, Vector2d size) {
        this.position = position;
        this.size = size;
    }

    /**
     * Creates a new rectangle with the given position, size, and rotation.
     * @param position The position of the rectangle, from the center.
     * @param size The size of the rectangle.
     * @param rotation The rotation of the rectangle.
     * */
    public Rectangle(Vector2d position, Vector2d size, Rotation2d rotation) {
        this.position = position;
        this.size = size;
        this.rotation = rotation;
    }

    /**
     * Gets the center position of the rectangle.
     * @return The center position of the rectangle.
     * */
    public Vector2d getPosition() {
        return position;
    }

    /**
     * Gets the size of the rectangle.
     * @return The size of the rectangle.
     * */
    public Vector2d getSize() {
        return size;
    }

    /**
     * Gets the rotation of the rectangle.
     * @return The rotation of the rectangle.
     * */
    public Rotation2d getRotation() {
        return rotation;
    }

    /**
     * Returns the vertices of the rectangle.
     * @return The vertices of the rectangle.
     *         0: Top right
     *         1: Bottom right
     *         2: Bottom left
     *         3: Top left
     * */
    public Vector2d[] getVertices() {
        Vector2d[] vertices = new Vector2d[4];

        Vector2d halfSize = size.scale(0.5);

        vertices[0] = new Vector2d(halfSize.getX(), halfSize.getY()).rotate(rotation).add(position);
        vertices[1] = new Vector2d(halfSize.getX(), -halfSize.getY()).rotate(rotation).add(position);
        vertices[2] = new Vector2d(-halfSize.getX(), -halfSize.getY()).rotate(rotation).add(position);
        vertices[3] = new Vector2d(-halfSize.getX(), halfSize.getY()).rotate(rotation).add(position);

        return vertices;
    }

}
