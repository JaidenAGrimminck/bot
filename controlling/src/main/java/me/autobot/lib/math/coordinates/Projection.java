package me.autobot.lib.math.coordinates;

public class Projection {
    public double min, max;

    public Projection(double min, double max) {
        this.min = min;
        this.max = max;
    }

    // Checks if this projection overlaps with another projection
    public boolean overlaps(Projection other) {
        return this.max >= other.min && other.max >= this.min;
    }
}
