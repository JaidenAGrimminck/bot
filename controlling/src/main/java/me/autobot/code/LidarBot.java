package me.autobot.code;

import me.autobot.code.mechanisms.LIDAR;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;

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
}
