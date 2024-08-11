package me.autobot.lib.math.coordinates;

import java.util.ArrayList;

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

    public double signedDistance(Vector2d point) {
        double dx = Math.max(position.x - point.getX(), Math.max(0, point.getX() - (position.x + size.x)));
        double dy = Math.max(position.y - point.getY(), Math.max(0, point.getY() - (position.y + size.y)));
        return Math.sqrt(dx * dx + dy * dy);
    }

    //not raycasting, literally just checking a few points to see if they intersect
    public boolean intersectsRaySimple(Vector2d start, Vector2d end) {
        ArrayList<Vector2d> somePoints = new ArrayList<>();

        for (int t = 0; t < 10; t++) {
            somePoints.add(start.add(end.subtract(start).multiply(t / 10.0)));
        }

        for (Vector2d point : somePoints) {
            if (isInside(point.toInt2())) return true;
        }

        return false;
    }
}
