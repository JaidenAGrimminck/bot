package me.autobot.lib.math.map;

import me.autobot.lib.math.coordinates.Vector2d;

import java.util.ArrayList;

public class Map2d {
    private ArrayList<Vector2d> locations;

    private int nSinceLastClear = 0;

    public Map2d() {
        locations = new ArrayList<>();
    }

    public void addObstaclePoint(Vector2d location) {
        locations.add(location);
        ++nSinceLastClear;
    }

    public ArrayList<Vector2d> getLocations() {
        return locations;
    }
}
