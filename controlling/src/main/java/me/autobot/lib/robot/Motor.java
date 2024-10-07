package me.autobot.lib.robot;

import me.autobot.lib.logging.Log;
import me.autobot.lib.math.Mathf;
import me.autobot.lib.serial.SensorHubI2CConnection;

public class Motor extends Device {
    //assuming the max motor properties

    private static double maxVoltage = 36; //same as battery
    private static double maxCurrent = 19; //split. 20 is max tech, but that'll trip the fuse

    private boolean isInverted = false;

    @Log
    private double speed = 0; //[-1, 1], 0 is stop

    private boolean simulating = false;

    private final int deviceAddress; //i2c address
    private final int bus;

    public Motor(int address) {
        super();

        this.deviceAddress = address;
        this.bus = SensorHubI2CConnection.default_bus;
    }

    public Motor(int address, int bus) {
        super();

        this.deviceAddress = address;
        this.bus = bus;
    }

    /**
     * Connects the motor to the I2C bus.
     * */
    public void connectToI2C(int pin) {
        if (this.getParent() == null) {
            throw new IllegalStateException("Cannot connect sensor to I2C bus without a parent.");
        }

        if (this.inSimulation()) {
            //ignore this if we are in simulation
            return;
        }

        System.out.println("Motor subclass must implement #connectToI2C! (This is a placeholder)");
    }

    public void setSpeed(double speed) {
        this.speed = (isInverted ? -1 : 1) * Mathf.clamp(speed, -1, 1);
        reportSpeed();
    }
    
    public void stop(double speed) {
        this.speed = 0;
        reportSpeed();
    }

    public void invert() {
        isInverted = !isInverted;
    }

    public void brake() {
        //report to motor controller using i2c directly
        //to be overridden by subclasses
    }

    protected void reportSpeed() {
        //report speed to motor controller
        //to be overridden by subclasses
    }

    @Override
    public void emergencyStop() {
        stop(0);
        brake(); //idk about brake but yeah ig
    }

    public boolean inSimulation() {
        return simulating;
    }

    public int getAddress() {
        return deviceAddress;
    }

    public int getBus() {
        return bus;
    }

    public boolean isInverted() {
        return isInverted;
    }
}
