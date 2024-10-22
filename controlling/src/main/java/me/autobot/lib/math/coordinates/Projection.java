package me.autobot.lib.math.coordinates;

/**
 * A projection of a shape onto an axis.
 * */
public class Projection {
    /**
     * The minimum and maximum values of the projection.
     * */
    public double min, max;

    /**
     * Creates a new projection with the given min and max values.
     * @param min The minimum value of the projection.
     *            This value must be less than or equal to max.
     * @param max The maximum value of the projection.
     *            This value must be greater than or equal to min.
     * */
    public Projection(double min, double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Checks if this projection overlaps with another projection
     * @param other The other projection to check for overlap
     *              with this projection.
     * @return True if the projections overlap, false otherwise.
     * */
    public boolean overlaps(Projection other) {
        return this.max >= other.min && other.max >= this.min;
    }
}
