package me.autobot.lib.robot.motors;

import me.autobot.lib.logging.Log;
import me.autobot.lib.math.Mathf;
import me.autobot.lib.robot.Motor;
import me.autobot.lib.serial.SensorHubI2CConnection;

public class Servo extends Motor {
    @Log
    private double speed = 0;

    private SensorHubI2CConnection connectionRef;
    private int pin;

    public Servo(int address) {
        super(address);
    }

    public Servo(int address, int bus) {
        super(address, bus);
    }

    /**
     * Connects the servo to the I2C bus.
     * @param pin The pin to connect the servo to.
     * */
    @Override
    public void connectToI2C(int pin) {
        if (this.getParent() == null) {
            throw new IllegalStateException("Cannot connect sensor to I2C bus without a parent.");
        }

        if (this.inSimulation()) {
            //ignore this if we are in simulation
            return;
        }

        connectionRef = SensorHubI2CConnection.getOrCreateConnection(
                SensorHubI2CConnection.generateId(this.getBus(), this.getAddress()), this.getBus(), this.getAddress()
        );

        this.pin = pin;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = (this.isInverted() ? 0 : 180) * Mathf.clamp(speed, 0, 180);
        reportSpeed();
    }

    @Override
    protected void reportSpeed() {
        if (this.inSimulation()) {
            return;
        }

        if (this.connectionRef == null) {
            throw new IllegalStateException("Cannot write to pin without a connection.");
        }

        connectionRef.writeToPin(this.pin, (float) this.speed);
    }
}
