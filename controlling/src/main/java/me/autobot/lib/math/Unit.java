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

        /**
         * Converts the value to meters
         * @param value How many you must multiply this unit by to convert to meters
         * */
        public double convertToMeters(double value) {
            return value * conversionToMeters;
        }

        /**
         * Converts the value from meters
         * @param value How many you need to divide this unit to convert from meters
         * */
        public double convertFromMeters(double value) {
            return value / conversionToMeters;
        }

        /**
         * Creates a unit with the value in this unit.
         * */
        public Unit c(double value) {
            return new Unit(value, this);
        }
    }

    private double inMeters;


    /**
     * Creates a unit with the value in meters.
     * @param inMeters The value in meters
     * **/
    public Unit(double inMeters) {
        this.inMeters = inMeters;
    }

    /**
     * Creates a unit with the value in the given unit.
     * @param value The value in the given unit
     * @param type The unit of the value
     * */
    public Unit(double value, Type type) {
        this.inMeters = type.convertToMeters(value);
    }

    /**
     * Gets the value in the given unit.
     * @param type The unit to convert to
     * @return The value in the given unit
     * */
    public double getValue(Type type) {
        return type.convertFromMeters(inMeters);
    }

    /**
     * Returns the value in meters.
     * @return The value in meters
     * **/
    public double getValue() {
        return inMeters;
    }
}
