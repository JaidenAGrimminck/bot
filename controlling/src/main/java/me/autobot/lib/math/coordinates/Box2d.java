package me.autobot.lib.math.coordinates;

public class Box2d {

    Int2 position;
    Int2 size;

    public Box2d(int x, int y, int width, int height) {
        this.position = new Int2(x, y);
        this.size = new Int2(width, height);
    }

    public Box2d(Int2 position, Int2 size) {
        this.position = position;
        this.size = size;
    }

    public boolean isInside(Int2 point) {
        return point.x >= position.x && point.x <= position.x + size.x &&
                point.y >= position.y && point.y <= position.y + size.y;
    }

    public Int2 getPosition() {
        return position;
    }

    public Int2 getSize() {
        return size;
    }
}
