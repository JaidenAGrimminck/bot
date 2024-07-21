package me.autobot.lib.robot;

public class UltrasonicSensor extends Sensor {

    public UltrasonicSensor() {
        super(1);
        setSensorValues(0);
    }

    public double getDistance() {
        return getSensorValues()[0];
    }
}
