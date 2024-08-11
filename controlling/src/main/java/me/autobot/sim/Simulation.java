package me.autobot.sim;

import me.autobot.code.Robot;
import me.autobot.lib.logging.Logger;
import me.autobot.sim.graphics.SimCanvas;
import me.autobot.sim.graphics.SimScreen;

import java.util.Timer;
import java.util.TimerTask;

public class Simulation {

    private static Simulation instance;

    public static void start() {
        Thread thread = new Thread(() -> {
            new SimScreen();
        });

        thread.start();

//        if (instance == null) {
//            instance = new Simulation();
//        }
//        getInstance();
    }

    public static void stop() {
        instance = null;
        //shutdown etc.
    }

    public static Simulation getInstance() {
        if (instance == null) {
            instance = new Simulation();
        }
        return instance;
    }

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

        public SimulationTimer() {
            super();
        }

        public void start() {
            timer.scheduleAtFixedRate(task, 0, 50);
        }

        public void adjustMultiplier(double multiplier) {
            if (multiplier < 0) {
                throw new IllegalArgumentException("Multiplier must be greater than 0");
            } else if (multiplier == 0) {
                this.pause();
                return;
            }

            this.multiplier = multiplier;
        }

        public void pause() {
            this.multiplier = 0;

            //etc.
        }

        public double getMultiplier() {
            return multiplier;
        }

        private double getDeltaTime() {
            return (1d / fps) * multiplier;
        }
    }


    private SimulationTimer timer = new SimulationTimer();
    private Robot robot;


    private Simulation() {
        robot = new Robot();

        robot.startSimulation();

        timer.start();
    }

    public SimulationTimer getTimer() {
        return timer;
    }

    public Robot getRobot() {
        return robot;
    }


}
