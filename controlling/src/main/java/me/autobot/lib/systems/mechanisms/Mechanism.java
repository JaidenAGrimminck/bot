package me.autobot.lib.systems.mechanisms;

import me.autobot.lib.robot.Robot;

import java.util.ArrayList;

/**
 * A mechanism that can be used on the robot.
 * */
public class Mechanism {
    private static ArrayList<Mechanism> mechanisms = new ArrayList<>();

    protected boolean disabled = false;

    protected Robot parent;

    /**
     * Runs the init method on all mechanisms.
     * */
    public static void init_all(Robot robotRef) {
        for (Mechanism mechanism : mechanisms) {
            mechanism.parent = robotRef;
            if (mechanism.isDisabled()) continue;

            Thread t = new Thread(mechanism::init);
            t.start();
        }
    }

    /**
     * Runs the update method on all mechanisms.
     * */
    public static void update_all() {
        for (Mechanism mechanism : mechanisms) {
            if (mechanism.isDisabled()) continue;

            Thread t = new Thread(mechanism::update);
            t.start();
        }
    }

    /**
     * Runs the stop method on all mechanisms.
     * */
    public static void stop_all() {
        for (Mechanism mechanism : mechanisms) {
            if (mechanism.isDisabled()) continue;

            Thread t = new Thread(mechanism::stop);
            t.start();
        }
    }

    /**
     * Creates a new Mechanism.
     * */
    public Mechanism() {
        mechanisms.add(this);
    }

    /**
     * Initializes the mechanism.
     * */
    protected void init() {

    }

    /**
     * Updates the mechanism.
     * */
    protected void update() {

    }

    /**
     * Stops the mechanism.
     */
    protected void stop() {

    }

    /**
     * Disables the mechanism.
     * */
    public void disable() {
        disabled = true;
    }

    /**
     * Enables the mechanism.
     * */
    public void enable() {
        disabled = false;
    }

    /**
     * Gets if the mechanism is disabled.
     * @return If the mechanism is disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }

}
