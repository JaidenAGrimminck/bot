package me.autobot.lib.math.coordinates;

import me.autobot.lib.math.Mathf;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class representing a 2D box.
 * */
public class Box2d {
    Int2 position;
    Int2 size;

//    public boolean inRay = false;
//    public boolean inZone = false;

    /**
     * All flags on the robot.
     * */
    public HashMap<String, Boolean> flags = new HashMap<>();

    /**
     * Creates a new 2D box object.
     * @param x The x position of the box.
     * @param y The y position of the box.
     * @param width The width of the box.
     * @param height The height of the box.
     * */
    public Box2d(int x, int y, int width, int height) {
        this.position = new Int2(x, y);
        this.size = new Int2(width, height);
    }

    /**
     * Creates a new 2D box object.
     * @param position The position of the box.
     * @param size The size of the box.
     * */
    public Box2d(Int2 position, Int2 size) {
        this.position = position;
        this.size = size;
    }

    /**
     * Checks if the given point is inside the box.
     * @param point The point to check.
     * @return Whether the point is inside the box.
     * */
    public boolean isInside(Int2 point) {
        return point.x >= position.x && point.x <= position.x + size.x &&
                point.y >= position.y && point.y <= position.y + size.y;
    }

    /**
     * Returns the position of the box.
     * @return The position of the box.
     * */
    public Int2 getPosition() {
        return position;
    }

    /**
     * Returns the size of the box.
     * @return The size of the box.
     * */
    public Int2 getSize() {
        return size;
    }

    //https://stackoverflow.com/questions/30545052/calculate-signed-distance-between-point-and-rectangle
    /**
     * Returns the signed distance between the box and the given point.
     * @param uv The point to calculate the distance to.
     * @return The signed distance between the box and the given point.
     * */
    public double signedDistance(Vector2d uv) {
        Vector2d min = position.toVector2d();
        Vector2d max = position.add(size).toVector2d();

        return Mathf.distanceBoxParticle2D(uv.getX(), uv.getY(), min.getX(), min.getY(), max.getX(), max.getY());
    }

    /**
     * Figures out the distance between the box and a point looking in a certain direction.
     * @param pos The position to start from.
     * @param to The position to look at.
     * @return The distance between the box and the point.
     * */
    public double raycastDistance(Vector2d pos, Vector2d to) {
        if (!intersectsRay(pos, to)) return Double.POSITIVE_INFINITY;

        Vector2d direction = to.subtract(pos);

        final double threshold = 1;
        final double rfac = 1;
        final int maxn = 100;

        double t = 0;

        double r = signedDistance(pos) * rfac;

        t += r;

        int n = 0;

        while (r > threshold && n++ < maxn) {
            Vector2d newPos = pos.add(direction.normalize().multiply(r));
            r = signedDistance(newPos) * rfac;

            t += r;

            pos = newPos;
        }

        if (n >= maxn) {
            return Double.POSITIVE_INFINITY;
        }

        return t;
    }

    /**
     * Checks if the box intersects with a ray.
     * @param start The start of the ray.
     * @param end The end of the ray.
     * @return Whether the box intersects with the ray.
     * */
    public boolean intersectsRay(Vector2d start, Vector2d end) {
        Vector2d direction = end.subtract(start);

        double t1 = (position.x - start.getX()) / direction.getX();
        double t2 = (position.x + size.x - start.getX()) / direction.getX();
        double t3 = (position.y - start.getY()) / direction.getY();
        double t4 = (position.y + size.y - start.getY()) / direction.getY();

        double tmin = Math.max(Math.min(t1, t2), Math.min(t3, t4));
        double tmax = Math.min(Math.max(t1, t2), Math.max(t3, t4));

        boolean i = !(tmax < 0 || tmin > tmax);

        //inRay = i;

        return i;
    }

    /**
     * Checks if the box intersects with a line
     * @param start The start of the line.
     * @param end The end of the line.
     * @return Whether the box intersects with the line.
     * */
    public boolean lineIntersects(Vector2d start, Vector2d end) {
        double slope = (end.getY() - start.getY()) / (end.getX() - start.getX());
        double yIntercept = start.getY() - slope * start.getX();

        //check if the line intersects with any of the four sides of the box
        //top
        if (start.getY() < position.y && end.getY() > position.y) {
            double x = (position.y - yIntercept) / slope;
            if (x >= position.x && x <= position.x + size.x) return true;
        }

        //bottom
        if (start.getY() > position.y + size.y && end.getY() < position.y + size.y) {
            double x = (position.y + size.y - yIntercept) / slope;
            if (x >= position.x && x <= position.x + size.x) return true;
        }

        //left
        if (start.getX() < position.x && end.getX() > position.x) {
            double y = slope * position.x + yIntercept;
            if (y >= position.y && y <= position.y + size.y) return true;
        }

        //right
        if (start.getX() > position.x + size.x && end.getX() < position.x + size.x) {
            double y = slope * (position.x + size.x) + yIntercept;
            if (y >= position.y && y <= position.y + size.y) return true;
        }

        return false;
    }

    /**
     * Gets the vertices of the box.
     * @return The vertices of the box.
     * */
    public Vector2d[] getVertices() {
        return new Vector2d[] {
                position.toVector2d(),
                position.add(new Int2(size.x, 0)).toVector2d(),
                position.add(size).toVector2d(),
                position.add(new Int2(0, size.y)).toVector2d()
        };
    }
}
