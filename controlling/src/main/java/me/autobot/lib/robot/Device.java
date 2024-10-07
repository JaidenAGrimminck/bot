package me.autobot.lib.robot;

import java.util.HashMap;
import java.util.ArrayList;

import me.autobot.lib.logging.Log;
import me.autobot.lib.logging.Logger;

public class Device implements Logger {

    public static ArrayList<Device> devices = new ArrayList<>();

    /**
     * Registers a device.
     * */
    public static void registerDevice(Device device) {
        devices.add(device);
    }

    /**
     * Emergency stops all devices. Used in emergencies.
     * */
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

    private boolean inSimulation = false;

    /**
     * Creates a new device.
     * */
    public Device() {
        properties = new HashMap<>();

        registerDevice(this);
    }

    /**
     * Reports the voltage of the device to be logged and stored.
     * */
    private void reportVoltage(double voltage) {
        this.voltage = voltage;
    }

    /**
     * Reports the current of the device to be logged and stored.
     * */
    private void reportCurrent(double current) {
        this.current = current;
    }

    /**
     * Sets a property of the device.
     * */
    public void setProperty(String key, double value) {
        properties.put(key, value);
    }

    /**
     * Returns the properties of the device.
     * */
    public HashMap<String, Double> getProperties() {
        return properties;
    }

    /**
     * Gets the properties of the device in a string format.
     * */
    @Log(as="properties")
    private String getPropertyString() {
        String properties = "";

        for (String key : this.properties.keySet()) {
            properties += key + ": " + this.properties.get(key) + ", ";
        }

        return properties;
    }

    /**
     * Get a property of the device.
     * */
    public double getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Get the voltage of the device (if applicable).
     * */
    public double getVoltage() {
        return voltage;
    }

    /**
     * Get the current of the device (if applicable).
     * */
    public double getCurrent() {
        return current;
    }

    /**
     * Called to emergency stop the device.
     * This means: stop all motors, turn off all LEDs, etc. Literally stop everything.
     * */
    public void emergencyStop() {

    }

    /**
     * Set the parent of the device, i.e what robot the device is attached to.
     * */
    public void setParent(Robot parent) {
        this.parent = parent;
        System.out.println("Assigned parent to device: " + this.getClass().getSimpleName());
    }

    /**
     * Enable simulation mode for the device.
     * **/
    public void enableSimulation() {
        inSimulation = true;
    }

    /**
     * Check if the device is in simulation mode.
     * @return true if the device is in simulation mode.
     * */
    public boolean inSimulation() {
        return inSimulation;
    }

    /**
     * Get the parent of the device.
     * */
    protected Robot getParent() {
        return parent;
    }
}
