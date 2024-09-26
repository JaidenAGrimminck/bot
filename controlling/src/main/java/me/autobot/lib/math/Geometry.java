package me.autobot.lib.math;

import kotlin.Pair;
import me.autobot.lib.math.coordinates.Projection;
import me.autobot.lib.math.coordinates.Vector2d;

import java.awt.*;

public class Geometry {

    /**
     * Checks if two polygons are intersecting
     * @param polygonA The first polygon
     *                 The polygon is represented as an array of Vector2d points
     *                 The points are in counter-clockwise order
     *                 The last point is connected to the first point
     *                 The polygon is assumed to be convex
     *                 The polygon is assumed to be simple (no self-intersections)
     * @param polygonB The second polygon
     *                 The polygon is represented as an array of Vector2d points
     *                 Same assumptions as polygonA
     * @return true if the polygons are intersecting, false otherwise
     * */
    public static boolean twoPolygonsIntersecting(Vector2d[] polygonA, Vector2d[] polygonB) {
        return isSeparatingAxisTheorem(polygonA, polygonB) && isSeparatingAxisTheorem(polygonB, polygonA);
    }

    /**
     * Checks if two polygons are intersecting using the Separating Axis Theorem
     * @param a The first polygon
     * @param b The second polygon
     * */
    public static boolean isSeparatingAxisTheorem(Vector2d[] a, Vector2d[] b) {
        for (int i = 0; i < a.length; i++) {
            Vector2d p1 = a[i];
            Vector2d p2 = a[(i + 1) % a.length];

            Vector2d edge = p2.subtract(p1);

            Vector2d axis = edge.perpendicular();

            // Project both polygons onto the axis
            Projection projA = projectPolygon(a, axis);
            Projection projB = projectPolygon(b, axis);

            // Check if there is an overlap on this axis
            if (!projA.overlaps(projB)) {
                return false;  // No overlap means a separating axis is found
            }
        }

        return true;
    }

    /**
     * Projects a polygon onto an axis and returns the projection range
     * @param polygon The polygon to project
     *                The polygon is represented as an array of Vector2d points
     * @param axis The axis to project the polygon onto
     *             The axis is represented as a Vector2d
     * @return The projection range
     * */
    public static Projection projectPolygon(Vector2d[] polygon, Vector2d axis) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (Vector2d point : polygon) {
            double projection = point.dot(axis);
            min = Math.min(min, projection);
            max = Math.max(max, projection);
        }

        return new Projection(min, max);
    }
}
