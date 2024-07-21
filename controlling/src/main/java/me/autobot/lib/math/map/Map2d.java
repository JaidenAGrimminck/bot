package me.autobot.lib.math.map;

import me.autobot.lib.math.coordinates.Vector2d;

import java.util.ArrayList;

public class Map2d {
    private ArrayList<Vector2d> obstacles;

    public Map2d() {
        obstacles = new ArrayList<>();
    }

    public ArrayList<Vector2d> getObstacles() {
        return obstacles;
    }

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
