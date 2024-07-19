package me.autobot.lib.math.coordinates;

public class Int2 {
    public int x;
    public int y;

    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Int2 other) {
        return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
    }
}
