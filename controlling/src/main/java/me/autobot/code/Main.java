package me.autobot.code;

import me.autobot.lib.serial.serial.SensorHubSerialConnection;
import me.autobot.sim.Simulation;

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
        System.out.println("Connection making.");
        SensorHubSerialConnection connection = SensorHubSerialConnection.getOrCreateConnection(9600, "/dev/cu.usbserial-10");
        System.out.println("Connection established.");

        Runnable run = () -> {
            while (!connection.open()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Connection opened.");
            connection.ping();

            connection.writeToPin(13, SensorHubSerialConnection.HIGH);
            System.out.println("Data written.");
        };

        Thread thread = new Thread(run);
        thread.start();
    }
}