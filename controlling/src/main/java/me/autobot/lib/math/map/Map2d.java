package me.autobot.lib.math.map;

import me.autobot.lib.math.coordinates.Vector2d;

import java.awt.*;
import java.util.ArrayList;

public class Map2d {
    private ArrayList<Vector2d> locations;

    public Map2d() {
        locations = new ArrayList<>();
    }

    public void addObstaclePoint(Vector2d location) {
//        locations.add(location);
//
//        //not the purpose, want a dynamic map that can "map out" the entire "maze" but this should work for now for in terms of memory management
//        if (locations.size() > 1000) {
//            locations.remove(0);
//        }
    }

    public ArrayList<Vector2d> getLocations() {
        return locations;
    }
}
