package me.autobot.lib.robot.sensors;

import me.autobot.code.Robot;
import me.autobot.lib.robot.Sensor;

public class CollisionSensor extends Sensor {
    public CollisionSensor(int address) {
        super(address, 1);
        setSensorValues(0);
    }

    @Override
    public double[] getValues() {
        return new double[] {
                inSimulation() && this.getParent().inCollision() ? 1 : 0
        };
    }
}
