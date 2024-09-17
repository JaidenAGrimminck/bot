package me.autobot.lib.math;

import kotlin.Pair;
import me.autobot.lib.math.coordinates.Projection;
import me.autobot.lib.math.coordinates.Vector2d;

import java.awt.*;

public class Geometry {

    public static boolean twoPolygonsIntersecting(Vector2d[] polygonA, Vector2d[] polygonB) {
        return isSeparatingAxisTheorem(polygonA, polygonB) && isSeparatingAxisTheorem(polygonB, polygonA);
    }

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

    // Projects a polygon onto an axis and returns the projection range
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
