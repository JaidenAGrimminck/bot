package me.autobot.lib.robot;

import me.autobot.lib.logging.Log;
import me.autobot.lib.math.Mathf;
import me.autobot.lib.serial.SensorHubI2CConnection;

/**
 * A moving device that can be controlled by the robot, such as a motor.
 * */
public class Motor extends Device {
    //assuming the max motor properties

    private static double maxVoltage = 36; //same as battery
    private static double maxCurrent = 19; //split. 20 is max tech, but that'll trip the fuse

    private boolean isInverted = false;

    @Log
    private double speed = 0; //[-1, 1], 0 is stopped

    private boolean simulating = false;

    private final int deviceAddress; //i2c address
    private final int bus;

    /**
     * Creates a new motor with the given I2C address (and default I2C bus).
     * @param address The I2C address of the motor controller.
     * */
    public Motor(int address) {
        super();

        this.deviceAddress = address;
        this.bus = SensorHubI2CConnection.default_bus;
    }

    /**
     * Creates a new motor with the given I2C address and bus.
     * @param address The I2C address of the motor controller.
     * @param bus The I2C bus the motor controller is connected to.
     * */
    public Motor(int address, int bus) {
        super();

        this.deviceAddress = address;
        this.bus = bus;
    }

    /**
     * Actually connects the motor to the I2C bus using the given pin.
     * This must be overridden by subclasses, it will throw a warning if not.
     * Note: The parent must be set before calling this method.
     * @param pin The pin the motor is connected to on the motor controller.
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

    /**
     * Sets the speed of the motor.
     * @param speed The speed of the motor, from -1 to 1.
     * */
    public void setSpeed(double speed) {
        this.speed = (isInverted ? -1 : 1) * Mathf.clamp(speed, -1, 1);
        reportSpeed();
    }

    /**
     * Stops the motor (sets speed to 0).
     * */
    public void stop() {
        this.speed = 0;
        reportSpeed();
    }

    /**
     * Inverts the motor's direction.
     * */
    public void invert() {
        isInverted = !isInverted;
    }

    /**
     * Brakes the motor. Should be overridden by subclasses.
     * */
    public void brake() {
        //report to motor controller using i2c directly
        //to be overridden by subclasses
    }

    /**
     * Sends the speed to the motor controller.
     * This should be overridden by subclasses due to the difference in motor controllers.
     * */
    protected void reportSpeed() {
        //report speed to motor controller
        //to be overridden by subclasses
    }

    /**
     * Emergency stops the motor. Runs both #stop and #brake.
     * @see #stop()
     * @see #brake()
     * */
    @Override
    public void emergencyStop() {
        stop();
        brake(); //idk about brake but yeah ig
    }

    /**
     * Returns whether the motor is currently in simulation mode.
     * @return Whether the motor is in simulation mode.
     */
    public boolean inSimulation() {
        return simulating;
    }

    /**
     * Returns the I2C address of the motor controller.
     * @return The I2C address of the motor controller.
     * */
    public int getAddress() {
        return deviceAddress;
    }

    /**
     * Returns the I2C bus the motor controller is connected to.
     * @return The I2C bus the motor controller is connected to.
     * */
    public int getBus() {
        return bus;
    }

    /**
     * Returns if the motor direction is inverted.
     * @return If the direction is inverted.
     */
    public boolean isInverted() {
        return isInverted;
    }
}
