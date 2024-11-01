package me.autobot.lib.robot.motors;

import me.autobot.lib.logging.Log;
import me.autobot.lib.math.Mathf;
import me.autobot.lib.robot.Motor;
import me.autobot.lib.serial.i2c.SensorHubI2CConnection;

/**
 * The servo motor class. This class is used to control a servo motor via the I2C bus.
 * */
public class Servo extends Motor {
    @Log
    private double speed = 0;

    private double lastReportedSpeed = 0;

    //I2C connection reference
    private SensorHubI2CConnection connectionRef;

    //The pin that the servo is connected to on the i2c bus
    private int pin;

    /**
     * Creates a new servo with the given address on the default I2C bus.
     * @param identifier The identifier of the servo.
     * @param address The I2C address of the controller.
     * */
    public Servo(int identifier, int address) {
        super(identifier, address);
    }

    /**
     * Creates a new servo with the given address on the given I2C bus.
     * @param identifier The identifier of the servo.
     * @param address The I2C address of the controller.
     * @param bus The I2C bus the controller is connected to.
     * */
    public Servo(int identifier, int address, int bus) {
        super(identifier, address, bus);
    }

    /**
     * Connects the servo to the I2C bus. This is assuming that the bot has the <a href="https://github.com/JaidenAGrimminck/bot/blob/main/controlling/arduino-scripts/readwriter/readwriter.ino">standard interpreter</a>
     * for the Arduino. If you're using a different interpreter, you'll need to change this method or create a different subclass.
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

        //since the servo should already be initialized on the i2c if configured properly,
        //we don't need to do anything here, just save the pin and connection reference
    }

    /**
     * Sets the speed of the servo to a number between 0 and 180.
     * @param speed The speed to set the servo to.
     * */
    public void setRawSpeed(double speed) {
        this.speed = Mathf.clamp(speed, 0, 180);
        if (this.isInverted()) {
            this.speed = 180 - this.speed;
        }

        reportSpeed();
    }

    /**
     * Sets the speed of the servo to a number between -1 and 1.
     * @param speed The speed to set the servo to.
     * */
    public void setSpeed(double speed) {
        //map the value to 0, 180
        this.setRawSpeed(Mathf.map(speed, -1, 1, 0, 180));
    }

    /**
     * Stops the servo via setting the speed to 90 (halfway point).
     * */
    @Override
    public void stop() {
        this.speed = 90;
        reportSpeed();
    }

    /**
     * Reports the speed of the servo to the I2C bus.
     * */
    @Override
    protected void reportSpeed() {
        if (this.inSimulation()) {
            return;
        }

        if (this.connectionRef == null) {
            throw new IllegalStateException("Cannot write to pin without a connection.");
        }

        if (Math.abs(this.speed - this.lastReportedSpeed) < 0.01) {
            //ignore since it's the same value - don't want to spam the i2c bus
            return;
        }

        connectionRef.writeToPin(this.pin, (float) this.speed);

        this.lastReportedSpeed = this.speed;
    }
}
