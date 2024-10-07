package me.autobot.code;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.serial.SensorHubI2CConnection;
import me.autobot.sim.AStarTest;
import me.autobot.sim.Simulation;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static boolean activated = false;

    public static void main(String[] args) {
        new ServoBot();


//        SensorHubI2CConnection sensorHubI2CConnection = new SensorHubI2CConnection(
//                "i2c-connection-1",
//                1,
//                0x12
//        );
//
//        sensorHubI2CConnection.ping();
//
//        TimerTask t = new TimerTask() {
//            @Override
//            public void run() {
//                if (!activated) {
//                    sensorHubI2CConnection.writeToPin(12, SensorHubI2CConnection.HIGH);
//                    sensorHubI2CConnection.writeToPin(8, 90);
//                } else {
//                    sensorHubI2CConnection.writeToPin(12, SensorHubI2CConnection.LOW);
//                    sensorHubI2CConnection.writeToPin(8, 180);
//                }
//
//                activated = !activated;
//            }
//        };
//
//        Timer tt = new Timer();
//        tt.schedule(t, 0, 1000);
    }
}