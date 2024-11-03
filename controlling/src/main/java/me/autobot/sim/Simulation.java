package me.autobot.sim;

import me.autobot.code.SimRobot;
import me.autobot.lib.os.OSDetector;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.pathing.Environment;
import me.autobot.server.WSServer;
import me.autobot.sim.graphics.SimCanvas;
import me.autobot.sim.graphics.SimScreen;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A class used to simulate the robot.
 * */
public class Simulation {

    private static Simulation instance;

    /**
     * The simulation environment.
     * */
    public Environment environment;

    /**
     * The main entry point to the simulation program.
     * @param args The arguments passed to the program.
     * */
    public static void main(String[] args) {
        start();
    }

    /**
     * Starts the simulation.
     * */
    public static void start() {
        WSServer.wsstart();

        Thread thread = new Thread(() -> {
            if (!OSDetector.usingLinux()) {
                new SimScreen();
            } else {
                System.out.println("Linux detected, skipping screen creation.");
            }
        });

        if (instance == null) {
            instance = new Simulation();
        }
        getInstance();

        thread.start();
    }

    /**
     * Stops the simulation.
     * */
    public static void stop() {
        instance = null;
        //shutdown etc.
    }

    /**
     * Returns the instance of the simulation.
     * @return The instance of the simulation.
     * */
    public static Simulation getInstance() {
        if (instance == null) {
            instance = new Simulation();
        }
        return instance;
    }

    /**
     * Returns whether the simulation is running.
     * @return Whether the simulation is running.
     * */
    public static boolean running() {
        return instance != null;
    }

    /**
     * A timer that keeps track of the simulation time.
     * */
    public static class SimulationTimer extends Timer {
        double time = 0;

        double multiplier = 1;

        private final int fps = 20;

        private Timer timer = new Timer();

        private TimerTask task = new TimerTask() {
            @Override
            public void run() {
                time += (1d / fps) * multiplier;
            }
        };

        /**
         * Creates a new simulation timer.
         * */
        public SimulationTimer() {
            super();
        }

        /**
         * Starts the simulation timer.
         * */
        public void start() {
            timer.scheduleAtFixedRate(task, 0, 50);
        }

        /**
         * Adjusts the multiplier of the simulation timer.
         * This can be used to speed up or slow down the simulation.
         * @param multiplier The multiplier to adjust the simulation by.
         * */
        public void adjustMultiplier(double multiplier) {
            if (multiplier < 0) {
                throw new IllegalArgumentException("Multiplier must be greater than 0");
            } else if (multiplier == 0) {
                this.pause();
                return;
            }

            this.multiplier = multiplier;
        }

        /**
         * Pauses the simulation timer.
         * */
        public void pause() {
            this.multiplier = 0;

            //etc.
        }

        /**
         * Gets the time multiplier of the simulation timer.
         * @return The time multiplier of the simulation timer.
         * */
        public double getMultiplier() {
            return multiplier;
        }

        /**
         * Gets the delta time of the simulation timer.
         * @return The delta time of the simulation timer.
         * */
        private double getDeltaTime() {
            return (1d / fps) * multiplier;
        }
    }


    private SimulationTimer timer = new SimulationTimer();

    /**
     * Creates a new simulation.
     * */
    private Simulation() {
        for (int i = 0; i < SimCanvas.numberOfAIRobots; i++) {
            new SimRobot();
        }
        environment = new Environment();

        Robot.startSimulation();

        timer.start();
    }

    /**
     * Gets the simulation timer.
     * @return The simulation timer.
     * */
    public SimulationTimer getTimer() {
        return timer;
    }

}
