package me.autobot.sim;

import me.autobot.code.Robot;
import me.autobot.lib.math.coordinates.Int2;

import java.util.ArrayList;

public class Simulation {
    private Robot robot;

    private int[][] obstacles = new int[][] {
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        { 1, 3, 0, 0, 1, 0, 0, 0, 1, 1},
        { 1, 1, 1, 0, 0, 1, 1, 0, 0, 1},
        { 1, 0, 1, 1, 0, 0, 1, 0, 1, 1},
        { 1, 0, 0, 0, 1, 0, 0, 0, 0, 1},
        { 1, 0, 1, 1, 1, 0, 1, 0, 0, 1},
        { 1, 0, 0, 1, 0, 1, 1, 1, 0, 1},
        { 1, 1, 2, 0, 0, 0, 1, 0, 0, 1},
        { 1, 0, 0, 0, 1, 0, 0, 0, 1, 1},
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    Int2 end = new Int2(1,1);

    private void printObstacles() {
        for (int i = 0; i < obstacles.length; i++) {
            for (int j = 0; j < obstacles[i].length; j++) {
                String l = "   ";

                int o = obstacles[i][j];

                if (o == 1) {
                    l = "███";
                } else if (o == 2) {
                    l = " @ ";
                } else if (o == 3) {
                    l = " X ";
                }
                
                System.out.print(l);
            }
            System.out.println();
        }
    }

    private double f(Int2 node, Int2 start) {
        double g = node.distance(start);
        double h = node.distance(end);

        return (g + h);
    }

    private void pathfind() {
        ArrayList<Int2> openlist = new ArrayList<>();
        ArrayList<Int2> closedList = new ArrayList<>();

        Int2 start = new Int2(1, 1);

        for (int y = 0; y < obstacles.length; y++) {
            for (int x = 0; x < obstacles[y].length; x++) {
                if (obstacles[y][x] == 2) {
                    start = new Int2(x, y);
                }
            }
        }

        openlist.add(start);

        while (!openlist.isEmpty()) {
            //get node with least f
            Int2 current = openlist.get(0);

            for (Int2 node : openlist) {
                if (f(node, start) < f(current, start)) {
                    current = node;
                }
            }

            
        }
    }

    public static void main() {
        Simulation sim = new Simulation();

        sim.printObstacles();
    }
} 
