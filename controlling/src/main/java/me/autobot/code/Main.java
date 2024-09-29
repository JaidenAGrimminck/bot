package me.autobot.code;

import me.autobot.lib.serial.SensorHubI2CConnection;
import me.autobot.sim.AStarTest;
import me.autobot.sim.Simulation;

public class Main {
    public static void main(String[] args) {
        SensorHubI2CConnection sensorHubI2CConnection = new SensorHubI2CConnection(
                "i2c-connection-1",
                1,
                0x02
        );

        sensorHubI2CConnection.ping();
    }
}