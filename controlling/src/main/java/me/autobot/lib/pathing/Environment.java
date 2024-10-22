package me.autobot.lib.pathing;

import me.autobot.lib.math.coordinates.Box2d;

import java.util.ArrayList;

/**
 * The environment in which the robot is operating. Contains all obstacles.
 * */
public class Environment {
    /**
     * List of all obstacles in the environment.
     * */
    public ArrayList<Box2d> obstacles = new ArrayList<>();

    //todo: actually implement this to work both for simulation and real life etc

    /**
     * Creates a new environment object.
     * */
    public Environment() {

    }
}
