package me.autobot.lib.robot.motors;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.robot.Motor;
import me.autobot.lib.serial.serial.SensorHubSerialConnection;

import java.nio.ByteBuffer;

/**
 * A hoverboard wheel motor.
 * */
public class HoverboardWheel extends Motor {

    private int directionPin;
    private int speedPin;

    private double speed = 0;
    private double lastSpeed = 0;

    private double maxSpeed = 1;

    private SensorHubSerialConnection connectionRef;

    /**
     * Creates a new HoverboardWheel with the given I2C address (and default I2C bus).
     * */
    public HoverboardWheel(int identifier, int address) {
        super(identifier, address);
    }

    /**
     * Creates a new HoverboardWheel with the given I2C address and bus.
     * */
    public HoverboardWheel(int identifier, int address, int bus) {
        super(identifier, address, bus);
    }

    @Override
    public void connectToSerial(String port) {
        throw new RuntimeException("HoverboardWheel needs ");
    }

    //ports: /dev/cu.wchusbserial.10, /dev/cu.wchusbserial.110
    public void connectToSerial(String port, int directionPin, int speedPin) {
        if (this.inSimulation()) {
            return;
        }

        if (this.getParent() == null) {
            throw new IllegalStateException("Cannot connect motor to serial without a parent.");
        }

        this.directionPin = directionPin;
        this.speedPin = speedPin;

        connectionRef = SensorHubSerialConnection.getOrCreateConnection(9600, port);
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = (isInverted() ? -1 : 1) * Mathf.clamp(speed, -maxSpeed, maxSpeed);

        reportSpeed();
    }

    @Override
    protected void reportSpeed() {
        if (inSimulation()) {
            return;
        }

        if (connectionRef == null) {
            throw new IllegalStateException("Cannot report speed without a connection!");
        }

        if (!connectionRef.open()) {
            return;
        }

        if (Math.signum(this.speed) != Math.signum(this.lastSpeed) || this.lastSpeed == 0) {
            if (this.speed < 0) {
                connectionRef.writeToPin(directionPin, SensorHubSerialConnection.HIGH);
            } else {
                connectionRef.writeToPin(directionPin, SensorHubSerialConnection.LOW);
            }
        }

        writeToPWMPin(speedPin, (float) Mathf.map(Math.abs(this.speed), 0, 1, 0, 255));
    }

    /**
     * Writes a float to a specific pin.
     * @param index The index of the pin to write to.
     * @param value The float to write to the pin.
     * */
    protected void writeToPWMPin(int index, float value) {
        byte[] payload = new byte[Float.BYTES];

        ByteBuffer bbuf = ByteBuffer.allocate(Float.BYTES);
        bbuf.putFloat(value);

        System.arraycopy(bbuf.array(), 0, payload, 0, Float.BYTES);

        writeToPWMPin(index, payload);
    }

    /**
     * Writes a byte list to a specific pin. Should be 4 bytes long (a float!)
     * @param index The index of the pin to write to.
     * @param value The byte list (length=4) to write to the pin.
     * */
    protected void writeToPWMPin(int index, byte[] value) {
        if (connectionRef == null) {
            throw new IllegalStateException("Cannot write to pin without a connection.");
        }

        if (!connectionRef.open()) {
            return;
        }

        if (value.length != Float.BYTES) {
            return;
        }

        byte[] payload = new byte[3 + value.length];
        payload[0] = (byte) 0xA2;
        payload[1] = (byte) index;

        System.arraycopy(value, 0, payload, 2, value.length);
        payload[2 + value.length] = SensorHubSerialConnection.THIS_DEVICE_ADDRESS;
        connectionRef.write(payload);
    }

    /**
     * Sets the maximum speed of the motor.
     * @param maxSpeed The maximum speed of the motor.
     * */
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
