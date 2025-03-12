package me.autobot.simulation;

import me.autobot.code.Main;
import me.autobot.lib.hardware.i2c.I2CConnection;
import me.autobot.lib.hardware.serial.SerialConnection;

/**
 * A class for simulating the robot.
 * */
public class Simulate {

    private static boolean simulation = false;

    /**
     * Returns whether the robot is in simulation mode.
     * @return Whether the robot is in simulation mode.
     * */
    public static boolean inSimulation() {
        return simulation;
    }


    /**
     * Main thread for simulation.
     * */
    public static void main(String[] args) {
        simulation = true;

        // disable any outgoing comms via serial
        SerialConnection.disableConnections();
        // disable any outgoing comms via i2c
        I2CConnection.disableConnections();

        // run through our main code
        Main.main(args);
    }
}
