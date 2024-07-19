package me.autobot.lib.math;

public class Mathf {
    public static double clamp(double n, double min, double max) {
        return Math.max(min, Math.min(max, n));
    }
}
