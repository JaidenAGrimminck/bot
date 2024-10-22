package me.autobot.lib.robot.sensors;

import me.autobot.lib.robot.Sensor;

/**
 * Checks if the robot is in collision (used IN SIMULATION ONLY).
 * */
public class CollisionSensor extends Sensor {
    /**
     * Creates a new collision sensor (for simulation) with the given identifier and address.
     * @param identifier The identifier of the sensor. Could be any number, should be unique to all other sensors on the robot.
     * @param address The address of the sensor hub on the I2C bus.
     * */
    public CollisionSensor(int identifier, int address) {
        super(identifier, address, 1);
        setSensorValues(0);
    }

    /**
     * Returns the value of the collision sensor.
     * */
    @Override
    public double[] getValues() {
        return new double[] {
                inSimulation() && this.getParent().inCollision() ? 1 : 0
        };
    }
}
