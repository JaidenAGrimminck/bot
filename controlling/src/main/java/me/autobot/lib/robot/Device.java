package me.autobot.lib.robot;

import java.util.HashMap;
import java.util.ArrayList;

import me.autobot.lib.logging.Log;
import me.autobot.lib.logging.Logger;

public class Device implements Logger {

    public static ArrayList<Device> devices = new ArrayList<>();

    public static void registerDevice(Device device) {
        devices.add(device);
    }

    public static void emergencyStopAll() {
        for (Device device : devices) {
            device.emergencyStop();
        }
    }

    @Log
    private double voltage = 0;

    @Log
    private double current = 0;

    private HashMap<String, Double> properties;

    private Robot parent;

    public Device() {
        properties = new HashMap<>();


    }

    private void reportVoltage(double voltage) {
        this.voltage = voltage;
    }

    private void reportCurrent(double current) {
        this.current = current;
    }

    public void setProperty(String key, double value) {
        properties.put(key, value);
    }

    public HashMap<String, Double> getProperties() {
        return properties;
    }

    @Log(as="properties")
    private String getPropertieString() {
        String properties = "";

        for (String key : this.properties.keySet()) {
            properties += key + ": " + this.properties.get(key) + ", ";
        }

        return properties;
    }

    public double getProperty(String key) {
        return properties.get(key);
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }

    public void emergencyStop() {

    }

    public void setParent(Robot parent) {
        this.parent = parent;
    }

    protected Robot getParent() {
        return parent;
    }
}
