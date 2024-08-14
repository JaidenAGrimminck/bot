package me.autobot.lib.math;

import me.autobot.lib.math.coordinates.Vector2d;

public class Mathf {
    public static double clamp(double n, double min, double max) {
        return Math.max(min, Math.min(max, n));
    }

    public static double max(double ...v) {
        if (v.length == 0) return 0;

        double max = v[0];
        for (double n : v) {
            max = Math.max(max, n);
        }
        return max;
    }

    public static double min(double ...v) {
        if (v.length == 0) return 0;

        double min = v[0];
        for (double n : v) {
            min = Math.min(min, n);
        }
        return min;
    }

    public static double hypot(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    //https://codereview.stackexchange.com/questions/175566/compute-shortest-distance-between-point-and-a-rectangle
    public static double distanceBoxParticle2D(double x, double y, double x_min, double y_min,
                                 double x_max, double y_max)
    {
        if (x < x_min) {
            if (y <  y_min) return hypot(x_min-x, y_min-y);
            if (y <= y_max) return x_min - x;
            return hypot(x_min-x, y_max-y);
        } else if (x <= x_max) {
            if (y <  y_min) return y_min - y;
            if (y <= y_max) return 0;
            return y - y_max;
        } else {
            if (y <  y_min) return hypot(x_max-x, y_min-y);
            if (y <= y_max) return x - x_max;
            return hypot(x_max-x, y_max-y);
        }
    }
}
