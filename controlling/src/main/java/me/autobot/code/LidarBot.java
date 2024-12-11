package me.autobot.code;

import me.autobot.code.mechanisms.LIDAR;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.systems.mechanisms.Mechanism;

@PlayableRobot(name = "Lidar Bot")
public class LidarBot extends Robot {
    private LIDAR lidar;

    @Override
    protected void setup() {
        lidar = new LIDAR();
    }

    @Override
    protected void loop() {
        // This is where you put code that you want to run repeatedly while the robot is running.
    }

    @Override
    protected void stop() {
        // This is where you put code that you want to run when the robot is stopped.
    }
}
