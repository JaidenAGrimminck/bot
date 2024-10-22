package me.autobot.lib.math.map;

import me.autobot.lib.math.coordinates.Vector2d;

import java.util.ArrayList;

/**
 * A simple 2D map that can be used to detect obstacles.
 * */
public class SimpleMap2d {
    private ArrayList<Vector2d> obstacles;

    /**
     * A simple 2D map that can be used to detect obstacles.
     * */
    public SimpleMap2d() {
        obstacles = new ArrayList<>();
    }

    /**
     * Returns a list of detected "obstacles" (points) on the map.
     * @return A list of detected "obstacles" (points) on the map.
     * */
    public ArrayList<Vector2d> getObstacles() {
        return obstacles;
    }

    /**
     * Creates a 2D array map of the obstacles in the map.
     * @param topLeft The top left corner of the map.
     * @param bottomRight The bottom right corner of the map.
     * @param decimals The number of decimals to use for the map.
     * @return A 2D array map of the obstacles in the map.
     * */
    public int[][] createMap(Vector2d topLeft, Vector2d bottomRight, int decimals) {
        int[][] map = new int[(int) (bottomRight.getY() - topLeft.getY()) * decimals][(int) (bottomRight.getX() - topLeft.getX()) * decimals];

        for (Vector2d obstacle : obstacles) {
            int x = (int) (obstacle.getX() - topLeft.getX()) * decimals;
            int y = (int) (obstacle.getY() - topLeft.getY()) * decimals;
            map[y][x] = 1;
        }

        return map;
    }
}
