package me.autobot.code;

import me.autobot.lib.serial.SensorHubI2CConnection;
import me.autobot.sim.AStarTest;
import me.autobot.sim.Simulation;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static boolean activated = false;

    public static void main(String[] args) {
        SensorHubI2CConnection sensorHubI2CConnection = new SensorHubI2CConnection(
                "i2c-connection-1",
                1,
                0x12
        );

        sensorHubI2CConnection.ping();

        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                if (!activated) {
                    sensorHubI2CConnection.writeToPin(0, SensorHubI2CConnection.HIGH);
                } else {
                    sensorHubI2CConnection.writeToPin(0, SensorHubI2CConnection.LOW);
                }

                activated = !activated;
            }
        };

        Timer tt = new Timer();
        tt.schedule(t, 0, 1000);
    }
}