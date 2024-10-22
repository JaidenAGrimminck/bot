package me.autobot.lib.math;

import me.autobot.lib.math.coordinates.Vector2d;

/**
 * A class that contains mathematical functions that are not included in the Math class.
 * **/
public class Mathf {

    /**
     * Tries to instantiate a Mathf object, but throws an exception because it is a utility class.
     * Please use the static methods instead.
     * */
    public Mathf() {
        throw new IllegalStateException("Cannot instantiate a utility class.");
    }

    /**
     * Returns the value of the given number clamped between the given minimum and maximum values.
     * @param n The number to clamp.
     *          Can be any number.
     *          If the number is less than the minimum value, the minimum value is returned.
     *          If the number is greater than the maximum value, the maximum value is returned.
     *          If the number is between the minimum and maximum values, the number is returned.
     * @param min The minimum value.
     * @param max The maximum value.
     * @return The clamped value.
     * */
    public static double clamp(double n, double min, double max) {
        return Math.max(min, Math.min(max, n));
    }

    /**
     * Returns a value that is linearly interpolated between the given minimum and maximum values.
     * @param n The number to interpolate.
     *          Can be any number.
     * @param premin The minimum value of the number.
     *               Can be any number.
     * @param premax The maximum value of the number.
     *               Can be any number greater than premin.
     * @param postmin The minimum value of the interpolation.
     *                Can be any number.
     * @param postmax The maximum value of the interpolation.
     *                Can be any number greater than postmin.
     *
     * @return The interpolated value.
     * */
    public static double map(double n, double premin, double premax, double postmin, double postmax) {
        return (n - premin) / (premax - premin) * (postmax - postmin) + postmin;
    }

    /**
     * Return the maximum value of the given numbers.
     * @param v The numbers to compare.
     *          Can be any number of arguments.
     *          If no arguments are given, 0 is returned.
     * @return The maximum value of the given numbers.
     * */
    public static double max(double ...v) {
        if (v.length == 0) return 0;

        double max = v[0];
        for (double n : v) {
            max = Math.max(max, n);
        }
        return max;
    }

    /**
     * Returns the minimum value of the given numbers.
     * @param v The numbers to compare.
     *          Can be any number of arguments.
     *          If no arguments are given, 0 is returned.
     * @return The minimum value of the given numbers.
     * */
    public static double min(double ...v) {
        if (v.length == 0) return 0;

        double min = v[0];
        for (double n : v) {
            min = Math.min(min, n);
        }
        return min;
    }

    /**
     * Returns the hypotenuse of the given sides.
     * @param x The length of the first side.
     *          Can be any number.
     * @param y The length of the second side.
     *          Can be any number.
     * @return The length of the hypotenuse.
     * */
    public static double hypot(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    //https://codereview.stackexchange.com/questions/175566/compute-shortest-distance-between-point-and-a-rectangle
    /**
     * Returns the signed distance between a point and a box in 2D space.
     * @param x The x-coordinate of the point.
     *          Can be any number.
     * @param y The y-coordinate of the point.
     *          Can be any number.
     * @param x_min The minimum x-coordinate of the box.
     *              Can be any number.
     *              Must be less than x_max.
     * @param y_min The minimum y-coordinate of the box.
     *              Can be any number.
     *              Must be less than y_max.
     * @param x_max The maximum x-coordinate of the box.
     *              Can be any number.
     *              Must be greater than x_min.
     * @param y_max The maximum y-coordinate of the box.
     *              Can be any number.
     *              Must be greater than y_min.
     * @return The signed distance between the point and the box.
     * **/
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

    /**
     * Returns the cosine of the given angle, but without any floating point errors.
     * @param r The angle in radians.
     *          Can be any number.
     * @return The cosine of the angle.
     * */
    public static double cos(double r) {
        if (r == 0) return 1;
        if (r == Math.PI) return -1;
        if (r == Math.PI / 2) return 0;
        if (r == 3 * Math.PI / 2) return 0;

        return Math.cos(r);
    }

    /**
     * Returns the sine of the given angle, but without any floating point errors.
     * @param r The angle in radians.
     *          Can be any number.
     * @return The sine of the angle.
     * */
    public static double sin(double r) {
        if (r == 0) return 0;
        if (r == Math.PI) return 0;
        if (r == Math.PI / 2) return 1;
        if (r == 3 * Math.PI / 2) return -1;

        return Math.sin(r);
    }

    /**
     * Takes in a number between -127 and 128 (a byte) and returns the equivalent positive number.
     * @param n The number to convert.
     * @return The equivalent positive number.
     * */
    public static int allPos(int n) {
        if ((int) n < 0) return ((int) n) + 256;
        return n;
    }

    /**
     * Takes in an array of numbers between -127 and 128 (bytes) and returns the equivalent positive numbers.
     * @param n The numbers to convert.
     * @return The equivalent positive numbers.
     * */
    public static int[] allPos(int[] n) {
        int[] pos = new int[n.length];
        for (int i = 0; i < n.length; i++) {
            pos[i] = allPos(n[i]);
        }
        return pos;
    }
}
