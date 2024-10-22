package me.autobot.code;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.serial.SensorHubI2CConnection;
import me.autobot.sim.AStarTest;
import me.autobot.sim.Simulation;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The main entry point to the program.
 * */
public class Main {
    /**
     * Isn't called! Rather, the main() method is called.
     * */
    public Main() {}

    /**
     * This is the entry point of the code.
     * Just create a new instance of whatever Robot class you may want to run, and the code will start running.
     * Note: This is NOT for simulation. This is for running the code on the actual robot.
     * @see Simulation
     * @see me.autobot.lib.robot.Robot
     * @param args The arguments passed to the program.
     * */
    public static void main(String[] args) {
        new ServoBot();
    }
}