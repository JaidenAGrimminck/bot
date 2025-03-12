package me.autobot.lib.robot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a robot that can be played.
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface PlayableRobot {
    /**
     * Name of the robot.
     * @return The name of the robot.
     * */
    public String name();
    /**
     * If the robot is disabled or not.
     * */
    public boolean disabled = false;

    /**
     * If the robot is able to be used in the simulator or not.
     * */
    public boolean simulatable = true;
}
