package me.autobot.lib.robot.motors;

import me.autobot.lib.robot.Motor;

/**
 * A hoverboard wheel motor.
 * */
public class HoverboardWheel extends Motor {


    private int directionPin;
    private int speedPin;

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
        this.directionPin = directionPin;
        this.speedPin = speedPin;

    }
}
