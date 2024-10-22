package me.autobot.lib.math;

/**
 * A class to represent a unit of measurement. Can be used for conversion, tracking, etc.
 * */
public class Unit {

    /**
     * The zero unit. Equivalent to 0 meters, or 0 of any other unit.
     * @return The zero unit
     * */
    public static Unit zero() {
        return new Unit(0);
    }

    /**
     * The type of unit. Each type has a conversion factor to meters.
     * */
    public enum Type {
        /**
         * A meter is 1 meter.
         * */
        METER(1),
        /**
         * A centimeter is 0.01 meters.
         * */
        CENTIMETER(0.01),
        /**
         * A millimeter is 0.001 meters.
         * */
        MILLIMETER(0.001),
        /**
         * A kilometer is 1000 meters.
         * */
        KILOMETER(1000),
        /**
         * An inch is 0.0254 meters.
         * */
        INCH(0.0254),
        /**
         * A foot is 0.3048 meters.
         * */
        FOOT(0.3048),
        /**
         * A yard is 0.9144 meters.
         * */
        YARD(0.9144),
        /**
         * A mile is 1609.34 meters.
         * */
        MILE(1609.34)
        ;

        private double conversionToMeters;

        /**
         * Creates a type with the given conversion factor to meters.
         * @param conversionToMeters How many you must multiply this unit by to convert to meters
         * */
        Type(double conversionToMeters) {
            this.conversionToMeters = conversionToMeters;
        }

        /**
         * Converts the value to meters
         * @param value How many you must multiply this unit by to convert to meters
         * @return The value in meters
         * */
        public double convertToMeters(double value) {
            return value * conversionToMeters;
        }

        /**
         * Converts the value from meters
         * @param value How many you need to divide this unit to convert from meters
         * @return The value in this unit
         * */
        public double convertFromMeters(double value) {
            return value / conversionToMeters;
        }

        /**
         * Creates a unit with the value in this unit.
         * @param value The value in this unit
         * @return The unit with the value in this unit
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
