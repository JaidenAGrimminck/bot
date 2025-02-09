package me.autobot.code;

import me.autobot.code.mechanisms.LIDAR;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.systems.mechanisms.Mechanism;

/**
 * Bot with a LIDAR sensor.
 * */
@PlayableRobot(name = "Lidar Bot")
public class LidarBot extends Robot {
    private LIDAR lidar;

    /**
     * Sets up the robot.
     * */
    @Override
    protected void setup() {
        lidar = new LIDAR();
    }

    /**
     * Runs the robot.
     * */
    @Override
    protected void loop() {
        // This is where you put code that you want to run repeatedly while the robot is running.
    }

    /**
     * Stops the robot.
     * */
    @Override
    protected void stop() {
        // This is where you put code that you want to run when the robot is stopped.
    }
}
