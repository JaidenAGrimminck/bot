package me.autobot.lib.math;

public class Unit {

    public static Unit zero() {
        return new Unit(0);
    }

    public enum Type {
        METER(1),
        CENTIMETER(0.01),
        MILLIMETER(0.001),
        KILOMETER(1000),
        INCH(0.0254),
        FOOT(0.3048),
        YARD(0.9144),
        MILE(1609.34)
        ;

        private double conversionToMeters;

        Type(double conversionToMeters) {
            this.conversionToMeters = conversionToMeters;
        }

        public double convertToMeters(double value) {
            return value * conversionToMeters;
        }

        public double convertFromMeters(double value) {
            return value / conversionToMeters;
        }

        public Unit c(double value) {
            return new Unit(value, this);
        }
    }

    private double inMeters;

    public Unit(double inMeters) {
        this.inMeters = inMeters;
    }

    public Unit(double value, Type type) {
        this.inMeters = type.convertToMeters(value);
    }

    public double getValue(Type type) {
        return type.convertFromMeters(inMeters);
    }

    public double getValue() {
        return inMeters;
    }
}
