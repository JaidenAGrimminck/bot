package me.autobot.lib.robot;

public class UltrasonicSensor extends Sensor {

    public UltrasonicSensor(int address) {
        super(address, 1);
        setSensorValues(0);
    }

    public double getDistance() {
        return getSensorValues()[0];
    }
}
