package me.autobot.lib.math.map;

import me.autobot.lib.math.coordinates.Vector2d;

import java.awt.*;
import java.util.ArrayList;

/**
 * A class that represents a 2D map of locations/obstacles.
 * **/
public class Map2d {
    private ArrayList<Vector2d> locations;

    /**
     * Create a new 2D map.
     * */
    public Map2d() {
        locations = new ArrayList<>();
    }

    /**
     * Add an obstacle to the map.
     * @param location The location of the obstacle.
     * */
    public void addObstaclePoint(Vector2d location) {
//        locations.add(location);
//
//        //not the purpose, want a dynamic map that can "map out" the entire "maze" but this should work for now for in terms of memory management
//        if (locations.size() > 1000) {
//            locations.remove(0);
//        }
    }

    /**
     * Get the locations of the obstacles on the map.
     * @return The locations of the obstacles on the map.
     * */
    public ArrayList<Vector2d> getLocations() {
        return locations;
    }
}
